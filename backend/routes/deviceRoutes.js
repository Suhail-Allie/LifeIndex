const express = require("express");
const authenticateToken = require("../middleware/authMiddleware");
const { registerDevice } = require("../controllers/deviceController");

const router = express.Router();

router.use(authenticateToken);
router.post("/register", registerDevice);

module.exports = router;
