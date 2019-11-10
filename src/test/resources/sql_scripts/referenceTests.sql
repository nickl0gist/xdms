insert into storage_location
  (storage_locationid, code, name)
VALUES
  (1, 'EX01', 'SL - EX01'),
  (2, 'EX11', 'SL - EX11');

insert into suppliers
  (supplierid, name, vendor_code, country,  city, email, post_code, street)
VALUES
  (1, 'BARTON', 111000, 'PL', 'London', 'londom@barton.pl', '06-717', 'Hilton- 55'),
  (2, 'NoName', 999777, 'FR', 'Washington', 'washington@NoName.fr', '777-777', 'Brayton- 555');

insert into customers
  (customerid, name, customer_code, country, city, email, is_active, post_code, street)
values
  (1, 'LugaName', 123123, 'RU', 'MOSCOW', 'luga@moscow.ru', true, '250501', 'Ptushkin'),
  (2, 'KalugaName', 808080, 'EN', 'BUDAPEST', 'kaluga@budapest.hu', true, '303030', 'Kokossowec');

insert into reference
  (referenceid, number, name, hs_code, pcs_perpu, pcs_perhu, weight, weight_of_packaging,
  designationen, designationru, pallet_height, pallet_length, pallet_width,
  pallet_weight, stackability, customer_customerid, customer_agreement,
  supplier_supplierid, supplier_agreement, storage_locationid, is_active)
VALUES
  (1, '44444XXX', 'Head Rest', '80090905', 1000, 100, 50, 1.5,
  'designatione Head Rest', 'designatione Подголовник', 1200, 1000, 800,	15, 1, 1, '5500000001', 1, '440000000', 1,true),
  (2, '11111XXX-CO', 'Anchor belt',	'89090909', 1500, 200, 25, 2.3,
  'designatione Anchor belt', 'designatione Якорь длинный, не короткий', 1100, 900, 1000,	20, 2, 2, '5500001111', 2, '4400022222', 2, true),
  (3, '11111YYY', 'Anchor belt-test',	'81111111', 1500, 200, 25, 2.3,
  'designatione Anchor belt', 'Testt Test Tessst тест ', 1100, 900, 1000,	20, 2, 2, '5500000000', 2, '440001111', 2, true);
