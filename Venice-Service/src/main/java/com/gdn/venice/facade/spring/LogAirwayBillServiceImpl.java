package com.gdn.venice.facade.spring;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.constants.LogActionAppliedConstants;
import com.gdn.venice.constants.LogActivityReconRecordConstants;
import com.gdn.venice.constants.LogReconActivityRecordResultConstants;
import com.gdn.venice.dao.LogActivityReconRecordDAO;
import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.persistence.LogActionApplied;
import com.gdn.venice.persistence.LogActivityReconRecord;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.LogApprovalStatus;
import com.gdn.venice.persistence.LogReconActivityRecordResult;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;


@Service
public class LogAirwayBillServiceImpl implements LogAirwayBillService{
	protected static Logger _log = Log4jLoggerFactory.getLogger("com.gdn.venice.facade.spring.LogAirwayBillServiceImpl");
	
	private SimpleDateFormat sdf = new SimpleDateFormat("d-MM-yyyy");
	
	@Autowired
	LogAirwayBillDAO logAirwayBillDAO;
	@Autowired
	LogActivityReconRecordDAO logActivityReconRecordDAO;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addDummyLogAirwayBillForNewlyFPOrderItem(VenOrderItem orderItem) {
		List<LogAirwayBill> logAirwayBillList = logAirwayBillDAO.findByOrderItemId(orderItem.getOrderItemId());
		if(logAirwayBillList.size() > 0){
			logAirwayBillDAO.delete(logAirwayBillList);
		}
		
		LogAirwayBill newLogAirwayBill = new LogAirwayBill();
		
		LogApprovalStatus logApprovalStatus = new LogApprovalStatus();
        logApprovalStatus.setApprovalStatusId(VeniceConstants.VEN_LOGISTICS_APPROVAL_STATUS_NEW);
		
		newLogAirwayBill.setVenOrderItem(orderItem);
		newLogAirwayBill.setLogApprovalStatus1(logApprovalStatus);
		newLogAirwayBill.setLogApprovalStatus2(logApprovalStatus);
		newLogAirwayBill.setMtaData(false);
		
		logAirwayBillDAO.save(newLogAirwayBill);
	}
	
	@Override
	public LogAirwayBill reconcileAirwayBill(LogAirwayBill providerAirwayBill, LogAirwayBill mtaAirwayBill, boolean isMcx){
		_log.debug("start performActivityReconciliation");
		
		List<LogActivityReconRecord> logActivityReconRecordList = getActivityReconRecord(providerAirwayBill.getAirwayBillId(), isMcx);
		
		deleteExistingActivityReconRecord(logActivityReconRecordList);
		
		int reconProblem = 0;
		
		reconcileActualPickupDate(providerAirwayBill.getAirwayBillId(), 
								  providerAirwayBill.getActualPickupDate(), 
								  mtaAirwayBill.getActualPickupDate());
		
		boolean reconcileServiceResult = reconcileService(providerAirwayBill.getAirwayBillId(), 
														  getLogisticsProviderCode(providerAirwayBill), 
												          providerAirwayBill.getService(), 
												          getLogisticsProviderCode(mtaAirwayBill));
		
		if(!reconcileServiceResult){
			reconProblem++;
		}	
		
		boolean reconcileRecipientResult = reconcileRecipient(providerAirwayBill.getAirwayBillId(), 
															  providerAirwayBill.getConsignee() != null ? providerAirwayBill.getConsignee().trim() : "", 
															  mtaAirwayBill.getVenOrderItem().getVenRecipient().getVenParty().getFullOrLegalName().trim());
		
		if(!reconcileRecipientResult){
			reconProblem++;
		}
			
		_log.debug("Total Recon Problem : " + reconProblem);
		
		providerAirwayBill.setActivityResultStatus(getActivityResultStatus(reconProblem));
		
		return providerAirwayBill;
	}

	private void deleteExistingActivityReconRecord(List<LogActivityReconRecord> logActivityReconRecordList) {
		if (logActivityReconRecordList != null || logActivityReconRecordList.size() > 0) {
			logActivityReconRecordDAO.delete(logActivityReconRecordList);
		}
	}
	
	public List<LogActivityReconRecord> getActivityReconRecord(Long airwayBillId, boolean isMcx){
		LogAirwayBill logAirwayBill = new LogAirwayBill();
		logAirwayBill.setAirwayBillId(airwayBillId);
		
		if (isMcx) {
			return logActivityReconRecordDAO.findByLogAirwayBillAndReconIsNotSettlementMismatch(logAirwayBill);
		}else{
			return logActivityReconRecordDAO.findByLogAirwayBill(logAirwayBill);
		}
	}
	
