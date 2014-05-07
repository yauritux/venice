package com.gdn.venice.seattle.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBException;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.app.jiraclient.GdnJiraClient;
import com.gdn.app.jiraclient.GdnJiraPluginConstants;
import com.gdn.app.jiraclient.exceptions.GdnJiraAlreadyExist;
import com.gdn.app.jiraclient.exceptions.GdnJiraInvalidTransitionException;
import com.gdn.app.jiraclient.request.OrderCompleteProcessIssue;
import com.gdn.app.jiraclient.request.PaymentApprovalIssue;
import com.gdn.app.jiraclient.util.GdnJiraCustomClientImpl;
import com.gdn.venice.persistence.SeatOrderStatus;
import com.gdn.venice.persistence.SeatSlaStatus;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.seattle.bean.SeattleOrder;

public class SeattleStatusOrderPaymentApprovalBatchJob {
	protected static Logger _log = null;
	private static String CONFIG_FILE = System.getenv("VENICE_HOME") + "/admin/config.properties";
	private String dbHost = "";
	private String dbPort = "";
	private String dbUsername = "";
	private String dbPassword = "";
	private String environment = "";
	private String dbName = "";
	private String jiraHost = "";
	private String jiraUsername = "";
	private String jiraPassword = "";
	private static Connection conn;
	private static ArrayList<Long> statusOrder;
	private static String jobStatus;
	private static Timestamp currentTimestamp=null;
	private static GdnJiraClient jira =null;
	
	private static Map<String,SeatStatusUom> SeatStatusUomMap=null;
	
	private static final String STATUS_ORDER_BY_STATUS_ID = "select sosh.seat_order_status_history_id , vo.wcs_order_id ,voi.wcs_order_item_id," +
			" vos.order_status_code, sosh.update_status_date, soe.etd_max,soe.start_date,soe.end_date,soe.logisticsetd,  vo.order_timestamp,vos.order_status_id,sof.order_fulfillment_id,sof.issue_id," +
			"soe.diff_etd,sost.seat_order_status_tracking_id,sost.issue_id as issue_id2,sof.etd_order_complete,sof.new_etd_max_order," +
			"sof.order_process_late_time,vop.payment_status_id,sost.status_issue,srst.result_status_tracking_desc,srst2.result_status_tracking_desc as result_status_tracking_desc2 ,soe.other,sof.order_status_dec,sof.created_date" +
			" from seat_order_status_history sosh" +
			" left join ven_order vo on vo.order_id=sosh.order_id" +
			" left join ven_order_payment_allocation vopa on vopa.order_id=vo.order_id" +
			" left join ven_order_payment vop on vop.order_payment_id=vopa.order_payment_id" +
			" left join ven_order_item voi on voi.order_item_id=sosh.order_item_id" +
			" left join ven_order_status vos on vos.order_status_id=sosh.order_status_id" +
			" left join seat_order_etd soe on soe.order_etd_id=sosh.order_etd_id" +
			" left join seat_order_fulfillment sof on sof.seat_order_status_history_id=sosh.seat_order_status_history_id" +
			" left join seat_fulfillment_in_percentage sfip on sfip.fulfillment_in_percentage_id=sof.fulfillment_in_percentage_id" +
			" left join seat_result_status_tracking srst2 on srst2.result_status_tracking_id=sfip.result_status_tracking_id" +	
			" left join seat_order_status_tracking sost on sost.seat_order_status_history_id=sosh.seat_order_status_history_id and vos.order_status_code=sost.status_desc" +
			" left join seat_sla_status_percentage sssp on sssp.seat_sla_status_percentage_id=sost.seat_sla_status_percentage_id" +
			" left join seat_result_status_tracking srst on srst.result_status_tracking_id=sssp.result_status_tracking_id" +			
			" where sosh.order_status_id = ? " +
			" and (vop.payment_status_id=0 or (vop.payment_status_id=1 and sost.issue_id is not null and sost.status_issue=true))" +
			" order by sosh.order_status_id";
	
	private static final String SLA_BY_STATUS_ID = "select sos2.order_status_decs,sss.sla, sss.sla_second," +
			" ssu.status_uom_id, ssu.status_uom_desc, ssu.status_uom_type, ssu.status_uom_from, ssu.status_uom_end"+
			" from seat_sla_status sss"+
			" left join seat_fulfillment_consist_of_sla_status sfcoss on sfcoss.sla_status_id=sss.sla_status_id"+
			" left join seat_order_status sos on sos.seat_order_status_id=sfcoss.seat_order_status_id"+
			" left join seat_order_status sos2 on sos2.seat_order_status_id=sss.seat_order_status_id"+
			" left join seat_status_uom ssu on ssu.status_uom_id=sss.status_uom_id"+
			" where sos.order_status_decs=? order by sfcoss.sla_status_id asc";
	
	private static final String RESULT_FULFILLMENT_TARCKING = "select sfip.fulfillment_in_percentage_id ,srst.result_status_tracking_desc" +
			" from seat_fulfillment_in_percentage sfip" +
			" left join seat_order_status sos on sos.seat_order_status_id=sfip.seat_order_status_id" +
			" left join seat_result_status_tracking srst on srst.result_status_tracking_id=sfip.result_status_tracking_id" +
			" where ? between sfip.min and sfip.max  and sos.order_status_decs=?";
	
	private static final String RESULT_STATUS_TARCKING = "select sssp.seat_sla_status_percentage_id ,srst.result_status_tracking_desc" +
			" from seat_sla_status_percentage sssp" +
			" left join seat_sla_status sss on sss.sla_status_id=sssp.sla_status_id" +
			" left join seat_order_status sos on sos.seat_order_status_id=sss.seat_order_status_id" +
			" left join seat_result_status_tracking srst on srst.result_status_tracking_id=sssp.result_status_tracking_id" +		
			" where ? between sssp.min and sssp.max  and sos.order_status_decs=?";
	
	private static final String UOM = "select * From seat_status_uom";
	
	private static final String HOLIDAY_TIMENTAMP = "select * from ven_holiday where holiday_date between ? and ? order by holiday_date";
	
	private static final String INSERT_RESULT_ORDER_FULFILLMENT = "insert into seat_order_fulfillment  (seat_order_status_history_id,fulfillment_in_percentage_id,order_status_dec," +
	"etd_order_complete,new_etd_max_order,order_process_late_time,order_process_late_time_second,issue_id,created_date,status_issue) values (?, ?, ?,?, ?, ?,?, ?, ?,?)";
	
	private static final String UPDATE_RESULT_ORDER_FULFILLMENT = "update  seat_order_fulfillment  set fulfillment_in_percentage_id=? ,order_status_dec=? ," +
			"etd_order_complete=?,new_etd_max_order=?,order_process_late_time=?,order_process_late_time_second=?,issue_id=?,created_date=?" +
			" where order_fulfillment_id=?";
	
	private static final String INSERT_RESULT_ORDER_STATUS = "insert into seat_order_status_tracking  (seat_order_status_history_id,status_timestamp,status_due_date," +
			"status_late_time,status_late_time_second,seat_sla_status_percentage_id,issue_id,created_date,status_issue,status_desc) values (?, ?, ?,?, ?, ?,?, ?,?,?)";

	private static final String UPDATE_RESULT_ORDER_STATUS = "update  seat_order_status_tracking  set status_due_date=?," +
			" status_late_time=?,status_late_time_second=?,seat_sla_status_percentage_id=?,issue_id=?,created_date=?,status_issue=?" +
			" where seat_order_status_tracking_id=?" ;
	
