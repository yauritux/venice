package com.gdn.venice.facade.spring.fraud.rule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VenCustomerUserTypeConstants;
import com.gdn.venice.persistence.VenCustomer;

@RunWith(MockitoJUnitRunner.class)
public class Rule1ImplTest {
	
	Rule1Impl sut;
	
	@Before
	public void setup(){
		sut = new Rule1Impl();
	}
	
	@Test
	public void isCustomerRegisteredUser_customerWithUserTypeR_returnsTrue() {
		boolean result = sut.isCustomerRegisteredUser(getRegisteredCustomer());
		
		assertTrue(result);
	}
	
	
	private VenCustomer getRegisteredCustomer(){
		VenCustomer customer = new VenCustomer();
		customer.setUserType("R");
		
		return customer;
	}
	
	@Test
	public void isCustomerRegisteredUser_customerWithUserTypeNull_returnsFalse() {
		boolean result = sut.isCustomerRegisteredUser(getUnregisteredCustomer());
		
		assertFalse(result);
	}
	
	
	private VenCustomer getUnregisteredCustomer(){
		VenCustomer customer = new VenCustomer();
		
		return customer;
	}
	
	@Test
	public void getCustomerType_customerWithUserTypeR_returnsCustomerTypeConstantRegistered(){
		VenCustomerUserTypeConstants result = sut.getCustomerType(getRegisteredCustomer());
		
		assertEquals(VenCustomerUserTypeConstants.USER_TYPE_REGISTERED, result);
	}
	
	@Test
	public void getCustomerType_customerWithUserTypeNull_returnsCustomerTypeConstantUnregistered(){
		VenCustomerUserTypeConstants result = sut.getCustomerType(getUnregisteredCustomer());
		
		assertEquals(VenCustomerUserTypeConstants.USER_TYPE_UNREGISTERED, result);
	}
	
	@After
	public void shutdown(){
		sut = null;
	}
	
}
