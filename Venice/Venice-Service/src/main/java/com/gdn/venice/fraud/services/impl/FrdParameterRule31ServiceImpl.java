package com.gdn.venice.fraud.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.FrdParameterRule31DAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.fraud.services.FrdParameterRule31Service;
import com.gdn.venice.persistence.FrdParameterRule31;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class FrdParameterRule31ServiceImpl implements FrdParameterRule31Service {

	@Autowired
	private FrdParameterRule31DAO frdParameterRule31DAO;
	
	@Override
	public List<FrdParameterRule31> findByEmailAndNoCc(String email, String noCc)
			throws VeniceInternalException {
		List<FrdParameterRule31> frdParameterRule31Lst = new ArrayList<FrdParameterRule31>();
		try {
			frdParameterRule31Lst = frdParameterRule31DAO.findByEmailAndNoCc(email, noCc);
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
		}
		return frdParameterRule31Lst;
	}

	
}
