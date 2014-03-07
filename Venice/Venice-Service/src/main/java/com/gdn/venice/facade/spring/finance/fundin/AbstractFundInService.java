package com.gdn.venice.facade.spring.finance.fundin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.gdn.venice.constants.FinArFundsInReportTimeConstants;
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.constants.FinArReconResultConstants;
import com.gdn.venice.dao.FinArFundsInReconRecordDAO;
import com.gdn.venice.dao.FinArFundsInReportDAO;
import com.gdn.venice.dao.FinPeriodDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.dao.VenOrderPaymentDAO;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.exception.FundInFileParserException;
import com.gdn.venice.exception.FundInNoFinancePeriodFoundException;
import com.gdn.venice.exportimport.finance.dataimport.BCA_IB_FileReader;
import com.gdn.venice.exportimport.finance.dataimport.BRI_IB_FileReader;
import com.gdn.venice.exportimport.finance.dataimport.MT942_FileReader;
import com.gdn.venice.finance.dataexportimport.BCA_CC_Record;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinArFundsIdReportTime;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.FinArFundsInReportType;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.persistence.FinPeriod;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.VeniceConstants;

public abstract class AbstractFundInService implements FundInService{
	
	public static final SimpleDateFormat SDF_yyyyMMdd_HHmmss = new SimpleDateFormat("yyyyMMdd HHmmss");
	public static final SimpleDateFormat SDF_yyyyMMdd_HHmmss2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat SDF_dd_MMM_yyyy = new SimpleDateFormat("dd-MMM-yyyy");
	
	@Autowired
	VenOrderDAO venOrderDAO;
	@Autowired
	FinArFundsInReportDAO finArFundsInReportDAO;
	@Autowired
	FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	@Autowired
	VenOrderPaymentDAO venOrderPaymentDAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	@Autowired
	FinPeriodDAO finPeriodDAO;
	
	public abstract ArrayList<PojoInterface> parse(String fileNameAndFullPath) throws FundInFileParserException;
	public abstract FundInData mergeAndSumDuplicate(ArrayList<PojoInterface> fundInList);
	
	public boolean isFileAlreadyUploaded(String fileNameAndFullPath, FinArFundsInReportTypeConstants reportType) throws NoSuchAlgorithmException{
		String uniqueId = constructUniqueId(fileNameAndFullPath, reportType);
		
		if(uniqueId == null) 
			return true;
		
		int result = finArFundsInReportDAO.countByReportDesc(uniqueId);
		
		return (result > 0);
	}
	
	public FinArFundsInReport createFundInReportRecord(FinArFundsInReportTypeConstants reportType,String fileNameAndFullPath, String userName) throws IOException, FundInNoFinancePeriodFoundException{
		FinPeriod finPeriod = finPeriodDAO.findCurrentPeriod();
		
		if(finPeriod == null)
			throw new FundInNoFinancePeriodFoundException("No finance period found for the current system date");
		
		FinArFundsInReport finArFundsInReport = new FinArFundsInReport();
		
		finArFundsInReport.setFileNameAndLocation(fileNameAndFullPath);
		FinArFundsInReportType finArFundsInReportType = new FinArFundsInReportType();
		finArFundsInReportType.setPaymentReportTypeId(reportType.id());
		finArFundsInReport.setFinArFundsInReportType(finArFundsInReportType);
		finArFundsInReport.setFinPeriod(finPeriod);
		
		finArFundsInReport.setReportDesc(constructUniqueId(fileNameAndFullPath, reportType));
		finArFundsInReport.setReportTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
		finArFundsInReport.setUserLogonName(userName != null ? userName : "System");
		
		return finArFundsInReportDAO.save(finArFundsInReport);
	}
	
	public void removeFundInReportRecord(FinArFundsInReport finArFundsInReport){
		finArFundsInReportDAO.delete(finArFundsInReport);
	}
	
	
	public VenOrderPayment getRelatedPayment(String referenceId, BigDecimal paymentAmount, FinArFundsInReportTypeConstants reportType){
		VenOrderPayment orderPayment = null;
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC){
			orderPayment = venOrderPaymentDAO.findByReferenceIdAndAmount(referenceId, paymentAmount);
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB){			
			
			orderPayment = venOrderPaymentDAO.findWithBankByReferenceIdAndBankId(referenceId, getBankId(reportType));
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB){
			orderPayment = venOrderPaymentDAO.findByReferenceId(referenceId);
		}
		
