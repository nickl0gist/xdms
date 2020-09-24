ALTER TABLE xdms_flat_fw.warehouse_manifest 
DROP FOREIGN KEY manifest_id,
DROP FOREIGN KEY warehouse_id;
ALTER TABLE xdms_flat_fw.warehouse_manifest 
CHANGE COLUMN ttt_id ttt_id BIGINT NOT NULL ,
CHANGE COLUMN tpa_id tpa_id BIGINT NULL DEFAULT NULL ,
ADD INDEX ttt_id_idx (ttt_id ASC) VISIBLE,
ADD INDEX tpa_idx_idx (tpa_id ASC) VISIBLE;
;
ALTER TABLE xdms_flat_fw.warehouse_manifest 
ADD CONSTRAINT manifest_idx
  FOREIGN KEY (manifest_id)
  REFERENCES xdms_flat_fw.manifests (manifestid)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
ADD CONSTRAINT warehouse_idx
  FOREIGN KEY (wh_id)
  REFERENCES xdms_flat_fw.warehouses (warehouseid)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT,
ADD CONSTRAINT ttt_idx
  FOREIGN KEY (ttt_id)
  REFERENCES xdms_flat_fw.ttt (tttid)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
ADD CONSTRAINT tpa_idx
  FOREIGN KEY (tpa_id)
  REFERENCES xdms_flat_fw.tpa (tpaid)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
