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
import com.gdn.venice.finance.dataexportimport.Mandiri_Installment_Record;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.VeniceConstants;

@Service("MandiriInstallmentCCFundInServiceImpl")
public class MandiriInstallmentCCFundInServiceImpl extends AbstractFundInService {
	private static final FinArFundsInReportTypeConstants REPORT_TYPE = FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC;
	private static final String CLASS_NAME = BCACCFundInServiceImpl.class.getCanonicalName();
	
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
			
			for(PojoInterface pojo : fundInData.getFundInList()){
				Mandiri_Installment_Record rec = (Mandiri_Installment_Record) pojo;
				
				if(isPaymentAmountNotLessThanZero(new BigDecimal(rec.getAmount()))){
					fundInRecon = processEachFundIn(pojo, finArFundsInReport);
					if(fundInRecon != null){
						fundInReconReadyToPersistList.add(fundInRecon);
						fundInData.getProcessedFundInList().add(fundInRecon.getNomorReff());
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

	@Override
	public ArrayList<PojoInterface> parse(String fileNameAndFullPath)
			throws FundInFileParserException {
		ExcelToPojo excelToPojo = null;
    	
        try {
			excelToPojo = new ExcelToPojo(Mandiri_Installment_Record.class, getFinanceFundInReportTemplateFileNameAndFullPath(REPORT_TYPE), fileNameAndFullPath, 9, 0);
			excelToPojo = excelToPojo.getPojoToExcel(19,"MID","TOTAL");
        } catch (Exception e) {
        	String errMsg = "Error parsing Excel File Processing row number:" + (excelToPojo != null && excelToPojo.getErrorRowNumber() != null?excelToPojo.getErrorRowNumber():"1");
			CommonUtil.logError(CLASS_NAME, errMsg);
			throw new FundInFileParserException(errMsg);
		}
        
		return excelToPojo.getPojoResult();
	}

	@Override
	public FundInData mergeAndSumDuplicate(ArrayList<PojoInterface> fundInList) {
		CommonUtil.logDebug(CLASS_NAME, "No fund in duplication check for this fund in");
		FundInData fundInData = new FundInData();
		
		Mandiri_Installment_Record rec = null;
		
		for(int i = 0 ; i < fundInList.size(); i++){
			rec = (Mandiri_Installment_Record) fundInList.get(i);
				
			fundInData.getFundInList().add(rec);
		}
		
		return fundInData;
	}
	
	public FinArFundsInReconRecord processEachFundIn(PojoInterface pojo, FinArFundsInReport finArFundsInReport) throws ParseException{
		FinArFundsInReconRecord fundInRecon = null;
		
		Mandiri_Installment_Record rec = (Mandiri_Installment_Record) pojo;
		
		String authCode = rec.getAuthCode().replaceAll("\'", "");
		BigDecimal paymentAmount = new BigDecimal(rec.getAmount());
		Date paymentDate = new java.sql.Timestamp(SDF_dd_MMM_yyyy.parse(rec.getTrxDate()).getTime());
		
		if(!isFundInOkToContinue(authCode, paymentDate + "", paymentAmount, REPORT_TYPE)) {
			return null;
		}
		
		VenOrder order = getOrderByRelatedPayment(authCode, paymentAmount, REPORT_TYPE);
		
		if(order != null && !isOrderPaymentExist(authCode, paymentAmount, REPORT_TYPE)){
			CommonUtil.logDebug(CLASS_NAME, "Payments were found in the import file that do not exist in the payment schedule in VENICE:" + authCode);
			return null;
		}
		
		if(order != null){
			VenOrderPaymentAllocation orderPaymentAllocation 
				= getPaymentAllocationByRelatedPayment(authCode, 
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
			fundInRecon.setNomorReff(authCode);
		}
		
		fundInRecon.setPaymentConfirmationNumber(authCode);
		fundInRecon.setFinArFundsInReport(finArFundsInReport);
		BigDecimal paidAmount = fundInRecon.getProviderReportPaidAmount()!=null?fundInRecon.getProviderReportPaidAmount(): new BigDecimal(0);
		
		CommonUtil.logDebug(CLASS_NAME, "MID "+rec.getmId().toUpperCase());
		
		BigDecimal feePercentage = getFeePercentageByMID(rec.getmId());
		CommonUtil.logDebug(CLASS_NAME, "Bank Fee "+ feePercentage.multiply(new BigDecimal(100))+" % "+fundInRecon.getProviderReportFeeAmount());
		
		fundInRecon.setProviderReportFeeAmount(paymentAmount.multiply(feePercentage));
		fundInRecon.setProviderReportPaidAmount(paidAmount.add(paymentAmount).abs());		
		fundInRecon.setProviderReportPaymentId(authCode);
		fundInRecon.setReconcilliationRecordTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
		fundInRecon.setProviderReportPaymentDate(new java.sql.Timestamp(paymentDate.getTime()));
		fundInRecon.setRefundAmount(new BigDecimal(0));
		
		fundInRecon.setFinArReconResult(getReconResult(fundInRecon));
		
		return fundInRecon;
	}

	private BigDecimal getFeePercentageByMID(String mid) {
		BigDecimal amountMultiplier;
		
		if(mid.toUpperCase().equals(VeniceConstants.BLIBLI_PB3)){
			amountMultiplier = new BigDecimal("0.03");
		}else if(mid.toUpperCase().equals(VeniceConstants.BLIBLI_PB6)){
			amountMultiplier = new BigDecimal("0.045");
		}else if(mid.toUpperCase().equals(VeniceConstants.BLIBLI_PB12)){
			amountMultiplier = new BigDecimal("0.06");
		}else{
			amountMultiplier = new BigDecimal("0.025");
		}
		
		return amountMultiplier;
	}

}
