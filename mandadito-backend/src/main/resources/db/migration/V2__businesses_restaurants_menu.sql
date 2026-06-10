-- Phase 2: Businesses, Restaurants, Menu Items

CREATE TABLE IF NOT EXISTS businesses (
    id          BIGSERIAL    PRIMARY KEY,
    owner_id    BIGINT       NOT NULL REFERENCES users(id),
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    logo_url    VARCHAR(500),
    phone       VARCHAR(20),
    email       VARCHAR(255),
    address     VARCHAR(300),
    city        VARCHAR(100),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_businesses_owner_id ON businesses(owner_id);
CREATE INDEX IF NOT EXISTS idx_businesses_status   ON businesses(status);

CREATE TABLE IF NOT EXISTS restaurants (
    id                BIGSERIAL        PRIMARY KEY,
    business_id       BIGINT           NOT NULL REFERENCES businesses(id),
    name              VARCHAR(200)     NOT NULL,
    description       VARCHAR(1000),
    category          VARCHAR(50),
    image_url         VARCHAR(500),
    cover_image_url   VARCHAR(500),
    rating            DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_ratings     INTEGER          NOT NULL DEFAULT 0,
    delivery_time_min INTEGER          NOT NULL DEFAULT 20,
    delivery_time_max INTEGER          NOT NULL DEFAULT 40,
    delivery_fee      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_open           BOOLEAN          NOT NULL DEFAULT TRUE,
    status            VARCHAR(20)      NOT NULL DEFAULT 'ACTIVE',
    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    created_at        TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_restaurants_business_id ON restaurants(business_id);
CREATE INDEX IF NOT EXISTS idx_restaurants_category    ON restaurants(category);
CREATE INDEX IF NOT EXISTS idx_restaurants_status      ON restaurants(status);

CREATE TABLE IF NOT EXISTS menu_items (
    id            BIGSERIAL        PRIMARY KEY,
    restaurant_id BIGINT           NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name          VARCHAR(200)     NOT NULL,
    description   VARCHAR(500),
    price         DOUBLE PRECISION NOT NULL,
    image_url     VARCHAR(500),
    category      VARCHAR(50),
    available     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_menu_items_restaurant_id ON menu_items(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_available     ON menu_items(available);
