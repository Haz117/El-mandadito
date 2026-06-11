-- =============================================================================
-- El Mandadito — Supabase Seed Data
-- Pega este script completo en: supabase.com → proyecto → SQL Editor → New query
-- =============================================================================

-- ─── 0. Desactivar confirmación de email (mientras testeas) ──────────────────
-- OPCIÓN A (SQL — puede no funcionar en todas las versiones de Supabase):
UPDATE auth.config SET mailer_autoconfirm = true WHERE id = 1;
-- OPCIÓN B (recomendada — UI): Authentication → Settings → Email → desactiva "Confirm email"
-- Si la línea anterior falla con "column does not exist", usa la opción B y continúa.

-- ─── 1. RLS: permitir INSERT anónimo en restaurants y menu_items ─────────────
-- Esto permite que el app lea los datos sin JWT
ALTER TABLE restaurants ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_items  ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anon can read restaurants" ON restaurants;
CREATE POLICY "Anon can read restaurants"
    ON restaurants FOR SELECT USING (true);

DROP POLICY IF EXISTS "Anon can read menu_items" ON menu_items;
CREATE POLICY "Anon can read menu_items"
    ON menu_items FOR SELECT USING (true);

-- ─── 2. Restaurantes ─────────────────────────────────────────────────────────
INSERT INTO restaurants
    (id, business_id, name, description, category, image_url, cover_image_url,
     rating, total_ratings, delivery_time_min, delivery_time_max,
     delivery_fee, is_open, status, latitude, longitude)
VALUES
    (1, 1,
     'Tacos El Güero',
     'Los mejores tacos de la ciudad, con recetas tradicionales y salsas caseras.',
     'mexican', NULL, NULL,
     4.8, 312, 25, 35, 0.0, true, 'ACTIVE',
     19.4326, -99.1332),

    (2, 1,
     'Burger Bros',
     'Hamburguesas artesanales de 200g con ingredientes frescos y papas crujientes.',
     'burgers', NULL, NULL,
     4.5, 187, 30, 40, 35.0, true, 'ACTIVE',
     19.4350, -99.1300),

    (3, 1,
     'Pizza Napoletana',
     'Pizza artesanal horneada en horno de leña, con ingredientes importados de Italia.',
     'pizza', NULL, NULL,
     4.7, 241, 35, 50, 40.0, true, 'ACTIVE',
     19.4300, -99.1380),

    (4, 1,
     'Sushi Nikkei',
     'Fusión japonesa-peruana con los rollos más creativos y mariscos ultra frescos.',
     'sushi', NULL, NULL,
     4.9, 98, 40, 55, 50.0, true, 'ACTIVE',
     19.4280, -99.1410),

    (5, 1,
     'El Pollito Feliz',
     'Pollo frito crujiente y jugoso. Combos familiares y cubetas para compartir.',
     'chicken', NULL, NULL,
     4.4, 423, 20, 30, 25.0, false, 'TEMPORARILY_CLOSED',
     19.4370, -99.1250),

    (6, 1,
     'Dulcería La Nube',
     'Postres artesanales, pasteles de diseño, helados cremosos y mucho más dulzura.',
     'desserts', NULL, NULL,
     4.6, 156, 25, 35, 30.0, true, 'ACTIVE',
     19.4315, -99.1360)
ON CONFLICT (id) DO UPDATE SET
    name             = EXCLUDED.name,
    description      = EXCLUDED.description,
    category         = EXCLUDED.category,
    rating           = EXCLUDED.rating,
    total_ratings    = EXCLUDED.total_ratings,
    delivery_time_min = EXCLUDED.delivery_time_min,
    delivery_time_max = EXCLUDED.delivery_time_max,
    delivery_fee     = EXCLUDED.delivery_fee,
    is_open          = EXCLUDED.is_open,
    status           = EXCLUDED.status;

-- ─── 3. Menú — Tacos El Güero (restaurant_id = 1) ───────────────────────────
INSERT INTO menu_items
    (id, restaurant_id, name, description, price, image_url, category, available)
