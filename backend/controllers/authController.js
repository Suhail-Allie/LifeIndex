const bcrypt = require("bcryptjs");
const crypto = require("crypto");
const { OAuth2Client } = require("google-auth-library");
const googleOAuthClient = new OAuth2Client();
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


// Verify a Google ID token, link it to an existing email account when safe,
// and issue the same LifeIndex JWT session used by password authentication.
const googleLogin = async (req, res) => {
    let client;

    try {
        const { idToken } = req.body || {};

        if (typeof idToken !== "string" || !idToken.trim()) {
            return res.status(400).json({
                message: "Google ID token is required"
            });
        }

        const webClientId = process.env.GOOGLE_WEB_CLIENT_ID;

        if (!webClientId) {
            console.error("Google sign-in is missing GOOGLE_WEB_CLIENT_ID");
            return res.status(503).json({
                message: "Google sign-in is not configured"
            });
        }

        const ticket = await googleOAuthClient.verifyIdToken({
            idToken: idToken.trim(),
            audience: webClientId
        });

        const payload = ticket.getPayload();

        if (!payload || !payload.sub || !payload.email || payload.email_verified !== true) {
            return res.status(401).json({
                message: "Google account could not be verified"
            });
        }

        const googleSub = payload.sub;
        const normalizedEmail = payload.email.toLowerCase();

        const proposedName = (
            payload.name ||
            normalizedEmail.split("@")[0] ||
            "Google User"
        ).trim().slice(0, 80);

        const displayName = proposedName.length >= 2
            ? proposedName
            : "Google User";

        client = await pool.connect();
        await client.query("BEGIN");

        const matches = await client.query(
            `SELECT id, display_name, email, google_sub
             FROM users
             WHERE LOWER(email) = $1 OR google_sub = $2
             FOR UPDATE`,
            [normalizedEmail, googleSub]
        );

        if (matches.rows.length > 1) {
            await client.query("ROLLBACK");

            return res.status(409).json({
                message: "This Google identity conflicts with more than one LifeIndex account"
            });
        }

        let user;

        if (matches.rows.length === 1) {
            const existing = matches.rows[0];

            if (existing.google_sub && existing.google_sub !== googleSub) {
                await client.query("ROLLBACK");

                return res.status(409).json({
                    message: "This email is already linked to a different Google account"
                });
            }

            const linked = await client.query(
                `UPDATE users
                 SET google_sub = COALESCE(google_sub, $1)
                 WHERE id = $2
                 RETURNING id, display_name, email`,
                [googleSub, existing.id]
            );

            user = linked.rows[0];
        } else {
            // Google-only users receive an unusable random password hash to satisfy
            // the existing NOT NULL password_hash column without exposing a password.
            const randomPassword = crypto.randomBytes(48).toString("hex");
            const passwordHash = await bcrypt.hash(randomPassword, 12);

            const inserted = await client.query(
                `INSERT INTO users (display_name, email, password_hash, google_sub)
                 VALUES ($1, $2, $3, $4)
                 RETURNING id, display_name, email`,
                [displayName, normalizedEmail, passwordHash, googleSub]
            );

            user = inserted.rows[0];
        }

        await client.query(
            `INSERT INTO user_settings (user_id)
             VALUES ($1)
             ON CONFLICT (user_id) DO NOTHING`,
            [user.id]
        );

        const accessToken = generateAccessToken(user.id);
        const refreshToken = generateRefreshToken(user.id);
        const refreshTokenHash = hashToken(refreshToken);

        await client.query(
            `INSERT INTO refresh_sessions (user_id, token_hash, expires_at)
             VALUES ($1, $2, NOW() + INTERVAL '30 days')`,
            [user.id, refreshTokenHash]
        );

        await client.query("COMMIT");

        return res.json({
            message: "Google login successful",
            accessToken,
            refreshToken,
            user
        });

    } catch (error) {
        if (client) {
            try {
                await client.query("ROLLBACK");
            } catch (rollbackError) {
                console.error("Google login rollback error:", rollbackError);
            }
        }

        console.error("Google login error:", error);

        return res.status(401).json({
            message: "Google sign-in could not be verified"
        });

    } finally {
        if (client) {
            client.release();
        }
    }
};
module.exports = {
    registerUser,
    loginUser,
    googleLogin,
    refreshAccessToken,
    logoutUser
};