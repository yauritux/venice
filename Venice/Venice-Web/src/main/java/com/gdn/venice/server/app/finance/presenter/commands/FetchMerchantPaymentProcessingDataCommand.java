package com.gdn.venice.server.app.finance.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FinSalesRecordSessionEJBRemote;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.DateToXsdDatetimeFormatter;

public class FetchMerchantPaymentProcessingDataCommand implements RafDsCommand {

    RafDsRequest request;
    protected static Logger _log = null;

    public FetchMerchantPaymentProcessingDataCommand(RafDsRequest request) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.finance.presenter.commands.FetchMerchantPaymentProcessingDataCommand");
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        JPQLAdvancedQueryCriteria criteria = request.getCriteria();
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        Locator<Object> locator = null;

        try {
            locator = new Locator<Object>();

            FinSalesRecordSessionEJBRemote finSalesRecordSessionHome = (FinSalesRecordSessionEJBRemote) locator
                    .lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");

            List<FinSalesRecord> finSalesRecordList;

            FinSalesRecord salesRecord = new FinSalesRecord();

            if (criteria != null) {
                JPQLSimpleQueryCriteria approvedCriteria = new JPQLSimpleQueryCriteria(),
                        cxFinanceCriteria = new JPQLSimpleQueryCriteria(),
                        apPaymentCriteria = new JPQLSimpleQueryCriteria(),
                        paymentStatusCriteria = new JPQLSimpleQueryCriteria();
                approvedCriteria.setFieldName(DataNameTokens.FINSALESRECORD_FINAPPROVALSTATUS_APPROVALSTATUSDESC);
                approvedCriteria.setOperator("equals");
                approvedCriteria.setValue("Approved");
                approvedCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FINSALESRECORD_FINAPPROVALSTATUS_APPROVALSTATUSDESC));
                criteria.add(approvedCriteria);

                cxFinanceCriteria.setFieldName(DataNameTokens.FINSALESRECORD_CXF_DATE);
                cxFinanceCriteria.setOperator("isNotNull");
                cxFinanceCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FINSALESRECORD_CXF_DATE));
                criteria.add(cxFinanceCriteria);

                apPaymentCriteria.setFieldName(DataNameTokens.FINSALESRECORD_FINAPPAYMENT);
                apPaymentCriteria.setOperator("isNull");
                apPaymentCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FINSALESRECORD_FINAPPAYMENT));
                criteria.add(apPaymentCriteria);

                paymentStatusCriteria.setFieldName(DataNameTokens.FINSALESRECORD_PAYMENT_STATUS);
                paymentStatusCriteria.setOperator("isNull");
                paymentStatusCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FINSALESRECORD_PAYMENT_STATUS));
                criteria.add(paymentStatusCriteria);

                criteria.setBooleanOperator("AND");

                finSalesRecordList = finSalesRecordSessionHome.findByFinSalesRecordLike(salesRecord, criteria, 0, 0);
            } else {
                String select = "select o from FinSalesRecord o join fetch o.venOrderItem oi "
                        + "where o.finApprovalStatus.approvalStatusDesc='Approved' "
                        + "and o.finApPayment is null and o.cxFinanceDate is not null and o.paymentStatus is null";
                finSalesRecordList = finSalesRecordSessionHome.queryByRange(select, 0, 0);
            }
            
            _log.debug("query without criteria, finSalesRecordList awal size: " + finSalesRecordList.size());
            int length = finSalesRecordList.size();
            for (int i = 0; i < length; i++) {
                salesRecord = finSalesRecordList.get(i);
                if (salesRecord.getVenOrderItem().getVenSettlementRecords() != null && salesRecord.getVenOrderItem().getVenSettlementRecords().size() > 0) {
                    if (!salesRecord.getVenOrderItem().getVenSettlementRecords().get(salesRecord.getVenOrderItem().getVenSettlementRecords().size() - 1).getCommissionType().equals("CM")
                            && !salesRecord.getVenOrderItem().getVenSettlementRecords().get(salesRecord.getVenOrderItem().getVenSettlementRecords().size() - 1).getCommissionType().equals("RB")) {
                        _log.info("commission type is not (CM or RB)");
                        finSalesRecordList.remove(salesRecord);
                        --i;
                        --length;
                        continue;
                    }
                }
            }
            _log.debug("finSalesRecordList akhir size: " + finSalesRecordList.size());
            for (int i = 0; i < finSalesRecordList.size(); i++) {
                salesRecord = finSalesRecordList.get(i);

                //only show Sales Records that do not have FinApPayment -> means not settled
                if (salesRecord.getFinApPayment() == null) {
                    HashMap<String, String> map = new HashMap<String, String>();

                    DateToXsdDatetimeFormatter formatter = new DateToXsdDatetimeFormatter();

                    map.put(DataNameTokens.FINSALESRECORD_SALESRECORDID, salesRecord.getSalesRecordId().toString());
                    map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_WCSMERCHANTID,
                            (salesRecord.getVenOrderItem() != null
                            && salesRecord.getVenOrderItem().getVenMerchantProduct() != null
                            && salesRecord.getVenOrderItem().getVenMerchantProduct().getVenMerchant() != null)
                            ? salesRecord.getVenOrderItem().getVenMerchantProduct().getVenMerchant().getWcsMerchantId() : "");
                    map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_VENPARTY_FULLORLEGALNAME,
                            (salesRecord.getVenOrderItem() != null
                            && salesRecord.getVenOrderItem().getVenMerchantProduct() != null
                            && salesRecord.getVenOrderItem().getVenMerchantProduct().getVenMerchant() != null
                            && salesRecord.getVenOrderItem().getVenMerchantProduct().getVenMerchant().getVenParty() != null)
                            ? salesRecord.getVenOrderItem().getVenMerchantProduct().getVenMerchant().getVenParty().getFullOrLegalName() : "");
                    map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_WCSORDERITEMID, salesRecord.getVenOrderItem().getWcsOrderItemId());
                    map.put(DataNameTokens.FINSALESRECORD_MCX_DATE, formatter.format(salesRecord.getCxMtaDate()));
                    map.put(DataNameTokens.FINSALESRECORD_CXF_DATE, formatter.format(salesRecord.getCxFinanceDate()));
                    map.put(DataNameTokens.FINSALESRECORD_MERCHANTPAYMENTAMOUNT, (salesRecord.getMerchantPaymentAmount() != null) ? salesRecord.getMerchantPaymentAmount().toString() : "");

                    map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENSETTLEMENTRECORDS_PPH23_FLAG, (salesRecord.getVenOrderItem() != null && salesRecord.getVenOrderItem().getVenSettlementRecords() != null && salesRecord.getVenOrderItem().getVenSettlementRecords().get(0).getPph23() != null) ? (salesRecord.getVenOrderItem().getVenSettlementRecords().get(0).getPph23() == true ? "Yes" : "No") : "");
                    map.put(DataNameTokens.FINSALESRECORD_PPH23_AMOUNT, salesRecord.getPph23Amount() != null ? salesRecord.getPph23Amount().toString() : "");
                    dataList.add(map);
                }
            }
            rafDsResponse.setStatus(0);
            rafDsResponse.setStartRow(request.getStartRow());
            rafDsResponse.setTotalRows(dataList.size());
            rafDsResponse.setEndRow(request.getStartRow() + finSalesRecordList.size());
        } catch (Exception e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        } finally {
            try {
                locator.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        rafDsResponse.setData(dataList);

        return rafDsResponse;
    }
}
