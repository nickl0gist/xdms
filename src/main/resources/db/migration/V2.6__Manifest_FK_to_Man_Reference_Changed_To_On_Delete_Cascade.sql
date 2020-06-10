ALTER TABLE manifest_reference
DROP FOREIGN KEY FKo4wuxs28nsijk64upl6pdl5c0;
ALTER TABLE manifest_reference
ADD CONSTRAINT FKo4wuxs28nsijk64upl6pdl5c0
  FOREIGN KEY (manifestid)
  REFERENCES manifests (manifestid)
  ON DELETE CASCADE
  ON UPDATE RESTRICT;