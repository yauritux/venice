package com.gdn.venice.facade.spring.fraud.rule;

import static junit.framework.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VenOrderPaymentConstants;
import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class Rule33ImplTest {
	
	Rule33Impl sut;
	
	@Before
	public void setup(){
		sut = new Rule33Impl();
		CommonUtil.veniceEnv = VeniceEnvironment.TESTING;
	}
	
	@Test
	public void getRiskPoint_paymentECommerceIndicator5_returnsMinus100(){
		int result = sut.getRiskPoint(VenOrderPaymentConstants.E_COMMERCE_INDICATOR_5.value());
		
		assertEquals(-100, result);
	}
	
	@Test
	public void getRiskPoint_paymentECommerceIndicator7_returns500(){
		int result = sut.getRiskPoint(VenOrderPaymentConstants.E_COMMERCE_INDICATOR_7.value());
		
		assertEquals(500, result);
	}
	
	@After
	public void shutdown(){
		sut = null;
		CommonUtil.veniceEnv = VeniceEnvironment.PRODUCTION;
	}
	
}