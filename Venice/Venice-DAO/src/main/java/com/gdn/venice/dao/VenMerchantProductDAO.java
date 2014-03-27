package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 *
 */
public interface VenMerchantProductDAO extends JpaRepository<VenMerchantProduct, Long>{
	public static final String VEN_PRODUCT_CATEGORY_PRODUCT_CATEGORY_ELECTRONIC = "Electronic";
	public static final String VEN_PRODUCT_CATEGORY_PRODUCT_CATEGORY_HANDPHONE = "Handphones";
	public static final String VEN_PRODUCT_CATEGORY_LEVEL_ONE = "1";
	
	public static final String FIND_SLOW_MOVING_PRODUCT = 
		"SELECT o " +
		"FROM VenMerchantProduct o " +
		"JOIN FETCH o.venProductCategories oi " +
		"WHERE o.productId in(?1) " +
		"AND (oi.productCategory not like ('"+VEN_PRODUCT_CATEGORY_PRODUCT_CATEGORY_ELECTRONIC+"%') " +
		"AND oi.productCategory not like ('"+VEN_PRODUCT_CATEGORY_PRODUCT_CATEGORY_HANDPHONE+"%')) " +
		"AND oi.level = "+VEN_PRODUCT_CATEGORY_LEVEL_ONE;

	public List<VenMerchantProduct> findByWcsProductSku(String wcsProductSku);
	
	@Query(FIND_SLOW_MOVING_PRODUCT)
	public List<VenMerchantProduct> findSlowMovingProduct(String productIds);
}