	public String getLogisticsProviderCode(LogAirwayBill logAirwayBill){
		return logAirwayBill.getLogLogisticsProvider().getLogisticsProviderCode();
	}
	
	private String getActivityResultStatus(int reconProblem){
		if(reconProblem == 0)
			return "OK";
		else{
			return "Problem Exists";
		}
	}
	
	@Override
	public boolean reconcileActualPickupDate(Long logAirwayBillId, Date actualPickupDateFromLogistic, Date actualPickupDateFromMTA){
		
		if(isActualPickupDateMatched(actualPickupDateFromLogistic, actualPickupDateFromMTA)){
			return true;
		}else{
			_log.debug("Pickup date mismatch found for LogAirwayBill id :" + logAirwayBillId);
			_log.debug("Logistic Actual Pickup Date : " + ((actualPickupDateFromLogistic != null)?actualPickupDateFromLogistic.toString():""));
			_log.debug("MTA Actual Pickup Date : " + ((actualPickupDateFromMTA != null)?actualPickupDateFromMTA.toString():""));
			
			LogActivityReconRecord logActivityReconRecord = setAppliedActivityRecon(logAirwayBillId, 
																					LogActionAppliedConstants.PROVIDER_DATA_APPLIED, 
																					LogReconActivityRecordResultConstants.PICKUP_DATE_MISMATCH, 
																					actualPickupDateFromLogistic != null?sdf.format(actualPickupDateFromLogistic):"", 
																					actualPickupDateFromMTA != null?sdf.format(actualPickupDateFromMTA):"", 
			 																		LogActivityReconRecordConstants.COMMENT_PICKUP_DATE_MISMATCH.toString());
			
			logActivityReconRecordDAO.save(logActivityReconRecord);
			
			return false;
		}
	}
	
