START TRANSACTION;

ALTER TABLE manifest_reference_plan
ADD COLUMN qty_real INT(11) NULL DEFAULT '0' AFTER qty_planned,
ADD COLUMN pallet_qty_real INT(11) NULL DEFAULT '0' AFTER pallet_qty_planned,
ADD COLUMN box_qty_real INT(11) NULL DEFAULT '0' AFTER box_qty_planned,
ADD COLUMN gross_weight_real DOUBLE NULL DEFAULT '0' AFTER gross_weight_planned,
ADD COLUMN pallet_width DOUBLE NOT NULL AFTER pallet_length,
ADD COLUMN net_weight_real DOUBLE NOT NULL DEFAULT '0' AFTER pallet_weigth,
ADD COLUMN pallet_id VARCHAR(10) NULL DEFAULT NULL AFTER net_weight_real,
ADD COLUMN stackability INT(10) NOT NULL AFTER pallet_id,
CHANGE COLUMN manifest_planid manifest_reference_id BIGINT(20) NOT NULL AUTO_INCREMENT ,
CHANGE COLUMN qty qty_planned INT(11) NOT NULL ,
CHANGE COLUMN pallet_qty pallet_qty_planned INT(11) NOT NULL ,
CHANGE COLUMN box_qty box_qty_planned INT(11) NOT NULL ,
CHANGE COLUMN gross_weight gross_weight_planned DOUBLE NOT NULL ,
RENAME TO manifest_reference ;


ALTER TABLE tpa_manifest_real 
DROP FOREIGN KEY FK_tpa_manifest_real_has_real_manifest,
RENAME TO tpa_manifest_reference;

ALTER TABLE tpa_manifest_reference
CHANGE COLUMN manifest_realid manifest_reference_id BIGINT(20) NOT NULL ;

ALTER TABLE tpa_manifest_reference
ADD CONSTRAINT FK_tpa_has_manifest_reference
  FOREIGN KEY (manifest_reference_id)
  REFERENCES manifest_reference (manifest_reference_id);

DROP TABLE manifest_reference_real;

COMMIT;