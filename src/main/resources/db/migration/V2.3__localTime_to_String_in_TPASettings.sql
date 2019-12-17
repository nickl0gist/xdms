ALTER TABLE tpa_days_settings
DROP FOREIGN KEY FK5ftk3gsra4kerf60orrmwsr4q;
ALTER TABLE tpa_days_settings
DROP INDEX FK5ftk3gsra4kerf60orrmwsr4q ;

ALTER TABLE warehouse_customer
CHANGE COLUMN wh_customerid wh_customerid BIGINT(20) NOT NULL AUTO_INCREMENT ;

ALTER TABLE tpa_days_settings
ADD CONSTRAINT FK_tpa_settings_wh_customer
  FOREIGN KEY (wh_customerid)
  REFERENCES warehouse_customer (wh_customerid)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;

ALTER TABLE tpa_days_settings
ADD COLUMN transitTime VARCHAR(15) NOT NULL DEFAULT 'P0DT1H0M' AFTER local_time;

INSERT INTO working_days (day_name, is_active)
VALUES (1, true), (2, true), (3, true), (4, true), (5, true), (6, true), (7, false);

insert into customers (name, customer_code, country, city, email, is_active, post_code, street) VALUES
('XD Świe', 'xd_swieb', 'PL', 'Świebodzice', 'pawel@wieczorek.pl', true, '04-200', 'Lotnicza - 1'),
('TXD Gró', 'txd_gro', 'PL', 'Grójec', 'sławek@barscz.pl', true, '05-600', 'Spółdzielcza - 2'),
('CC IRUN', 'cc_irun', 'ES', 'Irun', 'itziar@dominges.es', true, '99-992', 'EPO - 20'),
('TXD STD', 'txd_stad', 'GE', 'Stadthagen', 'udo@muller.ge', true, '20-212', 'Muller - 20'),
('CC ROMANY', 'cc_bamb', 'RO', 'Bambury', 'roman@romamiks.ro', true, '20-212', 'Muller - 20');

insert into warehouse_customer (warehouseid, customerid, is_active) VALUES
(1, 1, true), (1, 2, true), (1, 3, true), (1, 5, true), (1, 7, true),
(2, 1, true), (2, 2, true), (2, 3, true),
(3, 4, true), (3, 5, true), (3, 7, true),
(4, 1, true), (4, 2, true), (4, 3, true),
(5, 4, true), (5, 5, true), (5, 7, true);


