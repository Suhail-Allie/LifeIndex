const pool = require("../db");

const getTrackerFields = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT
                fd.id,
                fd.field_name,
                fd.field_type,
                fd.is_required,
                fd.field_options,
                fd.display_order,
                fv.text_value,
                fv.number_value,
                fv.date_value,
                fv.boolean_value,
                fv.json_value
             FROM field_definitions fd
             INNER JOIN trackers t
                ON fd.tracker_id = t.id
             LEFT JOIN field_values fv
                ON fd.id = fv.field_definition_id
             WHERE fd.tracker_id = $1
             AND t.user_id = $2
             ORDER BY fd.display_order ASC`,
            [req.params.id, req.user.id]
        );

        res.json({
            fields: result.rows
        });

    } catch (error) {
        console.error("Get tracker fields error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

const updateTrackerFields = async (req, res) => {
    try {
        const { fields } = req.body;

        if (!Array.isArray(fields)) {
            return res.status(400).json({
                message: "Fields must be an array"
            });
        }

        const trackerCheck = await pool.query(
            `SELECT id
             FROM trackers
             WHERE id = $1
             AND user_id = $2`,
            [req.params.id, req.user.id]
        );

        if (trackerCheck.rows.length === 0) {
            return res.status(404).json({
                message: "Tracker not found"
            });
        }

        for (const field of fields) {
            const {
                fieldDefinitionId,
                textValue,
                numberValue,
                dateValue,
                booleanValue,
                jsonValue
            } = field;

            await pool.query(
                `INSERT INTO field_values (
                    tracker_id,
                    field_definition_id,
                    text_value,
                    number_value,
                    date_value,
                    boolean_value,
                    json_value
                )
                VALUES ($1, $2, $3, $4, $5, $6, $7)
                ON CONFLICT (tracker_id, field_definition_id)
                DO UPDATE SET
                    text_value = EXCLUDED.text_value,
                    number_value = EXCLUDED.number_value,
                    date_value = EXCLUDED.date_value,
                    boolean_value = EXCLUDED.boolean_value,
                    json_value = EXCLUDED.json_value,
                    updated_at = NOW()`,
                [
                    req.params.id,
                    fieldDefinitionId,
                    textValue ?? null,
                    numberValue ?? null,
                    dateValue ?? null,
                    booleanValue ?? null,
                    jsonValue ?? null
                ]
            );
        }

        res.json({
            message: "Tracker fields updated successfully"
        });

    } catch (error) {
        console.error("Update tracker fields error:", error);

        res.status(500).json({
            message: "Server error"
        });
    }
};

module.exports = {
    getTrackerFields,
    updateTrackerFields
};