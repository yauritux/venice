package com.gdn.venice.facade.spring.fraud.rule;

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
public class Rule3ImplTest {
	
	Rule3Impl sut;
	
	@Before
	public void setup(){
		sut = new Rule3Impl();
	}
	
	@Test
	public void isCategoryAlreadyCalculated_listContainsCategorySpecified_returnsTrue(){
		
		List<String> calculatedCategoryList = new ArrayList<String>();
		calculatedCategoryList.add("cat1");
		calculatedCategoryList.add("cat2");
		calculatedCategoryList.add("cat3");
		
		boolean result = sut.isCategoryAlreadyCalculated(calculatedCategoryList, "cat2");
		
		assertTrue(result);
	}
	
	@Test
	public void isCategoryAlreadyCalculated_listDoesNotContainsCategorySpecified_returnsFalse(){
		
		List<String> calculatedCategoryList = new ArrayList<String>();
		calculatedCategoryList.add("cat1");
		calculatedCategoryList.add("cat2");
		calculatedCategoryList.add("cat3");
		
		boolean result = sut.isCategoryAlreadyCalculated(calculatedCategoryList, "cat4");
		
		assertFalse(result);
	}
	
	@After
	public void shutdown(){
		sut = null;
	}
	
}