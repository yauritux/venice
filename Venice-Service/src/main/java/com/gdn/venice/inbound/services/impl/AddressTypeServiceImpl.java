package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<VenAddressType> synchronizeVenAddressTypeReferences(
			List<VenAddressType> addressTypeReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenAddressTypeReferences::addressTypeReferences=" + addressTypeReferences);
		//if (addressTypeReferences == null || addressTypeReferences.isEmpty()) return null;
		
		List<VenAddressType> synchronizedAddressTypeReferences = new ArrayList<VenAddressType>();
		
		if (addressTypeReferences != null) {
			for (VenAddressType addressType : addressTypeReferences) {
				if (addressType.getAddressTypeId() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "Restricting VenAddressType... :" + addressType.getAddressTypeId());
					VenAddressType venAddressType = venAddressTypeDAO.findOne(addressType.getAddressTypeId());
					if (venAddressType == null) {
						VenAddressType persistedVenAddressType = venAddressTypeDAO.save(addressType);
						synchronizedAddressTypeReferences.add(persistedVenAddressType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenAddressTypeReferences::successfully added persistedVenAddressType into synchronizedAddressTypeReferences");
					} else {
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
