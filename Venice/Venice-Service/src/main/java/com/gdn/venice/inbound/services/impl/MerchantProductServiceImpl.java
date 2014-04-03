package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenProductCategoryDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.facade.VenProductCategorySessionEJBLocal;
import com.gdn.venice.inbound.services.MerchantProductService;
import com.gdn.venice.inbound.services.MerchantService;
import com.gdn.venice.inbound.services.ProductTypeService;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenProductCategory;
import com.gdn.venice.persistence.VenProductType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class MerchantProductServiceImpl implements MerchantProductService {

	@Autowired
	private VenMerchantProductDAO venMerchantProductDAO;
	
	@Autowired
	private VenProductCategoryDAO venProductCategoryDAO;
	
	@Autowired
	private MerchantService merchantService;
	
	@Autowired
	private ProductTypeService productTypeService;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenMerchantProduct> synchronizeVenMerchantProductRefs(
			List<VenMerchantProduct> merchantProductRefs)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductRefs::BEGIN, merchantProductRefs = " + merchantProductRefs);
		
		List<VenMerchantProduct> synchronizedMerchantProductRefs = new ArrayList<VenMerchantProduct>();
		
		for (VenMerchantProduct merchantProduct : merchantProductRefs) {
			if (merchantProduct.getWcsProductSku() != null) {
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductRefs::Synchronizing VenMerchantProduct... :" 
				          + merchantProduct.getWcsProductSku());
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProduct merchant = "  + merchantProduct.getVenMerchant());				
				
				merchantProduct = synchronizeVenMerchantProductReferenceData(merchantProduct); // merchantProduct is in attach mode here
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProduct merchant after synchronized = "  + merchantProduct.getVenMerchant());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProduct merchant WCS Merchant ID = "  + merchantProduct.getVenMerchant().getWcsMerchantId());	
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProduct SKU = " + merchantProduct.getMerchantProductSku());
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProduct merchant ID = "  + merchantProduct.getVenMerchant().getMerchantId());
				List<VenMerchantProduct> merchantProductList = venMerchantProductDAO.findByWcsProductSku(merchantProduct.getWcsProductSku());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProductList = " 
								+ merchantProductList);
				
				VenMerchantProduct venMerchantProduct = null;
				
				if (merchantProductList == null || (merchantProductList.isEmpty())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::VenMerchantProduct is not listed in the database, saving it");
					
					if (!em.contains(merchantProduct)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenMerchantProductReferences::explicitly call save for venMerchantProduct");
						venMerchantProduct = venMerchantProductDAO.save(merchantProduct); //attach merchantProduct
					} else {
						venMerchantProduct = merchantProduct;
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::adding venMerchantProduct into synchronizedMerchantProductRefs");
					synchronizedMerchantProductRefs.add(venMerchantProduct);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::successfully added venMerchantProduct into synchronizedMerchantProductRefs");
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::venMerchantProduct is in attached mode, going to detach it");
					venMerchantProduct = merchantProductList.get(0);
					synchronizedMerchantProductRefs.add(venMerchantProduct);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::successfully added venMerchantProduct into synchronizedMerchantProductRefs");
				}
				
				venMerchantProduct.setVenProductCategories(synchronizeVenProductCategories(venMerchantProduct.getVenProductCategories()));
				
			}
		} //end of 'for'
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductRefs::EOM, returning synchronizedMerchantProductRefs = "
				  + synchronizedMerchantProductRefs.size());
		return synchronizedMerchantProductRefs;
	}
	
	/**
	 * 
	 * @param categoryList
	 * @return the synchronized data object
	 */
	public List<VenProductCategory> synchronizeVenProductCategories(List<VenProductCategory> categoryList){
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductCategories::BEGIN, categoryList = " + categoryList);
		
		List<VenProductCategory> syncronizedProductCategoryList = new ArrayList<VenProductCategory>(categoryList.size());
		
		for (VenProductCategory category : categoryList) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenProductCategories::find product category = " + category.getProductCategory());
			
			List<VenProductCategory> syncronizedProductCategories = venProductCategoryDAO.findByProductCategory(category.getProductCategory());
			
			if(syncronizedProductCategories.size() > 0){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenProductCategories::product category found = " + category.getProductCategory());
				syncronizedProductCategoryList.add(syncronizedProductCategories.get(0));
			}else{
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenProductCategories::product category persist = " + category.getProductCategory());
				
				VenProductCategory syncronizedProductCategory = venProductCategoryDAO.save(category);
				syncronizedProductCategoryList.add(syncronizedProductCategory);
			}
		}
		
		return syncronizedProductCategoryList;
	}
	
	/**
	 * Synchronizes the data for the direct VenMerchantProduct references
	 * 
	 * @param venMerchantProduct
	 * @return the synchronized data object
	 */	
	@Override
	public VenMerchantProduct synchronizeVenMerchantProductReferenceData(
			VenMerchantProduct venMerchantProduct) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductReferenceData::BEGIN, venMerchantProduct = " + venMerchantProduct);
		
		//if (venMerchantProduct.getVenProductType() != null) {
		    CommonUtil.logDebug(this.getClass().getCanonicalName()
		    		, "synchronizeVenMerchantProductReferenceData::merchantProduct product type = " + venMerchantProduct.getVenProductType());
			List<VenProductType> productTypeRefs = new ArrayList<VenProductType>();
			productTypeRefs.add(venMerchantProduct.getVenProductType());
			productTypeRefs = productTypeService.synchronizeVenProductTypeReferences(productTypeRefs);
			for (VenProductType productType : productTypeRefs) {
				venMerchantProduct.setVenProductType(productType);
			}
		//}
		
		//if (venMerchantProduct.getVenMerchant() != null) {
			List<VenMerchant> merchantRefs = new ArrayList<VenMerchant>();
			merchantRefs.add(venMerchantProduct.getVenMerchant());
			List<VenMerchant> synchronizedMerchants = null;
			synchronizedMerchants = merchantService.synchronizeVenMerchantReferences(merchantRefs);
			//merchantRefs = discuss with team whether method synchronized for this particular class should be implemented or not
			/*
			for (VenMerchant merchant : merchantRefs) {
				venMerchantProduct.setVenMerchant(merchant);
			}
			*/
			for (VenMerchant merchant : synchronizedMerchants) {
				venMerchantProduct.setVenMerchant(merchant);
			}
		//}
		
		/*
		List<Object> references = new ArrayList<Object>();
		references.add(venMerchantProduct.getVenProductType());
		references.add(venMerchantProduct.getVenMerchant());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenProductType) {
				venMerchantProduct.setVenProductType((VenProductType) next);
			}else if(next instanceof VenMerchant){
				venMerchantProduct.setVenMerchant((VenMerchant) next);
			}
		}
		*/
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductReferenceData::EOM, returning venMerchantProduct = "
				  + venMerchantProduct);
		return venMerchantProduct;
	}

	@Override
	public List<VenMerchantProduct> findByWcsProductSku(String wcsProductSku) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "findByWcsProductSku::BEGIN, find merchantProduct with wcsProductSku="+ wcsProductSku);
		if (wcsProductSku == null || wcsProductSku.length() == 0) {
			return null;
		}
		
		List<VenMerchantProduct> merchantProducts = venMerchantProductDAO.findByWcsProductSku(wcsProductSku);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "merchantProducts found = " + (merchantProducts != null ? merchantProducts.size() : 0));
		
		return merchantProducts;
	}

}
