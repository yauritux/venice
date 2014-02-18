package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenPartyTypeDAO;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.PartyTypeService;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PartyTypeServiceImpl implements PartyTypeService {
	
	@Autowired
	private VenPartyTypeDAO venPartyTypeDAO;

	@Override
	public List<VenPartyType> synchronizeVenPartyTypeReferenceData(
			List<VenPartyType> venPartyTypes) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPartyTypeReferenceData::BEGIN,venPartyTypes=" + venPartyTypes);
		//if (venPartyTypes == null || venPartyTypes.size() == 0) return null;
		
		List<VenPartyType> synchronizedVenParties = new ArrayList<VenPartyType>();
		
		if (venPartyTypes != null) {
			for (VenPartyType venPartyType : venPartyTypes) {
				if (venPartyType.getPartyTypeDesc() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenPartyType... :" + venPartyType.getPartyTypeId());
					VenPartyType partyType = venPartyTypeDAO.findOne(venPartyType.getPartyTypeId());
					if (partyType == null) {
						throw CommonUtil.logAndReturnException(new InvalidOrderException(
								"Party type does not exist", VeniceExceptionConstants.VEN_EX_999999)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenPartyTypeReferenceData::adding partyType into retVal");
						synchronizedVenParties.add(partyType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenPartyTypeReferenceData::successfully added partyType into retVal");
					}
				}		
			} //end of 'for'
		}
		
		return synchronizedVenParties;
	}

}