	private static final String CLOSE_ISSUE_STATUS = "select sost.issue_id from seat_order_status_history sosh" +
	" inner join ven_order_status vos on vos.order_status_id=sosh.order_status_id" +
	" inner join seat_order_status_tracking sost on sosh.seat_order_status_history_id=sost.seat_order_status_history_id" +
	" left join ven_order vo on vo.order_id=sosh.order_id" +
	" left join ven_order_payment_allocation vopa on vopa.order_id=vo.order_id" +
	" left join ven_order_payment vop on vop.order_payment_id=vopa.order_payment_id" +
	" where sost.issue_id is not null and sost.status_issue = true and sost.status_desc in ('VA')" +
	" and vop.payment_status_id=1";
	
	private static final String CLOSE_ISSUE_STATUS_COMPLETE = "select sof.issue_id from seat_order_status_history sosh"+
	 " inner join ven_order_status vos on vos.order_status_id=sosh.order_status_id"+
	 " inner join seat_order_fulfillment sof on sosh.seat_order_status_history_id=sof.seat_order_status_history_id"+
	 " left join ven_order vo on vo.order_id=sosh.order_id"+
	 " left join ven_order_payment_allocation vopa on vopa.order_id=vo.order_id"+
	 " left join ven_order_payment vop on vop.order_payment_id=vopa.order_payment_id"+
	 " where sof.issue_id is not null and sof.status_issue = true and sof.order_status_dec in ('VA')"+
	 " and vop.payment_status_id=1";
	
	private static final String UPDATE_STATUS_ISSUE = "update  seat_order_status_tracking set status_issue=?" +
	" where issue_id=?" ;
	
	private static final String UPDATE_STATUS_COMPLETE_ISSUE = "update  seat_order_fulfillment set status_issue=?" +
	" where issue_id=?" ;
	

	
    private SeattleStatusOrderPaymentApprovalBatchJob() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.seattle.batch.SeattleStatusOrderPaymentApprovalBatchJob");
        
    	Properties prop = new Properties();
		prop.load(new FileInputStream(CONFIG_FILE));
		environment = prop.getProperty("environment");
		dbHost = prop.getProperty(environment + ".dbHost");
		dbPort = prop.getProperty(environment + ".dbPort");
		dbUsername = prop.getProperty(environment + ".dbUsername");
		dbPassword = prop.getProperty(environment + ".dbPassword");
		dbName = prop.getProperty(environment + ".dbName");
		jiraHost = prop.getProperty(environment + ".jiraHost");
		jiraUsername = prop.getProperty(environment + ".jiraUsername");
		jiraPassword = prop.getProperty(environment + ".jiraPassword");
				
		GdnJiraPluginConstants.actualPickupTimeCustomFieldId=prop.getProperty(environment + ".actualPickupTimeCustomFieldId");
		 GdnJiraPluginConstants.etdOrderCompleteCustomFieldId=prop.getProperty(environment + ".etdOrderCompleteCustomFieldId");
		 GdnJiraPluginConstants.fraudBinListedCustomFieldId=prop.getProperty(environment + ".fraudBinListedCustomFieldId");
		 GdnJiraPluginConstants.fraudBlacklistInfoCustomFieldId=prop.getProperty(environment + ".fraudBlacklistInfoCustomFieldId");
		 GdnJiraPluginConstants.fraudECICustomFieldId=prop.getProperty(environment + ".fraudECICustomFieldId");
		 GdnJiraPluginConstants.fraudOrderQuantityByEmailCustomFieldId=prop.getProperty(environment + ".fraudOrderQuantityByEmailCustomFieldId");
		 GdnJiraPluginConstants.fraudPaymentTypeCustomFieldId=prop.getProperty(environment + ".fraudPaymentTypeCustomFieldId");
		 GdnJiraPluginConstants.fraudTotalPaymentCustomFieldId=prop.getProperty(environment + ".fraudTotalPaymentCustomFieldId");
		 GdnJiraPluginConstants.issueDueDateCustomFieldId=prop.getProperty(environment + ".issueDueDateCustomFieldId");
		 GdnJiraPluginConstants.issueLateTimeCustomFieldId=prop.getProperty(environment + ".issueLateTimeCustomFieldId");
		 GdnJiraPluginConstants.issueTimestampCustomFieldId=prop.getProperty(environment + ".issueTimestampCustomFieldId");
		 GdnJiraPluginConstants.newEtdMaxOrderCustomFieldId=prop.getProperty(environment + ".newEtdMaxOrderCustomFieldId");
		 GdnJiraPluginConstants.orderIdCustomFieldId=prop.getProperty(environment + ".orderIdCustomFieldId");
		 GdnJiraPluginConstants.orderItemIdCustomFieldId=prop.getProperty(environment + ".orderItemIdCustomFieldId");
		 GdnJiraPluginConstants.orderProcessLateTimeCustomFieldId=prop.getProperty(environment + ".orderProcessLateTimeCustomFieldId");
		 GdnJiraPluginConstants.orderTrackingProjectKey=prop.getProperty(environment + ".orderTrackingProjectKey");
		 GdnJiraPluginConstants.selectedPickupTimeCustomFieldId=prop.getProperty(environment + ".selectedPickupTimeCustomFieldId");
		 GdnJiraPluginConstants.paymentApprovalIssueTypeId=new Long(prop.getProperty(environment + ".paymentApprovalIssueTypeId"));
		 GdnJiraPluginConstants.fraudCheckingIssueTypeId=new Long(prop.getProperty(environment + ".fraudCheckingIssueTypeId"));
		 GdnJiraPluginConstants.orderFulfillIssueTypeId=new Long(prop.getProperty(environment + ".orderFulfillIssueTypeId"));
		 GdnJiraPluginConstants.logisticSettleIssueTypeId=new Long(prop.getProperty(environment + ".logisticSettleIssueTypeId"));
		 GdnJiraPluginConstants.regularShipmentOrderDeliveryIssueTypeId=new Long(prop.getProperty(environment + ".regularShipmentOrderDeliveryIssueTypeId"));
		 GdnJiraPluginConstants.fulfillmentOrderItemIssueTypeId=new Long(prop.getProperty(environment + ".fulfillmentOrderItemIssueTypeId"));
		 GdnJiraPluginConstants.merchantPartnerShipmentOrderDeliveryIssueTypeId=new Long(prop.getProperty(environment + ".merchantPartnerShipmentOrderDeliveryIssueTypeId"));        
		 GdnJiraPluginConstants.issueApi = prop.getProperty(environment + ".issueApi");
		
		System.out.println("environment: "+environment);
		System.out.println("dbHost: "+dbHost);
		System.out.println("dbPort: "+dbPort);
		
		statusOrder = new ArrayList<Long>();
		statusOrder.add(new Long(0));//status VA
		statusOrder.add(new Long(18));//status CS
		
		jobStatus = new String("Payment Approval");
		
