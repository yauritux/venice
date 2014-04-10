package com.gdn.venice.facade.logistics.activity.processor;

import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.SQLDateUtility;
import com.gdn.awb.exchange.model.AirwayBillTransactionResource;
import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.dao.FinSalesRecordDAO;
import com.gdn.venice.dao.LogActivityReportUploadDAO;
import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.dao.LogInvoiceAirwaybillRecordDAO;
import com.gdn.venice.dao.LogFileUploadLogDAO;
import com.gdn.venice.dao.VenOrderItemStatusHistoryDAO;
import com.gdn.venice.facade.LogActivityReconRecordSessionEJBRemote;
import com.gdn.venice.facade.LogAirwayBillSessionEJBRemote;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.facade.spring.FinSalesRecordService;
import com.gdn.venice.facade.spring.VenOrderItemService;
import com.gdn.venice.logistics.dataexport.ActivityInvoiceFailedToUploadExport;
import com.gdn.venice.logistics.dataexport.FailedStatusUpdate;
import com.gdn.venice.logistics.dataimport.LogisticsConstants;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;
import com.gdn.venice.logistics.integration.bean.AirwayBillTransaction;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.LogActivityReconRecord;
import com.gdn.venice.persistence.LogActivityReportUpload;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.LogApprovalStatus;
import com.gdn.venice.persistence.LogFileUploadLog;
import com.gdn.venice.persistence.LogInvoiceAirwaybillRecord;
import com.gdn.venice.persistence.LogLogisticsProvider;
import com.gdn.venice.persistence.LogReconActivityRecordResult;
import com.gdn.venice.persistence.LogReportStatus;
import com.gdn.venice.persistence.LogReportTemplate;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemStatusHistory;
import com.gdn.venice.persistence.VenOrderItemStatusHistoryPK;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author Dito H Subandono
 * 
 * NOTE : Only extend this class with a Spring Service class
 */
public abstract class ActivityReportProcessor {
	public static final SimpleDateFormat formatDate = new SimpleDateFormat(LogisticsConstants.DATE_FORMAT_STRING);
	public static final SimpleDateFormat fileDateTimeFormat = new SimpleDateFormat(LogisticsConstants.FILE_DATE_TIME_FORMAT);
	
	@Autowired
	LogFileUploadLogDAO logFileUploadLogDAO;
	@Autowired
	LogActivityReportUploadDAO logActivityReportUploadDAO;
	@Autowired
	VenOrderItemStatusHistoryDAO venOrderItemStatusHistoryDAO;
	@Autowired
	FinSalesRecordDAO finSalesRecordDAO;
	@Autowired
	LogAirwayBillDAO logAirwayBillDAO;
	@Autowired
	FinSalesRecordService finSalesRecordService;
	@Autowired
	VenOrderItemService venOrderItemService;
	
	AirwayBillEngineConnector awbConn;
	
	private String activityReportFailFilePath;
	
	public abstract Logger getLogger();
	
	public abstract void process(LogFileUploadLog fileUploadLog);
	
	public void setActivityReportFailFilePath(String path){
		activityReportFailFilePath = path;
	}
	
	public boolean isEligibleForStatusChange(long existingOrderItemStatusId){
		
		VenOrderStatusConstants existingStatus = convertOrderStatusIdToVenOrderStatusConstants(existingOrderItemStatusId);
		
		switch (existingStatus) {
			case VEN_ORDER_STATUS_PU:
				return true;
			case VEN_ORDER_STATUS_PP:
				return true;
			case VEN_ORDER_STATUS_ES:
				return true;
			case VEN_ORDER_STATUS_CX:
				return true;
			case VEN_ORDER_STATUS_D:
				return true;
			default:
				getLogger().debug("status not PU, PP, ES, CX or D");
				return false;
		}
	}
	
