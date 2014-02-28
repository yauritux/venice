package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenOrderItem;

/**
 * 
 * @author yauritux
 *
 */
public interface MerchantProductService {
	
	public List<VenMerchantProduct> synchronizeVenMerchantProductRefs(
			List<VenMerchantProduct> merchantProductRefs) throws VeniceInternalException;
	public VenMerchantProduct synchronizeVenMerchantProductReferenceData(
			VenMerchantProduct venMerchantProduct) throws VeniceInternalException;	
	public List<VenOrderItem> processMerchantProduct(List<String> merchantProduct
			, List<VenOrderItem> orderItems) throws VeniceInternalException;
}
