package com.gdn.venice.finance.batch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.EJBException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FinSalesRecordSessionEJBRemote;
import com.gdn.venice.facade.VenOrderItemAdjustmentSessionEJBRemote;
import com.gdn.venice.facade.VenOrderItemSessionEJBRemote;
import com.gdn.venice.facade.VenOrderItemStatusHistorySessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentAllocationSessionEJBRemote;
import com.gdn.venice.facade.VenSettlementRecordSessionEJBRemote;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemAdjustment;
import com.gdn.venice.persistence.VenOrderItemStatusHistory;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenSettlementRecord;
import com.gdn.venice.util.VeniceConstants;

public class SalesRecordCleanupGeneratorBatchJob {

    protected static Logger _log = null;
    protected static final String AIRWAYBILL_ENGINE_PROPERTIES_FILE = System.getenv("VENICE_HOME") + "/conf/airwaybill-engine.properties";
    BigDecimal transFee = new BigDecimal(0);

    private SalesRecordCleanupGeneratorBatchJob() {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.finance.batch.SalesRecordCleanupGeneratorBatchJob");
    }

    private Properties getAirwayBillEngineProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(AIRWAYBILL_ENGINE_PROPERTIES_FILE));
        } catch (Exception e) {
            _log.error("Error getting airwaybill-engine.properties", e);
            e.printStackTrace();
            return null;
        }
        return properties;
    }

    private FinSalesRecord createSalesRecord(Locator<Object> locator, VenOrderItem venOrderItem) throws Exception {

        FinSalesRecordSessionEJBRemote salesRecordHome = (FinSalesRecordSessionEJBRemote) locator
                .lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");

        FinSalesRecord finSalesRecord = null;

        try {
            /*
             * If the sales record for the order item on the AWB does not
             * exist then create it. If the sales record exists already then update it
             */
            List<FinSalesRecord> finSalesRecordList = salesRecordHome.queryByRange("select o from FinSalesRecord o where o.venOrderItem.orderItemId = " + venOrderItem.getOrderItemId(), 0, 0);
            if (finSalesRecordList.isEmpty()) {
                _log.debug("create new sales record");
                finSalesRecord = new FinSalesRecord();

                finSalesRecord.setVenOrderItem(venOrderItem);

                // Calculate the sales record
                _log.info("calculate sales record for order item id: " + venOrderItem.getWcsOrderItemId());
                finSalesRecord = this.calculateSalesRecord(locator, finSalesRecord, venOrderItem);

                if (finSalesRecord == null) {
                    _log.error("calculate sales record skipped, because can not get commission value or trans fee from MTA service");
                    return null;
                }

                finSalesRecord.setSalesTimestamp(new Timestamp(System.currentTimeMillis()));

                finSalesRecord.setReconcileDate(venOrderItem.getCxDate());

                finSalesRecord.setCxMtaDate(venOrderItem.getCxMtaDate());

                if (venOrderItem.getCxDate() != null && venOrderItem.getCxMtaDate() != null) {
                    finSalesRecord.setCxFinanceDate(venOrderItem.getCxDate().after(venOrderItem.getCxMtaDate())
                            ? venOrderItem.getCxDate() : venOrderItem.getCxMtaDate());
                }

                FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
                finApprovalStatus.setApprovalStatusId(VeniceConstants.FIN_APPROVAL_STATUS_APPROVED);
                finSalesRecord.setFinApprovalStatus(finApprovalStatus);
            }

            return finSalesRecord;
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = "An exception occured when create Or Update SalesRecord:" + e.getMessage();
            _log.error(errMsg);
            return null;
        }
    }

    private BigDecimal getCustomerDownPayment(Locator<Object> locator, VenOrderItem venOrderItem, BigDecimal handlingFee, List<VenOrderItemAdjustment> adjustmentList) {
        /* 
         * Calculate as the catalog price of the item multiplied by the 
         * quantity plus the shipping (shipping cost plus insurance) plus the
         * handling fee for the payment (only for the first order item to go
         * to CX status) minus any adjustment.
         */

        _log.debug("Set customer downpayment");
        BigDecimal customerDownpaymentAmount = venOrderItem.getPrice().multiply(new BigDecimal(venOrderItem.getQuantity()));
        customerDownpaymentAmount.setScale(2, RoundingMode.HALF_UP);
        customerDownpaymentAmount = customerDownpaymentAmount.add(venOrderItem.getShippingCost()).add(venOrderItem.getInsuranceCost().add(venOrderItem.getGiftWrapPrice()));

        /*
         * If it is the first order item to go CX then add 
         * the handling fees to the downpayment amount
         * JASA HANDLING
         */
        if (this.isHandlingFeeExist(locator, venOrderItem) == false) {
            _log.debug("handling fee not exist, set handling fee");
            customerDownpaymentAmount = customerDownpaymentAmount.add(handlingFee);
        }

        /*
         *  Get any marginPromo and add them to the
         *  customer downpayment amount (note
         *  that they are -ve numbers)
         */
        _log.debug("Get marginPromo");
        BigDecimal marginPromo = new BigDecimal(0);
        marginPromo.setScale(2, RoundingMode.HALF_UP);

        for (VenOrderItemAdjustment adjustment : adjustmentList) {
            marginPromo = marginPromo.add(adjustment.getAmount());
        }

        return customerDownpaymentAmount.add(marginPromo);
    }

    private BigDecimal getMerchantPaymentAmount(VenSettlementRecord venSettlementRecord, VenOrderItem venOrderItem,
            BigDecimal commissionValue, BigDecimal transactionFee, BigDecimal merchantPromotion) {
        BigDecimal merchantPaymentAmount = new BigDecimal(0);
        merchantPaymentAmount.setScale(2, RoundingMode.HALF_UP);

        //if rebate, amount=(item_price * qty)
        //if commission, amount=(item_price * qty) - merchantPromo - Commission value (with tax taken out) - transactionFee (with tax taken out)
        //else set to 0
        if (venSettlementRecord.getCommissionType().equals(VeniceConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE)) {
            _log.debug("commission type rebate");
            merchantPaymentAmount = venOrderItem.getPrice().multiply(new BigDecimal(venOrderItem.getQuantity()));
        } else if (venSettlementRecord.getCommissionType().equals(VeniceConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION)) {
            _log.debug("commission type commission");
            merchantPaymentAmount = (venOrderItem.getPrice().multiply(new BigDecimal(venOrderItem.getQuantity())))
                    .subtract(commissionValue).subtract(transactionFee).subtract(merchantPromotion);
        }

        return merchantPaymentAmount;
    }

    /**
     * calculateSalesRecord - Calculates and journals the sales record for the
     * order item on the airway bill
     *
     * @param finSalesRecord is the existing sales record
     * @param logAirwayBill is the airway bill to use for the calculations
     * @return the completed calculated sales record
     */
    private FinSalesRecord calculateSalesRecord(Locator<Object> locator,
            FinSalesRecord finSalesRecord, VenOrderItem orderItem) {
        _log.info("Start calculate sales record");
        try {
            VenOrderItemSessionEJBRemote orderItemHome = (VenOrderItemSessionEJBRemote) locator
                    .lookup(VenOrderItemSessionEJBRemote.class, "VenOrderItemSessionEJBBean");

            VenSettlementRecordSessionEJBRemote settlementRecordHome = (VenSettlementRecordSessionEJBRemote) locator
                    .lookup(VenSettlementRecordSessionEJBRemote.class, "VenSettlementRecordSessionEJBBean");

            List<VenOrderItem> itemList = orderItemHome.queryByRange("select o from VenOrderItem o where o.orderItemId = " + orderItem.getOrderItemId(), 0, 0);
            VenOrderItem venOrderItem = itemList.get(0);

            /*
             * Get the commission amount from the settlement record
             *  - to be subtracted from merchant payment after tax taken out
             */
            _log.debug("Check settlement record");
            List<VenSettlementRecord> settlementRecordList = settlementRecordHome
                    .queryByRange("select o from VenSettlementRecord o where o.venOrderItem.orderItemId = " + venOrderItem.getOrderItemId(), 0, 0);

            VenSettlementRecord venSettlementRecord = new VenSettlementRecord();

            if (settlementRecordList == null || settlementRecordList.isEmpty()) {
                _log.debug("Settlement record is empty, request from MTA");
                VenSettlementRecord result = getMerchantSettlement(venOrderItem);
                if (result == null) {
                    _log.error("can not get merchant settlement from MTA service");
                    return null;
                } else {
                    result.setSettlementRecordTimestamp(new Timestamp(System.currentTimeMillis()));
                    venSettlementRecord = settlementRecordHome.persistVenSettlementRecord(result);
                }
            } else {
                _log.debug("Settlement record is not empty");
                venSettlementRecord = settlementRecordList.get(0);

                if (venSettlementRecord.getCommissionValue() == null
                        || venOrderItem.getTransactionFeeAmount() == null
                        || venSettlementRecord.getPph23() == null) {
                    _log.debug("One of the field in settlement record is empty, request from MTA");
                    VenSettlementRecord result = getMerchantSettlement(venOrderItem);
                    if (result == null) {
                        _log.error("can not get merchant settlement from MTA service");
                        return null;
                    } else {
                        _log.debug("If null set value from MTA");
                        if (venSettlementRecord.getPph23() == null) {
                            venSettlementRecord.setPph23(result.getPph23());
                        }

                        if (venSettlementRecord.getCommissionValue() == null) {
                            venSettlementRecord.setCommissionValue(result.getCommissionValue());
                        }

                        venSettlementRecord = settlementRecordHome.mergeVenSettlementRecord(venSettlementRecord);
                    }
                } else {
                    _log.debug("Settlement record complete, don't request settlement from MTA");
                    venSettlementRecord = settlementRecordList.get(0);
                }
            }

            /*
             * Calculate the downpayment amount (payment must have been made
             * already or the item would not have gone to logistics)
             * 
             */
            VenOrderItemAdjustmentSessionEJBRemote orderItemAdjustmentHome = (VenOrderItemAdjustmentSessionEJBRemote) locator
                    .lookup(VenOrderItemAdjustmentSessionEJBRemote.class, "VenOrderItemAdjustmentSessionEJBBean");

            List<VenOrderItemAdjustment> adjustmentList = orderItemAdjustmentHome.queryByRange("select o from VenOrderItemAdjustment o where o.venOrderItem.orderItemId = " + venOrderItem.getOrderItemId(), 0, 0);

            BigDecimal handlingFee = this.getHandlingFeesFromOrderPayments(locator, venOrderItem);

            _log.debug("Set customer downpayment");
            finSalesRecord.setCustomerDownpayment(getCustomerDownPayment(locator, venOrderItem, handlingFee, adjustmentList));

            /*
             * This is the divisor to use when calculating the PPN amount
             */
            _log.debug("Set ppn divisor");
            BigDecimal gdnPPN_Divisor = new BigDecimal(VeniceConstants.VEN_GDN_PPN_RATE).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).add(new BigDecimal(1));

            /*
             * This is the commission amount before the PPN is taken out
             */
            _log.debug("Set commission");
            BigDecimal gdnCommissionAmountBeforeTax = new BigDecimal(0);
            gdnCommissionAmountBeforeTax.setScale(2, RoundingMode.HALF_UP);

            _log.debug("Set Commission value from settlement record");
            gdnCommissionAmountBeforeTax = venSettlementRecord.getCommissionValue();

            finSalesRecord.setGdnCommissionAmount(gdnCommissionAmountBeforeTax);

            /*
             * This is the commission amount after the PPN is taken out
             */
            BigDecimal gdnCommissionAmountAfterTax = new BigDecimal(0);
            gdnCommissionAmountAfterTax.setScale(2, RoundingMode.HALF_UP);
            if (VeniceConstants.VEN_GDN_PPN_RATE > 0) {
                gdnCommissionAmountAfterTax = gdnCommissionAmountBeforeTax.divide(gdnPPN_Divisor, 2, RoundingMode.HALF_UP);
            }

            /*
             * This is the amount of PPN incurred by the commission
             */
            BigDecimal gdnCommissionAmountPPN = gdnCommissionAmountBeforeTax.subtract(gdnCommissionAmountAfterTax);


            /*
             * This is the handling fee for the payment from the order
             * before PPN is taken out.
             * 
             * Note that handling fees are only included for the 
             * first order item to go to CX status (i.e. have a sales record).
             */
            _log.debug("Set handling fee");
            BigDecimal gdnHandlingFeeAmountBeforeTax = new BigDecimal(0);
            gdnHandlingFeeAmountBeforeTax.setScale(2, RoundingMode.HALF_UP);

            /*
             * This is the handling fee after PPN is taken out
             */
            BigDecimal gdnHandlingFeeAmountAfterTax = new BigDecimal(0);
            gdnHandlingFeeAmountAfterTax.setScale(2, RoundingMode.HALF_UP);

            /*
             * This is the amount of PPN incurred by the handling fees
             */
            BigDecimal gdnHandlingFeePPN = new BigDecimal(0);
            gdnHandlingFeePPN.setScale(2, RoundingMode.HALF_UP);

            Boolean flagHandlingFeeExist = isHandlingFeeExist(locator, venOrderItem);
            _log.debug("Check handling fee exist");
            if (flagHandlingFeeExist == false) {
                _log.debug("handling fee not exist, set handling fee");
                gdnHandlingFeeAmountBeforeTax = handlingFee;

                if (VeniceConstants.VEN_GDN_PPN_RATE > 0) {
                    gdnHandlingFeeAmountAfterTax = gdnHandlingFeeAmountBeforeTax.divide(gdnPPN_Divisor, 2, RoundingMode.HALF_UP);
                }

                gdnHandlingFeePPN = gdnHandlingFeeAmountBeforeTax.subtract(gdnHandlingFeeAmountAfterTax);
            }

            _log.debug("gdnHandlingFeeAmountBeforeTax: " + gdnHandlingFeeAmountBeforeTax);
            finSalesRecord.setGdnHandlingFeeAmount(gdnHandlingFeeAmountBeforeTax);

            BigDecimal gdnTransactionFeeAmountBeforeTax = new BigDecimal(0);
            gdnTransactionFeeAmountBeforeTax.setScale(2, RoundingMode.HALF_UP);

            BigDecimal gdnTransactionFeeAmountPPN = new BigDecimal(0);

            /*                 
             * Cek trans fee, jika null maka request ke MTA
             */
            if (venOrderItem.getTransactionFeeAmount() == null) {
                _log.debug("[sales record generator] trans fee is null, set value from MTA");
                gdnTransactionFeeAmountBeforeTax = transFee;
                String sql = "update ven_order_item set transaction_fee_amount = " + transFee
                        + " where order_item_id = " + venOrderItem.getOrderItemId();
                _log.debug(sql);
                orderItemHome.getSingleResultUsingNativeQuery(sql);
            } else {
                _log.debug("[sales record generator] trans fee not null , set from order item");
                gdnTransactionFeeAmountBeforeTax = venOrderItem.getTransactionFeeAmount();
            }

            finSalesRecord.setGdnTransactionFeeAmount(gdnTransactionFeeAmountBeforeTax);

            BigDecimal gdnTransactionFeeAmountAfterTax = new BigDecimal(0);
            gdnTransactionFeeAmountAfterTax.setScale(2, RoundingMode.HALF_UP);

            if (VeniceConstants.VEN_GDN_PPN_RATE > 0) {
                gdnTransactionFeeAmountAfterTax = gdnTransactionFeeAmountBeforeTax.divide(gdnPPN_Divisor, 2, RoundingMode.HALF_UP);
            }
            gdnTransactionFeeAmountPPN = gdnTransactionFeeAmountBeforeTax.subtract(gdnTransactionFeeAmountAfterTax);


            /*
             * This is the amount of the promotion that is to be borne by the
             * merchant
             */
            _log.debug("Set merchant promotion");
            BigDecimal merchantPromotionAmount = getMerchantPromotionAmount(adjustmentList);
            finSalesRecord.setMerchantPromotionAmount(merchantPromotionAmount);

            /*
             * This is the promotion amount that the 3rd party is accountable
             * for.
             */
            _log.debug("Set 3rd party promotion");
            finSalesRecord.setThirdPartyPromotionAmount(getThirdPartyPromotionAmount(adjustmentList));

            /*
             * Calculate the GDN amount for the promotion
             * by subtracting the 3rd party promotion amounts
             * from the marginPromo
             */
            _log.debug("Set gdn promotion amount");
            finSalesRecord.setGdnPromotionAmount(getGdnPromotionAmount(adjustmentList));

            /*
             * Calculate the total payment amount that is 
             * due to the merchant for the order item.
             */
            _log.debug("Set merchant payment amount");
            finSalesRecord.setMerchantPaymentAmount(getMerchantPaymentAmount(venSettlementRecord, venOrderItem,
                    gdnCommissionAmountBeforeTax, gdnTransactionFeeAmountBeforeTax, merchantPromotionAmount));

            /*
             * Calculate the total of the logistics charges and PPN for the order item
             */
            _log.debug("Set logistic charge");
            BigDecimal totalLogisticsRelatedAmountBeforeTax = venOrderItem.getShippingCost().add(venOrderItem.getInsuranceCost());
            totalLogisticsRelatedAmountBeforeTax.setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalLogisticsRelatedAmountAfterTax = new BigDecimal(0);

            if (VeniceConstants.VEN_GDN_PPN_RATE > 0) {
                totalLogisticsRelatedAmountAfterTax = totalLogisticsRelatedAmountBeforeTax.divide(gdnPPN_Divisor, 2, RoundingMode.HALF_UP);
            }

            totalLogisticsRelatedAmountAfterTax.setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalLogisticsRelatedAmountPPN = totalLogisticsRelatedAmountBeforeTax.subtract(totalLogisticsRelatedAmountAfterTax);

            finSalesRecord.setTotalLogisticsRelatedAmount(totalLogisticsRelatedAmountAfterTax);

            /*
             * Calculate the gift wrap PPN and assign the amounts
             */
            _log.debug("Set gift wrap amount");
            BigDecimal gdnGiftWrapChargeAmountBeforeTax = venOrderItem.getGiftWrapPrice();
            gdnGiftWrapChargeAmountBeforeTax.setScale(2, RoundingMode.HALF_UP);

            BigDecimal gdnGiftWrapChargeAmountAfterTax = new BigDecimal(0);

            if (VeniceConstants.VEN_GDN_PPN_RATE > 0) {
                gdnGiftWrapChargeAmountAfterTax = gdnGiftWrapChargeAmountBeforeTax.divide(gdnPPN_Divisor, 2, RoundingMode.HALF_UP);
            }

            gdnGiftWrapChargeAmountAfterTax.setScale(2, RoundingMode.HALF_UP);
            BigDecimal gdnGiftWrapChargeAmountPPN = gdnGiftWrapChargeAmountBeforeTax.subtract(gdnGiftWrapChargeAmountAfterTax);

            finSalesRecord.setGdnGiftWrapChargeAmount(gdnGiftWrapChargeAmountBeforeTax);

            /*
             * Calculates the PPN amount for the sale
             * by adding the various PPN amounts
             * 
             */
            _log.debug("Set ppn amount");
            BigDecimal vatAmount = new BigDecimal(0);
            vatAmount = totalLogisticsRelatedAmountPPN.add(gdnCommissionAmountPPN).add(gdnTransactionFeeAmountPPN).add(gdnHandlingFeePPN).add(gdnGiftWrapChargeAmountPPN);

            vatAmount.setScale(2, RoundingMode.HALF_UP);
            finSalesRecord.setVatAmount(vatAmount);

            /*
             * Calculates the PPH 23		
             * (Commission + Trans Fee)*2%	 
             */
            _log.debug("Set pph 23 amount");
            BigDecimal pph23Amount = new BigDecimal(0);

            boolean isPPh23 = (venSettlementRecord.getPph23() != null) ? venSettlementRecord.getPph23() : false;
            if (isPPh23) {
                pph23Amount = ((finSalesRecord.getGdnCommissionAmount().add(finSalesRecord.getGdnTransactionFeeAmount())).divide(new BigDecimal(1.1),RoundingMode.HALF_UP)).multiply(new BigDecimal(0.02));
            }
            pph23Amount.setScale(2, RoundingMode.HALF_UP);
            _log.debug("pph 23 amount: " + pph23Amount);
            finSalesRecord.setPph23Amount(pph23Amount);

            _log.info("Done calculate sales record");

            //merge settlement record if there's changes
            settlementRecordHome.mergeVenSettlementRecord(venSettlementRecord);
        } catch (Exception e) {
            String errMsg = "An exception occured when calculating values for the sales record:" + e.getMessage();
            _log.error(errMsg);
            e.printStackTrace();
            return null;
        }
        return finSalesRecord;
    }

    /**
     * Calculates the GDN promotion amount for the order item
     *
     * @param locator is a locator to use for EJB lookup
     * @param venOrderItem is the order item in question
     * @param adjustmentList is a list of the marginPromo
     * @return the GDN promotion amount
     */
    private BigDecimal getGdnPromotionAmount(List<VenOrderItemAdjustment> adjustmentList) {
        BigDecimal gdnPromotionAmount = new BigDecimal(0);
        gdnPromotionAmount.setScale(2, RoundingMode.HALF_UP);

        try {
            for (VenOrderItemAdjustment adjustment : adjustmentList) {
                gdnPromotionAmount = gdnPromotionAmount.add(adjustment.getAmount()
                        .multiply(new BigDecimal(adjustment.getVenPromotion().getGdnMargin()))
                        .divide(new BigDecimal(100)));
            }
        } catch (Exception e) {
            String errorText = e.getMessage();
            _log.error(errorText);
            e.printStackTrace();
            throw new EJBException(errorText);
        }
        return gdnPromotionAmount;
    }

    /**
     * Calculates the third party promotion amount for the order item
     *
     * @param locator is a locator to use for EJB lookup
     * @param venOrderItem is the order item in question
     * @param adjustmentList is a list of the marginPromo
     * @return the third party promotion amount
     */
    private BigDecimal getThirdPartyPromotionAmount(List<VenOrderItemAdjustment> adjustmentList) {
        BigDecimal thirdPartyPromotionAmount = new BigDecimal(0);
        thirdPartyPromotionAmount.setScale(2, RoundingMode.HALF_UP);

        try {
            for (VenOrderItemAdjustment adjustment : adjustmentList) {
                thirdPartyPromotionAmount = thirdPartyPromotionAmount.add(adjustment.getAmount()
                        .multiply(new BigDecimal(adjustment.getVenPromotion().getOthersMargin()))
                        .divide(new BigDecimal(100)));
            }
        } catch (Exception e) {
            String errorText = e.getMessage();
            _log.error(errorText);
            e.printStackTrace();
            throw new EJBException(errorText);
        }
        return thirdPartyPromotionAmount;
    }

    /**
     * Returns the merchant promotion amount for the order item
     *
     * @param locator is a locator to use for EJB lookup
     * @param venOrderItem is the order item in question
     * @param adjustmentList is the list of marginPromo
     * @return the merchant promotion amount
     */
    private BigDecimal getMerchantPromotionAmount(List<VenOrderItemAdjustment> adjustmentList) {
        BigDecimal merchantPromotionAmount = new BigDecimal(0);
        merchantPromotionAmount.setScale(2, RoundingMode.HALF_UP);

        try {
            for (VenOrderItemAdjustment adjustment : adjustmentList) {
                merchantPromotionAmount = merchantPromotionAmount.add(adjustment.getAmount()
                        .multiply(new BigDecimal(adjustment.getVenPromotion().getMerchantMargin()))
                        .divide(new BigDecimal(100)));
            }
        } catch (Exception e) {
            String errorText = e.getMessage();
            _log.error(errorText);
            e.printStackTrace();
            throw new EJBException(errorText);
        }
        return merchantPromotionAmount;
    }

    /**
     * Returns the sum of all the handling fees for payments attached to the
     * order
     *
     * @param locator a locator for the EJBs
     * @param venOrderItem the order item in question
     * @return the handling fees
     */
    private BigDecimal getHandlingFeesFromOrderPayments(Locator<Object> locator, VenOrderItem venOrderItem) {
        BigDecimal handlingFee = new BigDecimal(0);
        handlingFee.setScale(2, RoundingMode.HALF_UP);

        try {
            VenOrderPaymentAllocationSessionEJBRemote venOrderPaymentAllocationHome = (VenOrderPaymentAllocationSessionEJBRemote) locator
                    .lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");

            List<VenOrderPaymentAllocation> venOrderPaymentAllocationList =
                    venOrderPaymentAllocationHome.queryByRange("select o from VenOrderPaymentAllocation o where o.venOrder.orderId = " + venOrderItem.getVenOrder().getOrderId(), 0, 0);

            if (!venOrderPaymentAllocationList.isEmpty()) {
                _log.debug("handling fee found from payment: " + venOrderPaymentAllocationList.get(0).getVenOrderPayment().getHandlingFee());
                handlingFee = handlingFee.add(venOrderPaymentAllocationList.get(0).getVenOrderPayment().getHandlingFee());
            }
        } catch (Exception e) {
            String errorText = e.getMessage();
            _log.error(errorText);
            e.printStackTrace();
            throw new EJBException(errorText);
        }

        return handlingFee;
    }

    /**
     * Returns true if the order item is the first order item for the order
     * created in the Sales record
     *
     * @param locator is a Locator object to locate the EJB
     * @param venOrderItem is the order item in question
     * @return true if the item is the first else false
     */
    private boolean isHandlingFeeExist(Locator<Object> locator, VenOrderItem venOrderItem) {
        boolean flagHandlingFeeExist = false;
        try {
            FinSalesRecordSessionEJBRemote finSalesRecordHome = (FinSalesRecordSessionEJBRemote) locator
                    .lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");

            List<FinSalesRecord> finSalesRecordList = finSalesRecordHome.queryByRange("select o from FinSalesRecord o where o.venOrderItem.venOrder.orderId = " + venOrderItem.getVenOrder().getOrderId(), 0, 0);

            for (FinSalesRecord finSalesRecord : finSalesRecordList) {
                if (finSalesRecord.getGdnHandlingFeeAmount().compareTo(new BigDecimal(0)) == 1) {
                    flagHandlingFeeExist = true;
                    break;
                }
            }
        } catch (Exception e) {
            String errorText = e.getMessage();
            _log.error(errorText);
            e.printStackTrace();
            throw new EJBException(errorText);
        }

        return flagHandlingFeeExist;
    }

    private VenSettlementRecord getMerchantSettlement(VenOrderItem venOrderItem) {
        _log.debug("start getMerchantSettlement from MTA");

        VenSettlementRecord settlementTemp = new VenSettlementRecord();
        settlementTemp.setVenOrderItem(venOrderItem);

        BigDecimal commissionValue = new BigDecimal(0);
        commissionValue.setScale(2, RoundingMode.HALF_UP);

        transFee = new BigDecimal(0);
        transFee.setScale(2, RoundingMode.HALF_UP);

        String commType, settlementType, settlementCode;
        Boolean pph23;

        try {
            String url = getAirwayBillEngineProperties().getProperty("mtaAddress") + "MtaHttpServices?serviceType=MarginTransactionFeeRequest&orderItemId=" + venOrderItem.getWcsOrderItemId();
            URL obj = new URL(url);

            _log.info("Service: " + url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", "GdnWS/1.0");

            if (con.getResponseCode() == HttpStatus.SC_OK) {

                _log.info("service call to MTA service success, response status: " + con.getResponseCode());

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (response != null) {
                    _log.info("result from MTA: " + response.toString());

                    JSONObject jsonObject = new JSONObject(response.toString());

                    if (jsonObject.getBoolean("success")) {
                        _log.debug("data is available from MTA");

                        commissionValue = new BigDecimal(jsonObject.getDouble("commissionValue"));
                        settlementTemp.setCommissionValue(commissionValue);
                        _log.debug("commission value from MTA: " + commissionValue);

                        transFee = new BigDecimal(jsonObject.getDouble("transxFee"));
                        _log.debug("transFee from MTA: " + transFee);

                        commType = jsonObject.getString("commissionType");
                        settlementTemp.setCommissionType(commType);
                        _log.debug("commType from MTA: " + commType);

                        pph23 = jsonObject.getBoolean("pph23");
                        settlementTemp.setPph23(pph23);
                        _log.debug("pph23 from MTA: " + pph23);

                        settlementType = jsonObject.getString("settlementType");
                        settlementTemp.setSettlementRecordType(settlementType);
                        _log.debug("settlementType from MTA: " + settlementType);

                        settlementCode = jsonObject.getString("settlementCode");
                        settlementTemp.setSettlementCode(settlementCode);
                        _log.debug("settlementCode from MTA: " + settlementCode);
                    } else {
                        _log.debug("data is not available from MTA");
                        return null;
                    }
                }
            } else {
                _log.error("service call to MTA service failed, response status: " + con.getResponseCode());
                return null;
            }
        } catch (Exception e) {
            _log.error("service call to MTA service failed", e);
            throw new EJBException(e.getMessage());
        }

        return settlementTemp;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SalesRecordCleanupGeneratorBatchJob salesRecordJob = new SalesRecordCleanupGeneratorBatchJob();
        _log.info("Prepare to create sales record");

        Locator<Object> locator = null;
        FinSalesRecordSessionEJBRemote finSalesRecordHome;
        VenOrderItemSessionEJBRemote itemHome;
        
        FinSalesRecord salesRecord;

        try {
            Long startTime = System.currentTimeMillis();
            int count = 0, errorCount = 0;
            locator = new Locator<Object>();
            finSalesRecordHome = (FinSalesRecordSessionEJBRemote) locator
                    .lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");
            itemHome = (VenOrderItemSessionEJBRemote) locator
                    .lookup(VenOrderItemSessionEJBRemote.class, "VenOrderItemSessionEJBBean");

            String query = "select o from VenOrderItem o "
                    + " where o.salesBatchStatus in ('Ready Cleanup', 'Failed Cleanup') ";
            _log.info("Query: " + query);
            List<VenOrderItem> items = itemHome.queryByRange(query, 0, 1000);
            _log.info("Query returns: " + items.size() + " row(s)");

            List<VenOrderItem> failed = new ArrayList<VenOrderItem>();

            for (VenOrderItem item : items) {
                try {
                    item.setSalesBatchStatus("In Process Cleanup");
                    item = itemHome.mergeVenOrderItem(item);
                } catch (Exception e) {
                    _log.error("Update sales in process status failed for item: " + item.getWcsOrderItemId(), e);
                    failed.add(item);
                }
            }

            for (VenOrderItem item : items) {
                try {
                    salesRecord = null;
                    if (item.getSalesBatchStatus().equals("In Process Cleanup")) {
                        _log.debug("ready to create sales record");
                        salesRecord = salesRecordJob.createSalesRecord(locator, item);

                        if (salesRecord != null) {
                            finSalesRecordHome.persistFinSalesRecord(salesRecord);
                            item.setSalesBatchStatus("Ready Journal Cleanup");
                            count++;
                        } else {
                            item.setSalesBatchStatus("Failed Cleanup");
                            _log.error("Sales record creation failed for order item: " + item.getWcsOrderItemId());
                            errorCount++;
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    _log.error("Sales creation failed for order item: " + item.getWcsOrderItemId(), e);
                    item.setSalesBatchStatus("Failed Cleanup");
                } finally {
                    itemHome.getSingleResultUsingNativeQuery("update ven_order_item set sales_batch_status = '" + item.getSalesBatchStatus() + "' "
                            + " where order_item_id = " + item.getOrderItemId());
                }
            }
            Long endTime = System.currentTimeMillis();
            _log.info(count + " sales(s) generated, with duration:" + (endTime - startTime) + "ms");
            _log.info("Sales not created for: " + errorCount + " item(s)");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (locator != null) {
                    locator.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