	public VenOrderStatusConstants convertOrderStatusIdToVenOrderStatusConstants(long existingOrderItemStatusId){
		return VenOrderStatusConstants.values()[(int)existingOrderItemStatusId];
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void updateOrderItemStatusBeforeAirwayBillAutomation(
			VenOrderItem venOrderItem,
            VenOrderStatusConstants existingOrderItemStatus,
            VenOrderStatusConstants newOrderItemStatus){

        getLogger().debug("Order Item : " + venOrderItem.getWcsOrderItemId());
        getLogger().debug("Existing Order Item Status : " + existingOrderItemStatus.name());
        getLogger().debug("New Order Item Status : " + newOrderItemStatus.name());
        
        switch (existingOrderItemStatus) {
			case VEN_ORDER_STATUS_PU:
	            if (newOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_CX) {
	                updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_ES);
	                updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
	            }
				break;
			case VEN_ORDER_STATUS_ES:
	            if (newOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_CX) {
	                updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
	            }
	            if (newOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_D) {
	                updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
	                updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
	            }
				break;	
			case VEN_ORDER_STATUS_CX:
	            if (newOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_D) {
	                updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
	            }
				break;	
			default:
				break;
		}
    }
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Boolean updateOrderItemStatus(VenOrderItem venOrderItem, VenOrderStatusConstants newOrderItemStatus) {
        VenOrderStatus venOrderStatus = new VenOrderStatus();
        venOrderStatus.setOrderStatusId(newOrderItemStatus.code());
        venOrderItem.setVenOrderStatus(venOrderStatus);
        
        if (newOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_CX) {
            if (venOrderItem.getSalesBatchStatus() == null
                    || venOrderItem.getSalesBatchStatus().isEmpty()) {
                venOrderItem.setSalesBatchStatus(VeniceConstants.FIN_SALES_BATCH_STATUS_READY);
            }

            Timestamp cxDate = new Timestamp(System.currentTimeMillis());
            venOrderItem.setCxDate(cxDate);
        }

        venOrderItemService.mergeVenOrderItem(venOrderItem);
        
        return true;
    }
	
