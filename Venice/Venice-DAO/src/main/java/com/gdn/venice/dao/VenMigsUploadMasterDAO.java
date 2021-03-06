package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.util.VeniceConstants;

public interface VenMigsUploadMasterDAO extends JpaRepository<VenMigsUploadMaster, Long> {
	public static final String VEN_MIGS_MASTER_UPLOAD_RESPONSE_CODE_APPROVE = "0 - Approved";
	
	public static final String FIND_BY_MERCHANTREFERENCE_CARDNUMBERNOTINORDERPAYMENT_RESPONSECODENOTAPPROVED = 
		"select o " +
		"from  VenMigsUploadMaster o " +
		"where o.merchantTransactionReference like ?1 "+
		"and o.cardNumber " +
		"not in (select b.venOrderPayment.maskedCreditCardNumber " +
		"        from VenOrderPaymentAllocation b " +
		"        where b.venOrder.orderId= ?2 ) " +
		"and o.responseCode not like '%Approved'";
	
	public static final String FIND_BY_MERCHANTREFERENCE_RESPONSECODEAPPROVED = 
		"select o from VenMigsUploadMaster o " +
		"where (o.merchantTransactionReference like ?1 or o.merchantTransactionReference = ?1) " +
		"and o.responseCode <> '"+VEN_MIGS_MASTER_UPLOAD_RESPONSE_CODE_APPROVE+"'";
	
	public static final String FIND_BY_TRANSACTIONIDAUTHCODEACTIONNOTREMOVED = 
		"SELECT o " +
		"FROM VenMigsUploadMaster o " +
		"WHERE o.transactionId = ?1 " +
		"	AND o.authorisationCode = ?2 " +
		"	AND o.transactionId is not null " +
		"	AND o.action <> 'REMOVE'";
	
	public static final String FIND_BY_TRANSACTIONIDAUTHCODE = 
		"SELECT o " +
		"FROM VenMigsUploadMaster o " +
		"WHERE o.transactionId = ?1 " +
		"	AND o.authorisationCode = ?2 " +
		"	AND o.transactionId is not null ";
	
	
	@Query(FIND_BY_MERCHANTREFERENCE_CARDNUMBERNOTINORDERPAYMENT_RESPONSECODENOTAPPROVED)
	public List<VenMigsUploadMaster> findByMerchantReferenceCardNumberNotInOrderPaymentResposeCodeNotApproved(String wcsOrderId, Long orderId);
	
	@Query(FIND_BY_MERCHANTREFERENCE_RESPONSECODEAPPROVED)
	public List<VenMigsUploadMaster> findByMerchantReferenceResposeCodeApproved(String wcsOrderId);
	
	@Query(FIND_BY_TRANSACTIONIDAUTHCODEACTIONNOTREMOVED)
	public List<VenMigsUploadMaster> findByTransactionIdAuthCodeAndActionNotRemoved(String transactionId, String authCode);
	
	@Query(FIND_BY_TRANSACTIONIDAUTHCODE)
	public List<VenMigsUploadMaster> findByTransactionIdAuthCode(String transactionId, String authCode);
}
