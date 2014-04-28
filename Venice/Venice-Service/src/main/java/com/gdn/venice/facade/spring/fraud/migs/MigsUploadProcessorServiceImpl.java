package com.gdn.venice.facade.spring.fraud.migs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.djarum.raf.utilities.SQLDateUtility;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.dao.VenMigsUploadTemporaryDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.exception.MIGSFileParserException;
import com.gdn.venice.facade.spring.fraud.migs.strategy.FileParserStrategy;
import com.gdn.venice.facade.spring.fraud.migs.strategy.MigsFileParserStrategy;
import com.gdn.venice.fraud.dataimport.MigsReport;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenMigsUploadTemporary;
import com.gdn.venice.util.CommonUtil;

@Service
public class MigsUploadProcessorServiceImpl implements
		MigsUploadProcessorService {
	
	private static final String CLASS_NAME = MigsUploadProcessorServiceImpl.class.getCanonicalName();
	
	@Autowired
	VenMigsUploadMasterDAO venMigsUploadMasterDAO;
	
	@Autowired
	VenMigsUploadTemporaryDAO venMigsUploadTemporaryDAO;
	
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	FileParserStrategy fileParserStrategy;
	
	@Override
	public String process(String fileNameAndFullPath) throws MIGSFileParserException{
		List<String> failedMessage = new ArrayList<String>();
		int noOfSuccess = 0;
		
		ArrayList<PojoInterface> resultList = parse(fileNameAndFullPath);
		
		for (PojoInterface pojoInterface : resultList) {
			MigsReport migsReport = (MigsReport) pojoInterface;
			
			String transactionId = isNull(migsReport.getTransactionId(),"");
			String authCode = isNull(formatAuthCode(migsReport.getAuthorisationCode()), "");
			
			if(isMigsAlreadyUploaded(transactionId, authCode)){
				CommonUtil.logInfo(CLASS_NAME, "Transaction Id : " + transactionId + ", Auth Code : " + authCode + " is already uploaded");
				addFailedMessage(failedMessage, transactionId, authCode);
			}else{
				processEachMigs(migsReport, fileNameAndFullPath);
				
				noOfSuccess++;
			}
		}
		
		return constructSuccessMessage(noOfSuccess, failedMessage);
	}
	
	public boolean processEachMigs(MigsReport migsReport, String fileNameAndFullPath){
		VenMigsUploadTemporary migsUploadTemporary = mapMigsReportToVenMigsUploadTemp(migsReport, fileNameAndFullPath);
		VenMigsUploadMaster migsUploadMaster = mapMigsReportToVenMigsUploadMaster(migsReport, fileNameAndFullPath);
		
		String merchantTransactionReference = isNull(migsReport.getMerchantTransactionReference(), "");
		String referenceId = isNull(formatAuthCode(migsReport.getAuthorisationCode()), "");
		String wcsOrderId = getWcsOrderIdFromMerchantTransactionReference(merchantTransactionReference);
		
		CommonUtil.logInfo(CLASS_NAME, "wcsOrderId : " + wcsOrderId + ", merchantTransactionReference : " + merchantTransactionReference + ", referenceId : " + referenceId);
		
		if(!isMigsResponseCodeApproved(migsReport.getResponseCode())){
			CommonUtil.logInfo(CLASS_NAME, "Not Approved");
			
			setMigsUploadMasterNotAnApprovedTransaction(migsUploadMaster, migsReport);
			setMigsUploadTempNotAnApprovedTransaction(migsUploadTemporary, migsReport);
			
		}else if(isMigsDataIncomplete(migsUploadTemporary)){
			CommonUtil.logInfo(CLASS_NAME, "Data Incomplete");
			ArrayList<String> incompleteNoteList = getIncompleteDataNote(migsUploadTemporary);
			
			setMigsUploadMasterIncompleteData(migsUploadMaster, migsReport, incompleteNoteList);
			setMigsUploadTempIncompleteData(migsUploadTemporary, migsReport, incompleteNoteList);
			
		}else if(!isOrderRelatedToPaymentExist(wcsOrderId, referenceId)){
			CommonUtil.logInfo(CLASS_NAME, "Order Not Found");
			setMigsUploadMasterOrderNotFound(migsUploadMaster, migsReport);
			setMigsUploadTempOrderNotFound(migsUploadTemporary, migsReport);
			
		}else{
			CommonUtil.logInfo(CLASS_NAME, "Submitted");
			setMigsUploadMasterActionSubmit(migsUploadMaster);
			setMigsUploadTempActionSubmit(migsUploadTemporary);
			
		}
		
		venMigsUploadMasterDAO.save(migsUploadMaster);
		venMigsUploadTemporaryDAO.save(migsUploadTemporary);
		
		return true;
	}
	
	
	@Override
	public ArrayList<PojoInterface> parse(String fileNameAndFullPath) throws MIGSFileParserException {
		if(fileParserStrategy == null)
			fileParserStrategy = new MigsFileParserStrategy();
		
		CommonUtil.logInfo(CLASS_NAME, "Parsing file : " + fileNameAndFullPath);
		
		fileParserStrategy.parse(fileNameAndFullPath);
		
		if(fileParserStrategy.isError()){
			CommonUtil.logError(CLASS_NAME, fileParserStrategy.getErrorMessage());
			throw new MIGSFileParserException(fileParserStrategy.getErrorMessage());
		}
		
		return fileParserStrategy.getParseResult();
	}
	
	@Override
	public String formatAuthCode(String authCode) {
		CommonUtil.logInfo(CLASS_NAME, "Formating Auth Code : " + authCode);
		if(!authCode.equals("null")){
			if(authCode.length()<6 ){
				authCode="000000".substring(0, 6-authCode.length())+authCode;
			}
		}else{
			authCode=null;
		}
		CommonUtil.logInfo(CLASS_NAME, "Auth Code Result: " + authCode);
		return authCode;
	}

	@Override
	public boolean isMigsAlreadyUploaded(String transactionId, String authCode) {
		CommonUtil.logInfo(CLASS_NAME, "isMigsAlreadyUploaded ? Transaction Id : " + transactionId + ", Auth Code : " + authCode);
		List<VenMigsUploadMaster> resultList = venMigsUploadMasterDAO.findByTransactionIdAuthCodeAndActionNotRemoved(transactionId, authCode);
		CommonUtil.logInfo(CLASS_NAME, "isMigsAlreadyUploaded Result : " + (resultList.size() > 0));
		return resultList.size() > 0;
	}

	@Override
	public VenMigsUploadTemporary mapMigsReportToVenMigsUploadTemp(MigsReport migsReport, String uploadFileNameAndFullPath) {
		CommonUtil.logInfo(CLASS_NAME, "Transaction Id : " + migsReport.getTransactionId() + " MIGS Upload Temp mapper");
		
		VenMigsUploadTemporary newMigsUploadTemporary = new VenMigsUploadTemporary();
		newMigsUploadTemporary.setTransactionId(migsReport.getTransactionId());
		newMigsUploadTemporary.setTransactionDate(SQLDateUtility.utilDateToSqlTimestamp(migsReport.getDate()));
		newMigsUploadTemporary.setMerchantId(migsReport.getMerchantId());
		newMigsUploadTemporary.setOrderReference(migsReport.getOrderReference());
		newMigsUploadTemporary.setOrderId(migsReport.getOrderId());
		newMigsUploadTemporary.setMerchantTransactionReference(migsReport.getMerchantTransactionReference());
		newMigsUploadTemporary.setTransactionType(migsReport.getTransactionType());
		newMigsUploadTemporary.setAcquirerId(migsReport.getAcquirerId());
		newMigsUploadTemporary.setBatchNumber(migsReport.getBatchNumber());
		newMigsUploadTemporary.setCurrency(migsReport.getCurrency());
		newMigsUploadTemporary.setAmount(migsReport.getAmount());
		newMigsUploadTemporary.setRrn(migsReport.getRrn());
		newMigsUploadTemporary.setResponseCode(migsReport.getResponseCode());
		newMigsUploadTemporary.setAcquirerResponseCode(migsReport.getAcquirerResponseCode());
		newMigsUploadTemporary.setAuthorisationCode(formatAuthCode(migsReport.getAuthorisationCode()));
		newMigsUploadTemporary.setOperator(migsReport.getOperatorId());
		newMigsUploadTemporary.setMerchantTransactionSource(migsReport.getMerchantTransactionSource());
		newMigsUploadTemporary.setOrderDate(SQLDateUtility.utilDateToSqlTimestamp(migsReport.getOrderDate()));
		newMigsUploadTemporary.setCardType(migsReport.getCardType());
		newMigsUploadTemporary.setCardNumber(migsReport.getCardNumber());
		newMigsUploadTemporary.setCardExpiryMonth(migsReport.getCardExpiryMonth());
		newMigsUploadTemporary.setCardExpiryYear(migsReport.getCardExpiryYear());
		newMigsUploadTemporary.setDialectCscResultCode(migsReport.getDialectCSCResultCode());
		newMigsUploadTemporary.setComment(migsReport.getComment());
		newMigsUploadTemporary.setEcommerceIndicator(migsReport.geteCommerceIndicator());
		newMigsUploadTemporary.setFileName(uploadFileNameAndFullPath);
		
		return newMigsUploadTemporary;

	}

	@Override
	public VenMigsUploadMaster mapMigsReportToVenMigsUploadMaster(MigsReport migsReport, String uploadFileNameAndFullPath) {
		VenMigsUploadMaster newMigsUploadMaster;
		
		List<VenMigsUploadMaster> migsMasterList = venMigsUploadMasterDAO.findByTransactionIdAuthCode(isNull(migsReport.getTransactionId(), ""), isNull(formatAuthCode(migsReport.getAuthorisationCode()), ""));
		
		if(migsMasterList.size() > 0){
			CommonUtil.logInfo(CLASS_NAME, "Transaction Id : " + migsReport.getTransactionId() + " MIGS Upload Master already exist, fetch existing");
			newMigsUploadMaster = migsMasterList.get(0);
		}else{
			CommonUtil.logInfo(CLASS_NAME, "Transaction Id : " + migsReport.getTransactionId() + " MIGS Upload Master not exist, create new");
			newMigsUploadMaster = new VenMigsUploadMaster();
		}
		
		newMigsUploadMaster.setTransactionId(migsReport.getTransactionId());
		newMigsUploadMaster.setTransactionDate(SQLDateUtility.utilDateToSqlTimestamp(migsReport.getDate()));
		newMigsUploadMaster.setMerchantId(migsReport.getMerchantId());
		newMigsUploadMaster.setOrderReference(migsReport.getOrderReference());
		newMigsUploadMaster.setOrderId(migsReport.getOrderId());
		newMigsUploadMaster.setMerchantTransactionReference(migsReport.getMerchantTransactionReference());
		newMigsUploadMaster.setTransactionType(migsReport.getTransactionType());
		newMigsUploadMaster.setAcquirerId(migsReport.getAcquirerId());
		newMigsUploadMaster.setBatchNumber(migsReport.getBatchNumber());
		newMigsUploadMaster.setCurrency(migsReport.getCurrency());
		newMigsUploadMaster.setAmount(migsReport.getAmount());
		newMigsUploadMaster.setRrn(migsReport.getRrn());
		newMigsUploadMaster.setResponseCode(migsReport.getResponseCode());
		newMigsUploadMaster.setAcquirerResponseCode(migsReport.getAcquirerResponseCode());
		newMigsUploadMaster.setAuthorisationCode(formatAuthCode(migsReport.getAuthorisationCode()));
		newMigsUploadMaster.setOperator(migsReport.getOperatorId());
		newMigsUploadMaster.setMerchantTransactionSource(migsReport.getMerchantTransactionSource());
		newMigsUploadMaster.setOrderDate(SQLDateUtility.utilDateToSqlTimestamp(migsReport.getOrderDate()));
		newMigsUploadMaster.setCardType(migsReport.getCardType());
		newMigsUploadMaster.setCardNumber(migsReport.getCardNumber());
		newMigsUploadMaster.setCardExpiryMonth(migsReport.getCardExpiryMonth());
		newMigsUploadMaster.setCardExpiryYear(migsReport.getCardExpiryYear());
		newMigsUploadMaster.setDialectCscResultCode(migsReport.getDialectCSCResultCode());
		newMigsUploadMaster.setComment(migsReport.getComment());
		newMigsUploadMaster.setEcommerceIndicator(migsReport.geteCommerceIndicator());
		newMigsUploadMaster.setFileName(uploadFileNameAndFullPath);
		
		return newMigsUploadMaster;
	}

	@Override
	public boolean isMigsResponseCodeApproved(String responseCode) {
		if(responseCode.equalsIgnoreCase("0 - Approved"))
			return true;
		else
			return false;
	}

	@Override
	public boolean isMigsDataIncomplete(VenMigsUploadTemporary migsUploadTemporary) {
		boolean isMerchantTransactionRefEmpty = isMerchantTransactionRefEmpty(migsUploadTemporary);
		boolean isAuthCodeEmpty = isAuthCodeEmpty(migsUploadTemporary);
		boolean isCardNumberEmpty = isCardNumberEmpty(migsUploadTemporary);
		
		if(isMerchantTransactionRefEmpty || isAuthCodeEmpty || isCardNumberEmpty)
			return true;
		else
			return false;
	}

	@Override
	public boolean isMerchantTransactionRefEmpty(VenMigsUploadTemporary migsUploadTemporary) {
		CommonUtil.logInfo(CLASS_NAME, "Merchant transaction ref : " + isNull(migsUploadTemporary.getMerchantTransactionReference(),""));
		if(isNull(migsUploadTemporary.getMerchantTransactionReference(),"").equals("")){
			CommonUtil.logInfo(CLASS_NAME, "Merchant transaction ref empty");
			return true;
		}else{
			CommonUtil.logInfo(CLASS_NAME, "Merchant transaction ref not empty");
			return false;
		}
	}

	@Override
	public boolean isAuthCodeEmpty(VenMigsUploadTemporary migsUploadTemporary) {
		CommonUtil.logInfo(CLASS_NAME, "Auth Code : " + isNull(migsUploadTemporary.getAuthorisationCode(),""));
		if(isNull(migsUploadTemporary.getAuthorisationCode(),"").equals("")){
			CommonUtil.logInfo(CLASS_NAME, "Auth Code empty");
			return true;
		}else{
			CommonUtil.logInfo(CLASS_NAME, "Auth Code not empty");
			return false;
		}
	}

	@Override
	public boolean isCardNumberEmpty(VenMigsUploadTemporary migsUploadTemporary) {
		CommonUtil.logInfo(CLASS_NAME, "Card Number : " + isNull(migsUploadTemporary.getCardNumber(),""));
		if(isNull(migsUploadTemporary.getCardNumber(),"").equals("")){
			CommonUtil.logInfo(CLASS_NAME, "Card Number empty");
			return true;
		}else{
			CommonUtil.logInfo(CLASS_NAME, "Card Number not empty");
			return false;
		}
	}

	@Override
	public boolean isOrderRelatedToPaymentExist(String wcsOrderId, String referenceId) {
		int relatedOrderFound = venOrderPaymentAllocationDAO.countByWcsOrderIdAndPaymentRef(wcsOrderId, referenceId);
		CommonUtil.logInfo(CLASS_NAME, "Order " + wcsOrderId + " with ref " + referenceId + " exist ? " + (relatedOrderFound > 0));
		return relatedOrderFound > 0;
	}
	
	public ArrayList<String> getIncompleteDataNote(VenMigsUploadTemporary migsUpload){
		ArrayList<String> incompleteDataNoteList = new ArrayList<String>(3);
		
		if(isMerchantTransactionRefEmpty(migsUpload))
			incompleteDataNoteList.add("Order ID");
		
		if(isAuthCodeEmpty(migsUpload))
			incompleteDataNoteList.add("Authorization Code");
		
		if(isCardNumberEmpty(migsUpload))
			incompleteDataNoteList.add("Credit Card No.");
		
		return incompleteDataNoteList;
	}
	
	public String getWcsOrderIdFromMerchantTransactionReference(String merchantTransRef){
		return merchantTransRef.replaceAll("-.*$", "");
	}
	
	public VenMigsUploadMaster setMigsUploadMasterNotAnApprovedTransaction(VenMigsUploadMaster migsUpload, MigsReport migsReport){
		migsUpload.setProblemDescription("Not an approved transaction (Status: " + migsReport.getResponseCode() + ")");
		migsUpload.setAction("REMOVE");
		
		return migsUpload;
	}
	
	public VenMigsUploadTemporary setMigsUploadTempNotAnApprovedTransaction(VenMigsUploadTemporary migsUpload, MigsReport migsReport){
		migsUpload.setProblemDescription("Not an approved transaction (Status: " + migsReport.getResponseCode() + ")");
		migsUpload.setAction("REMOVE");
		
		return migsUpload;
	}
	
	public VenMigsUploadMaster setMigsUploadMasterIncompleteData(VenMigsUploadMaster migsUpload, MigsReport migsReport, ArrayList<String> errorNoteList){
		migsUpload.setProblemDescription("Incomplete data (" + StringUtils.join(errorNoteList.toArray(),", ") + ") (Transaction Type : "+migsReport.getTransactionType()+" and Response Code : "+migsReport.getResponseCode()+")");
		migsUpload.setAction("REMOVE");
		
		return migsUpload;
	}
	
	public VenMigsUploadTemporary setMigsUploadTempIncompleteData(VenMigsUploadTemporary migsUpload, MigsReport migsReport, ArrayList<String> errorNoteList){
		migsUpload.setProblemDescription("Incomplete data (" + StringUtils.join(errorNoteList.toArray(),", ") + ") (Transaction Type : "+migsReport.getTransactionType()+" and Response Code : "+migsReport.getResponseCode()+")");
		migsUpload.setAction("REMOVE");
		
		return migsUpload;
	}
	
	public VenMigsUploadMaster setMigsUploadMasterOrderNotFound(VenMigsUploadMaster migsUpload, MigsReport migsReport){
		migsUpload.setProblemDescription("Order is not found on venice (Transaction Type : "+migsReport.getTransactionType()+" and Response Code : "+migsReport.getResponseCode()+")");
		migsUpload.setAction("KEEP");
		
		return migsUpload;
	}
	
	public VenMigsUploadTemporary setMigsUploadTempOrderNotFound(VenMigsUploadTemporary migsUpload, MigsReport migsReport){
		migsUpload.setProblemDescription("Order is not found on venice (Transaction Type : "+migsReport.getTransactionType()+" and Response Code : "+migsReport.getResponseCode()+")");
		migsUpload.setAction("KEEP");
		
		return migsUpload;
	}
	
	public VenMigsUploadMaster setMigsUploadMasterActionSubmit(VenMigsUploadMaster migsUpload){
		migsUpload.setAction("SUBMIT");
		
		return migsUpload;
	}
	
	public VenMigsUploadTemporary setMigsUploadTempActionSubmit(VenMigsUploadTemporary migsUpload){
		migsUpload.setAction("SUBMIT");
		
		return migsUpload;
	}
	
	public String isNull(Object object, String replacement) {
		return object == null ? replacement : object.toString();
	}
	
	public void addFailedMessage(List<String> failedMessage, String transactionId, String authCode){
		failedMessage.add("Already Exist Order ID: " + transactionId + ", Auth Code:" + authCode);
	}
	
	public String constructSuccessMessage(int noOfSuccess, List<String> failedMessage){
		StringBuffer sb = new StringBuffer();
		sb.append("Report uploaded successfully:");
		sb.append("\\\\n   " + noOfSuccess + " new rows(s) have been uploaded.");
		sb.append(failedMessage.size() > 0 ? "\\\\n   " + failedMessage.size() + " row(s) were found as duplicate entry on upload list.\\\\n" + StringUtils.join(failedMessage.toArray(), "\\\\n") : "");
		sb.append("\\\\n\\\\nPlease refresh upload list...");
		
		return sb.toString();
	}

}
