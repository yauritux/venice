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

import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class Rule32ImplTest {
	
	Rule32Impl sut;
	
	@Before
	public void setup(){
		sut = new Rule32Impl();
		CommonUtil.veniceEnv = VeniceEnvironment.TESTING;
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
		CommonUtil.veniceEnv = VeniceEnvironment.PRODUCTION;
	}
	
}