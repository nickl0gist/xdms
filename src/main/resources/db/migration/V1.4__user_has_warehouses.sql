create table user_warehouse (
  user_id bigint not null,
  warehouse_id bigint not null,
  primary key (user_id, warehouse_id)) engine=InnoDB;

alter table user_warehouse
  add constraint FK_user_warehouse_has_users
  foreign key (user_id) references users (id);

alter table user_warehouse
  add constraint FK_user_warehouse_has_warehouses
  foreign key (warehouse_id) references warehouses (warehouseid);
  
insert into wh_type
  (wh_typeid, name)
VALUES
  (1, "CC"),
  (2, "XD");

insert into warehouses
  (warehouseid, city, country, email, is_active, name, post_code, street, wh_typeid)
VALUES
  (1, "Świebodzice", "PL", "pawel@wieczorek.pl", true, "CC Świe", "04-200", "Lotnicza - 1", 1),
  (2, "Grójec", "PL", "sławek@barscz.pl", true, "XD Gró", "05-600", "Spółdzielcza - 2", 2),
  (3, "Irun", "ES", "itziar@dominges.es", false, "CC IRUN", "99-992", "EPO - 20", 1),
  (4, "Stadthagen", "GE", "udo@muller.ge", true, "XD STD", "20-212", "Muller - 20", 2),
  (5, "Bambury", "RO", "roman@romamiks.ro", false, "CC ROMANY", "20-212", "Muller - 20", 1);
    
INSERT into user_warehouse
  (user_id, warehouse_id)
values 
  (1, 1),
  (1, 2),
  (2, 1),
  (2, 2),
  (2, 4);