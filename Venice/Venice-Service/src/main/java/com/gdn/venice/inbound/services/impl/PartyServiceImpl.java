package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.djarum.raf.utilities.JPQLStringEscapeUtility;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.dao.VenPartyDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.ContactDetailService;
import com.gdn.venice.inbound.services.CustomerService;
import com.gdn.venice.inbound.services.PartyAddressService;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.inbound.services.PartyTypeService;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenAddressType;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyAddress;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 * 
 * There is a bad code starting from line 284 (related to short-lived object which can cause poor performance).
 * Notice that those lines should be refactored later !!
 * 
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PartyServiceImpl implements PartyService {
	
	@PersistenceContext
	EntityManager em;

	@Autowired
	private VenPartyDAO venPartyDAO;

	@Autowired
	private AddressService addressService;

	@Autowired
	private ContactDetailService contactDetailService;

	@Autowired
	private PartyAddressService partyAddressService;

	@Autowired
	private PartyTypeService partyTypeService;

	@Autowired
	private CustomerService customerService;

	@Override
	public List<VenParty> findByLegalName(String legalName) {
		return venPartyDAO.findByLegalName(legalName);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenParty persistParty(VenParty venParty, String type)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"persistParty::BEGIN, venParty=" + venParty + ", type=" + type);
		if (venParty != null) {
			VenParty existingParty = null;
			if (type.equals("Customer")) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"persistParty::Persisting VenParty (PartyType = Customer)... :"
								+ venParty.getVenCustomers().get(0)
										.getCustomerUserName());

				// Get any existing party based on customer username
				existingParty = retrieveExistingParty(venParty
						.getVenCustomers().get(0).getCustomerUserName(), true);
			} else {
				CommonUtil.logDebug(
						this.getClass().getCanonicalName(),
						"persistParty::Persisting VenParty (PartyType = " + type + ") :"
								+ venParty.getFullOrLegalName());

				// Get any existing party based on full or legal name
				existingParty = retrieveExistingParty(venParty
						.getFullOrLegalName(), false);
			}

			/*
			 * If the party already exists then the existing party addresses and
			 * contacts may have changed so we need to synchronize them
			 */
			if (existingParty != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"persistParty::existing Party not null");

				// Get the existing addresses				
				/*
				List<VenAddress> existingAddressList = new ArrayList<VenAddress>();				
				for (VenPartyAddress venPartyAddress : existingParty
						.getVenPartyAddresses()) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::ven Party Address... :"
									+ venPartyAddress.getVenAddress()
											.getAddressId());
					existingAddressList.add(venPartyAddress.getVenAddress());
				}				

				// Get the new addresses				
				List<VenAddress> newAddressList = new ArrayList<VenAddress>();
				if (venParty.getVenPartyAddresses() != null) {
					for (VenPartyAddress venPartyAddress : venParty
							.getVenPartyAddresses()) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"persistParty::New ven Party Address... :"
										+ venPartyAddress.getVenAddress()
												.getStreetAddress1());
						newAddressList.add(venPartyAddress.getVenAddress());
					}
				}
				*/				

				/*
				 * If any new addresses are provided then check that the
				 * existing addresses match the new addresses else add the new
				 * addresses for the party
				 */
				//if (!newAddressList.isEmpty()) {
				if (!venParty.getVenPartyAddresses().isEmpty()) {
					CommonUtil
							.logDebug(this.getClass().getCanonicalName(),
									"persistParty::New ven Party Address is not empty and consequently, Updating Address List");
					//List<VenAddress> updatedAddressList = addressService.updateAddressList(existingAddressList, newAddressList);
					
					List<VenPartyAddress> existingPartyVenAddresses = new ArrayList<VenPartyAddress>();
					for (VenPartyAddress venPartyAddress : existingParty.getVenPartyAddresses()) {
						venPartyAddress.setVenParty(existingParty); //we need to do this in order to avoid NullPointerException because of bidirectional design
						existingPartyVenAddresses.add(venPartyAddress);
					}
					
					List<VenPartyAddress> newPartyVenAddresses = new ArrayList<VenPartyAddress>();
					for (VenPartyAddress venPartyAddress : venParty.getVenPartyAddresses()) {
						venPartyAddress.setVenParty(venParty);
						newPartyVenAddresses.add(venPartyAddress);
					}
					
					List<VenPartyAddress> updatedPartyAddressList = partyAddressService.updatePartyAddressList(existingPartyVenAddresses, newPartyVenAddresses);
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::updatedAddressList size => "
									+ (updatedPartyAddressList != null ? updatedPartyAddressList.size() : 0));
					//List<VenAddress> tempAddressList = new ArrayList<VenAddress>();

					//tempAddressList.addAll(updatedAddressList);
					
					/*
					for (VenAddress updatedAddress : updatedAddressList) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::updatedAddress Street Address 1 = " + updatedAddress.getStreetAddress1());
						tempAddressList.add(updatedAddress);
					}
					*/

					/*
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::Remove old VenAddress");
					// Remove all the existing addresses
					updatedAddressList.removeAll(existingAddressList);
					*/
					
					/*
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::Removing old VenPartyAddress");
					updatedPartyAddressList.removeAll(existingParty.getVenPartyAddresses());
					*/					

					// Setup the new VenPartyAddress records
					List<VenPartyAddress> venPartyAddressList = new ArrayList<VenPartyAddress>();
					/*
					for (VenAddress updatedAddress : updatedAddressList) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::setup new venParrtyAddress = " + updatedAddress.getStreetAddress1());
						VenPartyAddress venPartyAddress = new VenPartyAddress();
						VenAddressType venAddressType = new VenAddressType();
						venAddressType
								.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);

						venPartyAddress.setVenAddress(updatedAddress);
						venPartyAddress.setVenAddressType(venAddressType);
						venPartyAddress.setVenParty(existingParty);
						existingParty.getVenPartyAddresses().add(
								venPartyAddress);

						venPartyAddressList.add(venPartyAddress);
					}
					*/
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::updatedPartyAddressList members = "
							+ (updatedPartyAddressList != null ? updatedPartyAddressList.size() : 0));
					
					for (VenPartyAddress venPartyAddress : updatedPartyAddressList) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::setup new VenPartyAddress = " + venPartyAddress.getVenAddress().getStreetAddress1());
						VenAddressType venAddressType = new VenAddressType();
						venAddressType.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);
						
						venPartyAddress.setVenAddressType(venAddressType);
						venPartyAddress.setVenParty(existingParty);
						venPartyAddressList.add(venPartyAddress);
					}

					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::venPartyAddressList members = " + (venPartyAddressList != null ? venPartyAddressList.size() : 0));
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::persist Party Addresses ");
					// Persist the new VenPartyAddress records
					venPartyAddressList = partyAddressService
							.persistPartyAddresses(venPartyAddressList);
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::venPartyAddressList=" + venPartyAddressList);
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::size=" + (venPartyAddressList != null ? venPartyAddressList.size() : 0));

					/*
					if (updatedAddressList == null || updatedAddressList.isEmpty()) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"persistParty::updatedAddressList.size == 0");
						for (VenAddress updatedAddress : tempAddressList) {
							CommonUtil.logDebug(this.getClass()
									.getCanonicalName(), "persistParty::updatedAddress = "
									+ updatedAddress);
							CommonUtil.logDebug(this.getClass()
									.getCanonicalName(),
									"persistParty::total venparty addresses : "
											+ updatedAddress
													.getVenPartyAddresses()
													.size());
							venPartyAddressList.addAll(updatedAddress
									.getVenPartyAddresses());
							for (VenPartyAddress venPartyAddress : venPartyAddressList) {
								CommonUtil.logDebug(this.getClass()
										.getCanonicalName(),
										"persistParty::VenPartyAddress => "
												+ venPartyAddress
														.getVenAddress()
														.getAddressId());
							}
						}
					}
					*/

					// copy existing address list to new list so it can be added
					// new address list
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::copy address list to new list");
					// List<VenPartyAddress> venPartyAddressList2=new
					// ArrayList<VenPartyAddress>(existingParty.getVenPartyAddresses()).subList(0,venPartyAddressList.size());

					// Add all the new party address records
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::add All VenParty Addresses");
					existingParty.setVenPartyAddresses(venPartyAddressList);
					for (VenPartyAddress venPartyAddress : venPartyAddressList) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"persistParty::VenPartyAddress => "
										+ venPartyAddress.getVenAddress()
												.getAddressId());
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"persistParty::done add All VenParty Addresses");
				}

				/*
				 * If any new contact details are provided then check that the
				 * new contact details match the existing contact details else
				 * add the new contact details to the party and then merge.
				 */

				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"persistParty::Get old and new party ven contact Detail  ");
				// Get the existing contact details
				List<VenContactDetail> existingContactDetailList = existingParty
						.getVenContactDetails();

				if (venParty.getVenContactDetails() != null) {
					// Get the new addresses
					List<VenContactDetail> newContactDetailList = venParty
							.getVenContactDetails();

					if (!newContactDetailList.isEmpty()) {
						CommonUtil
								.logDebug(
										this.getClass().getCanonicalName(),
										"persistParty::updatedContact Detail List from existingContactDetailList to newContactDetailList");
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"persistParty::start updating contact detail");
						// if the contact detail of existing party is null we
						// can not get the party id using
						// existingContactDetailList, so send the existing party
						List<VenContactDetail> updatedContactDetailList = contactDetailService
								.updateContactDetailList(existingParty,
										existingContactDetailList,
										newContactDetailList);
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"persistParty::done updating contact detail!!!");

						existingParty
								.setVenContactDetails(updatedContactDetailList);
					}
				}
				return existingParty;
			}

			// Persist addresses
			List<VenAddress> addressList = new ArrayList<VenAddress>();
			Iterator<VenPartyAddress> i = venParty.getVenPartyAddresses()
					.iterator();
			while (i.hasNext()) {
				addressList.add(i.next().getVenAddress());
			}
			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"persistParty::persist Address List");
			addressList = addressService.persistAddressList(addressList);

			// Assign the address keys back to the n-n object
			i = venParty.getVenPartyAddresses().iterator();
			int index = 0;
			while (i.hasNext()) {
				VenPartyAddress next = i.next();
				next.setVenAddress(addressList.get(index));
				VenAddressType addressType = new VenAddressType();
				addressType
						.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);
				List<Object> references = new ArrayList<Object>(); // short-lived object , weird isn't it ? clearly this is very-very bad code
				references.add(addressType);
				// references = this.synchronizeReferenceData(references);
				index++;
			}

			// Detach the party addresses object before persisting party
			List<VenPartyAddress> venPartyAddressList = venParty
					.getVenPartyAddresses();
			venParty.setVenPartyAddresses(null);

			// Detach the list of contact details before persisting party
			List<VenContactDetail> venContactDetailList = venParty
					.getVenContactDetails();
			venParty.setVenContactDetails(null);

			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"persistParty::synchronize VenParty Reference Data");
			// Synchronize the reference data
			venParty = synchronizeVenPartyReferenceData(venParty);

			// Persist the object

			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"persistParty::persist VenParty ");			
			if (!em.contains(venParty)) {
				//venParty is in detach mode, hence we need to call save explicitly here
				venParty = venPartyDAO.save(venParty);
			}			

			VenAddressType venAddressType = new VenAddressType();
			venAddressType
					.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);

			// Set the party relationship for each VenPartyAddress
			i = venPartyAddressList.iterator();
			while (i.hasNext()) {
				VenPartyAddress next = i.next();
				next.setVenParty(venParty);
				next.setVenAddressType(venAddressType);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"persistParty::persist Party Addresses ");
			// Persist the party addresses
			venParty.setVenPartyAddresses(partyAddressService
					.persistPartyAddresses(venPartyAddressList));
			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"persistParty::Venpartyaddress size = >"
							+ venParty.getVenPartyAddresses().size());
			// Set the party relationship for each contact detail
			Iterator<VenContactDetail> contactsIterator = venContactDetailList
					.iterator();
			while (contactsIterator.hasNext()) {
				contactsIterator.next().setVenParty(venParty);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"persistParty::persist Contact Details");
			// Persist the contact details
			venParty.setVenContactDetails(contactDetailService
					.persistContactDetails(venContactDetailList));
			CommonUtil.logDebug(
					this.getClass().getCanonicalName(),
					"persistParty::VenContactDetails size = >"
							+ venParty.getVenContactDetails());
		}
		return venParty;
	}

	/**
	 * Synchronizes the data for the direct VenParty references
	 * 
	 * @param venParty
	 * @return the synchronized data object
	 */
	@Override
	public VenParty synchronizeVenPartyReferenceData(VenParty venParty)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenPartyReferenceData(VenParty)::BEGIN, venParty = "
						+ venParty);

		List<VenPartyType> partyTypeReferences = new ArrayList<VenPartyType>();
		List<VenParty> partyReferences = new ArrayList<VenParty>(); // refers to
																	// party's
																	// parent

		if (venParty.getVenParty() != null) {
			partyReferences.add(venParty.getVenParty());
			partyReferences = synchronizeVenPartyReferenceData(partyReferences);
		}

		if (venParty.getVenPartyType() != null) {
			partyTypeReferences.add(venParty.getVenPartyType());
			partyTypeReferences = partyTypeService
					.synchronizeVenPartyTypeReferenceData(partyTypeReferences);
		}

		for (VenParty party : partyReferences) {
			venParty.setVenParty(party);
		}

		for (VenPartyType partyType : partyTypeReferences) {
			venParty.setVenPartyType(partyType);
		}

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenPartyReferenceData::EOM, returning venParty "
						+ venParty);
		return venParty;
	}

	@Override
	public List<VenParty> synchronizeVenPartyReferenceData(
			List<VenParty> venParties) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPartyReferenceData::BEGIN,venParties=" + venParties);
		
		List<VenParty> venPartyList = new ArrayList<VenParty>();

		if (venParties != null) {
			for (VenParty party : venParties) {
				// Parties need to be synchronized
				if (party.getFullOrLegalName() != null) {
					try {
						CommonUtil
						.logDebug(this.getClass().getCanonicalName(),
								"synchronizeVenPartyReferenceData::Synchronizing VenParty reference data... ");
						party = this.synchronizeVenPartyReferenceData(party);
						CommonUtil
						.logDebug(this.getClass().getCanonicalName(),
								"synchronizeReferenceData::adding party into venPartyList");
						venPartyList.add(party);
						CommonUtil
						.logDebug(this.getClass().getCanonicalName(),
								"synchronizeReferenceData::successfully added party into venPartyList");
					} catch (Exception e) {
						throw CommonUtil
						.logAndReturnException(
								new VeniceInternalException(
										"An exception occured synchronizing VenParty reference data"),
										CommonUtil.getLogger(this.getClass()
												.getCanonicalName()),
												LoggerLevel.ERROR);
					}
				}
			} //end of 'for'
		}

		return venPartyList;
	}

	/**
	 * Retreives an existing party from the cache along with contact and address
	 * details
	 * 
	 * @param name
	 * @return the party if it exists else null
	 */
	@Override
	public VenParty retrieveExistingParty(String name, boolean findByCustomer)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"retrieveExistingParty::BEGIN, name = " + name);
		VenParty party = null;
		if (findByCustomer) {
			List<VenCustomer> customerList = customerService.findByCustomerName(
					JPQLStringEscapeUtility.escapeJPQLStringData(name, ""));
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "retrieveExistingParty::finding by customerUserName, data found = "
					+ (customerList != null ? customerList.size() : 0));
			if (customerList != null && (!customerList.isEmpty())) {
				party = customerList.get(0).getVenParty();
			}
		} else {
			List<VenParty> venParties = venPartyDAO.findByLegalName(JPQLStringEscapeUtility.escapeJPQLStringData(name, ""));
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "retrieveExistingParty::venParties found = " + (venParties != null ? venParties.size() : 0));
			if (venParties != null && (!venParties.isEmpty())) {
				party = venParties.get(0);
			}
		}
	    	
		if (party != null) {
			//Fetch the list of contact details for the party
			List<VenContactDetail> venContactDetailList = contactDetailService.findByParty(party);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "retrieveExistingParty::venContactDetailList found = " + (venContactDetailList != null ? venContactDetailList.size() : 0));
			party.setVenContactDetails(venContactDetailList);

			//Fetch the list of party addresses for the party
			List<VenPartyAddress> venPartyAddresses = partyAddressService.findByVenParty(party);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "retrieveExistingParty::Total existing VenPartyAddress found = "
							+ (venPartyAddresses != null ? venPartyAddresses.size() : 0));
			party.setVenPartyAddresses(venPartyAddresses);
		}
		
		return party;
	}
}
