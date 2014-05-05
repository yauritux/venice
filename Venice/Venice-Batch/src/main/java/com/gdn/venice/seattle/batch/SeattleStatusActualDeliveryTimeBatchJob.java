package com.gdn.venice.seattle.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.gdn.app.jiraclient.util.GdnJiraCustomClientImpl;
import com.gdn.venice.persistence.SeatSlaStatus;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.seattle.bean.SeattleOrder;

public class SeattleStatusActualDeliveryTimeBatchJob {
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
	private static Long statusOrder;
	private static String jobStatus;
	private static Timestamp currentTimestamp=null;
	private static GdnJiraClient jira =null;
	
	private static Map<String,SeatStatusUom> SeatStatusUomMap=null;
	
	private static final String STATUS_ORDER_BY_STATUS_ID = "select sosh.seat_order_status_history_id , vo.wcs_order_id ,voi.wcs_order_item_id,"+
    " voi.logistics_service_id,soe.etd_max,soe.logisticsetd,lab.received"+
    " from seat_order_status_history sosh"+
    " left join ven_order vo on vo.order_id=sosh.order_id"+
    " left join ven_order_item voi on voi.order_item_id=sosh.order_item_id"+
    " left join log_airway_bill lab on lab.order_item_id=voi.order_item_id"+
    " left join ven_order_status vos on vos.order_status_id=sosh.order_status_id"+
    " left join seat_order_etd soe on soe.order_etd_id=sosh.order_etd_id"+
    " left join seat_order_status_tracking sost on sost.seat_order_status_history_id=sosh.seat_order_status_history_id and sost.status_desc='aD'"+
    "  where sosh.order_status_id = ? and sost.seat_order_status_tracking_id is null and lab.received is not null order by sosh.order_status_id";
	
	private static final String SLA_BY_STATUS_ID = "select sss.sla, sss.sla_second, ssu.status_uom_id, ssu.status_uom_desc, ssu.status_uom_type, ssu.status_uom_from, ssu.status_uom_end" +
			" from seat_sla_status sss" +
			" left join seat_fulfillment_consist_of_sla_status sfcoss on sfcoss.sla_status_id=sss.sla_status_id" +
			" left join seat_order_status sos on sos.seat_order_status_id=sfcoss.seat_order_status_id" +
			" left join seat_status_uom ssu on ssu.status_uom_id=sss.status_uom_id" +
			" where sos.order_status_decs=? order by sfcoss.sla_status_id asc";
	
	private static final String RESULT_STATUS_TARCKING = "select sssp.seat_sla_status_percentage_id ,srst.result_status_tracking_desc" +
			" from seat_sla_status_percentage sssp" +
			" left join seat_sla_status sss on sss.sla_status_id=sssp.sla_status_id" +
			" left join seat_order_status sos on sos.seat_order_status_id=sss.seat_order_status_id" +
			" left join seat_result_status_tracking srst on srst.result_status_tracking_id=sssp.result_status_tracking_id" +		
			" where ? between sssp.min and sssp.max  and sos.order_status_decs=?";

	private static final String UOM = "select * From seat_status_uom";
	
	private static final String GET_MORE_INFO_FOR_ISSUE = "select vosh.history_timestamp from ven_order vo"+
	" inner join ven_order_status_history vosh on vosh.order_id=vo.order_id"+
	" where vosh.order_status_id=4 and vo.wcs_order_id=?";
	
	private static final String HOLIDAY_TIMENTAMP = "select * from ven_holiday where holiday_date between ? and ? order by holiday_date";
	

	
	private static final String INSERT_RESULT_ORDER_STATUS = "insert into seat_order_status_tracking  (seat_order_status_history_id,status_timestamp,status_due_date," +
			"status_late_time,status_late_time_second,seat_sla_status_percentage_id,issue_id,created_date,status_issue,status_desc) values (?, ?, ?,?, ?, ?,?, ?,?,?)";

	

	
    private SeattleStatusActualDeliveryTimeBatchJob() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.seattle.batch.SeattleStatusActualDeliveryTimeBatchJob");
        
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
		
