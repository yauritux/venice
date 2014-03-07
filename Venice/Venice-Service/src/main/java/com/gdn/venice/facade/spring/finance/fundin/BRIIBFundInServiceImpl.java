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
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.exception.FundInFileAlreadyUploadedException;
import com.gdn.venice.exception.FundInFileParserException;
import com.gdn.venice.exception.FundInNoFinancePeriodFoundException;
import com.gdn.venice.exportimport.finance.dataimport.BRI_IB_FileReader;
import com.gdn.venice.finance.dataexportimport.BCA_VA_IB_Record;
import com.gdn.venice.finance.dataexportimport.BRI_IB_Record;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service("BRIIBFundInServiceImpl")
public class BRIIBFundInServiceImpl extends AbstractFundInService{
	private static final FinArFundsInReportTypeConstants REPORT_TYPE = FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB;
	private static final String CLASS_NAME = BRIIBFundInServiceImpl.class.getCanonicalName();
	
	@Override
	public String process(String fileNameAndFullPath, String uploadUserName)
			throws NoSuchAlgorithmException, IOException,
			FundInFileAlreadyUploadedException,
			FundInNoFinancePeriodFoundException {
		
		FinArFundsInReport finArFundsInReport = createFundInReportRecord(REPORT_TYPE, fileNameAndFullPath, uploadUserName);
		
		try{
			ArrayList<PojoInterface> fundInList = parse(fileNameAndFullPath);
			
			FundInData fundInData = mergeAndSumDuplicate(fundInList);
			
			FinArFundsInReconRecord fundInRecon = null;
			List<FinArFundsInReconRecord> fundInReconReadyToPersistList = new ArrayList<FinArFundsInReconRecord>(fundInData.getFundInList().size());
			
			for(PojoInterface pojo : fundInData.getFundInList()){
				BRI_IB_Record rec = (BRI_IB_Record) pojo;
				
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
		ArrayList<PojoInterface> records = null;
		
		BRI_IB_FileReader reader = new BRI_IB_FileReader();
		
		try {
			ArrayList<BRI_IB_Record> results = reader.readFile(fileNameAndFullPath);
			records = new ArrayList<PojoInterface>(results.size());
			
			for (BRI_IB_Record briIBRecord : results) {
				records.add(briIBRecord);
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
		CommonUtil.logDebug(CLASS_NAME, "Check for duplicate fund in");
		FundInData fundInData = new FundInData();
		
		BRI_IB_Record currentRecord = null;
		BRI_IB_Record otherRecord = null;
		String currentReferenceNo = null;
		String otherReferenceNo = null;
		
		for(int i = 0 ; i < fundInList.size(); i++){
			currentRecord = (BRI_IB_Record) fundInList.get(i);
			
			fundInList.remove(i);
			i = -1;
			
			currentReferenceNo = currentRecord.getBillReferenceNo();
			
			for(int j = 0 ; j < fundInList.size(); j++){
				otherRecord = (BRI_IB_Record) fundInList.get(j);
				
				otherReferenceNo = otherRecord.getBillReferenceNo();
				
				if(currentReferenceNo.equals(otherReferenceNo)){
					CommonUtil.logDebug(CLASS_NAME, "Duplicate Fund In with Bill Reference No " + currentReferenceNo);
					fundInList.remove(j);
					j = 0;
					
					currentRecord.setAmount(currentRecord.getAmount() + otherRecord.getAmount());
					currentRecord.setBankFee(currentRecord.getBankFee() + otherRecord.getBankFee());
				}
			}
			
			fundInData.getFundInList().add(currentRecord);
		}
		
		return fundInData;
	}
	
	public FinArFundsInReconRecord processEachFundIn(PojoInterface pojo, FinArFundsInReport finArFundsInReport) throws ParseException{
		FinArFundsInReconRecord fundInRecon = null;
		
		BRI_IB_Record rec = (BRI_IB_Record) pojo;
		
		String billReferenceNo = rec.getBillReferenceNo();
		
		BigDecimal paymentAmount = new BigDecimal(rec.getAmount());
		BigDecimal bankFee = new BigDecimal(rec.getBankFee());
		
		if(!isFundInOkToContinue(billReferenceNo, "", paymentAmount, REPORT_TYPE)) {
			return null;
		}
		
		VenOrder order = getOrderByRelatedPayment(billReferenceNo, paymentAmount, REPORT_TYPE);
		
		if(isInternetBankingFundInAlreadyExist(billReferenceNo, REPORT_TYPE.id())){
			order = null;
		}
		
		if(order != null && !isOrderPaymentExist(billReferenceNo, paymentAmount, REPORT_TYPE)){
			CommonUtil.logDebug(CLASS_NAME, "Payments were found in the import file that do not exist in the payment schedule in VENICE:" + billReferenceNo);
			return null;
		}
		
		if(order != null){
			VenOrderPaymentAllocation orderPaymentAllocation 
				= getPaymentAllocationByRelatedPayment(billReferenceNo, 
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
			fundInRecon.setNomorReff(billReferenceNo);
		}
		
		fundInRecon.setFinArFundsInReport(finArFundsInReport);
		BigDecimal feeAmount = fundInRecon.getProviderReportFeeAmount()!=null?fundInRecon.getProviderReportFeeAmount(): new BigDecimal(0);
		BigDecimal paidAmount = fundInRecon.getProviderReportPaidAmount()!=null?fundInRecon.getProviderReportPaidAmount(): new BigDecimal(0);
		fundInRecon.setProviderReportFeeAmount(feeAmount.add(bankFee));
		fundInRecon.setProviderReportPaidAmount(paidAmount.add(paymentAmount));		
		fundInRecon.setProviderReportPaymentId(billReferenceNo);
		fundInRecon.setReconcilliationRecordTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
		fundInRecon.setProviderReportPaymentDate(new java.sql.Timestamp(rec.getTransactionDateTime().getTime()));
		fundInRecon.setRefundAmount(new BigDecimal(0));
		
		fundInRecon.setFinArReconResult(getReconResult(fundInRecon));
		
		return fundInRecon;
	}

}
