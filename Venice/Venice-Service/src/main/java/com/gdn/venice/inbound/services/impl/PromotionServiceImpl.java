package com.gdn.venice.inbound.services.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenPromotionDAO;
import com.gdn.venice.exception.VenPromotionSynchronizingError;
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
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<VenPromotion> findByPromotionAndMargin(VenPromotion venPromotion) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "findByPromotionAndMargin::BEGIN,venPromotion = " + venPromotion);
		
		List<VenPromotion> promotionExactList = venPromotionDAO
				.findByPromotionAndMargin(venPromotion.getPromotionCode(),
						venPromotion.getPromotionName(),
						venPromotion.getGdnMargin(),
						venPromotion.getMerchantMargin(),
						venPromotion.getOthersMargin());
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "findByPromotionAndMargin::END,returning promotionExactList="
				+ (promotionExactList != null ? promotionExactList.size() : 0));
		
		return promotionExactList;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenPromotion synchronizeVenPromotionReferences(
			VenPromotion venPromotion) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenPromotionReferences::BEGIN, venPromotion = "
						+ venPromotion);

		VenPromotion synchronizedPromotion = venPromotion;
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenPromotionReferences::promotionId = " + venPromotion.getPromotionId());

		if (venPromotion.getPromotionId() == null && venPromotion.getPromotionCode() != null) {
			// for promotionId IS NOT NULL, we don't need to perform the synchronization again, hence these following lines merely executed for 'NULL' promotionId 
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenPromotionReferences::Synchronizing VenPromotion... :" + venPromotion.getPromotionCode());

				
				List<VenPromotion> promotionExactList = findByPromotionAndMargin(venPromotion);

				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenPromotionReferences::promotionExactList: " + promotionExactList);

				if (promotionExactList != null && (!promotionExactList.isEmpty())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenPromotionReferences::exact promo found");
					synchronizedPromotion = promotionExactList.get(0);
					if (synchronizedPromotion.getVenPromotionType() == null || synchronizedPromotion.getVenPromotionType().getPromotionType() == null) {
						if (synchronizedPromotion.getPromotionName().toLowerCase().contains("free shipping")) {
							//synchronizedPromotion = venPromotion;
							VenPromotionType type = new VenPromotionType();
							type.setPromotionType(VeniceConstants.VEN_PROMOTION_TYPE_FREESHIPPING);
							synchronizedPromotion.setVenPromotionType(type);
							if (!em.contains(synchronizedPromotion)) {
								// promotion is not in attach mode, hence should
								// call save explicitly
								CommonUtil.logDebug(this.getClass().getCanonicalName(),
												"synchronizeVenPromotionReferences::calling venPromotionDAO save explicitly");
								synchronizedPromotion = venPromotionDAO.save(synchronizedPromotion);
							}
						}
					}
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenPromotionReferences::exact promo not found, check uploaded promo");
					List<VenPromotion> promotionUploadedList = venPromotionDAO.findByPromotionAndMargin(
									venPromotion.getPromotionCode(), null, null, null, null);
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"synchronizeVenPromotionReferences::promotionUploadedList = " + promotionUploadedList);
					if (promotionUploadedList != null && (!promotionUploadedList.isEmpty())) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
										"synchronizeVenPromotionReferences::uploaded promo found, set the promo name and margins and then merge");
						synchronizedPromotion = promotionUploadedList.get(0);
						synchronizedPromotion.setPromotionName(venPromotion.getPromotionName());
						synchronizedPromotion.setGdnMargin(venPromotion.getGdnMargin());
						synchronizedPromotion.setMerchantMargin(venPromotion.getMerchantMargin());
						synchronizedPromotion.setOthersMargin(venPromotion.getOthersMargin());
						
						if (!em.contains(synchronizedPromotion)) { // synchronizedPromotion is in detach mode, hence should call save explicitly
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenPromotionReferences::calling venPromotionDAO.save explicitly");
							synchronizedPromotion = venPromotionDAO.save(synchronizedPromotion); 
						}
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
										"synchronizeVenPromotionReferences::successfully saved synchronizedPromotion into database");
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
										"synchronizeVenPromotionReferences::no exact matching promo code, no uploaded promo, persist promo from inbound");

						if (!em.contains(synchronizedPromotion)) {
							// synchronizedPromotion is in detach mode, hence should call
							// save explicitly
							CommonUtil.logDebug(this.getClass().getCanonicalName(),
											"synchronizeVenPromotionReferences::calling venPromotionDAO save explicitly");
							synchronizedPromotion = venPromotionDAO.save(synchronizedPromotion);
						} else {
							CommonUtil.logDebug(this.getClass().getCanonicalName(),
											"synchronizeVenPromotionReferences::synchronizedPromotion is in attached mode, no need to call save explicitly");
						}

						// check the promo code for free shipping
						if (synchronizedPromotion.getVenPromotionType() == null || synchronizedPromotion.getVenPromotionType().getPromotionType() == null) {
							if (synchronizedPromotion.getPromotionName().toLowerCase().contains("free shipping")) {
								VenPromotionType type = new VenPromotionType();
								type.setPromotionType(VeniceConstants.VEN_PROMOTION_TYPE_FREESHIPPING);
								synchronizedPromotion.setVenPromotionType(type);

								if (!em.contains(synchronizedPromotion)) {
									// synchronizedPromotion is in detach mode, hence
									// should call save explicitly
									CommonUtil.logDebug(this.getClass().getCanonicalName(),
													"synchronizeVenPromotionReferences::calling venPromotionDAO save explicitly");
									synchronizedPromotion = venPromotionDAO.save(synchronizedPromotion);
								}
								CommonUtil.logDebug(this.getClass().getCanonicalName(),
												"synchronizeVenPromotionReferences::successfully saved synchronizedPromotion");
							}
						}
					}
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				e.printStackTrace();
				throw CommonUtil.logAndReturnException(new VenPromotionSynchronizingError("Cannot synchronize VenPromotion", VeniceExceptionConstants.VEN_EX_130009),
								CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		} //end if promotionCode IS NOT NULL and promotionId IS NULL

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenPromotionReferences::EOM, returning synchronizedPromotion = " + synchronizedPromotion);
		return synchronizedPromotion;
	}
}
