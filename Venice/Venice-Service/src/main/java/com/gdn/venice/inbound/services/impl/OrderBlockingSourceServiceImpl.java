package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderBlockingSourceDAO;
import com.gdn.venice.exception.OrderBlockingSourceNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderBlockingSourceService;
import com.gdn.venice.persistence.VenOrderBlockingSource;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderBlockingSourceServiceImpl implements OrderBlockingSourceService {

	@Autowired
	private VenOrderBlockingSourceDAO venOrderBlockingSourceDAO;
	
	@Override
	public List<VenOrderBlockingSource> synchronizeVenOrderBlockingSourceReferences(
			List<VenOrderBlockingSource> orderBlockingSourceReferences) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderBlockingSourceReferences::BEGIN, orderBlockingSourceReferences="
				  + orderBlockingSourceReferences);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderBlockingSourceReferences::preparing synchronizedOrderBlockingSource");
		List<VenOrderBlockingSource> synchronizedOrderBlockingSource
		    = new ArrayList<VenOrderBlockingSource>();
		
		if (orderBlockingSourceReferences != null) {
			for (VenOrderBlockingSource orderBlockingSource : orderBlockingSourceReferences) {
				//em.detach(orderBlockingSource);
				if (orderBlockingSource.getBlockingSourceDesc() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderBlockingSourceReferences::Restricting VenOrderBlockingSource... :" 
									+ orderBlockingSource.getBlockingSourceDesc());

					VenOrderBlockingSource venOrderBlockingSource 
					= venOrderBlockingSourceDAO.findByBlockingSourceDesc(orderBlockingSource.getBlockingSourceDesc());

					if (venOrderBlockingSource == null) {
						throw CommonUtil.logAndReturnException(new OrderBlockingSourceNotFoundException(
								"Order blocking source does not exist", VeniceExceptionConstants.VEN_EX_000026)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						synchronizedOrderBlockingSource.add(venOrderBlockingSource);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenOrderBlockingSourceReferences::successfully added venOrderBlockingSource into synchronizedOrderBlockingSource");
					}
				}

			} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderBlockingSourceReferences::returning synchronizedOrderBlockingSource = "
				   + synchronizedOrderBlockingSource.size());
		return synchronizedOrderBlockingSource;
	}
}
