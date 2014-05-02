package com.gdn.venice.facade.spring.fraud.migs;

import java.util.ArrayList;

import com.gdn.venice.exception.MIGSFileParserException;
import com.gdn.venice.fraud.dataimport.MigsReport;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenMigsUploadTemporary;

public interface MigsUploadProcessorService {
	
	public ArrayList<PojoInterface> parse(String fileNameAndFullPath) throws MIGSFileParserException;

	public String formatAuthCode(String authCode);

	public boolean isMigsAlreadyUploaded(String transactionId, String authCode);

	public VenMigsUploadTemporary mapMigsReportToVenMigsUploadTemp(MigsReport migsReport, String uploadFileNameAndFullPath);
	
	public VenMigsUploadMaster mapMigsReportToVenMigsUploadMaster(MigsReport migs, String uploadFileNameAndFullPath);
	
	public boolean isMigsResponseCodeApproved(String responseCode);

	public boolean isMigsDataIncomplete(VenMigsUploadTemporary migsUploadTemporary);

	public boolean isMerchantTransactionRefEmpty(VenMigsUploadTemporary migsUploadTemporary);

	public boolean isAuthCodeEmpty(VenMigsUploadTemporary migsUploadTemporary);

	public boolean isCardNumberEmpty(VenMigsUploadTemporary migsUploadTemporary);

	public boolean isOrderRelatedToPaymentExist(String wcsOrderId, String referenceId);

	public abstract String process(String fileNameAndFullPath) throws MIGSFileParserException;
	
}
