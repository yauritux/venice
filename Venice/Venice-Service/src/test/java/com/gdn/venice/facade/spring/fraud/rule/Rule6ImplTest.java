package com.gdn.venice.facade.spring.fraud.rule;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class Rule6ImplTest {
	
	Rule6Impl sut;
	
	@Before
	public void setup(){
		sut = new Rule6Impl();
		CommonUtil.veniceEnv = VeniceEnvironment.TESTING;
	}
	
	@Test
	public void isOrderItemAddressForeignCountry_countryCodeNotID_returnsTrue(){
		
		boolean result = sut.isOrderItemAddressForeignCountry("");
		
		assertTrue(result);
	}
	
	@Test
	public void isOrderItemAddressForeignCountry_countryCodeID_returnsFalse(){
		
		boolean result = sut.isOrderItemAddressForeignCountry("ID");
		
		assertFalse(result);
	}
	
	@After
	public void shutdown(){
		CommonUtil.veniceEnv = VeniceEnvironment.PRODUCTION;
		sut = null;
	}
	
}
