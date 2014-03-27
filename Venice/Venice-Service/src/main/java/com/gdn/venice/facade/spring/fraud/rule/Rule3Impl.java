package com.gdn.venice.facade.spring.fraud.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule3DAO;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.FrdParameterRule3;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenProductCategory;
import com.gdn.venice.util.CommonUtil;

/**
 * Order that include several of the same time
 */
@Service("Rule3")
public class Rule3Impl implements Rule {
	private static final String CLASS_NAME = Rule3Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderItemDAO orderItemDAO;
	@Autowired
	VenMerchantProductDAO merchantProductDAO;
	@Autowired
	FrdParameterRule3DAO frdParameterRule3DAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		int totalRiskPoint = 0;
		String category=null;
		List<String> calculatedCategoryList = new ArrayList<String>();
		
		List<VenOrderItem> orderItemList = orderItemDAO.findWithVenMerchantProductVenProductCategoryByVenOrder(order);
		
		for (VenOrderItem venOrderItem : orderItemList) {
			CommonUtil.logInfo(CLASS_NAME, "Order Item : " + venOrderItem.getWcsOrderItemId());
			
			List<VenProductCategory> productCategoryList = venOrderItem.getVenMerchantProduct().getVenProductCategories();
			
			for (VenProductCategory productCategory : productCategoryList) {
				if(isCategoryLevel2(productCategory) && 
				   !isCategoryAlreadyCalculated(calculatedCategoryList, productCategory.getProductCategory())){
					
					category = sanitizeCategory(productCategory.getProductCategory());
					
					FrdParameterRule3 fraudRule = frdParameterRule3DAO
														.findByUpperCasedCategoryAndQuantityMoreThanMinQTY(category.toUpperCase(), 
																                                           venOrderItem.getQuantity());
					
					totalRiskPoint += getRiskPoint(fraudRule);
					calculatedCategoryList.add(productCategory.getProductCategory());

					CommonUtil.logInfo(CLASS_NAME, "Category : " + category + ", Quantity : " + venOrderItem.getQuantity() + ", Risk Point : " + getRiskPoint(fraudRule));
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
	
	public int getRiskPoint(FrdParameterRule3 rule){
		if(rule == null) return 0;
		return rule.getRiskPoint();
	}
	
}