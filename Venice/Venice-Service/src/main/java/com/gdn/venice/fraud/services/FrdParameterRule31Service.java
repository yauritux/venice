package com.gdn.venice.fraud.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.FrdParameterRule31;

/**
 * 
 * @author yauritux
 *
 */
public interface FrdParameterRule31Service {

	public List<FrdParameterRule31> findByEmailAndNoCc(final String email, final String noCc)
	   throws VeniceInternalException;
}
