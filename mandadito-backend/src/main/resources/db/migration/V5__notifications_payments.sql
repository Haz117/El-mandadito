-- Phase 6: Device Tokens and Payments

CREATE TABLE IF NOT EXISTS device_tokens (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token        VARCHAR(500) UNIQUE NOT NULL,
    platform     VARCHAR(20)  NOT NULL DEFAULT 'ANDROID',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON device_tokens(user_id);

CREATE TABLE IF NOT EXISTS payments (
    id         BIGSERIAL        PRIMARY KEY,
    order_id   BIGINT           NOT NULL REFERENCES orders(id),
    user_id    BIGINT           NOT NULL REFERENCES users(id),
    amount     DOUBLE PRECISION NOT NULL,
    method     VARCHAR(20)      NOT NULL,
    status     VARCHAR(20)      NOT NULL DEFAULT 'PENDING',
    reference  VARCHAR(200),
    notes      VARCHAR(500),
    created_at TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id  ON payments(user_id);
