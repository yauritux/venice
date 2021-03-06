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
import com.gdn.venice.dao.VenContactDetailDAO;
import com.gdn.venice.exception.CannotPersistVenContactDetailException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.ContactDetailService;
import com.gdn.venice.inbound.services.ContactDetailTypeService;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenContactDetailType;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class ContactDetailServiceImpl implements ContactDetailService {
	
	@Autowired
	private VenContactDetailDAO venContactDetailDAO;
	
	@Autowired
	private ContactDetailTypeService contactDetailTypeService;
	
	@Autowired
	private PartyService partyService;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<VenContactDetail> findByParty(VenParty party) {
		return venContactDetailDAO.findByParty(party);
	}
	
	/**
	 * updateContactDetailList - compares the existing contact detail list with the 
	 * new contact detail list, writes any new contact details to the database 
	 * and returns the updated contact detail list.
	 * @param existingVenContactDetailList
	 * @param newVenContactDetailList
	 * @return the updated contact detail list
	 */
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenContactDetail> updateContactDetailList(VenParty existingParty
			, List<VenContactDetail> existingVenContactDetailList, List<VenContactDetail> newVenContactDetailList)
			throws VeniceInternalException {
		List<VenContactDetail> updatedVenContactDetailList = new ArrayList<VenContactDetail>();
		List<VenContactDetail> persistVenContactDetailList = new ArrayList<VenContactDetail>();
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateContactDetailList::START");
		/*
		 * Iterate the list of existing contacts to determine if 
		 * the new contacts exist already
		 */
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "updateContactDetailList::newVenContactDetailList member = " 
				+ (newVenContactDetailList != null ? newVenContactDetailList.size() : 0));
		for(VenContactDetail newVenContactDetail:newVenContactDetailList){
			Boolean bFound = false;
			
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateContactDetailList::newVenContactDetail => " + newVenContactDetail.getContactDetail());
			
			if(existingVenContactDetailList != null && existingVenContactDetailList.size() > 0){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "updateContactDetailList::existingVenContactDetailList not empty");
				for(VenContactDetail existingVenContactDetail:existingVenContactDetailList){
					
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateContactDetailList::existingVenContactDetail => " + existingVenContactDetail.getContactDetail());
					
					/*
					 * If the contact detail and type are not null and they are equal to each other (new and existing) 
					 * then the contact is existing and is added to the return list only.
					 * 
					 * If it is a new contact it is added to the persist list
					 */
					if((existingVenContactDetail.getContactDetail() != null && newVenContactDetail.getContactDetail() != null) 
							&& existingVenContactDetail.getContactDetail().trim().equalsIgnoreCase(newVenContactDetail.getContactDetail().trim())
							&& ((existingVenContactDetail.getVenContactDetailType() != null	&& newVenContactDetail.getVenContactDetailType() != null)) 
							&& existingVenContactDetail.getVenContactDetailType().getContactDetailTypeDesc().equals(newVenContactDetail.getVenContactDetailType().getContactDetailTypeDesc())){
						/*
						 * The contact detail is assumed to be equal (note that the equals() 
						 * operation can't be used because it is implemented by 
						 * JPA on the primary key. Add it to the list
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "updateContactDetailList::contact detail equal with existing, added to updated list");
						updatedVenContactDetailList.add(existingVenContactDetail);
						
						bFound = true;
						//Break from the inner loop as the contact is found
						break;
					}
				}
			}else{
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "updateContactDetailList::existingVenContactDetailList is empty");
			}
			/*
			 * The contact detail is not found in the existing
			 * contact list therefore it is a new contact detail 
			 * and it needs to be persisted. The existing party
			 * record also needs to be set otherwise it
			 * will fail as the new contact record has a
			 * detached party
			 */
			if(!bFound){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "updateContactDetailList::contact detail not equal with existing, persist it");
				newVenContactDetail.setVenParty(existingParty);
				if(!persistVenContactDetailList.contains(newVenContactDetail)){
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "updateContactDetailList::added the new contact detail to list");
					persistVenContactDetailList.add(newVenContactDetail);
				}
			}
		}	
		
		/*
		 * Persist any contact details that are new
		 */
		if(persistVenContactDetailList != null && !persistVenContactDetailList.isEmpty()){
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "updateContactDetailList::new contact detail list not empty, start persist new contact detail");
			persistVenContactDetailList = this.persistContactDetails(persistVenContactDetailList);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "updateContactDetailList::done persist contact detail");
			//Add the persisted contact details to the new list
			updatedVenContactDetailList.addAll(persistVenContactDetailList);
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "updateContactDetailList::returning updated contact detail list ("
				+ updatedVenContactDetailList.size() + " members)");
		return updatedVenContactDetailList;		
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenContactDetail persistContactDetail(VenContactDetail venContactDetail) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetail::starting method persistContactDetail,venContactDetail=" + venContactDetail);
		
		VenContactDetail persistedContactDetail = venContactDetail;
		
		if (venContactDetail != null && venContactDetail.getContactDetailId() == null) {
			try {
				VenContactDetail synchContactDetail = synchronizeVenContactDetailReferenceData(venContactDetail);
				//persist contact detail
				if (!em.contains(synchContactDetail)) {
					// contactDetail instance is in detach mode, hence need to call save explicitly as shown below
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistContactDetails::calling save on venContactDetailDAO explicitly");
					persistedContactDetail = venContactDetailDAO.save(synchContactDetail);
				} else {
					persistedContactDetail = synchContactDetail;
				}	
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				throw CommonUtil.logAndReturnException(new CannotPersistVenContactDetailException(
						"Cannot persist VenContactDetail!", VeniceExceptionConstants.VEN_EX_110001)
				        , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetail::END, returning persistedContactDetail = " + persistedContactDetail);
		return persistedContactDetail;
	}
	
	/**
	 * Persists a list of contact details using the session tier.
	 * 
	 * @param venContactDetails
	 * @return the persisted object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenContactDetail> persistContactDetails(List<VenContactDetail> venContactDetails) 
	        throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetails::start method persist contact detail");
		List<VenContactDetail> newVenContactDetailList = new ArrayList<VenContactDetail>();
		if (venContactDetails != null && (!venContactDetails.isEmpty())) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistContactDetails::Persisting VenContactDetail list...:" + venContactDetails.size());
				try {
					for (VenContactDetail contactDetail : venContactDetails) {
						newVenContactDetailList.add(persistContactDetail(contactDetail));
					} //end of for
				} catch (Exception e) {
					CommonUtil.logError(this.getClass().getCanonicalName(), e);
					throw CommonUtil.logAndReturnException(new CannotPersistVenContactDetailException(
							"Cannot persist VenContactDetail!", VeniceExceptionConstants.VEN_EX_110001)
					        , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetails::EOM, returning newVenContactDetailList = " + newVenContactDetailList);
		return newVenContactDetailList;
	}
	
	/**
	 * Synchronizes the data for the direct VenContactDetail references
	 * 
	 * @param venContactDetail
	 * @return the synchronized data object
	 */	
	@Override
	public VenContactDetail synchronizeVenContactDetailReferenceData(VenContactDetail venContactDetail) 
	  throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailReferenceData::start sync contact detail method");
		
	    VenContactDetailType synchContactDetailType 
	       = contactDetailTypeService.synchronizeVenContactDetailType(venContactDetail.getVenContactDetailType());
		// Synchronize the data references		
	    venContactDetail.setVenContactDetailType(synchContactDetailType);
	    
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailReferenceData::EOM, returning venContactDetail = " + venContactDetail);
		return venContactDetail;		
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenContactDetail> synchronizeVenContactDetailReferences(
			List<VenContactDetail> contactDetailReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailReferences::BEGIN,contactDetailReferences=" + contactDetailReferences);
		
		List<VenContactDetail> synchronizedContactDetailReferences = new ArrayList<VenContactDetail>();
		
		if (contactDetailReferences != null) {
			for (VenContactDetail contactDetail : contactDetailReferences) {
				if (contactDetail.getVenContactDetailType() != null) {
					try {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), 
								"synchronizeVenContactDetailReferences::Synchronizing VenContactDetail... :" 
										+ contactDetail.getVenContactDetailType());
						// Synchronize the reference data
						VenContactDetail venContactDetail = synchronizeVenContactDetailReferenceData(contactDetail);
						
						// Synchronize the object
						VenContactDetail synchronizedVenContactDetail = venContactDetailDAO.save(venContactDetail);					
						synchronizedContactDetailReferences.add(synchronizedVenContactDetail);
						
						synchronizedContactDetailReferences.add(venContactDetail);						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenContactDetailReferences::successfully added synchronizedVenContactDetail into synchronizedContactDetailReferences");
					} catch (Exception e) {
						CommonUtil.logError(this.getClass().getCanonicalName(), e);
						CommonUtil.logAndReturnException(new CannotPersistVenContactDetailException("cannot persisting VenContactDetail", 
								VeniceExceptionConstants.VEN_EX_110001)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					}						
				}		
			} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenContactDetailReferences::EOM, returning synchronizedContactDetailReferences="
				+ synchronizedContactDetailReferences.size());
		return synchronizedContactDetailReferences;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenContactDetail persistContactDetail(VenContactDetail venContactDetail, VenParty venParty) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetail::starting method persistContactDetail,venContactDetail=" + venContactDetail
				+ ",venParty=" + venParty.getFullOrLegalName());
		
		VenContactDetail persistedContactDetail = venContactDetail;
		
		if (venContactDetail != null && venContactDetail.getContactDetailId() == null) {
			try {
				VenContactDetail synchContactDetail = synchronizeVenContactDetailReferenceData(venContactDetail);

				//persist contact detail
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistContactDetail::find existing contact detail");			
				List<VenContactDetail> existingContactDetail = venContactDetailDAO.findByContactDetail(synchContactDetail.getContactDetail());
				if (existingContactDetail != null && (!existingContactDetail.isEmpty())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistContactDetail::existing contact detail found, total member = " + existingContactDetail.size());
					persistedContactDetail = existingContactDetail.get(0);					
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistContactDetail::existing contact detail not found, going to persist it");
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistContactDetail::venParty=" + venParty);
					synchContactDetail.setVenParty(venParty);
					if (!em.contains(synchContactDetail)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistContactDetail::calling save on venContactDetailDAO explicitly");
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistContactDetail::venParty =  "+ synchContactDetail.getVenParty().getPartyId());
						persistedContactDetail = venContactDetailDAO.save(synchContactDetail);					
					} else {
						persistedContactDetail = synchContactDetail;
					}
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				e.printStackTrace();
				throw CommonUtil.logAndReturnException(new CannotPersistVenContactDetailException(
						"Cannot persist VenContactDetail!", VeniceExceptionConstants.VEN_EX_110001)
				        , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetail::END, returning persistedContactDetail = " + persistedContactDetail);
		
		return persistedContactDetail;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenContactDetail> persistContactDetails(List<VenContactDetail> venContactDetails, VenParty venParty) 
	        throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetails::starting method persist contact detail");
		List<VenContactDetail> newVenContactDetailList = new ArrayList<VenContactDetail>();
		if (venContactDetails != null && (!venContactDetails.isEmpty())) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistContactDetails::Persisting VenContactDetail list...:" + venContactDetails.size());
				
				for (VenContactDetail contactDetail : venContactDetails) {
					newVenContactDetailList.add(persistContactDetail(contactDetail, venParty));
				}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetails::EOM, returning newVenContactDetailList = " + newVenContactDetailList);
		return newVenContactDetailList;
	}
}
