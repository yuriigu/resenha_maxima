INSERT INTO brands (name) VALUES ('Dell'), ('HP'), ('Cisco');

INSERT INTO models (id_brand, name)
SELECT id, 'PowerEdge R740' FROM brands WHERE name = 'Dell';
