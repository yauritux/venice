package com.gdn.venice.facade.spring.finance.fundin;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gdn.venice.constants.FinApprovalStatusConstants;
import com.gdn.venice.constants.FinArFundsInActionAppliedConstants;
import com.gdn.venice.constants.FinArFundsInReportTimeConstants;
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.exception.FundInFileAlreadyUploadedException;
import com.gdn.venice.exception.FundInFileParserException;
import com.gdn.venice.exception.FundInNoFinancePeriodFoundException;
import com.gdn.venice.exportimport.finance.dataimport.BCA_VA_FileReader;
import com.gdn.venice.finance.dataexportimport.BCA_VA_IB_Record;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsIdReportTime;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service("BCAVAFundInServiceImpl")
public class BCAVAFundInServiceImpl extends AbstractFundInService {
	private static final FinArFundsInReportTypeConstants REPORT_TYPE = FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA;
	private static final String CLASS_NAME = BCAVAFundInServiceImpl.class.getCanonicalName();
	
	@Override
	public String process(String fileNameAndFullPath, String uploadUserName)
			throws NoSuchAlgorithmException, IOException,
			FundInFileAlreadyUploadedException,
			FundInNoFinancePeriodFoundException {
		
		if(isFileAlreadyUploaded(fileNameAndFullPath, REPORT_TYPE)){
			throw new FundInFileAlreadyUploadedException("The file is already uploaded");
		}
		
		FinArFundsInReport finArFundsInReport = createFundInReportRecord(REPORT_TYPE, fileNameAndFullPath, uploadUserName);
		
		try{
			ArrayList<PojoInterface> fundInList = parse(fileNameAndFullPath);
			
			FundInData fundInData = mergeAndSumDuplicate(fundInList);
			
			FinArFundsInReconRecord fundInRecon = null;
			List<FinArFundsInReconRecord> fundInReconReadyToPersistList = new ArrayList<FinArFundsInReconRecord>(fundInData.getFundInList().size());
			
			boolean isNextDayPaymentFile = isNextDayPaymentFile(fileNameAndFullPath);
			
			for(PojoInterface pojo : fundInData.getFundInList()){
				BCA_VA_IB_Record rec = (BCA_VA_IB_Record) pojo;
				
				if(isPaymentAmountNotLessThanZero(new BigDecimal(rec.getPaymentAmount()))){
					List<FinArFundsInReconRecord> nextDayFundInReconList = null;
					if(isNextDayPaymentFile){
						nextDayFundInReconList = getNextDayFundInRecon(rec);
					}
					
					if(nextDayFundInReconList == null || nextDayFundInReconList.size() == 0){
						fundInRecon = processEachFundIn(pojo, finArFundsInReport, isNextDayPaymentFile);
						if(fundInRecon != null){
							fundInReconReadyToPersistList.add(fundInRecon);
							fundInData.getProcessedFundInList().add(fundInRecon.getNomorReff());
						}
					}else{
						FinArFundsIdReportTime nextDayFundInReportTime = new FinArFundsIdReportTime();
						nextDayFundInReportTime.setReportTimeId(FinArFundsInReportTimeConstants.FIN_AR_FUNDS_IN_REPORT_TIME_H1.id());
						
						for(FinArFundsInReconRecord eachFundInRecon : nextDayFundInReconList){
							eachFundInRecon.setFinArFundsIdReportTime(nextDayFundInReportTime);
							fundInReconReadyToPersistList.add(eachFundInRecon);
							fundInData.getProcessedFundInList().add(eachFundInRecon.getNomorReff());
						}
					}
				}
			}
			
			finArFundsInReconRecordDAO.save(fundInReconReadyToPersistList);
			
			return constructSuccessMessage(fundInData);
			
		}catch(ParseException e){
			CommonUtil.logError(CLASS_NAME, e);
			removeFundInReportRecord(finArFundsInReport);
			e.printStackTrace();
			return e.getMessage();
		}catch(FundInFileParserException e) {
			CommonUtil.logError(CLASS_NAME, e);
			removeFundInReportRecord(finArFundsInReport);
			e.printStackTrace();
			return e.getMessage();
		}catch (Exception e) {
			CommonUtil.logError(CLASS_NAME, e);
			removeFundInReportRecord(finArFundsInReport);
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public List<FinArFundsInReconRecord> getNextDayFundInRecon(BCA_VA_IB_Record rec) {
		List<FinArFundsInReconRecord> fundInReconList;
		String accountNumber = rec.getAccountNumber();
		String paymentDate = SDF_yyyyMMdd_HHmmss2.format(rec.getPaymentDate());
		
		fundInReconList = getFundInReconForVAPayment(accountNumber, paymentDate);
		return fundInReconList;
	}

	@Override
	public ArrayList<PojoInterface> parse(String fileNameAndFullPath)
			throws FundInFileParserException {
		ArrayList<PojoInterface> records = null;
		
		BCA_VA_FileReader reader = new BCA_VA_FileReader();
		
		try {
			String reportType = getReportFileType(fileNameAndFullPath);
			ArrayList<BCA_VA_IB_Record> results = new ArrayList<BCA_VA_IB_Record>();
			
			if(isOldReportType(reportType)){
				CommonUtil.logDebug(CLASS_NAME, "Start Old Report ");
				results = reader.readFile(fileNameAndFullPath); 
			}else if(isNewReportType(reportType)){
				CommonUtil.logDebug(CLASS_NAME, "Start New Report ");
				results = reader.readNewFile(fileNameAndFullPath); 
			}else{
				CommonUtil.logDebug(CLASS_NAME, "Start New Report 2");
				results = reader.readNewFile2(fileNameAndFullPath); 
			}
			
			records = new ArrayList<PojoInterface>(results.size());
			
			for (BCA_VA_IB_Record bcaRecord : results) {
				records.add(bcaRecord);
			}
			
		} catch (Exception e) {
			String errMsg = "Error parsing Text File";
			CommonUtil.logError(CLASS_NAME, e);
			throw new FundInFileParserException(errMsg);
		}
		
		return records;
	}

	private boolean isNewReportType(String reportType) {
		return reportType.equalsIgnoreCase("new");
	}

	private boolean isOldReportType(String reportType) {
		return reportType.equalsIgnoreCase("old");
	}
	
	

	@Override
	public FundInData mergeAndSumDuplicate(ArrayList<PojoInterface> fundInList) {
		CommonUtil.logDebug(CLASS_NAME, "Check for duplicate fund in");
		FundInData fundInData = new FundInData();
		
		BCA_VA_IB_Record currentRecord = null;
		BCA_VA_IB_Record otherRecord = null;
		String currentAccountNumber = null;
		String otherAccountNumber = null;
		
		for(int i = 0 ; i < fundInList.size(); i++){
			currentRecord = (BCA_VA_IB_Record) fundInList.get(i);
			
			fundInList.remove(i);
			i = -1;
			
			currentAccountNumber = currentRecord.getAccountNumber();
			
			for(int j = 0 ; j < fundInList.size(); j++){
				otherRecord = (BCA_VA_IB_Record) fundInList.get(j);
				
				otherAccountNumber = otherRecord.getAccountNumber();
				
				if(currentAccountNumber.equals(otherAccountNumber)){
					CommonUtil.logDebug(CLASS_NAME, "Duplicate Fund In with Account Number" + currentAccountNumber);
					fundInList.remove(j);
					j = 0;
					
					currentRecord.setPaymentAmount(currentRecord.getPaymentAmount() + otherRecord.getPaymentAmount());
					currentRecord.setBankFee(currentRecord.getBankFee() + otherRecord.getBankFee());
				}
			}
			
			fundInData.getFundInList().add(currentRecord);
		}
		
		return fundInData;
	}
	
	public FinArFundsInReconRecord processEachFundIn(PojoInterface pojo, FinArFundsInReport finArFundsInReport, boolean isNextDayPayment) throws ParseException{
		FinArFundsInReconRecord fundInRecon = null;
		
		BCA_VA_IB_Record rec = (BCA_VA_IB_Record) pojo;
		
		String accountNumber = rec.getAccountNumber();
		
		BigDecimal paymentAmount = new BigDecimal(rec.getPaymentAmount());
		BigDecimal bankFee = new BigDecimal(rec.getBankFee());
		
		String paymentDate = SDF_yyyyMMdd_HHmmss2.format(rec.getPaymentDate());
		
		if(!isFundInOkToContinue(accountNumber, paymentDate, paymentAmount, REPORT_TYPE)) {
			return null;
		}
		
		VenOrder order = getOrderByRelatedPayment(accountNumber, paymentAmount, REPORT_TYPE,null);
		
		if(order != null && !isOrderPaymentExist(accountNumber, paymentAmount, REPORT_TYPE,null)){
			CommonUtil.logDebug(CLASS_NAME, "Payments were found in the import file that do not exist in the payment schedule in VENICE:" + accountNumber);
			return null;
		}
		
		if(order != null){
			VenOrderPaymentAllocation orderPaymentAllocation 
				= getPaymentAllocationByRelatedPayment(accountNumber, 
						                               paymentAmount, 
						                               REPORT_TYPE,null);
			
			List<FinArFundsInReconRecord> fundInReconList = orderPaymentAllocation.getVenOrderPayment().getFinArFundsInReconRecords();
			fundInRecon = fundInReconList.get(0);
		}else{
			fundInRecon = new FinArFundsInReconRecord();
		}
		
		FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
		finApprovalStatus.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
		fundInRecon.setFinApprovalStatus(finApprovalStatus);

		FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
		finArFundsInActionApplied.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id());
		fundInRecon.setFinArFundsInActionApplied(finArFundsInActionApplied);
		
		if(order != null && fundInRecon.getVenOrderPayment() != null){
			fundInRecon.setOrderDate(order.getOrderDate());
			fundInRecon.setWcsOrderId(order.getWcsOrderId());
			fundInRecon.setRemainingBalanceAmount(getRemainingBalanceAfterPayment(fundInRecon, paymentAmount));
		}else{
			fundInRecon.setRemainingBalanceAmount(paymentAmount.negate());
			fundInRecon.setNomorReff(accountNumber);
		}
		
		fundInRecon.setFinArFundsInReport(finArFundsInReport);
		
		BigDecimal paidAmount = fundInRecon.getProviderReportPaidAmount()!=null?fundInRecon.getProviderReportPaidAmount(): new BigDecimal(0);
		fundInRecon.setProviderReportPaidAmount(paidAmount.add(paymentAmount));	
		
		BigDecimal feeAmount = fundInRecon.getProviderReportFeeAmount()!=null?fundInRecon.getProviderReportFeeAmount(): new BigDecimal(0);
		fundInRecon.setProviderReportFeeAmount(feeAmount.add(bankFee));
		
		fundInRecon.setUniquePayment(paymentDate);
		fundInRecon.setProviderReportPaymentId(accountNumber);
		fundInRecon.setReconcilliationRecordTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
		fundInRecon.setProviderReportPaymentDate(new java.sql.Timestamp(rec.getPaymentDate().getTime()));
		fundInRecon.setRefundAmount(new BigDecimal(0));
		fundInRecon.setFinArReconResult(getReconResult(fundInRecon));
		
		FinArFundsIdReportTime finArFundsIdReportTime = getFundsIdReportTime(isNextDayPayment);
		fundInRecon.setFinArFundsIdReportTime(finArFundsIdReportTime);
		
		return fundInRecon;
	}
	
	public boolean isNextDayPaymentFile(String fileNameAndFullPath) throws Exception{
		BCA_VA_FileReader reader = new BCA_VA_FileReader();
		String uniqueIds = reader.getUniqueReportIdentifier(fileNameAndFullPath);
		String[] uniqueIdSplit=uniqueIds.split("&");
		String typeReport=uniqueIdSplit[1];
		
		return isOldReportType(typeReport);
	}
	
	public String getReportFileType(String fileNameAndFullPath) throws Exception{
		BCA_VA_FileReader reader = new BCA_VA_FileReader();
		String uniqueIds = reader.getUniqueReportIdentifier(fileNameAndFullPath);
		String[] uniqueIdSplit=uniqueIds.split("&");
		
		return uniqueIdSplit[1];
	}
	

}
