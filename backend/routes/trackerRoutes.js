const express = require("express");

const {
    createTracker,
    getTrackers,
    getTrackerById,
    updateTracker,
    deleteTracker,
    archiveTracker,
    restoreTracker
} = require("../controllers/trackerController");

const authenticateToken = require("../middleware/authMiddleware");

const router = express.Router();

router.post(
    "/",
    authenticateToken,
    createTracker
);

router.get(
    "/",
    authenticateToken,
    getTrackers
);

router.get(
    "/:id",
    authenticateToken,
    getTrackerById
);

router.patch(
    "/:id",
    authenticateToken,
    updateTracker
);

router.delete(
    "/:id",
    authenticateToken,
    deleteTracker
);

router.patch(
    "/:id/archive",
    authenticateToken,
    archiveTracker
);

router.patch(
    "/:id/restore",
    authenticateToken,
    restoreTracker
);

module.exports = router;