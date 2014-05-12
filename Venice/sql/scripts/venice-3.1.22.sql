--liquibase formatted sql
--changeset andri:1
ALTER TABLE fin_ar_funds_in_recon_record
ADD card_number varchar(100)s
ALTER TABLE ven_order_payment
ADD card_number varchar(100)
--rollback card number in fin_ar_funds_in_recon_record
--ALTER TABLE fin_ar_funds_in_recon_record DROP COLUMN card_number
--rollback card number in ven_order_payment
--ALTER TABLE ven_order_payment DROP COLUMN card_number