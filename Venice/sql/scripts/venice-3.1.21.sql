--liquibase formatted sql
--changeset andri:1
insert into ven_order_status (order_status_id, order_status_code, order_status_short_desc) values(24,'CR','Customer Request');
insert into insert into ven_wcs_payment_type values(23,'VisaCreditCard','VISA Credit Card')
--rollback delete from ven_order_status where order_status_id = 24;
--rollback delete from ven_wcs_payment_type where wcs_payment_type_id = 24;