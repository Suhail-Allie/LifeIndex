const pool = require("../db");

const getUserSettings = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT theme, notifications_enabled, default_reminder_minutes
             FROM user_settings
             WHERE user_id = $1`,
            [req.user.id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "User settings not found"
            });
        }

        res.json({
            settings: result.rows[0]
        });

    } catch (error) {
        console.error("Get settings error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const updateUserSettings = async (req, res) => {
    try {
        const {
            theme,
            notificationsEnabled,
            defaultReminderMinutes
        } = req.body;

        const result = await pool.query(
            `UPDATE user_settings
             SET
                 theme = COALESCE($1, theme),
                 notifications_enabled = COALESCE($2, notifications_enabled),
                 default_reminder_minutes = COALESCE($3, default_reminder_minutes),
                 updated_at = NOW()
             WHERE user_id = $4
             RETURNING theme, notifications_enabled, default_reminder_minutes`,
            [
                theme,
                notificationsEnabled,
                defaultReminderMinutes,
                req.user.id
            ]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({
                message: "User settings not found"
            });
        }

        res.json({
            message: "Settings updated successfully",
            settings: result.rows[0]
        });

    } catch (error) {
        console.error("Update settings error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    getUserSettings,
    updateUserSettings
};