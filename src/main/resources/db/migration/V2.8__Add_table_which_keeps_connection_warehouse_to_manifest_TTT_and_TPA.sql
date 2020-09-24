CREATE TABLE xdms_flat_fw.warehouse_manifest (
  manifest_id BIGINT NOT NULL,
  wh_id BIGINT NOT NULL,
  ttt_id INT NOT NULL,
  tpa_id INT NULL,
  pallet_qty INT NULL,
  box_qty_real INT NULL,
  gross_weight DOUBLE NULL,
  net_weight_real DOUBLE NULL,
  pallet_height DOUBLE NULL,
  pallet_width DOUBLE NULL,
  pallet_length DOUBLE NULL,
  kpi_label BIT NULL DEFAULT b'0',
  kpi_document BIT NULL DEFAULT b'0',
  kpi_manifest BIT NULL DEFAULT b'0',
  PRIMARY KEY (manifest_id, wh_id),
  INDEX warehouse_id_idx (wh_id ASC) VISIBLE,
  CONSTRAINT warehouse_id
    FOREIGN KEY (wh_id)
    REFERENCES xdms_flat_fw.warehouses (warehouseid)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT manifest_id
    FOREIGN KEY (manifest_id)
    REFERENCES xdms_flat_fw.manifests (manifestid)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'the table keeps information about movement of the manifest through particular warehouse, real qty, KPI, incoming TTT, outgoing TPA';