VALUES
    (101, 1, 'Taco de Pastor',    'Carne al pastor, cebolla, cilantro y piña',     25.0, NULL, 'Tacos',        true),
    (102, 1, 'Taco de Bistec',    'Bistec a la parrilla con salsa verde',           28.0, NULL, 'Tacos',        true),
    (103, 1, 'Taco de Chorizo',   'Chorizo con frijoles y queso',                   26.0, NULL, 'Tacos',        true),
    (104, 1, 'Taco de Suadero',   'Suadero tradicional del centro',                 27.0, NULL, 'Tacos',        true),
    (105, 1, 'Quesadilla de Queso','Queso Oaxaca derretido',                        35.0, NULL, 'Quesadillas',  true),
    (106, 1, 'Quesadilla de Pollo','Pollo deshebrado con rajas',                    42.0, NULL, 'Quesadillas',  true),
    (107, 1, 'Agua de Jamaica',   'Refrescante agua fresca natural',                20.0, NULL, 'Bebidas',      true),
    (108, 1, 'Horchata',          'Con canela y vainilla',                           20.0, NULL, 'Bebidas',      true),
    (109, 1, 'Refresco',          'Lata 355ml',                                      18.0, NULL, 'Bebidas',      true)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    price = EXCLUDED.price, category = EXCLUDED.category, available = EXCLUDED.available;

-- ─── 4. Menú — Burger Bros (restaurant_id = 2) ──────────────────────────────
INSERT INTO menu_items
    (id, restaurant_id, name, description, price, image_url, category, available)
VALUES
    (201, 2, 'Classic Burger',     'Carne 200g, lechuga, tomate, pepinillos',          89.0, NULL, 'Hamburguesas',      true),
    (202, 2, 'Double Smash',       'Doble carne, doble queso americano',              119.0, NULL, 'Hamburguesas',      true),
    (203, 2, 'BBQ Bacon',          'Tocino crujiente y salsa BBQ ahumada',            109.0, NULL, 'Hamburguesas',      true),
    (204, 2, 'Veggie Burger',      'Medallón de lentejas y vegetales',                 85.0, NULL, 'Hamburguesas',      true),
    (205, 2, 'Papas Fritas',       'Crujientes y bien saladas',                        45.0, NULL, 'Acompañamientos',   true),
    (206, 2, 'Papas con Queso',    'Cheddar fundido al momento',                       65.0, NULL, 'Acompañamientos',   true),
    (207, 2, 'Onion Rings',        'Rebozados y crujientes',                           55.0, NULL, 'Acompañamientos',   true),
    (208, 2, 'Malteada Chocolate', 'Cremosa, grande y deliciosa',                      75.0, NULL, 'Bebidas',           true),
    (209, 2, 'Refresco Grande',    'Con mucho hielo',                                  35.0, NULL, 'Bebidas',           true)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    price = EXCLUDED.price, category = EXCLUDED.category, available = EXCLUDED.available;

-- ─── 5. Menú — Pizza Napoletana (restaurant_id = 3) ─────────────────────────
INSERT INTO menu_items
    (id, restaurant_id, name, description, price, image_url, category, available)
VALUES
    (301, 3, 'Margherita',       'Tomate San Marzano, mozzarella, albahaca',          135.0, NULL, 'Pizzas Clásicas', true),
    (302, 3, 'Pepperoni',        'Pepperoni extra con doble queso',                   155.0, NULL, 'Pizzas Clásicas', true),
    (303, 3, 'Cuatro Quesos',    'Mozzarella, gouda, brie, parmesano',               165.0, NULL, 'Pizzas Clásicas', true),
    (304, 3, 'Vegetariana',      'Pimientos, champiñones, aceitunas, albahaca',       145.0, NULL, 'Pizzas Clásicas', true),
    (305, 3, 'BBQ Chicken',      'Pollo ahumado, cebolla morada caramelizada',        170.0, NULL, 'Especialidades',  true),
    (306, 3, 'Hawaiana',         'Jamón artesanal y piña tropical',                   150.0, NULL, 'Especialidades',  true),
    (307, 3, 'Palitos de Ajo',   'Con mantequilla de ajo y orégano',                  55.0, NULL, 'Extras',          true),
    (308, 3, 'Ensalada César',   'Con aderezo cremoso y crutones',                    75.0, NULL, 'Extras',          true)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    price = EXCLUDED.price, category = EXCLUDED.category, available = EXCLUDED.available;

