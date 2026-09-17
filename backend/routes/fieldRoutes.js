const express = require("express");

const {
    getTrackerFields,
    updateTrackerFields
} = require("../controllers/fieldController");

const authenticateToken = require("../middleware/authMiddleware");

const router = express.Router();

router.get(
    "/trackers/:id/fields",
    authenticateToken,
    getTrackerFields
);

router.patch(
    "/trackers/:id/fields",
    authenticateToken,
    updateTrackerFields
);

module.exports = router;