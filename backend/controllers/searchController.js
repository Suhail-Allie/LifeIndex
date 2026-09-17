const pool = require("../db");

const searchTrackers = async (req, res) => {
    try {
        const {
            query,
            trackerType,
            status,
            categoryId
        } = req.query;

        const values = [req.user.id];
        const conditions = [
            "t.user_id = $1",
            "t.is_archived = FALSE"
        ];

        if (query) {
            values.push(`%${query}%`);
            conditions.push(`t.title ILIKE $${values.length}`);
        }

        if (trackerType) {
            values.push(trackerType);
            conditions.push(`t.tracker_type = $${values.length}`);
        }

        if (status) {
            values.push(status);
            conditions.push(`t.status = $${values.length}`);
        }

        if (categoryId) {
            values.push(categoryId);
            conditions.push(`t.category_id = $${values.length}`);
        }

        const result = await pool.query(
            `SELECT
                t.id,
                t.title,
                t.tracker_type,
                t.status,
                t.priority,
                t.important_date,
                t.category_id,
                c.name AS category_name,
                t.updated_at
             FROM trackers t
             LEFT JOIN categories c
                ON t.category_id = c.id
             WHERE ${conditions.join(" AND ")}
             ORDER BY t.updated_at DESC`,
            values
        );

        res.json({
            trackers: result.rows
        });

    } catch (error) {
        console.error("Search trackers error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    searchTrackers
};