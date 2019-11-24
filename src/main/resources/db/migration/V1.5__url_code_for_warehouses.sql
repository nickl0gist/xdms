alter table warehouses
ADD COLUMN url_code VARCHAR (11) NOT NULL;

update warehouses set url_code = "cc_swieb" where warehouseid = 1;
update warehouses set url_code = "xd_gro" where warehouseid = 2;
update warehouses set url_code = "cc_irun" where warehouseid = 3;
update warehouses set url_code = "xd_stad" where warehouseid = 4;
update warehouses set url_code = "cc_bamb" where warehouseid = 5;