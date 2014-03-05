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

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.fraud.dataexport.bean.Installment;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.VeniceConstants;

/**
 * Fetch Command for convert installment
 * 
 * @author Roland
 */

public class FetchConvertInstallmentDataCommand implements RafDsCommand {

	RafDsRequest request;
	private static String CONFIG_FILE = System.getenv("VENICE_HOME") + "/admin/config.properties";
	private String dbHost = "";
	private String dbPort = "";
	private String dbUsername = "";
	private String dbPassword = "";
	private String environment = "";
	private String dbName = "";
	private static Connection conn;
	
	public FetchConvertInstallmentDataCommand(RafDsRequest request) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException{
		this.request=request;
		
    	Properties prop = new Properties();
		prop.load(new FileInputStream(CONFIG_FILE));
		environment = prop.getProperty("environment");
		dbHost = prop.getProperty(environment + ".dbHost");
		dbPort = prop.getProperty(environment + ".dbPort");
		dbUsername = prop.getProperty(environment + ".dbUsername");
		dbPassword = prop.getProperty(environment + ".dbPassword");
		dbName = prop.getProperty(environment + ".dbName");
		
		setupDBConnection();
	}
	
	private void setupDBConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");		
		conn = DriverManager.getConnection("jdbc:postgresql://" + dbHost +":" + dbPort + "/" + dbName, dbUsername, dbPassword);
	}
	
	private static final String CONVERT_INSTALLMENT_LIST_SQL = "select op.order_payment_id, op.wcs_payment_id, o.wcs_order_id, o.order_date, op.reference_id, op.amount, op.tenor, op.installment, op.interest, op.interest_installment, op.installment_sent_flag, c.customer_user_name, p.full_or_legal_name, op.wcs_payment_type_id " +
																												"from ven_order o " +
																												"left join ven_order_payment_allocation opa on o.order_id=opa.order_id " +
																												"left join ven_order_payment op on opa.order_payment_id=op.order_payment_id " +
																												"left join ven_customer c on c.customer_id=o.customer_id " +
																												"left join ven_party p on p.party_id=c.party_id " +
																												"left join ven_bin_credit_limit_estimate b on b.bin_number=substr(op.masked_credit_card_number,0,7) " +
																												"where op.amount>500000 and o.order_status_id= " + VeniceConstants.VEN_ORDER_STATUS_FP +
																												" and op.wcs_payment_type_id in (" +VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSBCAInstallment + ", " + VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSCreditCard + ")" +
																												" and b.bank_name='BCA' and op.installment_sent_flag=false and o.order_date>=?";
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		PreparedStatement psInstallmentList = null;      
      	ResultSet rsInstallmentList = null;
      	ArrayList<Installment> InstallmentList = null;	
      	
		try{
			InstallmentList = new ArrayList<Installment>();
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
    					
			if(totalInstallmentList>0){            	            
	            for (int i=0; i<totalInstallmentList;i++) {
	            	Installment installment= new Installment();
	            	installment.setOrderPaymentId(rsInstallmentList.getLong("order_payment_id"));
	            	installment.setWcsOrderId(rsInstallmentList.getString("wcs_order_id"));
	            	installment.setWcsPaymentId(rsInstallmentList.getString("wcs_payment_id"));
	            	installment.setOrderDate(rsInstallmentList.getDate("order_date"));
	            	installment.setReferenceId(rsInstallmentList.getString("reference_id"));
	            	installment.setAmount(rsInstallmentList.getBigDecimal("amount"));
	            	installment.setTenor(rsInstallmentList.getInt("tenor"));
	            	installment.setInstallment(rsInstallmentList.getBigDecimal("installment"));
	            	installment.setInterest(rsInstallmentList.getBigDecimal("interest"));
	            	installment.setInteresInstallment(rsInstallmentList.getBigDecimal("interest_installment"));
	            	installment.setInstallmentSentFlag(rsInstallmentList.getBoolean("installment_sent_flag"));
	            	installment.setCustomerUserName(rsInstallmentList.getString("customer_user_name"));
	            	installment.setCustomerName(rsInstallmentList.getString("full_or_legal_name"));
	            	installment.setWcsPaymentTypeId(rsInstallmentList.getLong("wcs_payment_type_id"));
	            	InstallmentList.add(installment);
	            	rsInstallmentList.next();
	            }
	            
				for(int i=0; i<InstallmentList.size();i++){
					HashMap<String, String> map = new HashMap<String, String>();
					Installment list = InstallmentList.get(i);
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_ORDERPAYMENTID, Util.isNull(list.getOrderPaymentId(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_WCSPAYMENTID, Util.isNull(list.getWcsPaymentId(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID, Util.isNull(list.getWcsOrderId(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_REFERENCEID, Util.isNull(list.getReferenceId(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID, Util.isNull(list.getWcsPaymentTypeId(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT, Util.isNull(list.getAmount(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR, Util.isNull(list.getTenor(), "").toString());
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_CUSTOMERUSERNAME, Util.isNull(list.getCustomerUserName(), "").toString());
					map.put(DataNameTokens.VENCUSTOMER_VENPARTY_FULLORLEGALNAME, Util.isNull(list.getCustomerName(), "").toString());		
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG, Util.isNull(list.getInstallmentSentFlag()==true?"Yes":"No", "").toString());
					
					dataList.add(map);				
				}
				rafDsResponse.setStatus(0);
				rafDsResponse.setStartRow(request.getStartRow());
				rafDsResponse.setTotalRows(dataList.size());
				rafDsResponse.setEndRow(request.getStartRow()+dataList.size());
			}	
		}catch(Exception e){
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}finally{
			try {
				if(psInstallmentList!=null) psInstallmentList.close();
				if(rsInstallmentList!=null) rsInstallmentList.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}        	
		}
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
