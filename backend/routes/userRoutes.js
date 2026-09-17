const express = require("express");

const {
    getUserSettings,
    updateUserSettings
} = require("../controllers/userController");

const authenticateToken = require("../middleware/authMiddleware");

const router = express.Router();

router.get(
    "/me/settings",
    authenticateToken,
    getUserSettings
);

router.patch(
    "/me/settings",
    authenticateToken,
    updateUserSettings
);

module.exports = router;