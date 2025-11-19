DROP TABLE IF EXISTS shopping_cart CASCADE;
DROP TABLE IF EXISTS cart_products CASCADE;

CREATE TABLE IF NOT EXISTS shopping_cart (
    shopping_cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    state VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS cart_products (
    cart_id UUID NOT NULL REFERENCES shopping_cart(shopping_cart_id),
    product_id UUID NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (cart_id, product_id)
);