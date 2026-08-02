ALTER TABLE public.order_event
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE public.phone
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();