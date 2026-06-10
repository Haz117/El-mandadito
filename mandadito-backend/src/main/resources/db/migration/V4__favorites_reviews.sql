-- Phase 5: Favorites and Reviews

CREATE TABLE IF NOT EXISTS favorites (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id BIGINT    NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_favorites UNIQUE (user_id, restaurant_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);

CREATE TABLE IF NOT EXISTS reviews (
    id                BIGSERIAL        PRIMARY KEY,
    order_id          BIGINT           NOT NULL REFERENCES orders(id),
    user_id           BIGINT           NOT NULL REFERENCES users(id),
    restaurant_id     BIGINT           NOT NULL REFERENCES restaurants(id),
    driver_id         BIGINT           REFERENCES users(id),
    restaurant_rating INTEGER          NOT NULL,
    driver_rating     INTEGER,
    comment           VARCHAR(1000),
    created_at        TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_review_order_user UNIQUE (order_id, user_id),
    CONSTRAINT chk_restaurant_rating CHECK (restaurant_rating BETWEEN 1 AND 5),
    CONSTRAINT chk_driver_rating     CHECK (driver_rating IS NULL OR driver_rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_id ON reviews(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_reviews_driver_id     ON reviews(driver_id);
