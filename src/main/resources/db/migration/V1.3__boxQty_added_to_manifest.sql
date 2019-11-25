ALTER TABLE manifests
ADD COLUMN box_qty_real INT(11) NULL AFTER pallet_qty_real,
ADD COLUMN box_qty_planned INT(11) NULL AFTER pallet_qty_planned;

alter table manifest_reference_plan
add column box_qty INT(11) NOT NULL AFTER pallet_qty;

alter table manifest_reference_real
add column box_qty INT(11) NOT NULL DEFAULT 0 AFTER pallet_qty;