-- ─── 6. Menú — Sushi Nikkei (restaurant_id = 4) ─────────────────────────────
INSERT INTO menu_items
    (id, restaurant_id, name, description, price, image_url, category, available)
VALUES
    (401, 4, 'Roll California',    'Cangrejo real, aguacate, pepino',               110.0, NULL, 'Rollos',           true),
    (402, 4, 'Roll Spicy Tuna',    'Atún, sriracha, cebollín japonés',              125.0, NULL, 'Rollos',           true),
    (403, 4, 'Dragon Roll',        'Camarón tempura, aguacate cremoso',             145.0, NULL, 'Rollos',           true),
    (404, 4, 'Roll Veggie',        'Pepino, aguacate, zanahoria juliana',            95.0, NULL, 'Rollos',           true),
    (405, 4, 'Nigiri Salmón x2',  'Salmón fresco sobre arroz de sushi',             90.0, NULL, 'Nigiri & Sashimi', true),
    (406, 4, 'Sashimi Atún x5',   'Atún rojo seleccionado',                         130.0, NULL, 'Nigiri & Sashimi', true),
    (407, 4, 'Edamame',            'Con flor de sal marina',                          55.0, NULL, 'Entradas',         true),
    (408, 4, 'Gyozas x6',          'Pollo y jengibre, salteadas',                    85.0, NULL, 'Entradas',         true),
    (409, 4, 'Miso Soup',          'Tofu sedoso y wakame',                            45.0, NULL, 'Entradas',         true)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    price = EXCLUDED.price, category = EXCLUDED.category, available = EXCLUDED.available;

-- ─── 7. Menú — El Pollito Feliz (restaurant_id = 5) ─────────────────────────
INSERT INTO menu_items
    (id, restaurant_id, name, description, price, image_url, category, available)
VALUES
    (501, 5, 'Pieza de Pollo',    'Crujiente por fuera, jugosa por dentro',          35.0, NULL, 'Piezas',      true),
    (502, 5, 'Cubeta 8 piezas',   'Surtida con guarnición a elegir',                159.0, NULL, 'Piezas',      true),
    (503, 5, 'Alitas x10',        'Picantes o BBQ a elegir',                        125.0, NULL, 'Piezas',      true),
    (504, 5, 'Combo Individual',  '2 piezas + papas + refresco',                     89.0, NULL, 'Combos',      true),
    (505, 5, 'Combo Familiar',    '8 piezas + 2 papas + 2 refrescos',               249.0, NULL, 'Combos',      true),
    (506, 5, 'Papas Fritas',      'Recién salidas del aceite',                        45.0, NULL, 'Guarniciones',true),
    (507, 5, 'Ensalada de Col',   'Cremosa y fresca',                                 30.0, NULL, 'Guarniciones',true),
    (508, 5, 'Puré de Papa',      'Suave con mantequilla y crema',                   35.0, NULL, 'Guarniciones',true)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    price = EXCLUDED.price, category = EXCLUDED.category, available = EXCLUDED.available;

-- ─── 8. Menú — Dulcería La Nube (restaurant_id = 6) ─────────────────────────
INSERT INTO menu_items
    (id, restaurant_id, name, description, price, image_url, category, available)
VALUES
    (601, 6, 'Pastel de Chocolate','3 capas con betún de chocolate belga',            85.0, NULL, 'Pasteles',   true),
    (602, 6, 'Pay de Limón',       'Base de galleta, crema de limón fresco',          75.0, NULL, 'Pasteles',   true),
    (603, 6, 'Cheesecake NY',      'Con coulis de fresa casero',                       80.0, NULL, 'Pasteles',   true),
    (604, 6, 'Sundae Chocolate',   '2 bolas con salsa caliente',                       65.0, NULL, 'Helados',    true),
    (605, 6, 'Copa de Nieve',      '3 sabores a elegir',                               75.0, NULL, 'Helados',    true),
    (606, 6, 'Malteada Vainilla',  'Con crema chantilly y cereza',                    70.0, NULL, 'Helados',    true),
    (607, 6, 'Churros x5',         'Con cajeta y chocolate caliente',                  55.0, NULL, 'Antojitos',  true),
    (608, 6, 'Crepa de Nutella',   'Con plátano caramelizado y fresa',                 65.0, NULL, 'Antojitos',  true),
    (609, 6, 'Brownie Caliente',   'Con helado de vainilla derritiéndose',             70.0, NULL, 'Antojitos',  true)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description,
    price = EXCLUDED.price, category = EXCLUDED.category, available = EXCLUDED.available;

