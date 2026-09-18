const dns = require("dns");
dns.setDefaultResultOrder("ipv4first");

const express = require("express");
const cors = require("cors");
const { Pool } = require("pg");
require("dotenv").config();

const authRoutes = require("./routes/authRoutes");
const userRoutes = require("./routes/userRoutes");
const trackerRoutes = require("./routes/trackerRoutes");
const categoryRoutes = require("./routes/categoryRoutes");
const templateRoutes = require("./routes/templateRoutes");
const fieldRoutes = require("./routes/fieldRoutes");
const dashboardRoutes = require("./routes/dashboardRoutes");
const searchRoutes = require("./routes/searchRoutes");

const pool = new Pool({
    connectionString: process.env.DATABASE_URL
});

const app = express();

const PORT = process.env.PORT || 3000;
const HOST = "0.0.0.0";

// Middleware
app.use(cors());
app.use(express.json());

// API routes
app.use("/api/v1/auth", authRoutes);
app.use("/api/v1/users", userRoutes);
app.use("/api/v1/trackers", trackerRoutes);
app.use("/api/v1/categories", categoryRoutes);
app.use("/api/v1/templates", templateRoutes);
app.use("/api/v1", fieldRoutes);
app.use("/api/v1/dashboard", dashboardRoutes);
app.use("/api/v1/search", searchRoutes);

// Root endpoint
app.get("/", (req, res) => {
    res.json({
        message: "LifeIndex API is running"
    });
});

// Health check
app.get("/api/v1/health", (req, res) => {
    res.json({
        status: "ok",
        service: "LifeIndex API"
    });
});

// Database connection test
app.get("/api/v1/db-test", async (req, res) => {
    try {
        const result = await pool.query("SELECT NOW()");

        res.json({
            status: "ok",
            database: "connected",
            time: result.rows[0].now
        });
    } catch (error) {
        console.error("Database connection error:", {
            message: error.message,
            code: error.code,
            detail: error.detail,
            hint: error.hint
        });

        res.status(500).json({
            status: "error",
            database: "connection failed"
        });
    }
});

// Start server
app.listen(PORT, HOST, () => {
    console.log(`LifeIndex API running on port ${PORT}`);
});