	@Transactional(propagation=Propagation.REQUIRED)
	public String updateOrderItemAndAirwayBillTransactionStatus(String uploadUsername,
													            VenOrderItem venOrderItem,
													            AirwayBillTransaction airwayBillTransaction,
													            VenOrderStatusConstants existingOrderItemStatus,
													            VenOrderStatusConstants newOrderItemStatus,
													            String existingAirwayBillTransactionStatus,
													            String newAirwayBillTransactionStatus,
													            String airwayBillTransactionLevel,
													            String airwayBillNoFromEngine,
													            String airwayBillNoFromLogistic,
													            String logProviderCode,
													            LogAirwayBill logAirwayBill, 
													            ActivityReportData activityReportData) {

        boolean isOverrideSuccess = true;
        String errorMessage = "";
        if (airwayBillNoFromEngine.equals(airwayBillNoFromLogistic)) {
            getLogger().debug("Airway Bill Engine & Logistic are matched");
            try {
                errorMessage = updateOrderItemAndAirwayBillTransactionStatus(uploadUsername, venOrderItem, airwayBillTransaction, existingOrderItemStatus , newOrderItemStatus, existingAirwayBillTransactionStatus, newAirwayBillTransactionStatus, true);
            } catch (Exception e) {
                getLogger().error("Problem updateOrderItemAndAirwayBillTransactionStatus ", e);
                errorMessage = "Unknown error.";
            }
            
        /**
         * Airway bill number from engine & logistics are different
         */
        } else {
            getLogger().debug("Airway Bill Engine & Logistic are NOT matched");
            getLogger().debug("Airway Bill Engine : " + airwayBillNoFromEngine);
            getLogger().debug("Airway Bill Logistics : " + airwayBillNoFromLogistic);
            
            LogAirwayBill logAirwayBillGetStatus = new LogAirwayBill();
            
            try {
            	logAirwayBillGetStatus = logAirwayBillDAO.findByAirwayBillId(logAirwayBill.getAirwayBillId());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            
            if (logAirwayBillGetStatus.getLogInvoiceAirwaybillRecord().getInvoiceResultStatus().equals(VeniceConstants.LOG_AIRWAYBILL_ACTIVITY_RESULT_OK)) {

                getLogger().debug("Airway Bill from engine " + airwayBillNoFromEngine + " activity status is OK, not allowed to override");

                isOverrideSuccess = false;
            } else {
                isOverrideSuccess = overrideAirwayBillNumber(airwayBillTransaction.getGdnRef(), airwayBillNoFromLogistic, uploadUsername, logProviderCode);
                getLogger().debug("Airway Bill override result from engine " + isOverrideSuccess);
            }

            airwayBillTransaction.setAirwayBillNo(airwayBillNoFromLogistic);

            try {
                errorMessage = updateOrderItemAndAirwayBillTransactionStatus(uploadUsername, venOrderItem, airwayBillTransaction, existingOrderItemStatus , newOrderItemStatus, existingAirwayBillTransactionStatus, newAirwayBillTransactionStatus, isOverrideSuccess);
            } catch (Exception e) {
                getLogger().error("Problem updateOrderItemAndAirwayBillTransactionStatus ", e);
                errorMessage = "Unknown error, Ops please reupload. If problem persists, please kindly contact IT team.";
            }
        }

        if (!isOverrideSuccess) {
            FailedStatusUpdate failedStatusUpdate = new FailedStatusUpdate();

            failedStatusUpdate.setGdnRef(airwayBillTransaction.getGdnRef());
            failedStatusUpdate.setAirwayBillNoLogistic(airwayBillNoFromLogistic);
            failedStatusUpdate.setAirwayBillNoMTA(airwayBillNoFromEngine);

            if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_CX) {
                failedStatusUpdate.setOrderItemStatus("CX");
            }

            if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_D) {
                failedStatusUpdate.setOrderItemStatus("D");
            }

            if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_ES) {
                failedStatusUpdate.setOrderItemStatus("ES");
            }

