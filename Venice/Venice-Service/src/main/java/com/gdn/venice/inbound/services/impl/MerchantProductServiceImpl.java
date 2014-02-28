package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.VenPartyTypeConstants;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenPartyDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.MerchantProductService;
import com.gdn.venice.inbound.services.MerchantService;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.inbound.services.ProductTypeService;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.persistence.VenProductType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class MerchantProductServiceImpl implements MerchantProductService {

	@Autowired
	private VenMerchantProductDAO venMerchantProductDAO;
	
	@Autowired
	private VenPartyDAO venPartyDAO;
	
	@Autowired 
	private MerchantService merchantService;
	
	@Autowired
	private PartyService partyService;
	
	@Autowired
	private ProductTypeService productTypeService;
	
	@Override
	public List<VenMerchantProduct> synchronizeVenMerchantProductRefs(
			List<VenMerchantProduct> merchantProductRefs)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductRefs::BEGIN, merchantProductRefs = " + merchantProductRefs);
		
		List<VenMerchantProduct> synchronizedMerchantProductRefs = new ArrayList<VenMerchantProduct>();
		
		for (VenMerchantProduct merchantProduct : merchantProductRefs) {
			if (merchantProduct.getWcsProductSku() != null) {
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductRefs::Synchronizing VenMerchantProduct... :" 
				          + merchantProduct.getWcsProductSku());
				
				merchantProduct = synchronizeVenMerchantProductReferenceData(merchantProduct);
				
				List<VenMerchantProduct> merchantProductList = venMerchantProductDAO.findByWcsProductSku(merchantProduct.getWcsProductSku());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantProductReferences::merchantProductList = " 
								+ merchantProductList);
				if (merchantProductList == null || (merchantProductList.size() == 0)) {
					VenMerchantProduct venMerchantProduct = venMerchantProductDAO.save(merchantProduct);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::adding venMerchantProduct into synchronizedMerchantProductRefs");
					synchronizedMerchantProductRefs.add(venMerchantProduct);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::successfully added venMerchantProduct into synchronizedMerchantProductRefs");
				} else {
					VenMerchantProduct venMerchantProduct = merchantProductList.get(0);
					synchronizedMerchantProductRefs.add(venMerchantProduct);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenMerchantProductReferences::successfully added venMerchantProduct into synchronizedMerchantProductRefs");
				}
			}
		} //end of 'for'
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductRefs::EOM, returning synchronizedMerchantProductRefs = "
				  + synchronizedMerchantProductRefs.size());
		return synchronizedMerchantProductRefs;
	}
	
	/**
	 * Synchronizes the data for the direct VenMerchantProduct references
	 * 
	 * @param venMerchantProduct
	 * @return the synchronized data object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchantProduct synchronizeVenMerchantProductReferenceData(
			VenMerchantProduct venMerchantProduct) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductReferenceData::BEGIN, venMerchantProduct = " + venMerchantProduct);
		
		if (venMerchantProduct.getVenProductType() != null) {
			List<VenProductType> productTypeRefs = new ArrayList<VenProductType>();
			productTypeRefs.add(venMerchantProduct.getVenProductType());
			productTypeRefs = productTypeService.synchronizeVenProductTypeReferences(productTypeRefs);
			for (VenProductType productType : productTypeRefs) {
				venMerchantProduct.setVenProductType(productType);
			}
		}
		
		/*
		if (venMerchantProduct.getVenMerchant() != null) {
			List<VenMerchant> merchantRefs = new ArrayList<VenMerchant>();
			merchantRefs.add(venMerchantProduct.getVenMerchant());
			//merchantRefs = discuss with team whether method synchronized for this particular class should be implemented or not
			for (VenMerchant merchant : merchantRefs) {
				venMerchantProduct.setVenMerchant(merchant);
			}
		}
		*/
		
		/*
		List<Object> references = new ArrayList<Object>();
		references.add(venMerchantProduct.getVenProductType());
		references.add(venMerchantProduct.getVenMerchant());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenProductType) {
				venMerchantProduct.setVenProductType((VenProductType) next);
			}else if(next instanceof VenMerchant){
				venMerchantProduct.setVenMerchant((VenMerchant) next);
			}
		}
		*/
				
		venMerchantProduct = venMerchantProductDAO.save(venMerchantProduct);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantProductReferenceData::EOM, returning venMerchantProduct = "
				  + venMerchantProduct);		
		
		return venMerchantProduct;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderItem> processMerchantProduct(List<String> merchantProduct, List<VenOrderItem> orderItems) 
	  throws VeniceInternalException {
		Pattern pattern = Pattern.compile("&");
		for(String party : merchantProduct){				
			String[] temp = pattern.split(party, 0);

			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "processMerchantProduct::show venParty in orderItem :  "+party);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "processMerchantProduct::string merchant :  "+temp[0]+" and "+temp[1]);

			if((temp[1] != null) && (!temp[1].trim().equals(""))){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "processMerchantProduct::temp[1] not empty");
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "processMerchantProduct::orderItems = " + orderItems);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "processMerchantProduct::orderItems size = " + (orderItems != null ? orderItems.size() : 0));
				
				for(int h =0; h < orderItems.size(); h++){
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchantProduct::h=" + h + ",orderItems=" + orderItems.get(h));
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchantProduct::venMerchantProduct SKU = " + orderItems.get(h).getVenMerchantProduct().getWcsProductSku());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchantProduct::venMerchant = " 
									+ orderItems.get(h).getVenMerchantProduct().getVenMerchant());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchantProduct::wcsMerchantId = " 
									+ orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId());
					if(orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId().equals(temp[0].trim())){
						List<VenMerchant> venMerchantList = merchantService.findByWcsMerchantId(temp[0]);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processMerchantProduct::venMerchantList found = " + venMerchantList);
						if (venMerchantList != null && venMerchantList.size() > 0) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "processMerchantProduct::venMerchantList size = " + venMerchantList.size());
							if (venMerchantList.get(0).getVenParty() == null) {
								List<VenParty> venPartyList = partyService.findByLegalName(temp[1]);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "processMerchantProduct::venPartyList found = " + venPartyList);
								if (venPartyList == null || venPartyList.size() == 0) { 
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "processMerchantProduct::venPartyList is empty, creating new one");
									VenParty venPartyitem = new VenParty();
									VenPartyType venPartyType = new VenPartyType();
									// set party type id = 1 adalah merchant
									venPartyType.setPartyTypeId(VenPartyTypeConstants.VEN_PARTY_TYPE_MERCHANT.code());
									venPartyitem.setVenPartyType(venPartyType);
									venPartyitem.setFullOrLegalName(temp[1]);	
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "processMerchantProduct::persist venParty :  "+venPartyitem.getFullOrLegalName());
									venPartyitem = venPartyDAO.save(venPartyitem);
									//venPartyitem = partyService.persistParty(venPartyitem, venPartyitem.getVenPartyType().getPartyTypeDesc());
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "processMerchantProduct::venPartyItem Type = " + venPartyitem.getVenPartyType().getPartyTypeDesc());
									venMerchantList.get(0).setVenParty(venPartyitem);
									VenMerchant venMerchant = venMerchantList.get(0);
									venMerchant = merchantService.persist(venMerchant);
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "processMerchantProduct::added  new party for venmerchant (Merchant Id :"
													+ orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() +" )"
											);
									orderItems.get(h).getVenMerchantProduct().setVenMerchant(venMerchant);									
								}else{
									venMerchantList.get(0).setVenParty(venPartyList.get(0));
									VenMerchant venMerchant = venMerchantList.get(0);
									venMerchant = merchantService.persist(venMerchant);
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "processMerchantProduct::add  party for venmerchant (Merchant Id :"
													+ orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() +" )"
											);			
									orderItems.get(h).getVenMerchantProduct().setVenMerchant(venMerchant);
								}
							}
						}
					}

					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processMerchantProduct::synchronizing venMerchantProduct referenceData");
					VenMerchantProduct synchronizedMerchantProduct = null;
					try {
						synchronizedMerchantProduct = synchronizeVenMerchantProductReferenceData(
								orderItems.get(h).getVenMerchantProduct());					
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processMerchantProduct::venMerchantProduct referenceData is synchronized now");					
						orderItems.get(h).setVenMerchantProduct(synchronizedMerchantProduct);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processMerchantProduct::added synchronizedMerchantProduct into orderItem");
					} catch (Exception e) {
						CommonUtil.logError(this.getClass().getCanonicalName(), e);
					}
				} //end of for orderItems
			}else {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "processMerchantProduct::party is null for venmerchant (Merchant Id :"+ temp[0] +" )");
			}

		} //EOF for		
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "processMerchantProduct::returning orderItems = "+ (orderItems != null ? orderItems.size() : 0));
		return orderItems;
	}

}
