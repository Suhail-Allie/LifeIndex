const pool = require("../db");
const { firestore, FieldValue, messaging } = require("../config/firebase");

// Each account receives its own notification collection.
const getUserNotifications = (userId) =>
    firestore
        .collection("users")
        .doc(userId)
        .collection("notifications");

const allowedTypes = ["REMINDER", "TRACKER", "SYSTEM", "SECURITY", "SYNC"];

const serializeNotification = (document) => {
    const data = document.data();
    return {
        id: document.id,
        title: data.title,
        body: data.body,
        type: data.type,
        trackerId: data.trackerId || null,
        isRead: Boolean(data.isRead),
        createdAt: data.createdAt?.toDate?.().toISOString() || null,
        readAt: data.readAt?.toDate?.().toISOString() || null
    };
};

// Push delivery is deliberately best-effort: a temporary FCM failure must
// never undo a notification that has already been stored in Firestore.
const sendPushToUser = async (userId, notification) => {
    try {
        const deviceResult = await pool.query(
            "SELECT fcm_token FROM public.devices WHERE user_id = $1",
            [userId]
        );

        const tokens = deviceResult.rows
            .map((row) => row.fcm_token)
            .filter(Boolean);

        if (tokens.length === 0) {
            console.log(`[FCM] No registered devices for user ${userId}`);
            return;
        }

        // FCM multicast requests accept up to 500 registration tokens.
        for (let index = 0; index < tokens.length; index += 500) {
            const batchTokens = tokens.slice(index, index + 500);
            const response = await messaging.sendEachForMulticast({
                tokens: batchTokens,
                notification: {
                    title: notification.title,
                    body: notification.body
                },
                data: {
                    notificationId: notification.id,
                    type: notification.type,
                    title: notification.title,
                    body: notification.body,
                    trackerId: notification.trackerId || ""
                },
                android: {
                    priority: "high",
                    notification: {
                        channelId: "lifeindex_notifications"
                    }
                }
            });

            const invalidTokens = [];
            response.responses.forEach((sendResult, tokenIndex) => {
                if (
                    !sendResult.success &&
                    [
                        "messaging/registration-token-not-registered",
                        "messaging/invalid-registration-token"
                    ].includes(sendResult.error?.code)
                ) {
                    invalidTokens.push(batchTokens[tokenIndex]);
                }
            });

            if (invalidTokens.length > 0) {
                await pool.query(
                    "DELETE FROM public.devices WHERE fcm_token = ANY($1::text[])",
                    [invalidTokens]
                );
            }

            console.log(
                `[FCM] Push sent for notification ${notification.id}: ` +
                `${response.successCount} succeeded, ${response.failureCount} failed`
            );
        }
    } catch (error) {
        console.error("FCM push delivery failed:", error);
    }
};

// Create a Firestore notification and then attempt push delivery to this
// account's registered Android devices.
const createNotification = async (req, res) => {
    try {
        const title =
            typeof req.body.title === "string" ? req.body.title.trim() : "";
        const body =
            typeof req.body.body === "string" ? req.body.body.trim() : "";
        const type =
            typeof req.body.type === "string"
                ? req.body.type.trim().toUpperCase()
                : "SYSTEM";
        const trackerId = req.body.trackerId || null;

        if (!title || title.length > 100) {
            return res.status(400).json({
                message: "Title is required and cannot exceed 100 characters"
            });
        }

        if (!body || body.length > 300) {
            return res.status(400).json({
                message: "Notification body is required and cannot exceed 300 characters"
            });
        }

        if (!allowedTypes.includes(type)) {
            return res.status(400).json({ message: "Invalid notification type" });
        }

        if (trackerId) {
            const trackerResult = await pool.query(
                `SELECT id FROM trackers WHERE id = $1 AND user_id = $2`,
                [trackerId, req.user.id]
            );

            if (trackerResult.rows.length === 0) {
                return res.status(404).json({ message: "Tracker not found" });
            }
        }

        const notificationRef = getUserNotifications(req.user.id).doc();
        await notificationRef.set({
            title,
            body,
            type,
            trackerId,
            isRead: false,
            createdAt: FieldValue.serverTimestamp(),
            readAt: null
        });

        const savedNotification = await notificationRef.get();
        const notification = serializeNotification(savedNotification);

        console.log(`[Firestore] Notification created for user ${req.user.id}`);
        await sendPushToUser(req.user.id, notification);

        return res.status(201).json({
            message: "Notification created successfully",
            notification
        });
    } catch (error) {
        console.error("Create notification error:", error);
        return res.status(500).json({
            message: "Unable to create notification"
        });
    }
};

const getNotifications = async (req, res) => {
    try {
        const requestedLimit = Number.parseInt(req.query.limit, 10);
        const limit = Number.isFinite(requestedLimit)
            ? Math.min(Math.max(requestedLimit, 1), 50)
            : 20;

        const snapshot = await getUserNotifications(req.user.id)
            .orderBy("createdAt", "desc")
            .limit(limit)
            .get();

        const notifications = snapshot.docs.map(serializeNotification);
        console.log(
            `[Firestore] Retrieved ${notifications.length} notifications for user ${req.user.id}`
        );

        return res.json({ notifications, count: notifications.length });
    } catch (error) {
        console.error("Get notifications error:", error);
        return res.status(500).json({ message: "Unable to retrieve notifications" });
    }
};

const markNotificationRead = async (req, res) => {
    try {
        const notificationRef = getUserNotifications(req.user.id)
            .doc(req.params.notificationId);
        const notification = await notificationRef.get();

        if (!notification.exists) {
            return res.status(404).json({ message: "Notification not found" });
        }

        await notificationRef.update({
            isRead: true,
            readAt: FieldValue.serverTimestamp()
        });

        return res.json({ message: "Notification marked as read" });
    } catch (error) {
        console.error("Mark notification read error:", error);
        return res.status(500).json({ message: "Unable to update notification" });
    }
};

const markAllNotificationsRead = async (req, res) => {
    try {
        const snapshot = await getUserNotifications(req.user.id)
            .where("isRead", "==", false)
            .get();
        const documents = snapshot.docs;
        let updatedCount = 0;

        for (let index = 0; index < documents.length; index += 450) {
            const batch = firestore.batch();
            const currentBatch = documents.slice(index, index + 450);
            currentBatch.forEach((document) => {
                batch.update(document.ref, {
                    isRead: true,
                    readAt: FieldValue.serverTimestamp()
                });
            });
            await batch.commit();
            updatedCount += currentBatch.length;
        }

        return res.json({
            message: "Notifications marked as read",
            updatedCount
        });
    } catch (error) {
        console.error("Mark all notifications read error:", error);
        return res.status(500).json({ message: "Unable to update notifications" });
    }
};

module.exports = {
    createNotification,
    getNotifications,
    markNotificationRead,
    markAllNotificationsRead
};
