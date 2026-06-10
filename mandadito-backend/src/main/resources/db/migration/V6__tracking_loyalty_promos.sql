-- Phase 7: Tracking, Loyalty Points, Promotions

CREATE TABLE IF NOT EXISTS order_events (
    id         BIGSERIAL        PRIMARY KEY,
    order_id   BIGINT           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status     VARCHAR(30)      NOT NULL,
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    note       VARCHAR(300),
    created_at TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_events_order_id ON order_events(order_id);

CREATE TABLE IF NOT EXISTS loyalty_points (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points      INTEGER   NOT NULL,
    type        VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    order_id    BIGINT    REFERENCES orders(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_loyalty_points_user_id ON loyalty_points(user_id);

CREATE TABLE IF NOT EXISTS promotions (
    id             BIGSERIAL        PRIMARY KEY,
    title          VARCHAR(200)     NOT NULL,
    description    VARCHAR(1000),
    image_url      VARCHAR(500),
    discount_type  VARCHAR(20)      NOT NULL,
    discount_value DOUBLE PRECISION NOT NULL,
    start_date     DATE,
    end_date       DATE,
    active         BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_promotions_active ON promotions(active);
