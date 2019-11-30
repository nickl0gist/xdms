ALTER TABLE manifest_reference_real
ADD COLUMN pallet_id VARCHAR(10) NULL AFTER pallet_weight;

insert into wh_type
  (wh_typeid, name)
VALUE
  (3, "TXD");