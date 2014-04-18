package com.gdn.venice.inbound.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenProductCategoryDAO;
import com.gdn.venice.exception.ProductCategoryNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenProductCategory;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class ProductCategoryServiceImpl implements ProductCategoryService {

	@Autowired
	private VenProductCategoryDAO venProductCategoryDAO;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public VenProductCategory synchronizeVenProductCategory(VenProductCategory venProductCategory)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductCategory::BEGIN,venProductCategory=" + venProductCategory);
		
		VenProductCategory synchProductCategory = venProductCategory;
		
		if (venProductCategory != null && venProductCategory.getProductCategory() != null 
				&& venProductCategory.getLevel() != null && venProductCategory.getProductCategoryId() == null) {
			try {
				List<VenProductCategory> productCategoryList = venProductCategoryDAO.findByProductCategoryAndLevel(
						venProductCategory.getProductCategory(), venProductCategory.getLevel());
				if (productCategoryList != null && (!productCategoryList.isEmpty())) {
					synchProductCategory = productCategoryList.get(0);
				} else {
					throw CommonUtil.logAndReturnException(new ProductCategoryNotFoundException("Cannot found Product Category!"
							, VeniceExceptionConstants.VEN_EX_600001), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenProductCategory::END,returning synchProductCategory=" + synchProductCategory);
		return synchProductCategory;
	}

}
