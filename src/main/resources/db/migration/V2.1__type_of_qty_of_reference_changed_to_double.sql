ALTER TABLE manifest_reference 
CHANGE COLUMN qty_planned qty_planned DOUBLE NOT NULL ,
CHANGE COLUMN qty_real qty_real DOUBLE NULL DEFAULT '0' ;

ALTER TABLE reference
CHANGE COLUMN pcs_perhu pu_perhu INT(11) NOT NULL ;