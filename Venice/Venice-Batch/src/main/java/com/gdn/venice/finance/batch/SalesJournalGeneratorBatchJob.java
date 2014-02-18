/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.finance.batch;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FinSalesRecordSessionEJBRemote;
import com.gdn.venice.facade.VenOrderItemSessionEJBRemote;
import com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author root
 */
public class SalesJournalGeneratorBatchJob {

    protected static Logger _log = null;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.finance.batch.SalesJournalGeneratorBatchJob");
        _log.info("Prepare to create sales journal");

        Locator<Object> locator = null;
        FinSalesRecordSessionEJBRemote finSalesRecordHome;
        VenOrderItemSessionEJBRemote itemHome;
        FinanceJournalPosterSessionEJBRemote financeJournalPosterHome;

        try {
            Long startTime = System.currentTimeMillis();
            int count = 0, errorCount = 0;
            locator = new Locator<Object>();
            finSalesRecordHome = (FinSalesRecordSessionEJBRemote) locator
                    .lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");
            itemHome = (VenOrderItemSessionEJBRemote) locator
                    .lookup(VenOrderItemSessionEJBRemote.class, "VenOrderItemSessionEJBBean");
            financeJournalPosterHome = (FinanceJournalPosterSessionEJBRemote) locator
                    .lookup(FinanceJournalPosterSessionEJBRemote.class, "FinanceJournalPosterSessionEJBBean");
            
            String query = "select o from FinSalesRecord o where o.venOrderItem.salesBatchStatus in ('Ready Journal', 'Journal Failed')";
            _log.info("Query: " + query);
            List<FinSalesRecord> salesRecords = finSalesRecordHome.queryByRange(query, 0, 100);

            _log.info("Query returns: " + salesRecords.size() + " row(s)");

            VenOrderItem item = null;
            List<FinSalesRecord> failed = new ArrayList<FinSalesRecord>();
            for (FinSalesRecord salesRecord : salesRecords) {
                try {
                    item = salesRecord.getVenOrderItem();
                    item.setSalesBatchStatus("Journal In Process");
                    itemHome.mergeVenOrderItem(item);
                } catch (Exception e) {
                    _log.warn("Update journal in process status failed for item: " + item.getWcsOrderItemId());
                    failed.add(salesRecord);
                }
            }

            salesRecords.removeAll(failed);
            for (FinSalesRecord salesRecord : salesRecords) {
                try {
                    item = salesRecord.getVenOrderItem();
                    _log.debug("\n Start post sales journal");
                    if (financeJournalPosterHome.postSalesJournalTransaction(salesRecord.getSalesRecordId(), VeniceConstants.VEN_GDN_PPN_RATE > 0)) {
                        _log.debug("\n Done post sales journal");
                        item.setSalesBatchStatus("Journal Done");
                        count++;
                    } else {
                        errorCount++;
                        _log.debug("\n Post sales journal failed");
                        item.setSalesBatchStatus("Journal Failed");
                    }
                } catch (Exception e) {
                    errorCount++;
                    _log.debug("Journal creation failed for order item: " + item.getWcsOrderItemId());
                    item.setSalesBatchStatus("Journal Failed");
                } finally{
                    itemHome.mergeVenOrderItem(item);                    
                }
            }
            Long endTime = System.currentTimeMillis();
            _log.info(count + " sales journal(s) generated, with duration:" + (endTime - startTime) + "ms");
            _log.info("Sales journal not created for: " + errorCount + " item(s)");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (locator != null) {
                    locator.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
