package com.gdn.venice.server.app.fraud.presenter.commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FinSalesRecordSessionEJBRemote;
import com.gdn.venice.facade.VenMerchantProductSessionEJBRemote;
import com.gdn.venice.facade.VenOrderContactDetailSessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentAllocationSessionEJBRemote;
import com.gdn.venice.fraud.dataexport.bean.Installment;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.DateToXsdDatetimeFormatter;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.VeniceConstants;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Fetch Command for convert installment
 * 
 * @author Roland
 */

public class FetchConvertInstallmentDataCommand implements RafDsCommand {

	RafDsRequest request;
	
	public FetchConvertInstallmentDataCommand(RafDsRequest request) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException{
		this.request=request;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		Locator<Object> locator=null;		
      	
		try{
			Calendar oneMonthAgo = Calendar.getInstance();
			oneMonthAgo.add(Calendar.DAY_OF_MONTH, -30);
			
			String dayOfOneMonthAgo = oneMonthAgo.get(Calendar.YEAR)+"-"+(oneMonthAgo.get(Calendar.MONTH)+1)+"-"+oneMonthAgo.get(Calendar.DAY_OF_MONTH);
			locator = new Locator<Object>();					
			VenOrderPaymentAllocationSessionEJBRemote orderAllocationSessionHome = (VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");		
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();		

			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			String query="select o from VenBinCreditLimitEstimate ob, VenOrderPaymentAllocation o join fetch o.venOrder oi join fetch o.venOrderPayment oe join fetch oi.venCustomer oa left join oa.venParty ou" +
					" where substring(oe.maskedCreditCardNumber,0,7) = ob.binNumber and oe.amount>500000 and oi.venOrderStatus.orderStatusId= " + VeniceConstants.VEN_ORDER_STATUS_FP +
					" and oe.venWcsPaymentType.wcsPaymentTypeId in (" +VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSBCAInstallment + ", " + VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSCreditCard + ") "+
					" and ob.bankName='BCA' ";

			if (criteria!=null){
				List<JPQLSimpleQueryCriteria> simpleCriteriaList = criteria.getSimpleCriteria();
				for (int i=0;i<simpleCriteriaList.size();i++) {
					if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG)) {
						if(simpleCriteriaList.get(i).getValue().toLowerCase().equals("yes")){
							query = query + " and oe.installmentSentFlag is true";
						}else if(simpleCriteriaList.get(i).getValue().toLowerCase().equals("no")){
							query = query + " and oe.installmentSentFlag is false";
						}
					}
				}
			}			
			query = query+" and oi.orderDate >= '"+dayOfOneMonthAgo+"'";
			venOrderPaymentAllocationList = orderAllocationSessionHome.queryByRange(query,0, 0);
			
				for(VenOrderPaymentAllocation venOrderPaymentAllocation:venOrderPaymentAllocationList ){
                    HashMap<String, String> map = new HashMap<String, String>();
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_ORDERPAYMENTID, venOrderPaymentAllocation.getVenOrderPayment().getOrderPaymentId()!=null?venOrderPaymentAllocation.getVenOrderPayment().getOrderPaymentId().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID, venOrderPaymentAllocation.getVenOrder().getWcsOrderId()!=null?venOrderPaymentAllocation.getVenOrder().getWcsOrderId().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_WCSPAYMENTID, venOrderPaymentAllocation.getVenOrderPayment().getWcsPaymentId()!=null?venOrderPaymentAllocation.getVenOrderPayment().getWcsPaymentId():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_REFERENCEID, venOrderPaymentAllocation.getVenOrderPayment().getReferenceId()!=null?venOrderPaymentAllocation.getVenOrderPayment().getReferenceId().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID, venOrderPaymentAllocation.getVenOrderPayment().getVenWcsPaymentType().getWcsPaymentTypeId()!=null?venOrderPaymentAllocation.getVenOrderPayment().getVenWcsPaymentType().getWcsPaymentTypeId().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT, venOrderPaymentAllocation.getVenOrderPayment().getAmount()!=null?venOrderPaymentAllocation.getVenOrderPayment().getAmount().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR, venOrderPaymentAllocation.getVenOrderPayment().getTenor()!=null?venOrderPaymentAllocation.getVenOrderPayment().getTenor().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_CUSTOMERUSERNAME, venOrderPaymentAllocation.getVenOrder().getVenCustomer().getCustomerUserName()!=null?venOrderPaymentAllocation.getVenOrder().getVenCustomer().getCustomerUserName().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_VENPARTY_FULLORLEGALNAME, venOrderPaymentAllocation.getVenOrder().getVenCustomer().getVenParty().getFullOrLegalName()!=null?venOrderPaymentAllocation.getVenOrder().getVenCustomer().getVenParty().getFullOrLegalName().toString():"");		
//					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG, venOrderPaymentAllocation.getVenOrderPayment().getInstallmentSentFlag()==true?"Yes":(venOrderPaymentAllocation.getVenOrderPayment().getInstallmentSentFlag()==false?"No":""));
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG, venOrderPaymentAllocation.getVenOrderPayment().getInstallmentSentFlag()!=null?venOrderPaymentAllocation.getVenOrderPayment().getInstallmentSentFlag().toString():"");
									
					dataList.add(map);				
				}
				rafDsResponse.setStatus(0);
				rafDsResponse.setStartRow(request.getStartRow());
				rafDsResponse.setTotalRows(dataList.size());
				rafDsResponse.setEndRow(request.getStartRow()+dataList.size());
			}catch (Exception e) {
	            e.printStackTrace();
	            rafDsResponse.setStatus(-1);
	        } finally {
	            try {
	                if (locator != null) {
	                    locator.close();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
