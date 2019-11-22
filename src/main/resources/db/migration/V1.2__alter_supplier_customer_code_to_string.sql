ALTER table suppliers CHANGE COLUMN vendor_code vendor_code VARCHAR(15) CHARACTER SET 'utf16';
ALTER table customers CHANGE COLUMN customer_code customer_code VARCHAR(15) CHARACTER SET 'utf16';

ALTER TABLE manifests
ADD COLUMN pallet_qty_real INT(11) NULL AFTER total_weight_planned,
ADD COLUMN totalldm_real DOUBLE NULL AFTER pallet_qty_real,
ADD COLUMN total_weight_real DOUBLE NULL AFTER totalldm_real,
CHANGE COLUMN pallet_qty pallet_qty_planned INT(11) NOT NULL ,
CHANGE COLUMN totalldm totalldm_planned DOUBLE NOT NULL ,
CHANGE COLUMN total_weight total_weight_planned DOUBLE NOT NULL;
