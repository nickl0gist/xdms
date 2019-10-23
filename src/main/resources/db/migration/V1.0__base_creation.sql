create table customer_agreement (
  customer_agreementid varchar(30) not null,
  customer_customerid bigint not null,
  primary key (customer_agreementid)) engine=InnoDB;

create table customers (
  customerid bigint not null auto_increment,
  name varchar(150) not null,
  customer_code bigint not null,
  country varchar(50) not null,
  city varchar(50) not null,
  email varchar(200) not null,
  is_active BIT default true not null,
  post_code varchar(30) not null,
  street varchar(50) not null,
  primary key (customerid)) engine=InnoDB;

create table manifest_reference_plan (
  manifest_planid bigint not null auto_increment,
  manifestid bigint not null,
  referenceid bigint not null,
  qty integer not null,
  pallet_qty integer not null,
  gross_weight double precision not null,
  pallet_height double precision not null,
  pallet_length double precision not null,
  pallet_weight double precision not null,
  primary key (manifest_planid)) engine=InnoDB;

create table manifest_reference_real (
  manifest_realid bigint not null auto_increment,
  manifest_planid bigint not null,
  qty int default 0,
  gross_weight double default 0,
  net_weight double default 0,
  pallet_qty int default 0,
  pallet_height double default 0,
  pallet_length double default 0,
  pallet_weight double default 0,
  primary key (manifest_realid)) engine=InnoDB;

create table manifests (
  manifestid bigint not null auto_increment,
  manifest_code varchar(255) not null,
  pallet_qty integer not null,
  totalldm double precision not null,
  total_weight double precision not null,
  customerid bigint not null,
  supplierid bigint not null, primary key (manifestid)) engine=InnoDB;

create table reference (
  referenceid bigint not null auto_increment,
  number varchar(30) not null,
  name varchar(200) not null,
  pcs_perpu integer not null,
  pcs_perhu integer not null,
  pallet_height integer not null,
  pallet_length integer not null,
  pallet_weight double precision not null,
  pallet_width integer not null,
  stackability integer not null,
  weight double precision not null,
  weight_hu double precision not null,
  weight_pu double precision not null,
  hs_code varchar(30) not null,
  is_active BIT default true not null,
  designationen varchar(200) not null,
  designationru varchar(200) not null,
  designationde varchar(200),
  primary key (referenceid)) engine=InnoDB;

create table reference_customer_agreement (
  referenceid bigint not null,
  customer_agreementid varchar(30) not null,
  primary key (referenceid, customer_agreementid)) engine=InnoDB;

create table reference_supplier_agreement (
  referenceid bigint not null,
  supplier_agreementid varchar(30) not null,
  primary key (referenceid, supplier_agreementid)) engine=InnoDB;

/*create table reference_storage_location (
  referenceid bigint not null,
  storage_locationid bigint not null,
  primary key (referenceid, storage_locationid)) engine=InnoDB;*/

create table roles (
  id bigint not null auto_increment,
  name varchar(255) not null,
  description varchar(200),
  is_active BIT default true,
  primary key (id)) engine=InnoDB;

create table suplier_agreement (
  supplier_agreementid varchar(30) not null,
  supplier_supplierid bigint not null,
  storage_locationid bigint not null,
  primary key (supplier_agreementid)) engine=InnoDB;

create table storage_location (
  storage_locationid bigint not null auto_increment,
  code varchar(100) not null,
  name varchar(100) not null,
  primary key (storage_locationid)) engine=InnoDB;

create table suppliers (
  supplierid bigint not null auto_increment,
  vendor_code bigint not null,
  name varchar(150) not null,
  is_active BIT default true not null,
  country varchar(50) not null,
  post_code varchar(30) not null,
  city varchar(50) not null,
  street varchar(50) not null,
  email varchar(200),
  primary key (supplierid)) engine=InnoDB;

create table tpa (
  tpaid bigint not null auto_increment,
  name varchar(255) not null,
  status_statusid bigint not null,
  departure_plan datetime not null,
  departure_real datetime,
  tpa_days_setting_id bigint not null,
  primary key (tpaid)) engine=InnoDB;

create table tpa_days_settings (
  id bigint not null auto_increment,
  local_time time not null,
  wh_customerid bigint not null,
  working_daysid bigint not null,
  primary key (id)) engine=InnoDB;

create table tpa_manifest_real (
  tpaid bigint not null,
  manifest_realid bigint not null,
  primary key (manifest_realid, tpaid)) engine=InnoDB;

create table tpa_status (
  statusid bigint not null auto_increment,
  status_name varchar(255) not null,
  primary key (statusid)) engine=InnoDB;

create table ttt (
  tttid bigint not null auto_increment,
  truck_name varchar(255) not null,
  ttt_arrival_date_plan datetime not null,
  ttt_arrival_date_real datetime,
  ttt_status_ttt_statusid bigint not null,
  warehouse_warehouseid bigint not null,
  primary key (tttid)) engine=InnoDB;

create table ttt_manifest (
  tttid bigint not null,
  manifestid bigint not null,
  primary key (tttid, manifestid)) engine=InnoDB;

create table ttt_status (
  ttt_statusid bigint not null auto_increment,
  ttt_status_name varchar(255) not null,
  primary key (ttt_statusid)) engine=InnoDB;

create table users (
  id bigint not null auto_increment,
  username varchar(100) not null,
  first_name varchar(50) not null,
  last_name varchar(50) not null,
  email varchar(200) not null,
  password varchar(100) not null,
  role_id bigint not null, primary key (id)) engine=InnoDB;

create table warehouse_customer (
  wh_customerid bigint not null,
  customerid bigint not null,
  warehouseid bigint not null,
  is_active BIT default true not null,
  primary key (wh_customerid)) engine=InnoDB;

