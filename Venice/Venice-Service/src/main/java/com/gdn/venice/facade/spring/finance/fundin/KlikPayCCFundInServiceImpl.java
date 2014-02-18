package com.gdn.venice.facade.spring.finance.fundin;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gdn.venice.constants.FinApprovalStatusConstants;
import com.gdn.venice.constants.FinArFundsInActionAppliedConstants;
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.exception.FundInFileAlreadyUploadedException;
import com.gdn.venice.exception.FundInFileParserException;
import com.gdn.venice.exception.FundInNoFinancePeriodFoundException;
import com.gdn.venice.finance.dataexportimport.BCA_CC_Record;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service("KlikPayCCFundInServiceImpl")
public class KlikPayCCFundInServiceImpl extends AbstractFundInService{
	private static final FinArFundsInReportTypeConstants REPORT_TYPE = FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC;
	private static final String CLASS_NAME = KlikPayCCFundInServiceImpl.class.getCanonicalName();
	
	@Override
	public String process(String fileNameAndFullPath, String uploadUserName) throws NoSuchAlgorithmException, 
																					IOException, 
																					FundInFileAlreadyUploadedException, 
																					FundInNoFinancePeriodFoundException{
		
		if(isFileAlreadyUploaded(fileNameAndFullPath, REPORT_TYPE)){
			throw new FundInFileAlreadyUploadedException("The file is already uploaded");
		}
		
		FinArFundsInReport finArFundsInReport = createFundInReportRecord(REPORT_TYPE, fileNameAndFullPath, uploadUserName);
		
		try{
			ArrayList<PojoInterface> fundInList = parse(fileNameAndFullPath);
			
			FundInData fundInData = mergeAndSumDuplicate(fundInList);
			
			FinArFundsInReconRecord fundInRecon = null;
			List<FinArFundsInReconRecord> fundInReconReadyToPersistList = new ArrayList<FinArFundsInReconRecord>(fundInData.getFundInList().size());
			
			for(PojoInterface pojo : fundInData.getFundInList()){
				fundInRecon = processEachFundIn(pojo, finArFundsInReport);
				if(fundInRecon != null){
					fundInReconReadyToPersistList.add(fundInRecon);
					fundInData.getProcessedFundInList().add(fundInRecon.getNomorReff());
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
	
	@Override
	public ArrayList<PojoInterface> parse(String fileNameAndFullPath) throws FundInFileParserException {
		ExcelToPojo excelToPojo = null;
        	
        try {
			excelToPojo = new ExcelToPojo(BCA_CC_Record.class, getFinanceFundInReportTemplateFileNameAndFullPath(REPORT_TYPE), fileNameAndFullPath, 0, 0);
			excelToPojo = excelToPojo.getPojo();
        } catch (Exception e) {
        	String errMsg = "Error parsing Excel File Processing row number:" + (excelToPojo != null && excelToPojo.getErrorRowNumber() != null?excelToPojo.getErrorRowNumber():"1");
			CommonUtil.logError(CLASS_NAME, errMsg);
			throw new FundInFileParserException(errMsg);
		}
        
		return excelToPojo.getPojoResult();
	}

	@Override
	public FundInData mergeAndSumDuplicate(ArrayList<PojoInterface> fundInList) {
		CommonUtil.logDebug(CLASS_NAME, "Check for duplicate fund in");
		FundInData fundInData = new FundInData();
		
		BCA_CC_Record currentBcaCcRecord = null;
		BCA_CC_Record otherBcaCcRecord = null;
		String currentAuthCd = null;
		String otherAuthCd = null;
		
		for(int i = 0 ; i < fundInList.size(); i++){
			currentBcaCcRecord = (BCA_CC_Record) fundInList.get(i);
			
			fundInList.remove(i);
			i = -1;
			
			currentAuthCd = currentBcaCcRecord.getAuthCd().length()<6?"000000".substring(0,6-currentBcaCcRecord.getAuthCd().length())+""+currentBcaCcRecord.getAuthCd():currentBcaCcRecord.getAuthCd();
			
			for(int j = 0 ; j < fundInList.size(); j++){
				otherBcaCcRecord = (BCA_CC_Record) fundInList.get(j);
				
				otherAuthCd = otherBcaCcRecord.getAuthCd().length()<6?"000000".substring(0,6-otherBcaCcRecord.getAuthCd().length())+""+otherBcaCcRecord.getAuthCd():otherBcaCcRecord.getAuthCd();
				
				if(currentAuthCd.equals(otherAuthCd)){
					CommonUtil.logDebug(CLASS_NAME, "Duplicate Fund In with Auth Code" + currentAuthCd);
					fundInList.remove(j);
					j = 0;
					
					currentBcaCcRecord.setNettAmt(add(currentBcaCcRecord.getNettAmt(), otherBcaCcRecord.getNettAmt()));
					currentBcaCcRecord.setGrossAmt(add(currentBcaCcRecord.getGrossAmt(), otherBcaCcRecord.getGrossAmt()));
					currentBcaCcRecord.setDiscAmt(add(currentBcaCcRecord.getDiscAmt(), otherBcaCcRecord.getDiscAmt()));
				}
			}
			
			if (compare(currentBcaCcRecord.getGrossAmt(),"0") == 0) {
				fundInData.getVoidFundInList().add(currentBcaCcRecord.getAuthCd());
			}else if (compare(currentBcaCcRecord.getGrossAmt(),"0") < 0) {
				fundInData.getRefundFundInList().add(currentBcaCcRecord.getAuthCd());
			}else {
				fundInData.getFundInList().add(currentBcaCcRecord);
			}
		}
		
		return fundInData;
	}
	
	public FinArFundsInReconRecord processEachFundIn(PojoInterface pojo, FinArFundsInReport finArFundsInReport) throws ParseException{
		FinArFundsInReconRecord fundInRecon = null;
		
		BCA_CC_Record rec = (BCA_CC_Record) pojo;
		
		String referenceId = rec.getAuthCd().length() < 6?"000000".substring(0,6-rec.getAuthCd().length())+""+rec.getAuthCd():rec.getAuthCd();
		String transactionTime = rec.getTransTime().length()<6?"000000".substring(0,6-rec.getTransTime().length())+""+rec.getTransTime():rec.getTransTime();
		
		BigDecimal paymentAmount = new BigDecimal(rec.getGrossAmt());
		BigDecimal discountAmount = new BigDecimal(rec.getDiscAmt());
		
		Date tgl=SDF_yyyyMMdd_HHmmss.parse(rec.getTransDate().replace(".", "").replace("E", "0")+" "+transactionTime);		
		
		if(!isFundInOkToContinue(referenceId, new java.sql.Timestamp(tgl.getTime())+"", paymentAmount, REPORT_TYPE)) {
			return null;
		}
		
		VenOrder order = getOrderByRelatedPayment(referenceId, paymentAmount, REPORT_TYPE);
		
		if(order != null && !isOrderPaymentExist(referenceId, paymentAmount, REPORT_TYPE)){
			CommonUtil.logDebug(CLASS_NAME, "Payments were found in the import file that do not exist in the payment schedule in VENICE:" + referenceId);
			return null;
		}
		
		if(order != null){
			VenOrderPaymentAllocation orderPaymentAllocation 
				= getPaymentAllocationByRelatedPayment(referenceId, 
						                               paymentAmount, 
						                               REPORT_TYPE);
			
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
			fundInRecon.setNomorReff(referenceId);
		}
		
		fundInRecon.setPaymentConfirmationNumber(referenceId);
		fundInRecon.setFinArFundsInReport(finArFundsInReport);
		BigDecimal feeAmount = fundInRecon.getProviderReportFeeAmount()!=null?fundInRecon.getProviderReportFeeAmount(): new BigDecimal(0);
		BigDecimal paidAmount = fundInRecon.getProviderReportPaidAmount()!=null?fundInRecon.getProviderReportPaidAmount(): new BigDecimal(0);
		fundInRecon.setProviderReportFeeAmount(feeAmount.add(discountAmount));
		fundInRecon.setProviderReportPaidAmount(paidAmount.add(paymentAmount));		
		fundInRecon.setProviderReportPaymentId(referenceId);
		fundInRecon.setReconcilliationRecordTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
		fundInRecon.setProviderReportPaymentDate(new java.sql.Timestamp(tgl.getTime()));
		fundInRecon.setRefundAmount(new BigDecimal(0));
		
		fundInRecon.setFinArReconResult(getReconResult(fundInRecon));
		
		return fundInRecon;
	}
	
}
