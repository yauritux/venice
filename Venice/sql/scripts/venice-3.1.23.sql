--liquibase formatted sql
--changeset daniel:1
CREATE TABLE frd_parameter_rule_46
( 
  id bigint NOT NULL,
  description character varying(40),
  "value" integer,
  CONSTRAINT frd_parameter_rule_46_pkey PRIMARY KEY (id)
);
insert into frd_parameter_rule_46 values (1,'same CC, order history<2',-10);
insert into frd_parameter_rule_46 values (2,'same CC, order history>2',-20);
insert into frd_parameter_rule_46 values (3,'different CC, order history<2',-5);
insert into frd_parameter_rule_46 values (4,'different CC, order history>2',-10);
CREATE TABLE frd_parameter_rule_47
(
  id bigint NOT NULL,
  description character varying(40),
  "value" integer,
  CONSTRAINT frd_parameter_rule_47_pkey PRIMARY KEY (id)
);
insert into frd_parameter_rule_47 values (1,'same CC, order history<2',-20);
insert into frd_parameter_rule_47 values (2,'same CC, order history>2',-30);
insert into frd_parameter_rule_47 values (3,'different CC, order history<2',-10);
insert into frd_parameter_rule_47 values (4,'different CC, order history>2',-15);
--rollback drop table frd_parameter_rule_46;
--rollback delete from frd_parameter_rule_46 where id=1;
--rollback delete from frd_parameter_rule_46 where id=2;
--rollback delete from frd_parameter_rule_46 where id=3;
--rollback delete from frd_parameter_rule_46 where id=4;
--rollback drop table frd_parameter_rule_47;
--rollback delete from frd_parameter_rule_47 where id=1;
--rollback delete from frd_parameter_rule_47 where id=2;
--rollback delete from frd_parameter_rule_47 where id=3;
--rollback delete from frd_parameter_rule_47 where id=4;


--liquibase formatted sql
--changeset daniel:1
ALTER TABLE venice.venice.ven_order_item ADD COLUMN logisticsEtd NUMERIC(10) NULL;
--rollback ALTER TABLE venice.venice.ven_order_item DROP COLUMN logisticsEtd






