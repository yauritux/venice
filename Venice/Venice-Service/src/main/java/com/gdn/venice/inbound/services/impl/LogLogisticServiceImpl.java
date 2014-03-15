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
import com.gdn.venice.dao.LogLogisticServiceDAO;
import com.gdn.venice.exception.LogLogisticServiceNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.LogLogisticService;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class LogLogisticServiceImpl implements com.gdn.venice.inbound.services.LogLogisticService {

	@Autowired
	private LogLogisticServiceDAO logLogisticServiceDAO;
	
	@PersistenceContext
	EntityManager em;
	
	@Override
	public List<LogLogisticService> synchronizeLogLogisticServiceReferences(
			List<LogLogisticService> logLogisticServiceRefs)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeLogLogisticServiceReferences::BEGIN,logLogisticServiceRefs = " + logLogisticServiceRefs);

		List<LogLogisticService> synchronizedLogLogisticServiceRefs
		   = new ArrayList<LogLogisticService>();
		
		for (LogLogisticService logLogisticService : logLogisticServiceRefs) {
			em.detach(logLogisticService);
			if (logLogisticService.getServiceCode() != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeLogLogisticServiceReferences::Restricting LogLogisticService... :" 
				          + logLogisticService.getServiceCode());
				LogLogisticService logisticService = logLogisticServiceDAO.findByServiceCode(logLogisticService.getServiceCode());
				if (logisticService == null) {
					throw CommonUtil.logAndReturnException(new LogLogisticServiceNotFoundException(
							"Logistics service does not exist", VeniceExceptionConstants.VEN_EX_500001)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				} else {
					synchronizedLogLogisticServiceRefs.add(logisticService);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeLogLogisticServiceReferences::successfully added logisticService into synchronizedLogLogisticServiceRefs");
				}
			}
		} // end of 'for'
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeLogLogisticServiceReferences::EOM, returning synchronizedLogLogisticServiceRefs = "
				   + synchronizedLogLogisticServiceRefs.size());
		return synchronizedLogLogisticServiceRefs;
	}
}
