package com.gdn.venice.facade.logistics.activity.processor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.djarum.raf.utilities.SQLDateUtility;
import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.dao.LogActivityReconRecordDAO;
import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenReturnItemDAO;
import com.gdn.venice.exception.ActivityReportDataFilterException;
import com.gdn.venice.exception.ActivityReportFileParserException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.facade.logistics.activity.filter.ActivityReportDataFilter;
import com.gdn.venice.facade.logistics.activity.parser.ActivityReportFileParser;
import com.gdn.venice.facade.logistics.activity.parser.MSGActivityReportFileParser;
import com.gdn.venice.facade.spring.LogAirwayBillService;
import com.gdn.venice.facade.spring.VenOrderItemService;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.logistics.dataimport.DailyReportJNE;
import com.gdn.venice.logistics.dataimport.DailyReportMSG;
import com.gdn.venice.logistics.dataimport.DailyReportNCS;
import com.gdn.venice.logistics.dataimport.LogisticsConstants;
import com.gdn.venice.logistics.integration.AirwayBillEngineClientConnector;
import com.gdn.venice.logistics.integration.bean.AirwayBillTransaction;
import com.gdn.venice.persistence.LogActivityReportUpload;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.LogFileUploadLog;
import com.gdn.venice.persistence.LogLogisticsProvider;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenReturItem;
import com.gdn.venice.util.VeniceConstants;

@Service("MSGActivityReportProcessor")
public class MSGActivityReportProcessor extends ActivityReportProcessor {

	protected static Logger _log = Log4jLoggerFactory.getLogger("com.gdn.venice.facade.logistics.activity.processor.MSGActivityReportProcessor");
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
    @Qualifier("MSGActivityReportDataFilter")
	ActivityReportDataFilter filter;
	
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	
	@Autowired
	VenReturnItemDAO venReturnItemDAO;
	
	@Autowired
	LogAirwayBillDAO logAirwayBillDAO;
	
	@Autowired
	LogActivityReconRecordDAO logActivityReconRecordDAO;
	
	@Autowired
	VenOrderItemService venOrderItemService;
	
	@Autowired
	LogAirwayBillService logAirwayBillService;
	
	@Autowired
	DataSource dataSource;
	
	ActivityReportFileParser fileParser = new MSGActivityReportFileParser();
	
