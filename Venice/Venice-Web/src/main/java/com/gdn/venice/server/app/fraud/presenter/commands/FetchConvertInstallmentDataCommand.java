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
	
	/*private void setupDBConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");		
		conn = DriverManager.getConnection("jdbc:postgresql://" + dbHost +":" + dbPort + "/" + dbName, dbUsername, dbPassword);
	}*/
	
	/*private static final String CONVERT_INSTALLMENT_LIST_SQL = "select op.order_payment_id, op.wcs_payment_id, o.wcs_order_id, o.order_date, op.reference_id, op.amount, op.tenor, op.installment, op.interest, op.interest_installment, op.installment_sent_flag, c.customer_user_name, p.full_or_legal_name, op.wcs_payment_type_id " +
																												"from ven_order o " +
																												"left join ven_order_payment_allocation opa on o.order_id=opa.order_id " +
																												"left join ven_order_payment op on opa.order_payment_id=op.order_payment_id " +
																												"left join ven_customer c on c.customer_id=o.customer_id " +
																												"left join ven_party p on p.party_id=c.party_id " +
																												"left join ven_bin_credit_limit_estimate b on b.bin_number=substr(op.masked_credit_card_number,0,7) " +
																												"where op.amount>500000 and o.order_status_id= " + VeniceConstants.VEN_ORDER_STATUS_FP +
																												" and op.wcs_payment_type_id in (" +VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSBCAInstallment + ", " + VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSCreditCard + ")" +
																												" and b.bank_name='BCA' and op.installment_sent_flag=false and o.order_date>=?";
	*/
	@Override
	public RafDsResponse execute() {
		/*RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		PreparedStatement psInstallmentList = null;      
      	ResultSet rsInstallmentList = null;
      	ArrayList<Installment> InstallmentList = null;	*/

		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		Locator<Object> locator=null;
		
		
      	
		try{
	/*		InstallmentList = new ArrayList<Installment>();
            psInstallmentList = conn.prepareStatement(CONVERT_INSTALLMENT_LIST_SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date paramDate = DateUtils.addDays((Date) sdf.parse(sdf.format(calendar.getTime())), -30);
            
            psInstallmentList.setDate(1, new java.sql.Date(paramDate.getTime()));
    		rsInstallmentList = psInstallmentList.executeQuery();
    		
    		rsInstallmentList.last();
			int totalInstallmentList = rsInstallmentList.getRow();
			rsInstallmentList.beforeFirst();
			rsInstallmentList.next();
    					*/

			locator = new Locator<Object>();					
			VenOrderPaymentAllocationSessionEJBRemote orderAllocationSessionHome = (VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");		
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();		

			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			

			
			String query="select o from VenBinCreditLimitEstimate ob, VenOrderPaymentAllocation o join fetch o.venOrder oi join fetch o.venOrderPayment oe join fetch oi.venCustomer oa left join oa.venParty ou" +
					" where substring(oe.maskedCreditCardNumber,0,7) = ob.binNumber and oe.amount>500000 and oi.venOrderStatus.orderStatusId= " + VeniceConstants.VEN_ORDER_STATUS_FP +
					" and oe.venWcsPaymentType.wcsPaymentTypeId in (" +VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSBCAInstallment + ", " + VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSCreditCard + ") "+
					" and ob.bankName='BCA' ";
		/*	if(criteria==null){		
				
			}else{
				List<JPQLSimpleQueryCriteria> simpleCriteriaList = criteria.getSimpleCriteria();
				for (int i=0;i<simpleCriteriaList.size();i++) {
					query = query + " and";
					if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_WCSPAYMENTID)) {
						query = query + " oe.wcsPaymentId like '%"+simpleCriteriaList.get(i).getValue()+"%'";
					}else if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID)) {
						query = query + " oi.wcsOrderId like '%"+simpleCriteriaList.get(i).getValue()+"%'";
					}else if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_REFERENCEID)) {
						query = query + " oe.referenceId like '%"+simpleCriteriaList.get(i).getValue()+"%'";
					}
				}
			}*/

			if (criteria!=null){
				List<JPQLSimpleQueryCriteria> simpleCriteriaList = criteria.getSimpleCriteria();
				for (int i=0;i<simpleCriteriaList.size();i++) {
					if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG)) {
						if(simpleCriteriaList.get(i).getValue().equals("Yes")){
							query = query + " and oe.installmentSentFlag is true ";
						}else if(simpleCriteriaList.get(i).getValue().equals("No")){
							query = query + " and oe.installmentSentFlag is false ";
						}
					}
				}
			}			
			
			venOrderPaymentAllocationList = orderAllocationSessionHome.queryByRange(query,0, 20);
			
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
