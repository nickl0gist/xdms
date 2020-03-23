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
  (supplierid, name, vendor_code, country,  city, email, post_code, street, is_active)
VALUES
  (1, 'BARTON', 111000, 'PL', 'London', 'londom@barton.pl', '06-7177', 'Hilton- 55', true),
  (2, 'NoName', 222000, 'FR', 'Amsterdam', 'Amsterdam@NoName.fr', '058-777', 'Kaszta- 555', true),
  (3, 'ABCDEF', 666000, 'AR', 'Grojec', 'Grojec@NoName.fr', '718-777', 'Brayton- 555', true),
  (4, 'BCDEFG', 777777, 'GE', 'Berlin', 'Berlin@NoName.fr', '222-777', 'Kinsey- 555', false),
  (5, 'XYZDEO', 707070, 'UK', 'Moscow', 'Moscow@NoName.fr', '301-777', 'Livs- 555', true),
  (6, 'KLNMOP', 555000, 'PT', 'New York', 'Moscow@NoName.fr', '101-777', 'Topolowa- 555', false);

insert into customers
  (customerid, name, customer_code, country, city, email, is_active, post_code, street, time_zone)
values
  (1, 'LugaName', 123123, 'DE', 'MOSCOW', 'LugaName@moscow.ru', true, 250250, 'Ptushkin', 'GMT+05'),
  (2, 'SAOName', 500500, 'EN', 'ARTEMOVCK', 'SAOName@budapest.hu', true, 303030, 'Kokossowec', 'GMT+06'),
  (3, 'BrasilName', 777222, 'IT', 'MILAN', 'BrasilName@budapest.hu', true, 745745, 'Koshmarkina', 'GMT+02'),
  (4, 'ArgentinaName', 194978, 'BE', 'ANTWERPEN', 'ArgentinaName@budapest.hu', false, 987987, 'Abcdefgth', 'GMT+02'),
  (5, 'NoName', 555555, 'AR', 'BUENOS', 'NoName@budapest.hu', false, 755755, 'Bdfres', 'GMT+02'),
  (6, 'CC Swie', 'cc_swie', 'PL', 'Swiebodzice','pawel@wieczorek.pl', true, '04-200', 'Lotnicza - 1', 'GMT+02'),
  (7, 'XD Gro', 'xd_gro', 'PL', 'Grojec', 'sławek@barscz.pl', true, '05-600', 'Spoldzielcza - 2', 'GMT+02'),
  (8, 'XD STD', 'xd_std', 'GE', 'Stadthagen', 'udo@muller.ge', true, '20-212', 'Muller - 20', 'GMT+02');

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
  (warehouseid, city, country, email, is_active, name, post_code, street, wh_typeid, url_code, time_zone)
VALUES
  (1, 'Swiebodzice', 'PL', 'pawel@wieczorek.pl', true, 'CC Swie', '04-200', 'Lotnicza - 1', 1, 'cc_swie', 'GMT+02'),
  (2, 'Grojec', 'PL', 'sławek@barscz.pl', true, 'XD Gro', '05-600', 'Spoldzielcza - 2', 3, 'xd_gro', 'GMT+02'),
  (3, 'Irun', 'ES', 'itziar@dominges.es', false, 'CC IRUN', '99-992', 'EPO - 20', 1, 'cc_irun', 'GMT+02'),
  (4, 'Stadthagen', 'GE', 'udo@muller.ge', true, 'XD STD', '20-212', 'Muller - 20', 2, 'xd_std', 'GMT+02'),
  (5, 'Bambury', 'RO', 'roman@romamiks.ro', false, 'CC ROMANY', '20-212', 'Muller - 20', 1, 'cc_arad', 'GMT+02');

insert into warehouse_customer
  (wh_customerid, warehouseid, customerid, is_active,transit_time)
VALUES
  (1,  1, 1, true, 'P0DT1H0M'),
  (2,  1, 2, true, 'P0DT1H0M'),
  (3,  1, 3, false, 'P0DT1H0M'),
  (4,  1, 4, true, 'P0DT1H0M'),
  (5,  1, 5, true, 'P0DT1H0M'),
  (6,  2, 1, true, 'P0DT1H0M'),
  (7,  2, 2, true, 'P0DT1H0M'),
  (8,  2, 3, true, 'P0DT1H0M'),
  (9,  2, 4, true, 'P0DT1H0M'),
  (10, 2, 5, true, 'P0DT1H0M'),
  (11, 1, 7, true, 'P0DT1H0M'),
  (12, 3, 8, true, 'P0DT1H40M'),
  (13, 5, 8, true, 'P0DT2H10M'),
  (14, 4, 7, true, 'P0DT2H30M'),
  (15, 4, 4, true, 'P0DT2H0M'),
  (16, 4, 3, true, 'P0DT2H0M'),
  (17, 4, 1, true, 'P0DT2H0M');

insert into users
  (id,username, first_name, last_name, email, password, role_id)
VALUES
  (1,'Admin', 'Adm Name', 'Adm Last Name', 'admin@bla.pl', 'admin_pass', 1),
  (2,'User', 'User Name', 'User Last Name', 'user@bla.pl', 'user_pass', 2);
  
