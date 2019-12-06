insert into tpa_status (status_name)
values ("CLOSED"),("DELAYED"),("IN_PROGRESS"),("BUFFER");

insert into ttt_status (ttt_status_name)
values ("PENDING"), ("DELAYED"), ("ARRIVED");

create table tpa_manifest (
  tpaid bigint not null,
  manifest_id bigint not null,
  primary key (manifest_id, tpaid)) engine=InnoDB;

alter table tpa_manifest
add constraint FK_tpa_manifest_has_manifest
foreign key (manifest_id) references manifests (manifestid);

alter table tpa_manifest
add constraint FK_tpa_manifest_has_tpa
foreign key (tpaid) references tpa (tpaid);
