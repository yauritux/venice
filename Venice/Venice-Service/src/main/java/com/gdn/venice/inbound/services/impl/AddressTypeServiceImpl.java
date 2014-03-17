package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenAddressTypeDAO;
import com.gdn.venice.inbound.services.AddressTypeService;
import com.gdn.venice.persistence.VenAddressType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class AddressTypeServiceImpl implements AddressTypeService {
	
	@Autowired
	VenAddressTypeDAO venAddressTypeDAO;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenAddressType> synchronizeVenAddressTypeReferences(
			List<VenAddressType> addressTypeReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenAddressTypeReferences::addressTypeReferences=" + addressTypeReferences);
		
		List<VenAddressType> synchronizedAddressTypeReferences = new ArrayList<VenAddressType>();
		
		if (addressTypeReferences != null) {
			for (VenAddressType addressType : addressTypeReferences) {
				if (addressType.getAddressTypeId() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "Restricting VenAddressType... :" + addressType.getAddressTypeId());
					VenAddressType venAddressType = venAddressTypeDAO.findOne(addressType.getAddressTypeId());
					if (venAddressType == null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenAddressTypeReferences::venAddressType is not listed in the database, saving it");
						/*
						if (!em.contains(addressType)) {
							//addressType is in detach mode, hence need to call save explicitly below
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenAddressTypeReferences::call save explicitly");
							addressType = venAddressTypeDAO.save(addressType);
						}
						*/
						if (em.contains(addressType)) {
							em.detach(addressType);
						}
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenAddressTypeReferences::addressType is detached");
						synchronizedAddressTypeReferences.add(addressType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenAddressTypeReferences::successfully added persistedVenAddressType into synchronizedAddressTypeReferences");
					} else {
						em.detach(venAddressType);
						synchronizedAddressTypeReferences.add(venAddressType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenAddressTypeReferences::successfully added venAddressType into synchronizedAddressTypeReferences");
					}
				}
			} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenAddressTypeReferences::returning synchronizedAddressTypeReferences = "
				+ synchronizedAddressTypeReferences.size());
		return synchronizedAddressTypeReferences;
	}

}
