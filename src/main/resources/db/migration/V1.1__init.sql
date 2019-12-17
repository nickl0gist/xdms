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

insert into storage_location
  (storage_locationid, code, name)
VALUES
  (1, "EX01", "STP_LUGA"),
  (2, "EX11", "KALUGA_LUGA"),
  (3, "EX10", "BUENOS");

insert into reference
  (referenceid, number, name, hs_code, pcs_perpu, pcs_perhu, weight, weight_of_packaging, designationen, designationru,
  pallet_height, pallet_length, pallet_width, pallet_weight, stackability, customer_customerid,
  customer_agreement, supplier_supplierid, supplier_agreement, storage_locationid, is_active)
values
  (1, "1234XXX", "Head Rest", "80090905", 1000, 100, 50, 1.5, "designatione Head Rest", "designatione Подголовник",
   1200, 1000, 800, 15, 1, 1, 5500000001, 2, 440000000, 1, true),
  (2, "2000XXX-CO", "Anchor belt", "89090909", 1500, 200, 25, 2.3, "designatione Anchor belt", "designatione Якорь длинный, не короткий",
   1100, 900, 1000, 20, 2, 2, 5500001111, 3, 4400022222, 2, true);
