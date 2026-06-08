package com.elmandadito.app.data

object SampleData {

    val restaurants = listOf(
        Restaurant(
            id = 1, name = "Tacos El Güero", category = "mexican", emoji = "🌮",
            rating = 4.8, deliveryTime = "25–35 min", deliveryFee = 0, minimumOrder = 100,
            tags = listOf("Mexicana", "Tacos", "Antojitos"), promo = "Envío GRATIS", isOpen = true,
            menu = listOf(
                MenuCategory("Tacos", listOf(
                    MenuItem(101, "Taco de Pastor", "Carne al pastor, cebolla, cilantro y piña", 25, "🌮", true),
                    MenuItem(102, "Taco de Bistec", "Bistec a la parrilla con salsa verde", 28, "🥩"),
                    MenuItem(103, "Taco de Chorizo", "Chorizo con frijoles y queso", 26, "🌶️"),
                    MenuItem(104, "Taco de Suadero", "Suadero tradicional del centro", 27, "🫔", true)
                )),
                MenuCategory("Quesadillas", listOf(
                    MenuItem(105, "Quesadilla de Queso", "Queso Oaxaca derretido", 35, "🧀"),
                    MenuItem(106, "Quesadilla de Pollo", "Pollo deshebrado con rajas", 42, "🍗")
                )),
                MenuCategory("Bebidas", listOf(
                    MenuItem(107, "Agua de Jamaica", "Refrescante agua fresca natural", 20, "🍹"),
                    MenuItem(108, "Horchata", "Con canela y vainilla", 20, "🥛"),
                    MenuItem(109, "Refresco", "Lata 355ml", 18, "🥤")
                ))
            )
        ),
        Restaurant(
            id = 2, name = "Burger Bros", category = "burgers", emoji = "🍔",
            rating = 4.5, deliveryTime = "30–40 min", deliveryFee = 35, minimumOrder = 150,
            tags = listOf("Hamburguesas", "Papas", "Americano"), promo = "2x1 miércoles", isOpen = true,
            menu = listOf(
                MenuCategory("Hamburguesas", listOf(
                    MenuItem(201, "Classic Burger", "Carne 200g, lechuga, tomate, pepinillos", 89, "🍔", true),
                    MenuItem(202, "Double Smash", "Doble carne, doble queso americano", 119, "🍔", true),
                    MenuItem(203, "BBQ Bacon", "Tocino crujiente y salsa BBQ ahumada", 109, "🥓"),
                    MenuItem(204, "Veggie Burger", "Medallón de lentejas y vegetales", 85, "🥗")
                )),
                MenuCategory("Acompañamientos", listOf(
                    MenuItem(205, "Papas Fritas", "Crujientes y bien saladas", 45, "🍟"),
                    MenuItem(206, "Papas con Queso", "Cheddar fundido al momento", 65, "🧀"),
                    MenuItem(207, "Onion Rings", "Rebozados y crujientes", 55, "🧅")
                )),
                MenuCategory("Bebidas", listOf(
                    MenuItem(208, "Malteada Chocolate", "Cremosa, grande y deliciosa", 75, "🥤"),
                    MenuItem(209, "Refresco Grande", "Con mucho hielo", 35, "🧃")
                ))
            )
        ),
        Restaurant(
            id = 3, name = "Pizza Napoletana", category = "pizza", emoji = "🍕",
            rating = 4.7, deliveryTime = "35–50 min", deliveryFee = 40, minimumOrder = 200,
            tags = listOf("Pizza", "Italiana", "Artesanal"), isOpen = true,
            menu = listOf(
                MenuCategory("Pizzas Clásicas", listOf(
                    MenuItem(301, "Margherita", "Tomate San Marzano, mozzarella, albahaca", 135, "🍕", true),
                    MenuItem(302, "Pepperoni", "Pepperoni extra con doble queso", 155, "🍕", true),
                    MenuItem(303, "Cuatro Quesos", "Mozzarella, gouda, brie, parmesano", 165, "🧀"),
                    MenuItem(304, "Vegetariana", "Pimientos, champiñones, aceitunas, albahaca", 145, "🥦")
                )),
                MenuCategory("Especialidades", listOf(
                    MenuItem(305, "BBQ Chicken", "Pollo ahumado, cebolla morada caramelizada", 170, "🍗"),
                    MenuItem(306, "Hawaiana", "Jamón artesanal y piña tropical", 150, "🍍")
                )),
                MenuCategory("Extras", listOf(
                    MenuItem(307, "Palitos de Ajo", "Con mantequilla de ajo y orégano", 55, "🥖"),
                    MenuItem(308, "Ensalada César", "Con aderezo cremoso y crutones", 75, "🥗")
                ))
            )
        ),
        Restaurant(
            id = 4, name = "Sushi Nikkei", category = "sushi", emoji = "🍣",
            rating = 4.9, deliveryTime = "40–55 min", deliveryFee = 50, minimumOrder = 250,
            tags = listOf("Sushi", "Japonesa", "Fusión"), promo = "Combo familiar -15%", isOpen = true,
            menu = listOf(
                MenuCategory("Rollos", listOf(
                    MenuItem(401, "Roll California", "Cangrejo real, aguacate, pepino", 110, "🍣", true),
                    MenuItem(402, "Roll Spicy Tuna", "Atún, sriracha, cebollín japonés", 125, "🔥", true),
                    MenuItem(403, "Dragon Roll", "Camarón tempura, aguacate cremoso", 145, "🐉"),
                    MenuItem(404, "Roll Veggie", "Pepino, aguacate, zanahoria juliana", 95, "🥑")
                )),
                MenuCategory("Nigiri & Sashimi", listOf(
                    MenuItem(405, "Nigiri Salmón x2", "Salmón fresco sobre arroz de sushi", 90, "🍱"),
                    MenuItem(406, "Sashimi Atún x5", "Atún rojo seleccionado", 130, "🐟")
                )),
                MenuCategory("Entradas", listOf(
                    MenuItem(407, "Edamame", "Con flor de sal marina", 55, "🫛"),
                    MenuItem(408, "Gyozas x6", "Pollo y jengibre, salteadas", 85, "🥟"),
                    MenuItem(409, "Miso Soup", "Tofu sedoso y wakame", 45, "🍜")
                ))
            )
        ),
        Restaurant(
            id = 5, name = "El Pollito Feliz", category = "chicken", emoji = "🍗",
            rating = 4.4, deliveryTime = "20–30 min", deliveryFee = 25, minimumOrder = 120,
            tags = listOf("Pollo", "Rápido", "Familiar"), promo = "Cubeta 20pz $299", isOpen = false,
            menu = listOf(
                MenuCategory("Piezas", listOf(
                    MenuItem(501, "Pieza de Pollo", "Crujiente por fuera, jugosa por dentro", 35, "🍗", true),
                    MenuItem(502, "Cubeta 8 piezas", "Surtida con guarnición a elegir", 159, "🪣", true),
                    MenuItem(503, "Alitas x10", "Picantes o BBQ a elegir", 125, "🌶️")
                )),
                MenuCategory("Combos", listOf(
                    MenuItem(504, "Combo Individual", "2 piezas + papas + refresco", 89, "🍱"),
                    MenuItem(505, "Combo Familiar", "8 piezas + 2 papas + 2 refrescos", 249, "👨‍👩‍👧‍👦")
                )),
                MenuCategory("Guarniciones", listOf(
                    MenuItem(506, "Papas Fritas", "Recién salidas del aceite", 45, "🍟"),
                    MenuItem(507, "Ensalada de Col", "Cremosa y fresca", 30, "🥗"),
                    MenuItem(508, "Puré de Papa", "Suave con mantequilla y crema", 35, "🥔")
                ))
            )
        ),
        Restaurant(
            id = 6, name = "Dulcería La Nube", category = "desserts", emoji = "🍰",
            rating = 4.6, deliveryTime = "25–35 min", deliveryFee = 30, minimumOrder = 80,
            tags = listOf("Postres", "Pasteles", "Helados"), isOpen = true,
            menu = listOf(
                MenuCategory("Pasteles", listOf(
                    MenuItem(601, "Pastel de Chocolate", "3 capas con betún de chocolate belga", 85, "🍫", true),
                    MenuItem(602, "Pay de Limón", "Base de galleta, crema de limón fresco", 75, "🍋"),
                    MenuItem(603, "Cheesecake NY", "Con coulis de fresa casero", 80, "🍓", true)
                )),
                MenuCategory("Helados", listOf(
                    MenuItem(604, "Sundae Chocolate", "2 bolas con salsa caliente", 65, "🍨"),
                    MenuItem(605, "Copa de Nieve", "3 sabores a elegir", 75, "🍦"),
                    MenuItem(606, "Malteada Vainilla", "Con crema chantilly y cereza", 70, "🥛")
                )),
                MenuCategory("Antojitos", listOf(
                    MenuItem(607, "Churros x5", "Con cajeta y chocolate caliente", 55, "🍩"),
                    MenuItem(608, "Crepa de Nutella", "Con plátano caramelizado y fresa", 65, "🫓"),
                    MenuItem(609, "Brownie Caliente", "Con helado de vainilla derritiéndose", 70, "🍪")
                ))
            )
        )
    )
}
