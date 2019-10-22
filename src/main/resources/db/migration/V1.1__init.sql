insert into roles
  (id, description, is_active, name)
values
  (1, "bla bla", true, "ADMIN_ROLE"),
  (2, "bla bla", true, "USER_ROLE");

insert into users
  (email, first_name, last_name, password, username, role_id)
values
  ("booo@bo.com", "bobo", "bobo", "123wsd", "BoBo", 1),
  ("yuammy@yu.me", "youm", "youm", "123wsd", "YMmm", 2);

insert into reference
  (referenceid, number, name, hs_code, pcs_perhu, pcs_perpu, weight,
   is_active, pallet_height, pallet_length, pallet_width, pallet_weight,
    stackability, weight_pu, weight_hu, designationen, designationru)
values
  (1, "1234XXX", "Head Rest", "80090905", 1000, 100, 1.0,
   true, 1000, 1200, 800, 500.5, 2, 1.0, 15,
   "Head Rest", "Подголовник"),
  (2, "880055X-CO", "Anchor", "71190900", 2000, 100, 1,
   true, 1000, 1200, 800, 500.5, 2, 1, 15,
   "Head Rest", "Ручник");

insert into customers
  (customerid, city, country, customer_code, email, is_active, name, post_code, street)
values
  (1,"Luga", "RU", 1672000000, "luga@mail.pl", true, "LugaName", "00-200", "Popov"),
  (2,"Kaluga", "RU", 1462000000, "kaluga@mail.pl", true, "KalugaName", "22-222", "Nosov"),
  (3,"St.Petersburg", "RU", 1573000000, "stpet@mail.pl", true, "St.P.Name", "55-555", "Pushkin");

insert into suppliers
  (supplierid, city, country, email, is_active, name, post_code, street, vendor_code)
values
  (1,"Warsaw", "PL", "email@mail.pl", true, "GRAMMER", "00-200", "Krakowska", 123456),
  (2,"London", "EN", "barton@mail.pl", true, "BARTON", "22-222", "Johnson", 777000),
  (3,"Paris", "FR", "noname@mail.pl", true, "NoName", "55-555", "DeGol", 333555);

insert into customer_agreement
  (customer_agreementid, customer_customerid)
VALUES
  (5500000001, 1),
  (5500000010, 2),
  (5500000100, 3);

insert into storage_location
  (storage_locationid, code, name)
VALUES
  (1, "EX01", "STP_LUGA"),
  (2, "EX11", "KALUGA_LUGA"),
  (3, "EX10", "BUENOS");

insert into suplier_agreement
  (supplier_agreementid, supplier_supplierid, storage_locationid)
VALUES
  (4400000001, 1, 1),
  (4400000010, 2, 2),
  (4400000100, 3, 3);

insert into reference_supplier_agreement
  (referenceid, supplier_agreementid)
values
  (1, 4400000001),
  (2, 4400000010);

insert into reference_customer_agreement
  (referenceid, customer_agreementid)
VALUES
  (1, 5500000001),
  (2, 5500000010);

