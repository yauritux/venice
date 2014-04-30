--liquibase formatted sql
--changeset andri:1
insert into ven_order_status (order_status_id, order_status_code, order_status_short_desc) values(24,'CR','Customer Request');
--rollback delete from ven_order_status where order_status_id = 24;