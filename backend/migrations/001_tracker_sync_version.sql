-- LifeIndex offline-first tracker synchronisation
-- Safe to run more than once.
ALTER TABLE public.trackers
    ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;

UPDATE public.trackers
SET version = 1
WHERE version IS NULL;