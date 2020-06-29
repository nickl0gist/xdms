ALTER TABLE manifest_reference
ADD COLUMN reception_number VARCHAR(15) NULL DEFAULT NULL AFTER stackability,
ADD COLUMN delivery_number VARCHAR(15) NULL DEFAULT NULL AFTER reception_number;