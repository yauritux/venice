package com.gdn.venice.exportimport.finance.dataimport.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FinArFundsInReconRecordSessionEJBRemote;
import com.gdn.venice.facade.FinArFundsInReportSessionEJBRemote;
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.exportimport.finance.dataimport.BCA_IB_FileReader;
import com.gdn.venice.finance.dataexportimport.BCA_VA_IB_Record;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.util.VeniceConstants;

/**
 * Servlet class for importing BCA internet banking transaction reports.
 * 
 * <p>
 * <b>author:</b> <a href="mailto:david@pwsindonesia.com">David Forden</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 * 
 */
public class KlikPay_IB_ReportImportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KlikPay_IB_ReportImportServlet() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.finance.dataimport.servlet.KlikPay_IB_ReportImportServlet");
    }

	protected static Logger _log = null;
	
	private String notificationText = "";

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		_log.debug("KlikPay_IB_ReportImportServlet:hello");
		
		notificationText = FinanceImportServletConstants.JAVASCRIPT_ALERT_NOTIFICATION_TEXT_DEFAULT;

		if (isMultipart) { // import
			String filePath = System.getenv("VENICE_HOME")	+ "/files/import/finance/";
			
			//Upload the file
			String fileNameAndPath = FinanceImportServletHelper.upload(request, filePath, "-KlikPay_IB_Report.txt");
			
			if(fileNameAndPath == null){
				String errMsg = FinanceImportServletConstants.EXCEPTION_TEXT_UPLOAD_EXCEPTION;
				_log.info(errMsg);
				notificationText = notificationText.replaceFirst("REPLACE", errMsg);
				response.getOutputStream().println(notificationText);
				return;
			}

			String username = null;

            if (request.getParameter("username") == null) {
                username = "Roland";
            } else {
                username = request.getParameter("username");
            }
			
			FinArFundsInReportSessionEJBRemote fundsInReportHome = null;
			Locator<Object> locator = null;
			
			try{
				locator = new Locator<Object>();
				
				fundsInReportHome = (FinArFundsInReportSessionEJBRemote) locator
					.lookup(FinArFundsInReportSessionEJBRemote.class, "FinArFundsInReportSessionEJBBean");
				
				String resultMessage = fundsInReportHome.processFundIn(fileNameAndPath, FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB, username);
				notificationText = notificationText.replaceFirst("REPLACE", resultMessage);
				response.getOutputStream().println(notificationText);
			}catch (Exception e) {
				_log.error(e);
				String errMsg = FinanceImportServletConstants.EXCEPTION_TEXT_UPLOAD_EXCEPTION;
				notificationText = notificationText.replaceFirst("REPLACE", errMsg);
				response.getOutputStream().println(notificationText);
				return;
			}finally{
				try{
					if(locator!=null){
						locator.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}
