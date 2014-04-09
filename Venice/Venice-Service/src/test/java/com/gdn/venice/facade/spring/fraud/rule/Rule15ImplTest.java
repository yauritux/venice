package com.gdn.venice.facade.spring.fraud.rule;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.dao.VenBinCreditLimitEstimateDAO;
import com.gdn.venice.persistence.VenBinCreditLimitEstimate;

@RunWith(MockitoJUnitRunner.class)
public class Rule15ImplTest {
	
	Rule15Impl sut;
	@Mock
	VenBinCreditLimitEstimateDAO venBinCreditLimitEstimateDAOMock;
	
	@Before
	public void setup(){
		sut = new Rule15Impl();
		sut.venBinCreditLimitEstimateDAO = venBinCreditLimitEstimateDAOMock;
	}
	
	@Test
	public void isBinNumberNotRegistered_notRegisteredBinSpecified_returnsTrue(){
		when(venBinCreditLimitEstimateDAOMock.findByActiveAndBinNumber(anyString())).thenReturn(null);
		
		boolean result = sut.isBinNumberNotRegistered("1234");
		
		assertTrue(result);
	}
	
	@Test
	public void isBinNumberNotRegistered_registeredBinSpecified_returnsFalse(){
		when(venBinCreditLimitEstimateDAOMock.findByActiveAndBinNumber(anyString())).thenReturn(new VenBinCreditLimitEstimate());
		
		boolean result = sut.isBinNumberNotRegistered("1234");
		
		assertFalse(result);
	}
	
	@After
	public void shutdown(){
		sut = null;
		venBinCreditLimitEstimateDAOMock = null;
	}
	
}