package com.gdn.venice.facade.spring.finance.fundin;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.dto.FundInData;
import com.gdn.venice.finance.dataexportimport.Niaga_IB_Record;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class NiagaIBFundInServiceImplTest {

	NiagaIBFundInServiceImpl sut;
	VeniceEnvironment veniceEnv;
	
	@Before
	public void setup(){
		veniceEnv = VeniceEnvironment.TESTING;
		CommonUtil.veniceEnv = veniceEnv;
		
		sut = new NiagaIBFundInServiceImpl();
	}
	
	@Test
	public void mergeDuplicate_5FundInWith3DuplicateFundIn_returns3FundIn(){
		ArrayList<PojoInterface> fundInList = get5FundInWith3DuplicateFundIn();
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(3, fundInData.getFundInList().size());
	}
	
	@Test
	public void mergeDuplicate_1FundIn_returns1FundIn(){
		Niaga_IB_Record fundIn1 = new Niaga_IB_Record();
		fundIn1.setAccountNumber("uniquefundin1");
		fundIn1.setPaymentAmount(new Double(1000));
		fundIn1.setBankFee(new Double(10));
		
		ArrayList<PojoInterface> fundInList = new ArrayList<PojoInterface>(1);
		fundInList.add(fundIn1);
		
		FundInData fundInData = sut.mergeAndSumDuplicate(fundInList);
		
		assertEquals(1, fundInData.getFundInList().size());
	}
	
	
	private ArrayList<PojoInterface> get5FundInWith3DuplicateFundIn(){
		Niaga_IB_Record duplicateFundIn1 = new Niaga_IB_Record();
		duplicateFundIn1.setAccountNumber("duplicate");
		duplicateFundIn1.setPaymentAmount(new Double(1000));
		duplicateFundIn1.setBankFee(new Double(10));
		
		Niaga_IB_Record fundIn1 = new Niaga_IB_Record();
		fundIn1.setAccountNumber("unique1");
		fundIn1.setPaymentAmount(new Double(1500));
		fundIn1.setBankFee(new Double(10));
		
		Niaga_IB_Record fundIn2 = new Niaga_IB_Record();
		fundIn2.setAccountNumber("unique2");
		fundIn2.setPaymentAmount(new Double(4000));
		fundIn2.setBankFee(new Double(10));
		
		Niaga_IB_Record duplicateFundIn2 = new Niaga_IB_Record();
		duplicateFundIn2.setAccountNumber("duplicate");
		duplicateFundIn2.setPaymentAmount(new Double(2000));
		duplicateFundIn2.setBankFee(new Double(10));
		
		Niaga_IB_Record duplicateFundIn3 = new Niaga_IB_Record();
		duplicateFundIn3.setAccountNumber("duplicate");
		duplicateFundIn3.setPaymentAmount(new Double(3000));
		duplicateFundIn3.setBankFee(new Double(10));
		
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
		sut = null;
	}
	
}
