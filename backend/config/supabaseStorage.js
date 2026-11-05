const { createClient } = require("@supabase/supabase-js");

let cachedClient = null;

const getSupabaseStorageClient = () => {
    const url = process.env.SUPABASE_URL;
    // Prefer the current Supabase secret key. The legacy name is retained only
    // to support projects that have not yet migrated their API keys.
    const secretKey =
        process.env.SUPABASE_SECRET_KEY || process.env.SUPABASE_SERVICE_ROLE_KEY;

    if (!url || !secretKey) {
        throw new Error(
            "Supabase Storage is not configured. Set SUPABASE_URL and SUPABASE_SECRET_KEY on the backend."
        );
    }

    if (!cachedClient) {
        cachedClient = createClient(url, secretKey, {
            auth: {
                autoRefreshToken: false,
                persistSession: false,
                detectSessionInUrl: false
            }
        });
    }

    return cachedClient;
};

module.exports = { getSupabaseStorageClient };