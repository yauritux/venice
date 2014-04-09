package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenMerchantProduct;

/**
 * 
 * @author yauritux
 *
 */
public interface MerchantProductService {
	
	public VenMerchantProduct synchronizeVenMerchantProductData(
			VenMerchantProduct venMerchantProduct) throws VeniceInternalException;
	public VenMerchantProduct synchronizeVenMerchantProductReferenceData(
			VenMerchantProduct venMerchantProduct) throws VeniceInternalException;	
	public List<VenMerchantProduct> findByWcsProductSku(String wcsProductSku);
}
