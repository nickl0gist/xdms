ALTER TABLE warehouse_customer
ADD COLUMN transit_time VARCHAR(15) NOT NULL DEFAULT 'P0DT1H0M' AFTER warehouseid;

ALTER TABLE warehouses
ADD COLUMN time_zone VARCHAR(6) NOT NULL AFTER url_code;

ALTER TABLE customers
ADD COLUMN time_zone VARCHAR(6) NOT NULL AFTER country;
