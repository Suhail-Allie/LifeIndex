const pool = require("../db");

const getCategories = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT id, name, description
             FROM categories
             ORDER BY name ASC`
        );

        res.json({
            categories: result.rows
        });

    } catch (error) {
        console.error("Get categories error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    getCategories
};