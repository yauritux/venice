package com.gdn.venice.seattle.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.gdn.app.jiraclient.util.GdnJiraCustomClientImpl;
import com.gdn.venice.persistence.SeatSlaStatus;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.seattle.bean.SeattleOrder;

public class SeattleFulfillmentOrderBatchJob {
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
	private static Map<Integer,Long> statusOrder=null;
	private static Map<Long,String> jobStatus=null;
	private static Timestamp currentTimestamp=null;
	private static GdnJiraClient jira =null;
	
	private static Map<String,SeatStatusUom> SeatStatusUomMap=null;
	
	private static final String STATUS_ORDER_BY_STATUS_ID = "select sosh.seat_order_status_history_id , vo.wcs_order_id ,voi.wcs_order_item_id,srst.result_status_tracking_desc," +
			" vos.order_status_code, sosh.update_status_date, soe.etd_max,soe.start_date,soe.end_date,soe.logisticsetd,  vo.order_timestamp,vos.order_status_id,sof.order_fulfillment_id,sof.issue_id,soe.diff_etd,vop.payment_status_id" +
			" from seat_order_status_history sosh" +
			" left join ven_order vo on vo.order_id=sosh.order_id" +
			" left join ven_order_payment_allocation vopa on vopa.order_id=vo.order_id" +
			" left join ven_order_payment vop on vop.order_payment_id=vopa.order_payment_id" +
			" left join ven_order_item voi on voi.order_item_id=sosh.order_item_id" +
			" left join ven_order_status vos on vos.order_status_id=sosh.order_status_id" +
			" left join seat_order_etd soe on soe.order_etd_id=sosh.order_etd_id" +
			" left join seat_order_fulfillment sof on sof.seat_order_status_history_id=sosh.seat_order_status_history_id" +
			" left join seat_fulfillment_in_percentage sfip on sfip.fulfillment_in_percentage_id=sof.fulfillment_in_percentage_id" +
			" left join seat_result_status_tracking srst on srst.result_status_tracking_id=sfip.result_status_tracking_id" +
			" where sosh.order_status_id = ? " +
			" and (vos.order_status_code not in ('D','X') or (vos.order_status_code in ('D','X')  and sof.issue_id is not null and sof.status_issue=false) ) " +
			" and ((vop.payment_status_id=1 and vos.order_status_code <> 'VA') or (vop.payment_status_id=0 and vos.order_status_code = 'VA')" +
			" or (vop.payment_status_id=1 and vos.order_status_code = 'VA' and sof.issue_id is not null and sof.status_issue=false) )" +
			" order by sosh.order_status_id";
	
	private static final String SLA_BY_STATUS_ID = "select sss.sla, sss.sla_second, ssu.status_uom_id, ssu.status_uom_desc, ssu.status_uom_type, ssu.status_uom_from, ssu.status_uom_end" +
			" from seat_sla_status sss" +
			" left join seat_fulfillment_consist_of_sla_status sfcoss on sfcoss.sla_status_id=sss.sla_status_id" +
			" left join seat_order_status sos on sos.seat_order_status_id=sfcoss.seat_order_status_id" +
			" left join seat_status_uom ssu on ssu.status_uom_id=sss.status_uom_id" +
			" where sos.order_status_decs=? order by sfcoss.sla_status_id asc";
	

	private static final String RESULT_STATUS_TARCKING = "select sfip.fulfillment_in_percentage_id ,srst.result_status_tracking_desc" +
			" from seat_fulfillment_in_percentage sfip" +
			" left join seat_order_status sos on sos.seat_order_status_id=sfip.seat_order_status_id" +
			" left join seat_result_status_tracking srst on srst.result_status_tracking_id=sfip.result_status_tracking_id" +
			" where ? between sfip.min and sfip.max  and sos.order_status_decs=?";
	
	private static final String UOM = "select * From seat_status_uom";
	
	private static final String HOLIDAY_TIMENTAMP = "select * from ven_holiday where holiday_date between ? and ? order by holiday_date";
	
	private static final String INSERT_RESULT_ORDER_FULFILLMENT = "insert into seat_order_fulfillment  (seat_order_status_history_id,fulfillment_in_percentage_id,order_status_dec," +
			"etd_order_complete,new_etd_max_order,order_process_late_time,order_process_late_time_second,issue_id,created_date,status_issue) values (?, ?, ?,?, ?, ?,?, ?, ?,?)";
	
	private static final String UPDATE_RESULT_ORDER_FULFILLMENT = "update  seat_order_fulfillment  set fulfillment_in_percentage_id=? ,order_status_dec=? ," +
			"etd_order_complete=?,new_etd_max_order=?,order_process_late_time=?,order_process_late_time_second=?,issue_id=?,created_date=?" +
			" where order_fulfillment_id=?";
	
