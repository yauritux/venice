package com.gdn.venice.facade.spring.finance.fundin;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.dao.FinArFundsInReportDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.finance.dataexportimport.BCA_CC_Record;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class KlikPayCCFundInServiceImplTest {

	@Mock
	FinArFundsInReportDAO finArFundsInReportDAOMock;
	@Mock
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAOMock;
	
	private KlikPayCCFundInServiceImpl sut;
	
	VeniceEnvironment veniceEnv;
	
	@Before
	public void setup(){
		veniceEnv = VeniceEnvironment.TESTING;
		CommonUtil.veniceEnv = veniceEnv;
		KlikPayCCFundInServiceImpl bca = new KlikPayCCFundInServiceImpl();
		sut = spy(bca);
		sut.finArFundsInReportDAO = finArFundsInReportDAOMock;
		sut.venOrderPaymentAllocationDAO = venOrderPaymentAllocationDAOMock;
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
	
	@After
	public void shutdown(){
		veniceEnv = VeniceEnvironment.PRODUCTION;
		CommonUtil.veniceEnv = veniceEnv;
		finArFundsInReportDAOMock = null;
		sut = null;;
	}
	
}
