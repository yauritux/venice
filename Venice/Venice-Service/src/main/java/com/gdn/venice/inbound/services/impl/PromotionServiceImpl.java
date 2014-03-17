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
import com.gdn.venice.dao.VenPromotionDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.PromotionService;
import com.gdn.venice.persistence.VenPromotion;
import com.gdn.venice.persistence.VenPromotionType;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 * 
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PromotionServiceImpl implements PromotionService {

	@Autowired
	private VenPromotionDAO venPromotionDAO;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public List<VenPromotion> synchronizeVenPromotionReferences(
			List<VenPromotion> promotionReferences)
			throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenPromotionReferences::BEGIN, promotionReferences = "
						+ promotionReferences);

		List<VenPromotion> synchronizedPromotionRefs = new ArrayList<VenPromotion>();

		for (VenPromotion promotion : promotionReferences) {
			if (promotion.getPromotionCode() != null) {
				try {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"synchronizeVenPromotionReferences::Synchronizing VenPromotion... :"
									+ promotion.getPromotionCode());
					VenPromotion venPromotion = new VenPromotion();

					List<VenPromotion> promotionExactList = venPromotionDAO
							.findByPromotionAndMargin(
									promotion.getPromotionCode(),
									promotion.getPromotionName(),
									promotion.getGdnMargin(),
									promotion.getMerchantMargin(),
									promotion.getOthersMargin());

					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"synchronizeVenPromotionReferences::promotionExactList: "
									+ promotionExactList);

					if (promotionExactList != null
							&& (promotionExactList.size() > 0)) {
						CommonUtil
								.logDebug(this.getClass().getCanonicalName(),
										"synchronizeVenPromotionReferences::exact promo found");
						venPromotion = promotionExactList.get(0);
						if (venPromotion.getVenPromotionType() == null
								|| venPromotion.getVenPromotionType()
										.getPromotionType() == null) {
							if (venPromotion.getPromotionName().toLowerCase()
									.contains("free shipping")) {
								venPromotion = promotion;								
								VenPromotionType type = new VenPromotionType();
								type.setPromotionType(VeniceConstants.VEN_PROMOTION_TYPE_FREESHIPPING);
								venPromotion.setVenPromotionType(type);
								/*
								if (!em.contains(promotion)) {
									// promotion is not in attach mode, hence should call save explicitly
									venPromotion = venPromotionDAO.save(promotion);
								}
								*/								
							}
						}
					} else {
						CommonUtil
								.logDebug(
										this.getClass().getCanonicalName(),
										"synchronizeVenPromotionReferences::exact promo not found, check uploaded promo");
						List<VenPromotion> promotionUploadedList = venPromotionDAO
								.findByPromotionAndMargin(
										promotion.getPromotionCode(), null,
										null, null, null);
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"synchronizeVenPromotionReferences::promotionUploadedList = "
										+ promotionUploadedList);
						if (promotionUploadedList != null
								&& (promotionUploadedList.size() > 0)) {
							CommonUtil
									.logDebug(
											this.getClass().getCanonicalName(),
											"synchronizeVenPromotionReferences::uploaded promo found, set the promo name and margins and then merge");
							venPromotion = promotionUploadedList.get(0);
							venPromotion.setPromotionName(promotion
									.getPromotionName());
							venPromotion.setGdnMargin(promotion.getGdnMargin());
							venPromotion.setMerchantMargin(promotion
									.getMerchantMargin());
							venPromotion.setOthersMargin(promotion
									.getOthersMargin());
							/*
							if (!em.contains(venPromotion)) {
								// venPromotion is in detach mode, hence should call save explicitly 
								venPromotion = venPromotionDAO.save(venPromotion);
							}
							*/
							CommonUtil
									.logDebug(this.getClass()
											.getCanonicalName(),
											"synchronizeVenPromotionReferences::successfully saved promotion into database");
						} else {
							CommonUtil
									.logDebug(
											this.getClass().getCanonicalName(),
											"synchronizeVenPromotionReferences::no exact matching promo code, no uploaded promo, persist promo from inbound");
							/*
							if (!em.contains(promotion)) {
								// promotion is in detach mode, hence should call save explicitly
								venPromotion = venPromotionDAO.save(promotion);
							}
							*/
							venPromotion = promotion;

							// check the promo code for free shipping
							if (venPromotion.getVenPromotionType() == null
									|| venPromotion.getVenPromotionType()
											.getPromotionType() == null) {
								if (venPromotion.getPromotionName()
										.toLowerCase()
										.contains("free shipping")) {
									VenPromotionType type = new VenPromotionType();
									type.setPromotionType(VeniceConstants.VEN_PROMOTION_TYPE_FREESHIPPING);
									venPromotion.setVenPromotionType(type);
									/*
									if (!em.contains(venPromotion)) {
										// venPromotion is in detach mode, hence should call save explicitly
										venPromotion = venPromotionDAO.save(venPromotion);
									}
									*/
									CommonUtil
											.logDebug(this.getClass()
													.getCanonicalName(),
													"synchronizeVenPromotionReferences::successfully saved promotion");
								}
							}
						}
					}
					CommonUtil
							.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenPromotionReferences::adding venPromotion into retVal");
					if (em.contains(venPromotion)) {
						em.detach(venPromotion);
					}
					synchronizedPromotionRefs.add(venPromotion);
					CommonUtil
							.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenPromotionReferences::successfully added venPromotion into retVal");
				} catch (Exception e) {
					throw CommonUtil
							.logAndReturnException(
									new VeniceInternalException(
											"An unknown exception occured inside synchronizeReferenceData method"),
									CommonUtil.getLogger(this.getClass()
											.getCanonicalName()),
									LoggerLevel.ERROR);
				}
			} 

		} // end of 'for'
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPromotionReferences::EOM, returning synchronizedPromotionRefs = " + synchronizedPromotionRefs.size());
		return synchronizedPromotionRefs;		
	}
}
