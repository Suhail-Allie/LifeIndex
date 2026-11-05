const crypto = require("crypto");
const path = require("path");
const multer = require("multer");
const pool = require("../db");
const { getSupabaseStorageClient } = require("../config/supabaseStorage");

const BUCKET = "lifeindex-attachments";
const MAX_FILE_SIZE = 10 * 1024 * 1024;
const MIME_EXTENSIONS = {
    "application/pdf": "pdf",
    "image/jpeg": "jpg",
    "image/png": "png"
};

const receiveFile = multer({
    storage: multer.memoryStorage(),
    limits: { fileSize: MAX_FILE_SIZE, files: 1 },
    fileFilter: (_req, file, callback) => {
        if (!Object.prototype.hasOwnProperty.call(MIME_EXTENSIONS, file.mimetype)) {
            return callback(new Error("UNSUPPORTED_MIME_TYPE"));
        }
        return callback(null, true);
    }
});

const receiveAttachmentFile = (req, res, next) => {
    receiveFile.single("file")(req, res, (error) => {
        if (!error) return next();

        if (error.code === "LIMIT_FILE_SIZE") {
            return res.status(413).json({
                code: "FILE_TOO_LARGE",
                message: "Files must be 10 MB or smaller"
            });
        }

        if (error.message === "UNSUPPORTED_MIME_TYPE") {
            return res.status(415).json({
                code: "UNSUPPORTED_FILE_TYPE",
                message: "Only PDF, JPG and PNG files are supported"
            });
        }

        console.warn("Attachment upload rejected:", error.message);
        return res.status(400).json({ message: "Unable to read the uploaded file" });
    });
};

const hasValidFileSignature = (file) => {
    const buffer = file.buffer;
    if (!buffer || buffer.length < 8) return false;

    if (file.mimetype === "application/pdf") {
        return buffer.subarray(0, 5).toString("ascii") === "%PDF-";
    }

    if (file.mimetype === "image/jpeg") {
        return buffer[0] === 0xff && buffer[1] === 0xd8 && buffer[2] === 0xff;
    }

    if (file.mimetype === "image/png") {
        return buffer.subarray(0, 8).equals(
            Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a])
        );
    }

    return false;
};

const createSafeFileName = (originalName, extension) => {
    const baseName = path.basename(String(originalName || "document").replace(/\\/g, "/"));
    const withoutExtension = baseName.replace(/\.[^.]*$/, "");
    const safeBase = withoutExtension
        .normalize("NFKD")
        .replace(/[^a-zA-Z0-9_-]+/g, "_")
        .replace(/^_+|_+$/g, "")
        .slice(0, 100) || "document";
    return `${safeBase}.${extension}`;
};

// The existing Part 3 schema uses uploader_id, original_name and size_bytes.
// storage_bucket is read per row so legacy attachments can retain their bucket.
const getAttachmentForUser = async (attachmentId, userId) => {
    const result = await pool.query(
        `SELECT id, tracker_id, original_name AS file_name, storage_path,
                storage_bucket, mime_type, size_bytes AS file_size, created_at
         FROM public.attachments
         WHERE id = $1 AND uploader_id = $2`,
        [attachmentId, userId]
    );
    return result.rows[0] || null;
};

