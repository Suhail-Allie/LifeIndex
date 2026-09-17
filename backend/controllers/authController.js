const bcrypt = require("bcryptjs");
const pool = require("../db");

const {
    generateAccessToken,
    generateRefreshToken,
    verifyRefreshToken
} = require("../jwt");

const { hashToken } = require("../tokenUtils");

const registerUser = async (req, res) => {
    try {
        const { displayName, email, password } = req.body;

        if (!displayName || !email || !password) {
            return res.status(400).json({
                message: "Display name, email and password are required"
            });
        }

        const normalizedEmail = email.toLowerCase();

        const existingUser = await pool.query(
            "SELECT id FROM users WHERE email = $1",
            [normalizedEmail]
        );

        if (existingUser.rows.length > 0) {
            return res.status(409).json({
                message: "An account with this email already exists"
            });
        }

        const passwordHash = await bcrypt.hash(password, 12);

        const result = await pool.query(
            `INSERT INTO users (
                display_name,
                email,
                password_hash
            )
            VALUES ($1, $2, $3)
            RETURNING id, display_name, email, created_at`,
            [
                displayName,
                normalizedEmail,
                passwordHash
            ]
        );

        const user = result.rows[0];

        await pool.query(
            `INSERT INTO user_settings (user_id)
             VALUES ($1)`,
            [user.id]
        );

        const accessToken = generateAccessToken(user.id);
        const refreshToken = generateRefreshToken(user.id);

        const refreshTokenHash = hashToken(refreshToken);

        await pool.query(
            `INSERT INTO refresh_sessions (
                user_id,
                token_hash,
                expires_at
            )
            VALUES ($1, $2, NOW() + INTERVAL '30 days')`,
            [
                user.id,
                refreshTokenHash
            ]
        );

        res.status(201).json({
            message: "User registered successfully",
            accessToken,
            refreshToken,
            user
        });

    } catch (error) {
        console.error("Register error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const loginUser = async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                message: "Email and password are required"
            });
        }

        const result = await pool.query(
            `SELECT
                id,
                display_name,
                email,
                password_hash
             FROM users
             WHERE email = $1`,
            [email.toLowerCase()]
        );

        if (result.rows.length === 0) {
            return res.status(401).json({
                message: "Invalid email or password"
            });
        }

        const user = result.rows[0];

        const passwordMatches = await bcrypt.compare(
            password,
            user.password_hash
        );

        if (!passwordMatches) {
            return res.status(401).json({
                message: "Invalid email or password"
            });
        }

        const accessToken = generateAccessToken(user.id);
        const refreshToken = generateRefreshToken(user.id);

        const refreshTokenHash = hashToken(refreshToken);

        await pool.query(
            `INSERT INTO refresh_sessions (
                user_id,
                token_hash,
                expires_at
            )
            VALUES ($1, $2, NOW() + INTERVAL '30 days')`,
            [
                user.id,
                refreshTokenHash
            ]
        );

        res.json({
            message: "Login successful",
            accessToken,
            refreshToken,
            user: {
                id: user.id,
                display_name: user.display_name,
                email: user.email
            }
        });

    } catch (error) {
        console.error("Login error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const refreshAccessToken = async (req, res) => {
    try {
        const { refreshToken } = req.body;

        if (!refreshToken) {
            return res.status(400).json({
                message: "Refresh token is required"
            });
        }

        const decoded = verifyRefreshToken(refreshToken);

        const tokenHash = hashToken(refreshToken);

        const result = await pool.query(
            `SELECT
                id,
                user_id,
                expires_at,
                revoked_at
             FROM refresh_sessions
             WHERE user_id = $1
             AND token_hash = $2`,
            [
                decoded.userId,
                tokenHash
            ]
        );

        if (result.rows.length === 0) {
            return res.status(401).json({
                message: "Invalid refresh token"
            });
        }

        const session = result.rows[0];

        if (session.revoked_at) {
            return res.status(401).json({
                message: "Refresh token has been revoked"
            });
        }

        if (new Date(session.expires_at) < new Date()) {
            return res.status(401).json({
                message: "Refresh token has expired"
            });
        }

        const newAccessToken = generateAccessToken(
            decoded.userId
        );

        res.json({
            accessToken: newAccessToken
        });

    } catch (error) {
        console.error("Refresh token error:", error);

        return res.status(401).json({
            message: "Invalid refresh token"
        });
    }
};

const logoutUser = async (req, res) => {
    try {
        const { refreshToken } = req.body;

        if (!refreshToken) {
            return res.status(400).json({
                message: "Refresh token is required"
            });
        }

        const tokenHash = hashToken(refreshToken);

        await pool.query(
            `UPDATE refresh_sessions
             SET revoked_at = NOW()
             WHERE token_hash = $1
             AND revoked_at IS NULL`,
            [tokenHash]
        );

        res.json({
            message: "Logout successful"
        });

    } catch (error) {
        console.error("Logout error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    registerUser,
    loginUser,
    refreshAccessToken,
    logoutUser
};