create table warehouses (
  warehouseid bigint not null,
  city varchar(50) not null,
  country varchar(50) not null,
  email varchar(200) not null,
  is_active BIT default true,
  name varchar(200) not null,
  post_code varchar(30) not null,
  street varchar(50) not null,
  wh_typeid bigint not null,
  primary key (warehouseid)) engine=InnoDB;

create table wh_type (
  wh_typeid bigint not null,
  name varchar(200) not null,
  is_active BIT default true,
  primary key (wh_typeid)) engine=InnoDB;

create table working_days (
  id bigint not null auto_increment,
  day_name integer not null,
  is_active BIT default true,
  primary key (id)) engine=InnoDB;

alter table customer_agreement add constraint
  FK_customer_has_agreement foreign key (customer_customerid)
  references customers (customerid);

alter table customers add constraint UK_customer_code unique (customer_code);
alter table manifests add constraint UK_manifest_code unique (manifest_code);
alter table reference add constraint UK_ref_number unique (number);

/*alter table reference_customer_agreement add constraint UK_rl1id0jix6976fo1ikry3vu09 unique (customer_agreementid)
alter table reference_storage_location add constraint UK_i4vtrxqu6fhqmelu5sco48jmt unique (storage_locationid)
alter table tpa_manifest_real add constraint UK_7j5p50symyoimu87n87os1euw unique (manifest_realid)
alter table tpa_manifest_real add constraint UK_pt72pdep74yqgar64irt0vbn5 unique (tpaid)
alter table ttt_manifest add constraint UK_fijik3g56ghj2d6a1nk1d4566 unique (manifestid)
alter table reference_supplier_agreement add constraint UK_26ykruesknbxghbaxv75xdvod unique (supplier_agreementid)*/

alter table storage_location add constraint UK_stor_loc_code unique (code);
alter table suppliers add constraint UK_suppl_vendor_code unique (vendor_code);
alter table tpa_status add constraint UK_tpa_status_name unique (status_name);
alter table users add constraint UK_username unique (username);

/*manifest_reference_plan*/
alter table manifest_reference_plan add constraint
FKo4wuxs28nsijk64upl6pdl5c0 foreign key (manifestid) references manifests (manifestid);

alter table manifest_reference_plan add constraint
FKfjm5jnnvg6qe52yvm4k4g2vbb foreign key (referenceid) references reference (referenceid);

alter table manifest_reference_real add constraint
FK74lp45imiv3618v5lv54oennk foreign key (manifest_planid) references manifest_reference_plan (manifest_planid);

/*manifests*/

alter table manifests add constraint
FK992eduddxe502hvc0oamp09ya foreign key (customerid) references customers (customerid);
alter table manifests add constraint
FKeesr8uocnnbiwbh0vt71ahltq foreign key (supplierid) references suppliers (supplierid);

/*reference_customer_agreement*/
alter table reference_customer_agreement
add constraint FK_reference_to_ca foreign key (referenceid)
references reference (referenceid);

alter table reference_customer_agreement
add constraint FK_customer_agreement foreign key (customer_agreementid)
references customer_agreement (customer_agreementid);

/*reference_supplier_agreement*/
alter table reference_supplier_agreement
add constraint FK_reference_to_sa
foreign key (referenceid) references reference (referenceid);

alter table reference_supplier_agreement
add constraint FK_supplier_agreement
foreign key (supplier_agreementid) references suplier_agreement (supplier_agreementid);

alter table suplier_agreement
  add constraint FKahck418ythrr37tiw3xjm21ha
  foreign key (supplier_supplierid) references suppliers (supplierid);

/* = storage_location to suplier_agreement = */
alter table suplier_agreement
  add constraint FK_suplier_agreement_to_storage_locationid
  foreign key (storage_locationid)
  references storage_location (storage_locationid);

alter table tpa
add constraint FK7s8srhwg1mj56ssrk7yxoey1x
foreign key (status_statusid) references tpa_status (statusid);
alter table tpa
add constraint FK8nfv1n6cuakgdrgakr85to79p
foreign key (tpa_days_setting_id) references tpa_days_settings (id);

alter table tpa_days_settings
add constraint FK5ftk3gsra4kerf60orrmwsr4q
foreign key (wh_customerid) references warehouse_customer (wh_customerid);

alter table tpa_days_settings
add constraint FKqne6ro4itsqhu9htwgkjdwi8c
foreign key (working_daysid) references working_days (id);

alter table tpa_manifest_real
add constraint FK_tpa_manifest_real_has_real_manifest
foreign key (manifest_realid) references manifest_reference_real (manifest_realid);

alter table tpa_manifest_real
add constraint FK_tpa_manifest_has_real_real_tpa
foreign key (tpaid) references tpa (tpaid);

alter table ttt
add constraint FK_ttt_has_ttt_status
foreign key (ttt_status_ttt_statusid) references ttt_status (ttt_statusid);

alter table ttt
add constraint FK_ttt_has_warehouse
foreign key (warehouse_warehouseid) references warehouses (warehouseid);

alter table ttt_manifest
add constraint FK_ttt_manifest_has_ttt
foreign key (tttid) references ttt (tttid);

alter table ttt_manifest
add constraint FK_ttt_manifest_has_manifest
foreign key (manifestid) references manifests (manifestid);

alter table warehouse_customer
add constraint FK_warehouse_customer_has_costomer
foreign key (customerid) references customers (customerid);

alter table warehouse_customer
add constraint FK_warehouse_customer_has_warehouse
foreign key (warehouseid) references warehouses (warehouseid);

alter table warehouses
add constraint FK_wh_has_type
foreign key (wh_typeid) references wh_type (wh_typeid);

alter table users
add constraint FK_user_has_role
foreign key (role_id) references roles (id);

