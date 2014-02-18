package com.gdn.venice.server.app.finance.presenter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.VenOrderItemSessionEJBRemote;
import com.gdn.venice.facade.VenSettlementRecordSessionEJBRemote;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenSettlementRecord;

/**
 * Servlet implementation class TransactionFeeAndSettlementUpdaterJavaServlet
 */
public class TransactionFeeAndSettlementUpdaterJavaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    protected static Logger _log = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public TransactionFeeAndSettlementUpdaterJavaServlet() {
        super();
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.finance.presenter.TransactionFeeAndSettlementUpdaterJavaServlet");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String wcsOrderItemIds = request.getParameter("orderItemId");
        String commissionValue = request.getParameter("commissionValue");
        String transactionFee = request.getParameter("transxFee");
        String commissionType = request.getParameter("commissionType");
        String pph23 = request.getParameter("pph23");
        _log.info("Update TransactionFee and settlement : "+wcsOrderItemIds);

        if (wcsOrderItemIds != null) {
            response.getOutputStream().println("true");
            Locator<Object> locator = null;

            try {
                locator = new Locator<Object>();
                VenSettlementRecordSessionEJBRemote settlementRecordHome = (VenSettlementRecordSessionEJBRemote) locator
                        .lookup(VenSettlementRecordSessionEJBRemote.class, "VenSettlementRecordSessionEJBBean");
                VenOrderItemSessionEJBRemote venOrderItemHome = (VenOrderItemSessionEJBRemote) locator
                        .lookup(VenOrderItemSessionEJBRemote.class, "VenOrderItemSessionEJBBean");

                //set transaction fee
                String sql = "select o from VenOrderItem o where o.wcsOrderItemId ='" + wcsOrderItemIds + "'";
                List<VenOrderItem> venOrderItemList = venOrderItemHome.queryByRange(sql, 0, 0);
                VenOrderItem item;
                if (!venOrderItemList.isEmpty()) {
                    item = venOrderItemList.get(0);
                    if (item.getTransactionFeeAmount() == null) {
                        _log.debug("trans fee null, set with "+transactionFee);
                        venOrderItemHome.getSingleResultUsingNativeQuery("update ven_order_item set transaction_fee_amount = " + transactionFee 
                                + " where order_item_id = " + item.getOrderItemId());
                    }

                    _log.debug("merge the settlement record");
                    List<VenSettlementRecord> venSettlementRecordList = settlementRecordHome.queryByRange("select o from VenSettlementRecord o where o.venOrderItem.wcsOrderItemId='" + wcsOrderItemIds + "'", 0, 0);
                    VenSettlementRecord venSettlementRecord;

                    if (!venSettlementRecordList.isEmpty()) {
                        venSettlementRecord = venSettlementRecordList.get(0);

                    } else {
                        venSettlementRecord = new VenSettlementRecord();
                        venSettlementRecord.setVenOrderItem(item);
                    }

                    if (venSettlementRecord.getCommissionValue() == null) {
                        _log.debug("commission Value null, set with "+commissionValue);
                        venSettlementRecord.setCommissionValue(commissionValue != null ? new BigDecimal(commissionValue) : null);
                    }
                    
                    if (venSettlementRecord.getCommissionType() == null) {
                        _log.debug("commission Type null, set with "+commissionType);
                        venSettlementRecord.setCommissionType(commissionType);
                    }
                    
                    if (venSettlementRecord.getPph23() == null) {
                        _log.debug("pph23 null, set with "+pph23);
                        venSettlementRecord.setPph23(pph23 != null ? Boolean.getBoolean(pph23) : null);
                    }
                    
                    settlementRecordHome.mergeVenSettlementRecord(venSettlementRecord);
                }
            } catch (Exception e) {
                String errMessage = "Problem Update TransactionFee and settlement";
                _log.error(errMessage, e);

                throw new ServletException(errMessage, e);

            } finally {
                if (locator != null) {
                    try {
                        locator.close();
                    } catch (Exception e) {
                        _log.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            response.getOutputStream().println("false");
        }
    }
}
