package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule42;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule42SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule42
	 */
	public List<FrdParameterRule42> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule42 persists a country
	 * 
	 * @param frdParameterRule42
	 * @return the persisted FrdParameterRule42
	 */
	public FrdParameterRule42 persistFrdParameterRule42(FrdParameterRule42 frdParameterRule42);

	/**
	 * persistFrdParameterRule42List - persists a list of FrdParameterRule42
	 * 
	 * @param frdParameterRule42List
	 * @return the list of persisted FrdParameterRule42
	 */
	public ArrayList<FrdParameterRule42> persistFrdParameterRule42List(
			List<FrdParameterRule42> frdParameterRule42List);

	/**
	 * mergeFrdParameterRule42 - merges a FrdParameterRule42
	 * 
	 * @param frdParameterRule42
	 * @return the merged FrdParameterRule42
	 */
	public FrdParameterRule42 mergeFrdParameterRule42(FrdParameterRule42 frdParameterRule42);

	/**
	 * mergeFrdParameterRule42List - merges a list of FrdParameterRule42
	 * 
	 * @param frdParameterRule42List
	 * @return the merged list of FrdParameterRule42
	 */
	public ArrayList<FrdParameterRule42> mergeFrdParameterRule42List(
			List<FrdParameterRule42> frdParameterRule42List);

	/**
	 * removeFrdParameterRule42 - removes a FrdParameterRule42
	 * 
	 * @param frdParameterRule42
	 */
	public void removeFrdParameterRule42(FrdParameterRule42 frdParameterRule42);

	/**
	 * removeFrdParameterRule42List - removes a list of FrdParameterRule42
	 * 
	 * @param frdParameterRule42List
	 */
	public void removeFrdParameterRule42List(List<FrdParameterRule42> frdParameterRule42List);

	/**
	 * findByFrdParameterRule42Like - finds a list of FrdParameterRule42 Like
	 * 
	 * @param frdParameterRule42
	 * @return the list of FrdParameterRule42 found
	 */
	public List<FrdParameterRule42> findByFrdParameterRule42Like(FrdParameterRule42 frdParameterRule42,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule42>LikeFR - finds a list of FrdParameterRule42> Like with a finder return object
	 * 
	 * @param frdParameterRule42
	 * @return the list of FrdParameterRule42 found
	 */
	public FinderReturn findByFrdParameterRule42LikeFR(FrdParameterRule42 frdParameterRule42,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
