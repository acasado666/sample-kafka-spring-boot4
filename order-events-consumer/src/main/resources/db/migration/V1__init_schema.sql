CREATE TABLE order_event
(
    order_event_id SERIAL PRIMARY KEY,
    event_type     VARCHAR(255)                             NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE              NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP WITHOUT TIME ZONE              NOT NULL DEFAULT now()
);

CREATE TABLE  phone
(
    phone_id           INTEGER                     PRIMARY KEY,
    phone_name         VARCHAR(255)                NOT NULL,
    phone_model        VARCHAR(255)                NOT NULL,
    phone_manufacturer VARCHAR(255)                NOT NULL,
    phone_price        BIGINT                      NOT NULL,
    order_event_id     INTEGER,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT FK_PHONE_ON_ORDER_EVENT FOREIGN KEY (order_event_id) REFERENCES order_event (order_event_id)
);


CREATE TABLE IF NOT EXISTS order_event_failure (
                                     failure_id         BIGSERIAL PRIMARY KEY,
                                     topic              VARCHAR(255) NOT NULL,
                                     partition_id       INTEGER      NOT NULL,
                                     offset_value       BIGINT       NOT NULL,
                                     record_key         VARCHAR(255),
                                     payload            TEXT         NOT NULL,
                                     exception_class    VARCHAR(500) NOT NULL,
                                     exception_message  TEXT,
                                     stack_trace        TEXT         NOT NULL,
                                     failed_at          TIMESTAMP    NOT NULL DEFAULT now()
);