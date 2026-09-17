const pool = require("../db");

const getDashboard = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                id,
                title,
                tracker_type,
                status,
                priority,
                important_date,
                updated_at
             FROM trackers
             WHERE user_id = $1
             AND is_archived = FALSE
             ORDER BY important_date ASC NULLS LAST`,
            [req.user.id]
        );

        const trackers = result.rows;

        const now = new Date();

        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);

        const endOfToday = new Date();
        endOfToday.setHours(23, 59, 59, 999);

        const dueToday = trackers.filter((tracker) => {
            if (!tracker.important_date) {
                return false;
            }

            const date = new Date(tracker.important_date);

            return date >= startOfToday && date <= endOfToday;
        });

        const upcoming = trackers.filter((tracker) => {
            if (!tracker.important_date) {
                return false;
            }

            const date = new Date(tracker.important_date);

            return date > endOfToday;
        });

        const overdue = trackers.filter((tracker) => {
            if (!tracker.important_date) {
                return false;
            }

            const date = new Date(tracker.important_date);

            return date < startOfToday;
        });

        const recentlyUpdated = [...trackers]
            .sort(
                (a, b) =>
                    new Date(b.updated_at) - new Date(a.updated_at)
            )
            .slice(0, 5);

        res.json({
            summary: {
                total: trackers.length,
                dueToday: dueToday.length,
                upcoming: upcoming.length,
                overdue: overdue.length
            },
            dueToday,
            upcoming,
            overdue,
            recentlyUpdated
        });

    } catch (error) {
        console.error("Get dashboard error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    getDashboard
};