	private boolean isActualPickupDateMatched(Date actualPickupDateFromLogistic, Date actualPickupDateFromMTA){
		if(actualPickupDateFromLogistic == null && actualPickupDateFromMTA != null)
			return false;
		
		if(actualPickupDateFromLogistic != null && actualPickupDateFromMTA == null)
			return false;
		
		if(actualPickupDateFromLogistic.compareTo(actualPickupDateFromMTA) == 0)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean reconcileService(Long logAirwayBillId, String providerCodeFromLogistic, String serviceFromLogistic, String serviceFromMTA){
		
		String mappedProviderCodeFromLogistic = getMappedProviderCode(providerCodeFromLogistic, serviceFromLogistic);
		String mappedProviderCodeFromMTA = getMappedProviderCode(serviceFromMTA);
		
		if(isServiceMatched(mappedProviderCodeFromLogistic, mappedProviderCodeFromMTA)){
			return true;
		}else{
			_log.debug("Service mismatch found for LogAirwayBill id : " + logAirwayBillId);
			_log.debug("Logistic Service : " + mappedProviderCodeFromLogistic);
			_log.debug("MTA Service : " + mappedProviderCodeFromMTA);
			
			LogActivityReconRecord logActivityReconRecord = setActivityRecon(logAirwayBillId, 
																			 LogReconActivityRecordResultConstants.SERVICE_MISMATCH, 
																			 mappedProviderCodeFromLogistic, 
																			 mappedProviderCodeFromMTA, 
																			 LogActivityReconRecordConstants.COMMENT_SERVICE_MISMATCH.toString());	
			
			logActivityReconRecordDAO.save(logActivityReconRecord);
			
			return false;
		}
		
	}
	
	private boolean isServiceMatched(String serviceFromLogistic, String serviceFromMTA){
		
		if(serviceFromLogistic == null && serviceFromMTA != null)
			return false;
		
		if(serviceFromLogistic != null && serviceFromMTA == null)
			return false;
		
		if(serviceFromLogistic.trim().equals(serviceFromMTA.trim())){
			return true;
		}else{
			return false;
		}
	}
	
	private String getMappedProviderCode(String providerCodeFromLogistic, String serviceFromLogistic){
		_log.debug("Provider Code From Logistic : " + providerCodeFromLogistic);
		_log.debug("Service Code From Logistic : " + serviceFromLogistic);
		
		String codeFromLogistic = "";
		if (providerCodeFromLogistic.equalsIgnoreCase("NCS")) {
            if (serviceFromLogistic.equalsIgnoreCase("REGULER") || serviceFromLogistic.equalsIgnoreCase("REGULAR")) {
            	codeFromLogistic = "NCS_REG";
            } else if (serviceFromLogistic.equalsIgnoreCase("KIRIMAN 1 HARI")) {
            	codeFromLogistic = "NCS_EXP";
            } else {
            	codeFromLogistic = "NCS_REG";
            }
            
        } else if (providerCodeFromLogistic.equalsIgnoreCase("RPX")) {
            if (serviceFromLogistic.equalsIgnoreCase("PP")) {
            	codeFromLogistic = "RPX_EXP";
            } else if (serviceFromLogistic.equalsIgnoreCase("EP")) {
            	codeFromLogistic = "RPX_REG";
            } else {
            	codeFromLogistic = "RPX_EXP";
            }
        } else if (providerCodeFromLogistic.equalsIgnoreCase("JNE")) {
        	codeFromLogistic = "JNE_REG";
        } else if (providerCodeFromLogistic.equalsIgnoreCase("MSG")) {
        	codeFromLogistic = "MSG";
        }
		
		return codeFromLogistic;
	}
	
	private String getMappedProviderCode(String serviceFromMTA){
		_log.debug("Service Code From MTA : " + serviceFromMTA);
		String codeFromMTA = "";
		if (serviceFromMTA.equalsIgnoreCase("NCS")) {
        	codeFromMTA = "NCS_REG";
        } else if (serviceFromMTA.equalsIgnoreCase("RPX")) {
        	codeFromMTA = "RPX_EXP";
        } else if (serviceFromMTA.equalsIgnoreCase("JNE")) {
        	codeFromMTA = "JNE_REG";
        } else if (serviceFromMTA.equalsIgnoreCase("MSG")) {
        	codeFromMTA = "MSG";
        }
		
		return codeFromMTA;
	}
	
	@Override
	public boolean reconcileRecipient(Long logAirwayBillId, String recipientFromLogistic, String recipientFromMTA){
		if(isRecipientMatched(recipientFromLogistic,recipientFromMTA)){
			return true;
		}else{
			_log.debug("Recipient mismatch found for LogAirwayBill id : " + logAirwayBillId);
			_log.debug("Logistic Recipient : " + recipientFromLogistic);
			_log.debug("MTA Recipient : " + recipientFromMTA);
			
			LogActivityReconRecord logActivityReconRecord = setActivityRecon(logAirwayBillId, 
																			 LogReconActivityRecordResultConstants.RECIPIENT_MISMATCH, 
																			 recipientFromLogistic, 
																			 recipientFromMTA, 
																			 LogActivityReconRecordConstants.COMMENT_RECIPIENT_MISMATCH.toString());	

			logActivityReconRecordDAO.save(logActivityReconRecord);
			
			return false;
		}
	}
	
	private boolean isRecipientMatched(String recipientFromLogistic, String recipientFromMTA){
		
		if(recipientFromLogistic != null && recipientFromMTA == null)
			return false;
		
		if(recipientFromLogistic == null && recipientFromMTA != null)
			return false;
		
		if(recipientFromLogistic.equals(recipientFromMTA)){
			return true;
		}else{
			return false;
		}
	}
	
	public LogActivityReconRecord setAppliedActivityRecon(Long logAirwayBillId, 
														  LogActionAppliedConstants actionAppliedConstants,
														  LogReconActivityRecordResultConstants reconActivityRecordResultConstants,
												   		  String dataFromLogistic, 
												   		  String datafromMTA,
												   		  String comment){
		
		LogActivityReconRecord logActivityReconRecord = setActivityRecon(logAirwayBillId, 
																		 reconActivityRecordResultConstants,
																		 dataFromLogistic, 
																		 datafromMTA, 
																		 comment);
		
		LogActionApplied logActionApplied = new LogActionApplied();
		logActionApplied.setActionAppliedId(actionAppliedConstants.id());
		
		logActivityReconRecord.setLogActionApplied(logActionApplied);
		
		return logActivityReconRecord;
	}
	
	public LogActivityReconRecord setActivityRecon(Long logAirwayBillId, 
												   LogReconActivityRecordResultConstants reconActivityRecordResultConstants,
												   String dataFromLogistic, 
												   String datafromMTA,
												   String comment){

		LogAirwayBill logAirwayBill = new LogAirwayBill();
		logAirwayBill.setAirwayBillId(logAirwayBillId);
		
		LogActivityReconRecord logActivityReconRecord = new LogActivityReconRecord();
		
		LogReconActivityRecordResult logReconActivityRecordResult = new LogReconActivityRecordResult();
		logReconActivityRecordResult.setReconRecordResultId(reconActivityRecordResultConstants.id());
		logActivityReconRecord.setLogReconActivityRecordResult(logReconActivityRecordResult);
		
		logActivityReconRecord.setLogAirwayBill(logAirwayBill);
        logActivityReconRecord.setComment(comment);

        logActivityReconRecord.setProviderData(dataFromLogistic);
        logActivityReconRecord.setVeniceData(datafromMTA);
        
        return logActivityReconRecord;
	}
	
}
