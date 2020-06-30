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
  (8, 'XD STD', 'xd_std', 'GE', 'Stadthagen', 'udo@muller.ge', true, '20-212', 'Muller - 20', 'GMT+02'),
  (9, 'SPEC_TEST', 123456, 'UA', 'KYIV', 'kaktus@coco.ua', true, 832454, 'Watutina', 'GMT+03');

insert into reference
  (referenceid, number, name, hs_code, pcs_perpu, pu_perhu, weight, weight_of_packaging,
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
  (5, 'Bambury', 'RO', 'roman@romamiks.ro', false, 'CC ROMANY', '20-212', 'Muller - 20', 1, 'cc_arad', 'GMT+02'),
  (6, 'Stadthagen', 'GE', 'udo@muller.ge', true, 'TXD STD', '20-212', 'Muller - 20', 2, 'txd_std', 'GMT+02');

insert into warehouse_customer
  (wh_customerid, warehouseid, customerid, is_active, transit_time)
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
  (17, 4, 1, true, 'P0DT2H0M'),
  (18, 5, 7, true, 'P0DT2H0M'),
  (19, 6, 7, true, 'P0DT2H0M'),
  (20, 6, 3, true, 'P0DT2H0M'),
  (21, 6, 4, true, 'P0DT2H0M');

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
    (50, '10:00', 'P1DT2H0M', 6, 3), /*Transit time does not correspond to Wh_Customer settings*/
    (51, '10:00', 'P1DT2H0M', 6, 4),
    (52, '10:00', 'P1DT2H0M', 6, 5),
    (53, '10:00', 'P1DT2H0M', 17, 1),
    (54, '10:00', 'P1DT2H0M', 17, 2),
    (55, '10:00', 'P1DT2H0M', 17, 3),
    (56, '10:00', 'P1DT2H0M', 17, 4),
    (57, '10:00', 'P1DT2H0M', 17, 5),
    (58, '12:00', 'P1DT1H0M', 18, 1),
    (59, '12:00', 'P1DT1H0M', 18, 2),
    (60, '12:00', 'P1DT1H0M', 18, 3),
    (61, '12:00', 'P1DT1H0M', 18, 4),
    (62, '12:00', 'P1DT1H0M', 18, 5),
    (63, '12:00', 'P1DT1H0M', 19, 1),
    (64, '12:00', 'P1DT1H0M', 19, 2),
    (65, '12:00', 'P1DT1H0M', 19, 3),
    (66, '12:00', 'P1DT1H0M', 19, 4),
    (67, '12:00', 'P1DT1H0M', 19, 5),
    (68, '17:00', 'P1DT4H0M', 20, 1),
    (69, '17:00', 'P1DT4H0M', 20, 2),
    (70, '17:00', 'P1DT4H0M', 20, 3),
    (71, '17:00', 'P1DT4H0M', 20, 4),
    (72, '17:00', 'P1DT4H0M', 20, 5),
    (73, '14:00', 'P1DT5H0M', 21, 1),
    (74, '14:00', 'P1DT5H0M', 21, 2),
    (75, '14:00', 'P1DT5H0M', 21, 3),
    (76, '14:00', 'P1DT5H0M', 21, 4),
    (77, '14:00', 'P1DT5H0M', 21, 5),
    (78, '03:00', 'P1DT5H0M', 16, 4);

insert into manifests
(manifestid, manifest_code, pallet_qty_planned, box_qty_planned, totalldm_planned,
total_weight_planned, customerid, supplierid)
VALUES
(1, 'TEST-MAN-I',  1, 3, 1.2, 100.0, 1, 1),
(2, 'TEST-MAN-II', 1, 3, 1.2, 100.0, 1, 1),
(3, 'TEST-MAN-01', 1, 3, 1.2, 100.0, 1, 1),
(4, 'TEST-MAN-02', 1, 3, 1.2, 500.0, 2, 2),
(5, 'TEST-MAN-03', 1, 3, 1.2, 1000.0, 3, 3),
(6, 'TEST-MAN-04', 1, 3, 1.2, 1000.0, 4, 3),
(7, 'MAN-X-01',    1, 3, 1.2, 500.0,  1, 1),
(8, 'MAN-X-02',    1, 3, 2.4, 500.0,  2, 2),
(9, 'MAN-X-03',    1, 3, 4.6, 1000.0, 3, 3),
(10, 'MAN-X-04',      1, 3, 4.6, 1000.0, 4, 3),
(11, 'TEST-MAN-III',  1, 3, 1.2, 100.0, 1, 1),
(12, 'TEST-MAN-IV',   1, 3, 1.2, 100.0, 1, 1),
(13, 'MAN-X-05',      1, 3, 2.4, 500.0, 2, 2),
(14, 'Man-Y-01',      2, 4, 5.4, 1500.0, 1, 1),
(15, 'Man-Y-02',      2, 4, 5.4, 2500.0, 1, 2),
(16, 'Man-Y-03',      2, 4, 5.4, 3500.0, 3, 3),
(17, 'Man-Y-04',      2, 4, 5.4, 4500.0, 3, 3),
(18, 'AB-TEST',       2, 4, 5.4, 4500.0, 3, 3),
(19, 'AB-TEST2',      2, 4, 5.4, 4500.0, 3, 3);

insert into manifest_reference
(manifest_reference_id, manifestid, referenceid, qty_planned, pallet_qty_planned, box_qty_planned, gross_weight_planned,
pallet_height, pallet_length, pallet_width, pallet_weight, stackability)
values
(1, 3,  1, 10  , 1, 10, 100.0, 1000.0, 800.0, 1200.0, 15, 1),
(2, 4,  2, 100 , 2, 20, 200.0, 1000.0, 800.0, 1200.0, 15, 1),
(3, 4,  3, 1000, 3, 30, 300.0, 1000.0, 800.0, 1200.0, 15, 1),
(4, 7,  1, 10  , 1, 10, 100.0, 1000.0, 800.0, 1200.0, 15, 1),
(5, 8,  2, 100 , 2, 20, 200.0, 1000.0, 800.0, 1200.0, 15, 1),
(6, 8,  3, 1000, 3, 30, 300.0, 1000.0, 800.0, 1200.0, 15, 1),
(7, 13, 3, 1000, 3, 30, 300.0, 1000.0, 800.0, 1200.0, 15, 1),
(8,  14, 3, 2000, 2, 40, 400.0, 2000.0, 800.0, 1200.0, 15, 1),
(9,  15, 3, 2000, 2, 40, 400.0, 2000.0, 800.0, 1200.0, 15, 1),
(10, 16, 3, 2000, 2, 40, 400.0, 2000.0, 800.0, 1200.0, 15, 1),
(11, 17, 3, 2000, 2, 40, 400.0, 2000.0, 800.0, 1200.0, 15, 1);

insert into ttt
(tttid, truck_name, ttt_arrival_date_plan, ttt_status_ttt_statusid, warehouse_warehouseid)
values
(1,  'TPA1',    '2020-04-29T12:30', 3, 1),
(2,  'TPA3',    '2020-05-20T15:30', 1, 1),
(3,  'BART01',  '2020-04-21T15:30', 1, 1),
(4,  'NON01',   '2020-04-21T05:00', 1, 3),
(5,  'ABSD01',  '2020-04-22T11:00', 1, 5),
(6,  'ABSD02',  '2020-04-22T12:00', 1, 5),
(7,  'IRU1',    '2020-04-23T12:00', 1, 4),
(8,  'ROM1',    '2020-04-27T20:00', 1, 4),
(9,  'GRO1',    '2020-04-28T20:00', 1, 2),
(10, 'GRO2',    '2020-04-28T10:00', 1, 2),
(11, 'BRT01',   '2020-05-07T17:00', 1, 5),
(12, 'NN001',   '2020-05-10T15:00', 1, 4),
(13, 'ABC01',   '2020-05-06T10:00', 1, 5),
(14, 'ABC02',   '2020-05-10T15:15', 1, 4),
(15, 'EXT0',    '2020-05-09T22:22', 1, 4),
(16, 'EXT1',    '2020-05-10T11:11', 1, 4),
(17, 'GRO-X',   '2020-05-14T13:13', 1, 2),
(18, 'BRT02',   '2020-05-04T17:00', 1, 5),
(19, 'GRO-X2',  '2020-05-13T07:07', 1, 2),
(20, 'BRT01-II',  '2020-05-11T17:00', 1, 5),
(21, 'NN001-II',  '2020-05-20T13:13', 1, 2),
(22, 'ABS-II',    '2020-05-05T17:00', 1, 5),
(23, 'ABS2-II',   '2020-05-05T15:00', 1, 5),
(24, 'EXT-11',   '2020-05-13T11:11', 1, 4),
(25, 'ROM-II',   '2020-05-07T20:00', 1, 4),
(26, 'ROM1-II',  '2020-05-08T19:00', 1, 4),
(27, 'GRO-XII',  '2020-05-20T13:13', 1, 2),
(28, 'BRA1-II',  '2020-05-12T12:00', 3, 2);


insert into ttt_manifest
(tttid, manifestid)
values
(1,  1),(1,  2),
(3,  3),(9,  3),
(4,  4),(7,  4),(10, 4),(5,  5),
(8,  5),
(6,  6),(8,  6),
(11,  7),(16,  7),(17,  7),
(12,  8),(17,  8),
(13,  9),(15,  9),
(14,  10),
(2,  11),(2,  12),
(18, 13), (16, 13), (19, 13),
(20, 14), (24, 14), (27, 14),
(21 ,15),
(22, 16), (25, 16), (28, 16),
(23, 17), (26, 17), (28, 17);



insert into tpa
(tpaid, name, status_statusid, departure_plan, tpa_days_setting_id)
values
(1, 'TPA_', 3, '2020-04-21T21:21', 1),
(2, 'GRO1', 3, '2020-04-22T05:00', 11),
(3, 'IRU1', 3, '2020-04-22T18:00', 20),
(4, 'ROM1', 3, '2020-04-27T15:00', 24),
(5, 'GRO2', 3, '2020-04-24T12:00', 67),
(6, 'BRA1', 3, '2020-04-28T17:00', 69),
(7, 'ARG1', 3, '2020-04-28T14:00', 74),
(8, 'SAO1', 3, '2020-04-29T10:00', 50),
(9, 'BRA1', 3, '2020-04-29T12:00', 14),
/*------------------------------------*/
(10, 'EXT1',  3, '2020-05-08T11:00', 27),
(11, 'EXT0',  3, '2020-05-07T15:00', 26),
(12, 'GRO-X', 3, '2020-05-11T14:30', 28),
(13, 'DIRECT', 3, '2020-05-11T15:00', 43),
(14, 'DIRECT2', 3, '2020-05-11T15:00', 38),
(15, 'LUGA1', 3, '2020-05-15T10:00', 52),
(16, 'SAO-2', 3, '2020-05-15T16:00', 15),
(17, 'GRO-X2', 3, '2020-05-11T14:30', 28),
(18, 'SAO-3', 3, '2020-05-15T16:00', 15),
/*------------------------------------*/
(19, 'EXT-11',  3, '2020-05-12T15:00', 24), /*CC ROMANY _ WhID = 5; XD STD CustID = 8; WHCust id = 13*/
(20, 'ROM-II',  3, '2020-05-06T11:00', 25),
(21, 'ROM1-II', 3, '2020-05-06T11:00', 25),
(22, 'GRO-XII', 3, '2020-05-14T14:30', 31), /*XD STD _ WhID = 4; XD Gro CustID = 7; WHCust id = 14*/
(23, 'BRA1-II', 3, '2020-05-11T14:30', 28),
(24, 'LUGA1',   3, '2020-05-21T10:00', 51),/* Wh Cust 6*/
(25, 'BRA-II',  3, '2020-05-13T20:00', 35),/* Wh Cust 8*/
(26, 'CLOSED_TPA',  1, '2020-05-20T20:00', 35),
(27, 'TEST_TPA',   3, '2020-05-25T10:00', 51),/* Wh Cust 8*/
/*(1,'CLOSED'),(2,'DELAYED'),(3,'IN_PROGRESS'),(4,'BUFFER'),(5,'ERROR');*/
(28, 'TEST_TPA1',   1, '2020-06-04T10:00', 51), /*xd_gro*/
(29, 'TEST_TPA2',   1, '2020-06-03T12:00', 14),
(31, 'TEST_TPA3',   2, '2020-06-05T16:00', 15),
(32, 'TEST_TPA4',   3, '2020-06-11T20:00', 36),
(33, 'TEST_TPA5',   2, '2020-06-12T20:00', 37),
(34, 'TEST_TPA6',   3, '2020-06-08T00:00', 9),
(35, 'TEST_TPA7',   3, '2020-06-08T00:00', 6),
(36, 'TEST_TPA8',   4, '2020-06-15T00:00', 10),
(37, 'TEST_TPA9',   4, '2020-06-16T10:00', 49),
(38, 'TEST_TPA10',  4, '2020-06-22T00:00', 10);


insert into tpa_manifest
(tpaid, manifest_id)
values
(2, 3),(3, 4),(5, 4),(4, 5),(6, 5),(4, 6),(7, 6),
/*---------------------------------------------*/
(10, 7),(12, 7),
(12, 8),
(11, 9),(13, 9),(14, 10),
(10, 13),(17, 13),
/*---------------------------------------------*/
(19, 14),(22, 14),
(20, 16),(23, 16),
(21, 17),(23, 17);

insert into tpa_manifest_reference
(tpaid, manifest_reference_id)
values
(8, 1),
(9, 2),
(9, 3),
(15, 4),
(16, 5),
(16, 6),
(18, 7),
(24, 8),
(24, 9),
(25, 10),
(25, 11);

update manifests
SET pallet_qty_real = 10 where manifestid = 1;