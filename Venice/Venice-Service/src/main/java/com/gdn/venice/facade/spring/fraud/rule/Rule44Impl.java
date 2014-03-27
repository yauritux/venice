package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule44DAO;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.CommonUtil;

/**
 * Slow Moving Category
 */
@Service("Rule44")
public class Rule44Impl implements Rule {
	private static final String CLASS_NAME = Rule44Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule44DAO frdParameterRule44DAO;
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	@Autowired
	VenMerchantProductDAO venMerchantProductDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		int riskPoint = getRiskPointValue();
		
		List<VenOrderItem> orderItemList = getOrderItemListByOrder(order);
		List<VenMerchantProduct> slowMovingProductList = getSlowMovingProductList(orderItemList);
		
		if(orderItemList.size() > 0){
			if(isAllItemSlowMovingCategory(slowMovingProductList, orderItemList)){
				totalRiskPoint = riskPoint;
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPointValue(){
		return frdParameterRule44DAO.findByDescription("Risk Point").getValue();
	}
	
	public List<VenOrderItem> getOrderItemListByOrder(VenOrder order){
		return venOrderItemDAO.findByVenOrder(order);
	}
	
	public List<VenMerchantProduct> getSlowMovingProductList(List<VenOrderItem> orderItemList){
		String productIds = "";
		
		for(int i=0; i < orderItemList.size();i++){	    			
			if(i==0)
				productIds=orderItemList.get(i).getVenMerchantProduct().getProductId().toString();
			else
				productIds=productIds+","+orderItemList.get(i).getVenMerchantProduct().getProductId();
		}	
		
		return venMerchantProductDAO.findSlowMovingProduct(productIds);
	}
	
	public boolean isAllItemSlowMovingCategory(List<VenMerchantProduct> slowMovingCategories, List<VenOrderItem> orderItemList){
		if(slowMovingCategories.size() == orderItemList.size())
			return true;
		else
			return false;
	}
		
}