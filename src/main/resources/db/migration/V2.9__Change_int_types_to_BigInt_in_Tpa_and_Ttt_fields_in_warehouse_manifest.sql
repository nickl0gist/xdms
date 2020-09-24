ALTER TABLE xdms_flat_fw.warehouse_manifest 
CHANGE COLUMN ttt_id ttt_id BIGINT NOT NULL ,
CHANGE COLUMN tpa_id tpa_id BIGINT NULL DEFAULT NULL ;