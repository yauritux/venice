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
import com.gdn.venice.dao.VenProductTypeDAO;
import com.gdn.venice.exception.CannotPersistProductTypeException;
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
	EntityManager em;
	
	@Override
	public List<VenProductType> synchronizeVenProductTypeReferences(
			List<VenProductType> productTypeRefs)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductTypeReferences::BEGIN, productTypeRefs = " + productTypeRefs);
		
		List<VenProductType> synchronizedProductTypeRefs = new ArrayList<VenProductType>();
		
		for (VenProductType productType : productTypeRefs) {
			em.detach(productType);
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
						synchronizedProductTypeRefs.add(venProductType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenProductTypeReferences::successfully added venProductType into synchronizedProductTypeRefs");
					}							
				} catch (Exception e) {
					throw CommonUtil.logAndReturnException(new CannotPersistProductTypeException("cannot persisting VenProductType"
							, VeniceExceptionConstants.VEN_EX_010001)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}			
			}
		} //end of 'for'
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductTypeReferences::EOM, returning synchronizedProductTypeRefs = "
				   + synchronizedProductTypeRefs.size());
		return synchronizedProductTypeRefs;
	}
}
