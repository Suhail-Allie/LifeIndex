const pool = require("../db");

const syncTrackers = async (req, res) => {
    const operations = req.body?.operations;
    if (!Array.isArray(operations) || operations.length === 0) {
        return res.status(400).json({ message: "A non-empty operations array is required" });
    }
    if (operations.length > 50) {
        return res.status(400).json({ message: "A maximum of 50 sync operations is allowed per request" });
    }

    const results = [];

    for (const operation of operations) {
        const { id, action, baseVersion, payload } = operation || {};
        if (!id || !["CREATE", "UPDATE", "DELETE"].includes(action)) {
            results.push({ id: id || "unknown", status: "FAILED", message: "Invalid sync operation" });
            continue;
        }

        const client = await pool.connect();
        try {
            await client.query("BEGIN");
            const currentResult = await client.query(
                "SELECT * FROM trackers WHERE id = $1 AND user_id = $2 FOR UPDATE",
                [id, req.user.id]
            );
            const current = currentResult.rows[0];

            if (action === "CREATE") {
                // Idempotency: a retry after a lost HTTP response must not create a duplicate.
                if (current) {
                    await client.query("COMMIT");
                    results.push({ id, status: "SYNCED", tracker: current });
                    continue;
                }
                if (!payload?.title || !["ITEM", "PROCESS", "RECURRING"].includes(payload.trackerType)) {
                    await client.query("ROLLBACK");
                    results.push({ id, status: "FAILED", message: "Tracker title or type is invalid" });
                    continue;
                }

                const inserted = await client.query(
                    `INSERT INTO trackers (
                        id, user_id, template_id, category_id, title, tracker_type,
                        status, priority, important_date, notes, is_archived, version
                    ) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,1)
                    RETURNING *`,
                    [
                        id, req.user.id, payload.templateId || null, payload.categoryId || null,
                        payload.title, payload.trackerType, payload.status || "ACTIVE",
                        payload.priority || "NORMAL", payload.importantDate || null,
                        payload.notes || null, Boolean(payload.isArchived)
                    ]
                );

                if (payload.templateId) {
                    await client.query(
                        `INSERT INTO field_definitions (
                            tracker_id, field_name, field_type, is_required, field_options, display_order
                        )
                        SELECT $1, field_name, field_type, is_required, field_options, display_order
                        FROM template_fields WHERE template_id = $2`,
                        [id, payload.templateId]
                    );
                }

                await client.query("COMMIT");
                results.push({ id, status: "SYNCED", tracker: inserted.rows[0] });
                continue;
            }

            if (!current) {
                await client.query("ROLLBACK");
                results.push({ id, status: "CONFLICT", tracker: null, message: "The server record no longer exists" });
                continue;
            }

            if (Number(current.version) !== Number(baseVersion)) {
                await client.query("ROLLBACK");
                results.push({ id, status: "CONFLICT", tracker: current, message: "The server record was changed on another device" });
                continue;
            }

            if (action === "DELETE") {
                await client.query("DELETE FROM trackers WHERE id = $1 AND user_id = $2", [id, req.user.id]);
                await client.query("COMMIT");
                results.push({ id, status: "SYNCED" });
                continue;
            }

            if (!payload?.title || !["ITEM", "PROCESS", "RECURRING"].includes(payload.trackerType)) {
                await client.query("ROLLBACK");
                results.push({ id, status: "FAILED", message: "Tracker title or type is invalid" });
                continue;
            }

            const updated = await client.query(
                `UPDATE trackers SET
                    title = $1,
                    tracker_type = $2,
                    template_id = $3,
                    category_id = $4,
                    status = $5,
                    priority = $6,
                    important_date = $7,
                    notes = $8,
                    is_archived = $9,
                    version = version + 1,
                    updated_at = NOW()
                 WHERE id = $10 AND user_id = $11 AND version = $12
                 RETURNING *`,
                [
                    payload.title, payload.trackerType, payload.templateId || null,
                    payload.categoryId || null, payload.status || "ACTIVE",
                    payload.priority || "NORMAL", payload.importantDate || null,
                    payload.notes || null, Boolean(payload.isArchived),
                    id, req.user.id, Number(baseVersion)
                ]
            );

            if (updated.rows.length === 0) {
                const latest = await client.query(
                    "SELECT * FROM trackers WHERE id = $1 AND user_id = $2",
                    [id, req.user.id]
                );
                await client.query("ROLLBACK");
                results.push({ id, status: "CONFLICT", tracker: latest.rows[0] || null, message: "The server record changed during synchronisation" });
                continue;
            }

            await client.query("COMMIT");
            results.push({ id, status: "SYNCED", tracker: updated.rows[0] });

        } catch (error) {
            try { await client.query("ROLLBACK"); } catch (_) { /* transaction may already be closed */ }
            console.error("Tracker sync operation failed:", { id, action, message: error.message });
            results.push({ id, status: "FAILED", message: "The server could not process this tracker change" });
        } finally {
            client.release();
        }
    }

    return res.json({ results });
};

module.exports = { syncTrackers };