	private static final String UPDATE_STATUS_FULFILLMENT_ISSUE = "update  seat_order_fulfillment  set status_issue=?," +
	" where issue_id=?" ;
	
    private SeattleFulfillmentOrderBatchJob() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.seattle.batch.SeattleFulfillmentOrderBatchJob");
        
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
	            	item.setStatusPayment(rsSeattletList.getLong("payment_status_id"));      
	            	item.setStartNewEtd(rsSeattletList.getDate("start_date"));
	            	item.setEndNewEtd(rsSeattletList.getDate("end_date"));
	            	item.setLogisticsEtd(rsSeattletList.getBigDecimal("logisticsetd"));
	            	item.setResultStatus(rsSeattletList.getString("result_status_tracking_desc"));
	            			
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
	
	private ArrayList<SeatSlaStatus>  getSeatSlaStatusByStatus(String jobStatus){
		PreparedStatement psSLAStatusList = null;      
      	ResultSet rsSLAStatusList = null;
      	ArrayList<SeatSlaStatus> slaStatusList = null;	
		 try{	            
		    	_log.debug("This Sla Status consist Of  "+jobStatus);
		    	
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
		            	item.setSla(rsSLAStatusList.getBigDecimal("sla"));
		            	item.setSlaSecond(rsSLAStatusList.getBigDecimal("sla_second"));		            	
		            	SeatStatusUom itemSeatStatusUom = new SeatStatusUom();		            	
		            	itemSeatStatusUom.setStatusUomDesc(rsSLAStatusList.getString("status_uom_desc"));	
		            	itemSeatStatusUom.setStatusUomType(rsSLAStatusList.getString("status_uom_type"));			            	            	
		            	item.setSeatStatusUom(itemSeatStatusUom);		            	
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
					
			orderStatusUpdateDate=getStartTimeWithUoMWorkHour(orderStatusUpdateDate,uomItem);	
			int rangeUoM = new Integer(uomItem.getStatusUomEnd()+"") - new Integer(uomItem.getStatusUomFrom()+"");
			int slaValue = new Integer(seatSlaStatus.getSla()+"");
			_log.info("get Hour Timebefore "+orderStatusUpdateDate+" sla : "+slaValue);
			
			Timestamp later= orderStatusUpdateDate;			
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
			if(later.getHours()==new Integer(uomItem.getStatusUomEnd()+"") && (later.getMinutes() > 0 || later.getSeconds() > 0)){
				later.setHours(new Integer(uomItem.getStatusUomFrom()+""));
			}
			
			_log.info("get calTimeWithWorkHour before " + orderStatusUpdateDate +" after :"+later);				 
			return later;
		 }	
	 
	 private Timestamp calTimeWithWorkDay(Timestamp orderStatusUpdateDate,SeatSlaStatus seatSlaStatus){			
		 	SeatStatusUom uomItem =  SeatStatusUomMap.get("WorkHour");	
		 	
		 	int addday = new Integer(seatSlaStatus.getSla()+"");
		 	int rangeUoM = new Integer(uomItem.getStatusUomEnd()+"") - new Integer(uomItem.getStatusUomFrom()+"");
		 	_log.info("get Day Timebefore "+orderStatusUpdateDate+" rangeUoM : "+rangeUoM+" day : "+addday);
		 
		 	SeatSlaStatus newSlaStatus = new SeatSlaStatus();
		 	newSlaStatus.setSla(new BigDecimal(rangeUoM*addday));
	 
		 	Timestamp later  = calTimeWithWorkHour(orderStatusUpdateDate,newSlaStatus);			
				 	
			    
			_log.info("get TimeWithWorkDay before " + orderStatusUpdateDate +" after :"+later);				 
		 return later;
	 }
	 
	 protected static String selisihDateTime(Timestamp  waktuSatu, Timestamp waktuDua) {
	        long selisihMS = Math.abs(waktuSatu.getTime() - waktuDua.getTime());
	        long selisihDetik = selisihMS / 1000 % 60;
	        long selisihMenit = selisihMS / (60 * 1000) % 60;
	        long selisihJam = selisihMS / (60 * 60 * 1000) % 24;
	        long selisihHari = selisihMS / (24 * 60 * 60 * 1000);
	        String selisih = selisihHari + " hari " + selisihJam + " Jam "
	                + selisihMenit + " Menit " + selisihDetik + " Detik";
	        return selisih;
	    }
	 private void loadVariabel(){
		 statusOrder = new HashMap<Integer,Long> ();
		 statusOrder.put(0, new Long(0));//VA
		statusOrder.put(1, new Long(18));//CS
		 statusOrder.put(2, new Long(1));//C 
		 statusOrder.put(3, new Long(4));//FP 
		statusOrder.put(4, new Long(8));//PU
		statusOrder.put(5, new Long(16));//CX
		 statusOrder.put(6, new Long(5));//D
		 statusOrder.put(7, new Long(6));//X
		 
		 jobStatus = new HashMap<Long,String> ();
		 jobStatus.put(new Long(0),"Payment Approval");//VA
		 jobStatus.put(new Long(18),"Payment Approval");//CS
		 jobStatus.put(new Long(1),"Fraud Checking");//C 
		jobStatus.put(new Long(4),"Order Fulfill");//FP 
		 jobStatus.put( new Long(8),"Logistic Settle");//PU
		 jobStatus.put( new Long(16),"Order Delivery");//CX
		 jobStatus.put( new Long(5),"Actual Delivery");//D
		 jobStatus.put(new Long(6),"Actual Delivery");//X
				 
	 }	
	 private void closeIssue(String issueID){ 		 

		 PreparedStatement psCloseIssue = null;						
		 try {
			 jira.closeOrderTrackingIssue(issueID);			
			_log.info("Close Issue Id  : "+issueID);
			psCloseIssue = conn.prepareStatement(UPDATE_STATUS_FULFILLMENT_ISSUE);				

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
	 
	 private OrderCompleteProcessIssue getInfoOfIssueOrderComplete(SeattleOrder item){		 
		 OrderCompleteProcessIssue itemIssue = new OrderCompleteProcessIssue();			 
			itemIssue.setEtdOrderComplete(item.getEtdOrderComplate());
			itemIssue.setNewEtdMaxOrder(item.getNewEtdMax());
			itemIssue.setOrderProcessLateTime(item.getLate());
			itemIssue.setOrderId(item.getWcsOrderId());
			itemIssue.setOrderItemId(item.getWcsOrderItemId());
			
		 return itemIssue;
	 }	 
	 	 
	private SeattleOrder getResultIssue(SeattleOrder item){ 
		  /**
		  * cek sudah ada issue atau belum
		  */
		 try {
				 if(item.getIssueId()!=null){
					 OrderCompleteProcessIssue itemIssue = getInfoOfIssueOrderComplete(item);					   
					  itemIssue.setIssueKey(item.getIssueId());
					  _log.info("getIssueId "+item.getIssueId()+" result Issue "+item.getResultStatus());	
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
			     _log.info("Issue Id "+issue );
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
	 
	 private SeattleOrder getResultStatusTracking(SeattleOrder item){
		 PreparedStatement psResultStatusList = null;      
	      	ResultSet rsResultStatusList = null;	  
			 try{	            
				   Date maxEtd = getMaxEtd(item); 
				     item.setNewEtdMax(new Timestamp(maxEtd.getTime()));
					 long selisihMS = Math.round(new Double(Math.abs(item.getEtdOrderComplate().getTime() - item.getOrderTimestamp().getTime()))/new Double(Math.abs(maxEtd.getTime()- item.getOrderTimestamp().getTime()))*100);

					  _log.info("min "+Math.abs(item.getEtdOrderComplate().getTime() - item.getOrderTimestamp().getTime())+" ==> bagi: "+Math.abs(maxEtd.getTime()- item.getOrderTimestamp().getTime()));
					  _log.info("selisih "+selisihMS+" ==>Orderdate: "+item.getOrderTimestamp()+" new ETD Max :"+maxEtd+"' DueDate : "+item.getEtdOrderComplate());    
					 	psResultStatusList = conn.prepareStatement(RESULT_STATUS_TARCKING, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);            			            
				    	psResultStatusList.setInt(1, new Integer(selisihMS+""));
				    	psResultStatusList.setString(2, jobStatus.get(item.getOrderStatusId()));
				    	rsResultStatusList = psResultStatusList.executeQuery();		    		
				    	rsResultStatusList.last();
						int totalResultStatusList = rsResultStatusList.getRow();
						rsResultStatusList.beforeFirst();
						rsResultStatusList.next();		    							
						
						_log.info("Query returns: " + totalResultStatusList + " row(s)");
			            for (int i=0; i<totalResultStatusList;i++) {
			            	item.setResultStatusId(rsResultStatusList.getLong("fulfillment_in_percentage_id"));
			            	_log.info("item.getResultStatus() " + item.getResultStatus() + " row(s)");
			            	if(item.getResultStatus()!=null && (item.getResultStatus().equals("Late") && rsResultStatusList.getString("result_status_tracking_desc").equals("Late") ||
			            			item.getResultStatus().equals("Attention") && rsResultStatusList.getString("result_status_tracking_desc").equals("Attention") )){
			            		item.setResultStatus(item.getResultStatus()+rsResultStatusList.getString("result_status_tracking_desc"));
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
	 private void saveOrUpdateResult(SeattleOrder item){		
				if(item.getOrderfulfillmentId()!=null && item.getOrderfulfillmentId()>0){
					updateResult(item);				
				}else{
					saveResult(item);
				}	  				 
	 }	 
	 private void prosesOrderByStatus(ArrayList<SeattleOrder> orderList,Long statusId){		      	
	        	ArrayList<SeatSlaStatus> seatSlaStatusList = getSeatSlaStatusByStatus(jobStatus.get(statusId));
	    		try{
	    			for (SeattleOrder item : orderList){	  
	    				if(item.getIssueId()!=null && (item.getStatusOrder().equals("D") || item.getStatusOrder().equals("X") 
	    						|| (item.getStatusOrder().equals("VA") && item.getStatusPayment().equals(new Long(1))) )){
	    					closeIssue(item.getIssueId());
	    				}else{
			    				ArrayList<SeatSlaStatus> seatSlaStatusListTemp = new ArrayList<SeatSlaStatus>();
			    				for(SeatSlaStatus slaItem : seatSlaStatusList){
			    					seatSlaStatusListTemp.add(slaItem);
			    				}	    							    				
			    				_log.info("Status Calculate is "+item.getStatusOrder()+" size of SLA : "+seatSlaStatusListTemp.size());
			    			   Timestamp slaDate = getSLADueDate(item.getUpdateStatusDate(),seatSlaStatusListTemp.get(0));
			    				boolean cekHoliday = seatSlaStatusListTemp.get(0).getSeatStatusUom().getStatusUomDesc().equals("WorkDay") || seatSlaStatusListTemp.get(0).getSeatStatusUom().getStatusUomDesc().equals("WorkHour") ;
			    				seatSlaStatusListTemp.remove(0);
				            	Timestamp sumOfslaDate =slaDate;		            	
				            	
				            	
				            	if(item.getDiffEtd()!=null && item.getDiffEtd().compareTo(new BigDecimal(0))==1){
				            		SeatSlaStatus seatSlaStatusFulfill = new SeatSlaStatus();
					            	seatSlaStatusFulfill.setSla(item.getDiffEtd());
					            	seatSlaStatusFulfill.setSeatStatusUom(SeatStatusUomMap.get("WorkDay"));
					            	seatSlaStatusListTemp.add(seatSlaStatusFulfill);
				            	}
				            	SeatSlaStatus seatSlalogisticsEtd = new SeatSlaStatus();				            	
				            	seatSlalogisticsEtd.setSla(item.getLogisticsEtd());
				            	seatSlalogisticsEtd.setSeatStatusUom(SeatStatusUomMap.get("WorkDay"));
				            	seatSlaStatusListTemp.add(seatSlalogisticsEtd);
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
				            
				            	item= getResultStatusTracking(item);				            
				            	item= getResultIssue(item);	
					            saveOrUpdateResult(item);
				            	
				            	_log.info(item.getOrderfulfillmentId()+"========>ETD Order Complate : "+item.getEtdOrderComplate()+" new ETD Max :"+
				            			item.getNewEtdMax()+" Late :"+item.getLate()+
				            			" result Status : "+item.getResultStatus()+" resultStatusid :"+item.getResultStatusId()+" issue :"+item.getIssueId());    
	    				}
	    			}
	    			
	    		}catch (Exception e) {
	    			_log.error(e);    			
	    			throw new EJBException(e);
	    		}	        
	 }
	 
	 
	 public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		 SeattleFulfillmentOrderBatchJob seattleJob = new SeattleFulfillmentOrderBatchJob();
	    	_log.info("Start SeattleFulfillmentOrderBatchJob");
	        
	        Long startTime = System.currentTimeMillis();
	         
	        Calendar calendar = Calendar.getInstance();
        	currentTimestamp = new Timestamp(calendar.getTime().getTime());	                     
	        try{	        				 
	        	 seattleJob. loadVariabel();
	 	         seattleJob. setUoM();	  
	 	         
			        for(int i=0 ;i<statusOrder.size();i++){
			        	 ArrayList<SeattleOrder> orderList = seattleJob.fetchOrderByStatus(statusOrder.get(i));
			        	 if(orderList!=null){	
			        		 _log.info("=========>>>>start calculate by status "+jobStatus.get(statusOrder.get(i)));			        		
			        		 	seattleJob.prosesOrderByStatus(orderList,statusOrder.get(i));
			        	 }else{
			 	        	_log.info("==========<<<<<<No order for calculated by status "+jobStatus.get(statusOrder.get(i)));
			 	        }
			        }
	        }catch (Exception e) {
    			_log.error(e);    			
    			throw new EJBException(e);
    		}finally{
    		
    			conn.close();
    		}	        
	      
	        Long endTime = System.currentTimeMillis();
	        _log.info("SeattleFulfillmentOrderBatchJob finished, with duration:" + (endTime - startTime) + "ms");
	    }
}
