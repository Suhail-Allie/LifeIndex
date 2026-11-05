-- LifeIndex attachment storage. The attachments table already exists in the
-- Part 3 database setup; keep its uploader_id/original_name/size_bytes schema.
BEGIN;

CREATE INDEX IF NOT EXISTS attachments_uploader_created_idx
    ON public.attachments (uploader_id, created_at DESC);

CREATE INDEX IF NOT EXISTS attachments_tracker_id_idx
    ON public.attachments (tracker_id);

ALTER TABLE public.attachments ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE public.attachments FROM anon, authenticated;
GRANT ALL ON TABLE public.attachments TO service_role;

-- Private Supabase Storage bucket. Only the authenticated LifeIndex API,
-- using a server-side secret key, accesses objects and creates signed URLs.
INSERT INTO storage.buckets (
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
)
VALUES (
    'lifeindex-attachments',
    'lifeindex-attachments',
    FALSE,
    10485760,
    ARRAY['application/pdf', 'image/jpeg', 'image/png']::text[]
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    public = FALSE,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

COMMIT;