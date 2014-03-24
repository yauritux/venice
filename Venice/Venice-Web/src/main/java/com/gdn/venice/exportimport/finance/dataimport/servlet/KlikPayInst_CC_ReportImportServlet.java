package com.gdn.venice.exportimport.finance.dataimport.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.facade.FinArFundsInReconRecordSessionEJBRemote;
import com.gdn.venice.facade.FinArFundsInReportSessionEJBRemote;
import com.gdn.venice.finance.dataexportimport.BCA_CC_Record;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInReport;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.util.VeniceConstants;

/**
 * Servlet class for importing BCA credit card transaction reports (MIGS).
 * 
 * <p>
 * <b>author:</b> <a href="mailto:david@pwsindonesia.com">David Forden</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 * 
 */
public class KlikPayInst_CC_ReportImportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

	/*
	 * The row number in the import file if an error occurs
	 */
	private Integer errorRowNumber = 1;
	
	protected static Logger _log = null;

	private String notificationText = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KlikPayInst_CC_ReportImportServlet() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.finance.dataimport.servlet.KlikPayInst_CC_ReportImportServlet");
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		_log.debug("KlikPayInst_CC_ReportImportServlet:hello");

		notificationText = FinanceImportServletConstants.JAVASCRIPT_ALERT_NOTIFICATION_TEXT_DEFAULT;

		if (isMultipart) { // import
			String filePath = System.getenv("VENICE_HOME") + "/files/import/finance/";

			// Upload the file
			String fileNameAndPath = FinanceImportServletHelper.upload(request, filePath, "-KlikPayInst_CC_Report.xls");
			if (fileNameAndPath == null) {
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
				
				String resultMessage = fundsInReportHome.processFundIn(fileNameAndPath, FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC, username);
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
