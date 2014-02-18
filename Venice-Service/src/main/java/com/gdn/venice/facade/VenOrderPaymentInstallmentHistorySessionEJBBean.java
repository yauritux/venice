package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.gdn.venice.facade.callback.SessionCallback;
import com.gdn.venice.facade.finder.FinderReturn;
import com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLQueryStringBuilder;
import com.djarum.raf.utilities.Log4jLoggerFactory;

/**
 * Session Bean implementation class VenOrderPaymentInstallmentHistorySessionEJBBean
 * 
 * <p>
 * <b>author:</b> <a href="mailto:david@pwsindonesia.com">David Forden</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 * 
 */
@Stateless(mappedName = "VenOrderPaymentInstallmentHistorySessionEJBBean")
public class VenOrderPaymentInstallmentHistorySessionEJBBean implements VenOrderPaymentInstallmentHistorySessionEJBRemote,
		VenOrderPaymentInstallmentHistorySessionEJBLocal {

	/*
	 * Implements an IOC model for pre/post callbacks to persist, merge, and
	 * remove operations. The onPrePersist, onPostPersist, onPreMerge,
	 * onPostMerge, onPreRemove and OnPostRemove operations must be implemented
	 * by the callback class.
	 */
	private String _sessionCallbackClassName = null;

	// A reference to the callback object that has been instantiated
	private SessionCallback _callback = null;

	protected static Logger _log = null;

	// The configuration file to use
	private String _configFile = System.getenv("VENICE_HOME")
			+ "/conf/module-config.xml";

	//The binding array used when binding variables into a JPQL query
	private Object[] bindingArray = null;

	@PersistenceContext(unitName = "GDN-Venice-Persistence", type = PersistenceContextType.TRANSACTION)
	protected EntityManager em;

	/**
	 * Default constructor.
	 */
	public VenOrderPaymentInstallmentHistorySessionEJBBean() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory
				.getLog4JLogger("com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBBean");
		// If the configuration is successful then instantiate the callback
		if (this.configure())
			this.instantiateTriggerCallback();
	}

	/**
	 * Reads the venice configuration file and configures the EJB's
	 * triggerCallbackClassName
	 */
	private Boolean configure() {
		_log.debug("Venice Configuration File:" + _configFile);
		try {
			XMLConfiguration config = new XMLConfiguration(_configFile);

			/*
			 * Get the index entry for the adapter configuration from the
			 * configuration file - there will be multiple adapter
			 * configurations
			 */
			@SuppressWarnings({ "rawtypes" })
			List callbacks = config
					.getList("sessionBeanConfig.callback.[@name]");
			Integer beanConfigIndex = new Integer(Integer.MAX_VALUE);
			@SuppressWarnings("rawtypes")
			Iterator i = callbacks.iterator();
			while (i.hasNext()) {
				String beanName = (String) i.next();
				if (this.getClass().getSimpleName().equals(beanName)) {
					beanConfigIndex = callbacks.indexOf(beanName);
					_log.debug("Bean configuration for " + beanName
							+ " found at " + beanConfigIndex);
				}
			}
			this._sessionCallbackClassName = config
					.getString("sessionBeanConfig.callback(" + beanConfigIndex + ").[@class]");

			_log.debug("Loaded configuration for _sessionCallbackClassName:"
					+ _sessionCallbackClassName);
		} catch (ConfigurationException e) {
			_log.error("A ConfigurationException occured when processing the configuration file"
					+ e.getMessage());
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * Instantiates the trigger callback handler class
	 * 
	 * @return
	 */
	Boolean instantiateTriggerCallback() {
		if (_sessionCallbackClassName != null
				&& !_sessionCallbackClassName.isEmpty())
			try {
				Class<?> c = Class.forName(_sessionCallbackClassName);
				_callback = (SessionCallback) c.newInstance();
			} catch (ClassNotFoundException e) {
				_log.error("A ClassNotFoundException occured when trying to instantiate:"
						+ this._sessionCallbackClassName);
				e.printStackTrace();
				return Boolean.FALSE;
			} catch (InstantiationException e) {
				_log.error("A InstantiationException occured when trying to instantiate:"
						+ this._sessionCallbackClassName);
				e.printStackTrace();
				return Boolean.FALSE;
			} catch (IllegalAccessException e) {
				_log.error("A IllegalAccessException occured when trying to instantiate:"
						+ this._sessionCallbackClassName);
				e.printStackTrace();
				return Boolean.FALSE;
			}
		return Boolean.TRUE;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<VenOrderPaymentInstallmentHistory> queryByRange(String jpqlStmt, int firstResult,
			int maxResults) {
		Long startTime = System.currentTimeMillis();
		_log.debug("queryByRange()");

		Query query = null;
		try {
			query = em.createQuery(jpqlStmt);
			if(this.bindingArray != null){
				for(int i = 0; i < bindingArray.length; ++i){
				    if(bindingArray[i] != null){
						query.setParameter(i+1, bindingArray[i]);
					}
				}
			}
		} catch (Exception e) {
			_log.error("An exception occured when calling em.createQuery():"
					+ e.getMessage());
			throw new EJBException(e);
		}
		try {
			if (firstResult > 0) {
				query = query.setFirstResult(firstResult);
			}
			if (maxResults > 0) {
				query = query.setMaxResults(maxResults);
			}
		} catch (Exception e) {
			_log.error("An exception occured when accessing the result set of a query:"
					+ e.getMessage());
			throw new EJBException(e);
		}		
		List<VenOrderPaymentInstallmentHistory> returnList = (List<VenOrderPaymentInstallmentHistory>)query.getResultList();
		this.bindingArray = null;
		
		Long endTime = System.currentTimeMillis();
		Long duration = startTime - endTime;
		_log.debug("queryByRange() duration:" + duration + "ms");
		return returnList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#persistVenOrderPaymentInstallmentHistory(com
	 * .gdn.venice.persistence.VenOrderPaymentInstallmentHistory)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public VenOrderPaymentInstallmentHistory persistVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory) {
		Long startTime = System.currentTimeMillis();
		_log.debug("persistVenOrderPaymentInstallmentHistory()");

		// Call the onPrePersist() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPrePersist(venOrderPaymentInstallmentHistory)) {
				_log.error("An onPrePersist callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPrePersist callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}
		
		VenOrderPaymentInstallmentHistory existingVenOrderPaymentInstallmentHistory = null;

		if (venOrderPaymentInstallmentHistory != null && venOrderPaymentInstallmentHistory.getId() != null) {
			_log.debug("persistVenOrderPaymentInstallmentHistory:em.find()");
			try {
				existingVenOrderPaymentInstallmentHistory = em.find(VenOrderPaymentInstallmentHistory.class,
						venOrderPaymentInstallmentHistory.getId());
			} catch (Exception e) {
				_log.error("An exception occured when calling em.find():"
						+ e.getMessage());
				throw new EJBException(e);
			}
		}
		
		if (existingVenOrderPaymentInstallmentHistory == null) {
			_log.debug("persistVenOrderPaymentInstallmentHistory:em.persist()");
			try {
				em.persist(venOrderPaymentInstallmentHistory);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.persist():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			_log.debug("persistVenOrderPaymentInstallmentHistory:em.flush()");
			try {
				em.flush();
				em.clear();
			} catch (Exception e) {
				_log.error("An exception occured when calling em.flush():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			
			// Call the onPostPersist() callback and throw an exception if it fails
			if (this._callback != null) {
				if (!this._callback.onPostPersist(venOrderPaymentInstallmentHistory)) {
					_log.error("An onPostPersist callback operation failed for:"
							+ this._sessionCallbackClassName);
					throw new EJBException(
							"An onPostPersist callback operation failed for:"
									+ this._sessionCallbackClassName);
				}
			}			
			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("persistVenOrderPaymentInstallmentHistory() duration:" + duration + "ms");
			
			return venOrderPaymentInstallmentHistory;
		} else {
			throw new EJBException("VenOrderPaymentInstallmentHistory exists!. VenOrderPaymentInstallmentHistory = "
					+ venOrderPaymentInstallmentHistory.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#persistVenOrderPaymentInstallmentHistoryList
	 * (java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ArrayList<VenOrderPaymentInstallmentHistory> persistVenOrderPaymentInstallmentHistoryList(
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList) {
		_log.debug("persistVenOrderPaymentInstallmentHistoryList()");
		Iterator i = venOrderPaymentInstallmentHistoryList.iterator();
		while (i.hasNext()) {
			this.persistVenOrderPaymentInstallmentHistory((VenOrderPaymentInstallmentHistory) i.next());
		}
		return (ArrayList<VenOrderPaymentInstallmentHistory>)venOrderPaymentInstallmentHistoryList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#mergeVenOrderPaymentInstallmentHistory(com.
	 * gdn.venice.persistence.VenOrderPaymentInstallmentHistory)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public VenOrderPaymentInstallmentHistory mergeVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory) {
		Long startTime = System.currentTimeMillis();
		_log.debug("mergeVenOrderPaymentInstallmentHistory()");

		// Call the onPreMerge() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPreMerge(venOrderPaymentInstallmentHistory)) {
				_log.error("An onPreMerge callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPreMerge callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}
		
		VenOrderPaymentInstallmentHistory existing = null;
		if (venOrderPaymentInstallmentHistory.getId() != null){
			_log.debug("mergeVenOrderPaymentInstallmentHistory:em.find()");
			existing = em.find(VenOrderPaymentInstallmentHistory.class, venOrderPaymentInstallmentHistory.getId());
		}
		
		if (existing == null) {
			return this.persistVenOrderPaymentInstallmentHistory(venOrderPaymentInstallmentHistory);
		} else {
			_log.debug("mergeVenOrderPaymentInstallmentHistory:em.merge()");
			try {
				em.merge(venOrderPaymentInstallmentHistory);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.merge():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			_log.debug("mergeVenOrderPaymentInstallmentHistory:em.flush()");
			try {
				em.flush();
				em.clear();
			} catch (Exception e) {
				_log.error("An exception occured when calling em.flush():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			VenOrderPaymentInstallmentHistory newobject = em.find(VenOrderPaymentInstallmentHistory.class,
					venOrderPaymentInstallmentHistory.getId());
			_log.debug("mergeVenOrderPaymentInstallmentHistory():em.refresh");
			try {
				em.refresh(newobject);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.refresh():"
						+ e.getMessage());
				throw new EJBException(e);
			}

			// Call the onPostMerge() callback and throw an exception if it fails
			if (this._callback != null) {
				if (!this._callback.onPostMerge(newobject)) {
					_log.error("An onPostMerge callback operation failed for:"
							+ this._sessionCallbackClassName);
					throw new EJBException(
							"An onPostMerge callback operation failed for:"
									+ this._sessionCallbackClassName);
				}
			}	
			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("mergeVenOrderPaymentInstallmentHistory() duration:" + duration + "ms");
						
			return newobject;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#mergeVenOrderPaymentInstallmentHistoryList(
	 * java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ArrayList<VenOrderPaymentInstallmentHistory> mergeVenOrderPaymentInstallmentHistoryList(
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList) {
		_log.debug("mergeVenOrderPaymentInstallmentHistoryList()");
		Iterator i = venOrderPaymentInstallmentHistoryList.iterator();
		while (i.hasNext()) {
			this.mergeVenOrderPaymentInstallmentHistory((VenOrderPaymentInstallmentHistory) i.next());
		}
		return (ArrayList<VenOrderPaymentInstallmentHistory>)venOrderPaymentInstallmentHistoryList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#removeVenOrderPaymentInstallmentHistory(com.
	 * gdn.venice.persistence.VenOrderPaymentInstallmentHistory)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory) {
		Long startTime = System.currentTimeMillis();
		_log.debug("removeVenOrderPaymentInstallmentHistory()");

		// Call the onPreRemove() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPreRemove(venOrderPaymentInstallmentHistory)) {
				_log.error("An onPreRemove callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPreRemove callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}
	
		_log.debug("removeVenOrderPaymentInstallmentHistory:em.find()");
		venOrderPaymentInstallmentHistory = em.find(VenOrderPaymentInstallmentHistory.class, venOrderPaymentInstallmentHistory.getId());
		
		try {
			_log.debug("removeVenOrderPaymentInstallmentHistory:em.remove()");
			em.remove(venOrderPaymentInstallmentHistory);
		} catch (Exception e) {
			_log.error("An exception occured when calling em.remove():"
					+ e.getMessage());
			throw new EJBException(e);
		}
		
		// Call the onPostRemove() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPostRemove(venOrderPaymentInstallmentHistory)) {
				_log.error("An onPostRemove callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPostRemove callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}			

		_log.debug("removeVenOrderPaymentInstallmentHistory:em.flush()");
		em.flush();
		em.clear();
		Long endTime = System.currentTimeMillis();
		Long duration = startTime - endTime;
		_log.debug("removeVenOrderPaymentInstallmentHistory() duration:" + duration + "ms");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#removeVenOrderPaymentInstallmentHistoryList(
	 * java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeVenOrderPaymentInstallmentHistoryList(List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList) {
		_log.debug("removeVenOrderPaymentInstallmentHistoryList()");
		Iterator i = venOrderPaymentInstallmentHistoryList.iterator();
		while (i.hasNext()) {
			this.removeVenOrderPaymentInstallmentHistory((VenOrderPaymentInstallmentHistory) i.next());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#findByVenOrderPaymentInstallmentHistoryLike(
	 * com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory, int, int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<VenOrderPaymentInstallmentHistory> findByVenOrderPaymentInstallmentHistoryLike(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults) {
		Long startTime = System.currentTimeMillis();
		_log.debug("findByVenOrderPaymentInstallmentHistoryLike()");
		JPQLQueryStringBuilder qb = new JPQLQueryStringBuilder(venOrderPaymentInstallmentHistory);
		HashMap complexTypeBindings = new HashMap();
		String stmt = qb.buildQueryString(complexTypeBindings, criteria);
		if(criteria != null){
			/*
			 * Get the binding array from the query builder and make
			 * it available to the queryByRange method
			 */
			this.bindingArray = qb.getBindingArray();
			for(int i = 0; i < qb.getBindingArray().length; i++){
				_log.debug("Bindings:" + i + ":" + qb.getBindingArray()[i]);
			}
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList = this.queryByRange(stmt, firstResult, maxResults);			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("findByVenOrderPaymentInstallmentHistoryLike() duration:" + duration + "ms");
			return venOrderPaymentInstallmentHistoryList;			
		}else{
			String errMsg = "A query has been initiated with null criteria.";
			_log.error(errMsg);
			throw new EJBException(errMsg);
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#findByVenOrderPaymentInstallmentHistoryLikeFR(
	 * com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory, int, int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FinderReturn findByVenOrderPaymentInstallmentHistoryLikeFR(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults) {
		Long startTime = System.currentTimeMillis();
		_log.debug("findByVenOrderPaymentInstallmentHistoryLikeFR()");
		JPQLQueryStringBuilder qb = new JPQLQueryStringBuilder(venOrderPaymentInstallmentHistory);
		HashMap complexTypeBindings = new HashMap();
		String stmt = qb.buildQueryString(complexTypeBindings, criteria);
		if(criteria != null){
			/*
			 * Get the binding array from the query builder and make
			 * it available to the queryByRange method
			 */
			this.bindingArray = qb.getBindingArray();
			for(int i = 0; i < qb.getBindingArray().length; i++){
				_log.debug("Bindings:" + i + ":" + qb.getBindingArray()[i]);
			}
			
			//Set the finder return object with the count of the total query rows
			FinderReturn fr = new FinderReturn();
			String countStmt = "select count(o) " + stmt.substring(stmt.indexOf("from"));
			Query query = null;
			try {
				query = em.createQuery(countStmt);
				if(this.bindingArray != null){
					for(int i = 0; i < bindingArray.length; ++i){
					    if(bindingArray[i] != null){
							query.setParameter(i+1, bindingArray[i]);
						}
					}
				}
				Long totalRows = (Long)query.getSingleResult();
				fr.setNumQueryRows(totalRows);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.createQuery():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			
			//Set the finder return object with the query list
			fr.setResultList(this.queryByRange(stmt, firstResult, maxResults));			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("findByVenOrderPaymentInstallmentHistoryLike() duration:" + duration + "ms");
			return fr;			
		}else{
			String errMsg = "A query has been initiated with null criteria.";
			_log.error(errMsg);
			throw new EJBException(errMsg);
		}		
	}

}
