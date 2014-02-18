package com.gdn.venice.server.app.finance.presenter.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FinSalesRecordSessionEJBRemote;
import com.gdn.venice.facade.VenReturItemSessionEJBRemote;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.VenReturItem;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.util.VeniceConstants;

public class FetchSalesSettlementRecordDataCommand implements RafDsCommand {
	RafDsRequest request;
		
	public FetchSalesSettlementRecordDataCommand(RafDsRequest request) {
		this.request = request;
	}

	@Override
	public RafDsResponse execute() {		
		RafDsResponse rafDsResponse = new RafDsResponse();
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();
			List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
			Locator<Object> locator = null;
			
			try {
				locator = new Locator<Object>();
				
				FinSalesRecordSessionEJBRemote finSalesRecordSessionHome = (FinSalesRecordSessionEJBRemote) locator.lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");
				VenReturItemSessionEJBRemote venReturItemSessionHome = (VenReturItemSessionEJBRemote) locator.lookup(VenReturItemSessionEJBRemote.class, "VenReturItemSessionEJBBean");
				
				List<FinSalesRecord> finSalesRecordList = null;
				
				if(criteria == null){
					String query = "select o from FinSalesRecord o join fetch o.venOrderItem oi "
	                    + "where o.finApprovalStatus.approvalStatusDesc='Approved' "
	                    + "and o.cxFinanceDate is not null";					
					finSalesRecordList = finSalesRecordSessionHome.queryByRange(query, 0, 100);
				}else{
					FinSalesRecord finSalesRecord = new FinSalesRecord();
				
					 JPQLSimpleQueryCriteria approvedCriteria = new JPQLSimpleQueryCriteria(), cxFinanceCriteria = new JPQLSimpleQueryCriteria();
		             approvedCriteria.setFieldName(DataNameTokens.FINSALESRECORD_FINAPPROVALSTATUS_APPROVALSTATUSDESC);
		             approvedCriteria.setOperator("equals");
		             approvedCriteria.setValue("Approved");
		             approvedCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FINSALESRECORD_FINAPPROVALSTATUS_APPROVALSTATUSDESC));
		             criteria.add(approvedCriteria);
		
		             cxFinanceCriteria.setFieldName(DataNameTokens.FINSALESRECORD_CXF_DATE);
		             cxFinanceCriteria.setOperator("isNotNull");
		             cxFinanceCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FINSALESRECORD_CXF_DATE));
		             criteria.add(cxFinanceCriteria);

	             	criteria.setBooleanOperator("AND");
					finSalesRecordList = finSalesRecordSessionHome.findByFinSalesRecordLike(finSalesRecord, criteria, 0, 0);	
				}
				String desc=null;
				String sqlQuery =null;
				for ( FinSalesRecord finSalesRecordItem : finSalesRecordList) {
					HashMap<String, String> map = new HashMap<String, String>();			
					desc=null;
					
					sqlQuery = "select o from VenReturItem o where o.wcsReturItemId ='"+finSalesRecordItem.getVenOrderItem().getWcsOrderItemId()+"' and o.venReturStatus.orderStatusId="+VeniceConstants.VEN_ORDER_STATUS_RF;
					List<VenReturItem> returItem = venReturItemSessionHome.queryByRange(sqlQuery, 0, 0);					
					if(!returItem.isEmpty() && returItem.size()>0 ){
						desc=" ( Retur - Refund )";
					}						
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_WCSMERCHANTID, finSalesRecordItem.getVenOrderItem().getVenMerchantProduct().getVenMerchant().getWcsMerchantId());
					map.put(DataNameTokens.FINSALESRECORD_SALESRECORDID, finSalesRecordItem.getSalesRecordId()+"");
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_VENPARTY_FULLORLEGALNAME, finSalesRecordItem.getVenOrderItem().getVenMerchantProduct().getVenMerchant().getVenParty().getFullOrLegalName()+(desc!=null?desc:""));
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENORDER_WCSORDERID, finSalesRecordItem.getVenOrderItem().getVenOrder().getWcsOrderId());
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_WCSORDERITEMID, finSalesRecordItem.getVenOrderItem().getWcsOrderItemId());
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_WCSPRODUCTNAME, finSalesRecordItem.getVenOrderItem().getVenMerchantProduct().getWcsProductName() +(desc!=null?desc:""));
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_QUANTITY, desc==null?(finSalesRecordItem.getVenOrderItem().getQuantity()+""):(finSalesRecordItem.getVenOrderItem().getQuantity() * new Double(-1)+""));
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_PRICE, desc==null?finSalesRecordItem.getVenOrderItem().getPrice()+"":finSalesRecordItem.getVenOrderItem().getPrice().negate()+"");
	
					map.put(DataNameTokens.FINSALESRECORD_VENORDERITEM_TOTAL, desc==null?finSalesRecordItem.getVenOrderItem().getTotal()+"":finSalesRecordItem.getVenOrderItem().getTotal().negate()+"");
					map.put(DataNameTokens.FINSALESRECORD_GDNCOMMISIONAMOUNT, desc==null?finSalesRecordItem.getGdnCommissionAmount()+"":finSalesRecordItem.getGdnCommissionAmount().negate()+"");
					map.put(DataNameTokens.FINSALESRECORD_GDNTRANSACTIONFEEAMOUNT, desc==null?finSalesRecordItem.getGdnTransactionFeeAmount()+"":finSalesRecordItem.getGdnTransactionFeeAmount().negate()+"");
					map.put(DataNameTokens.FINSALESRECORD_PPH23_AMOUNT, desc==null?finSalesRecordItem.getPph23Amount()+"":finSalesRecordItem.getPph23Amount().negate()+"");
					
					BigDecimal sum = finSalesRecordItem.getVenOrderItem().getTotal().subtract(finSalesRecordItem.getGdnCommissionAmount()!=null?finSalesRecordItem.getGdnCommissionAmount():new BigDecimal(0)).subtract(finSalesRecordItem.getGdnTransactionFeeAmount()!=null?finSalesRecordItem.getGdnTransactionFeeAmount():new BigDecimal(0)) ;	

					map.put(DataNameTokens.FINSALESRECORD_JUMLAH , desc==null?sum+"":sum.negate()+"");	
					map.put(DataNameTokens.FINSALESRECORD_CXF_DATE, finSalesRecordItem.getCxFinanceDate()!=null?finSalesRecordItem.getCxFinanceDate()+"":"");	
					
									
					dataList.add(map);
				}
				rafDsResponse.setStatus(0);
				rafDsResponse.setStartRow(request.getStartRow());
				rafDsResponse.setTotalRows(finSalesRecordList.size());
				rafDsResponse.setEndRow(request.getStartRow()+finSalesRecordList.size());
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