		return orderPayment;
	}
	
	public long getBankId(FinArFundsInReportTypeConstants reportType){
		switch (reportType) {
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB:
				return VeniceConstants.VEN_BANK_ID_BCA;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB:
				return VeniceConstants.VEN_BANK_ID_BRI;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB:
				return VeniceConstants.VEN_BANK_ID_BCA;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB:
				return VeniceConstants.VEN_BANK_ID_MANDIRI;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB:
				return VeniceConstants.VEN_BANK_ID_NIAGA;
			default:
				return -1;
		}
	}
	
	public boolean isOrderPaymentExist(String referenceId, BigDecimal paymentAmount, FinArFundsInReportTypeConstants reportType){
		VenOrderPayment orderPayment = getRelatedPayment(referenceId, paymentAmount, reportType);
		
		return (orderPayment != null);
	}
	
	public VenOrderPaymentAllocation getPaymentAllocationByRelatedPayment(String referenceId, BigDecimal paymentAmount, FinArFundsInReportTypeConstants reportType){
		VenOrderPaymentAllocation orderPaymentAllocation = null;
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC){
			
			List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findWithVenOrderPaymentFinArFundsInReconRecordByCreditCardDetail(referenceId, paymentAmount);
			orderPaymentAllocation = orderPaymentAllocationList.size() > 0 ? orderPaymentAllocationList.get(0) : null;
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB){			
					
			List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findWithVenOrderPaymentFinArFundsInReconRecordByInternetBankingDetail(referenceId);
			orderPaymentAllocation = orderPaymentAllocationList.size() > 0 ? orderPaymentAllocationList.get(0) : null;
			
			if(orderPaymentAllocation == null){
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"A record in the report being processed contains an order id that has no corresponding order record in the Venice database:" + referenceId);
			}
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB){			
							
			List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findWithVenOrderPaymentFinArFundsInReconRecordByPaymentReferenceId(referenceId);
			orderPaymentAllocation = orderPaymentAllocationList.size() > 0 ? orderPaymentAllocationList.get(0) : null;
			
			if(orderPaymentAllocation == null){
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"A record in the report being processed contains an order id that has no corresponding order record in the Venice database:" + referenceId);
			}
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA){			
							
			List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findWithVenOrderPaymentFinArFundsInReconRecordByVADetail(referenceId);
			orderPaymentAllocation = orderPaymentAllocationList.size() > 0 ? orderPaymentAllocationList.get(0) : null;
			
			if(orderPaymentAllocation == null){
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"A record in the report being processed contains an order id that has no corresponding order record in the Venice database:" + referenceId);
			}
		}
		
		return orderPaymentAllocation;
	}
	
	public VenOrder getOrderByRelatedPayment(String referenceId, BigDecimal paymentAmount, FinArFundsInReportTypeConstants reportType){
		VenOrderPaymentAllocation orderPaymentAllocation = getPaymentAllocationByRelatedPayment(referenceId, paymentAmount, reportType);
		
		if(orderPaymentAllocation == null){
			CommonUtil.logDebug(this.getClass().getCanonicalName(), 
					            "A record in the report being processed contains an reference Id that has no corresponding order/payment " + reportType + " record in the Venice database:" + referenceId);
			return null;
		}
		
		return venOrderDAO.findOne(orderPaymentAllocation.getVenOrder().getOrderId());
	}
	
	public boolean isPaymentAlreadyProcessed(String paymentIdentifier, String uniquePayment, BigDecimal paymentAmount, FinArFundsInReportTypeConstants reportType) throws ParseException{
		
		List<FinArFundsInReconRecord> fundInReconList = null;
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC){
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date uniquePaymentDate = df.parse(uniquePayment);
			
			fundInReconList = finArFundsInReconRecordDAO.findForCreditCardDetail(paymentIdentifier, paymentAmount, uniquePaymentDate);
			return (fundInReconList.size() > 0);
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB){			
			
			fundInReconList = finArFundsInReconRecordDAO.findForInternetBankingDetail(paymentIdentifier);
			return (fundInReconList.size() > 0);
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB){			
			fundInReconList = finArFundsInReconRecordDAO.findForInternetBankingDetail(paymentIdentifier);
			return (fundInReconList.size() > 0);
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA){
			
			fundInReconList = finArFundsInReconRecordDAO.findByNomorReffUniquePayment(paymentIdentifier, uniquePayment);
			return (fundInReconList.size() > 0);
		}
		
		return true;
	}
	
	public boolean isFundInOkToContinue(String paymentIdentifier, String uniquePayment, BigDecimal paymentAmount, FinArFundsInReportTypeConstants reportType) throws ParseException{
		if(isPaymentAlreadyProcessed(paymentIdentifier, uniquePayment, paymentAmount, reportType)){
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "This payment with reference "+ paymentIdentifier +" already been processed before");
			return false;
		}
		
		return true;
	}
	
	public BigDecimal getRemainingBalanceAfterPayment(FinArFundsInReconRecord fundInRecon, BigDecimal paymentAmount){
		BigDecimal totalPaidAmount = fundInRecon.getProviderReportPaidAmount();
		BigDecimal remainingBalanceAmount = fundInRecon.getRemainingBalanceAmount();
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "New Payment Total: "+ totalPaidAmount);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "Remaining Balance: "+ remainingBalanceAmount);
		
		if(totalPaidAmount == null){
			totalPaidAmount = paymentAmount;
		}else{
			totalPaidAmount = totalPaidAmount.add(paymentAmount);
		}
		
		remainingBalanceAmount = remainingBalanceAmount.subtract(totalPaidAmount);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "Paid Amount: "+ totalPaidAmount);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "Remaining Balance: "+ remainingBalanceAmount);
		
		return remainingBalanceAmount;
	}
	
	public FinArReconResult getReconResult(FinArFundsInReconRecord fundInRecon){
		FinArReconResult finArReconResult = new FinArReconResult();
		BigDecimal remainingBalanceAmount =fundInRecon.getRemainingBalanceAmount().abs().compareTo(VeniceConstants.TRACEHOLD_RECEIVED) <= 0 ? new BigDecimal(0):fundInRecon.getRemainingBalanceAmount();
		
		if (remainingBalanceAmount.compareTo(new BigDecimal(0)) == 0) {
			finArReconResult.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id());
		} else if (remainingBalanceAmount.compareTo(new BigDecimal(0)) > 0) {
			finArReconResult.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_PARTIAL.id());
		} else {
			finArReconResult.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_OVERPAID.id());
		}
		
		if(fundInRecon.getRemainingBalanceAmount().compareTo(new BigDecimal(0)) < 0 && (fundInRecon.getWcsOrderId()==null || fundInRecon.getWcsOrderId()=="")){
			finArReconResult.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id());
		}
		
		return finArReconResult;
	}
	
	public String getFinanceFundInReportTemplateFileNameAndFullPath(FinArFundsInReportTypeConstants reportType){
		StringBuffer sb = new StringBuffer();
		sb.append(System.getenv("VENICE_HOME"));
		sb.append("/files/template/");
		
		switch (reportType) {
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC:
				sb.append("BCA_CC_Record.xml");
				break;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC:
				sb.append("BCA_CC_Record.xml");
				break;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC:
				sb.append("BCA_CC_Record.xml");
				break;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB:
				sb.append("XL_IB_Record.xml");
				break;
			case FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC:
				sb.append("Mandiri_Installment_Record.xml");
				break;
		}
		
		return sb.toString();
	}
	
	public String constructUniqueId(String fileNameAndFullPath, FinArFundsInReportTypeConstants reportType){
		String uniqueContent = getUniqueContentFromFile(fileNameAndFullPath, reportType);
		
		if(uniqueContent == null)
			return null;
		
		StringBuffer reportDescSB = new StringBuffer();
		reportDescSB.append(getReportPrefix(reportType));
		reportDescSB.append("-");
		reportDescSB.append(uniqueContent);
		
		return reportDescSB.toString();
	}
	
	public String getUniqueContentFromFile(String fileNameAndFullPath, FinArFundsInReportTypeConstants reportType){
		String uniqueContent = null;
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC){
			
			uniqueContent = getUniqueContentFromCCFile(fileNameAndFullPath, reportType);
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB || 
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB){
			BCA_IB_FileReader reader = new BCA_IB_FileReader();
			try {
				uniqueContent = reader.getUniqueReportIdentifier(fileNameAndFullPath);
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), "Unable to retrieve unique content from file");
				e.printStackTrace();
			}
		}
			
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB){
			MT942_FileReader reader = new MT942_FileReader();
			try {
				uniqueContent = reader.getUniqueReportIdentifier(fileNameAndFullPath);
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), "Unable to retrieve unique content from file");
				e.printStackTrace();
			}
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB){
			BRI_IB_FileReader reader = new BRI_IB_FileReader();
			try {
				uniqueContent = reader.getUniqueReportIdentifier(fileNameAndFullPath);
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), "Unable to retrieve unique content from file");
				e.printStackTrace();
			}
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC){
			uniqueContent = new Timestamp(System.currentTimeMillis()).toString().replace(".", "");
		}
		
		if(reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA ||
		   reportType == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA){
			MT942_FileReader reader = new MT942_FileReader();
			try {
				String uniqueIds = reader.getUniqueReportIdentifier(fileNameAndFullPath);
				String[] uniqueIdSplit=uniqueIds.split("&");
				uniqueContent = uniqueIdSplit[0];
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), "Unable to retrieve unique content from file");
				e.printStackTrace();
			}
		}
			
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "Unique content for " + reportType + " : " + uniqueContent);
		
		return uniqueContent;
	}
	
	public String getUniqueContentFromCCFile(String fileNameAndFullPath, FinArFundsInReportTypeConstants reportType){
		ExcelToPojo excelToPojo = null;
    	
        try {
			excelToPojo = new ExcelToPojo(BCA_CC_Record.class, getFinanceFundInReportTemplateFileNameAndFullPath(reportType), fileNameAndFullPath, 0, 1);
			excelToPojo = excelToPojo.getPojo();
			
			ArrayList<PojoInterface> result = excelToPojo.getPojoResult();
			
			if(result.isEmpty()){
				return null;
			}
			
			return ((BCA_CC_Record)result.get(0)).getAuthCd() +" "+((BCA_CC_Record)result.get(0)).getTransDate()+" "+((BCA_CC_Record)result.get(0)).getTransTime();			
        } catch (Exception e) {
        	String errMsg = "Error parsing Excel File Processing row number:" + (excelToPojo != null && excelToPojo.getErrorRowNumber() != null?excelToPojo.getErrorRowNumber():"1");
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg);
			return null;
		}
	}
	
	public String getFileMD5Checksum(String fileNameAndFullPath) throws IOException{
		InputStream is = new FileInputStream(fileNameAndFullPath);
		
		return DigestUtils.md5Hex(IOUtils.toByteArray(is));
	}
	
	private String getReportPrefix(FinArFundsInReportTypeConstants reportType){
		switch (reportType) {
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC:
				return "BCA CC";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB:
				return "BCA IB";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA:
				return "BCA VA";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB:
				return "Mandiri IB";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA:
				return "Mandiri VA";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC:
				return "Mandiri Installment CC";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB:
				return "Niaga IB";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB:
				return "KlikPay IB";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC:
				return "KlikPay Full CC";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC:
				return "KlikPay Inst CC";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB:
				return "XL IB";
			case FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB:
				return "BRI IB";
		}
		
		return null;
	}
	
	public String add(String value1, String value2){
		return (new BigDecimal(value1).add(new BigDecimal(value2))).toString();
	}
	
	public int compare(String value1, String value2){
		return (new BigDecimal(value1).compareTo(new BigDecimal(value2)));
	}
	
	public String convertListToCommaSeparated(List<String> list){
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < list.size(); i++){
			sb.append(list.get(i));
			
			if(i < list.size() - 1)
				sb.append(", ");
		}
		
		return sb.toString();
		
	}
	
	public String constructSuccessMessage(FundInData fundInData){
		StringBuffer sb = new StringBuffer();
		sb.append("Report uploaded successfully... please refresh => New :");
		sb.append(fundInData.getProcessedFundInList().size());
		sb.append(" => void : ");
		sb.append(fundInData.getVoidFundInList().size());
		sb.append(" => Refund : ");
		sb.append(fundInData.getRefundFundInList().size());
		
		return sb.toString();
	}
	
	public boolean isInternetBankingFundInAlreadyExist(String referenceId, long reportTypeId){
		int total = finArFundsInReconRecordDAO.countByNomorReffAndPaymentReportTypeId(referenceId, reportTypeId);
		
		return (total > 0);
	}
	
	public boolean isPaymentAmountNotLessThanZero(BigDecimal paymentAmount){
		return !(paymentAmount.compareTo(new BigDecimal(0)) < 0);
	}
	
	public List<FinArFundsInReconRecord> getFundInReconForVAPayment(String paymentConfirmationNumber, String uniquePayment){
		List<FinArFundsInReconRecord> fundInReconList = finArFundsInReconRecordDAO
															.findByNomorReffUniquePaymentAndRealTimeId(paymentConfirmationNumber, 
																	                         uniquePayment);
		
		return fundInReconList;
	}
	
	public FinArFundsIdReportTime getFundsIdReportTime(boolean isNextDayPayment) {
		FinArFundsIdReportTime finArFundsIdReportTime = new FinArFundsIdReportTime();
		if(isNextDayPayment){
			finArFundsIdReportTime.setReportTimeId(FinArFundsInReportTimeConstants.FIN_AR_FUNDS_IN_REPORT_TIME_H1.id());
		}else{
			finArFundsIdReportTime.setReportTimeId(FinArFundsInReportTimeConstants.FIN_AR_FUNDS_IN_REPORT_TIME_REAL_TIME.id());
		}
		return finArFundsIdReportTime;
	}
	
}
