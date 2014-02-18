package com.gdn.venice.facade.processor;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.bpmenablement.BPMAdapter;
import com.gdn.venice.dao.FinArFundsInReconRecordDAO;
import com.gdn.venice.dao.FinSalesRecordDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;

@Service("finSalesRecordMergeProcessor")
public class FinSalesRecordMergeProcessor extends MergeProcessor {
	
	protected static Logger _log = Log4jLoggerFactory.getLogger("com.gdn.venice.facade.processor.FinSalesRecordMergeProcessor");
	
	private static final String WCSORDERID = "wcsOrderId";
	private static final String FUNDINRECONRECORDID = "fundInReconRecordId";
	private static final String FINANCECASHRECEIVEANDSALESJOURNALRECONCILIATION = "Finance Cash Receive and Sales Journal Reconciliation";
	
	@PersistenceContext
    EntityManager em;
	
	@Autowired
	FinSalesRecordDAO finSalesRecordDAO;
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	@Autowired
	FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	
	@Override
	public boolean preMerge(Object obj) {
		_log.debug("preMerge");
		
		FinSalesRecord newFinSalesRecord = (FinSalesRecord) obj;
		
		em.detach(newFinSalesRecord);
		try {
						
			_log.debug("query existing sales record");
			FinSalesRecord existingFinSalesRecord = finSalesRecordDAO.findOne(newFinSalesRecord.getSalesRecordId());
			
			if(existingFinSalesRecord != null){
				
				_log.debug("existingSalesRecord id: "+existingFinSalesRecord.getSalesRecordId());
				_log.debug("existingSalesRecord status: "+existingFinSalesRecord.getFinApprovalStatus().getApprovalStatusId());
				_log.debug("newSalesRecord status: "+newFinSalesRecord.getFinApprovalStatus().getApprovalStatusId());
				/*
				 * If the status of the sales record is approved for the first time then create the journal entries
				 */
				if(newFinSalesRecord.getFinApprovalStatus().getApprovalStatusId() == VeniceConstants.FIN_APPROVAL_STATUS_APPROVED
						&& existingFinSalesRecord.getFinApprovalStatus().getApprovalStatusId() != VeniceConstants.FIN_APPROVAL_STATUS_APPROVED){
					_log.debug("approval status change to approved");
					
					/*
					 * Setelah sales journal di create.
					 * 
					 * after the last order item become cx, reconcile sales journal vs cash receive journal. 
					 * because it is the same as funds in reconcile, we just check the reconcilement status, if not all fund received, then trigger bpm.
					 */
					
					_log.info("\n Start reconcile sales journal vs cash receive journal");
					Boolean allOrderItemCx=false;
					List< VenOrderItem> venOrderItemList =  venOrderItemDAO.findWithVenOrderStatusByVenOrder(existingFinSalesRecord.getVenOrderItem().getVenOrder());
	
					_log.debug("\n Check order item list status");
					for (VenOrderItem item : venOrderItemList) {
						if (item.getVenOrderStatus().getOrderStatusId() == VeniceConstants.VEN_ORDER_STATUS_CX) {
							_log.debug("\n Order item status is CX");
							allOrderItemCx=true;
						}else{
							_log.debug("\n Order item status is not CX");
							allOrderItemCx=false;
							break;
						}
					}
						
					if(allOrderItemCx==true){
						_log.info("\n All order item status is already CX, Check is cash receive journal exist");
						@SuppressWarnings("unused")
						Boolean cashReceiveJournalExist=false;
						List<FinArFundsInReconRecord> fundInRecordList =  finArFundsInReconRecordDAO.findByWcsOrderId(existingFinSalesRecord.getVenOrderItem().getVenOrder().getWcsOrderId());
	
						//there might be an order which has multiple payment, therefore there is multiple recon record
						//here we check all recon record id must have cash receive journal.
						for (FinArFundsInReconRecord reconRecord : fundInRecordList) {
							//here we check the reconciliation status, if it's not all funds received, trigger bpm.
							long reconStatus = reconRecord.getFinArReconResult().getReconResultId();
							if(reconStatus!=VeniceConstants.FIN_AR_RECON_RESULT_ALL){
								_log.debug("\n Recon record status not all funds received");
								
								Properties properties = new Properties();
								properties.load(new FileInputStream(BPMAdapter.WEBAPI_PROPERTIES_FILE));
								String userName = properties.getProperty("javax.xml.rpc.security.auth.username");		
								String password = BPMAdapter.getUserPasswordFromLDAP(userName);
								BPMAdapter bpmAdapter = BPMAdapter.getBPMAdapter(userName, password);
								bpmAdapter.synchronize();
								
								HashMap<String, String> taskData = new HashMap<String, String>();
								taskData.put(FUNDINRECONRECORDID, reconRecord.getReconciliationRecordId().toString());
								taskData.put(WCSORDERID, reconRecord.getWcsOrderId());							
	
								try {
									_log.debug("\n Starting bpm");
									bpmAdapter.startBusinessProcess(FINANCECASHRECEIVEANDSALESJOURNALRECONCILIATION, taskData);
									_log.debug("\n Done starting bpm");
								} catch (Exception e) {
									_log.debug("\n Error when starting bpm");
									e.printStackTrace();
								}
							}else{
								_log.info("\n Recon record status all funds received, no need to trigger bpm");
							}
						}			
					}
				}
			}
		} catch (Exception e) {
			String errMsg = "An exception occured when processing the callback for FinSalesRecordSessionEJBCallback:" + e.getMessage();
			_log.error(errMsg,e);
			e.printStackTrace();
			return false;
		} 
		
		return true;
	}

	@Override
	public boolean merge(Object obj) {
		FinSalesRecord newFinSalesRecord = (FinSalesRecord) obj;
		finSalesRecordDAO.save(newFinSalesRecord);
		return true;
	}

	@Override
	public boolean postMerge(Object obj) {
		return true;
	}

}
