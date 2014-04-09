package com.gdn.venice.inbound.services.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenProductTypeDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.ProductTypeService;
import com.gdn.venice.persistence.VenProductType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class ProductTypeServiceImpl implements ProductTypeService {

	@Autowired
	private VenProductTypeDAO venProductTypeDAO;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public VenProductType synchronizeVenProductType(VenProductType venProductType) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenProductType::BEGIN,venProductType = " + venProductType);
		
		VenProductType synchronizedVenProductType = venProductType;
		
		if (venProductType != null && venProductType.getProductTypeCode() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenProductTypeReferences::Synchronizing VenProductType... :" 
							+ venProductType.getProductTypeCode());
			List<VenProductType> productTypeList = venProductTypeDAO.findByProductTypeCode(venProductType.getProductTypeCode());
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenProductTypeReferences::productTypeList found = " + (productTypeList != null ? productTypeList.size() : 0));
			if (productTypeList == null || productTypeList.isEmpty()) {
				if (!em.contains(venProductType)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenProductTypeReferences::calling venProductType save explicitly");
					synchronizedVenProductType = venProductTypeDAO.save(venProductType);					
				}
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenProductTypeReferences::data found, synchronizing data with cache");
				synchronizedVenProductType = productTypeList.get(0);
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductType::END,returning synchronizedVenProductType = " + synchronizedVenProductType);
		
		return synchronizedVenProductType;
	}
	
	/*
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenProductType> synchronizeVenProductTypeReferences(
			List<VenProductType> productTypeRefs)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductTypeReferences::BEGIN, productTypeRefs = " + productTypeRefs);
		
		List<VenProductType> synchronizedProductTypeRefs = new ArrayList<VenProductType>();
		
		if (productTypeRefs != null && (!(productTypeRefs.isEmpty()))) {
			try {
				for (VenProductType productType : productTypeRefs) {
					if (productType.getProductTypeCode() != null) {
						try {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenProductTypeReferences::Synchronizing VenProductType... :" 
											+ productType.getProductTypeCode());

							List<VenProductType> productTypeList = venProductTypeDAO.findByProductTypeCode(productType.getProductTypeCode());
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenProductTypeReferences::productTypeList = "
											+ productTypeList);
							if (productTypeList == null || (productTypeList.isEmpty())) {						
								VenProductType venProductType = venProductTypeDAO.save(productType);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "synchronizeVenProductTypeReferences::productType is saved");						
								synchronizedProductTypeRefs.add(venProductType);						
							} else {						
								VenProductType venProductType = productTypeList.get(0);								
						//List<VenMerchantProduct> productTypeMerchantProducts = (venProductType.getVenMerchantProducts() != null
							//	&& (!venProductType.getVenMerchantProducts().isEmpty())
								//? new ArrayList<VenMerchantProduct>(venProductType.getVenMerchantProducts())
									//	: new ArrayList<VenMerchantProduct>());			
								//venProductType.setVenMerchantProducts(productTypeMerchantProducts);
								synchronizedProductTypeRefs.add(venProductType);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "synchronizeVenProductTypeReferences::successfully added venProductType into synchronizedProductTypeRefs");
							}							
						} catch (Exception e) {
							throw CommonUtil.logAndReturnException(new CannotPersistProductTypeException("cannot persisting VenProductType"
									, VeniceExceptionConstants.VEN_EX_010001)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						}			
					} //end if productTypeCode IS NOT NULL
				} //end of 'for'
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				e.printStackTrace();
				CommonUtil.logAndReturnException(new CannotPersistProductTypeException("Cannot persist VenProductType!"
						, VeniceExceptionConstants.VEN_EX_120002), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		} //end if productTypeRefs IS NOT NULL
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductTypeReferences::EOM, returning synchronizedProductTypeRefs = "
				   + synchronizedProductTypeRefs.size());
		return synchronizedProductTypeRefs;
	}
	*/	
}