const express = require("express");

const {
    searchTrackers
} = require("../controllers/searchController");

const authenticateToken = require("../middleware/authMiddleware");

const router = express.Router();

router.get(
    "/trackers",
    authenticateToken,
    searchTrackers
);

module.exports = router;