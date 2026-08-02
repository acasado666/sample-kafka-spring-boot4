-- auto-generated definition
-- create schema public;

-- comment on schema public is 'standard public schema';
--
-- alter schema public owner to pg_database_owner;
--
-- grant usage on schema public to public;
-- grant create on schema public to public;


CREATE TABLE public.order_event (
    order_event_id      SERIAL PRIMARY KEY,
    event_type          VARCHAR(255) DEFAULT 'ADD' CHECK (event_type IN ('ADD', 'UPDATE'))
);

CREATE TABLE public.phone (
    phone_id                INTEGER      PRIMARY KEY,
    phone_name              VARCHAR(255) NOT NULL,
    phone_model             VARCHAR(255) NOT NULL,
    phone_price             NUMERIC(10, 2) NOT NULL,
    phone_manufacturer      VARCHAR(255) NOT NULL,
    order_event_id          INTEGER,
    CONSTRAINT fk_phone_order_event
        FOREIGN KEY (order_event_id)
        REFERENCES  public.order_event (order_event_id)
);

