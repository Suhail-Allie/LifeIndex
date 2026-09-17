const pool = require("../db");

const getTemplates = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                t.id,
                t.name,
                t.description,
                t.tracker_type,
                t.category_id,
                c.name AS category_name
             FROM templates t
             LEFT JOIN categories c
                ON t.category_id = c.id
             ORDER BY t.name ASC`
        );

        res.json({
            templates: result.rows
        });

    } catch (error) {
        console.error("Get templates error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const getTemplateFields = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                id,
                template_id,
                field_name,
                field_type,
                is_required,
                field_options,
                display_order
             FROM template_fields
             WHERE template_id = $1
             ORDER BY display_order ASC`,
            [req.params.id]
        );

        res.json({
            fields: result.rows
        });

    } catch (error) {
        console.error("Get template fields error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    getTemplates,
    getTemplateFields
};