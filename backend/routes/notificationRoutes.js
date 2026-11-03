
const express = require("express");

const authenticateToken = require("../middleware/authMiddleware");

const {
    createNotification,
    getNotifications,
    markNotificationRead,
    markAllNotificationsRead
} = require("../controllers/notificationController");

const router = express.Router();

// All notification operations require an authenticated account.
router.use(authenticateToken);

router.post("/", createNotification);

router.get("/", getNotifications);

// Keep this route before the parameterised notification route.
router.patch("/read-all", markAllNotificationsRead);

router.patch("/:notificationId/read", markNotificationRead);

module.exports = router;
