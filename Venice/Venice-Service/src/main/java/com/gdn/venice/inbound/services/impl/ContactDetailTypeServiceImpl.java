package com.gdn.venice.inbound.services.impl;

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
	public VenContactDetailType synchronizeVenContactDetailType(
			VenContactDetailType contactDetailType) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailType::BEGIN,contactDetailType="
				  + contactDetailType);
		
		VenContactDetailType synchronizedContactDetailType = new VenContactDetailType();
		
		if (contactDetailType != null) {
			//for (VenContactDetailType contactDetailType : contactDetailTypes) {
				if (contactDetailType.getContactDetailTypeDesc() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenContactDetailType::Restricting VenContactDetailType... :" 
									+ contactDetailType.getContactDetailTypeDesc());

					List<VenContactDetailType> contactDetailTypeList = venContactDetailTypeDAO.findByContactDetailTypeDesc(contactDetailType.getContactDetailTypeDesc());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "::synchronizeVenContactDetailType::contactDetailTypeList.size = "
									+ (contactDetailTypeList != null ? contactDetailTypeList.size() : 0));
					if (contactDetailTypeList == null || (contactDetailTypeList.isEmpty())) {
						throw CommonUtil.logAndReturnException(new InvalidOrderException(
								"Contact detail type does not exist", VeniceExceptionConstants.VEN_EX_999999)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						/*
						VenContactDetailType venContactDetailType = contactDetailTypeList.get(0);
						synchronizedContactDetailTypes.add(venContactDetailType);
						*/
						synchronizedContactDetailType = contactDetailTypeList.get(0);
					}
				}		
			//} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailType::END,returning synchronizedContactDetailType=" + synchronizedContactDetailType);
		
		return synchronizedContactDetailType;
	}

}