		statusOrder= new Long(5);//status D
		jobStatus = new String("Actual Delivery");
		
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
	            	item.setUpdateStatusDate(rsSeattletList.getTimestamp("received"));	  
	            	item.setLogisticsEtd(rsSeattletList.getBigDecimal("logisticsetd"));
	            	item.setTypeOfOrder(rsSeattletList.getLong("logistics_service_id"));	          
	            		            	
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
			_log.info("loop after hour = "+later.getHours()+" minut : "+later.getMinutes()+" secon :"+later.getSeconds());
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
	        long selisihMS = Math.abs(waktuSatu.getTime()-waktuDua.getTime());
	        long selisihDetik = selisihMS / 1000 % 60;
	        long selisihMenit = selisihMS / (60 * 1000) % 60;
	        long selisihJam = selisihMS / (60 * 60 * 1000) % 24;
	        long selisihHari = selisihMS / (24 * 60 * 60 * 1000);
	        String selisih = selisihHari + " hari " + selisihJam + " Jam "
	                + selisihMenit + " Menit " + selisihDetik + " Detik";
	        return selisih;
	    }
	 

	 private Timestamp getInfoForIssue(SeattleOrder item){
		 
		 Timestamp  info = null;		 
		    PreparedStatement psResultStatusList = null;      
	      	ResultSet rsResultStatusList = null;	  
			 try{	            				 
					 	psResultStatusList = conn.prepareStatement(GET_MORE_INFO_FOR_ISSUE, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);       
						psResultStatusList.setString(1, item.getWcsOrderId());	
				    	rsResultStatusList = psResultStatusList.executeQuery();		    		
				    	rsResultStatusList.last();
						int totalResultStatusList = rsResultStatusList.getRow();
						rsResultStatusList.beforeFirst();
						rsResultStatusList.next();		    							
						
						if(totalResultStatusList>0){
							info = rsResultStatusList.getTimestamp("history_timestamp");							
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
				    	psResultStatusList.setLong(2, statusOrder);
				    	rsResultStatusList = psResultStatusList.executeQuery();		    		
				    	rsResultStatusList.last();
						int totalResultStatusList = rsResultStatusList.getRow();
						rsResultStatusList.beforeFirst();
						rsResultStatusList.next();		    							
						
						_log.info("Query returns: " + totalResultStatusList + " row(s)");
			            for (int i=0; i<totalResultStatusList;i++) {
			            	item.setResultStatusTrackingId(rsResultStatusList.getLong("seat_sla_status_percentage_id"));
			            	item.setResultStatusTracking(rsResultStatusList.getString("result_status_tracking_desc"));
			            	item.setLateStatus("0");
			            	item.setLateSecondStatus(0); 
			            	if(item.getUpdateStatusDate().compareTo(item.getStatusDueDate())==new Long(1)){
								item.setLateStatus(selisihDateTime(item.getUpdateStatusDate(),item.getStatusDueDate()));
				            	item.setLateSecondStatus((int) Math.abs(item.getUpdateStatusDate().getTime()-item.getStatusDueDate().getTime()));		 
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
		    	psLogisticProvider.setString(10,"aD");
		    	
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

	 private void prosesOrderByStatus(ArrayList<SeattleOrder> orderList){
		 		SeatStatusUom uomItem =  SeatStatusUomMap.get("WorkHour");	
		 
	        	ArrayList<SeatSlaStatus> seatSlaStatusList = getSeatSlaStatusByStatus();
	    		try{
	    			for (SeattleOrder item : orderList){	    				    				
	    				ArrayList<SeatSlaStatus> seatSlaStatusListTemp = new ArrayList<SeatSlaStatus>();
	    				for(SeatSlaStatus slaItem : seatSlaStatusList){
	    					seatSlaStatusListTemp.add(slaItem);
	    				}	
	    				 Timestamp slaDate = null; 
		    			 boolean cekHoliday = seatSlaStatusListTemp.get(0).getSeatStatusUom().getStatusUomDesc().equals("WorkDay") || seatSlaStatusListTemp.get(0).getSeatStatusUom().getStatusUomDesc().equals("WorkHour") ;

	    				if(item.getTypeOfOrder().equals(new Long(0)) || item.getTypeOfOrder().equals(new Long(1))){
	    					slaDate=getInfoForIssue(item);
	    					if(slaDate!=null){
	    						slaDate=getSLADueDate(slaDate,seatSlaStatusListTemp.get(0)); 
	    					}
	    				}else{
	    					slaDate=getTimeafterAddDay(new Timestamp(item.getEtdMax().getTime()),item.getLogisticsEtd().intValue());
	    				}	    			
	    				slaDate=getStartTimeWithUoMWorkHour(slaDate,uomItem);		    			  
	    				if(cekHoliday){
	    					slaDate=cekHoliday(item.getUpdateStatusDate(),slaDate);
		            	}	

	    				item.setStatusDueDate(slaDate); 		
			        	item= getResultStatusTracking(item);			
			        	saveResultStatus(item);	
		            	
			            _log.info(item.getWcsOrderId()+","+item.getWcsOrderItemId()+"==> new ETD Max :"+
		            			item.getStatusDueDate()+" result Status : "+item.getResultStatusTracking()+" resultStatusid :"+item.getResultStatusTrackingId()+" issueStatus :"+item.getLateStatus());    
	    			}
	    			
	    		}catch (Exception e) {
	    			_log.error(e);    			
	    			throw new EJBException(e);
	    		}	        
	 }
	 public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		 SeattleStatusActualDeliveryTimeBatchJob seattleJob = new SeattleStatusActualDeliveryTimeBatchJob();
	    	_log.info("Start SeattleStatusActualDeliveryTimeBatchJob");
	        
	        Long startTime = System.currentTimeMillis();
	         
	        Calendar calendar = Calendar.getInstance();
        	currentTimestamp = new Timestamp(calendar.getTime().getTime());	                     
	        try{	        				 
	        	         seattleJob. setUoM();	  	  
			        	 ArrayList<SeattleOrder> orderList = seattleJob.fetchOrderByStatus(statusOrder);
			        	 if(orderList!=null){	
			        		 _log.info("start calculate by status D");
			        		 	seattleJob.prosesOrderByStatus(orderList);
			        	 }else{
			 	        	_log.info("No order for calculated by status D");
			 	        }			        	
	        }catch (Exception e) {
    			_log.error(e);    			
    			throw new EJBException(e);
    		}finally{
    		
    			conn.close();
    		}	        
	      
	        Long endTime = System.currentTimeMillis();
	        _log.info("SeattleStatusActualDeliveryTimeBatchJob finished, with duration:" + (endTime - startTime) + "ms");
	    }
}