		setupDBConnection();
		setupJiraConnection();
    }
    
	private void setupDBConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");		
		conn = DriverManager.getConnection("jdbc:postgresql://" + dbHost +":" + dbPort + "/" + dbName, dbUsername, dbPassword);
	}
	
	private void setupJiraConnection() throws ClassNotFoundException, SQLException{
		 jira = new GdnJiraClient();
		 jira.setGdnJiraCustomClient(new GdnJiraCustomClientImpl());
		 jira.setJiraHost(jiraHost);
		 jira.setJiraUser(jiraUsername);
		 jira.setJiraPass(jiraPassword);
	}
	
	
	private  ArrayList<SeattleOrder> fetchOrderByStatus(long orderStatusId){
		PreparedStatement psSeattleList = null;      
      	ResultSet rsSeattletList = null;
      	ArrayList<SeattleOrder> seattleOrderList = null;	
	    
	    try{	            
	    	_log.debug("----------Query Seattle Order fot fulfillment data");
	    	
	    	seattleOrderList = new ArrayList<SeattleOrder>();
	    	psSeattleList = conn.prepareStatement(STATUS_ORDER_BY_STATUS_ID, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            
            
	    	psSeattleList.setLong(1, orderStatusId);
	    	rsSeattletList = psSeattleList.executeQuery();
    		
	    	rsSeattletList.last();
			int totalOrderList = rsSeattletList.getRow();
			rsSeattletList.beforeFirst();
			rsSeattletList.next();
    		
			_log.info("Query returns: " + totalOrderList + " row(s)");
			
			if(totalOrderList==0){
				return null;
			}else{         				
	            for (int i=0; i<totalOrderList;i++) {
	            	SeattleOrder item= new SeattleOrder();
	            	item.setSeatOrderStatusHistory(rsSeattletList.getLong("seat_order_status_history_id"));
	            	item.setWcsOrderId(rsSeattletList.getString("wcs_order_id"));
	            	item.setWcsOrderItemId(rsSeattletList.getString("wcs_order_item_id"));
	            	item.setUpdateStatusDate(rsSeattletList.getTimestamp("update_status_date"));	            	
	            	item.setStatusOrder(rsSeattletList.getString("order_status_code"));
	            	item.setEtdMax(rsSeattletList.getDate("etd_max"));	            	
	            	item.setOrderTimestamp(rsSeattletList.getTimestamp("order_timestamp"));
	            	item.setOrderStatusId(rsSeattletList.getLong("order_status_id"));
	            	item.setOrderfulfillmentId(rsSeattletList.getLong("order_fulfillment_id"));	    
	            	item.setIssueId(rsSeattletList.getString("issue_id"));       
	            	item.setDiffEtd(rsSeattletList.getBigDecimal("diff_etd"));            	
	            	item.setOrderStatusTrackingtId(rsSeattletList.getLong("seat_order_status_tracking_id"));	 
	            	item.setIssueStatusId(rsSeattletList.getString("issue_id2"));
	            	item.setResultStatus(rsSeattletList.getString("result_status_tracking_desc2"));
	            	item.setEtdOrderComplate(rsSeattletList.getTimestamp("etd_order_complete"));
	            	item.setNewEtdMax(rsSeattletList.getTimestamp("new_etd_max_order"));
	            	item.setLate(rsSeattletList.getString("order_process_late_time"));
	            	item.setStatusPayment(rsSeattletList.getLong("payment_status_id"));            	
	            	item.setCloseIssueStatus(rsSeattletList.getBoolean("status_issue"));
	            	item.setResultStatusTracking(rsSeattletList.getString("result_status_tracking_desc"));	           	
	             	item.setStartNewEtd(rsSeattletList.getDate("start_date"));
	            	item.setEndNewEtd(rsSeattletList.getDate("end_date"));
	            	item.setLogisticsEtd(rsSeattletList.getBigDecimal("logisticsetd"));
	            	item.setOrderStatusDescComplete(rsSeattletList.getString("order_status_dec"));
	            	item.setTimeComplete(rsSeattletList.getTimestamp("created_date"));
	            	item.setMoreInfo(rsSeattletList.getString("other"));
	            	
	            	seattleOrderList.add(item);
	            	rsSeattletList.next();	            	
	            }
			}
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
            	if(psSeattleList!=null) psSeattleList.close();
            	if(rsSeattletList!=null) rsSeattletList.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
		return seattleOrderList;
	}	
	
	private void  setUoM(){
		PreparedStatement psUoMList = null;      
      	ResultSet rsUoMList = null;      
		 try{	            
		    	_log.debug("Query UoM");
		    	
		    	SeatStatusUomMap = new HashMap<String,SeatStatusUom>();
		    	psUoMList = conn.prepareStatement(UOM, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);                     
		    	rsUoMList = psUoMList.executeQuery();
	    		
		    	rsUoMList.last();
				int totalUoMList = rsUoMList.getRow();
				rsUoMList.beforeFirst();
				rsUoMList.next();	    		
				_log.info("Query returns: " + totalUoMList + " row(s)");				
		            for (int i=0; i<totalUoMList;i++) {
		            	SeatStatusUom item= new SeatStatusUom();
		            	item.setStatusUomId(rsUoMList.getLong("status_uom_id"));
		            	item.setStatusUomDesc(rsUoMList.getString("status_uom_desc"));
		            	item.setStatusUomType(rsUoMList.getString("status_uom_type"));
		            	item.setStatusUomFrom(rsUoMList.getBigDecimal("status_uom_from"));
		            	item.setStatusUomEnd(rsUoMList.getBigDecimal("status_uom_end"));
		            	SeatStatusUomMap.put(item.getStatusUomDesc()+"", item);		
		            	rsUoMList.next();
		            }
				
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	            	if(psUoMList!=null) psUoMList.close();
	            	if(rsUoMList!=null) rsUoMList.close();
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }        
	}
	
	private ArrayList<SeatSlaStatus>  getSeatSlaStatusByStatus(){
		PreparedStatement psSLAStatusList = null;      
      	ResultSet rsSLAStatusList = null;
      	ArrayList<SeatSlaStatus> slaStatusList = null;	
		 try{	            
		    	_log.debug("Query Seattle Order fot fulfillment data -> get SLA for statusId "+jobStatus);
		    	
		    	slaStatusList = new ArrayList<SeatSlaStatus>();
		    	psSLAStatusList = conn.prepareStatement(SLA_BY_STATUS_ID, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            
	            
		    	psSLAStatusList.setString(1, jobStatus);
		    	rsSLAStatusList = psSLAStatusList.executeQuery();
	    		
		    	rsSLAStatusList.last();
				int totalSLAList = rsSLAStatusList.getRow();
				rsSLAStatusList.beforeFirst();
				rsSLAStatusList.next();
	    		
				_log.info("Query returns: " + totalSLAList + " row(s)");
				
				if(totalSLAList==0){
					return null;
				}else{         					
		            for (int i=0; i<totalSLAList;i++) {
		            	SeatSlaStatus item= new SeatSlaStatus();
		            	
		            	SeatOrderStatus seatOrderStatus = new SeatOrderStatus();
		            	seatOrderStatus.setOrderStatusDecs(rsSLAStatusList.getString("order_status_decs"));
		            	item.setSeatOrderStatus(seatOrderStatus);
		          	            	
		            	SeatStatusUom itemSeatStatusUom = new SeatStatusUom();		            	
		            	itemSeatStatusUom.setStatusUomDesc(rsSLAStatusList.getString("status_uom_desc"));	
		            	itemSeatStatusUom.setStatusUomType(rsSLAStatusList.getString("status_uom_type"));		
		            	item.setSeatStatusUom(itemSeatStatusUom);	
		            	
		              	item.setSla(rsSLAStatusList.getBigDecimal("sla"));		                  	
		            	item.setSlaSecond(rsSLAStatusList.getBigDecimal("sla_second"));	       	
		            	slaStatusList.add(item);
		            	rsSLAStatusList.next();
		            }
				}
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	            	if(psSLAStatusList!=null) psSLAStatusList.close();
	            	if(rsSLAStatusList!=null) rsSLAStatusList.close();
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	        
			return slaStatusList;
	}
	
	
	private Timestamp getSLADueDate(Timestamp orderStatusUpdateDate,SeatSlaStatus seatSlaStatus){
		Timestamp later =null;		
		_log.info("##---SLADueDate :"+orderStatusUpdateDate+" type SLA : " + seatSlaStatus.getSeatStatusUom().getStatusUomDesc());			
		if(seatSlaStatus.getSeatStatusUom().getStatusUomDesc().equals("WorkDay")){
			later= calTimeWithWorkDay(orderStatusUpdateDate,seatSlaStatus);			
		}else if(seatSlaStatus.getSeatStatusUom().getStatusUomDesc().equals("WorkHour")){
			later= calTimeWithWorkHour(orderStatusUpdateDate,seatSlaStatus);			
		}else if(seatSlaStatus.getSeatStatusUom().getStatusUomDesc().equals("Day")){
			later= getTimeafterAddDay(orderStatusUpdateDate,new Integer(seatSlaStatus.getSla()+""));
		}else if(seatSlaStatus.getSeatStatusUom().getStatusUomDesc().equals("Hour")){
			later= getTimeafterAddHour(orderStatusUpdateDate,new Integer(seatSlaStatus.getSla()+""));
		}		
		
		_log.info("result SLADueDate " + later);				
		return later;
	}
	
	@SuppressWarnings("deprecation")
	private Timestamp getStartTimeWithUoMWorkHour(Timestamp orderStatusUpdateDate,SeatStatusUom uomItem){
			int rangeUoM = new Integer(uomItem.getStatusUomEnd()+"") - new Integer(uomItem.getStatusUomFrom()+"");
			
			if(orderStatusUpdateDate.getHours() < new Integer(uomItem.getStatusUomFrom()+"") ){
				   orderStatusUpdateDate.setMinutes(0);
				   orderStatusUpdateDate.setSeconds(0);
				   orderStatusUpdateDate = getTimeafterAddHour(orderStatusUpdateDate,new Integer(uomItem.getStatusUomFrom()+"")-orderStatusUpdateDate.getHours());	 
			 }else if(orderStatusUpdateDate.getHours() > new Integer(uomItem.getStatusUomEnd()+"")){
				 	orderStatusUpdateDate.setMinutes(0);
				 	orderStatusUpdateDate.setSeconds(0);
				 	orderStatusUpdateDate = getTimeafterAddHour(orderStatusUpdateDate,rangeUoM);			  
			 }	else{
				 return orderStatusUpdateDate; 
			 }
		
		return getStartTimeWithUoMWorkHour(orderStatusUpdateDate,uomItem);
	}
	
	
		 
	 private Timestamp cekHoliday(Timestamp orderStatusUpdateDate,Timestamp later){
		    PreparedStatement psHolidayList = null;      
	      	ResultSet rsHolidayList = null;	  
	      	Timestamp laters = null;	  
			 try{	            
			    	_log.debug("Query Cek Holiday from : "+orderStatusUpdateDate+" until :"+later);			    	
			    	psHolidayList = conn.prepareStatement(HOLIDAY_TIMENTAMP, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            
		            
			    	psHolidayList.setDate(1, new Date(orderStatusUpdateDate.getTime()));
			    	psHolidayList.setDate(2, new Date(later.getTime()));
			    	rsHolidayList = psHolidayList.executeQuery();		    		
			    	rsHolidayList.last();
					int totalHolidayList = rsHolidayList.getRow();
					rsHolidayList.beforeFirst();
					rsHolidayList.next();		    							
					
					_log.info("Query returns: " + totalHolidayList + " row(s)");
					if(totalHolidayList>0){						
					    laters = getTimeafterAddDay(later,totalHolidayList);
						later =getTimeafterAddDay(later,1);
					}			
			
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        } finally {
		            try {
		            	if(psHolidayList!=null) psHolidayList.close();
		            	if(rsHolidayList!=null) rsHolidayList.close();
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }
		        }
		        
		       if( laters==null){
		    	   return later;
		       }else{
		    	   /**
		    	    * recursive until no holiday
		    	    */
		  		  return cekHoliday(later,laters);
		       }
		 
	 }
	 private Timestamp getTimeafterAddDay(Timestamp dateTime, int addDay){
		 Calendar cal = Calendar.getInstance();
		    cal.setTime(dateTime);
		    cal.add(Calendar.DATE, addDay);		 
		    return new Timestamp(cal.getTime().getTime());
	 }
	 private Timestamp getTimeafterAddHour(Timestamp dateTime, int AddHour){
		 Calendar cal = Calendar.getInstance();
		    cal.setTime(dateTime);
		    cal.add(Calendar.HOUR, AddHour);		 
		    return new Timestamp(cal.getTime().getTime());
	 }
	 @SuppressWarnings("unused")
	private Timestamp getTimeafterAddSecond(Timestamp dateTime, int AddSecond){
		 Calendar cal = Calendar.getInstance();
		    cal.setTime(dateTime);
		    cal.add(Calendar.SECOND, AddSecond);		 
		    return new Timestamp(cal.getTime().getTime());
	 }
	 
	 @SuppressWarnings("deprecation")
		private Timestamp calTimeWithWorkHour(Timestamp orderStatusUpdateDate, SeatSlaStatus seatSlaStatus){	
			SeatStatusUom uomItem =  SeatStatusUomMap.get("WorkHour");	 
			
			Timestamp later= new Timestamp(orderStatusUpdateDate.getTime());		
			later=getStartTimeWithUoMWorkHour(later,uomItem);	
			int rangeUoM = new Integer(uomItem.getStatusUomEnd()+"") - new Integer(uomItem.getStatusUomFrom()+"");
			int slaValue = new Integer(seatSlaStatus.getSla()+"");
			_log.info("get Hour Timebefore "+orderStatusUpdateDate+" sla : "+slaValue);
			
				
			while(slaValue!=0 ){
				if(later.getHours()==new Integer(uomItem.getStatusUomEnd()+"")){				
					later = getTimeafterAddHour(later,24-rangeUoM);
				}				
				_log.info("loop date = "+later+" sla : "+slaValue);
				int interval = new Integer(uomItem.getStatusUomEnd()+"") - later.getHours();
				if(slaValue> interval){
					slaValue = slaValue-interval;				
				}	else{
					interval=slaValue;
					slaValue=0;
				}					
				later = getTimeafterAddHour(later,interval );				
				_log.info("loop after date = "+later+" sla : "+slaValue);
				
			}
			_log.info("loop after hour = "+later.getHours()+" minut : "+later.getMinutes()+" secon :"+later.getSeconds());
			if(later.getHours()==new Integer(uomItem.getStatusUomEnd()+"") && (later.getMinutes() > 0 || later.getSeconds() > 0)){
				later.setHours(new Integer(uomItem.getStatusUomFrom()+""));
				later=getTimeafterAddDay(later,1);
			}
			
			_log.info("get calTimeWithWorkHour before " + orderStatusUpdateDate +" after :"+later);				 
			return later;
		 }	
	 
	 @SuppressWarnings("deprecation")
	private Timestamp calTimeWithWorkDay(Timestamp orderStatusUpdateDate,SeatSlaStatus seatSlaStatus){			
		 	SeatStatusUom uomItem =  SeatStatusUomMap.get("WorkHour");	
		 	
		 	int addday = new Integer(seatSlaStatus.getSla()+"");
		 	Timestamp later= new Timestamp(orderStatusUpdateDate.getTime());
		 	if(addday>0){		
		 			int rangeUoM = new Integer(uomItem.getStatusUomEnd()+"") - new Integer(uomItem.getStatusUomFrom()+"");
		 			_log.info("get Day Timebefore "+later+" rangeUoM : "+rangeUoM+" day : "+addday);
				 	SeatSlaStatus newSlaStatus = new SeatSlaStatus();
				 	newSlaStatus.setSla(new BigDecimal(rangeUoM*addday));
				 	
				 	 later  = calTimeWithWorkHour(later,newSlaStatus);	
		 	}else{		 		
		 		later=getStartTimeWithUoMWorkHour(later,uomItem);	
		 		later.setHours(uomItem.getStatusUomEnd().intValue());
		 		later.setSeconds(0);
		 		later.setMinutes(0);
		 	}	 		 			
			_log.info("get TimeWithWorkDay before " + orderStatusUpdateDate +" after :"+later);				 
		 return later;
	 }
	 protected static String selisihDateTime(Timestamp  waktuSatu, Timestamp waktuDua) {
	        long selisihMS = Math.abs(waktuSatu.getTime()-waktuDua.getTime());
	        long selisihDetik = selisihMS / 1000 % 60;
	        long selisihMenit = selisihMS / (60 * 1000) % 60;
	        long selisihJam = selisihMS / (60 * 60 * 1000) % 24;
	        long selisihHari = selisihMS / (24 * 60 * 60 * 1000);
	        String selisih = selisihHari + " hari " + selisihJam + " Jam "
	                + selisihMenit + " Menit " + selisihDetik + " Detik";
	        return selisih;
	    }
 	 
	 private OrderCompleteProcessIssue getInfoOfIssueOrderComplete(SeattleOrder item){		 
		 OrderCompleteProcessIssue itemIssue = new OrderCompleteProcessIssue();	 
			itemIssue.setEtdOrderComplete(item.getEtdOrderComplate());
			itemIssue.setNewEtdMaxOrder(item.getNewEtdMax());
			itemIssue.setOrderProcessLateTime(item.getLate());
			itemIssue.setOrderId(item.getWcsOrderId());
			itemIssue.setOrderItemId(item.getWcsOrderItemId());
			
		 return itemIssue;
	 }	 
	 	 
	 private SeattleOrder getResultIssueOfFulfillment(SeattleOrder item){ 
		  /**
		  * cek sudah ada issue atau belum
		  */
		 try {
				 if(item.getIssueId()!=null){
					 OrderCompleteProcessIssue itemIssue = getInfoOfIssueOrderComplete(item);					   
					  itemIssue.setIssueKey(item.getIssueId());
					  _log.info("getIssueId "+item.getIssueId());	
					  jira.updateIssueCustomField(itemIssue);
					 if(item.getResultStatus().equals("Late")){
						 _log.info("Update Issue Late  ");		
					      jira.updateOrderTrackingLateIssue(item.getIssueId());  
					 }	else  if(item.getResultStatus().equals("Attention")){
						 _log.info("Update Issue Attention ");			
					    jira.updateOrderCompleteIssueToAttention(item.getIssueId());  
					 }
					 
				 }else{
					 /**
					  * jika belum ada create issue
					  */
					 String issue=null;
					 			 if(item.getResultStatus().equals("Attention") || item.getResultStatus().equals("Late")){		
					 				 _log.info("Create Issue Attention  ");
					 				 issue = jira.createAttention(getInfoOfIssueOrderComplete(item));					 				
					 				 if(item.getResultStatus().equals("Late")){
					 					 _log.info("Create Issue Late  ");
									     jira.updateOrderTrackingLateIssue(issue);  
					 				 }						 				
					 		     }		
			     _log.info("Issue Id "+issue);
				 item.setIssueId(issue);
				 }	
		 } catch (URISyntaxException e) {		
				e.printStackTrace();
			} catch (GdnJiraAlreadyExist e) {			
				e.printStackTrace();
			} catch (GdnJiraInvalidTransitionException e) {			
			e.printStackTrace();
		}
		        
		  return item;
		 
	 }
	 private void closeIssue(String issueID){ 		
		 
		 PreparedStatement psCloseIssue = null;						
		 try {
			 jira.closeOrderTrackingIssue(issueID);			
			_log.info("Close Issue Id  : "+issueID);
			psCloseIssue = conn.prepareStatement(UPDATE_STATUS_ISSUE);				

			psCloseIssue.setBoolean(1, false);   
			psCloseIssue.setString(2, issueID);		    	
			psCloseIssue.executeUpdate();	
	    	
		} catch (URISyntaxException e) {	
			e.printStackTrace();
		} catch (SQLException e) {		
			e.printStackTrace();
		}finally {
            try {
            	if(psCloseIssue!=null) psCloseIssue.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
	 }
 private void closeCompleteIssue(String issueID){ 		
		 
		 PreparedStatement psCloseIssue = null;						
		 try {
			 jira.closeOrderTrackingIssue(issueID);			
			_log.info("Close Issue Id  : "+issueID);
			psCloseIssue = conn.prepareStatement(UPDATE_STATUS_COMPLETE_ISSUE);				

			psCloseIssue.setBoolean(1, false);   
			psCloseIssue.setString(2, issueID);		    	
			psCloseIssue.executeUpdate();	
	    	
		} catch (URISyntaxException e) {	
			e.printStackTrace();
		} catch (SQLException e) {		
			e.printStackTrace();
		}finally {
            try {
            	if(psCloseIssue!=null) psCloseIssue.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
	 }
	 private SeattleOrder getResultIssue(SeattleOrder item){ 
		  /**
		  * cek sudah ada issue atau belum
		  */
		 try {
				 if(item.getIssueStatusId()!=null){
					 if(item.getResultStatusTracking().equals("Late")){
					      jira.updateOrderTrackingLateIssue(item.getIssueStatusId());  
					 }	
					 
				 }else{
					 /**
					  * jika belum ada create issue
					  */
					 String issue=null;
					 			 if(item.getResultStatusTracking().equals("Attention") || item.getResultStatusTracking().equals("Late")){
					 				 issue = jira.createAttention(getInfoForIssue(item));
					 				 if(item.getResultStatusTracking().equals("Late")){
									     jira.updateOrderTrackingLateIssue(issue);  
					 				 }	
					 				
					 		     }			
				 item.setIssueStatusId(issue);
					 
				 }	
		 } catch (URISyntaxException e) {		
				e.printStackTrace();
			} catch (GdnJiraAlreadyExist e) {			
				e.printStackTrace();
			} catch (GdnJiraInvalidTransitionException e) {			
			e.printStackTrace();
		}
		        
		  return item;
		 
	 }
	 
	 private PaymentApprovalIssue getInfoForIssue(SeattleOrder item){
		 
		 PaymentApprovalIssue  info = new PaymentApprovalIssue ();		 		 
		    info.setOrderId(item.getWcsOrderId());
			info.setOrderItemId(item.getWcsOrderItemId());		
			info.setIssueTimestamp( item.getUpdateStatusDate());
			info.setIssueDueDate(item.getStatusDueDate());	
			info.setIssueLateTime(item.getLateStatus());
			
			info.setEtdOrderComplete(item.getEtdOrderComplate());
			info.setNewEtdMaxOrder(item.getNewEtdMax());
			info.setOrderProcessLateTime(item.getLate());					  		  
				 
		 return info;
	 }
	 
	 private SeattleOrder getResultStatusTracking(SeattleOrder item){
		 PreparedStatement psResultStatusList = null;      
	      	ResultSet rsResultStatusList = null;	  
			 try{	            
					 long selisihMS = Math.round(new Double(Math.abs(currentTimestamp.getTime() - item.getUpdateStatusDate().getTime()))/new Double(Math.abs(item.getStatusDueDate().getTime()- item.getUpdateStatusDate().getTime()))*100);

					  _log.info("terbagi "+Math.abs(currentTimestamp.getTime() - item.getUpdateStatusDate().getTime())+" ==> bagi: "+Math.abs(item.getStatusDueDate().getTime()- item.getUpdateStatusDate().getTime()));
					  _log.info("selisih "+selisihMS+" ==>currentTimestamp: "+currentTimestamp+" status update date :"+item.getUpdateStatusDate()+"' DueDate : "+item.getStatusDueDate());    
					 	psResultStatusList = conn.prepareStatement(RESULT_STATUS_TARCKING, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            			            
				    	psResultStatusList.setInt(1, new Integer(selisihMS+""));
				    	psResultStatusList.setString(2, jobStatus);
				    	rsResultStatusList = psResultStatusList.executeQuery();		    		
				    	rsResultStatusList.last();
						int totalResultStatusList = rsResultStatusList.getRow();
						rsResultStatusList.beforeFirst();
						rsResultStatusList.next();		    							
						
						_log.info("Query returns: " + totalResultStatusList + " row(s)");
			            for (int i=0; i<totalResultStatusList;i++) {
			            	
			            	item.setResultStatusTrackingId(rsResultStatusList.getLong("seat_sla_status_percentage_id"));		
			            	if(item.getResultStatusTracking()!=null && item.getResultStatusTracking().equals(rsResultStatusList.getString("result_status_tracking_desc"))){
			            		item.setResultStatusTracking(rsResultStatusList.getString("result_status_tracking_desc")+"2");
			            	}else{
			            		item.setResultStatusTracking(rsResultStatusList.getString("result_status_tracking_desc"));	
			            	}
			            	item.setLateStatus("0");
			            	item.setLateSecondStatus(0);
							if(item.getResultStatusTracking().contains("Late")){
								item.setLateStatus(selisihDateTime(currentTimestamp,item.getStatusDueDate()));
			            		item.setLateSecondStatus((int) Math.abs(currentTimestamp.getTime()-item.getStatusDueDate().getTime()));
			            	}	
							
							rsResultStatusList.next();
			            }			            		
				
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        } finally {
		            try {
		            	if(psResultStatusList!=null) psResultStatusList.close();
		            	if(rsResultStatusList!=null) rsResultStatusList.close();
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }
		        }
		        
		  		  return item;
		 
	 }
	 
	 private Date getMaxEtd(SeattleOrder item){
			Timestamp maxEtd= new Timestamp(item.getEtdMax().getTime());
			_log.info("maxEtd " +maxEtd);
			_log.info("getDiffEtd " +item.getDiffEtd());
			if(item.getDiffEtd().compareTo(new BigDecimal(0))==1 
				&& (new Date(currentTimestamp.getTime())).compareTo(item.getStartNewEtd())>=new Long(1)
				&& (new Date(currentTimestamp.getTime())).compareTo(item.getEndNewEtd())<=new Long(1)){
				maxEtd = getTimeafterAddDay(new Timestamp(maxEtd.getTime()),new Integer(item.getDiffEtd()+""));
				_log.info("new maxEtd " +maxEtd);
			}		
			return new Date(maxEtd.getTime());
		}
	 
	 private SeattleOrder getResultFulfillmentTracking(SeattleOrder item){
		 PreparedStatement psResultStatusList = null;      
	      	ResultSet rsResultStatusList = null;	  
			 try{	            
				     Date maxEtd = getMaxEtd(item); 
					 long selisihMS = Math.round(new Double(Math.abs(item.getEtdOrderComplate().getTime() - item.getOrderTimestamp().getTime()))/new Double(Math.abs(maxEtd.getTime()- item.getOrderTimestamp().getTime()))*100);
					 item.setNewEtdMax(new Timestamp(maxEtd.getTime()));
					 
					  _log.info("min "+Math.abs(item.getEtdOrderComplate().getTime() - item.getOrderTimestamp().getTime())+" ==> bagi: "+Math.abs(maxEtd.getTime()- item.getOrderTimestamp().getTime()));
					  _log.info("selisih "+selisihMS+" ==>Orderdate: "+item.getOrderTimestamp()+" new ETD Max :"+maxEtd+"' DueDate : "+item.getEtdOrderComplate());    
					 	psResultStatusList = conn.prepareStatement(RESULT_FULFILLMENT_TARCKING, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            			            
				    	psResultStatusList.setInt(1, new Integer(selisihMS+""));
				    	psResultStatusList.setString(2, jobStatus);
				    	rsResultStatusList = psResultStatusList.executeQuery();		    		
				    	rsResultStatusList.last();
						int totalResultStatusList = rsResultStatusList.getRow();
						rsResultStatusList.beforeFirst();
						rsResultStatusList.next();		    							
						
						_log.info("Query returns: " + totalResultStatusList + " row(s)");
			            for (int i=0; i<totalResultStatusList;i++) {
			            	item.setResultStatusId(rsResultStatusList.getLong("fulfillment_in_percentage_id"));
			            	if(item.getResultStatus()!=null && item.getResultStatus().equals(rsResultStatusList.getString("result_status_tracking_desc"))){
			            		item.setResultStatus(rsResultStatusList.getString("result_status_tracking_desc")+"2");
			            	}else{
			            		item.setResultStatus(rsResultStatusList.getString("result_status_tracking_desc"));
			            	}			            	
			            	item.setLate("0");
			            	item.setLateSecond(0);
							if(item.getResultStatus().contains("Late")){
								item.setLate(selisihDateTime(item.getEtdOrderComplate(),new Timestamp(maxEtd.getTime())));
				            	item.setLateSecond((int) Math.abs(item.getEtdOrderComplate().getTime() - maxEtd.getTime()));								
							}	
							rsResultStatusList.next();
			            }			            		
				
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        } finally {
		            try {
		            	if(psResultStatusList!=null) psResultStatusList.close();
		            	if(rsResultStatusList!=null) rsResultStatusList.close();
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }
		        }
		        
		  		  return item;
		 
	 }
	 
	 private void saveResult(SeattleOrder item){
		 PreparedStatement psLogisticProvider = null;	
		 try{	

			 _log.info("Save Fulfillment For Order  : "+item.getWcsOrderItemId());
				psLogisticProvider = conn.prepareStatement(INSERT_RESULT_ORDER_FULFILLMENT,Statement.RETURN_GENERATED_KEYS);	
					
				psLogisticProvider.setLong(1, item.getSeatOrderStatusHistory());
		    	psLogisticProvider.setLong(2, item.getResultStatusId());
		    	psLogisticProvider.setString(3, item.getStatusOrder());
		    	psLogisticProvider.setTimestamp(4, item.getEtdOrderComplate());
		    	psLogisticProvider.setTimestamp(5, item.getNewEtdMax());
		    	psLogisticProvider.setString(6, item.getLate());
		    	psLogisticProvider.setInt(7, item.getLateSecond());		
		    	psLogisticProvider.setString(8, item.getIssueId());
		    	psLogisticProvider.setTimestamp(9, currentTimestamp);
		    	psLogisticProvider.setBoolean(10, item.getIssueId()!=null);
		    	
		    	psLogisticProvider.executeUpdate();		  
		    	ResultSet generatedKeys = psLogisticProvider.getGeneratedKeys();				    
		    	generatedKeys.close();
				
			}catch (Exception e) {
				_log.error("insert result of fulfillment " + e.getMessage(), e);
			}try {
            	if(psLogisticProvider!=null) psLogisticProvider.close();	            
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		 
	 }
	 
	 private void updateResult(SeattleOrder item){
			PreparedStatement psLogisticProvider = null;	
		 try{		
			 _log.info("Update Fulfillment For Order  : "+item.getWcsOrderItemId());
				psLogisticProvider = conn.prepareStatement(UPDATE_RESULT_ORDER_FULFILLMENT);				
		
		    	psLogisticProvider.setLong(1, item.getResultStatusId());
		    	psLogisticProvider.setString(2, item.getStatusOrder());
		    	psLogisticProvider.setTimestamp(3, item.getEtdOrderComplate());
		    	psLogisticProvider.setTimestamp(4, item.getNewEtdMax());
		    	psLogisticProvider.setString(5, item.getLate());
		    	psLogisticProvider.setInt(6,item.getLateSecond());	    	
		    	psLogisticProvider.setString(7,item.getIssueId());
		    	psLogisticProvider.setTimestamp(8, currentTimestamp);
		    	psLogisticProvider.setLong(9, item.getOrderfulfillmentId());
		    	
		    	psLogisticProvider.executeUpdate();		    			    	
				
			}catch (Exception e) {
				_log.error("update result of fulfillment " + e.getMessage(), e);
			} finally {
	            try {
	            	if(psLogisticProvider!=null) psLogisticProvider.close();	            
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }		
		 
	 }	
	 
	 private void saveResultStatus(SeattleOrder item){
		 PreparedStatement psLogisticProvider = null;	
		 try{	

			 _log.info("Save status Tracking For Order  : "+item.getWcsOrderItemId());
				psLogisticProvider = conn.prepareStatement(INSERT_RESULT_ORDER_STATUS,Statement.RETURN_GENERATED_KEYS);	
					
				psLogisticProvider.setLong(1, item.getSeatOrderStatusHistory());
		    	psLogisticProvider.setTimestamp(2, item.getUpdateStatusDate());
		    	psLogisticProvider.setTimestamp(3, item.getStatusDueDate());
		    	psLogisticProvider.setString(4, item.getLateStatus());
		    	psLogisticProvider.setInt(5, item.getLateSecondStatus());
		    	psLogisticProvider.setLong(6, item.getResultStatusTrackingId());
		    	psLogisticProvider.setString(7, item.getIssueStatusId());		
		    	psLogisticProvider.setTimestamp(8, currentTimestamp);
		    	psLogisticProvider.setBoolean(9, item.getIssueStatusId()!=null);
		    	psLogisticProvider.setString(10, item.getStatusOrder());
		    	
		    	psLogisticProvider.executeUpdate();		  
		    	ResultSet generatedKeys = psLogisticProvider.getGeneratedKeys();				    
		    	generatedKeys.close();
				
			}catch (Exception e) {
				_log.error("insert result of status Tracking " + e.getMessage(), e);
			}try {
            	if(psLogisticProvider!=null) psLogisticProvider.close();	            
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		 
	 }
	 
	 private void updateResultStatus(SeattleOrder item){
			PreparedStatement psLogisticProvider = null;	
		 try{		
			 _log.info("Update status Tracking For Order  : "+item.getWcsOrderItemId());
				psLogisticProvider = conn.prepareStatement(UPDATE_RESULT_ORDER_STATUS);				
		
		    	psLogisticProvider.setTimestamp(1, item.getStatusDueDate());
		    	psLogisticProvider.setString(2, item.getLateStatus());
		    	psLogisticProvider.setInt(3,item.getLateSecondStatus());	    	
		    	psLogisticProvider.setLong(4, item.getResultStatusTrackingId());
		    	psLogisticProvider.setString(5,item.getIssueStatusId());
		    	psLogisticProvider.setTimestamp(6, currentTimestamp);
		    	psLogisticProvider.setBoolean(7, item.getIssueStatusId()!=null);
		    	psLogisticProvider.setLong(8, item.getOrderStatusTrackingtId());
		    	
		    	psLogisticProvider.executeUpdate();		    			    	
				
			}catch (Exception e) {
				_log.error("update result of status Tracking " + e.getMessage(), e);
			} finally {
	            try {
	            	if(psLogisticProvider!=null) psLogisticProvider.close();	            
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }		
		 
	 }	
	 
	 private void saveOrUpdateResult(SeattleOrder item){		
				if(item.getOrderfulfillmentId()!=null && item.getOrderfulfillmentId()>0){
					updateResult(item);				
				}else{
					saveResult(item);
				}	  				 
	 }	 
	 
	 private void saveOrUpdateResultStatus(SeattleOrder item){		
			if(item.getOrderStatusTrackingtId()!=null && item.getOrderStatusTrackingtId()>0){
				updateResultStatus(item);				
			}else{
				saveResultStatus(item);
			}	  				 
}	 
	 private BigDecimal getSlaAdditional(SeattleOrder item){
			_log.info("getDiffEtd " +item.getDiffEtd());
			if(item.getDiffEtd().compareTo(new BigDecimal(0))==1 
				&& (new Date(currentTimestamp.getTime())).compareTo(item.getStartNewEtd())>=new Long(1)
				&& (new Date(currentTimestamp.getTime())).compareTo(item.getEndNewEtd())<=new Long(1)){
				_log.info("retuen  maxEtd " +item.getDiffEtd());				
				return item.getDiffEtd();				
			}else{
				return null;
			}
	 }
	 private void prosesOrderByStatus(ArrayList<SeattleOrder> orderList){
		      	
	        	ArrayList<SeatSlaStatus> seatSlaStatusList = getSeatSlaStatusByStatus();
	    		try{
	    			for (SeattleOrder item : orderList){	    				    	
	    				if(item.getStatusPayment().equals(new Long(1)) && item.getIssueStatusId()!=null && item.isCloseIssueStatus()){
	    					closeIssue(item.getIssueStatusId());
	    				}else{
		    					Timestamp slaDate =null;
			    				boolean cekHoliday = true;
			    				ArrayList<SeatSlaStatus> seatSlaStatusListTemp = new ArrayList<SeatSlaStatus>();
			    				for(SeatSlaStatus slaItem : seatSlaStatusList){
			    					if(slaItem.getSeatOrderStatus().getOrderStatusDecs().equals("Order Delivery")){
			    						BigDecimal regShipment= item.getLogisticsEtd().divide(new BigDecimal(1.2),RoundingMode.HALF_DOWN);
			    						slaItem.setSla(regShipment);
			    					}
			    					if(slaItem.getSeatOrderStatus().getOrderStatusDecs().equals(jobStatus)){
			    						 slaDate = getSLADueDate(item.getUpdateStatusDate(),slaItem);
			    	    				 cekHoliday = slaItem.getSeatStatusUom().getStatusUomDesc().equals("WorkDay") || slaItem.getSeatStatusUom().getStatusUomDesc().equals("WorkHour") ;
			    					}else{
			    							seatSlaStatusListTemp.add(slaItem);			  
			    					}
			    				}	 	    		
			    				if(cekHoliday){
			    					slaDate=cekHoliday(item.getUpdateStatusDate(),slaDate);
				            	}	
			    				item.setStatusDueDate(slaDate);
			    				
			    				if((item.getOrderfulfillmentId()==0 || (item.getOrderfulfillmentId()!=0 && !item.getOrderStatusDescComplete().equals(item.getStatusOrder()))) 
			    						&& !item.getMoreInfo().contains("BOPIS") && !item.getMoreInfo().contains("Big"))	{
							            	Timestamp sumOfslaDate =slaDate;		            	
						            	
						            	if(getSlaAdditional(item)!=null){
						            		SeatSlaStatus seatSlaStatusFulfill = new SeatSlaStatus();
							            	seatSlaStatusFulfill.setSla(item.getDiffEtd());
							            	seatSlaStatusFulfill.setSeatStatusUom(SeatStatusUomMap.get("WorkDay"));
							            	seatSlaStatusListTemp.add(seatSlaStatusFulfill);
						            	}						            	
						            	_log.info("currentDate : "+currentTimestamp+" DateStatus  : "+item.getUpdateStatusDate() +" compare DateSla : "+slaDate);
						            	if(currentTimestamp.compareTo(slaDate)==1){		            	
						            		_log.info("Lebih");
						            		sumOfslaDate =currentTimestamp;		  		            		
						            	}	            			            			            	
						            	for(SeatSlaStatus slaItem : seatSlaStatusListTemp){
						            		if(!cekHoliday){
						            			 cekHoliday = slaItem.getSeatStatusUom().getStatusUomDesc().equals("WorkDay") || slaItem.getSeatStatusUom().getStatusUomDesc().equals("WorkHour") ;
						            		}
					            			sumOfslaDate = getSLADueDate(sumOfslaDate,slaItem);
					            		}     
						            	if(cekHoliday){
							            	sumOfslaDate=cekHoliday(item.getUpdateStatusDate(),sumOfslaDate);
						            	}		    
						            	item.setEtdOrderComplate(sumOfslaDate);
						            	item.setNewEtdMax(new Timestamp(item.getEtdMax().getTime()));
						            
						            	item= getResultFulfillmentTracking(item);				            
						            	item= getResultIssueOfFulfillment(item);	
							            saveOrUpdateResult(item);
			    				}
					        	item= getResultStatusTracking(item);			      
				            	item= getResultIssue(item);	
					            saveOrUpdateResultStatus(item);
				            	
					            _log.info(item.getWcsOrderId()+","+item.getWcsOrderItemId()+"==>ETD Order Complate : "+item.getEtdOrderComplate()+" new ETD Max :"+
				            			item.getNewEtdMax()+" LateFulfill :"+item.getLate()+
				            			" result Status : "+item.getResultStatusTracking()+" resultStatusid :"+item.getResultStatusTrackingId()+" issueStatus :"+item.getIssueStatusId()+" fulfillment Id :"+item.getOrderfulfillmentId());    
		    			}
	    			}
	    			
	    		}catch (Exception e) {
	    			_log.error(e);    			
	    			throw new EJBException(e);
	    		}	        
	 }
	 
	 private  void closeIssueStatus(){
			PreparedStatement psSeattleList = null;      
	      	ResultSet rsSeattletList = null;	    
		    try{	            
		    	_log.debug("----------Query close issue VA/CS Approved");
		    	psSeattleList = conn.prepareStatement(CLOSE_ISSUE_STATUS, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            
		    	rsSeattletList = psSeattleList.executeQuery();
	    		
		    	rsSeattletList.last();
				int totalOrderList = rsSeattletList.getRow();
				rsSeattletList.beforeFirst();
				rsSeattletList.next();
	    		
				_log.info("Query returns: " + totalOrderList + " row(s)");			
				if(totalOrderList>0){				   				
		            for (int i=0; i<totalOrderList;i++) {
		            	String issueId = rsSeattletList.getString("issue_id");	            
		            	closeIssue(issueId);	            		            	
		            	rsSeattletList.next();	            	
		            }
				}
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	            	if(psSeattleList!=null) psSeattleList.close();
	            	if(rsSeattletList!=null) rsSeattletList.close();
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	        
		}	
	 private  void closeIssueCompleteStatus(){
			PreparedStatement psSeattleList = null;      
	      	ResultSet rsSeattletList = null;	    
		    try{	            
		    	_log.debug("----------Query close issue Complete VA/CS Approved");
		    	psSeattleList = conn.prepareStatement(CLOSE_ISSUE_STATUS_COMPLETE, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            
		    	rsSeattletList = psSeattleList.executeQuery();
	    		
		    	rsSeattletList.last();
				int totalOrderList = rsSeattletList.getRow();
				rsSeattletList.beforeFirst();
				rsSeattletList.next();
	    		
				_log.info("Query returns: " + totalOrderList + " row(s)");			
				if(totalOrderList>0){				   				
		            for (int i=0; i<totalOrderList;i++) {
		            	String issueId = rsSeattletList.getString("issue_id");	            
		            	closeCompleteIssue(issueId);	            		            	
		            	rsSeattletList.next();	            	
		            }
				}
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	            	if(psSeattleList!=null) psSeattleList.close();
	            	if(rsSeattletList!=null) rsSeattletList.close();
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	        
		}	
	 
	 
	 public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		 SeattleStatusOrderPaymentApprovalBatchJob seattleJob = new SeattleStatusOrderPaymentApprovalBatchJob();
	    	_log.info("Start SeattleStatusOrderPaymentApprovalBatchJob");
	        
	        Long startTime = System.currentTimeMillis();
	         
	        Calendar calendar = Calendar.getInstance();
        	currentTimestamp = new Timestamp(calendar.getTime().getTime());	                     
	        try{	        				 
	        	      seattleJob. setUoM();	  	  
	        	      for(Long IdStatus : statusOrder){
				        	 ArrayList<SeattleOrder> orderList = seattleJob.fetchOrderByStatus(IdStatus);
				        	 if(orderList!=null){	
				        		 _log.info("start calculate by status VA/CS");
				        		 	seattleJob.prosesOrderByStatus(orderList);
				        	 }else{
				 	        	_log.info("No order for calculated by status VA/CS");
				 	        }
	        	      }
	        	  	 _log.info("close Issue CS/VA");
		        	  seattleJob.closeIssueStatus();
		        	  _log.info("close Issue Complete CS/VA");
		        	  seattleJob.closeIssueCompleteStatus();
			        
	        }catch (Exception e) {
    			_log.error(e);    			
    			throw new EJBException(e);
    		}finally{
    		
    			conn.close();
    		}	        
	      
	        Long endTime = System.currentTimeMillis();
	        _log.info("SeattleStatusOrderPaymentApprovalBatchJob finished, with duration:" + (endTime - startTime) + "ms");
	    }
}
