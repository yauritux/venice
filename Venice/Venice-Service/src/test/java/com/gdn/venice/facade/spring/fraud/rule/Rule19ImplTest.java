package com.gdn.venice.facade.spring.fraud.rule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Rule19ImplTest {
	private final static String FULL_MATCH = "full";
	private final static String TRUE_MATCH = "true";
	private final static String FALSE_MATCH = "false";
	
	Rule19Impl sut;
	
	@Before
	public void setup(){
		sut = new Rule19Impl();
	}
	
	@Test
	public void determineAddressesSimilarity_allAddressAreSame_returnsFull(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(orderAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(orderAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(FULL_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_onlyOrderAddressAndItemAddressAreSame_returnsFullString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		String differentAddress = "Jalan KS Tubun No. 57";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(orderAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(differentAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(TRUE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_onlyItemAddressAndPaymentAddressAreSame_returnsTrueString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		String differentAddress = "Jalan KS Tubun No. 57";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(differentAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(differentAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(TRUE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_onlyOrderAddressAndPaymentAddressAreSame_returnsTrueString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		String differentAddress = "Jalan KS Tubun No. 57";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(differentAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(orderAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(TRUE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_allAddressAreDifferent_returnsFalseString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		String differentAddress = "Jalan KS Tubun No. 57";
		String anotherAddress = "Jalan KS Tubun No. 1";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(differentAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(anotherAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(FALSE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_allAddressAreSameWithMultipleItemAddressAndSinglePaymentAddress_returnsTrueString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(orderAddress);
		itemAddressList.add(orderAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(orderAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(TRUE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_allAddressAreSameWithSingleItemAddressAndMultiplePaymentAddress_returnsTrueString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(orderAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(orderAddress);
		paymentAddressList.add(orderAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(TRUE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_onlyOrderAddressAndItemAddressAreSameWithMultipleItemAddressAndSinglePaymentAddress_returnsTrueString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		String differentAddress = "Jalan KS Tubun No. 57";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(orderAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(differentAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(TRUE_MATCH, result);
	}
	
	@Test
	public void determineAddressesSimilarity_allAddressAreDifferentWithMultipleItemAddressAndSinglePaymentAddress_returnsTrueString(){
		String orderAddress = "Jalan KS Tubun No. 2c/8";
		String differentAddress = "Jalan KS Tubun No. 57";
		String anotherAddress = "Jalan KS Tubun No. 1";
		
		List<String> itemAddressList = new ArrayList<String>();
		itemAddressList.add(differentAddress);
		itemAddressList.add(differentAddress);
		
		List<String> paymentAddressList = new ArrayList<String>();
		paymentAddressList.add(anotherAddress);
		
		String result = sut.determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		assertEquals(FALSE_MATCH, result);
	}
	
	
	@After
	public void shutdown(){
		sut = null;
	}
	
}