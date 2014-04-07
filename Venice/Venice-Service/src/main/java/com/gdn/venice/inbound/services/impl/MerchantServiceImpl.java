package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VenPartyTypeConstants;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenMerchantDAO;
import com.gdn.venice.exception.CannotPersistMerchantException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.MerchantService;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class MerchantServiceImpl implements MerchantService {
	
	@Autowired
	private VenMerchantDAO venMerchantDAO;
	
	@Autowired
	private PartyService partyService;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public List<VenMerchant> findByWcsMerchantId(String wcsMerchantId) {
		return venMerchantDAO.findByWcsMerchantId(wcsMerchantId);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean processMerchant(List<String> merchantProducts, List<VenOrderItem> orderItems) {
		Pattern pattern = Pattern.compile("&");
		
		try {
			
			for(String party : merchantProducts){				
				String[] temp = pattern.split(party, 0);

				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "processMerchant::show venParty in orderItem :  " + party);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "processMerchant::string merchant :  " + temp[0] + " and " + temp[1]);

				if((temp[1] != null) && (!temp[1].trim().equals(""))) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchant::temp[1] not empty");
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchant::orderItems = " + (orderItems != null ? orderItems.size() : 0));

					for(int h =0; h < orderItems.size(); h++){
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processMerchant::venMerchantProduct#wcsProductSku = "
										+ (orderItems.get(h) != null ? orderItems.get(h).getVenMerchantProduct().getWcsProductSku() : null));
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processMerchant::wcsMerchantId = " 
										+ (orderItems.get(h) != null && orderItems.get(h).getVenMerchantProduct() != null ? 
												orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() : null));
						if(orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId().equalsIgnoreCase(temp[0].trim())){
							List<VenMerchant> venMerchantList = findByWcsMerchantId(temp[0]);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "processMerchant::venMerchantList found = " + (venMerchantList != null ? venMerchantList.size() : 0));
							if (venMerchantList != null && (!venMerchantList.isEmpty())) {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "processMerchant::venMerchantList size = " + venMerchantList.size());
								if (venMerchantList.get(0).getVenParty() == null) {
									List<VenParty> venPartyList = partyService.findByLegalName(temp[1] != null ? temp[1].trim() : temp[1]);
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "processMerchant::venPartyList found = " + (venPartyList != null ? venPartyList.size() : 0));
									if (venPartyList == null || (venPartyList.isEmpty())) { 
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "processMerchant::venPartyList is empty, creating new one");
										VenParty venPartyitem = new VenParty();
										VenPartyType venPartyType = new VenPartyType();
										venPartyType.setPartyTypeId(VenPartyTypeConstants.VEN_PARTY_TYPE_MERCHANT.code());
										venPartyitem.setVenPartyType(venPartyType);
										venPartyitem.setFullOrLegalName(temp[1].trim());	
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "processMerchant::persisting venParty :  " + venPartyitem.getFullOrLegalName());

										venPartyitem = partyService.persistParty(venPartyitem, "Merchant");
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "processMerchant::venParty successfully persisted");

										venMerchantList.get(0).setVenParty(venPartyitem);
										VenMerchant venMerchant = venMerchantList.get(0);
										persist(venMerchant);
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "createOrder::added  new party for venmerchant (Merchant Id :"
														+ orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() +" )"
												);			
									}else{
										venMerchantList.get(0).setVenParty(venPartyList.get(0));
										VenMerchant venMerchant = venMerchantList.get(0);
										persist(venMerchant);
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "createOrder::add  party for venmerchant (Merchant Id :"
														+ orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() +" )"
												);						
									}
								}
							}
						}

					}
				}else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::party is null for venmerchant (Merchant Id :"+ temp[0] +" )");
				}

			} //EOF for
		
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			e.printStackTrace();
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchant synchronizeVenMerchantData(VenMerchant venMerchant)
	   throws VeniceInternalException {
		VenMerchant merchant = venMerchant;
		if (venMerchant != null) {
			List<VenMerchant> merchantList = findByWcsMerchantId(venMerchant.getWcsMerchantId());
			if (merchantList != null && (!merchantList.isEmpty())) {
				merchant = merchantList.get(0);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::found venMerchant WCS Merchant ID = "  + merchant.getWcsMerchantId());	
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::found venMerchant Merchant ID = "  + merchant.getMerchantId());					
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::venMerchant is not listed in the DB, saving it");
				merchant = persist(venMerchant);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::successfully persisted venMerchant");
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::new venMerchant ID = " + merchant.getMerchantId());				
			}
		} 
		return merchant;
	}

	@Override
	public List<VenMerchant> synchronizeVenMerchantReferences(
			List<VenMerchant> merchantRefs) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantReferences::BEGIN");
		
		List<VenMerchant> synchronizedMerchantReferences = new ArrayList<VenMerchant>();
		
		for (VenMerchant merchant : merchantRefs) {	
			VenMerchant synchMerchant = synchronizeVenMerchantData(merchant);
			synchronizedMerchantReferences.add(synchMerchant);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantReferences::EOF, returning synchronizedMerchantReferences = "
				+ synchronizedMerchantReferences.size());
		return synchronizedMerchantReferences;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchant persist(VenMerchant venMerchant)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::BEGIN, venMerchant = " + venMerchant);
		
		VenMerchant persistedVenMerchant = null;
		
		if (venMerchant != null) {
			if (!em.contains(venMerchant)) {
				// venMerchant is in detach mode, hence should call save explicitly as shown below
				try {
					persistedVenMerchant = venMerchantDAO.save(venMerchant);
				} catch (Exception e) {
					CommonUtil.logAndReturnException(new CannotPersistMerchantException(
							"Cannot persist VenMerchant," + e, VeniceExceptionConstants.VEN_EX_120001)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
			}
		}
		
		persistedVenMerchant  = venMerchant;
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::EOM, returning persistedVenMerchant = " + persistedVenMerchant);
		return persistedVenMerchant;
	}

}