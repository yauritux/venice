package com.gdn.venice.facade.spring.finance.fundin;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.constants.FinArReconResultConstants;
import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.dao.FinArFundsInReportDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.finance.dataexportimport.BCA_CC_Record;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class BCACCFundInServiceImplTest {

	@Mock
	FinArFundsInReportDAO finArFundsInReportDAOMock;
	@Mock
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAOMock;
	
	private String fileNameAndFullPath = "";
	
	private BCACCFundInServiceImpl sut;
	
	VeniceEnvironment veniceEnv;
	
	@Before
	public void setup(){
		veniceEnv = VeniceEnvironment.TESTING;
		CommonUtil.veniceEnv = veniceEnv;
		BCACCFundInServiceImpl bca = new BCACCFundInServiceImpl();
		sut = spy(bca);
		sut.finArFundsInReportDAO = finArFundsInReportDAOMock;
		sut.venOrderPaymentAllocationDAO = venOrderPaymentAllocationDAOMock;
	}
	
	@Test
	public void isFileAlreadyUploaded_newFileUploaded_returnsFalse() throws NoSuchAlgorithmException, IOException{
		doReturn("1234").when(sut).constructUniqueId(anyString(), any(FinArFundsInReportTypeConstants.class));
		when(finArFundsInReportDAOMock.countByReportDesc(anyString())).thenReturn(0);
		
		boolean result = sut.isFileAlreadyUploaded(fileNameAndFullPath, FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC);
		
		assertFalse(result);
	}
	
	@Test
	public void isFileAlreadyUploaded_fileAlreadyUploaded_returnsTrue() throws NoSuchAlgorithmException, IOException{
		doReturn("1234").when(sut).constructUniqueId(anyString(), any(FinArFundsInReportTypeConstants.class));
		when(finArFundsInReportDAOMock.countByReportDesc(anyString())).thenReturn(1);
		
		boolean result = sut.isFileAlreadyUploaded(fileNameAndFullPath, FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC);
		
		assertTrue(result);
	}
	
	@Test
	public void mergeDuplicate_5FundInWith3DuplicateVoidFundIn_returns1VoidFundIn(){
		ArrayList<PojoInterface> fundInList = get5FundInWith3DuplicateVoidFundIn();
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(1, fundInData.getVoidFundInList().size());
	}
	
	@Test
	public void mergeDuplicate_5FundInWith3DuplicateVoidFundIn_returns2FundIn(){
		ArrayList<PojoInterface> fundInList = get5FundInWith3DuplicateVoidFundIn();
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(2, fundInData.getFundInList().size());
	}
	
	@Test
	public void mergeDuplicate_5FundInWith3DuplicateRefundFundIn_returns1RefundFundIn(){
		ArrayList<PojoInterface> fundInList = get5FundInWith3DuplicateRefundFundIn();
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(1, fundInData.getRefundFundInList().size());
	}
	
	@Test
	public void mergeDuplicate_5FundInWith3DuplicateRefundFundIn_returns2FundIn(){
		ArrayList<PojoInterface> fundInList = get5FundInWith3DuplicateRefundFundIn();
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(2, fundInData.getFundInList().size());
	}
	
	@Test
	public void mergeDuplicate_8FundInWith6DuplicateRefundFundIn_returns2FundIn(){
		ArrayList<PojoInterface> fundInList = get8FundInWith6DuplicateRefundFundIn();
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(3, fundInData.getFundInList().size());
	}
	
	@Test
	public void mergeDuplicate_1FundIn_returns1FundIn(){
		BCA_CC_Record fundIn1 = new BCA_CC_Record();
		fundIn1.setAuthCd("uniquefundin1");
		fundIn1.setGrossAmt("10000");
		fundIn1.setNettAmt("10000");
		fundIn1.setDiscAmt("0");
		
		ArrayList<PojoInterface> fundInList = new ArrayList<PojoInterface>(1);
		fundInList.add(fundIn1);
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(1, fundInData.getFundInList().size());
	}
	
	
	private ArrayList<PojoInterface> get5FundInWith3DuplicateVoidFundIn(){
		BCA_CC_Record duplicateFundIn1 = new BCA_CC_Record();
		duplicateFundIn1.setAuthCd("duplicate");
		duplicateFundIn1.setGrossAmt("10000");
		duplicateFundIn1.setNettAmt("10000");
		duplicateFundIn1.setDiscAmt("0");
		
		BCA_CC_Record fundIn1 = new BCA_CC_Record();
		fundIn1.setAuthCd("uniquefundin1");
		fundIn1.setGrossAmt("10000");
		fundIn1.setNettAmt("10000");
		fundIn1.setDiscAmt("0");
		
		BCA_CC_Record fundIn2 = new BCA_CC_Record();
		fundIn2.setAuthCd("uniquefundin2");
		fundIn2.setGrossAmt("12000");
		fundIn2.setNettAmt("12000");
		fundIn2.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn2 = new BCA_CC_Record();
		duplicateFundIn2.setAuthCd("duplicate");
		duplicateFundIn2.setGrossAmt("-5000");
		duplicateFundIn2.setNettAmt("-5000");
		duplicateFundIn2.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn3 = new BCA_CC_Record();
		duplicateFundIn3.setAuthCd("duplicate");
		duplicateFundIn3.setGrossAmt("-5000");
		duplicateFundIn3.setNettAmt("-5000");
		duplicateFundIn3.setDiscAmt("0");
		
		ArrayList<PojoInterface> fundInList = new ArrayList<PojoInterface>(5);
		fundInList.add(duplicateFundIn1);
		fundInList.add(fundIn1);
		fundInList.add(fundIn2);
		fundInList.add(duplicateFundIn2);
		fundInList.add(duplicateFundIn3);
		
		return fundInList;
	}
	
	private ArrayList<PojoInterface> get5FundInWith3DuplicateRefundFundIn(){
		BCA_CC_Record duplicateFundIn1 = new BCA_CC_Record();
		duplicateFundIn1.setAuthCd("duplicate");
		duplicateFundIn1.setGrossAmt("-10000");
		duplicateFundIn1.setNettAmt("-10000");
		duplicateFundIn1.setDiscAmt("0");
		
		BCA_CC_Record fundIn1 = new BCA_CC_Record();
		fundIn1.setAuthCd("uniquefundin1");
		fundIn1.setGrossAmt("10000");
		fundIn1.setNettAmt("10000");
		fundIn1.setDiscAmt("0");
		
		BCA_CC_Record fundIn2 = new BCA_CC_Record();
		fundIn2.setAuthCd("uniquefundin2");
		fundIn2.setGrossAmt("12000");
		fundIn2.setNettAmt("12000");
		fundIn2.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn2 = new BCA_CC_Record();
		duplicateFundIn2.setAuthCd("duplicate");
		duplicateFundIn2.setGrossAmt("-5000");
		duplicateFundIn2.setNettAmt("-5000");
		duplicateFundIn2.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn3 = new BCA_CC_Record();
		duplicateFundIn3.setAuthCd("duplicate");
		duplicateFundIn3.setGrossAmt("-5000");
		duplicateFundIn3.setNettAmt("-5000");
		duplicateFundIn3.setDiscAmt("0");
		
		ArrayList<PojoInterface> fundInList = new ArrayList<PojoInterface>(5);
		fundInList.add(duplicateFundIn1);
		fundInList.add(fundIn1);
		fundInList.add(fundIn2);
		fundInList.add(duplicateFundIn2);
		fundInList.add(duplicateFundIn3);
		
		return fundInList;
	}
	
	private ArrayList<PojoInterface> get8FundInWith6DuplicateRefundFundIn(){
		BCA_CC_Record duplicateFundIn1 = new BCA_CC_Record();
		duplicateFundIn1.setAuthCd("duplicate");
		duplicateFundIn1.setGrossAmt("10000");
		duplicateFundIn1.setNettAmt("10000");
		duplicateFundIn1.setDiscAmt("0");
		
		BCA_CC_Record fundIn1 = new BCA_CC_Record();
		fundIn1.setAuthCd("uniquefundin1");
		fundIn1.setGrossAmt("10000");
		fundIn1.setNettAmt("10000");
		fundIn1.setDiscAmt("0");
		
		BCA_CC_Record fundIn2 = new BCA_CC_Record();
		fundIn2.setAuthCd("uniquefundin2");
		fundIn2.setGrossAmt("12000");
		fundIn2.setNettAmt("12000");
		fundIn2.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn2 = new BCA_CC_Record();
		duplicateFundIn2.setAuthCd("duplicate");
		duplicateFundIn2.setGrossAmt("5000");
		duplicateFundIn2.setNettAmt("5000");
		duplicateFundIn2.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn3 = new BCA_CC_Record();
		duplicateFundIn3.setAuthCd("duplicate");
		duplicateFundIn3.setGrossAmt("5000");
		duplicateFundIn3.setNettAmt("5000");
		duplicateFundIn3.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn4 = new BCA_CC_Record();
		duplicateFundIn4.setAuthCd("duplicate");
		duplicateFundIn4.setGrossAmt("5000");
		duplicateFundIn4.setNettAmt("5000");
		duplicateFundIn4.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn5 = new BCA_CC_Record();
		duplicateFundIn5.setAuthCd("duplicate");
		duplicateFundIn5.setGrossAmt("5000");
		duplicateFundIn5.setNettAmt("5000");
		duplicateFundIn5.setDiscAmt("0");
		
		BCA_CC_Record duplicateFundIn6 = new BCA_CC_Record();
		duplicateFundIn6.setAuthCd("duplicate");
		duplicateFundIn6.setGrossAmt("5000");
		duplicateFundIn6.setNettAmt("5000");
		duplicateFundIn6.setDiscAmt("0");
		
		ArrayList<PojoInterface> fundInList = new ArrayList<PojoInterface>(8);
		fundInList.add(duplicateFundIn1);
		fundInList.add(fundIn1);
		fundInList.add(fundIn2);
		fundInList.add(duplicateFundIn2);
		fundInList.add(duplicateFundIn3);
		fundInList.add(duplicateFundIn4);
		fundInList.add(duplicateFundIn5);
		fundInList.add(duplicateFundIn6);
		
		return fundInList;
	}
	
	@Test
	public void convertListToCommaSeparated_5ElementsOfString_returnsCorrectString(){
		ArrayList<String> stringList = new ArrayList<String>(5);
		stringList.add("123");
		stringList.add("124");
		stringList.add("125");
		stringList.add("126");
		stringList.add("127");
		
		String string = sut.convertListToCommaSeparated(stringList);
		
		assertEquals("123, 124, 125, 126, 127", string);
	}
	
	@Test
	public void getReconResult_remainingBalanceIsZero_reconResultAllFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(0));
		fundInRecon.setWcsOrderId("1");
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getReconResult_remainingBalanceIs5000_reconResultAllFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(5000));
		fundInRecon.setWcsOrderId("1");
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getReconResult_remainingBalanceIs10000_reconResultPartialFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(10000));
		fundInRecon.setWcsOrderId("1");
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_PARTIAL.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getReconResult_remainingBalanceIsMinus5000AndWcsOrderIdNull_reconResultNotRecognizeFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(-5000));
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getReconResult_remainingBalanceIsMinus5000_reconResultAllFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(-5000));
		fundInRecon.setWcsOrderId("1");
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getReconResult_remainingBalanceIsPlus5000_reconResultAllFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(5000));
		fundInRecon.setWcsOrderId("1");
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getReconResult_remainingBalanceIsMinus10000_reconResultOverPaidFundIn(){
		FinArFundsInReconRecord fundInRecon = new FinArFundsInReconRecord();
		fundInRecon.setRemainingBalanceAmount(new BigDecimal(-10000));
		fundInRecon.setWcsOrderId("1");
		
		FinArReconResult reconResult = sut.getReconResult(fundInRecon);
		
		assertEquals(FinArReconResultConstants.FIN_AR_RECON_RESULT_OVERPAID.id(), reconResult.getReconResultId().longValue());
	}
	
	@Test
	public void getRemainingBalanceAfterPayment_paymentNotYetPaid_returns0() {
		
		FinArFundsInReconRecord fundInNotYetPaid = new FinArFundsInReconRecord();
		fundInNotYetPaid.setRemainingBalanceAmount(new BigDecimal("10000"));
		
		BigDecimal paymentAmount = new BigDecimal("10000");
		
		BigDecimal result = sut.getRemainingBalanceAfterPayment(fundInNotYetPaid, paymentAmount);
		
		assertEquals(new BigDecimal(0), result);
	}
	
	@Test
	public void getRemainingBalanceAfterPayment_paymentPaidPartialy_returns0() {
		
		FinArFundsInReconRecord fundInPaidPartialy = new FinArFundsInReconRecord();
		fundInPaidPartialy.setRemainingBalanceAmount(new BigDecimal("5000"));
		fundInPaidPartialy.setProviderReportPaidAmount(new BigDecimal("5000"));
		
		BigDecimal paymentAmount = new BigDecimal("5000");
		
		BigDecimal result = sut.getRemainingBalanceAfterPayment(fundInPaidPartialy, paymentAmount);
		
		assertEquals(new BigDecimal(0), result);
	}
	
	@Test
	public void getRemainingBalanceAfterPayment_overPaid_returnsExcessAmount() {
		
		FinArFundsInReconRecord fundInPaidPartialy = new FinArFundsInReconRecord();
		fundInPaidPartialy.setRemainingBalanceAmount(new BigDecimal("5000"));
		fundInPaidPartialy.setProviderReportPaidAmount(new BigDecimal("5000"));
		
		BigDecimal paymentAmount = new BigDecimal("10000");
		
		BigDecimal result = sut.getRemainingBalanceAfterPayment(fundInPaidPartialy, paymentAmount);
		
		assertEquals(new BigDecimal(-5000), result);
	}
	
	@After
	public void shutdown(){
		veniceEnv = VeniceEnvironment.PRODUCTION;
		CommonUtil.veniceEnv = veniceEnv;
		finArFundsInReportDAOMock = null;
		fileNameAndFullPath = null;
		sut = null;
	}
	
}