insert into tpa_status (statusid, status_name)
values (1,'CLOSED'),(2,'DELAYED'),(3,'IN_PROGRESS'),(4,'BUFFER'),(5,'ERROR');

insert into ttt_status (ttt_statusid, ttt_status_name)
values (1,'PENDING'), (2,'DELAYED'), (3,'ARRIVED'), (4,'ERROR');

insert into working_days
    (id, day_name, is_active)
values (1, 'MONDAY' ,true),
    (2, 'TUESDAY', true),
    (3, 'WEDNESDAY', true),
    (4, 'THURSDAY',	true),
    (5, 'FRIDAY', true),
    (6, 'SATURDAY',	true),
    (7, 'SUNDAY', false);

insert into tpa_days_settings
    (id, local_time, transit_time, wh_customerid, working_daysid)
values
    (1, '00:00', 'P0DT1H0M', 1, 1),
    (2, '00:00', 'P0DT1H0M', 2, 1),
    (3, '00:00', 'P0DT1H0M', 3, 1),
    (4, '00:00', 'P0DT1H0M', 4, 1),
    (5, '00:00', 'P0DT1H0M', 5, 1),
    (6, '00:00', 'P0DT1H0M', 6, 1),
    (7, '00:00', 'P0DT1H0M', 7, 1),
    (8, '00:00', 'P0DT1H0M', 8, 1),
    (9, '00:00', 'P0DT1H0M', 9, 1),
    (10, '00:00', 'P0DT1H0M', 10, 1),
    (11, '05:00', 'P0DT1H0M', 11, 3),
    (12, '10:00', 'P0DT1H0M', 11, 1),
    (13, '20:00', 'P0DT1H0M', 11, 2),
    (14, '12:00', 'P0DT1H0M', 7, 3),
    (15, '16:00', 'P0DT1H0M', 7, 5),
    (16, '17:00', 'P0DT1H0M', 11, 4),
    (17, '18:00', 'P0DT1H0M', 11, 5),
    (18, '18:00', 'P0DT1H40M', 12, 5),
    (19, '10:00', 'P0DT1H40M', 12, 4),
    (20, '18:00', 'P0DT1H40M', 12, 3),
    (21, '10:00', 'P0DT1H40M', 12, 2),
    (22, '18:00', 'P0DT1H40M', 12, 1),
    (23, '11:00', 'P0DT2H10M', 13, 1),
    (24, '15:00', 'P0DT2H10M', 13, 2),
    (25, '11:00', 'P0DT2H10M', 13, 3),
    (26, '15:00', 'P0DT2H10M', 13, 4),
    (27, '11:00', 'P0DT2H10M', 13, 5),
    (28, '14:30', 'P0DT2H30M', 14, 1),
    (29, '14:30', 'P0DT2H30M', 14, 2),
    (30, '14:30', 'P0DT2H30M', 14, 3),
    (31, '14:30', 'P0DT2H30M', 14, 4),
    (32, '14:30', 'P0DT2H30M', 14, 5),
    (33, '20:00', 'P0DT1H30M', 8, 1),
    (34, '20:00', 'P0DT1H30M', 8, 2),
    (35, '20:00', 'P0DT1H30M', 8, 3),
    (36, '20:00', 'P0DT1H30M', 8, 4),
    (37, '20:00', 'P0DT1H30M', 8, 5),
    (38, '15:00', 'P0DT2H0M', 15, 1),
    (39, '15:00', 'P0DT2H0M', 15, 2),
    (40, '15:00', 'P0DT2H0M', 15, 3),
    (41, '15:00', 'P0DT2H0M', 15, 4),
    (42, '15:00', 'P0DT2H0M', 15, 5),
    (43, '15:00', 'P0DT2H0M', 16, 1),
    (44, '15:00', 'P0DT2H0M', 16, 2),
    (45, '15:00', 'P0DT2H0M', 16, 3),
    (46, '15:00', 'P0DT2H0M', 16, 4),
    (47, '15:00', 'P0DT2H0M', 16, 5),
    (48, '10:00', 'P1DT2H0M', 6, 1),
    (49, '10:00', 'P1DT2H0M', 6, 2),
    (50, '10:00', 'P1DT2H0M', 6, 3),
    (51, '10:00', 'P1DT2H0M', 6, 4),
    (52, '10:00', 'P1DT2H0M', 6, 5),
    (53, '10:00', 'P1DT2H0M', 17, 1),
    (54, '10:00', 'P1DT2H0M', 17, 2),
    (55, '10:00', 'P1DT2H0M', 17, 3),
    (56, '10:00', 'P1DT2H0M', 17, 4),
    (57, '10:00', 'P1DT2H0M', 17, 5);

insert into manifests
(manifestid, manifest_code, pallet_qty_planned, box_qty_planned, totalldm_planned,
total_weight_planned, customerid, supplierid)
VALUES (1, 'TEST-MAN-00', 1, 3, 1.2, 800, 1, 1);

