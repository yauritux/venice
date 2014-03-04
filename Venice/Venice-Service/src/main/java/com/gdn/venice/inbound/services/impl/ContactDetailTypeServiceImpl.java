package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenContactDetailTypeDAO;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.ContactDetailTypeService;
import com.gdn.venice.persistence.VenContactDetailType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class ContactDetailTypeServiceImpl implements ContactDetailTypeService {
	
	@Autowired
	private VenContactDetailTypeDAO venContactDetailTypeDAO;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenContactDetailType> synchronizeVenContactDetailTypeReferences(
			List<VenContactDetailType> contactDetailTypes) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailTypeReferences::BEGIN,contactDetailTypes="
				  + contactDetailTypes);
		
		List<VenContactDetailType> synchronizedContactDetailTypes = new ArrayList<VenContactDetailType>();
		
		if (contactDetailTypes != null) {
			for (VenContactDetailType contactDetailType : contactDetailTypes) {
				if (contactDetailType.getContactDetailTypeDesc() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenContactDetailTypeReferences::Restricting VenContactDetailType... :" 
									+ contactDetailType.getContactDetailTypeDesc());

					/*
					List<VenContactDetailType> contactDetailTypeList 
					= venContactDetailTypeDAO.findByContactDetailTypeDesc(contactDetailType.getContactDetailTypeDesc());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "::synchronizeVenContactDetailTypeReferences::contactDetailTypeList.size = "
									+ (contactDetailTypeList != null ? contactDetailTypeList.size() : 0));
					if (contactDetailTypeList == null || (contactDetailTypeList.size() == 0)) {
						throw CommonUtil.logAndReturnException(new InvalidOrderException(
								"Contact detail type does not exist", VeniceExceptionConstants.VEN_EX_999999)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {					
						VenContactDetailType venContactDetailType = contactDetailTypeList.get(0);						
						synchronizedContactDetailTypes.add(venContactDetailType);
						*/
					VenContactDetailType venContactDetailType = venContactDetailTypeDAO.save(contactDetailType);
					synchronizedContactDetailTypes.add(venContactDetailType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenContactDetailTypeReferences::successfully added contactDetailType into synchronizedContactDetailTypes");
					//}
				}		
			} //end of 'for'
		}
		
		return synchronizedContactDetailTypes;
	}

}
