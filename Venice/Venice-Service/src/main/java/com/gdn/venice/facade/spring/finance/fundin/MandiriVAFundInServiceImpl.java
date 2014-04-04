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
import com.gdn.venice.exportimport.finance.dataimport.MT942_FileReader;
import com.gdn.venice.finance.dataexportimport.MT942_Record;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsIdReportTime;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service("MandiriVAFundInServiceImpl")
public class MandiriVAFundInServiceImpl extends AbstractFundInService {
	private static final FinArFundsInReportTypeConstants REPORT_TYPE = FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA;
	private static final String CLASS_NAME = MandiriVAFundInServiceImpl.class.getCanonicalName();
	
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
				MT942_Record rec = (MT942_Record) pojo;
				
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

	public List<FinArFundsInReconRecord> getNextDayFundInRecon(MT942_Record rec) {
		List<FinArFundsInReconRecord> fundInReconList;
		String accountNumber = rec.getAccountNumber();
		BigDecimal paymentAmount = new BigDecimal(rec.getPaymentAmount());
		String paymentDate = SDF_yyyyMMdd_HHmmss2.format(rec.getPaymentDate());
		
		fundInReconList = getFundInReconForVAPayment(accountNumber, paymentDate + " - " + paymentAmount);
		return fundInReconList;
	}

	@Override
	public ArrayList<PojoInterface> parse(String fileNameAndFullPath)
			throws FundInFileParserException {
		ArrayList<PojoInterface> records = null;
		
		MT942_FileReader reader = new MT942_FileReader();
		
		try {
			ArrayList<MT942_Record> results = reader.readFile(fileNameAndFullPath);
			records = new ArrayList<PojoInterface>(results.size());
			
			for (MT942_Record mandiriRecord : results) {
				records.add(mandiriRecord);
			}
			
		} catch (Exception e) {
			String errMsg = "Error parsing Text File";
			CommonUtil.logError(CLASS_NAME, e);
			throw new FundInFileParserException(errMsg);
		}
		
		return records;
	}

	@Override
	public FundInData mergeAndSumDuplicate(ArrayList<PojoInterface> fundInList) {
		CommonUtil.logDebug(CLASS_NAME, "No fund in duplication check for this fund in");
		FundInData fundInData = new FundInData();
		
		MT942_Record rec = null;
		
		for(int i = 0 ; i < fundInList.size(); i++){
			rec = (MT942_Record) fundInList.get(i);
				
			fundInData.getFundInList().add(rec);
		}
		
		return fundInData;
	}
	
	public FinArFundsInReconRecord processEachFundIn(PojoInterface pojo, FinArFundsInReport finArFundsInReport, boolean isNextDayPayment) throws ParseException{
		FinArFundsInReconRecord fundInRecon = null;
		
		MT942_Record rec = (MT942_Record) pojo;
		
		String accountNumber = rec.getAccountNumber();
		
		BigDecimal paymentAmount = new BigDecimal(rec.getPaymentAmount());
		
		String paymentDate = SDF_yyyyMMdd_HHmmss2.format(rec.getPaymentDate());
		
		String uniquePayment = paymentDate + " - " + paymentAmount;
		
		if(!isFundInOkToContinue(accountNumber, uniquePayment, paymentAmount, REPORT_TYPE)) {
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
			BigDecimal remainingAmount = fundInRecon.getRemainingBalanceAmount()!=null?fundInRecon.getRemainingBalanceAmount(): new BigDecimal(0);
			fundInRecon.setRemainingBalanceAmount(remainingAmount.subtract(paymentAmount));
			fundInRecon.setNomorReff(accountNumber);
		}
		
		fundInRecon.setFinArFundsInReport(finArFundsInReport);
		
		BigDecimal paidAmount = fundInRecon.getProviderReportPaidAmount()!=null?fundInRecon.getProviderReportPaidAmount(): new BigDecimal(0);
		fundInRecon.setProviderReportPaidAmount(paidAmount.add(paymentAmount));	
		
		fundInRecon.setProviderReportFeeAmount(new BigDecimal(0));
		fundInRecon.setUniquePayment(uniquePayment);
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
		MT942_FileReader reader = new MT942_FileReader();
		String uniqueIds = reader.getUniqueReportIdentifier(fileNameAndFullPath);
		String[] uniqueIdSplit=uniqueIds.split("&");
		String typeReport=uniqueIdSplit[1];
		
		return typeReport.equalsIgnoreCase("old");
	}

}
