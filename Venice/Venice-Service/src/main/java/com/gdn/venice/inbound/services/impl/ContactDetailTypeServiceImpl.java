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
import com.gdn.venice.dao.VenContactDetailTypeDAO;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VenContactDetailTypeSynchronizingError;
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
	
	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenContactDetailType synchronizeVenContactDetailType(VenContactDetailType venContactDetailType) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailType::BEGIN,venContactDetailType = " + venContactDetailType);
		
		VenContactDetailType synchContactDetailType = venContactDetailType;
		
		if (venContactDetailType != null && venContactDetailType.getContactDetailTypeDesc() != null) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenContactDetailType::Restricting VenContactDetailType... :" 
								+ venContactDetailType.getContactDetailTypeDesc());
				List<VenContactDetailType> contactDetailTypeList = venContactDetailTypeDAO.findByContactDetailTypeDesc(
						venContactDetailType.getContactDetailTypeDesc());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "::synchronizeVenContactDetailTypeReferences::contactDetailTypeList.size = "
								+ (contactDetailTypeList != null ? contactDetailTypeList.size() : 0));
				if (contactDetailTypeList == null || (contactDetailTypeList.isEmpty())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenContactDetailType::venContactDetailType doesn't exist yet, persisting it");
					if (!em.contains(venContactDetailType)) {
						//venContactDetailType is in detach mode, hence need to explicitly call save method
						synchContactDetailType = venContactDetailTypeDAO.save(venContactDetailType);
					}				
				} else {
					synchContactDetailType = contactDetailTypeList.get(0);
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName()
						, e);
				CommonUtil.logAndReturnException(new VenContactDetailTypeSynchronizingError("Error in synchronyzing VenContactDetailType"
						, VeniceExceptionConstants.VEN_EX_130005), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailType::returning synchContactDetailType = " + synchContactDetailType);
		return synchContactDetailType;
	}
	
	@Override
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

					List<VenContactDetailType> contactDetailTypeList = venContactDetailTypeDAO.findByContactDetailTypeDesc(contactDetailType.getContactDetailTypeDesc());
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
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenContactDetailTypeReferences::successfully added contactDetailType into synchronizedContactDetailTypes");
					}
				}		
			} //end of 'for'
		}
		
		return synchronizedContactDetailTypes;
	}

}