-- ─── 9. Actualizar secuencias para que futuros AUTO IDs no colisionen ─────────
SELECT setval('restaurants_id_seq', (SELECT MAX(id) FROM restaurants));
SELECT setval('menu_items_id_seq',  (SELECT MAX(id) FROM menu_items));

-- ─── 10. Políticas RLS para orders (usuarios autenticados) ───────────────────
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can insert their own orders" ON orders;
CREATE POLICY "Users can insert their own orders"
    ON orders FOR INSERT
    WITH CHECK (auth.uid()::text = user_id);

DROP POLICY IF EXISTS "Users can read their own orders" ON orders;
CREATE POLICY "Users can read their own orders"
    ON orders FOR SELECT
    USING (auth.uid()::text = user_id);

DROP POLICY IF EXISTS "Users can update their own orders" ON orders;
CREATE POLICY "Users can update their own orders"
    ON orders FOR UPDATE
    USING (auth.uid()::text = user_id);

-- ─── 11. Políticas RLS para order_items ──────────────────────────────────────
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can insert order items for their orders" ON order_items;
CREATE POLICY "Users can insert order items for their orders"
    ON order_items FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM orders
            WHERE orders.id = order_items.order_id
              AND orders.user_id = auth.uid()::text
        )
    );

DROP POLICY IF EXISTS "Users can read their order items" ON order_items;
CREATE POLICY "Users can read their order items"
    ON order_items FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM orders
            WHERE orders.id = order_items.order_id
              AND orders.user_id = auth.uid()::text
        )
    );

-- ─── 12. Políticas RLS para reviews ──────────────────────────────────────────
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;

-- Trigger: auto-set user_id y restaurant_id al insertar review
CREATE OR REPLACE FUNCTION set_review_user_and_restaurant()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    -- Pone el UUID del usuario autenticado
    NEW.user_id := auth.uid()::text;
    -- Deriva restaurant_id desde la orden (si no lo trae el cliente)
    IF NEW.restaurant_id IS NULL THEN
        SELECT restaurant_id INTO NEW.restaurant_id
        FROM orders WHERE id = NEW.order_id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_review_set_user ON reviews;
CREATE TRIGGER trg_review_set_user
    BEFORE INSERT ON reviews
    FOR EACH ROW EXECUTE FUNCTION set_review_user_and_restaurant();

DROP POLICY IF EXISTS "Users can insert reviews" ON reviews;
CREATE POLICY "Users can insert reviews"
    ON reviews FOR INSERT
    WITH CHECK (auth.uid() IS NOT NULL);

DROP POLICY IF EXISTS "Anyone can read reviews" ON reviews;
CREATE POLICY "Anyone can read reviews"
    ON reviews FOR SELECT USING (true);

-- ─── 13. Políticas RLS para device_tokens (FCM) ──────────────────────────────
ALTER TABLE device_tokens ENABLE ROW LEVEL SECURITY;

-- Trigger: auto-set user_id al registrar token FCM
CREATE OR REPLACE FUNCTION set_device_token_user()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    NEW.user_id := auth.uid()::text;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_device_token_set_user ON device_tokens;
CREATE TRIGGER trg_device_token_set_user
    BEFORE INSERT ON device_tokens
    FOR EACH ROW EXECUTE FUNCTION set_device_token_user();

DROP POLICY IF EXISTS "Users can manage their own tokens" ON device_tokens;
CREATE POLICY "Users can manage their own tokens"
    ON device_tokens FOR ALL
    USING (auth.uid()::text = user_id)
    WITH CHECK (auth.uid() IS NOT NULL);

-- ─── Verificación ─────────────────────────────────────────────────────────────
SELECT 'Restaurantes insertados: ' || COUNT(*)::text FROM restaurants;
SELECT 'Platillos insertados: '    || COUNT(*)::text FROM menu_items;
