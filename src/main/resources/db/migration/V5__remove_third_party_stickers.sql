-- The original catalogue included stickers depicting real people and third-party
-- brands, shows and characters. They are removed before the project is published.
-- This is a new migration rather than an edit to V2__seed_products.sql: Flyway
-- checksums applied migrations, so changing V2 would fail validation on any
-- database that has already run it.
DELETE FROM products
WHERE image_url IN ('/stickers/Mbappe.png',
                    '/stickers/ronaldo.png',
                    '/stickers/Messi.png',
                    '/stickers/Virat.png',
                    '/stickers/HouseOfTheDragonSymbol.png',
                    '/stickers/SquidGame.png',
                    '/stickers/Shinchan.png',
                    '/stickers/Naruto.png',
                    '/stickers/Goku.png',
                    '/stickers/BreakingCode.png');