            if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_PU) {
                failedStatusUpdate.setOrderItemStatus("PU");
            }

            failedStatusUpdate.setAirwayBillStatus(existingAirwayBillTransactionStatus);

            try {
                StringBuilder items = new StringBuilder();
                List<AirwayBillTransactionResource> awbOld = awbConn.getAirwayBillTransaction(airwayBillNoFromLogistic, true);
                for (AirwayBillTransactionResource airwayBillTransactionResource : awbOld) {
                    for (int i = 0; i < airwayBillTransactionResource.getItems().length; i++) {
                        if (!items.toString().isEmpty()) {
                            items.append(";");
                        }
                        items.append(airwayBillTransactionResource.getItems()[i].getGdnRefNo());
                    }
                }

                failedStatusUpdate.setUsedOnItems(items.toString());
            } catch (Exception e) {
                getLogger().info("Failed getting items with logistic's airwaybill number");
            }
            activityReportData.getFailedStatusUpdateList().add(failedStatusUpdate);

            createProblemExistAwbNumber(airwayBillNoFromLogistic, airwayBillNoFromEngine, logAirwayBill, activityReportData.getActivityReportUpload());
        }
        return errorMessage;
    }
	
	public boolean isAirwayBillLevelMain(AirwayBillTransaction airwayBillTransaction){
		return (airwayBillTransaction.getLevel().equals(AirwayBillTransaction.LEVEL_ORDER_MAIN)
                      || airwayBillTransaction.getLevel().equals(AirwayBillTransaction.LEVEL_RETUR_MAIN));
	}
	
	public String updateOrderItemAndAirwayBillTransactionStatus(String uploadUsername, 
												               	VenOrderItem venOrderItem, 
												               	AirwayBillTransaction airwayBillTransaction, 
												               	VenOrderStatusConstants existingOrderItemStatus, 
												               	VenOrderStatusConstants newOrderItemStatus, 
												               	String existingAirwayBillTransactionStatus, 
												               	String newAirwayBillTransactionStatus, 
												               	boolean isOverrideSuccess) throws Exception {

        getLogger().debug("Order Item : " + venOrderItem.getWcsOrderItemId());
        getLogger().debug("Existing Order Item Status : " + existingOrderItemStatus.name());
        getLogger().debug("New Order Item Status : " + newOrderItemStatus.name());
        getLogger().debug("Airway Bill Number : " + airwayBillTransaction.getAirwayBillNo());
        getLogger().debug("Existing Airway Bill Transaction Status : " + existingAirwayBillTransactionStatus);
        getLogger().debug("New Airway Bill Transaction Status : " + newAirwayBillTransactionStatus);
        getLogger().debug("Airway Bill Transaction Level : " + airwayBillTransaction.getLevel());

        String errorMessage = "";
        boolean isUpdateAwbSuccess = true;
        
        try{
	        if (existingAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_PICK_UP)) {
	            /**
	             * PU -> CX : PU -> ES -> CX
	             */
	            if (newAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_SETTLED)) {
	            	
                    isUpdateAwbSuccess = awbConn.updateAirwayBillToES(airwayBillTransaction.getAirwayBillNo(), airwayBillTransaction.getKodeLogistik(), uploadUsername);
                    getLogger().info("Update AWB " + airwayBillTransaction.getAirwayBillNo() + " to ES successful");
                    
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_ES);
                    }

                    if (isOverrideSuccess) {
                        isUpdateAwbSuccess = awbConn.updateAirwayBillToCX(airwayBillTransaction.getAirwayBillNo(), airwayBillTransaction.getKodeLogistik(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    } else {
                        isUpdateAwbSuccess = awbConn.forceUpdateAirwayBillToCX(airwayBillTransaction.getGdnRef(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    }
                    
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
                    }
                    
                    getLogger().info("Update AWB " + airwayBillTransaction.getAirwayBillNo() + " to CX successful");
	            }
	        }
	        /**
	         * Airway bill is ES
	         */
	        if (existingAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_EMAIL_SENT)) {
	            /**
	             * ES -> CX
	             */
	            if (newAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_SETTLED)) {
	            	
                    if (isOverrideSuccess) {
                        isUpdateAwbSuccess = awbConn.updateAirwayBillToCX(airwayBillTransaction.getAirwayBillNo(), airwayBillTransaction.getKodeLogistik(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    } else {
                        isUpdateAwbSuccess = awbConn.forceUpdateAirwayBillToCX(airwayBillTransaction.getGdnRef(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    }
                    
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
                    }
                    
                    getLogger().info("Update AWB " + airwayBillTransaction.getAirwayBillNo() + " to CX successful");
	            }
	            /**
	             * ES -> D will put CX in the middle : ES -> CX -> D
	             */
	            if (newAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_CLOSED)) {
	            	
                    if (isOverrideSuccess) {
                        isUpdateAwbSuccess = awbConn.updateAirwayBillToCX(airwayBillTransaction.getAirwayBillNo(), airwayBillTransaction.getKodeLogistik(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    } else {
                        isUpdateAwbSuccess = awbConn.forceUpdateAirwayBillToCX(airwayBillTransaction.getGdnRef(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    }
                    
                    getLogger().info("Update AWB " + airwayBillTransaction.getAirwayBillNo() + " to CX successful");
                    
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
                    }

                    if (isOverrideSuccess) {
                        isUpdateAwbSuccess = awbConn.updateAirwayBillToD(airwayBillTransaction.getAirwayBillNo(), airwayBillTransaction.getKodeLogistik(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    } else {
                        isUpdateAwbSuccess = awbConn.forceUpdateAirwayBillToD(airwayBillTransaction.getGdnRef(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    }
                    getLogger().info("Update AWB " + airwayBillTransaction.getAirwayBillNo() + " to D successful");
                    
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
                    }
                    
	            }
	        }
	
	        /**
	         * Airway bill is CX
	         */
	        if (existingAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_SETTLED)) {
	            /**
	             * CX -> D
	             */
	            if (newAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_CLOSED)) {
	            	
                    if (isOverrideSuccess) {
                        isUpdateAwbSuccess = awbConn.updateAirwayBillToD(airwayBillTransaction.getAirwayBillNo(), airwayBillTransaction.getKodeLogistik(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    } else {
                        isUpdateAwbSuccess = awbConn.forceUpdateAirwayBillToD(airwayBillTransaction.getGdnRef(), uploadUsername, airwayBillTransaction.getTanggalActualPickup(), airwayBillTransaction.getRecipient(), airwayBillTransaction.getRelation(), airwayBillTransaction.getReceived());
                    }
                    getLogger().info("Update AWB " + airwayBillTransaction.getAirwayBillNo() + " to D successful");
                    
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
                    }
	            }
	
                /**
                 * Airway bill is CX but order item is ES, update order item to CX
                 */
                if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_ES) {
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
                    }
                }

                /**
                 * Airway bill is CX but order item is PU, update order item to ES then CX
                 */
                if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_PU) {
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_ES);
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
                    }
                }
	        }
	
	        /**
	         * Airway bill is D
	         */
	        if (existingAirwayBillTransactionStatus.equals(AirwayBillTransaction.STATUS_CLOSED)) {
                /**
                 * Airway bill is D but order item is CX, update order item to D
                 */
                if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_CX) {
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
                    }
                }
                /**
                 * Airway bill is D but order item is ES, update order item to
                 * CX then D
                 */
                if (existingOrderItemStatus == VenOrderStatusConstants.VEN_ORDER_STATUS_ES) {
                    if (isAirwayBillLevelMain(airwayBillTransaction)) {
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
                        updateOrderItemStatus(venOrderItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
                    }
                }
	        }
	
	        if (!isUpdateAwbSuccess) {
	            errorMessage = "Failed update awb engine status.";
	        }

        } catch (Exception e) {
            getLogger().error("Failed update status for: " + airwayBillTransaction.getGdnRef(), e);
            errorMessage = "Failed update venice status.";
        }    
	        
        return errorMessage;
    }
	
	public void createProblemExistAwbNumber(String logisticAWB, 
											String mtaAwb,
            							    LogAirwayBill awb, 
            							    LogActivityReportUpload report) {
        Locator<Object> locator = null;
        LogActivityReconRecordSessionEJBRemote activityReconRecordHome;
        LogAirwayBillSessionEJBRemote awbHome;
        try {
            getLogger().debug("Cannot override awb number: " + mtaAwb + " to "
                    + logisticAWB + "create problem exist");
            locator = new Locator<Object>();
            activityReconRecordHome = (LogActivityReconRecordSessionEJBRemote) locator
                    .lookup(LogActivityReconRecordSessionEJBRemote.class, "LogActivityReconRecordSessionEJBBean");
            awbHome = (LogAirwayBillSessionEJBRemote) locator
                    .lookup(LogAirwayBillSessionEJBRemote.class, "LogAirwayBillSessionEJBBean");

            LogActivityReconRecord logActivityReconRecord = new LogActivityReconRecord();

            logActivityReconRecord.setLogAirwayBill(awb);

            logActivityReconRecord.setLogActivityReportUpload(report);

            logActivityReconRecord.setComment("Venice identified an airway bill no mismatch");

            LogReconActivityRecordResult logReconActivityRecordResult = new LogReconActivityRecordResult();
            logReconActivityRecordResult.setReconRecordResultId(VeniceConstants.VEN_ACTIVITY_RECON_RESULT_2);
            logActivityReconRecord.setLogReconActivityRecordResult(logReconActivityRecordResult);

            logActivityReconRecord.setProviderData(logisticAWB);
            logActivityReconRecord.setVeniceData(mtaAwb);

            // Persist the record
            activityReconRecordHome.persistLogActivityReconRecord(logActivityReconRecord);

            awb.setActivityResultStatus("Problem Exists");
            awb = awbHome.mergeLogAirwayBill(awb);
        } catch (Exception e) {
            getLogger().error("Failed create problem exist recon for airwaybill number mismatch");
        } finally {
            try {
                locator.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	public Boolean overrideAirwayBillNumber(String gdnRefNo, String newAirwayBillNo, String uploadUsername, String logProviderCode) {
        getLogger().debug("GDN Ref No : " + gdnRefNo);
        getLogger().debug("New Airway Bill No : " + newAirwayBillNo);

        return awbConn.overrideAirwayBillNumber(gdnRefNo, newAirwayBillNo, uploadUsername, logProviderCode);
    }
	
	public void saveCXFinanceHistory(LogAirwayBill logAirwayBill) {    
    	getLogger().debug("saveCXFinanceHistory");

        try {

            Timestamp cxFinanceDate = new Timestamp(System.currentTimeMillis());

            FinSalesRecord finSalesRecord = finSalesRecordDAO.findWithVenOrderItemByOrderItemId(logAirwayBill.getVenOrderItem().getOrderItemId());
            
            if (finSalesRecord != null) {
                if (finSalesRecord.getCxFinanceDate() == null) {
                    getLogger().debug("sales record found, add CX Finance date in sales record");
                    finSalesRecord.setCxFinanceDate(logAirwayBill.getVenOrderItem().getCxDate().after(logAirwayBill.getVenOrderItem().getCxMtaDate())
                            ? logAirwayBill.getVenOrderItem().getCxDate() : logAirwayBill.getVenOrderItem().getCxMtaDate());
                    finSalesRecordService.mergeFinSalesRecord(finSalesRecord);
                    
                    List<VenOrderItemStatusHistory> histories 
	                	= venOrderItemStatusHistoryDAO
	                			.findCXFinWithVenOrderItemAndVenOrderItemStatusByOrderItemId(
	                					logAirwayBill.getVenOrderItem().getOrderItemId());
	                
	                if (histories != null || histories.isEmpty()) {
	                    VenOrderItemStatusHistoryPK venOrderItemStatusHistoryPK = new VenOrderItemStatusHistoryPK();
	
	                    venOrderItemStatusHistoryPK.setOrderItemId(logAirwayBill.getVenOrderItem().getOrderItemId());
	
	                    venOrderItemStatusHistoryPK.setHistoryTimestamp(cxFinanceDate);
	                    VenOrderItemStatusHistory orderItemStatusHistory = new VenOrderItemStatusHistory();
	
	                    orderItemStatusHistory.setId(venOrderItemStatusHistoryPK);
	
	                    orderItemStatusHistory.setStatusChangeReason("Updated by System (CX Finance)");
	
	                    VenOrderStatus statusCX = new VenOrderStatus();
	
	                    statusCX.setOrderStatusId(VeniceConstants.VEN_ORDER_STATUS_CX);
	
	                    statusCX.setOrderStatusCode("CX");
	                    orderItemStatusHistory.setVenOrderStatus(statusCX);
	                    venOrderItemStatusHistoryDAO.save(orderItemStatusHistory);
	                }
                }
            }
            
        } catch (Exception e) {
            getLogger().error("Failed on saving CX Finance History or updating CX Finance on sales record");
        }
    }
	
	public void generateUploadFailData(ActivityReportData activityReportData, LogFileUploadLog fileUploadLog) throws Exception{
		if(isFailReportAvailable(activityReportData)){
			
			if(activityReportFailFilePath == null)
				activityReportFailFilePath = System.getenv("VENICE_HOME") + LogisticsConstants.ACTIVITY_REPORT_FOLDER;
			
			String outputFileName = "ActivityReportFailedToUpload-" + fileDateTimeFormat.format(new Date()) + ".xls";
			FileOutputStream fos = new FileOutputStream(activityReportFailFilePath + outputFileName);

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("ActivityReportFailedToUpload");

            ActivityInvoiceFailedToUploadExport activityInvoiceFailedToUploadExport = new ActivityInvoiceFailedToUploadExport(wb);
            wb = activityInvoiceFailedToUploadExport.ExportExcel(activityReportData.getGdnRefNotFoundList(), activityReportData.getFailedItemList(), activityReportData.getFailedStatusUpdateList(), activityReportData.getFailedProviderForGdnReff(),sheet, "activity");
            wb.write(fos);
            
            getLogger().debug("done export excel");

            fileUploadLog.setFailedFileUploadName(outputFileName);
            fileUploadLog.setFailedFileUploadNameAndLoc(activityReportFailFilePath + outputFileName);
            fileUploadLog.setUploadStatus(LogisticsConstants.UPLOAD_STATUS_SUCCESS);
		}
	}
	
	public void updateUploadStatus(LogFileUploadLog fileUploadLog){
		logFileUploadLogDAO.save(fileUploadLog);
	}
	
	public boolean isFailReportAvailable(ActivityReportData activityReportData){
		if (activityReportData.getGdnRefNotFoundList().size() > 0 || activityReportData.getFailedItemList().size() > 0 || activityReportData.getFailedStatusUpdateList().size() > 0) {
            getLogger().info(activityReportData.getGdnRefNotFoundList().size() + " row(s) was not uploaded");
            getLogger().info(activityReportData.getFailedItemList().size() + " row(s) has problem when being processed");
            getLogger().info(activityReportData.getFailedStatusUpdateList().size() + " row(s) has fail status update");
          
            return true;
		}else{
			return false;
		}
	}
	
	public LogActivityReportUpload createLogActivityReportUpload(LogFileUploadLog fileUploadLog, long totalRecords, LogLogisticsProvider logisticsProvider){
		LogActivityReportUpload logActivityReportUpload = new LogActivityReportUpload();
		 logActivityReportUpload.setFileNameAndLocation(fileUploadLog.getFileUploadNameAndLoc());
         logActivityReportUpload.setLogLogisticsProvider(logisticsProvider);

         LogReportStatus logReportStatus = new LogReportStatus();
         logReportStatus.setReportStatusId(new Long(0));
         logActivityReportUpload.setLogReportStatus(logReportStatus);
         LogReportTemplate logReportTemplate = new LogReportTemplate();
         logReportTemplate.setTemplateId(new Long(6)); //TODO harus set sesuai dgn template id provider yang bersangkutan

         logActivityReportUpload.setLogReportTemplate(logReportTemplate);
         logActivityReportUpload.setNumberOfRecords(totalRecords);
         logActivityReportUpload.setReportDesc(fileUploadLog.getFileUploadName() + ": Logistics activity report");
         logActivityReportUpload.setReportTimestamp(SQLDateUtility.utilDateToSqlTimestamp(new Date()));
         
         logActivityReportUpload = logActivityReportUploadDAO.save(logActivityReportUpload);
         
		return logActivityReportUpload;
	}
	
	public void setAirwayBillStatusApproved(LogAirwayBill logAirwayBill){
	   getLogger().debug("activity result status OK, set approval status to approved");
	   
	   LogApprovalStatus activityStatusApproved = new LogApprovalStatus();
       activityStatusApproved.setApprovalStatusId(VeniceConstants.LOG_APPROVAL_STATUS_APPROVED);
       logAirwayBill.setLogApprovalStatus2(activityStatusApproved);
       logAirwayBill.setActivityApprovedByUserId("System");
       logAirwayBillDAO.save(logAirwayBill);
	}
	
}
