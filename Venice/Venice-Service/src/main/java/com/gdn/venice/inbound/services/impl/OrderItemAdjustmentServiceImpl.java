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
import com.gdn.venice.dao.VenOrderItemAdjustmentDAO;
import com.gdn.venice.exception.CannotPersistOrderItemAdjustmentsException;
import com.gdn.venice.exception.VenOrderItemAdjustmentSynchronizingError;
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
	public List<VenOrderItemAdjustment> persistOrderItemAdjustmentList(VenOrderItem venOrderItem,
			List<VenOrderItemAdjustment> venOrderItemAdjustmentList) throws VeniceInternalException {
		List<VenOrderItemAdjustment> newVenOrderItemAdjustmentList = new ArrayList<VenOrderItemAdjustment>();
		if (venOrderItemAdjustmentList != null && !venOrderItemAdjustmentList.isEmpty()) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrderItemAdjustmentList::Persisting VenOrderItemAdjustment list...:" 
				           + venOrderItemAdjustmentList.size());
				for (VenOrderItemAdjustment venOrderItemAdjustment : venOrderItemAdjustmentList) {

					VenOrderItemAdjustment synchOrderItemAdjustment = synchronizeVenOrderItemAdjustmentReferenceData(venOrderItemAdjustment);
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemAdjustmentList::synchOrderItemadjustment = " + synchOrderItemAdjustment);
					
					VenPromotion venPromotion = synchOrderItemAdjustment.getVenPromotion();
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemAdjustmentList::venPromotion = " + venPromotion);
					
					// Attach the order item
					synchOrderItemAdjustment.setVenOrderItem(venOrderItem);
					// Attach a primary key
					VenOrderItemAdjustmentPK id = new VenOrderItemAdjustmentPK();
					id.setOrderItemId(venOrderItem.getOrderItemId());					
					//id.setPromotionId(next.getVenPromotion().getPromotionId());
					id.setPromotionId(venPromotion.getPromotionId());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemAdjustmentList::id.getOrderItemId: "+id.getOrderItemId());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemAdjustmentList::id.getPromotionId: "+id.getPromotionId());
					synchOrderItemAdjustment.setId(id);
					// Persist the object
					List<VenOrderItemAdjustment> existingVenOrderItemAdjustments = venOrderItemAdjustmentDAO.findByOrderItemAndPromotion(venOrderItem, venPromotion);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemAdjustmentList::existingVenOrderItemAdjustments.size: "
					           + (existingVenOrderItemAdjustments != null ? existingVenOrderItemAdjustments.size() : 0));
					if(existingVenOrderItemAdjustments != null && (!(existingVenOrderItemAdjustments.isEmpty()))) {
						if(synchOrderItemAdjustment.getAdminDesc() == null || synchOrderItemAdjustment.getAdminDesc().trim().equals("")){
							synchOrderItemAdjustment.setAdminDesc("-");
							CommonUtil.logInfo(CommonUtil.getLogger(this.getClass().getCanonicalName())
									, "persistOrderItemAdjustmentList::Set adminDesc to (-) if null or empty");
						}
						//newVenOrderItemAdjustmentList.add((VenOrderItemAdjustment) venOrderItemAdjustmentDAO.save(next));
						newVenOrderItemAdjustmentList.add(synchOrderItemAdjustment);
					}else{
						//newVenOrderItemAdjustmentList.add((VenOrderItemAdjustment) next);
						newVenOrderItemAdjustmentList.add(synchOrderItemAdjustment);						
					}					
				}
			} catch (Exception e) {
				String errMsg = "An exception occured when persisting VenOrderItemAdjustment:";
				e.printStackTrace();
				throw CommonUtil.logAndReturnException(new CannotPersistOrderItemAdjustmentsException(errMsg, VeniceExceptionConstants.VEN_EX_120003)
				  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderItemAdjustmentList::returning newVenOrderItemAdjustmentList = " +  newVenOrderItemAdjustmentList);
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
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderItemAdjustment synchronizeVenOrderItemAdjustmentReferenceData(
			VenOrderItemAdjustment venOrderItemAdjustment)
			throws VeniceInternalException {
		
		try {
			if (venOrderItemAdjustment.getVenPromotion() != null) {
				VenPromotion promotion = venOrderItemAdjustment.getVenPromotion();
				if (promotion.getPromotionName() == null || promotion.getPromotionName().trim().equals("")) {
					promotion.setPromotionName("-");
				}
				if (promotion.getPromotionCode() == null || promotion.getPromotionCode().trim().equals("")) {
					promotion.setPromotionCode("-");
				}

				VenPromotion synchronizedPromotion = promotionService.synchronizeVenPromotionReferences(promotion);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenOrderItemAdjustmentReferenceData::venPromotion is being synchronized now, -> " + synchronizedPromotion);
				venOrderItemAdjustment.setVenPromotion(synchronizedPromotion);
				// Set the adjusment side of the primary key
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderItemAdjustmentReferenceData::promotionId = " 
						+ venOrderItemAdjustment.getVenPromotion().getPromotionId());
				VenOrderItemAdjustmentPK pk = new VenOrderItemAdjustmentPK();
				pk.setPromotionId(venOrderItemAdjustment.getVenPromotion().getPromotionId());
				venOrderItemAdjustment.setId(pk);			
			}
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			e.printStackTrace();
			CommonUtil.logAndReturnException(new VenOrderItemAdjustmentSynchronizingError("Cannot synchronize VenOrderItemAdjustment !"
					, VeniceExceptionConstants.VEN_EX_130010), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
			  , "synchronizeVenOrderItemAdjustmentReferenceData::END, returning venOrderItemAdjustment = " + venOrderItemAdjustment);
		return venOrderItemAdjustment;
	}
}
