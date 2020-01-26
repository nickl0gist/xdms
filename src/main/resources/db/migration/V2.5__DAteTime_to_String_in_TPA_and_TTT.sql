ALTER TABLE ttt
CHANGE COLUMN ttt_arrival_date_plan ttt_arrival_date_plan VARCHAR(19) NOT NULL ,
CHANGE COLUMN ttt_arrival_date_real ttt_arrival_date_real VARCHAR(19) NULL DEFAULT NULL;

ALTER TABLE tpa
CHANGE COLUMN departure_plan departure_plan VARCHAR(19) NOT NULL ,
CHANGE COLUMN departure_real departure_real VARCHAR(19) NULL DEFAULT NULL ;

ALTER TABLE tpa
ADD UNIQUE INDEX uniqueNameAndPlanDate (name ASC, departure_plan ASC);

ALTER TABLE ttt
ADD UNIQUE INDEX uniqueTruckNameAndPlanDate (truck_name ASC, ttt_arrival_date_plan ASC);