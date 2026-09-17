const pool = require("../db");

const createTracker = async (req, res) => {
    try {
        const {
            title,
            trackerType,
            templateId,
            categoryId,
            status,
            priority,
            importantDate,
            notes
        } = req.body;

        if (!title || !trackerType) {
            return res.status(400).json({
                message: "Title and tracker type are required"
            });
        }

        const validTrackerTypes = ["ITEM", "PROCESS", "RECURRING"];

        if (!validTrackerTypes.includes(trackerType)) {
            return res.status(400).json({
                message: "Invalid tracker type"
            });
        }

        let finalCategoryId = categoryId || null;

        if (templateId && !finalCategoryId) {
            const templateResult = await pool.query(
                `SELECT category_id
                 FROM templates
                 WHERE id = $1`,
                [templateId]
            );

            if (templateResult.rows.length > 0) {
                finalCategoryId = templateResult.rows[0].category_id;
            }
        }

        const result = await pool.query(
            `INSERT INTO trackers (
                user_id,
                template_id,
                category_id,
                title,
                tracker_type,
                status,
                priority,
                important_date,
                notes
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
            RETURNING *`,
            [
                req.user.id,
                templateId || null,
                finalCategoryId,
                title,
                trackerType,
                status || "ACTIVE",
                priority || "NORMAL",
                importantDate || null,
                notes || null
            ]
        );

        if (templateId) {
            await pool.query(
                `INSERT INTO field_definitions (
                    tracker_id,
                    field_name,
                    field_type,
                    is_required,
                    field_options,
                    display_order
                )
                SELECT
                    $1,
                    field_name,
                    field_type,
                    is_required,
                    field_options,
                    display_order
                FROM template_fields
                WHERE template_id = $2`,
                [result.rows[0].id, templateId]
            );
        }

        res.status(201).json({
            message: "Tracker created successfully",
            tracker: result.rows[0]
        });

    } catch (error) {
        console.error("Create tracker error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const getTrackers = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                id,
                title,
                tracker_type,
                status,
                priority,
                important_date,
                notes,
                category_id,
                template_id,
                is_archived,
                created_at,
                updated_at
             FROM trackers
             WHERE user_id = $1
             AND is_archived = FALSE
             ORDER BY updated_at DESC`,
            [req.user.id]
        );

        res.json({
            trackers: result.rows
        });

    } catch (error) {
        console.error("Get trackers error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const getTrackerById = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                id,
                title,
                tracker_type,
                status,
                priority,
                important_date,
                notes,
                category_id,
                template_id,
                is_archived,
                created_at,
                updated_at
             FROM trackers
             WHERE id = $1
             AND user_id = $2`,
            [req.params.id, req.user.id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "Tracker not found"
            });
        }

        res.json({
            tracker: result.rows[0]
        });

    } catch (error) {
        console.error("Get tracker error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const updateTracker = async (req, res) => {
    try {
        const {
            title,
            trackerType,
            categoryId,
            status,
            priority,
            importantDate,
            notes
        } = req.body;

        if (trackerType) {
            const validTrackerTypes = ["ITEM", "PROCESS", "RECURRING"];

            if (!validTrackerTypes.includes(trackerType)) {
                return res.status(400).json({
                    message: "Invalid tracker type"
                });
            }
        }

        const result = await pool.query(
            `UPDATE trackers
             SET
                 title = COALESCE($1, title),
                 tracker_type = COALESCE($2, tracker_type),
                 category_id = COALESCE($3, category_id),
                 status = COALESCE($4, status),
                 priority = COALESCE($5, priority),
                 important_date = COALESCE($6, important_date),
                 notes = COALESCE($7, notes),
                 updated_at = NOW()
             WHERE id = $8
             AND user_id = $9
             RETURNING *`,
            [
                title,
                trackerType,
                categoryId,
                status,
                priority,
                importantDate,
                notes,
                req.params.id,
                req.user.id
            ]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "Tracker not found"
            });
        }

        res.json({
            message: "Tracker updated successfully",
            tracker: result.rows[0]
        });

    } catch (error) {
        console.error("Update tracker error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const deleteTracker = async (req, res) => {
    try {
        const result = await pool.query(
            `DELETE FROM trackers
             WHERE id = $1
             AND user_id = $2
             RETURNING id`,
            [req.params.id, req.user.id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "Tracker not found"
            });
        }

        res.json({
            message: "Tracker deleted successfully"
        });

    } catch (error) {
        console.error("Delete tracker error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const archiveTracker = async (req, res) => {
    try {
        const result = await pool.query(
            `UPDATE trackers
             SET is_archived = TRUE,
                 updated_at = NOW()
             WHERE id = $1
             AND user_id = $2
             RETURNING id, title, is_archived`,
            [req.params.id, req.user.id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "Tracker not found"
            });
        }

        res.json({
            message: "Tracker archived successfully",
            tracker: result.rows[0]
        });

    } catch (error) {
        console.error("Archive tracker error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const restoreTracker = async (req, res) => {
    try {
        const result = await pool.query(
            `UPDATE trackers
             SET is_archived = FALSE,
                 updated_at = NOW()
             WHERE id = $1
             AND user_id = $2
             RETURNING id, title, is_archived`,
            [req.params.id, req.user.id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "Tracker not found"
            });
        }

        res.json({
            message: "Tracker restored successfully",
            tracker: result.rows[0]
        });

    } catch (error) {
        console.error("Restore tracker error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    createTracker,
    getTrackers,
    getTrackerById,
    updateTracker,
    deleteTracker,
    archiveTracker,
    restoreTracker
};