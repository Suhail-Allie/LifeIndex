const express = require("express");
const authenticateToken = require("../middleware/authMiddleware");
const {
    receiveAttachmentFile,
    uploadAttachment,
    listAttachments,
    getAttachmentDownloadUrl,
    deleteAttachment
} = require("../controllers/attachmentController");

const router = express.Router();

router.post(
    "/trackers/:trackerId/attachments",
    authenticateToken,
    receiveAttachmentFile,
    uploadAttachment
);

router.get("/attachments", authenticateToken, listAttachments);
router.get("/attachments/:id/download", authenticateToken, getAttachmentDownloadUrl);
router.delete("/attachments/:id", authenticateToken, deleteAttachment);

module.exports = router;