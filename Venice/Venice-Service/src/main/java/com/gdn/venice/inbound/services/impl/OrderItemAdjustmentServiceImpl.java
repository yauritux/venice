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

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderItemAdjustmentDAO;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderItemAdjustmentService;
import com.gdn.venice.inbound.services.PromotionService;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemAdjustment;
import com.gdn.venice.persistence.VenOrderItemAdjustmentPK;
import com.gdn.venice.persistence.VenPromotion;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderItemAdjustmentServiceImpl implements OrderItemAdjustmentService {

	@Autowired
	private VenOrderItemAdjustmentDAO venOrderItemAdjustmentDAO;
	
	@Autowired
	private PromotionService promotionService;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * Persists a list of order item marginPromo
	 * 
	 * @param venOrderItemAdjustment
	 * @return the persisted object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderItemAdjustment> persistOrderItemAdjustmentList(
			VenOrderItem venOrderItem,
			List<VenOrderItemAdjustment> venOrderItemAdjustmentList)
			throws VeniceInternalException {
		List<VenOrderItemAdjustment> newVenOrderItemAdjustmentList = new ArrayList<VenOrderItemAdjustment>();
		if (venOrderItemAdjustmentList != null && !venOrderItemAdjustmentList.isEmpty()) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::persistOrderItemAdjustmentList::Persisting VenOrderItemAdjustment list...:" 
				           + venOrderItemAdjustmentList.size());
				Iterator<VenOrderItemAdjustment> i = venOrderItemAdjustmentList.iterator();
				while (i.hasNext()) {
					VenOrderItemAdjustment next = i.next();
					// Synchronize the references
					next = this.synchronizeVenOrderItemAdjustmentReferenceData(next);

					VenPromotion venPromotion = next.getVenPromotion();
					// Attach the order item
					next.setVenOrderItem(venOrderItem);
					// Attach a primary key
					VenOrderItemAdjustmentPK id = new VenOrderItemAdjustmentPK();
					id.setOrderItemId(venOrderItem.getOrderItemId());					
					//id.setPromotionId(next.getVenPromotion().getPromotionId());
					id.setPromotionId(venPromotion.getPromotionId());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistOrderItemAdjustmentList::id.getOrderItemId: "+id.getOrderItemId());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistOrderItemAdjustmentList::id.getPromotionId: "+id.getPromotionId());
					next.setId(id);
					// Persist the object
					List<VenOrderItemAdjustment> existingVenOrderItemAdjustments = venOrderItemAdjustmentDAO.findByOrderItemAndPromotion(venOrderItem, venPromotion);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistOrderItemAdjustmentList::existingVenOrderItemAdjustments.size: "
					           + existingVenOrderItemAdjustments.size());
					if(existingVenOrderItemAdjustments.size() == 0){
						if(next.getAdminDesc() == null || next.getAdminDesc().equals("")){
							next.setAdminDesc("-");
							CommonUtil.logInfo(CommonUtil.getLogger(this.getClass().getCanonicalName())
									, "OrderServiceImpl::persistOrderItemAdjustmentList::Set adminDesc to (-) if null or empty");
						}
						VenOrderItemAdjustment venOrderItemAdjustment = next;
						if (em.contains(venOrderItemAdjustment)) {
							em.detach(venOrderItemAdjustment);
						}
						//newVenOrderItemAdjustmentList.add((VenOrderItemAdjustment) venOrderItemAdjustmentDAO.save(next));
						newVenOrderItemAdjustmentList.add(venOrderItemAdjustment);
					}else{
						VenOrderItemAdjustment venOrderItemAdjustment = next;						
						if (em.contains(venOrderItemAdjustment)) {
							em.detach(venOrderItemAdjustment);
						}						
						//newVenOrderItemAdjustmentList.add((VenOrderItemAdjustment) next);
						newVenOrderItemAdjustmentList.add(venOrderItemAdjustment);						
					}					
				}
			} catch (Exception e) {
				String errMsg = "An exception occured when persisting VenOrderItemAdjustment:";
				e.printStackTrace();
				throw CommonUtil.logAndReturnException(new InvalidOrderException(errMsg, VeniceExceptionConstants.VEN_EX_000022)
				  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::persistOrderItemAdjustmentList::successfully cloned newVenOrderItemAdjustmentList, returning it");
		return newVenOrderItemAdjustmentList;
	}

	/**
	 * Synchronizes the reference data for the direct VenOrderItemAdjustment
	 * references
	 * 
	 * @param venOrderItemAdjustment
	 * @return the synchronized data object
	 */	
	@Override
	public VenOrderItemAdjustment synchronizeVenOrderItemAdjustmentReferenceData(
			VenOrderItemAdjustment venOrderItemAdjustment)
			throws VeniceInternalException {
		
		if (venOrderItemAdjustment.getVenPromotion() != null) {
			VenPromotion promotion = venOrderItemAdjustment.getVenPromotion();
			if (promotion.getPromotionName() == null || promotion.getPromotionName().equals("")) {
				promotion.setPromotionName("-");
			}
			if (promotion.getPromotionCode() == null || promotion.getPromotionCode().equals("")) {
				promotion.setPromotionCode("-");
			}
			List<VenPromotion> promotionRefs = new ArrayList<VenPromotion>();
			if (em.contains(promotion)) {
				em.detach(promotion);
			}
			promotionRefs.add(promotion);
			
			promotionRefs = promotionService.synchronizeVenPromotionReferences(promotionRefs);
			
			for (VenPromotion venPromotion : promotionRefs) {
				venOrderItemAdjustment.setVenPromotion(venPromotion);
				// Set the adjustment side of the primary key
				VenOrderItemAdjustmentPK pk = new VenOrderItemAdjustmentPK();
				pk.setPromotionId(venOrderItemAdjustment.getVenPromotion().getPromotionId());
				venOrderItemAdjustment.setId(pk);
			}
		}
		
		/*
		List<Object> references = new ArrayList<Object>();	
		VenPromotion promotion = venOrderItemAdjustment.getVenPromotion();
		if(venOrderItemAdjustment.getVenPromotion().getPromotionName()==null || venOrderItemAdjustment.getVenPromotion().getPromotionName().equals("") )			
			promotion.setPromotionName("-");
		if(venOrderItemAdjustment.getVenPromotion().getPromotionCode()==null || venOrderItemAdjustment.getVenPromotion().getPromotionCode().equals("") )
			promotion.setPromotionCode("-");
		references.add(promotion);
		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenPromotion) {
				venOrderItemAdjustment.setVenPromotion((VenPromotion) next);
				// Set the adjustment side of the primary key
				VenOrderItemAdjustmentPK pk = new VenOrderItemAdjustmentPK();
				pk.setPromotionId(venOrderItemAdjustment.getVenPromotion().getPromotionId());
				venOrderItemAdjustment.setId(pk);
			}
		}
		*/

		CommonUtil.logDebug(this.getClass().getCanonicalName()
			  , "synchronizeVenOrderItemAdjustmentReferenceData::EOM, returning venOrderItemAdjustment = " + venOrderItemAdjustment);
		return venOrderItemAdjustment;
	}
}
