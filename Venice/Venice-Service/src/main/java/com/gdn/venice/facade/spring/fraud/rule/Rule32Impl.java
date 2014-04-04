package com.gdn.venice.facade.spring.fraud.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule32DAO;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.FrdParameterRule32;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenProductCategory;
import com.gdn.venice.util.CommonUtil;

/**
 * Collection Blacklist  
 */
@Service("Rule32")
public class Rule32Impl implements Rule{
	private static final String CLASS_NAME = Rule32Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule32DAO frdParameterRule32DAO;
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	@Autowired
	VenMerchantProductDAO venMerchantProductDAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		int totalRiskPoint = 0;
		String category=null;
		List<String> calculatedCategoryList = new ArrayList<String>();
		
		List<VenOrderItem> orderItemList = venOrderItemDAO.findWithVenMerchantProductVenProductCategoryByVenOrder(order);
		
		for (VenOrderItem venOrderItem : orderItemList) {
			CommonUtil.logInfo(CLASS_NAME, "Order Item : " + venOrderItem.getWcsOrderItemId());
			
			List<VenProductCategory> productCategoryList = venOrderItem.getVenMerchantProduct().getVenProductCategories();
			
			for (VenProductCategory productCategory : productCategoryList) {
				if(isCategoryLevel2(productCategory) && 
				   !isCategoryAlreadyCalculated(calculatedCategoryList, productCategory.getProductCategory())){
					
					category = sanitizeCategory(productCategory.getProductCategory());
					
					FrdParameterRule32 fraudRule = frdParameterRule32DAO
														.findByUpperCaseProductType(category.trim().toUpperCase());
					
					totalRiskPoint += getRiskPoint(fraudRule);
					calculatedCategoryList.add(productCategory.getProductCategory());

					CommonUtil.logInfo(CLASS_NAME, "Category : " + category + ", Risk Point : " + getRiskPoint(fraudRule));
				}
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
		
	}
	
	public boolean isCategoryLevel2(VenProductCategory productCategory){
		return productCategory.getLevel().equals(2);
	}
	
	public boolean isCategoryAlreadyCalculated(List<String> calculatedCategoryList, String category){
		for(String calculatedCategory : calculatedCategoryList){
			if(calculatedCategory.equals(category)){
				CommonUtil.logInfo(CLASS_NAME, "Category already calculated : " + category + ", skip this");
				return true;
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Category not yet calculated : " + category);
		
		return false;
	}
	
	public String sanitizeCategory(String category){
		String [] categorySplit=null;
		
		if(category.contains("(")){
			categorySplit=category.split("\\(");
			category=categorySplit[0].trim();
		}
		
		return category;
	}
	
	public int getRiskPoint(FrdParameterRule32 rule){
		if(rule == null) return 0;
		return rule.getRiskPoint();
	}
		
}