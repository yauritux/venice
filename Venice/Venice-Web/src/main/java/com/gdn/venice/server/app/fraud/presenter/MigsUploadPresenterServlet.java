package com.gdn.venice.server.app.fraud.presenter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.exception.MIGSFileParserException;
import com.gdn.venice.facade.MigsUploadSessionEJBRemote;
import com.gdn.venice.server.app.fraud.presenter.commands.AddMigsDataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.FetchMasterMigsDataDataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.FetchMigsUploadTemporaryDataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.UpdateMigsUploadTemporaryDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;


public class MigsUploadPresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String notificationText = "";
    
	public MigsUploadPresenterServlet() {
        super();
    }

	@SuppressWarnings({ "unchecked" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String type = request.getParameter("type") == null ? "" : request.getParameter("type");
		String retVal =  "";
		if (type.equals(RafDsCommand.DataSource)) {
			String requestBody = Util.extractRequestBody(request);
	
			RafDsRequest rafDsRequest = null;
			try {
				rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String method = request.getParameter("method");
			if (method.equals("fetchMigsUploadData")) {
				RafDsCommand fetchMigsUploadTemporaryDataCommand = new FetchMigsUploadTemporaryDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchMigsUploadTemporaryDataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if (method.equals("fetchMasterMigsData")) {				
				RafDsCommand fetchMasterMigsDataDataCommand = new FetchMasterMigsDataDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchMasterMigsDataDataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (method.equals("updateMigsUploadData")) {
				RafDsCommand updateMigsUploadTemporaryDataCommand = new UpdateMigsUploadTemporaryDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = updateMigsUploadTemporaryDataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (type.equalsIgnoreCase("rpc")) {
			AddMigsDataCommand addMigsDataCommand = new AddMigsDataCommand(Util.getUserName(request));
			retVal = addMigsDataCommand.execute();
		} else {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			
			notificationText = "<html>\n" + "<head>"
			+ "<title>Processing Report Completed</title>" + "</head>\n"
			+ "<body onload=\"alert('REPLACE')\">" + "<p>TEST</p>\n"
			+ "</body>\n" + "</html>";
			
			if (isMultipart) { // import
				String formatString = "yyyy.MM.dd HH:mm:ss";
				SimpleDateFormat sdf = new SimpleDateFormat(formatString);
				
				String filePath = System.getenv("VENICE_HOME") + "/files/import/migs/";
				String fileName = "MIGS-" + sdf.format(new Date()) + ".xls";
				
				File uploadedFile = new File(filePath + fileName);
				ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
				List<FileItem> fileItemsList = null;

				try {
					fileItemsList = servletFileUpload.parseRequest(request);
				} catch (FileUploadException e) {
					String errMsg = "An exception occured when parsing the sevlet file upload:" + e.getMessage();
					e.printStackTrace();
					notificationText = notificationText.replaceFirst("REPLACE", errMsg);
				}
				
				Iterator<FileItem> iter = fileItemsList.iterator();
				while (iter.hasNext()) {
					FileItem fileItem = (FileItem) iter.next();

					if (!fileItem.isFormField()) {
						try {
							fileItem.write(uploadedFile);
						} catch (Exception e) {
							String errMsg = "An exception occured when writing to file:" + fileName + " :" + e.getMessage();
							e.printStackTrace();
							notificationText = notificationText.replaceFirst("REPLACE", errMsg);
						}
					}
				}
				
				Locator<Object> locator = null;
				try {

					locator = new Locator<Object>();
					
					MigsUploadSessionEJBRemote migsUpload = (MigsUploadSessionEJBRemote) locator.lookup(MigsUploadSessionEJBRemote.class, "MigsUploadSessionEJBBean");
					
					String successMsg = migsUpload.process(filePath + fileName);
					
					retVal = notificationText.replaceFirst("REPLACE",	successMsg);
				} catch (MIGSFileParserException e) {
					e.printStackTrace();
					notificationText = notificationText.replaceFirst("REPLACE",	e.getMessage());
				} catch (Exception e) {
					String errMsg = "An exception occured when uploading the MIGS report. Please contact the systems administrator.";
					e.printStackTrace();
					notificationText = notificationText.replaceFirst("REPLACE",	errMsg);
				} finally {
					try {
						if (locator != null) {
							locator.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		response.getOutputStream().println(retVal);
	}
	
}