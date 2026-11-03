const pool = require("../db");

// Register the FCM destination for the authenticated LifeIndex account.
// A token is unique: signing into another account on the same device moves
// that device token to the newly authenticated account.
const registerDevice = async (req, res) => {
    try {
        const fcmToken =
            typeof req.body.fcmToken === "string"
                ? req.body.fcmToken.trim()
                : "";

        if (!fcmToken || fcmToken.length > 4096) {
            return res.status(400).json({
                message: "A valid FCM device token is required"
            });
        }

        const result = await pool.query(
            `INSERT INTO public.devices (
                user_id,
                fcm_token,
                platform,
                last_seen_at
            )
            VALUES ($1, $2, 'ANDROID', NOW())
            ON CONFLICT (fcm_token)
            DO UPDATE SET
                user_id = EXCLUDED.user_id,
                platform = EXCLUDED.platform,
                last_seen_at = NOW()
            RETURNING id, platform, last_seen_at`,
            [req.user.id, fcmToken]
        );

        console.log(`[FCM] Device registered for user ${req.user.id}`);

        return res.status(200).json({
            message: "Device registered for push notifications",
            device: result.rows[0]
        });
    } catch (error) {
        console.error("Register FCM device error:", error);
        return res.status(500).json({
            message: "Unable to register this device"
        });
    }
};

module.exports = { registerDevice };