	@Override
	public Logger getLogger() {
		return _log;
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void process(LogFileUploadLog fileUploadLog) {
		try {
			 
			if(awbConn == null)
			   awbConn = new AirwayBillEngineClientConnector();
			
			ActivityReportData activityReportData = fileParser.parse(fileUploadLog.getFileUploadNameAndLoc());
		    activityReportData = filter.filter(activityReportData);
		    
		    LogActivityReportUpload logActivityReportUpload = createLogActivityReportUpload(fileUploadLog, activityReportData.getOrderItemList().size(), getLogLogisticsProvider());
		    activityReportData.setActivityReportUpload(logActivityReportUpload);
			
		    DailyReportMSG dailyReportOrderItem = null;
		    
		    for(PojoInterface item : activityReportData.getOrderItemList()){
		    	dailyReportOrderItem = (DailyReportMSG) item;
		    	
		    	if(filter.isReturnOrderItem(dailyReportOrderItem.getRefNo())){
		    		processEachReturnItem(dailyReportOrderItem);
		    	}else{
		    		processEachOrderItem(dailyReportOrderItem, activityReportData, fileUploadLog);
		    	}
		    }
		    
		    generateUploadFailData(activityReportData, fileUploadLog);
		    fileUploadLog.setUploadStatus("Success");
		    updateUploadStatus(fileUploadLog);
			
		} catch (ActivityReportFileParserException e) {
			_log.error("ActivityReportFileParserException thrown", e);
			e.printStackTrace();
			
			fileUploadLog.setUploadStatus("System Fail");
			updateUploadStatus(fileUploadLog);
		} catch (ActivityReportDataFilterException e) {
			_log.error("ActivityReportDataFilterException thrown", e);
			e.printStackTrace();
			
			fileUploadLog.setUploadStatus("System Fail");
			updateUploadStatus(fileUploadLog);
		} catch (Exception e) {
			_log.error("Exception thrown", e);
			e.printStackTrace();
			
			fileUploadLog.setUploadStatus("System Fail");
			updateUploadStatus(fileUploadLog);
		} 
	}
	
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void processEachReturnItem(DailyReportMSG dailyReportOrderItem){
		
//		VenReturItem venReturnItem = venReturnItemDAO
//				.findWithVenOrderStatusByWcsReturItemId(
//						filter.getWcsOrderItemId(
//								dailyReportOrderItem.getRefNo()));
//		
//		if(!isOrderItemEligibleForStatusUpdate(dailyReportOrderItem, venReturnItem.getVenReturStatus().getOrderStatusId())){
//			_log.debug("Not eligible for status update, Ignoring order item : " + venReturnItem.getWcsReturItemId());
//			return;
//		}
		
	}
	
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void processEachOrderItem(DailyReportMSG dailyReportOrderItem, ActivityReportData activityReportData, LogFileUploadLog fileUploadLog){
		String gdnRef = dailyReportOrderItem.getRefNo();
		String wcsOrderItemId = filter.getWcsOrderItemId(gdnRef);
		
		VenOrderItem existingVenOrderItem = venOrderItemDAO
				.findWithVenOrderStatusByWcsOrderItemId(wcsOrderItemId);
		
		if(!isOrderItemEligibleForStatusUpdate(dailyReportOrderItem, existingVenOrderItem.getVenOrderStatus().getOrderStatusId())){
			_log.debug("Not eligible for status update, Ignoring order item : " + existingVenOrderItem.getWcsOrderItemId());
			return;
		}
		
		int totalAirwayBillByGDNRef = logAirwayBillDAO.countByGdnReference(gdnRef);
		
		AirwayBillTransaction airwayBillTransaction = new AirwayBillTransaction();
		
		try {
			airwayBillTransaction = awbConn.getAirwayBillTransaction(gdnRef);
		} catch (Exception e) {
			addFailedRecordCausedByAWBException(dailyReportOrderItem, activityReportData, e);
			return;
		}
		
		boolean isOrderItemAfterAWBEngine = (totalAirwayBillByGDNRef == 0) && (airwayBillTransaction != null) && (airwayBillTransaction.getStatus() != null);
		_log.debug(isOrderItemAfterAWBEngine);
		
		if(isOrderItemAfterAWBEngine){
			_log.debug("existing logistic provider " +airwayBillTransaction.getKodeLogistik());
			if(differentLogisticProvider(dailyReportOrderItem,activityReportData,airwayBillTransaction.getKodeLogistik()))
				return;
			
			processOrderItemAfterAWBEngine(existingVenOrderItem, dailyReportOrderItem, activityReportData, fileUploadLog, airwayBillTransaction);
		}else {
			processOrderItemBeforeAWBEngine(existingVenOrderItem, dailyReportOrderItem, activityReportData, fileUploadLog);
		}
		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void processOrderItemAfterAWBEngine(VenOrderItem existingVenOrderItem,
										       DailyReportMSG dailyReportOrderItem, 
										       ActivityReportData activityReportData, 
										       LogFileUploadLog fileUploadLog,
										       AirwayBillTransaction airwayBillTransaction){
		
		LogAirwayBill existingLogAirwayBill = logAirwayBillDAO.findByOrderItemId(existingVenOrderItem.getOrderItemId()).get(0);
		em.detach(existingLogAirwayBill);
		
		if(!composeLogAirwayBillAfterAWBEngine(existingLogAirwayBill, dailyReportOrderItem, activityReportData, fileUploadLog, airwayBillTransaction)){
			_log.warn("Problem updating LogAirwayBill, skip order item: " + existingVenOrderItem.getWcsOrderItemId());
			return;
		}

		if(!reconcileLogAirwayBillAfterAWBEngine(existingLogAirwayBill, existingVenOrderItem, dailyReportOrderItem, activityReportData, fileUploadLog, airwayBillTransaction)){
			_log.warn("Problem reconcile LogAirwayBill, skip order item: " + existingVenOrderItem.getWcsOrderItemId());
			return;
		}
		
		existingLogAirwayBill = logAirwayBillDAO.save(existingLogAirwayBill);
		
		VenOrderStatusConstants existingItemStatus = convertOrderStatusIdToVenOrderStatusConstants(existingVenOrderItem.getVenOrderStatus().getOrderStatusId());
		VenOrderStatusConstants newItemStatus  = getOrderStatusChange(dailyReportOrderItem);
		
		String errorMessage = updateOrderItemAndAirwayBillTransactionStatus(fileUploadLog.getUploadUsername(),
																			existingVenOrderItem,
															                airwayBillTransaction,
															                existingItemStatus,
															                newItemStatus,
															                airwayBillTransaction.getStatus(),
															                getAWBStatusChange(dailyReportOrderItem),
															                airwayBillTransaction.getLevel(),
															                airwayBillTransaction.getAirwayBillNo(),
															                dailyReportOrderItem.getAwb(), 
															                "MSG",
															                existingLogAirwayBill, 
															                activityReportData);
		 
		if(!errorMessage.isEmpty())
			 addFailedRecordCausedByErrorMessage(dailyReportOrderItem, activityReportData, errorMessage);
		
		 if (existingLogAirwayBill.getActivityResultStatus() != null && existingLogAirwayBill.getActivityResultStatus().equals("OK")) {
			 setAirwayBillStatusApproved(existingLogAirwayBill);
         }
		 
		 if(existingVenOrderItem.getCxMtaDate() != null){
			 saveCXFinanceHistory(existingLogAirwayBill);
		 }
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void processOrderItemBeforeAWBEngine(VenOrderItem existingVenOrderItem, 
												DailyReportMSG dailyReportOrderItem, 
												ActivityReportData activityReportData,
												LogFileUploadLog fileUploadLog){
		
		_log.debug("Order Item Before Airwaybill Engine : " + existingVenOrderItem.getWcsOrderItemId());
		
		LogAirwayBill existingLogAirwayBill = logAirwayBillDAO.findByOrderItemId(existingVenOrderItem.getOrderItemId()).get(0);
		em.detach(existingLogAirwayBill);
		
		if(!composeLogAirwayBill(existingLogAirwayBill, dailyReportOrderItem, activityReportData, fileUploadLog)){
			_log.warn("Problem updating LogAirwayBill, skip order item: " + existingVenOrderItem.getWcsOrderItemId());
			return;
		}

		if(!reconcileLogAirwayBill(existingLogAirwayBill, existingVenOrderItem, dailyReportOrderItem, activityReportData, fileUploadLog)){
			_log.warn("Problem reconcile LogAirwayBill, skip order item: " + existingVenOrderItem.getWcsOrderItemId());
			return;
		}
		
		logAirwayBillDAO.save(existingLogAirwayBill);
		
		VenOrderStatusConstants existingItemStatus = convertOrderStatusIdToVenOrderStatusConstants(existingVenOrderItem.getVenOrderStatus().getOrderStatusId());
		VenOrderStatusConstants newItemStatus  = getOrderStatusChange(dailyReportOrderItem);
		
		updateOrderItemStatusBeforeAirwayBillAutomation(existingVenOrderItem, existingItemStatus, newItemStatus);
	}
	
	public boolean composeLogAirwayBill(LogAirwayBill logAirwayBill, DailyReportMSG dailyReportOrderItem, ActivityReportData activityReportData, LogFileUploadLog fileUploadLog){
		VenOrderStatusConstants newOrderItemStatus = getOrderStatusChange(dailyReportOrderItem);

		switch(newOrderItemStatus){
			case VEN_ORDER_STATUS_D:

				logAirwayBill.setStatus(VeniceConstants.LOG_LOGISTICS_STATUS_CODE_POD);
				logAirwayBill.setRelation(getReceiverRelation(dailyReportOrderItem));
				logAirwayBill.setRecipient(getRecipient(dailyReportOrderItem));

                if (dailyReportOrderItem.getReceived() != null && !dailyReportOrderItem.getReceived().isEmpty()) {
                    try {
						logAirwayBill.setReceived(new Timestamp(formatDate.parse(dailyReportOrderItem.getReceived()).getTime()));
					} catch (Exception e) {
                        addFailedRecordCausedByReceivedDateException(dailyReportOrderItem, activityReportData, e);
                        return false;
                    }
                }
				break;
			case VEN_ORDER_STATUS_CX:
				logAirwayBill.setStatus(VeniceConstants.LOG_LOGISTICS_STATUS_CODE_MDE);
				break;
			default:
				break;
		}
		
        try {
            logAirwayBill.setAirwayBillPickupDateTime(SQLDateUtility.utilDateToSqlTimestamp(formatDate.parse(dailyReportOrderItem.getPuDate())));
            logAirwayBill.setActualPickupDate(SQLDateUtility.utilDateToSqlTimestamp(formatDate.parse(dailyReportOrderItem.getPuDate())));
        } catch (Exception e) {
           	addFailedRecordCausedByPickupTimeException(dailyReportOrderItem, activityReportData, e);
           	return false;
        }

        try {
            logAirwayBill.setNumPackages(new Integer(new Double(dailyReportOrderItem.getPieces()).intValue()));
        } catch (Exception e) {
            _log.warn("Problem getting Quantity from GDN Ref " + dailyReportOrderItem.getRefNo() + ", value : " + dailyReportOrderItem.getPieces(), e);
        }
        try {
            logAirwayBill.setPackageWeight(new BigDecimal(new Double(dailyReportOrderItem.getWeight()).doubleValue()));
        } catch (Exception e) {
            _log.warn("Problem getting Weight from GDN Ref " + dailyReportOrderItem.getRefNo() + ", value : " + dailyReportOrderItem.getWeight(), e);
        }

		logAirwayBill.setActivityFileNameAndLoc(fileUploadLog.getFileUploadNameAndLoc());
        logAirwayBill.setAirwayBillTimestamp(SQLDateUtility.utilDateToSqlTimestamp(new Date()));
        logAirwayBill.setTrackingNumber(dailyReportOrderItem.getTrNo());
        logAirwayBill.setDeliveryOrder(dailyReportOrderItem.getDoNumber());
        logAirwayBill.setConsignee(dailyReportOrderItem.getConsignee());
        logAirwayBill.setAddress(dailyReportOrderItem.getAddress());
        logAirwayBill.setDestCode(dailyReportOrderItem.getDestination());
        logAirwayBill.setDestination(dailyReportOrderItem.getDestination());
        logAirwayBill.setService(dailyReportOrderItem.getServices());
        logAirwayBill.setContent(dailyReportOrderItem.getContent());
        
        //if status already approved don't update awb number
        if (logAirwayBill.getLogApprovalStatus2().getApprovalStatusId() != VeniceConstants.VEN_LOGISTICS_APPROVAL_STATUS_APPROVED) {
            if (logAirwayBill.getAirwayBillNumber() == null || logAirwayBill.getAirwayBillNumber().isEmpty()) {
                logAirwayBill.setAirwayBillNumber(dailyReportOrderItem.getAwb());
            } else {
                if (!logAirwayBill.getAirwayBillNumber().equalsIgnoreCase(dailyReportOrderItem.getAwb())) {
                    logAirwayBill.setAirwayBillNumber(dailyReportOrderItem.getAwb());
                }
            }
        }
		return true;
	}
	
	public boolean reconcileLogAirwayBill(LogAirwayBill logAirwayBill, 
										  VenOrderItem venOrderItem,
										  DailyReportMSG dailyReportOrderItem, 
										  ActivityReportData activityReportData, 
										  LogFileUploadLog fileUploadLog){

		if (logAirwayBill != null && venOrderItem.getCxMtaDate()!=null) {
			_log.debug("mta data true");
			
			LogAirwayBill mtaLogAirwayBill = logAirwayBillDAO.findByOrderItemId(venOrderItem.getOrderItemId()).get(0);
			
			em.detach(mtaLogAirwayBill);
			
			if (logAirwayBill.getStatus() == null || 
				(!(venOrderItem.getVenOrderStatus().getOrderStatusId() == VenOrderStatusConstants.VEN_ORDER_STATUS_CX.code() 
				&& getOrderStatusChange(dailyReportOrderItem) == VenOrderStatusConstants.VEN_ORDER_STATUS_D))) {
				_log.debug("reconcile awb");
				try {
					logAirwayBill = logAirwayBillService.reconcileAirwayBill(logAirwayBill, mtaLogAirwayBill, false);
				} catch (Exception e) {
					addFailedRecordCausedByAWBReconciliationException(dailyReportOrderItem, activityReportData, e);
					return false;
				}
			}
		/**
		* No data from MTA
		*/
		} else {
			_log.debug("no data from mta");
			logAirwayBill.setActivityResultStatus(LogisticsConstants.RESULT_STATUS_NO_DATA_FROM_MTA);
			logAirwayBill.setMtaData(false);
		}
		
		return true;
	}
	
	public boolean reconcileLogAirwayBillAfterAWBEngine(LogAirwayBill logAirwayBill, 
			VenOrderItem venOrderItem,
			DailyReportMSG dailyReportOrderItem, 
			ActivityReportData activityReportData, 
			LogFileUploadLog fileUploadLog,
			AirwayBillTransaction airwayBillTransaction){

		if (logAirwayBill != null && venOrderItem.getCxMtaDate()!=null) {
			_log.debug("mta data true");
			
			LogAirwayBill mtaLogAirwayBill = logAirwayBillDAO.findByOrderItemId(venOrderItem.getOrderItemId()).get(0);
			
			em.detach(mtaLogAirwayBill);
			
			if ((!(airwayBillTransaction.getStatus().equals(AirwayBillTransaction.STATUS_SETTLED) && getAWBStatusChange(dailyReportOrderItem).equals(AirwayBillTransaction.STATUS_CLOSED)))) {
				_log.debug("reconcile awb");
				try {
					logAirwayBill = logAirwayBillService.reconcileAirwayBill(logAirwayBill, mtaLogAirwayBill, false);
				} catch (Exception e) {
					addFailedRecordCausedByAWBReconciliationException(dailyReportOrderItem, activityReportData, e);
					return false;
				}
			}
			/**
			* No data from MTA
			*/
		} else {
			_log.debug("no data from mta");
			logAirwayBill.setActivityResultStatus(LogisticsConstants.RESULT_STATUS_NO_DATA_FROM_MTA);
			logAirwayBill.setMtaData(false);
		}
		return true;
	}
	
	public boolean composeLogAirwayBillAfterAWBEngine(LogAirwayBill logAirwayBill, 
			   DailyReportMSG dailyReportOrderItem, 
			   ActivityReportData activityReportData, 
			   LogFileUploadLog fileUploadLog,
			   AirwayBillTransaction airwayBillTransaction){

		VenOrderStatusConstants newOrderItemStatus = getOrderStatusChange(dailyReportOrderItem);
		
		switch(newOrderItemStatus){
			case VEN_ORDER_STATUS_D:
				logAirwayBill.setStatus(VeniceConstants.LOG_LOGISTICS_STATUS_CODE_POD);
				logAirwayBill.setRecipient(getRecipient(dailyReportOrderItem));
		
				airwayBillTransaction.setRelation(getReceiverRelation(dailyReportOrderItem));
				airwayBillTransaction.setRecipient(getRecipient(dailyReportOrderItem));
		
				if (dailyReportOrderItem.getReceived() != null && !dailyReportOrderItem.getReceived().isEmpty()) {
					try {
						logAirwayBill.setReceived(new Timestamp(formatDate.parse(dailyReportOrderItem.getReceived()).getTime()));
						airwayBillTransaction.setReceived(new Timestamp(formatDate.parse(dailyReportOrderItem.getReceived()).getTime()));
					} catch (Exception e) {
						addFailedRecordCausedByReceivedDateException(dailyReportOrderItem, activityReportData, e);
						return false;
					}
				}
		
			break;
			case VEN_ORDER_STATUS_CX:
				logAirwayBill.setStatus(VeniceConstants.LOG_LOGISTICS_STATUS_CODE_MDE);
			break;
			default:
				break;
		}
		
		try {
			logAirwayBill.setAirwayBillPickupDateTime(SQLDateUtility.utilDateToSqlTimestamp(formatDate.parse(dailyReportOrderItem.getPuDate())));
			logAirwayBill.setActualPickupDate(SQLDateUtility.utilDateToSqlTimestamp(formatDate.parse(dailyReportOrderItem.getPuDate())));
		} catch (Exception e) {
			addFailedRecordCausedByPickupTimeException(dailyReportOrderItem, activityReportData, e);
			return false;
		}
		
		logAirwayBill.setActivityFileNameAndLoc(fileUploadLog.getFileUploadNameAndLoc());
		logAirwayBill.setAirwayBillTimestamp(SQLDateUtility.utilDateToSqlTimestamp(new Date()));
		logAirwayBill.setTrackingNumber(dailyReportOrderItem.getTrNo());
		logAirwayBill.setDeliveryOrder(dailyReportOrderItem.getDoNumber());
		logAirwayBill.setConsignee(dailyReportOrderItem.getConsignee());
		logAirwayBill.setAddress(dailyReportOrderItem.getAddress());
		logAirwayBill.setDestination(dailyReportOrderItem.getDestination());
		logAirwayBill.setService(dailyReportOrderItem.getServices());
		
		logAirwayBill.setDestCode(airwayBillTransaction.getKodeDestination());
		logAirwayBill.setContent(airwayBillTransaction.getNamaProduk());
		logAirwayBill.setGiftWrapCharge(airwayBillTransaction.getGiftWrap());
		logAirwayBill.setInsuranceCharge(airwayBillTransaction.getInsuranceCost());
		logAirwayBill.setInsuredAmount(airwayBillTransaction.getAirwaybillInsuranceCost());
		logAirwayBill.setNumPackages(airwayBillTransaction.getQtyProduk());
		logAirwayBill.setPackageWeight(new BigDecimal(airwayBillTransaction.getWeight()));
		logAirwayBill.setPricePerKg(airwayBillTransaction.getPricePerKg());
		logAirwayBill.setReceived(airwayBillTransaction.getReceived());
		logAirwayBill.setRecipient(airwayBillTransaction.getRecipient());
		logAirwayBill.setRelation(airwayBillTransaction.getRelation());
		logAirwayBill.setService(airwayBillTransaction.getKodeLogistik());
		logAirwayBill.setShipper(airwayBillTransaction.getNamaPengirim());
		
		//if status already approved don't update awb number
		if (logAirwayBill.getLogApprovalStatus2().getApprovalStatusId() != VeniceConstants.VEN_LOGISTICS_APPROVAL_STATUS_APPROVED) {
			if (logAirwayBill.getAirwayBillNumber() == null || logAirwayBill.getAirwayBillNumber().isEmpty()) {
				logAirwayBill.setAirwayBillNumber(airwayBillTransaction.getAirwayBillNo());
			} else {
				if (!logAirwayBill.getAirwayBillNumber().equalsIgnoreCase(dailyReportOrderItem.getAwb())) {
					logAirwayBill.setAirwayBillNumber(airwayBillTransaction.getAirwayBillNo());
				}
			}
		}
		
		return true;
	}
	
	public boolean differentLogisticProvider(DailyReportMSG dailyReportOrderItem, ActivityReportData activityReportData, String kodeLogisticPorvider){	
		if(!kodeLogisticPorvider.equalsIgnoreCase("MSG")){
			addFailedRecordCausedByDifferentProviderForGdnReffException(dailyReportOrderItem,activityReportData,kodeLogisticPorvider);
			return true;
		}else{
			return false;
		}
	};
	
	private String getReceiverRelation(DailyReportMSG dailyReportOrderItem){
		if ((dailyReportOrderItem.getConsignee().equalsIgnoreCase(dailyReportOrderItem.getRecipient())) || (dailyReportOrderItem.getConsignee().contains(dailyReportOrderItem.getRecipient()))) {
		    return "Yang bersangkutan";
		} else if (dailyReportOrderItem.getRecipient().contains("(") && dailyReportOrderItem.getRecipient().contains(")")) {
		    return dailyReportOrderItem.getRecipient().substring(dailyReportOrderItem.getRecipient().indexOf("(") + 1, dailyReportOrderItem.getRecipient().indexOf(")"));
        } else {
            return "Lain-lain";
        }
	}

	private String getRecipient(DailyReportMSG dailyReportOrderItem){
		if (dailyReportOrderItem.getRecipient().contains("(") && dailyReportOrderItem.getRecipient().contains(")")) {
            return dailyReportOrderItem.getRecipient().substring(0, dailyReportOrderItem.getRecipient().indexOf("("));
        }else{
        	return dailyReportOrderItem.getRecipient();
        }	
	}
	
	private boolean isOrderItemEligibleForStatusUpdate(DailyReportMSG dailyReportOrderItem, long existingOrderItemStatusId){
		if(isEligibleForStatusChange(existingOrderItemStatusId)){
			_log.debug("isEligibleForStatusChange");
			VenOrderStatusConstants existingOrderStatus = convertOrderStatusIdToVenOrderStatusConstants(existingOrderItemStatusId);
			
			if(existingOrderStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_CX 
					&& getOrderStatusChange(dailyReportOrderItem) == VenOrderStatusConstants.VEN_ORDER_STATUS_CX){
				_log.debug("Existing status already CX");
				return false;
			}else{
				return true;
			}
		}else{
			return false;
		}
	}
	
	private VenOrderStatusConstants getOrderStatusChange(DailyReportMSG dailyReportOrderItem){
		if (dailyReportOrderItem.getStatus().equalsIgnoreCase("OK")
             && (!dailyReportOrderItem.getReceived().isEmpty() && dailyReportOrderItem.getReceived() != null)
             && (!dailyReportOrderItem.getRecipient().isEmpty() && dailyReportOrderItem.getRecipient() != null)) {
			return VenOrderStatusConstants.VEN_ORDER_STATUS_D;
		}
		
		if (dailyReportOrderItem.getStatus().equalsIgnoreCase("OK") && (dailyReportOrderItem.getReceived().isEmpty() || dailyReportOrderItem.getRecipient().isEmpty())) {
			return VenOrderStatusConstants.VEN_ORDER_STATUS_CX;
		}
		
		return VenOrderStatusConstants.VEN_ORDER_STATUS_CX; 
	}
	
	private String getAWBStatusChange(DailyReportMSG dailyReportOrderItem){
		if (dailyReportOrderItem.getStatus().equalsIgnoreCase("OK")
             && (!dailyReportOrderItem.getReceived().isEmpty() && dailyReportOrderItem.getReceived() != null)
             && (!dailyReportOrderItem.getRecipient().isEmpty() && dailyReportOrderItem.getRecipient() != null)) {
			return AirwayBillTransaction.STATUS_CLOSED;
		}
		
		 if (dailyReportOrderItem.getStatus().equalsIgnoreCase("OK") && (dailyReportOrderItem.getReceived().isEmpty() || dailyReportOrderItem.getRecipient().isEmpty())) {
			 return AirwayBillTransaction.STATUS_SETTLED;
		 }
		
		 return AirwayBillTransaction.STATUS_SETTLED;
	}

	private void addFailedRecordCausedByAWBException(DailyReportMSG dailyReportMSG, ActivityReportData activityReportData, Exception e){
		_log.error("Problem contacting AWB Engine", e);
		e.printStackTrace();

		activityReportData.getFailedItemList()
			.put("No : " + dailyReportMSG.getTrNo().replace(".0", "") + ", GDN Ref : " + dailyReportMSG.getRefNo(), "System/connection problem");
	}

	private void addFailedRecordCausedByDifferentProviderForGdnReffException(DailyReportMSG dailyReportMSG, ActivityReportData activityReportData, String logisticProvider){

		activityReportData.getFailedProviderForGdnReff()
			.put("No : " + dailyReportMSG.getTrNo().replace(".0", "") + ", GDN Ref : " + dailyReportMSG.getRefNo(), logisticProvider);
	}	
	
	private void addFailedRecordCausedByReceivedDateException(DailyReportMSG dailyReportMSG, ActivityReportData activityReportData, Exception e){
		_log.error("Received Date Problem", e);
		activityReportData.getFailedItemList()
			.put("No : " + dailyReportMSG.getTrNo().replace(".0", "") + ", GDN Ref : " + dailyReportMSG.getRefNo(), "Ops please fix Received Date : " + dailyReportMSG.getReceived());
	}

	private void addFailedRecordCausedByPickupTimeException(DailyReportMSG dailyReportMSG, ActivityReportData activityReportData, Exception e){
		_log.error("CNoteDate problem", e);
        activityReportData.getFailedItemList()
        	.put("No : " + dailyReportMSG.getTrNo().replace(".0", "") + ", GDN Ref : " + dailyReportMSG.getRefNo(), "Ops please fix PuDate : " + dailyReportMSG.getPuDate());
            
	}
	
	private void addFailedRecordCausedByAWBReconciliationException(DailyReportMSG dailyReportMSG, ActivityReportData activityReportData, Exception e){
		_log.error("AWBReconciliation Problem", e);
		activityReportData.getFailedItemList()
			.put("No : " + dailyReportMSG.getTrNo().replace(".0", "") + ", GDN Ref : " + dailyReportMSG.getRefNo(), "Ops tell IT about this Reconciliation Problem : " + e.getMessage());
	}
	
	private void addFailedRecordCausedByErrorMessage(DailyReportMSG dailyReportMSG, ActivityReportData activityReportData, String errorMessage){
		_log.error("Problem " + errorMessage);
		activityReportData.getFailedItemList()
			.put("No : " + dailyReportMSG.getTrNo().replace(".0", "") + ", GDN Ref : " + dailyReportMSG.getRefNo(), errorMessage + " Ops please reupload.");
	}
	
	private LogLogisticsProvider getLogLogisticsProvider(){
		LogLogisticsProvider provider = new LogLogisticsProvider();
		provider.setLogisticsProviderId(4L);
		provider.setLogisticsProviderCode("MSG");
		
		return provider;
	}

}
