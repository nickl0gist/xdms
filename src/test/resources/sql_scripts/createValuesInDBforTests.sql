insert into roles
  (id, description, is_active, name)
values
  (1,'bla bla', true, 'ADMIN_ROLE'),
  (2,'bla bla', true, 'USER_ROLE'),
  (3,'bla bla', true, 'GUEST_ROLE'),
  (4,'bla bla', true, 'PLANNER_ROLE'),
  (5,'bla bla', true, 'TRAFFIC_ROLE');

insert into storage_location
  (storage_locationid, code, name)
VALUES
  (1, 'EX01', 'SL - EX01'),
  (2, 'EX11', 'SL - EX11'),
  (3, 'EX25', 'SL - EX25'),
  (4, 'EX40', 'SL - EX40');

insert into suppliers
  (supplierid, name, vendor_code, country,  city, email, post_code, street, is_active )
VALUES
  (1, 'BARTON', 111000, 'PL', 'London', 'londom@barton.pl', '06-7177', 'Hilton- 55', true),
  (2, 'NoName', 222000, 'FR', 'Amsterdam', 'Amsterdam@NoName.fr', '058-777', 'Kaszta- 555', true),
  (3, 'ABCDEF', 666000, 'AR', 'Grojec', 'Grojec@NoName.fr', '718-777', 'Brayton- 555', true),
  (4, 'BCDEFG', 777777, 'GE', 'Berlin', 'Berlin@NoName.fr', '222-777', 'Kinsey- 555', false),
  (5, 'XYZDEO', 707070, 'UK', 'Moscow', 'Moscow@NoName.fr', '301-777', 'Livs- 555', true),
  (6, 'KLNMOP', 555000, 'PT', 'New York', 'Moscow@NoName.fr', '101-777', 'Topolowa- 555', false);

insert into customers
  (customerid, name, customer_code, country, city, email, is_active, post_code, street)
values
  (1, 'LugaName', 123123, 'DE', 'MOSCOW', 'LugaName@moscow.ru', true, 250250, 'Ptushkin'),
  (2, 'SAOName', 500500, 'EN', 'ARTEMOVCK', 'SAOName@budapest.hu', true, 303030, 'Kokossowec'),
  (3, 'BrasilName', 777222, 'IT', 'MILAN', 'BrasilName@budapest.hu', true, 745745, 'Koshmarkina'),
  (4, 'ArgentinaName', 194978, 'BE', 'ANTWERPEN', 'ArgentinaName@budapest.hu', false, 987987, 'Abcdefgth'),
  (5, 'NoName', 555555, 'AR', 'BUENOS', 'NoName@budapest.hu', false, 755755, 'Bdfres');

insert into reference
  (referenceid, number, name, hs_code, pcs_perpu, pu_perhu, weight, weight_of_packaging,
  designationen, designationru, pallet_height, pallet_length, pallet_width,
  pallet_weight, stackability, customer_customerid, customer_agreement,
  supplier_supplierid, supplier_agreement, storage_locationid, is_active)
VALUES
  (1, '44444XXX', 'Head Rest', '80090905', 1000, 100, 50, 1.5,
  'designatione Head Rest', 'designatione Подголовник', 1200, 1000, 800,	15, 1, 1, '5500000001', 1, '440000000', 1,true),
  (2, '11111XXX-CO', 'Anchor belt',	'89090909', 1500, 200, 25, 2.3,
  'designatione Anchor belt', 'designatione Якорь длинный, не короткий', 1100, 900, 1000,	20, 2, 2, '5500001111', 2, '4400022222', 2, false),
  (3, '11111YYY', 'Anchor belt-test',	'81111111', 1500, 200, 25, 2.3,
  'designatione Anchor belt', 'Testt Test Tessst тест ', 1100, 900, 1000,	20, 2, 2, '5500000000', 2, '440001111', 2, true);

insert into wh_type
  (wh_typeid, name)
VALUES
  (1, 'CC'),
  (2, 'XD'),
  (3, 'TXD');

insert into warehouses
  (warehouseid, city, country, email, is_active, name, post_code, street, wh_typeid, url_code)
VALUES
  (1, 'Świebodzice', 'PL', 'pawel@wieczorek.pl', true, 'CC Świe', '04-200', 'Lotnicza - 1', 1, 'cc_swie'),
  (2, 'Grójec', 'PL', 'sławek@barscz.pl', true, 'XD Gró', '05-600', 'Spółdzielcza - 2', 2, 'xd_gro'),
  (3, 'Irun', 'ES', 'itziar@dominges.es', false, 'CC IRUN', '99-992', 'EPO - 20', 1, 'cc_irun'),
  (4, 'Stadthagen', 'GE', 'udo@muller.ge', true, 'XD STD', '20-212', 'Muller - 20', 2, 'xd_std'),
  (5, 'Bambury', 'RO', 'roman@romamiks.ro', false, 'CC ROMANY', '20-212', 'Muller - 20', 1, 'cc_arad');

insert into warehouse_customer
  (wh_customerid, customerid, warehouseid, is_active)
VALUES
  (1, 1, 1, true),
  (2, 2, 1, true),
  (3, 3, 1, false),
  (4, 4, 1, true),
  (5, 5, 1, true),
  (6, 1, 2, true),
  (7, 2, 2, true),
  (8, 3, 2, false),
  (9, 4, 2, true),
  (10, 5, 2, true);

insert into users
  (id,username, first_name, last_name, email, password, role_id)
VALUES
  (1,'Admin', 'Adm Name', 'Adm Last Name', 'admin@bla.pl', 'admin_pass', 1),
  (2,'User', 'User Name', 'User Last Name', 'user@bla.pl', 'user_pass', 2);
  
insert into tpa_status (status_name)
values ('CLOSED'),('DELAYED'),('IN_PROGRESS'),('BUFFER');

insert into ttt_status (ttt_status_name)
values ('PENDING'), ('DELAYED'), ('ARRIVED');