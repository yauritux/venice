package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenProductCategoryDAO;
import com.gdn.venice.exception.VenMerchantProductSynchronizingError;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.MerchantProductService;
import com.gdn.venice.inbound.services.MerchantService;
import com.gdn.venice.inbound.services.ProductCategoryService;
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
	private ProductCategoryService productCategoryService;

	@Autowired
	private ProductTypeService productTypeService;

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchantProduct synchronizeVenMerchantProductData(
			VenMerchantProduct venMerchantProduct) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenMerchantProductData::BEGIN, venMerchantProduct = "
						+ venMerchantProduct);

		VenMerchantProduct synchVenMerchantProduct = venMerchantProduct;
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductData::venMerchantProduct.productId=" + venMerchantProduct.getProductId());
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductData::venMerchantProduct.productCategories=" + venMerchantProduct.getVenProductCategories());
		
		try {
			if (venMerchantProduct != null && venMerchantProduct.getWcsProductSku() != null && venMerchantProduct.getProductId() == null) {

				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenMerchantProductData::Synchronizing VenMerchantProduct... :"
								+ venMerchantProduct.getWcsProductSku());

				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenMerchantProductData::merchantProduct merchant = "
								+ venMerchantProduct.getVenMerchant());

				List<VenMerchantProduct> merchantProductList = findByWcsProductSku(venMerchantProduct.getWcsProductSku());
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenMerchantProductData::found merchantProductList = "
								+ (merchantProductList != null ? merchantProductList.size() : 0));

				if (merchantProductList == null || (merchantProductList.isEmpty())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenMerchantProductData::VenMerchantProduct is not listed in the database, saving it");
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductData::first of all, synchronizing all VenMerchantProduct References");
					synchVenMerchantProduct = synchronizeVenMerchantProductReferenceData(venMerchantProduct);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductData::venMerchantProduct is being synchronized now");

					if (!em.contains(venMerchantProduct)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
										"synchronizeVenMerchantProductData::explicitly call save for venMerchantProduct");
						synchVenMerchantProduct = venMerchantProductDAO.save(venMerchantProduct); // attach merchantProduct
					} else {
						synchVenMerchantProduct = venMerchantProduct;
					}
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductData::VenMerchantProduct already exists, get it from the DB");
					synchVenMerchantProduct = merchantProductList.get(0);
				}

				//synchronize and persist venProductCategories
				Boolean isProductCategorySynchronized = synchronizeAndPersistVenProductCategories(venMerchantProduct, synchVenMerchantProduct);
				if (!isProductCategorySynchronized) {
					throw new VenMerchantProductSynchronizingError("Cannot synchronize VenMerchantProduct.productCategories!",
							VeniceExceptionConstants.VEN_EX_130007);
				}
				
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductData::venMerchantProduct already synchronized, no need to do it twice");
			}
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			CommonUtil.logAndReturnException(new VenMerchantProductSynchronizingError("Cannot synchronize VenMerchantProduct!-" + e.getMessage(),
							VeniceExceptionConstants.VEN_EX_130007), CommonUtil.getLogger(this.getClass().getCanonicalName()),
					LoggerLevel.ERROR);
		}

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenMerchantProductRefs::EOM, returning synchVenMerchantProduct = "
								+ synchVenMerchantProduct);
		
		return synchVenMerchantProduct;
	}

	public VenMerchantProduct synchronizeVenMerchantProductCategories(
			VenMerchantProduct merchantProductWithCategories) {
		VenMerchantProduct existingProduct = venMerchantProductDAO
				.findOne(merchantProductWithCategories.getProductId());

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenMerchantProductCategories::BEGIN, Product "
						+ existingProduct.getWcsProductName()
						+ " Category in database = "
						+ existingProduct.getVenProductCategories().size());

		if (existingProduct.getVenProductCategories().size() == 0) {

			for (VenProductCategory category : merchantProductWithCategories
					.getVenProductCategories()) {
				ArrayList<VenMerchantProduct> venMerchantProductList2 = new ArrayList<VenMerchantProduct>();
				if (category.getVenMerchantProducts() == null) {
					category.setVenMerchantProducts(venMerchantProductList2);
				}
				category.getVenMerchantProducts().add(
						merchantProductWithCategories);
			}

			for (VenProductCategory category : merchantProductWithCategories
					.getVenProductCategories()) {
				category = venProductCategoryDAO.save(category);
			}

			merchantProductWithCategories = venMerchantProductDAO
					.save(merchantProductWithCategories);

		}

		return merchantProductWithCategories;
	}

	/**
	 * 
	 * @param categoryList
	 * @return the synchronized data object
	 */
	public List<VenProductCategory> synchronizeVenProductCategories(
			List<VenProductCategory> categoryList) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenProductCategories::BEGIN, categoryList = "
						+ categoryList);

		List<VenProductCategory> syncronizedProductCategoryList = new ArrayList<VenProductCategory>(
				categoryList.size());

		for (VenProductCategory category : categoryList) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"synchronizeVenProductCategories::find product category = "
							+ category.getProductCategory());

			List<VenProductCategory> syncronizedProductCategories = venProductCategoryDAO
					.findByProductCategory(category.getProductCategory());

			if (syncronizedProductCategories.size() > 0) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenProductCategories::product category found = "
								+ category.getProductCategory());
				syncronizedProductCategoryList.add(syncronizedProductCategories
						.get(0));
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenProductCategories::product category persist = "
								+ category.getProductCategory());

				VenProductCategory syncronizedProductCategory = venProductCategoryDAO
						.save(category);
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
	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public VenMerchantProduct synchronizeVenMerchantProductReferenceData(
			VenMerchantProduct venMerchantProduct)
			throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenMerchantProductReferenceData::BEGIN, venMerchantProduct = "
						+ venMerchantProduct);

		// if (venMerchantProduct.getVenProductType() != null) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenMerchantProductReferenceData::merchantProduct product type = "
						+ venMerchantProduct.getVenProductType());
		VenProductType productType = venMerchantProduct.getVenProductType();
		VenProductType synchronizedProductType = productTypeService
				.synchronizeVenProductType(productType);
		venMerchantProduct.setVenProductType(synchronizedProductType);
		// }

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenMerchantProductReferenceData::venProductType is being synchronized");

		// if (venMerchantProduct.getVenMerchant() != null) {
		VenMerchant merchant = venMerchantProduct.getVenMerchant();
		VenMerchant synchronizedMerchant = merchantService.synchronizeVenMerchantData(merchant);
		venMerchantProduct.setVenMerchant(synchronizedMerchant);
		// }

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenMerchantProductReferenceData::EOM, returning venMerchantProduct = "
								+ venMerchantProduct);
		return venMerchantProduct;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<VenMerchantProduct> findByWcsProductSku(String wcsProductSku) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"findByWcsProductSku::BEGIN, find merchantProduct with wcsProductSku="
						+ wcsProductSku);
		if (wcsProductSku == null || wcsProductSku.length() == 0) {
			return null;
		}

		List<VenMerchantProduct> merchantProducts = venMerchantProductDAO
				.findByWcsProductSku(wcsProductSku);

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"merchantProducts found = "
						+ (merchantProducts != null ? merchantProducts.size()
								: 0));

		return merchantProducts;
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private Boolean synchronizeAndPersistVenProductCategories(VenMerchantProduct venMerchantProduct, VenMerchantProduct synchVenMerchantProduct) {
		
		try {
			if (venMerchantProduct.getVenProductCategories() != null && (!venMerchantProduct.getVenProductCategories().isEmpty())) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeAndPersistVenProductCategories::synchronizing and persisting venProductCategories");
				List<VenProductCategory> venProductCategories = new ArrayList<VenProductCategory>();
				List<VenMerchantProduct> venMerchantProducts = new ArrayList<VenMerchantProduct>();
				for (VenProductCategory venProductCategory : venMerchantProduct.getVenProductCategories()) {
					venProductCategory = productCategoryService.synchronizeVenProductCategory(venProductCategory);
					venMerchantProducts = venProductCategory.getVenMerchantProducts();
					boolean merchantProductExists = false;
					for (VenMerchantProduct merchantProduct : venMerchantProducts) {
						if (merchantProduct.getProductId() == synchVenMerchantProduct.getProductId()) {
							merchantProductExists = true;
							break;
						}
					}
					if (!merchantProductExists) {
						venMerchantProducts.add(synchVenMerchantProduct);
					}
					venProductCategory.setVenMerchantProducts(venMerchantProducts);
					venProductCategories.add(venProductCategory);
				}

				if (synchVenMerchantProduct.getVenProductCategories() != null && (!synchVenMerchantProduct.getVenProductCategories().isEmpty())) {

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeAndPersistVenProductCategories::synchVenMerchantProduct.productCategories is not empty");
					List<VenProductCategory> synchVenProductCategories = new ArrayList<VenProductCategory>(synchVenMerchantProduct.getVenProductCategories());

					boolean productCategoryExists = false;
					for (VenProductCategory venProductCategory : venProductCategories) {
						for (VenProductCategory productCategory : synchVenMerchantProduct.getVenProductCategories()) {
							if (venProductCategory.getProductCategory().equalsIgnoreCase(productCategory.getProductCategory())
									&& (venProductCategory.getLevel() == productCategory.getLevel())) {
								productCategoryExists = true;
								break;
							} 
						}
						if (!productCategoryExists) {
							synchVenProductCategories.add(venProductCategory);
						} else {
							productCategoryExists = false;
						}
					}

					synchVenMerchantProduct.setVenProductCategories(synchVenProductCategories);
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeAndPersistVenProductCategories::synchVenMerchantProduct.productCategories is empty");
					synchVenMerchantProduct.setVenProductCategories(venProductCategories);						
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeAndPersistVenProductCategories::successfully synchronized and persisted venProductCategories");
			}
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			e.printStackTrace();
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}

}
