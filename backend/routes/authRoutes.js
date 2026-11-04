const express = require("express");

const {
    registerUser,
    loginUser,
    googleLogin,
    refreshAccessToken,
    logoutUser
} = require("../controllers/authController");

const router = express.Router();

router.post("/register", registerUser);
router.post("/login", loginUser);
router.post("/google", googleLogin);
router.post("/refresh", refreshAccessToken);
router.post("/logout", logoutUser);

module.exports = router;