const uploadAttachment = async (req, res) => {
    let storageClient = null;

    try {
        if (!req.file) {
            return res.status(400).json({ message: "Choose a file to upload" });
        }

        if (!hasValidFileSignature(req.file)) {
            return res.status(415).json({
                code: "INVALID_FILE_CONTENT",
                message: "The selected file does not match its file type"
            });
        }

        const trackerResult = await pool.query(
            `SELECT id, title
             FROM public.trackers
             WHERE id = $1 AND user_id = $2`,
            [req.params.trackerId, req.user.id]
        );

        if (trackerResult.rows.length === 0) {
            return res.status(404).json({ message: "Tracker not found" });
        }

        const extension = MIME_EXTENSIONS[req.file.mimetype];
        const fileName = createSafeFileName(req.file.originalname, extension);
        const storagePath = `${req.user.id}/${req.params.trackerId}/${crypto.randomUUID()}-${fileName}`;
        storageClient = getSupabaseStorageClient();

        const { error: uploadError } = await storageClient.storage
            .from(BUCKET)
            .upload(storagePath, req.file.buffer, {
                contentType: req.file.mimetype,
                cacheControl: "3600",
                upsert: false
            });

        if (uploadError) throw uploadError;

        try {
            const result = await pool.query(
                `INSERT INTO public.attachments (
                    uploader_id, tracker_id, original_name, storage_path,
                    storage_bucket, mime_type, size_bytes
                 )
                 VALUES ($1, $2, $3, $4, $5, $6, $7)
                 RETURNING id, tracker_id, original_name AS file_name,
                           mime_type, size_bytes AS file_size, created_at`,
                [
                    req.user.id,
                    req.params.trackerId,
                    fileName,
                    storagePath,
                    BUCKET,
                    req.file.mimetype,
                    req.file.size
                ]
            );

            const attachment = result.rows[0];
            return res.status(201).json({
                message: "Document uploaded successfully",
                attachment: {
                    ...attachment,
                    tracker_title: trackerResult.rows[0].title
                }
            });
        } catch (databaseError) {
            const { error: cleanupError } = await storageClient.storage
                .from(BUCKET)
                .remove([storagePath]);
            if (cleanupError) {
                console.error("Could not clean up an attachment after a database error:", cleanupError);
            }
            throw databaseError;
        }
    } catch (error) {
        console.error("Upload attachment error:", error);
        return res.status(500).json({ message: "Unable to upload document" });
    }
};

const listAttachments = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                a.id,
                a.tracker_id,
                t.title AS tracker_title,
                a.original_name AS file_name,
                a.mime_type,
                a.size_bytes AS file_size,
                a.created_at
             FROM public.attachments a
             INNER JOIN public.trackers t ON t.id = a.tracker_id
             WHERE a.uploader_id = $1 AND t.user_id = $1
             ORDER BY a.created_at DESC`,
            [req.user.id]
        );

        return res.json({ attachments: result.rows, count: result.rows.length });
    } catch (error) {
        console.error("List attachments error:", error);
        return res.status(500).json({ message: "Unable to load documents" });
    }
};

const getAttachmentDownloadUrl = async (req, res) => {
    try {
        const attachment = await getAttachmentForUser(req.params.id, req.user.id);
        if (!attachment) {
            return res.status(404).json({ message: "Document not found" });
        }

        const { data, error } = await getSupabaseStorageClient()
            .storage
            .from(attachment.storage_bucket)
            .createSignedUrl(attachment.storage_path, 600, { download: attachment.file_name });

        if (error) throw error;
        return res.json({ downloadUrl: data.signedUrl, expiresIn: 600 });
    } catch (error) {
        console.error("Create attachment download URL error:", error);
        return res.status(500).json({ message: "Unable to prepare document download" });
    }
};

const deleteAttachment = async (req, res) => {
    try {
        const attachment = await getAttachmentForUser(req.params.id, req.user.id);
        if (!attachment) {
            return res.status(404).json({ message: "Document not found" });
        }

        const { error: storageError } = await getSupabaseStorageClient()
            .storage
            .from(attachment.storage_bucket)
            .remove([attachment.storage_path]);

        if (storageError) throw storageError;

        await pool.query(
            "DELETE FROM public.attachments WHERE id = $1 AND uploader_id = $2",
            [req.params.id, req.user.id]
        );

        return res.json({ message: "Document deleted successfully" });
    } catch (error) {
        console.error("Delete attachment error:", error);
        return res.status(500).json({ message: "Unable to delete document" });
    }
};

module.exports = {
    receiveAttachmentFile,
    uploadAttachment,
    listAttachments,
    getAttachmentDownloadUrl,
    deleteAttachment
};