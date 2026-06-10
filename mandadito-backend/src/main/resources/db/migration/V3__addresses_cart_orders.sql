-- Phase 3: Addresses, Cart, Orders

CREATE TABLE IF NOT EXISTS addresses (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    street        VARCHAR(200) NOT NULL,
    number        VARCHAR(20),
    neighborhood  VARCHAR(100),
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100),
    zip_code      VARCHAR(10),
    reference     VARCHAR(300),
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);

CREATE TABLE IF NOT EXISTS carts (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT    UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id BIGINT    REFERENCES restaurants(id),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT    NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    menu_item_id BIGINT   NOT NULL REFERENCES menu_items(id),
    quantity    INTEGER   NOT NULL DEFAULT 1,
    notes       VARCHAR(300),
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);

CREATE TABLE IF NOT EXISTS orders (
    id             BIGSERIAL        PRIMARY KEY,
    user_id        BIGINT           NOT NULL REFERENCES users(id),
    restaurant_id  BIGINT           NOT NULL REFERENCES restaurants(id),
    driver_id      BIGINT           REFERENCES users(id),
    address_id     BIGINT           NOT NULL REFERENCES addresses(id),
    status         VARCHAR(30)      NOT NULL DEFAULT 'CREATED',
    subtotal       DOUBLE PRECISION NOT NULL,
    delivery_fee   DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    service_fee    DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    discount       DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total          DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(20)      NOT NULL DEFAULT 'CASH',
    payment_status VARCHAR(20)      NOT NULL DEFAULT 'PENDING',
    notes          VARCHAR(500),
    created_at     TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP        NOT NULL DEFAULT NOW(),
    delivered_at   TIMESTAMP,
    cancelled_at   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id       ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_orders_driver_id     ON orders(driver_id);
CREATE INDEX IF NOT EXISTS idx_orders_status        ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
    id             BIGSERIAL        PRIMARY KEY,
    order_id       BIGINT           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id   BIGINT,
    name_snapshot  VARCHAR(200)     NOT NULL,
    price_snapshot DOUBLE PRECISION NOT NULL,
    quantity       INTEGER          NOT NULL,
    subtotal       DOUBLE PRECISION NOT NULL,
    notes          VARCHAR(300)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
