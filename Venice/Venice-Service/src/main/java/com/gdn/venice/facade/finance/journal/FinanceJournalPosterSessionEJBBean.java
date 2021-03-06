package com.gdn.venice.facade.finance.journal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.djarum.raf.utilities.Locator;
import com.gdn.awb.exchange.model.LogisticProviderResource;
import com.gdn.awb.exchange.response.GetLogisticProviderListResponse;
import com.gdn.venice.constants.FinAccountConstants;
import com.gdn.venice.constants.FinApprovalStatusConstants;
import com.gdn.venice.constants.FinArFundsInActionAppliedConstants;
import com.gdn.venice.constants.FinArFundsInReportTimeConstants;
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.constants.FinArReconResultConstants;
import com.gdn.venice.constants.FinJournalConstants;
import com.gdn.venice.constants.FinRefundTypeConstants;
import com.gdn.venice.constants.FinTransactionStatusConstants;
import com.gdn.venice.constants.FinTransactionTypeConstants;
import com.gdn.venice.constants.VenLogisticsProviderConstants;
import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.constants.VenPartyTypeConstants;
import com.gdn.venice.constants.VenPromotionTypeConstants;
import com.gdn.venice.constants.VenSettlementRecordCommissionTypeConstants;
import com.gdn.venice.dao.FinApManualJournalTransactionDAO;
import com.gdn.venice.dao.FinApPaymentDAO;
import com.gdn.venice.dao.FinArFundsInAllocatePaymentDAO;
import com.gdn.venice.dao.FinArFundsInJournalTransactionDAO;
import com.gdn.venice.dao.FinArFundsInReconRecordDAO;
import com.gdn.venice.dao.FinArFundsInRefundDAO;
import com.gdn.venice.dao.FinJournalApprovalGroupDAO;
import com.gdn.venice.dao.FinJournalTransactionDAO;
import com.gdn.venice.dao.FinSalesRecordDAO;
import com.gdn.venice.dao.LogProviderAgreementDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.dao.VenOrderItemAdjustmentDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.dao.VenOrderPaymentDAO;
import com.gdn.venice.dao.VenPartyDAO;
import com.gdn.venice.dao.VenPromotionDAO;
import com.gdn.venice.dao.VenSettlementRecordDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.facade.VenSettlementRecordSessionEJBLocal;
import com.gdn.venice.facade.util.FinancePeriodUtil;
import com.gdn.venice.finance.services.FinAccountService;
import com.gdn.venice.logistics.integration.AirwayBillEngineClientConnector;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;
import com.gdn.venice.logistics.integration.bean.AirwayBillTransaction;
import com.gdn.venice.persistence.FinAccount;
import com.gdn.venice.persistence.FinApInvoice;
import com.gdn.venice.persistence.FinApManualJournalTransaction;
import com.gdn.venice.persistence.FinApPayment;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInAllocatePayment;
import com.gdn.venice.persistence.FinArFundsInJournalTransaction;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArFundsInRefund;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.persistence.FinJournal;
import com.gdn.venice.persistence.FinJournalApprovalGroup;
import com.gdn.venice.persistence.FinJournalTransaction;
import com.gdn.venice.persistence.FinPeriod;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.FinTransactionStatus;
import com.gdn.venice.persistence.FinTransactionType;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.LogProviderAgreement;
import com.gdn.venice.persistence.VenBank;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemAdjustment;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPromotion;
import com.gdn.venice.persistence.VenSettlementRecord;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.VeniceConstants;

/**
 * Session bean implementation class for finance journal posting
 * 
 * <p>
 * <b>author:</b> <a href="mailto:david@pwsindonesia.com">David Forden</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 * 
 */
@Interceptors(SpringBeanAutowiringInterceptor.class)
@Stateless(mappedName = "FinanceJournalPosterSessionEJBBean")
public class FinanceJournalPosterSessionEJBBean implements
		FinanceJournalPosterSessionEJBRemote,
		FinanceJournalPosterSessionEJBLocal {
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private FinAccountService finAccountService;
	
	@Autowired
	private FinApManualJournalTransactionDAO finApManualJournalTransactionDAO;
	
	@Autowired
	private FinApPaymentDAO finApPaymentDAO;

	@Autowired
	private FinArFundsInAllocatePaymentDAO finArFundsInAllocatePaymentDAO;
	
	@Autowired
	private FinArFundsInJournalTransactionDAO finArFundsInJournalTransactionDAO;
	
	@Autowired
	private FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	
	@Autowired
	private FinArFundsInRefundDAO finArFundsInRefundDAO;
	
	@Autowired
	private FinJournalApprovalGroupDAO finJournalApprovalGroupDAO;
	
	@Autowired
	private FinJournalTransactionDAO finJournalTransactionDAO;
	
	@Autowired
	private FinSalesRecordDAO finSalesRecordDAO;
	
	@Autowired
	private LogProviderAgreementDAO logProviderAgreementDAO;
	
	@Autowired
	private VenOrderDAO venOrderDAO;
	
	@Autowired
	private VenOrderItemDAO venOrderItemDAO;
	
	@Autowired
	private VenOrderItemAdjustmentDAO venOrderItemAdjustmentDAO;
	
	@Autowired
	private VenOrderPaymentDAO venOrderPaymentDAO;
	
	@Autowired
	private VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	@Autowired
	private VenPartyDAO venPartyDAO;
	
	@Autowired
	private VenPromotionDAO venPromotionDAO;
	
	@Autowired
	private VenSettlementRecordDAO venSettlementRecordDAO;
	
	private static BigDecimal GDNPPN_DIVISOR = new BigDecimal(
			VeniceConstants.VEN_GDN_PPN_RATE).divide(new BigDecimal(100), 2,
			RoundingMode.HALF_UP).add(new BigDecimal(1));

	/**
	 * Default constructor.
	 */
	public FinanceJournalPosterSessionEJBBean() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postCashReceiveJournalTransaction(ArrayList<Long>
	 * finArFundsInReconRecordIdList)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postCashReceiveJournalTransactions(
			ArrayList<Long> finArFundsInReconRecordIdList) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		try {
			if (finArFundsInReconRecordIdList.isEmpty()) {
				throw new EJBException(
						"Empty list passed to postCashReceiveJournalTransaction. The reconciliation record list must contain entries");
			}

			// Read all the relevant funds in records from the database
			List<FinArFundsInReconRecord> reconRecordList = new ArrayList<FinArFundsInReconRecord>();
			for (Long reconciliationRecordId : finArFundsInReconRecordIdList) {
				FinArFundsInReconRecord reconRecord = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(reconciliationRecordId);
				if (reconRecord != null) {
					reconRecordList.add(reconRecord);
				} else {
					throw new EJBException(
							"List passed to postCashReceiveJournalTransaction contains no valid keys. The reconciliation record list must contain valid keys for existing reconciliation records");
				}
			}

			FinJournalApprovalGroup finJournalApprovalGroup = null;
			FinJournal finJournalCashReceive = new FinJournal();
			// FinJournalApprovalGroup finJournalApprovalGroups = null;
			int count = 0;

			/*
			 * Process the list: o create the grouping record o create the
			 * funds-in journal transactions as credits o create bank account
			 * journal transactions as debits o create bank admin (fee) journal
			 * transactions as debits
			 */
			List<FinJournalTransaction> cashReceivedTransactionList = new ArrayList<FinJournalTransaction>();
			List<FinJournalTransaction> bankFeeTransactionList = new ArrayList<FinJournalTransaction>();
			List<FinJournalTransaction> bankTransactionList = new ArrayList<FinJournalTransaction>();
			boolean createJournal = true;
			Timestamp time = new Timestamp(System.currentTimeMillis());
			for (FinArFundsInReconRecord reconRecord : reconRecordList) {
				ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();
				String wcsOrderId = reconRecord.getWcsOrderId() != null ? reconRecord
						.getWcsOrderId() : reconRecord.getWcsOrderId();
				List<FinArFundsInAllocatePayment> itemsAllocate = null;
				List<FinArFundsInJournalTransaction> finArFundsInJournalTransactionList = null;
				createJournal = true;
				if (reconRecord
						.getFinArFundsInActionApplied()
						.getActionAppliedId()
						.equals(VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED)) {
					itemsAllocate = finArFundsInAllocatePaymentDAO.findByIdReconRecordSourceIsActive(reconRecord.getReconciliationRecordId());
				}

				finArFundsInJournalTransactionList = finArFundsInJournalTransactionDAO.findByReconcilicationRecordId(
						reconRecord.getReconciliationRecordId());

				if (!finArFundsInJournalTransactionList.isEmpty()
						&& itemsAllocate != null
						&& (reconRecord
								.getFinArFundsInActionApplied()
								.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
					// BLOCK FOR ALLOCATION JOURNAL
					postAllocationJournal(reconRecord,
							itemsAllocate.get(0),
							reconRecordList);
				} else { // CASH RECEIVE JOURNAL

					if (finJournalApprovalGroup == null
							&& finArFundsInJournalTransactionList.isEmpty()) {
						finJournalCashReceive
								.setJournalId(FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id());

						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MMM-dd");
						finJournalApprovalGroup = new FinJournalApprovalGroup();
						FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
						finApprovalStatus
								.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
						finJournalApprovalGroup
								.setFinApprovalStatus(finApprovalStatus);

						finJournalApprovalGroup
								.setFinJournal(finJournalCashReceive);

						finJournalApprovalGroup
								.setJournalGroupDesc("Cash Received Journal for :"
										+ sdf.format(new Date()));
						finJournalApprovalGroup
								.setJournalGroupTimestamp(new Timestamp(System
										.currentTimeMillis()));

						// Persist the journal group
						if (!em.contains(finJournalApprovalGroup)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalApprovalGroupDAO.save explicitly.");
							finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
						}

					}

					BigDecimal paidAmount = reconRecord
							.getProviderReportPaidAmount() != null ? reconRecord
							.getProviderReportPaidAmount() : new BigDecimal(0);
					BigDecimal feeAmount = reconRecord
							.getProviderReportFeeAmount() != null ? reconRecord
							.getProviderReportFeeAmount() : new BigDecimal(0);
					if (reconRecord
							.getFinArFundsInActionApplied()
							.getActionAppliedId()
							.equals(VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED)
							&& itemsAllocate != null
							&& !itemsAllocate.isEmpty()) {
						paidAmount = itemsAllocate.get(0).getPaidamount() != null ? itemsAllocate
								.get(0).getPaidamount() : new BigDecimal(0);
						feeAmount = itemsAllocate.get(0).getBankfee() != null ? itemsAllocate
								.get(0).getBankfee() : new BigDecimal(0);
					}
					paidAmount = paidAmount
							.subtract(feeAmount != null ? feeAmount
									: new BigDecimal(0));

					long accountNumberBank = 0;
					try {
						accountNumberBank = finAccountService.getAccountNumberBank(reconRecord.getFinArFundsInReport()
								.getFinArFundsInReportType().getPaymentReportTypeId());
					} catch (VeniceInternalException e) {
						throw new EJBException(e.getMessage());						
					}
					
					FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
					finTransactionStatus
							.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());

					FinTransactionType finTransactionType = new FinTransactionType();
					finTransactionType
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id());

					List<FinArFundsInAllocatePayment> cekIdRecordDest = finArFundsInAllocatePaymentDAO.findByIdReconRecordDest(
							reconRecord.getReconciliationRecordId());
					if (!cekIdRecordDest.isEmpty()
							&& (reconRecord
									.getFinArFundsInActionApplied()
									.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id())) {
						createJournal = false;
					}
					if (createJournal) {
						if (finArFundsInJournalTransactionList.isEmpty()) {
							/*
							 * Post the DEBIT for the bank account transactions
							 */
							FinJournalTransaction bankAccountJournalTransaction = new FinJournalTransaction();
							bankAccountJournalTransaction
									.setComments("System Debit Bank:"
											+ wcsOrderId);
							bankAccountJournalTransaction
									.setCreditDebitFlag(false);
							bankAccountJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);

							FinAccount finAccountBank = new FinAccount();
							finAccountBank.setAccountId(accountNumberBank);
							bankAccountJournalTransaction
									.setFinAccount(finAccountBank);
							List<FinArFundsInReconRecord> bankFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							bankFundsInRecordList.add(reconRecord);
							bankAccountJournalTransaction
									.setFinArFundsInReconRecords(bankFundsInRecordList);
							bankAccountJournalTransaction
									.setFinJournal(finJournalCashReceive);
							bankAccountJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							bankAccountJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							bankAccountJournalTransaction
									.setFinTransactionType(finTransactionType);

							bankAccountJournalTransaction
									.setTransactionAmount(paidAmount);
							bankAccountJournalTransaction
									.setTransactionTimestamp(time);

							bankAccountJournalTransaction
									.setWcsOrderID(wcsOrderId);
							bankAccountJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							bankAccountJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the bank account journal entry
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( BANK )");
							/*journalTransactionHome
									.persistFinJournalTransaction(bankAccountJournalTransaction);*/
							if (!em.contains(bankAccountJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								bankAccountJournalTransaction = finJournalTransactionDAO.save(bankAccountJournalTransaction);
							}
							bankTransactionList
									.add(bankAccountJournalTransaction);
							transactionList.add(bankAccountJournalTransaction);

							/*
							 * Post the journal transaction for bank fees
							 */
							FinJournalTransaction bankFeeJournalTransaction = new FinJournalTransaction();
							bankFeeJournalTransaction
									.setComments("System Debit Bank Fee:"
											+ wcsOrderId);
							bankFeeJournalTransaction.setCreditDebitFlag(false);
							bankFeeJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);
							// bankFeeJournalTransaction.setFinAccount(finAccountBank);

							// salah save. seharusnya save account number hpp
							// bank FIN_ACCOUNT_5110003
							FinAccount finAccountBankFee = new FinAccount();
							finAccountBankFee
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110003.id());
							bankFeeJournalTransaction
									.setFinAccount(finAccountBankFee);

							List<FinArFundsInReconRecord> bankFeeFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							bankFeeFundsInRecordList.add(reconRecord);
							bankFeeJournalTransaction
									.setFinArFundsInReconRecords(bankFeeFundsInRecordList);
							bankFeeJournalTransaction
									.setFinJournal(finJournalCashReceive);
							bankFeeJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							bankFeeJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							bankFeeJournalTransaction
									.setFinTransactionType(finTransactionType);
							bankFeeJournalTransaction
									.setTransactionAmount(feeAmount);
							bankFeeJournalTransaction
									.setTransactionTimestamp(time);

							bankFeeJournalTransaction.setWcsOrderID(wcsOrderId);
							bankFeeJournalTransaction.setVenBank(reconRecord
									.getVenOrderPayment() != null ? reconRecord
									.getVenOrderPayment().getVenBank()
									: reconRecord.getFinArFundsInReport()
											.getFinArFundsInReportType()
											.getVenBank());
							bankFeeJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( HPP BIAYA Bank)");
							/*journalTransactionHome
									.persistFinJournalTransaction(bankFeeJournalTransaction);*/
							if (!em.contains(bankFeeJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Calling finJournalTransactionDAO.save explicitly");
								finJournalTransactionDAO.save(bankFeeJournalTransaction);
							}
							bankFeeTransactionList
									.add(bankFeeJournalTransaction);
							transactionList.add(bankFeeJournalTransaction);
						}
						if ((reconRecord.getFinArFundsIdReportTime() != null ? reconRecord
								.getFinArFundsIdReportTime().getReportTimeId()
								: new Long(0))
								.equals(FinArFundsInReportTimeConstants.FIN_AR_FUNDS_IN_REPORT_TIME_REAL_TIME.id())) {
							/**
							 * Journal untuk report realtime Post the journal
							 * transaction for Ayat-Ayat Silang Bank
							 */
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Ayat-Ayat Silang Bank) ::BEGIN");
							FinJournalTransaction ayatSilangBankJournalTransaction = new FinJournalTransaction();
							ayatSilangBankJournalTransaction = setJournalTransaction(
									reconRecord,
									wcsOrderId,
									finJournalApprovalGroup,
									FinAccountConstants.FIN_ACCOUNT_1120999.id(),
									FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id(),
									FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
									FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_AYAT_AYAT_SILANG_PADA_BANK.id(),
									time,
									reconRecord.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank(),
									//journalTransactionHome, accountNumberBank,
													accountNumberBank,
									reconRecord.getProviderReportPaidAmount(),
									true,
									"System Credit Ayat-Ayat Silang Bank :"
											+ wcsOrderId);
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Ayat-Ayat Silang Bank)::END");
							bankFeeTransactionList
									.add(ayatSilangBankJournalTransaction);
							transactionList
									.add(ayatSilangBankJournalTransaction);

							/**
							 * update jornal group untuk VA Realtime Status
							 * diubah menjadi submitted agar tidak ikut ke
							 * rollup
							 */
							FinApprovalStatus finApprovalStatuss = new FinApprovalStatus();
							finApprovalStatuss
									.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_SUBMITTED.id());
							finJournalApprovalGroup
									.setFinApprovalStatus(finApprovalStatuss);
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy-MMM-dd");
							finJournalApprovalGroup
									.setJournalGroupDesc("VA Real Time - Cash Received Journal for :"
											+ sdf.format(new Date()));
							/*finJournalApprovalGroup = journalApprovalGroupHome
									.mergeFinJournalApprovalGroup(finJournalApprovalGroup);*/
							if (!em.contains(finJournalApprovalGroup)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalApprovalGroupDAO.save explicitly.");
								finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
							}

						} else if (reconRecord
								.getFinArFundsInActionApplied()
								.getActionAppliedId()
								.equals(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id())
								|| reconRecord
										.getFinArFundsInActionApplied()
										.getActionAppliedId()
										.equals(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_CUSTOMER.id())) {
							Long type = reconRecord
									.getFinArFundsInActionApplied()
									.getActionAppliedId()
									.equals(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id()) ? FinAccountConstants.FIN_ACCOUNT_2170002.id()
									: FinAccountConstants.FIN_ACCOUNT_2170001.id();
							if (!finArFundsInJournalTransactionList.isEmpty()) {
								/*
								List<FinArFundsInRefund> refundAmountList = refundRecordHome
										.queryByRange(
												"select o from FinArFundsInRefund o where o.finArFundsInReconRecord.reconciliationRecordId="
														+ reconRecord
																.getReconciliationRecordId(),
												0, 0);
								*/
								List<FinArFundsInRefund> refundAmountList = finArFundsInRefundDAO.findByReconciliationRecordId(reconRecord.getReconciliationRecordId());
								Double refundAmount = new Double(
										reconRecord.getRefundAmount() != null ? reconRecord
												.getRefundAmount() + ""
												: "0");
								if (!refundAmountList.isEmpty()) {
									for (FinArFundsInRefund i : refundAmountList) {
										refundAmount = refundAmount
												- new Double(i.getApAmount()
														+ "");
									}
								}
								postRefundJournalTransaction(
										reconRecord.getReconciliationRecordId(),
										refundAmount, new Double(0),
										new Integer(type + ""), true);
							} else {
								if (reconRecord.getProviderReportPaidAmount()
										.compareTo(
												reconRecord.getRefundAmount()) != 0) {
									/*
									 * Post the CREDIT for UANG jaminan
									 * transaksi
									 */
									CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Start Create Journal order : "
											+ wcsOrderId);
									FinJournalTransaction cashReceiveJournalTransaction = new FinJournalTransaction();
									cashReceiveJournalTransaction
											.setComments("System Credit Uang Jaminan Transaksi:"
													+ wcsOrderId);
									cashReceiveJournalTransaction
											.setCreditDebitFlag(true);
									cashReceiveJournalTransaction
											.setFinJournalApprovalGroup(finJournalApprovalGroup);

									FinAccount finAccountUangMukaPelanggan = new FinAccount();
									finAccountUangMukaPelanggan
											.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());

									cashReceiveJournalTransaction
											.setFinAccount(finAccountUangMukaPelanggan);

									List<FinArFundsInReconRecord> cashReceiveFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
									cashReceiveFundsInRecordList
											.add(reconRecord);
									cashReceiveJournalTransaction
											.setFinArFundsInReconRecords(cashReceiveFundsInRecordList);
									cashReceiveJournalTransaction
											.setFinJournal(finJournalCashReceive);
									cashReceiveJournalTransaction
											.setFinPeriod(FinancePeriodUtil
													.getCurrentPeriod());
									cashReceiveJournalTransaction
											.setFinTransactionStatus(finTransactionStatus);
									cashReceiveJournalTransaction
											.setFinTransactionType(finTransactionType);

									cashReceiveJournalTransaction
											.setTransactionAmount((reconRecord
													.getPaymentAmount() != null ? reconRecord
													.getPaymentAmount()
													: new BigDecimal(0))
													.subtract(reconRecord
															.getRefundAmount()));
									cashReceiveJournalTransaction
											.setTransactionTimestamp(time);
									// set WCSOrderID
									cashReceiveJournalTransaction
											.setWcsOrderID(wcsOrderId);
									cashReceiveJournalTransaction
											.setVenBank(reconRecord
													.getVenOrderPayment() != null ? reconRecord
													.getVenOrderPayment()
													.getVenBank()
													: reconRecord
															.getFinArFundsInReport()
															.getFinArFundsInReportType()
															.getVenBank());
									cashReceiveJournalTransaction
											.setGroupJournal(accountNumberBank);
									// Persist the cash received journal
									// transaction
									CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI )");
									/*journalTransactionHome
											.persistFinJournalTransaction(cashReceiveJournalTransaction);*/
									if (!em.contains(cashReceiveJournalTransaction)) {
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "postCashReceiveJournalTransactions::Calling finJournalTransactionDAO.save explicitly");
										cashReceiveJournalTransaction = finJournalTransactionDAO.save(cashReceiveJournalTransaction);
									}
									transactionList
											.add(cashReceiveJournalTransaction);
								}
								/*
								 * Post the journal transaction for Refund
								 */
								FinJournalTransaction refundJournalTransaction = new FinJournalTransaction();
								refundJournalTransaction.setComments("");
								refundJournalTransaction
										.setCreditDebitFlag(true);
								refundJournalTransaction
										.setFinJournalApprovalGroup(finJournalApprovalGroup);
								// bankFeeJournalTransaction.setFinAccount(finAccountBank);

								// salah save. seharusnya save account number
								// hpp bank FIN_ACCOUNT_5110003
								FinAccount finAccountRefrund = new FinAccount();
								finAccountRefrund.setAccountId(type);
								refundJournalTransaction
										.setFinAccount(finAccountRefrund);

								List<FinArFundsInReconRecord> refundFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
								refundFundsInRecordList.add(reconRecord);
								refundJournalTransaction
										.setFinArFundsInReconRecords(refundFundsInRecordList);
								refundJournalTransaction
										.setFinJournal(finJournalCashReceive);
								refundJournalTransaction
										.setFinPeriod(FinancePeriodUtil
												.getCurrentPeriod());
								refundJournalTransaction
										.setFinTransactionStatus(finTransactionStatus);
								refundJournalTransaction
										.setFinTransactionType(finTransactionType);
								refundJournalTransaction
										.setTransactionAmount(reconRecord
												.getRefundAmount());
								refundJournalTransaction
										.setTransactionTimestamp(time);

								refundJournalTransaction
										.setWcsOrderID(wcsOrderId);
								refundJournalTransaction
										.setVenBank(reconRecord
												.getVenOrderPayment() != null ? reconRecord
												.getVenOrderPayment()
												.getVenBank() : reconRecord
												.getFinArFundsInReport()
												.getFinArFundsInReportType()
												.getVenBank());
								refundJournalTransaction
										.setGroupJournal(accountNumberBank);
								// Persist the fee journal transaction
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( REFUND)");
								/*journalTransactionHome
										.persistFinJournalTransaction(refundJournalTransaction);*/
								if (!em.contains(refundJournalTransaction)) {
									CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Calling finJournalTransactionDAO.save explicitly");
									refundJournalTransaction = finJournalTransactionDAO.save(refundJournalTransaction);
								}
								transactionList.add(refundJournalTransaction);

								// Create a refund record
								FinArFundsInRefund finArFundsInRefund = new FinArFundsInRefund();
								finArFundsInRefund
										.setFinArFundsInReconRecord(reconRecord);
								finArFundsInRefund.setApAmount(reconRecord
										.getRefundAmount());
								finArFundsInRefund.setRefundTimestamp(time);
								finArFundsInRefund.setRefundType(reconRecord
										.getFinArFundsInActionApplied()
										.getActionAppliedDesc());
								//refundRecordHome.persistFinArFundsInRefund(finArFundsInRefund);
								if (!em.contains(finArFundsInRefund)) {
									CommonUtil.logDebug(this.getClass().getCanonicalName(),
											"postCashReceiveJournalTransactions:: finArFundsInRefund not attached, calling save.");
									finArFundsInRefund = finArFundsInRefundDAO.save(finArFundsInRefund);
								}

							}
						} else if ((reconRecord
								.getFinArReconResult()
								.getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id())
								&& (reconRecord
										.getFinArFundsInActionApplied()
										.getActionAppliedId() != FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
							/*
							 * Post the CREDIT for UANG jaminan transaksi
							 */
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Start Create Journal order : "
									+ wcsOrderId);
							FinJournalTransaction cashReceiveJournalTransaction = new FinJournalTransaction();
							cashReceiveJournalTransaction
									.setComments("System Credit Uang Jaminan Transaksi:"
											+ wcsOrderId);
							cashReceiveJournalTransaction
									.setCreditDebitFlag(true);
							cashReceiveJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);

							FinAccount finAccountUangMukaPelanggan = new FinAccount();
							finAccountUangMukaPelanggan
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());

							cashReceiveJournalTransaction
									.setFinAccount(finAccountUangMukaPelanggan);

							List<FinArFundsInReconRecord> cashReceiveFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							cashReceiveFundsInRecordList.add(reconRecord);
							cashReceiveJournalTransaction
									.setFinArFundsInReconRecords(cashReceiveFundsInRecordList);
							cashReceiveJournalTransaction
									.setFinJournal(finJournalCashReceive);
							cashReceiveJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							cashReceiveJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							cashReceiveJournalTransaction
									.setFinTransactionType(finTransactionType);

							cashReceiveJournalTransaction
									.setTransactionAmount(reconRecord
											.getPaymentAmount());
							cashReceiveJournalTransaction
									.setTransactionTimestamp(time);
							// set WCSOrderID
							cashReceiveJournalTransaction
									.setWcsOrderID(wcsOrderId);
							cashReceiveJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							cashReceiveJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the cash received journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI )");
							/*journalTransactionHome
									.persistFinJournalTransaction(cashReceiveJournalTransaction);
							*/
							if (!em.contains(cashReceiveJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), 
										"postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								cashReceiveJournalTransaction = finJournalTransactionDAO.save(cashReceiveJournalTransaction);
							}
							cashReceivedTransactionList
									.add(cashReceiveJournalTransaction);
							transactionList.add(cashReceiveJournalTransaction);

							/*
							 * Post the journal transaction for PENGHASILAN
							 * BEBAN DAN LAIN-LAIN
							 */
							FinJournalTransaction penghasilanJournalTransaction = new FinJournalTransaction();
							penghasilanJournalTransaction.setComments("");
							if (reconRecord.getRemainingBalanceAmount()
									.compareTo(new BigDecimal(0)) < 0) {
								penghasilanJournalTransaction
										.setCreditDebitFlag(true);
							} else {
								penghasilanJournalTransaction
										.setCreditDebitFlag(false);
							}
							penghasilanJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);

							// salah save. seharusnya save account number hpp
							// bank FIN_ACCOUNT_5110003
							FinAccount finAccountPenghasilan = new FinAccount();
							finAccountPenghasilan
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_7140001.id());
							penghasilanJournalTransaction
									.setFinAccount(finAccountPenghasilan);

							List<FinArFundsInReconRecord> penghasilanFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							penghasilanFundsInRecordList.add(reconRecord);
							penghasilanJournalTransaction
									.setFinArFundsInReconRecords(penghasilanFundsInRecordList);
							penghasilanJournalTransaction
									.setFinJournal(finJournalCashReceive);
							penghasilanJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							penghasilanJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							penghasilanJournalTransaction
									.setFinTransactionType(finTransactionType);
							penghasilanJournalTransaction
									.setTransactionAmount(reconRecord
											.getRemainingBalanceAmount().abs());
							penghasilanJournalTransaction
									.setTransactionTimestamp(time);

							penghasilanJournalTransaction
									.setWcsOrderID(wcsOrderId);
							penghasilanJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							penghasilanJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( HPP BIAYA BARANG)");
							/*journalTransactionHome
									.persistFinJournalTransaction(penghasilanJournalTransaction);*/
							if (!em.contains(penghasilanJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								penghasilanJournalTransaction = finJournalTransactionDAO.save(penghasilanJournalTransaction);
							}
							transactionList.add(penghasilanJournalTransaction);

						} else if ((reconRecord
								.getFinArReconResult()
								.getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id())
								&& (reconRecord
										.getFinArFundsInActionApplied()
										.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
							/*
							 * Post the journal transaction for uang jaminan
							 * transaksi
							 */
							FinJournalTransaction belumTerindetifikasiJournalTransaction = new FinJournalTransaction();
							belumTerindetifikasiJournalTransaction
									.setComments("");
							belumTerindetifikasiJournalTransaction
									.setCreditDebitFlag(true);

							belumTerindetifikasiJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);

							// salah save. seharusnya save account number
							// FIN_ACCOUNT_2230002
							FinAccount finAccountPenghasilan = new FinAccount();
							finAccountPenghasilan
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());
							belumTerindetifikasiJournalTransaction
									.setFinAccount(finAccountPenghasilan);

							List<FinArFundsInReconRecord> belumTerindetifikasiFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							belumTerindetifikasiFundsInRecordList
									.add(reconRecord);
							belumTerindetifikasiJournalTransaction
									.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							belumTerindetifikasiJournalTransaction
									.setFinJournal(finJournalCashReceive);
							belumTerindetifikasiJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							belumTerindetifikasiJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							belumTerindetifikasiJournalTransaction
									.setFinTransactionType(finTransactionType);
							belumTerindetifikasiJournalTransaction
									.setTransactionAmount(reconRecord
											.getPaymentAmount() != null ? reconRecord
											.getPaymentAmount()
											: new BigDecimal(0));
							belumTerindetifikasiJournalTransaction
									.setTransactionTimestamp(time);

							belumTerindetifikasiJournalTransaction
									.setWcsOrderID(wcsOrderId);
							belumTerindetifikasiJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							belumTerindetifikasiJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi )");
							/*journalTransactionHome
									.persistFinJournalTransaction(belumTerindetifikasiJournalTransaction);*/
							if (!em.contains(belumTerindetifikasiJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								belumTerindetifikasiJournalTransaction = finJournalTransactionDAO.save(belumTerindetifikasiJournalTransaction);
							}
							transactionList
									.add(belumTerindetifikasiJournalTransaction);

							FinJournalTransaction belumTerindetifikasiJournalTransactions = new FinJournalTransaction();
							belumTerindetifikasiJournalTransactions
									.setComments("");
							belumTerindetifikasiJournalTransactions
									.setCreditDebitFlag(false);

							belumTerindetifikasiJournalTransactions
									.setFinJournalApprovalGroup(finJournalApprovalGroup);
							belumTerindetifikasiJournalTransactions
									.setFinAccount(finAccountPenghasilan);

							belumTerindetifikasiJournalTransactions
									.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							belumTerindetifikasiJournalTransactions
									.setFinJournal(finJournalCashReceive);
							belumTerindetifikasiJournalTransactions
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							belumTerindetifikasiJournalTransactions
									.setFinTransactionStatus(finTransactionStatus);
							belumTerindetifikasiJournalTransactions
									.setFinTransactionType(finTransactionType);
							belumTerindetifikasiJournalTransactions
									.setTransactionAmount(itemsAllocate.get(0)
											.getAmount());
							belumTerindetifikasiJournalTransactions
									.setTransactionTimestamp(time);

							belumTerindetifikasiJournalTransactions
									.setWcsOrderID(wcsOrderId);
							belumTerindetifikasiJournalTransactions
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							belumTerindetifikasiJournalTransactions
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi)");
							/*journalTransactionHome
									.persistFinJournalTransaction(belumTerindetifikasiJournalTransactions);*/
							if (!em.contains(belumTerindetifikasiJournalTransactions)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								belumTerindetifikasiJournalTransactions = finJournalTransactionDAO.save(belumTerindetifikasiJournalTransactions);
							}
							transactionList
									.add(belumTerindetifikasiJournalTransactions);

							FinJournalTransaction cashReceiveJournalTransaction = new FinJournalTransaction();
							cashReceiveJournalTransaction.setComments("");
							cashReceiveJournalTransaction
									.setCreditDebitFlag(true);
							cashReceiveJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);
							cashReceiveJournalTransaction
									.setFinAccount(finAccountPenghasilan);

							cashReceiveJournalTransaction
									.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							cashReceiveJournalTransaction
									.setFinJournal(finJournalCashReceive);
							cashReceiveJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							cashReceiveJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							cashReceiveJournalTransaction
									.setFinTransactionType(finTransactionType);

							cashReceiveJournalTransaction
									.setTransactionAmount(itemsAllocate.get(0)
											.getAmount());
							cashReceiveJournalTransaction
									.setTransactionTimestamp(time);
							// set WCSOrderID
							if (!itemsAllocate.isEmpty()
									&& itemsAllocate != null) {
								/*
								List<FinArFundsInReconRecord> destItems = fundsInReconRecordHome
										.queryByRange(
												"select o from FinArFundsInReconRecord o where o.reconciliationRecordId = "
														+ itemsAllocate
																.get(0)
																.getIdReconRecordDest()
														+ " and o.finArFundsInActionApplied.actionAppliedId<>"
														+ VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED,
												0, 0);
								*/
								FinArFundsInReconRecord destItem = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(
										itemsAllocate.get(0).getIdReconRecordDest());
									
								if (destItem != null) {
									wcsOrderId = destItem.getWcsOrderId();
								}
							}
							cashReceiveJournalTransaction
									.setWcsOrderID(wcsOrderId);
							cashReceiveJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							cashReceiveJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the cash received journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi )");
							/*journalTransactionHome
									.persistFinJournalTransaction(cashReceiveJournalTransaction);*/
							if (!em.contains(cashReceiveJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								cashReceiveJournalTransaction = finJournalTransactionDAO.save(cashReceiveJournalTransaction);
							}
							transactionList.add(cashReceiveJournalTransaction);
						} else if ((reconRecord
								.getFinArReconResult()
								.getReconResultId() != FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id())
								&& (reconRecord
										.getFinArFundsInActionApplied()
										.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
							/*
							 * Post the journal transaction for uang jaminan
							 * transaksi
							 */
							FinJournalTransaction belumTerindetifikasiJournalTransaction = new FinJournalTransaction();
							belumTerindetifikasiJournalTransaction
									.setComments("");
							belumTerindetifikasiJournalTransaction
									.setCreditDebitFlag(true);

							belumTerindetifikasiJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);

							// salah save. seharusnya save account number
							// FIN_ACCOUNT_2230002
							FinAccount finAccountPenghasilan = new FinAccount();
							finAccountPenghasilan
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230002.id());
							belumTerindetifikasiJournalTransaction
									.setFinAccount(finAccountPenghasilan);

							List<FinArFundsInReconRecord> belumTerindetifikasiFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							belumTerindetifikasiFundsInRecordList
									.add(reconRecord);
							belumTerindetifikasiJournalTransaction
									.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							belumTerindetifikasiJournalTransaction
									.setFinJournal(finJournalCashReceive);
							belumTerindetifikasiJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							belumTerindetifikasiJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							belumTerindetifikasiJournalTransaction
									.setFinTransactionType(finTransactionType);
							belumTerindetifikasiJournalTransaction
									.setTransactionAmount(itemsAllocate != null ? itemsAllocate
											.get(0).getPaidamount()
											: new BigDecimal(0));
							belumTerindetifikasiJournalTransaction
									.setTransactionTimestamp(time);

							belumTerindetifikasiJournalTransaction
									.setWcsOrderID(wcsOrderId);
							belumTerindetifikasiJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							belumTerindetifikasiJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi )");
							/*journalTransactionHome
									.persistFinJournalTransaction(belumTerindetifikasiJournalTransaction);*/
							if (!em.contains(belumTerindetifikasiJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), 
										"postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								belumTerindetifikasiJournalTransaction = finJournalTransactionDAO.save(belumTerindetifikasiJournalTransaction);
							}
							transactionList
									.add(belumTerindetifikasiJournalTransaction);

							FinJournalTransaction belumTerindetifikasiJournalTransactions = new FinJournalTransaction();
							belumTerindetifikasiJournalTransactions
									.setComments("");
							belumTerindetifikasiJournalTransactions
									.setCreditDebitFlag(false);

							belumTerindetifikasiJournalTransactions
									.setFinJournalApprovalGroup(finJournalApprovalGroup);
							belumTerindetifikasiJournalTransactions
									.setFinAccount(finAccountPenghasilan);

							belumTerindetifikasiJournalTransactions
									.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							belumTerindetifikasiJournalTransactions
									.setFinJournal(finJournalCashReceive);
							belumTerindetifikasiJournalTransactions
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							belumTerindetifikasiJournalTransactions
									.setFinTransactionStatus(finTransactionStatus);
							belumTerindetifikasiJournalTransactions
									.setFinTransactionType(finTransactionType);
							belumTerindetifikasiJournalTransactions
									.setTransactionAmount(itemsAllocate.get(0)
											.getAmount());
							belumTerindetifikasiJournalTransactions
									.setTransactionTimestamp(time);

							belumTerindetifikasiJournalTransactions
									.setWcsOrderID(wcsOrderId);
							belumTerindetifikasiJournalTransactions
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							belumTerindetifikasiJournalTransactions
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi)");
							/*journalTransactionHome
									.persistFinJournalTransaction(belumTerindetifikasiJournalTransactions);*/
							if (!em.contains(belumTerindetifikasiJournalTransactions)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), 
										"postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								belumTerindetifikasiJournalTransactions = finJournalTransactionDAO.save(belumTerindetifikasiJournalTransactions);
							}
							transactionList
									.add(belumTerindetifikasiJournalTransactions);

							FinJournalTransaction cashReceiveJournalTransaction = new FinJournalTransaction();
							cashReceiveJournalTransaction.setComments("");
							cashReceiveJournalTransaction
									.setCreditDebitFlag(true);
							cashReceiveJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);

							FinAccount finAccountPenghasilannew = new FinAccount();
							finAccountPenghasilannew
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());

							cashReceiveJournalTransaction
									.setFinAccount(finAccountPenghasilannew);

							cashReceiveJournalTransaction
									.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							cashReceiveJournalTransaction
									.setFinJournal(finJournalCashReceive);
							cashReceiveJournalTransaction
									.setFinPeriod(FinancePeriodUtil
											.getCurrentPeriod());
							cashReceiveJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							cashReceiveJournalTransaction
									.setFinTransactionType(finTransactionType);

							cashReceiveJournalTransaction
									.setTransactionAmount(itemsAllocate.get(0)
											.getAmount());
							cashReceiveJournalTransaction
									.setTransactionTimestamp(time);
							// set WCSOrderID
							if (!itemsAllocate.isEmpty()
									&& itemsAllocate != null) {
								/*
								List<FinArFundsInReconRecord> destItems = fundsInReconRecordHome
										.queryByRange(
												"select o from FinArFundsInReconRecord o where o.reconciliationRecordId = "
														+ itemsAllocate
																.get(0)
																.getIdReconRecordDest()
														+ " and o.finArFundsInActionApplied.actionAppliedId<>"
														+ VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED,
												0, 0);
								*/
								FinArFundsInReconRecord destItem = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(
										itemsAllocate.get(0).getIdReconRecordDest());
								if (destItem != null) {
									wcsOrderId = destItem.getWcsOrderId();
								}
							}
							cashReceiveJournalTransaction.setWcsOrderID(wcsOrderId);
							cashReceiveJournalTransaction.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord.getVenOrderPayment().getVenBank()
											: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank());
							cashReceiveJournalTransaction.setGroupJournal(accountNumberBank);
							// Persist the cash received journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi )");
							if (!em.contains(cashReceiveJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), 
										"postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								cashReceiveJournalTransaction = finJournalTransactionDAO.save(cashReceiveJournalTransaction);
							}
							transactionList.add(cashReceiveJournalTransaction);
						} else {
							/*
							 * Post the journal transaction for PENGHASILAN
							 * BEBAN DAN LAIN-LAIN
							 */
							FinJournalTransaction belumTerindetifikasiJournalTransaction = new FinJournalTransaction();
							belumTerindetifikasiJournalTransaction
									.setComments("");
							belumTerindetifikasiJournalTransaction
									.setCreditDebitFlag(true);

							belumTerindetifikasiJournalTransaction
									.setFinJournalApprovalGroup(finJournalApprovalGroup);
							// bankFeeJournalTransaction.setFinAccount(finAccountBank);

							// salah save. seharusnya save account number hpp
							// bank FIN_ACCOUNT_5110003
							FinAccount finAccountPenghasilan = new FinAccount();
							finAccountPenghasilan
									.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230002.id());
							belumTerindetifikasiJournalTransaction
									.setFinAccount(finAccountPenghasilan);

							List<FinArFundsInReconRecord> belumTerindetifikasiFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
							belumTerindetifikasiFundsInRecordList.add(reconRecord);
							belumTerindetifikasiJournalTransaction.setFinArFundsInReconRecords(belumTerindetifikasiFundsInRecordList);
							belumTerindetifikasiJournalTransaction.setFinJournal(finJournalCashReceive);
							belumTerindetifikasiJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
							belumTerindetifikasiJournalTransaction
									.setFinTransactionStatus(finTransactionStatus);
							belumTerindetifikasiJournalTransaction
									.setFinTransactionType(finTransactionType);
							belumTerindetifikasiJournalTransaction
									.setTransactionAmount(reconRecord
											.getProviderReportPaidAmount() != null ? reconRecord
											.getProviderReportPaidAmount()
											: new BigDecimal(0));
							belumTerindetifikasiJournalTransaction
									.setTransactionTimestamp(time);

							belumTerindetifikasiJournalTransaction
									.setWcsOrderID(wcsOrderId);
							belumTerindetifikasiJournalTransaction
									.setVenBank(reconRecord
											.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord
													.getFinArFundsInReport()
													.getFinArFundsInReportType()
													.getVenBank());
							belumTerindetifikasiJournalTransaction
									.setGroupJournal(accountNumberBank);
							// Persist the fee journal transaction
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::Save ( Uang jaminan transaksi belum terindentifikasi)");
							/*journalTransactionHome
									.persistFinJournalTransaction(belumTerindetifikasiJournalTransaction);*/
							if (!em.contains(belumTerindetifikasiJournalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalTransactionDAO.save explicitly");
								belumTerindetifikasiJournalTransaction = finJournalTransactionDAO.save(belumTerindetifikasiJournalTransaction);								
							}
								
							transactionList
									.add(belumTerindetifikasiJournalTransaction);
						}

						if (itemsAllocate != null
								&& (reconRecord
										.getFinArFundsInActionApplied()
										.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
							FinArFundsInAllocatePayment finallocate = itemsAllocate
									.get(0);
							finallocate.setIsactive(false);
							//finArFundsInAllocateHome.mergeFinArFundsInAllocatePayment(finallocate);
							if (!em.contains(finallocate)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finArFundsInAllocatePaymentDAO.save explicitly.");
								finallocate = finArFundsInAllocatePaymentDAO.save(finallocate);
							}
						}
						// merge the recon record with the list of transactions
						reconRecord.setFinJournalTransactions(transactionList);
						/*
						reconRecord = fundsInReconRecordHome
								.mergeFinArFundsInReconRecord(reconRecord);
						*/
						if (!em.contains(reconRecord)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "postCashReceiveJournalTransactions::calling finArFundsInReconRecordDAO.save explicitly.");
							reconRecord = finArFundsInReconRecordDAO.save(reconRecord);
						}
						
						if (transactionList.size() > 0) {
							count++;
						}
					}
				}
			}
			if (finJournalApprovalGroup != null && count == 0) {
				/*journalApprovalGroupHome
						.removeFinJournalApprovalGroup(finJournalApprovalGroup);*/
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransactions::calling finJournalApprovalGroupDAO.save explicitly.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting cash receive journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}
		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCashReceiveJournalTransaction()" + " completed in "
				+ duration + "ms");
		return true;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postVirtualAccountH1Journal(
			ArrayList<Long> finArFundsInReconRecordIdList) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::BEGIN");
		Long startTime = System.currentTimeMillis();
		try {

			if (finArFundsInReconRecordIdList.isEmpty()) {
				throw new EJBException(
						"Empty list passed to postVirtualAccountH1Journal. The reconciliation record list must contain entries");
			}

			// Read all the relevant funds in records from the database
			List<FinArFundsInReconRecord> reconRecordList = new ArrayList<FinArFundsInReconRecord>();
			for (Long reconciliationRecordId : finArFundsInReconRecordIdList) {
				FinArFundsInReconRecord reconRecordTemp = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(
						reconciliationRecordId);
				if (reconRecordTemp != null) {
					reconRecordList.add(reconRecordTemp);
				} else {
					throw new EJBException(
							"List passed to postCashReceiveJournalTransaction contains no valid keys. The reconciliation record list must contain valid keys for existing reconciliation records");
				}
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			Timestamp time = new Timestamp(System.currentTimeMillis());
			/**
			 * persist journal group
			 */
			FinJournalApprovalGroup finJournalApprovalGroup = new FinJournalApprovalGroup();
			FinJournal finJournalCashReceive = new FinJournal();
			finJournalCashReceive
					.setJournalId(FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id());
			FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
			finApprovalStatus
					.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
			finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);
			finJournalApprovalGroup.setFinJournal(finJournalCashReceive);
			finJournalApprovalGroup
					.setJournalGroupDesc("Cash Received Journal for :"
							+ sdf.format(time));
			finJournalApprovalGroup.setJournalGroupTimestamp(time);
			//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);

			int count = 0;
			for (FinArFundsInReconRecord reconRecord : reconRecordList) {
				ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();
				String wcsOrderId = reconRecord.getWcsOrderId() != null ? reconRecord
						.getWcsOrderId() : reconRecord.getWcsOrderId();
				List<FinArFundsInAllocatePayment> itemsAllocate = null;
				Long accountNumberBank = null;
				boolean createJournal = true;

				if ((reconRecord
						.getFinArFundsInActionApplied()
						.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
					itemsAllocate = finArFundsInAllocatePaymentDAO.findByIdReconRecordSourceIsActive(
							reconRecord.getReconciliationRecordId());
				}
				List<FinArFundsInAllocatePayment> cekIdRecordDest = finArFundsInAllocatePaymentDAO.findByIdReconRecordDest(
						reconRecord.getReconciliationRecordId());
				if (!cekIdRecordDest.isEmpty()
						&& (reconRecord
								.getFinArFundsInActionApplied()
								.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id())) {
					createJournal = false;
				}
				if (createJournal) {
					if (reconRecord.getFinArFundsInReport()
							.getFinArFundsInReportType()
							.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA.id()) {
						accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120102.id();
					} else if (reconRecord.getFinArFundsInReport()
							.getFinArFundsInReportType()
							.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA.id()) {
						accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120301.id();
					}

					BigDecimal paidAmount = reconRecord
							.getProviderReportPaidAmount() != null ? reconRecord
							.getProviderReportPaidAmount() : new BigDecimal(0);
					BigDecimal paymentAmountOrderDestination = new BigDecimal(0);
					FinArFundsInReconRecord destItem = null;
					if ((reconRecord
							.getFinArFundsInActionApplied()
							.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())
							&& itemsAllocate != null
							&& !itemsAllocate.isEmpty()) {
						paidAmount = itemsAllocate.get(0).getAmount() != null ? itemsAllocate
								.get(0).getAmount() : new BigDecimal(0);

						destItem = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(
								itemsAllocate.get(0).getIdReconRecordDest());
						if (destItem != null) {
							paymentAmountOrderDestination = destItem.getPaymentAmount();
						}

					}
					/**
					 * Journal untuk report realtime Post the journal
					 * transaction for Ayat-Ayat Silang Bank
					 */
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( Ayat-Ayat Silang Bank) ::BEGIN");
					FinJournalTransaction ayatSilangBankJournalTransaction = new FinJournalTransaction();
					ayatSilangBankJournalTransaction = setJournalTransaction(
							reconRecord,
							wcsOrderId,
							finJournalApprovalGroup,
							FinAccountConstants.FIN_ACCOUNT_1120999.id(),
							FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id(),
							FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
							FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_AYAT_AYAT_SILANG_PADA_BANK.id(),
							time,
							reconRecord.getVenOrderPayment() != null ? reconRecord
									.getVenOrderPayment().getVenBank()
									: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank(), 
							accountNumberBank, 
							paidAmount, false,
							"System Debit Ayat-Ayat Silang Bank :" + wcsOrderId);
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( Ayat-Ayat Silang Bank)::END");
					transactionList.add(ayatSilangBankJournalTransaction);

					boolean debetOrCredit = true;
					BigDecimal remainingBalanceAmount = paidAmount
							.subtract(reconRecord.getPaymentAmount() != null ? reconRecord
									.getPaymentAmount() : new BigDecimal(0));
					BigDecimal paymentAmount = reconRecord.getPaymentAmount() != null ? reconRecord
							.getPaymentAmount() : new BigDecimal(0);
					if ((reconRecord
							.getFinArFundsInActionApplied()
							.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id())) {
						remainingBalanceAmount = paidAmount
								.subtract(paymentAmountOrderDestination);
						if (destItem != null) {
							paymentAmount = destItem.getPaymentAmount();
							wcsOrderId = destItem.getWcsOrderId();
						}
					}
					if (remainingBalanceAmount.compareTo(new BigDecimal(0)) < 1) {
						debetOrCredit = false;
					}

					if ((reconRecord.getFinArReconResult().getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id())
							|| (reconRecord
									.getFinArFundsInActionApplied()
									.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id()) && (paidAmount
									.subtract(paymentAmountOrderDestination)
									.abs().compareTo(new BigDecimal(5000)) <= 0)) {
						/**
						 * Post the journal transaction for Uang Jaminan
						 * Transaksi
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( Uang Jaminan Transaksi) ::BEGIN");
						FinJournalTransaction uangJaminanTransaksiJournalTransaction = new FinJournalTransaction();
						uangJaminanTransaksiJournalTransaction = setJournalTransaction(
								reconRecord,
								wcsOrderId,
								finJournalApprovalGroup,
								FinAccountConstants.FIN_ACCOUNT_2230001.id(),
								FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id(),
								FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
								FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id(),
								time,
								reconRecord.getVenOrderPayment() != null ? reconRecord.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank(),
								accountNumberBank,
								paymentAmount, true,
								"System Credit Uang Jaminan Transaksi :"
										+ wcsOrderId);
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( Uang Jaminan Transaksi)::END");
						transactionList
								.add(uangJaminanTransaksiJournalTransaction);

						if (remainingBalanceAmount.compareTo(new BigDecimal(0)) != 0) {
							/**
							 * Post the journal transaction for lebih/kurang
							 * bayar
							 */
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( lebih/kurang bayar) ::BEGIN");
							FinJournalTransaction lebihKurangBayarJournalTransaction = new FinJournalTransaction();
							lebihKurangBayarJournalTransaction = setJournalTransaction(
									reconRecord,
									wcsOrderId,
									finJournalApprovalGroup,
									FinAccountConstants.FIN_ACCOUNT_7140001.id(),
									FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id(),
									FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
									FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_LEBIH_ATAU_KURANG_BAYAR.id(),
									time,
									reconRecord.getVenOrderPayment() != null ? reconRecord
											.getVenOrderPayment().getVenBank()
											: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank(),
									accountNumberBank,
									remainingBalanceAmount.abs(),
									debetOrCredit,
									"System Lebih / Kurang Bayar :"
											+ wcsOrderId);
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save (lebih/kurang bayar)::END");
							transactionList
									.add(lebihKurangBayarJournalTransaction);
						}
					} else {

						/**
						 * Post the journal transaction for Uang Jaminan
						 * Transaksi Belum Teridentifikasi
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( Uang Jaminan Transaksi Belum Teridentifikasi) ::BEGIN");
						FinJournalTransaction uangJaminanTransaksiBelumTeridentifikasiJournalTransaction = new FinJournalTransaction();
						uangJaminanTransaksiBelumTeridentifikasiJournalTransaction = setJournalTransaction(
								reconRecord,
								wcsOrderId,
								finJournalApprovalGroup,
								FinAccountConstants.FIN_ACCOUNT_2230002.id(),
								FinJournalConstants.FIN_JOURNAL_CASH_RECEIVE.id(),
								FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
								FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_BELUM_TERIDENTIFIKASI.id(),
								time,
								reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank(),
								accountNumberBank,
								paymentAmount, true,
								"System Credit Uang Jaminan Transaksi Belum Teridentifikasi :"
										+ wcsOrderId);
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::Save ( Uang Jaminan Transaksi Belum Teridentifikasi)::END");
						transactionList
								.add(uangJaminanTransaksiBelumTeridentifikasiJournalTransaction);
					}

					if (itemsAllocate != null
							&& reconRecord
									.getFinArFundsInActionApplied()
									.getActionAppliedId()
									.equals(VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED)) {
						FinArFundsInAllocatePayment finallocate = itemsAllocate
								.get(0);
						finallocate.setIsactive(false);
						if (!em.contains(finallocate)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::calling finArFundsInAllocatePaymentDAO.save explicitly.");
							finallocate = finArFundsInAllocatePaymentDAO.save(finallocate);
						}
					}
					// merge the recon record with the list of transactions
					reconRecord.setFinJournalTransactions(transactionList);
					reconRecord.setFinApprovalStatus(finApprovalStatus);
					if (!em.contains(reconRecord)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualaccountH1Journal::calling finArFundsInReconRecordDAO.save explicitly.");
						reconRecord = finArFundsInReconRecordDAO.save(reconRecord);
					}
					count++;
				}
			}
			/**
			 * remove group journal jika tidak ada journal detail yang terbentuk
			 */
			if (finJournalApprovalGroup != null && count == 0) {
				//journalApprovalGroupHome.removeFinJournalApprovalGroup(finJournalApprovalGroup);
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::removing group journal if there's no detail");
				finJournalApprovalGroupDAO.delete(finJournalApprovalGroup);
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting Virtual Account H1 journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postVirtualAccountH1Journal::" + " completed in "
				+ duration + "ms");
		return true;
	}

	/**
	 * 
	 * @param reconRecord
	 * @param wcsOrderId
	 *            > order dari journal
	 * @param finJournalApprovalGroup
	 *            -> status approval untuk group journal
	 * @param accountId
	 *            -> COA Journal
	 * @param finJournalID
	 *            -> jenis journal Group
	 * @param finTransactionStatusID
	 *            -> status dari journal detail
	 * @param finTransactionTypeID
	 *            -> jenis journal detail
	 * @param time
	 *            ->tanggal terbentuk journal
	 * @param bank
	 *            -> bank dari journal
	 * @param journalTransactionHome
	 *            -> Beans
	 * @param accountNumberBankForGroup
	 *            -> COA group Journal berdasarkan bank
	 * @param amount
	 *            -> uang transaksi
	 * @param creditDebitFlag
	 *            -> true=credit dan false=debet
	 * @param Comment
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public FinJournalTransaction setJournalTransaction(
			FinArFundsInReconRecord reconRecord, String wcsOrderId,
			FinJournalApprovalGroup finJournalApprovalGroup, Long accountId,
			Long finJournalID, Long finTransactionStatusID,
			Long finTransactionTypeID, Timestamp time, VenBank bank,
			//FinJournalTransactionSessionEJBLocal journalTransactionHome,
			Long accountNumberBankForGroup, BigDecimal amount,
			boolean creditDebitFlag, String Comment) {

	    CommonUtil.logDebug(this.getClass().getCanonicalName(), "setJournalTransaction::BEGIN => ");
		Long startTime = System.currentTimeMillis();

		FinJournalTransaction journalTransaction = null;
		try {
			FinJournal finJournal = new FinJournal();
			finJournal.setJournalId(finJournalID);

			FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
			finTransactionStatus.setTransactionStatusId(finTransactionStatusID);

			FinTransactionType finTransactionType = new FinTransactionType();
			finTransactionType.setTransactionTypeId(finTransactionTypeID);

			/**
			 * Set Journal Transaction
			 */
			journalTransaction = new FinJournalTransaction();
			journalTransaction.setComments(Comment);
			journalTransaction.setCreditDebitFlag(creditDebitFlag);
			journalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountAyatAyatSilangBank = new FinAccount();
			finAccountAyatAyatSilangBank.setAccountId(accountId);
			journalTransaction.setFinAccount(finAccountAyatAyatSilangBank);

			List<FinArFundsInReconRecord> fundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
			fundsInRecordList.add(reconRecord);
			journalTransaction.setFinArFundsInReconRecords(fundsInRecordList);
			journalTransaction.setFinJournal(finJournal);
			journalTransaction.setFinPeriod(FinancePeriodUtil
					.getCurrentPeriod());
			journalTransaction.setFinTransactionStatus(finTransactionStatus);
			journalTransaction.setFinTransactionType(finTransactionType);
			journalTransaction.setTransactionAmount(amount);
			journalTransaction.setTransactionTimestamp(time);

			journalTransaction.setWcsOrderID(wcsOrderId);
			journalTransaction.setVenBank(bank);
			journalTransaction.setGroupJournal(accountNumberBankForGroup);
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "setJournalTransaction::Save ( Journal Transaction)");
			/*
			journalTransaction = journalTransactionHome
					.persistFinJournalTransaction(journalTransaction);
			*/
			if (!em.contains(journalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "setJournalTransaction::calling finJournalTransactionDAO.save explicitly");
				journalTransaction = finJournalTransactionDAO.save(journalTransaction);
			}
		} catch (Exception e) {
			String errMsg = "An Exception occured when posting journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "setJournalTransaction::" + " completed in " + duration
				+ "ms");
		return journalTransaction;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postAllocationJournal(
			FinArFundsInReconRecord reconRecord,
			FinArFundsInAllocatePayment finArFundsInAllocatePayment,
			List<FinArFundsInReconRecord> reconRecordList) {

		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::BEGIN");
		Long startTime = System.currentTimeMillis();

		try {
			String wcsOrderId = reconRecord.getWcsOrderId() != null ? reconRecord
					.getWcsOrderId() : reconRecord.getWcsOrderId();

			FinArFundsInReconRecord destItem = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(
					finArFundsInAllocatePayment.getIdReconRecordDest());

			ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

			if (destItem != null) {

				FinJournal finJournalAllocation = new FinJournal();
				finJournalAllocation
						.setJournalId(FinJournalConstants.FIN_JOURNAL_ALLOCATION.id());

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
				FinJournalApprovalGroup finJournalApprovalGroups = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
				finJournalApprovalGroups
						.setFinApprovalStatus(finApprovalStatus);
				finJournalApprovalGroups.setFinJournal(finJournalAllocation);
				finJournalApprovalGroups
						.setJournalGroupDesc("Allocation Journal for :"
								+ sdf.format(new Date()));
				finJournalApprovalGroups
						.setJournalGroupTimestamp(new Timestamp(System
								.currentTimeMillis()));

				// initialize journal account
				FinAccount finAccount2230001 = new FinAccount();
				finAccount2230001
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id()); // UANG JAMINAN TRANSAKSI
				FinAccount finAccount2230002 = new FinAccount();
				finAccount2230002
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230002.id()); // UANG JAMINAN TRANSAKSI BELUM TERIDENTIFIKASI

				FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
				finTransactionStatus
						.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
				FinTransactionType finTransactionType = new FinTransactionType();
				finTransactionType
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id());

				// Persist the journal group
				if (!em.contains(finJournalApprovalGroups)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalApprovalGroupDAO.save explicitly.");
					finJournalApprovalGroups = finJournalApprovalGroupDAO.save(finJournalApprovalGroups);
				}

				// decide which journal allocation type belongs to, based on the
				// calculation between payment amt and allocation amt
				if (reconRecord.getFinArReconResult().getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id()) {
					// recon's source is considered as 'Not Recognized', create
					// Journal allocation for Payment Not Recognized

					// DEBIT the account
					FinJournalTransaction originAllocJournalTransaction = new FinJournalTransaction();
					StringBuffer debitMsgBuffer = new StringBuffer();
					debitMsgBuffer.append("Allocation DEBIT from WCS Order:");
					debitMsgBuffer.append(wcsOrderId);
					debitMsgBuffer.append(" to WCS Order:");
					debitMsgBuffer.append(destItem.getWcsOrderId());
					originAllocJournalTransaction.setComments(debitMsgBuffer.toString());
					originAllocJournalTransaction.setCreditDebitFlag(false);
					originAllocJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroups);
					originAllocJournalTransaction.setFinAccount(finAccount2230002);
					originAllocJournalTransaction.setFinArFundsInReconRecords(reconRecordList);
					originAllocJournalTransaction.setFinJournal(finJournalAllocation);
					originAllocJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());

					originAllocJournalTransaction.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType finTransactionTypeNotRecognized = new FinTransactionType();
					finTransactionTypeNotRecognized.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_BELUM_TERIDENTIFIKASI.id());
					originAllocJournalTransaction.setFinTransactionType(finTransactionTypeNotRecognized);
					originAllocJournalTransaction.setTransactionAmount(finArFundsInAllocatePayment.getAmount());
					originAllocJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

					originAllocJournalTransaction.setWcsOrderID(reconRecord.getWcsOrderId() != null ? reconRecord.getWcsOrderId() : null);
					originAllocJournalTransaction.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
							.getVenOrderPayment().getVenBank() : reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank());
					// Persist the DEBIT journal transaction
					if (!em.contains(originAllocJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
						originAllocJournalTransaction = finJournalTransactionDAO.save(originAllocJournalTransaction);
					}
					transactionList.add(originAllocJournalTransaction);

					// CREDIT the account
					FinJournalTransaction destAllocJournalTransaction = new FinJournalTransaction();
					StringBuffer creditMsgBuffer = new StringBuffer();
					creditMsgBuffer.append("Allocation CREDIT from WCS Order:");
					creditMsgBuffer.append(wcsOrderId);
					creditMsgBuffer.append(" to WCS Order:");
					creditMsgBuffer.append(destItem.getWcsOrderId());
					destAllocJournalTransaction.setComments(creditMsgBuffer.toString());
					destAllocJournalTransaction.setCreditDebitFlag(true);
					destAllocJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroups);

					destAllocJournalTransaction.setFinAccount(finAccount2230001);

					destAllocJournalTransaction.setFinArFundsInReconRecords(reconRecordList);
					destAllocJournalTransaction.setFinJournal(finJournalAllocation);
					destAllocJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					destAllocJournalTransaction.setFinTransactionStatus(finTransactionStatus);

					destAllocJournalTransaction.setFinTransactionType(finTransactionType);
					destAllocJournalTransaction.setTransactionAmount(destItem.getPaymentAmount());
					destAllocJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

					destAllocJournalTransaction.setWcsOrderID(destItem.getWcsOrderId());
					destAllocJournalTransaction.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
							.getVenOrderPayment().getVenBank() : reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank());
					// Persist the CREDIT journal transaction
					if (!em.contains(destAllocJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
						destAllocJournalTransaction = finJournalTransactionDAO.save(destAllocJournalTransaction);
					}
					transactionList.add(destAllocJournalTransaction);

					switch (finArFundsInAllocatePayment.getAmount().compareTo(
							destItem.getPaymentAmount())) {
					case -1:
						FinJournalTransaction destDebitDevAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer debitDeviationMsgBuffer = new StringBuffer();
						debitDeviationMsgBuffer
								.append("Allocation DEBIT (deviation) from WCS Order:");
						debitDeviationMsgBuffer.append(wcsOrderId);
						debitDeviationMsgBuffer.append(" to WCS Order:");
						debitDeviationMsgBuffer.append(destItem.getWcsOrderId());
						destDebitDevAllocJournalTransaction.setComments(debitDeviationMsgBuffer.toString());

						destDebitDevAllocJournalTransaction.setCreditDebitFlag(false);
						destDebitDevAllocJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroups);

						destDebitDevAllocJournalTransaction.setFinAccount(finAccount2230001);

						destDebitDevAllocJournalTransaction.setFinArFundsInReconRecords(reconRecordList);
						destDebitDevAllocJournalTransaction.setFinJournal(finJournalAllocation);
						destDebitDevAllocJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						destDebitDevAllocJournalTransaction.setFinTransactionStatus(finTransactionStatus);

						destDebitDevAllocJournalTransaction.setFinTransactionType(finTransactionType);
						destDebitDevAllocJournalTransaction
								.setTransactionAmount(finArFundsInAllocatePayment.getAmount().subtract(destItem.getPaymentAmount()));
						destDebitDevAllocJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

						destDebitDevAllocJournalTransaction.setWcsOrderID(destItem.getWcsOrderId());
						destDebitDevAllocJournalTransaction.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank() : reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank());
						// Persist the DEBIT (deviation) journal transaction
						if (!em.contains(destDebitDevAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destDebitDevAllocJournalTransaction = finJournalTransactionDAO.save(destDebitDevAllocJournalTransaction);
						}
						transactionList
								.add(destDebitDevAllocJournalTransaction);
						break;
					case 1:
						FinJournalTransaction destCreditDevAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer creditDeviationMsgBuffer = new StringBuffer();
						creditDeviationMsgBuffer
								.append("Allocation CREDIT (deviation) from WCS Order:");
						creditDeviationMsgBuffer.append(wcsOrderId);
						creditDeviationMsgBuffer.append(" to WCS Order:");
						creditDeviationMsgBuffer.append(destItem.getWcsOrderId());
						destCreditDevAllocJournalTransaction.setComments(creditDeviationMsgBuffer.toString());

						destCreditDevAllocJournalTransaction.setCreditDebitFlag(true);
						destCreditDevAllocJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroups);

						destCreditDevAllocJournalTransaction.setFinAccount(finAccount2230001);

						destCreditDevAllocJournalTransaction.setFinArFundsInReconRecords(reconRecordList);
						destCreditDevAllocJournalTransaction.setFinJournal(finJournalAllocation);
						destCreditDevAllocJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						destCreditDevAllocJournalTransaction.setFinTransactionStatus(finTransactionStatus);

						destCreditDevAllocJournalTransaction.setFinTransactionType(finTransactionType);
						destCreditDevAllocJournalTransaction.setTransactionAmount(finArFundsInAllocatePayment
										.getAmount().subtract(destItem.getPaymentAmount()));
						destCreditDevAllocJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

						destCreditDevAllocJournalTransaction.setWcsOrderID(destItem.getWcsOrderId());
						destCreditDevAllocJournalTransaction.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank());
						// Persist the CREDIT (deviation) journal transaction
						if (!em.contains(destCreditDevAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destCreditDevAllocJournalTransaction = finJournalTransactionDAO.save(destCreditDevAllocJournalTransaction);
						}
						transactionList
								.add(destCreditDevAllocJournalTransaction);
						break;
					default: // do nothing
					}
					// end of 'payment not recognized' allocation journal
				} else {
					// other than 'payment not recognized' alloc journal
					switch (finArFundsInAllocatePayment.getAmount().compareTo(destItem.getPaymentAmount())) {
					case -1: // Partial Funds Received
						// DEBIT the account
						FinJournalTransaction originPartialAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer debitMsgBuffer = new StringBuffer();
						debitMsgBuffer.append("Allocation DEBIT from WCS Order:");
						debitMsgBuffer.append(wcsOrderId);
						debitMsgBuffer.append(" to WCS Order:");
						debitMsgBuffer.append(destItem.getWcsOrderId());
						originPartialAllocJournalTransaction.setComments(debitMsgBuffer.toString());
						originPartialAllocJournalTransaction.setCreditDebitFlag(false);
						originPartialAllocJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroups);
						originPartialAllocJournalTransaction.setFinAccount(finAccount2230001);
						originPartialAllocJournalTransaction.setFinArFundsInReconRecords(reconRecordList);
						originPartialAllocJournalTransaction.setFinJournal(finJournalAllocation);
						originPartialAllocJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());

						originPartialAllocJournalTransaction.setFinTransactionStatus(finTransactionStatus);

						originPartialAllocJournalTransaction.setFinTransactionType(finTransactionType);
						originPartialAllocJournalTransaction.setTransactionAmount(finArFundsInAllocatePayment.getAmount());
						originPartialAllocJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

						originPartialAllocJournalTransaction.setWcsOrderID(reconRecord.getWcsOrderId() != null ? reconRecord
										.getWcsOrderId() : null);
						originPartialAllocJournalTransaction.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport().getFinArFundsInReportType().getVenBank());
						// Persist the DEBIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(originPartialAllocJournalTransaction);*/
						if (!em.contains(originPartialAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocaitonJournal::calling finJournalTransactionDAO.save explicitly");
							originPartialAllocJournalTransaction = finJournalTransactionDAO.save(originPartialAllocJournalTransaction);
						}
						transactionList
								.add(originPartialAllocJournalTransaction);

						// CREDIT the account
						FinJournalTransaction destPartialAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer creditMsgBuffer = new StringBuffer();
						creditMsgBuffer
								.append("Allocation CREDIT from WCS Order:");
						creditMsgBuffer.append(wcsOrderId);
						creditMsgBuffer.append(" to WCS Order:");
						creditMsgBuffer.append(destItem.getWcsOrderId());
						destPartialAllocJournalTransaction
								.setComments(creditMsgBuffer.toString());
						destPartialAllocJournalTransaction
								.setCreditDebitFlag(true);
						destPartialAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);

						destPartialAllocJournalTransaction
								.setFinAccount(finAccount2230001);

						destPartialAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						destPartialAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						destPartialAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());
						destPartialAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						destPartialAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						destPartialAllocJournalTransaction
								.setTransactionAmount(destItem.getPaymentAmount());
						destPartialAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						destPartialAllocJournalTransaction
								.setWcsOrderID(destItem.getWcsOrderId());
						destPartialAllocJournalTransaction
								.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport()
												.getFinArFundsInReportType()
												.getVenBank());
						// Persist the CREDIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(destPartialAllocJournalTransaction);*/
						if (!em.contains(destPartialAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destPartialAllocJournalTransaction = finJournalTransactionDAO.save(destPartialAllocJournalTransaction);
						}
						transactionList.add(destPartialAllocJournalTransaction);

						// DEBIT (deviation) the account
						FinJournalTransaction destPartialDevAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer debitDevMsgBuffer = new StringBuffer();
						debitDevMsgBuffer
								.append("Allocation DEBIT (deviation) from WCS Order:");
						debitDevMsgBuffer.append(wcsOrderId);
						debitDevMsgBuffer.append(" to WCS Order:");
						debitDevMsgBuffer.append(destItem.getWcsOrderId());
						destPartialDevAllocJournalTransaction
								.setComments(debitDevMsgBuffer.toString());
						destPartialDevAllocJournalTransaction
								.setCreditDebitFlag(false);
						destPartialDevAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);
						destPartialDevAllocJournalTransaction
								.setFinAccount(finAccount2230001);
						destPartialDevAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						destPartialDevAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						destPartialDevAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());

						destPartialDevAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						destPartialDevAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						destPartialDevAllocJournalTransaction
								.setTransactionAmount(destItem.getPaymentAmount()
										.subtract(finArFundsInAllocatePayment.getAmount()));
						destPartialDevAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						destPartialDevAllocJournalTransaction
								.setWcsOrderID(destItem.getWcsOrderId() != null ? destItem.getWcsOrderId() : null);
						destPartialDevAllocJournalTransaction
								.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport()
												.getFinArFundsInReportType()
												.getVenBank());
						// Persist the DEBIT (deviation) journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(destPartialDevAllocJournalTransaction);*/
						if (!em.contains(destPartialDevAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destPartialDevAllocJournalTransaction = finJournalTransactionDAO.save(destPartialDevAllocJournalTransaction);
						}
						transactionList
								.add(destPartialDevAllocJournalTransaction);
						break;
					case 0: // All Funds Received
						// DEBIT the account
						FinJournalTransaction originAllAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer debitAllFundsMsgBuffer = new StringBuffer();
						debitAllFundsMsgBuffer
								.append("Allocation DEBIT from WCS Order:");
						debitAllFundsMsgBuffer.append(wcsOrderId);
						debitAllFundsMsgBuffer.append(" to WCS Order:");
						debitAllFundsMsgBuffer.append(destItem.getWcsOrderId());
						originAllAllocJournalTransaction
								.setComments(debitAllFundsMsgBuffer.toString());
						originAllAllocJournalTransaction
								.setCreditDebitFlag(false);
						originAllAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);
						originAllAllocJournalTransaction
								.setFinAccount(finAccount2230001);
						originAllAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						originAllAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						originAllAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());

						originAllAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						originAllAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						originAllAllocJournalTransaction
								.setTransactionAmount(finArFundsInAllocatePayment
										.getAmount());
						originAllAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						originAllAllocJournalTransaction
								.setWcsOrderID(reconRecord.getWcsOrderId() != null ? reconRecord
										.getWcsOrderId() : null);
						originAllAllocJournalTransaction.setVenBank(reconRecord
								.getVenOrderPayment() != null ? reconRecord
								.getVenOrderPayment().getVenBank()
								: reconRecord.getFinArFundsInReport()
										.getFinArFundsInReportType()
										.getVenBank());
						// Persist the DEBIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(originAllAllocJournalTransaction);*/
						if (!em.contains(originAllAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							originAllAllocJournalTransaction = finJournalTransactionDAO.save(originAllAllocJournalTransaction);
						}
						transactionList.add(originAllAllocJournalTransaction);

						// CREDIT the account
						FinJournalTransaction destAllAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer creditAllMsgBuffer = new StringBuffer();
						creditAllMsgBuffer
								.append("Allocation CREDIT from WCS Order:");
						creditAllMsgBuffer.append(wcsOrderId);
						creditAllMsgBuffer.append(" to WCS Order:");
						creditAllMsgBuffer.append(destItem.getWcsOrderId());
						destAllAllocJournalTransaction
								.setComments(creditAllMsgBuffer.toString());
						destAllAllocJournalTransaction.setCreditDebitFlag(true);
						destAllAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);

						destAllAllocJournalTransaction
								.setFinAccount(finAccount2230001);

						destAllAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						destAllAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						destAllAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());
						destAllAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						destAllAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						destAllAllocJournalTransaction
								.setTransactionAmount(destItem.getPaymentAmount());
						destAllAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						destAllAllocJournalTransaction.setWcsOrderID(destItem.getWcsOrderId());
						destAllAllocJournalTransaction.setVenBank(reconRecord
								.getVenOrderPayment() != null ? reconRecord
								.getVenOrderPayment().getVenBank()
								: reconRecord.getFinArFundsInReport()
										.getFinArFundsInReportType()
										.getVenBank());
						// Persist the CREDIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(destAllAllocJournalTransaction);*/
						if (!em.contains(destAllAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destAllAllocJournalTransaction = finJournalTransactionDAO.save(destAllAllocJournalTransaction);
						}
						transactionList.add(destAllAllocJournalTransaction);
						break;
					case 1: // Over paid
						// DEBIT the account
						FinJournalTransaction originOverAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer debitOverMsgBuffer = new StringBuffer();
						debitOverMsgBuffer
								.append("Allocation DEBIT from WCS Order:");
						debitOverMsgBuffer.append(wcsOrderId);
						debitOverMsgBuffer.append(" to WCS Order:");
						debitOverMsgBuffer.append(destItem.getWcsOrderId());
						originOverAllocJournalTransaction
								.setComments(debitOverMsgBuffer.toString());
						originOverAllocJournalTransaction
								.setCreditDebitFlag(false);
						originOverAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);
						originOverAllocJournalTransaction
								.setFinAccount(finAccount2230001);
						originOverAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						originOverAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						originOverAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());

						originOverAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						originOverAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						originOverAllocJournalTransaction
								.setTransactionAmount(finArFundsInAllocatePayment
										.getAmount());
						originOverAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						originOverAllocJournalTransaction
								.setWcsOrderID(reconRecord.getWcsOrderId() != null ? reconRecord
										.getWcsOrderId() : null);
						originOverAllocJournalTransaction
								.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport()
												.getFinArFundsInReportType()
												.getVenBank());
						// Persist the DEBIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(originOverAllocJournalTransaction);*/
						if (!em.contains(originOverAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							originOverAllocJournalTransaction = finJournalTransactionDAO.save(originOverAllocJournalTransaction);
						}
						transactionList.add(originOverAllocJournalTransaction);

						// CREDIT the account
						FinJournalTransaction destOverAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer creditOverMsgBuffer = new StringBuffer();
						creditOverMsgBuffer
								.append("Allocation CREDIT from WCS Order:");
						creditOverMsgBuffer.append(wcsOrderId);
						creditOverMsgBuffer.append(" to WCS Order:");
						creditOverMsgBuffer.append(destItem.getWcsOrderId());
						destOverAllocJournalTransaction
								.setComments(creditOverMsgBuffer.toString());
						destOverAllocJournalTransaction
								.setCreditDebitFlag(true);
						destOverAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);

						destOverAllocJournalTransaction
								.setFinAccount(finAccount2230001);

						destOverAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						destOverAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						destOverAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());
						destOverAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						destOverAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						destOverAllocJournalTransaction
								.setTransactionAmount(destItem.getPaymentAmount());
						destOverAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						destOverAllocJournalTransaction.setWcsOrderID(destItem.getWcsOrderId());
						destOverAllocJournalTransaction.setVenBank(reconRecord
								.getVenOrderPayment() != null ? reconRecord
								.getVenOrderPayment().getVenBank()
								: reconRecord.getFinArFundsInReport()
										.getFinArFundsInReportType()
										.getVenBank());
						// Persist the CREDIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(destOverAllocJournalTransaction);*/
						if (!em.contains(destOverAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destOverAllocJournalTransaction = finJournalTransactionDAO.save(destOverAllocJournalTransaction);
						}
						transactionList.add(destOverAllocJournalTransaction);

						// CREDIT (deviation) the account
						FinJournalTransaction destOverDevAllocJournalTransaction = new FinJournalTransaction();
						StringBuffer creditOverDevMsgBuffer = new StringBuffer();
						creditOverDevMsgBuffer
								.append("Allocation CREDIT (deviation) from WCS Order:");
						creditOverDevMsgBuffer.append(wcsOrderId);
						creditOverDevMsgBuffer.append(" to WCS Order:");
						creditOverDevMsgBuffer.append(destItem.getWcsOrderId());
						destOverDevAllocJournalTransaction
								.setComments(creditOverDevMsgBuffer.toString());
						destOverDevAllocJournalTransaction
								.setCreditDebitFlag(true);
						destOverDevAllocJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroups);
						destOverDevAllocJournalTransaction
								.setFinAccount(finAccount2230001);
						destOverDevAllocJournalTransaction
								.setFinArFundsInReconRecords(reconRecordList);
						destOverDevAllocJournalTransaction
								.setFinJournal(finJournalAllocation);
						destOverDevAllocJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());

						destOverDevAllocJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);

						destOverDevAllocJournalTransaction
								.setFinTransactionType(finTransactionType);
						destOverDevAllocJournalTransaction
								.setTransactionAmount(finArFundsInAllocatePayment
										.getAmount().subtract(destItem.getPaymentAmount()));
						destOverDevAllocJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));

						destOverDevAllocJournalTransaction
								.setWcsOrderID(destItem.getWcsOrderId() != null ? destItem.getWcsOrderId() : null);
						destOverDevAllocJournalTransaction
								.setVenBank(reconRecord.getVenOrderPayment() != null ? reconRecord
										.getVenOrderPayment().getVenBank()
										: reconRecord.getFinArFundsInReport()
												.getFinArFundsInReportType()
												.getVenBank());
						// Persist the DEBIT journal transaction
						/*journalTransactionHome
								.persistFinJournalTransaction(destOverDevAllocJournalTransaction);*/
						if (!em.contains(destOverDevAllocJournalTransaction)) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "postAllocationJournal::calling finJournalTransactionDAO.save explicitly");
							destOverDevAllocJournalTransaction = finJournalTransactionDAO.save(destOverDevAllocJournalTransaction);
						}
						transactionList.add(destOverDevAllocJournalTransaction);
						break;
					}
				} // end of other then 'Payment Not Recognized' alloc journal

				finArFundsInAllocatePayment.setIsactive(false);
				//finArFundsInAllocateHome.mergeFinArFundsInAllocatePayment(finArFundsInAllocatePayment);
				if (!em.contains(finArFundsInAllocatePayment)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::calling finArFundsInAllocatePaymentDAO.save explicitly.");
					finArFundsInAllocatePayment = finArFundsInAllocatePaymentDAO.save(finArFundsInAllocatePayment);
				}
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting allocation journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournal::" + " completed in " + duration
				+ "ms");
		return true;
	} // end of postAllocationJournal

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #allocatePaymentAndpostAllocationJournalTransaction(Long
	 * sourceVenOrderPaymentId, Long fundsInReconRecordId, Long
	 * destinationVenOrderPaymentId, Double allocationAmount, Long
	 * destinationVenOrderId)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean allocatePaymentAndpostAllocationJournalTransaction(
			Long sourceVenWCSOrderId, Long sourceVenOrderPaymentId,
			Long fundsInReconRecordId, Long destinationVenOrderPaymentId,
			Double allocationAmount, Long destinationVenOrderId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		try {
			/*
			FinArFundsInReconRecordSessionEJBLocal fundsInReconRecordHome = (FinArFundsInReconRecordSessionEJBLocal) this._genericLocator
					.lookupLocal(FinArFundsInReconRecordSessionEJBLocal.class,
							"FinArFundsInReconRecordSessionEJBBeanLocal");
			*/

			/*
			VenOrderPaymentSessionEJBLocal orderPaymentHome = (VenOrderPaymentSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderPaymentSessionEJBLocal.class,
							"VenOrderPaymentSessionEJBBeanLocal");

			VenOrderSessionEJBLocal orderHome = (VenOrderSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderSessionEJBLocal.class,
							"VenOrderSessionEJBBeanLocal");
			*/
			/*
			FinArFundsInAllocatePaymentSessionEJBLocal finArFundsInAllocateHome = (FinArFundsInAllocatePaymentSessionEJBLocal) this._genericLocator
					.lookupLocal(
							FinArFundsInAllocatePaymentSessionEJBLocal.class,
							"FinArFundsInAllocatePaymentSessionEJBBeanLocal");
            */

			if (sourceVenOrderPaymentId != null && fundsInReconRecordId != null) {
				throw new EJBException(
						"Both sourceVenOrderPaymentId and fundsInReconRecordId are not null. These parameters are mutally exclusive!");
			}

			if (sourceVenOrderPaymentId == null && fundsInReconRecordId == null) {
				throw new EJBException(
						"Both sourceVenOrderPaymentId and fundsInReconRecordId are null. At least one of these parameters is required!");
			}

			if (allocationAmount == null) {
				throw new EJBException(
						"Allocation Amount is Null! Please contact your admin");
			}

			/*
			List<VenOrder> destinationOrderList = orderHome.queryByRange(
					"select o from VenOrder o where o.orderId = "
							+ destinationVenOrderId, 0, 0);
			if (destinationOrderList.isEmpty()) {
				throw new EJBException(
						"The identifier provided for the destination order is not valid - no order exists!");
			}
			*/
		    VenOrder destinationOrder = venOrderDAO.findByOrderId(destinationVenOrderId);
		    if (destinationOrder == null) {
				throw new EJBException(
						"The identifier provided for the destination order is not valid - no order exists!");		    	
		    }

			FinArFundsInReconRecord destinationVenOrderPayment = null;

			FinArFundsInReconRecord payment = null;
			List<FinArFundsInReconRecord> reconRecordList = null;
			if (sourceVenOrderPaymentId != null) {
				/*
				 * Read all the relevant funds in records from the database and
				 * determine how much has been paid as well as the remaining
				 * balance according to the reconciliation.
				 */
				/*
				reconRecordList = fundsInReconRecordHome
						.queryByRange(
								"select o from FinArFundsInReconRecord o where o.venOrderPayment.orderPaymentId = "
										+ sourceVenOrderPaymentId
										+ " and o.wcsOrderId ='"
										+ sourceVenWCSOrderId.toString().trim()
										+ "' and o.finArFundsInActionApplied.actionAppliedId<>"
										+ VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED,
								0, 0);
				*/
				reconRecordList = finArFundsInReconRecordDAO.findByOrderPaymentAndWcsOrderAndActionAppliedNotRemoved(
						sourceVenOrderPaymentId, sourceVenWCSOrderId.toString().trim());
				if (reconRecordList.isEmpty()) {
					throw new EJBException(
							"No payments have been reconciled for payment id:"
									+ sourceVenOrderPaymentId
									+ " And OrderId = " + sourceVenWCSOrderId);
				}
				payment = reconRecordList.get(0);
			} else if (fundsInReconRecordId != null) {
				/*
				 * This case is for where a funds in with no corresponding
				 * VenOrderPayment is allocated to an existing order
				 */
				/*
				List<FinArFundsInReconRecord> paymentList = fundsInReconRecordHome
						.queryByRange(
								"select o from FinArFundsInReconRecord o where o.reconciliationRecordId = "
										+ fundsInReconRecordId, 0, 0);
				*/
				List<FinArFundsInReconRecord> paymentList = finArFundsInReconRecordDAO.findByReconciliationRecordId(fundsInReconRecordId);
				if (paymentList.isEmpty()) {
					throw new EJBException(
							"The funds in reconciliation record ID provided is invalid. Funds in record does not exist!");
				}
				payment = paymentList.get(0);

				/*
				 * Get the destination order payment and check it is CC type If
				 * so then update the payment confirmation number to the new
				 * payment confirmation number.
				 */
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Start Allocation");
				List<FinArFundsInReconRecord> funReconRecordList = null;
				/*
				funReconRecordList = fundsInReconRecordHome
						.queryByRange(
								"select o from FinArFundsInReconRecord o where o.venOrderPayment.orderPaymentId="
										+ destinationVenOrderPaymentId
										+ " and o.wcsOrderId = (select o.wcsOrderId from VenOrder b where b.orderId ="
										+ destinationVenOrderId
										+ ") and o.finArFundsInActionApplied.actionAppliedId<>"
										+ VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED,
								0, 0);
				*/
				funReconRecordList = finArFundsInReconRecordDAO.findByDestOrderPaymentAndDestWcsOrderAndActionAppliedNotRemoved(
						destinationVenOrderPaymentId, destinationVenOrderId);
				if (funReconRecordList.isEmpty()) {
					throw new EJBException(
							"The destination order payment ID provided is invalid. Order payment does not exist!");
				}

				/*
				List<FinArFundsInAllocatePayment> allocateAmount = finArFundsInAllocateHome
						.queryByRange(
								"select o from FinArFundsInAllocatePayment o where o.idReconRecordDest="
										+ payment.getReconciliationRecordId()
										+ " and o.isactive=true", 0, 0);
				*/
				List<FinArFundsInAllocatePayment> allocateAmount = finArFundsInAllocatePaymentDAO.findByIdReconRecordDestinationIsActive(
						payment.getReconciliationRecordId());
				if (!allocateAmount.isEmpty()) {
					throw new EJBException(
							"Payment Dari Order : "
									+ payment.getWcsOrderId()
									+ " menerima dana dari Reff :"
									+ payment.getNomorReff()
									+ " yang belum di approved! Approved terlebih dahulu Reff :"
									+ payment.getNomorReff());
				}

				destinationVenOrderPayment = funReconRecordList.get(0);

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::from Id Record Recon = " + fundsInReconRecordId);
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::to Destination paymentID  = "
						+ destinationVenOrderPayment.getVenOrderPayment()
								.getOrderPaymentId());

				/*
				List<FinArFundsInAllocatePayment> itemAllocate = finArFundsInAllocateHome
						.queryByRange(
								"select o from FinArFundsInAllocatePayment o where o.idReconRecordDest="
										+ destinationVenOrderPayment
												.getReconciliationRecordId(),
								0, 0);
			    */
				List<FinArFundsInAllocatePayment> itemAllocate = finArFundsInAllocatePaymentDAO
						.findByIdReconRecordDest(destinationVenOrderPayment.getReconciliationRecordId());
				FinArFundsInReconRecord newRecord = new FinArFundsInReconRecord();

				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());

				if (itemAllocate.isEmpty()) {
					newRecord
							.setWcsOrderId(destinationVenOrderPayment
									.getWcsOrderId() != null ? destinationVenOrderPayment
									.getWcsOrderId() : "");
					newRecord.setOrderDate(destinationVenOrderPayment
							.getOrderDate());
					newRecord.setNomorReff(payment.getNomorReff());
					newRecord.setReconcilliationRecordTimestamp(new Timestamp(
							System.currentTimeMillis()));
					
					if(payment.getCardNumber()!=null){
						String cardNumber = payment.getCardNumber();
						VenOrderPayment venOrderPayment = payment.getVenOrderPayment();
						venOrderPayment.setCardNumber(cardNumber);
						destinationVenOrderPayment.setVenOrderPayment(venOrderPayment);
					}
					
					newRecord.setVenOrderPayment(destinationVenOrderPayment
							.getVenOrderPayment());
					newRecord.setFinArFundsInReport(payment
							.getFinArFundsInReport());
					newRecord.setProviderReportPaymentId(payment
							.getProviderReportPaymentId());
					newRecord.setProviderReportPaymentDate(payment
							.getProviderReportPaymentDate());
					newRecord.setProviderReportFeeAmount(new BigDecimal(0));
					newRecord.setPaymentAmount(destinationVenOrderPayment
							.getPaymentAmount());
					newRecord.setPaymentConfirmationNumber(payment
							.getPaymentConfirmationNumber() != null ? payment
							.getPaymentConfirmationNumber() : null);
					BigDecimal paidAmount = (destinationVenOrderPayment
							.getProviderReportPaidAmount() != null ? destinationVenOrderPayment
							.getProviderReportPaidAmount() : new BigDecimal(0))
							.add(new BigDecimal(allocationAmount));

					newRecord.setProviderReportPaidAmount(paidAmount);
					newRecord
							.setRemainingBalanceAmount(destinationVenOrderPayment
									.getPaymentAmount().subtract(paidAmount));
					
					

					if (payment.getUniquePayment() != null) {
						newRecord.setUniquePayment(payment.getUniquePayment());
					}
					if (payment.getFinArFundsIdReportTime() != null) {
						newRecord.setFinArFundsIdReportTime(payment
								.getFinArFundsIdReportTime());
					}

					newRecord.setUserLogonName("System");
					newRecord.setFinApprovalStatus(finApprovalStatus);

					FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
					finArFundsInActionApplied
							.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id());
					newRecord
							.setFinArFundsInActionApplied(finArFundsInActionApplied);

					FinArReconResult finArReconResult = new FinArReconResult();

					BigDecimal remainingBalanceAmoun = newRecord
							.getRemainingBalanceAmount().abs()
							.compareTo(VeniceConstants.TRACEHOLD_RECEIVED) <= 0 ? new BigDecimal(
							0) : newRecord.getRemainingBalanceAmount();

					if (remainingBalanceAmoun.compareTo(new BigDecimal(0)) == 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id());
					} else if (remainingBalanceAmoun
							.compareTo(new BigDecimal(0)) > 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_PARTIAL.id());
					} else {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_OVERPAID.id());
					}
					newRecord.setFinArReconResult(finArReconResult);
					//newRecord = fundsInReconRecordHome.persistFinArFundsInReconRecord(newRecord);
					if (!em.contains(newRecord)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::newRecord is not in attach mode");
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::calling finArFundsInReconRecordDAO.save explicitly.");
						newRecord = finArFundsInReconRecordDAO.save(newRecord);
					}
					

					FinArFundsInActionApplied finArFundsInActionAppliedss = new FinArFundsInActionApplied();
					finArFundsInActionAppliedss
							.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED.id());
					destinationVenOrderPayment
							.setFinArFundsInActionApplied(finArFundsInActionAppliedss);

					//destinationVenOrderPayment = fundsInReconRecordHome.mergeFinArFundsInReconRecord(destinationVenOrderPayment);
					if (!em.contains(destinationVenOrderPayment)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::destinationVenOrderPayment is not in attach mode");
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::calling finArFundsInReconRecordDAO.save explicitly");
						destinationVenOrderPayment = finArFundsInReconRecordDAO.save(destinationVenOrderPayment);
					}
				} else {
					newRecord = destinationVenOrderPayment;

					BigDecimal paidAmount = (destinationVenOrderPayment
							.getProviderReportPaidAmount() != null ? destinationVenOrderPayment
							.getProviderReportPaidAmount() : new BigDecimal(0))
							.add(new BigDecimal(allocationAmount));

					newRecord.setProviderReportPaidAmount(paidAmount);
					newRecord
							.setRemainingBalanceAmount(destinationVenOrderPayment
									.getPaymentAmount().subtract(paidAmount));
					newRecord.setReconcilliationRecordTimestamp(new Timestamp(
							System.currentTimeMillis()));

					newRecord.setFinApprovalStatus(finApprovalStatus);
					FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
					finArFundsInActionApplied
							.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id());
					newRecord
							.setFinArFundsInActionApplied(finArFundsInActionApplied);

					FinArReconResult finArReconResult = new FinArReconResult();

					newRecord.setFinArFundsInReport(payment
							.getFinArFundsInReport());
					newRecord.setProviderReportPaymentId(payment
							.getProviderReportPaymentId());
					newRecord.setProviderReportPaymentDate(payment
							.getProviderReportPaymentDate());
					newRecord.setPaymentConfirmationNumber(payment
							.getPaymentConfirmationNumber() != null ? payment
							.getPaymentConfirmationNumber() : null);
					newRecord.setNomorReff(payment.getNomorReff());
					newRecord
							.setUniquePayment(payment.getUniquePayment() != null ? payment
									.getUniquePayment() : null);
					newRecord.setFinArFundsIdReportTime(payment
							.getFinArFundsIdReportTime() != null ? payment
							.getFinArFundsIdReportTime() : null);

					BigDecimal remainingBalanceAmoun = newRecord
							.getRemainingBalanceAmount().abs()
							.compareTo(VeniceConstants.TRACEHOLD_RECEIVED) <= 0 ? new BigDecimal(
							0) : newRecord.getRemainingBalanceAmount();

							newRecord.setCardNumber(payment.getCardNumber()!=null?payment.getCardNumber():null);
							
					if (remainingBalanceAmoun.compareTo(new BigDecimal(0)) == 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id());
					} else if (remainingBalanceAmoun
							.compareTo(new BigDecimal(0)) > 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_PARTIAL.id());
					} else {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_OVERPAID.id());
					}
					newRecord.setFinArReconResult(finArReconResult);
					//newRecord = fundsInReconRecordHome.mergeFinArFundsInReconRecord(newRecord);
					if (!em.contains(newRecord)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::newRecord is not in attach mode");
						newRecord = finArFundsInReconRecordDAO.save(newRecord);
					}
				}

				FinArFundsInAllocatePayment finArFundsInAllocates = new FinArFundsInAllocatePayment();
				finArFundsInAllocates
						.setIdReconRecordSource(fundsInReconRecordId);
				finArFundsInAllocates.setIdReconRecordDest(newRecord
						.getReconciliationRecordId());
				finArFundsInAllocates
						.setAmount(new BigDecimal(allocationAmount));
				finArFundsInAllocates.setPaidamount(payment
						.getProviderReportPaidAmount());
				finArFundsInAllocates.setBankfee(payment
						.getProviderReportFeeAmount() != null ? payment
						.getProviderReportFeeAmount() : new BigDecimal(0));
				finArFundsInAllocates.setIsactive(true);
				finArFundsInAllocates.setAllocationTimestamp(new Timestamp(
						System.currentTimeMillis()));
				//finArFundsInAllocates = finArFundsInAllocateHome.persistFinArFundsInAllocatePayment(finArFundsInAllocates);
				if (!em.contains(finArFundsInAllocates)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::finArFundsInAllocates is not in attach mode");
					finArFundsInAllocates = finArFundsInAllocatePaymentDAO.save(finArFundsInAllocates);
				}

				/*
				 * Now we need to update the funds-in record with the new order
				 * id, payment id and confirmation number and merge both the
				 * destination VenOrderPayment and the reconciliation record
				 */
				FinArFundsInActionApplied finArFundsInActionApplieds = new FinArFundsInActionApplied();
				//finArFundsInActionApplieds.setActionAppliedId(VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED);
				finArFundsInActionApplieds.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id());
				payment.setFinArFundsInActionApplied(finArFundsInActionApplieds);

				payment.setProviderReportPaidAmount(payment
						.getProviderReportPaidAmount().subtract(
								new BigDecimal(allocationAmount)));
				payment.setRemainingBalanceAmount(payment
						.getRemainingBalanceAmount().add(
								new BigDecimal(allocationAmount)));
				payment.setFinApprovalStatus(finApprovalStatus);

				FinArReconResult finArReconResults = new FinArReconResult();
				finArReconResults
						.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id());
				payment.setFinArReconResult(finArReconResults);

				//fundsInReconRecordHome.mergeFinArFundsInReconRecord(payment);
				if (!em.contains(payment)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postAllocationJournalTransaction::payment is not in attach mode, hence calling save explicitly.");
					payment = finArFundsInReconRecordDAO.save(payment);
				}

				return true;
			}

			/*
			 * This is the case where an allocation is being made from a u=funds
			 * in record with a correlated VenOrderPayment so we need to check
			 * the balance and find the order to which the payment is attached
			 * etc.
			 */

			/*
			 * Cek Order Dibatalkan atau tidak
			 */

			// List<VenOrder> OrderList =
			// orderHome.queryByRange("select o from VenOrder o where o.wcsOrderId = '"
			// + payment.getWcsOrderId()+
			// "' or o.orderId = "+destinationVenOrderId, 0, 0);
			// if (OrderList!=null && !OrderList.isEmpty()){
			// /*for (VenOrder allocation : OrderList)
			// if (allocation.getVenOrderStatus().getOrderStatusId() ==
			// VeniceConstants.VEN_ORDER_STATUS_X) {
			// throw new EJBException("Selected order is canceled");
			//
			// }*/
			// }else{
			// throw new EJBException("Selected order Is not found");
			// }
			//
			BigDecimal remainingUnpaidOrderPaidAmount = payment
					.getProviderReportPaidAmount() != null ? payment
					.getProviderReportPaidAmount() : new BigDecimal(0);

			// jika Paid Amount nya null maka tidak bisa diallocate
			if (remainingUnpaidOrderPaidAmount.compareTo(new BigDecimal(0)) <= 0) {
				throw new EJBException(
						"Selected order payment has a remaining paid Amount for the order and cannot be allocated");
			}

			/*
			List<VenOrderPayment> venOrderPaymentList = orderPaymentHome
					.queryByRange(
							"select o from VenOrderPayment o where o.orderPaymentId = "
									+ sourceVenOrderPaymentId, 0, 0);
			*/
			VenOrderPayment venOrderPayment = venOrderPaymentDAO.findByOrderPaymentId(sourceVenOrderPaymentId);
			//if (venOrderPaymentList.isEmpty()) {
			if (venOrderPayment == null) {
				throw new EJBException(
						"The identifier provided for the payment is not valid - no payment exists!");
			}
			List<FinArFundsInReconRecord> funReconRecordList = null;
			FinArFundsInReconRecord tempRecord = new FinArFundsInReconRecord();
			FinArFundsInReconRecord newRecord = null;
			/*
			funReconRecordList = fundsInReconRecordHome
					.queryByRange(
							"select o from FinArFundsInReconRecord o where o.venOrderPayment.orderPaymentId="
									+ destinationVenOrderPaymentId
									+ " and o.wcsOrderId = (select o.wcsOrderId from VenOrder b where b.orderId ="
									+ destinationVenOrderId
									+ ") and o.finArFundsInActionApplied.actionAppliedId<>"
									+ VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED,
							0, 0);
			*/
			funReconRecordList = finArFundsInReconRecordDAO.findByDestOrderPaymentAndDestWcsOrderAndActionAppliedNotRemoved(
					destinationVenOrderPaymentId, destinationVenOrderId);
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Start Allocation");
			if (!funReconRecordList.isEmpty()) {
				tempRecord = funReconRecordList.get(0);
				/*
				List<FinArFundsInAllocatePayment> itemAllocate = finArFundsInAllocateHome
						.queryByRange(
								"select o from FinArFundsInAllocatePayment o where  o.idReconRecordDest="
										+ funReconRecordList.get(0)
												.getReconciliationRecordId(),
								0, 0);
				*/
				List<FinArFundsInAllocatePayment> itemAllocate = finArFundsInAllocatePaymentDAO.findByIdReconRecordDest(
						funReconRecordList.get(0).getReconciliationRecordId());
				/*
				List<FinArFundsInAllocatePayment> itemAllocateSrc = finArFundsInAllocateHome
						.queryByRange(
								"select o from FinArFundsInAllocatePayment o where  o.idReconRecordSource="
										+ funReconRecordList.get(0)
												.getReconciliationRecordId(),
								0, 0);
				*/
				List<FinArFundsInAllocatePayment> itemAllocateSrc = finArFundsInAllocatePaymentDAO.findByIdReconRecordSource(
						funReconRecordList.get(0).getReconciliationRecordId());

				if (itemAllocate.isEmpty() && itemAllocateSrc.isEmpty()) {

					FinArFundsInActionApplied tempFinArFundsInActionApplied = new FinArFundsInActionApplied();
					//tempFinArFundsInActionApplied.setActionAppliedId(VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED);
					tempFinArFundsInActionApplied.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED.id());
					tempRecord.setFinArFundsInActionApplied(tempFinArFundsInActionApplied);
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::cancel Old destination Payment ");
					//tempRecord = fundsInReconRecordHome.mergeFinArFundsInReconRecord(tempRecord);
					if (!em.contains(tempRecord)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::tempRecord is not in attached, hence call save explicitly.");
						tempRecord = finArFundsInReconRecordDAO.save(tempRecord);
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Update  Old destination Payment");

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Start Create Payment Allocation");
					newRecord = new FinArFundsInReconRecord();
					newRecord
							.setWcsOrderId(tempRecord.getWcsOrderId() != null ? tempRecord
									.getWcsOrderId() : "");
					newRecord.setOrderDate(tempRecord.getOrderDate());
					newRecord.setNomorReff(sourceVenWCSOrderId.toString());
					Date d = new Date();
					newRecord.setReconcilliationRecordTimestamp(new Timestamp(d
							.getTime()));
					
					if(payment.getCardNumber()!=null)
					{
						String cardNumber = payment.getCardNumber();
						VenOrderPayment venOrderPaymentDest = payment.getVenOrderPayment();
						venOrderPaymentDest.setCardNumber(cardNumber);
						newRecord.setVenOrderPayment(venOrderPaymentDest);
					}
					
					newRecord.setVenOrderPayment(tempRecord
							.getVenOrderPayment());
					newRecord.setFinArFundsInReport(payment
							.getFinArFundsInReport());
					newRecord.setProviderReportPaymentId(payment
							.getProviderReportPaymentId());
					newRecord.setProviderReportPaymentDate(payment
							.getProviderReportPaymentDate());
					newRecord.setPaymentAmount(tempRecord.getPaymentAmount());
					newRecord.setPaymentConfirmationNumber(payment
							.getPaymentConfirmationNumber() != null ? payment
							.getPaymentConfirmationNumber() : null);
					BigDecimal paidAmount = (tempRecord
							.getProviderReportPaidAmount() != null ? tempRecord
							.getProviderReportPaidAmount() : new BigDecimal(0))
							.add(new BigDecimal(allocationAmount));
					newRecord.setProviderReportPaidAmount(paidAmount);
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::set Paid Amount record =  "
							+ (tempRecord.getProviderReportPaidAmount() != null ? tempRecord
							.getProviderReportPaidAmount() : new BigDecimal(0)));
					newRecord.setRemainingBalanceAmount(tempRecord
							.getPaymentAmount().subtract(paidAmount));
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::set Paid Amountt =  " + paidAmount);
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::set Remaining Balance Amount =  "
							+ newRecord.getRemainingBalanceAmount());
					newRecord.setUserLogonName("System");

					if (payment.getUniquePayment() != null) {
						newRecord.setUniquePayment(payment.getUniquePayment());
					}
					if (payment.getFinArFundsIdReportTime() != null) {
						newRecord.setFinArFundsIdReportTime(payment
								.getFinArFundsIdReportTime());
					}

					FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
					finApprovalStatus
							.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
					newRecord.setFinApprovalStatus(finApprovalStatus);

					FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
					finArFundsInActionApplied
							.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id());
					newRecord
							.setFinArFundsInActionApplied(finArFundsInActionApplied);

					FinArReconResult finArReconResult = new FinArReconResult();

					BigDecimal remainingBalanceAmoun = newRecord
							.getRemainingBalanceAmount().abs()
							.compareTo(VeniceConstants.TRACEHOLD_RECEIVED) <= 0 ? new BigDecimal(
							0) : newRecord.getRemainingBalanceAmount();
							
					tempRecord.setCardNumber(payment.getCardNumber()!=null?payment.getCardNumber():null);

					if (remainingBalanceAmoun.compareTo(new BigDecimal(0)) == 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id());
					} else if (remainingBalanceAmoun
							.compareTo(new BigDecimal(0)) > 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_PARTIAL.id());
					} else {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_OVERPAID.id());
					}

					newRecord.setFinArReconResult(finArReconResult);

					//newRecord = fundsInReconRecordHome.persistFinArFundsInReconRecord(newRecord);
					if (!em.contains(newRecord)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::newRecord is detached, calling save explicitly.");
						newRecord = finArFundsInReconRecordDAO.save(newRecord);
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Allocate and  reconciliation record persisted id:"
							+ newRecord.getReconciliationRecordId());
				} else {
					BigDecimal paidAmount = (tempRecord
							.getProviderReportPaidAmount() != null ? tempRecord
							.getProviderReportPaidAmount() : new BigDecimal(0))
							.add(new BigDecimal(allocationAmount));
					tempRecord.setProviderReportPaidAmount(paidAmount);
					tempRecord.setRemainingBalanceAmount(tempRecord
							.getPaymentAmount().subtract(paidAmount));
					tempRecord.setReconcilliationRecordTimestamp(new Timestamp(
							System.currentTimeMillis()));

					FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
					finApprovalStatus
							.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
					tempRecord.setFinApprovalStatus(finApprovalStatus);

					tempRecord.setFinArFundsInReport(payment
							.getFinArFundsInReport());
					tempRecord.setProviderReportPaymentId(payment
							.getProviderReportPaymentId());
					tempRecord.setProviderReportPaymentDate(payment
							.getProviderReportPaymentDate());
					tempRecord.setPaymentConfirmationNumber(payment
							.getPaymentConfirmationNumber() != null ? payment
							.getPaymentConfirmationNumber() : null);
					tempRecord
							.setNomorReff(payment.getWcsOrderId() != null ? payment
									.getWcsOrderId() : payment.getNomorReff());
					tempRecord
							.setUniquePayment(payment.getUniquePayment() != null ? payment
									.getUniquePayment() : null);
					tempRecord.setFinArFundsIdReportTime(payment
							.getFinArFundsIdReportTime() != null ? payment
							.getFinArFundsIdReportTime() : null);

					if (itemAllocateSrc.isEmpty()) {
						FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
						finArFundsInActionApplied
								.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id());
						tempRecord
								.setFinArFundsInActionApplied(finArFundsInActionApplied);
					}

					FinArReconResult finArReconResult = new FinArReconResult();

					BigDecimal remainingBalanceAmoun = tempRecord
							.getRemainingBalanceAmount().abs()
							.compareTo(VeniceConstants.TRACEHOLD_RECEIVED) <= 0 ? new BigDecimal(
							0) : tempRecord.getRemainingBalanceAmount();

					if (remainingBalanceAmoun.compareTo(new BigDecimal(0)) == 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_ALL.id());
					} else if (remainingBalanceAmoun
							.compareTo(new BigDecimal(0)) > 0) {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_PARTIAL.id());
					} else {
						finArReconResult
								.setReconResultId(FinArReconResultConstants.FIN_AR_RECON_RESULT_OVERPAID.id());
					}

					tempRecord.setFinArReconResult(finArReconResult);

					//tempRecord = fundsInReconRecordHome.mergeFinArFundsInReconRecord(tempRecord);
					if (!em.contains(tempRecord)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::tempRecord is detached, calling save explicitly.");
						tempRecord = finArFundsInReconRecordDAO.save(tempRecord);
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Allocate and  reconciliation record merge id:"
							+ tempRecord.getReconciliationRecordId());

				}
			} else {
				throw new EJBException(
						"Fund In reconciliation Is not found! Please contact your admin");
			}

			FinArFundsInAllocatePayment finArFundsInAllocate = new FinArFundsInAllocatePayment();
			finArFundsInAllocate.setIdReconRecordSource(payment
					.getReconciliationRecordId());
			finArFundsInAllocate
					.setIdReconRecordDest(newRecord != null ? newRecord
							.getReconciliationRecordId() : tempRecord
							.getReconciliationRecordId());
			finArFundsInAllocate.setAmount(new BigDecimal(allocationAmount));
			finArFundsInAllocate.setPaidamount(payment.getProviderReportPaidAmount());
			finArFundsInAllocate.setBankfee(payment.getProviderReportFeeAmount());
			finArFundsInAllocate.setIsactive(true);
			finArFundsInAllocate.setAllocationTimestamp(new Timestamp(System.currentTimeMillis()));
			//finArFundsInAllocate = finArFundsInAllocateHome.persistFinArFundsInAllocatePayment(finArFundsInAllocate);
			if (!em.contains(finArFundsInAllocate)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::finArFundsInAllocate is in detached, calling save explicitly.");
				finArFundsInAllocate = finArFundsInAllocatePaymentDAO.save(finArFundsInAllocate);
			}

			BigDecimal oldRemainingBalanceAmount = payment
					.getProviderReportPaidAmount().subtract(
							new BigDecimal(allocationAmount));

			BigDecimal oldRemainingBalance = oldRemainingBalanceAmount.abs()
					.compareTo(VeniceConstants.TRACEHOLD_RECEIVED) <= 0 ? new BigDecimal(
					0) : oldRemainingBalanceAmount;

			if (oldRemainingBalance.compareTo(new BigDecimal(0)) == 0) {
				payment.setRemainingBalanceAmount(new BigDecimal(0));
				payment.setProviderReportPaidAmount(new BigDecimal(0));
			} else if (oldRemainingBalance.compareTo(new BigDecimal(0)) > 0) {
				payment.setProviderReportPaidAmount(oldRemainingBalanceAmount);
				payment.setRemainingBalanceAmount(payment.getPaymentAmount()
						.subtract(oldRemainingBalanceAmount));

			} else {
				throw new EJBException(
						"Allocation Amount more than Paid Amount! Please check against Allocation Amount");
			}
			FinArFundsInActionApplied newFinArFundsInActionApplied = new FinArFundsInActionApplied();
			newFinArFundsInActionApplied
					.setActionAppliedId(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED.id());
			payment.setFinArFundsInActionApplied(newFinArFundsInActionApplied);

			FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
			finApprovalStatus
					.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
			payment.setFinApprovalStatus(finApprovalStatus);

			//fundsInReconRecordHome.mergeFinArFundsInReconRecord(payment);
			if (!em.contains(payment)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postAllocationJournalTransaction::payment is not attached, calling save explicitly.");
				payment = finArFundsInReconRecordDAO.save(payment);
			}
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::Update Fund In Source  ");

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting allocation journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postAllocationJournalTransaction::" + " completed in "
				+ duration + "ms");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postRefundJournalTransaction(ArrayList<Long> finSalesRecordId, Boolean
	 * pkp)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postRefundJournalTransaction(Long finSalesRecordId,
			Boolean pkp) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();
		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {
			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");
			*/

			/*
			FinSalesRecordSessionEJBLocal salesRecordHome = (FinSalesRecordSessionEJBLocal) this._genericLocator
					.lookupLocal(FinSalesRecordSessionEJBLocal.class,
							"FinSalesRecordSessionEJBBeanLocal");

			VenOrderItemSessionEJBLocal orderItemHome = (VenOrderItemSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderItemSessionEJBLocal.class,
							"VenOrderItemSessionEJBBeanLocal");
			*/

			FinPeriod finPeriod = FinancePeriodUtil.getCurrentPeriod();

			// join with ven_orderItem_adjustments
			/*
			List<FinSalesRecord> finSalesRecordList = salesRecordHome
					.queryByRange(
							"select o from FinSalesRecord o left join fetch o.venOrderItem left join fetch o.venOrderItem.venOrderItemAdjustments where o.salesRecordId = "
									+ finSalesRecordId, 0, 0);
			*/
			List<FinSalesRecord> finSalesRecordList = finSalesRecordDAO.findBySalesRecordIdInnerJoinOrderItemLeftJoinOrderItemAdjustment(finSalesRecordId);
					
			if (finSalesRecordList.isEmpty()) {
				throw new EJBException(
						"Invalid finSalesRecordId passed to postRefundJournalTransaction. The sales record does not exist!");
			}

			FinSalesRecord finSalesRecord = finSalesRecordList.get(0);

			if (finSalesRecord.getFinJournalTransactions() == null) {
				finSalesRecord.setFinJournalTransactions(transactionList);
			}

			/**
			 * Set Order Item status to returned
			 */
			/*
			List<VenOrderItem> venOrderItemList = orderItemHome
					.queryByRange(
							"select o from VenOrderItem o where o.orderItemId = "
									+ finSalesRecord.getVenOrderItem()
											.getOrderItemId(), 0, 0);

			if (venOrderItemList.isEmpty()) {
				throw new EJBException(
						"Invalid finSalesRecordId passed to postRefundJournalTransaction. The VenOrderItem does not exist!");
			}

			VenOrderItem venOrderItem = venOrderItemList.get(0);
			*/
			VenOrderItem venOrderItem = venOrderItemDAO.findByOrderItemId(finSalesRecord.getVenOrderItem().getOrderItemId());
			if (venOrderItem == null) {
				throw new EJBException(
						"Invalid finSalesRecordId passed to postRefundJournalTransaction. The VenOrderItem does not exist!");				
			}
			
			//orderItemHome.mergeVenOrderItem(venOrderItem);
			if (!em.contains(venOrderItem)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "postRefundJournalTransaction:: venOrderItem not attached, calling save explicitly to attach.");
				venOrderItem = venOrderItemDAO.save(venOrderItem);
			}

			/*
			 * Process the list of Refund Journal Transactions: o create the
			 * grouping record
			 * 
			 * o journal transaction
			 * 
			 * 2130001 Hutang Merchant (merchant status "Commission/Rebate")
			 * 4110007 PENJUALAN (merchant status "Trading/Consignment") 4110001
			 * Pendapatan Komisi (merchant status "Commission") 4110004
			 * Pendapatan Logistik (only for non free shipping) 4110003
			 * Pendapatan Jasa transaksi (merchant status "Commission") 4110005
			 * Pendapatan jasa handling 2180008 PPN Keluaran 1180001 Inventory
			 * (Nilai inventory) (merchant status "Trading/Consignment") Over
			 * Voucher 2230001 Uang Jaminan Transaksi 6133010 SM - Promosi
			 * Konsumen (merchant status "Commission/Rebate") 5110003 HPP -
			 * BIAYA BARANG (merchant status "Trading/Consignment") 4900000 Pot
			 * Penjualan (merchant status "Trading/Consignment") 2220001 Voucher
			 * yang belum direalisasi 6133020 SM-Voucher Customer Service (Masuk
			 * ke potongan penjualan untuk merchant status "Trading") 2150099
			 * Barang Diterima Dimuka (merchant status "Consignment") 1180001
			 * Inventory (merchant status "Consignment")
			 * 
			 * 6133018 SM - Beban promosi bebas biaya kirim (will not be posted)
			 * 2210002 Biaya harus dibayar - logistik (will not be posted)
			 */

			/*
			 * Lookup the sales journal approval group for the day If there is
			 * none then create it
			 */

			FinJournal finJournalSales = new FinJournal();
			finJournalSales
					.setJournalId(FinJournalConstants.FIN_JOURNAL_REFUND_OTHERS.id());
			FinJournalApprovalGroup finJournalApprovalGroup = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			/*
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = journalApprovalGroupHome
					.queryByRange(
							"select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Refund Journal for :"
									+ sdf.format(new Date())
									+ "'"
									+ " and o.finApprovalStatus.approvalStatusId = "
									+ VeniceConstants.FIN_APPROVAL_STATUS_APPROVED,
							0, 0);
			*/
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = finJournalApprovalGroupDAO.findByDescriptionAndApprovalStatusId(
					"Refund Journal for :" + sdf.format(new Date()), FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
			if (finJournalApprovalGroupList.isEmpty()) {
				finJournalApprovalGroup = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
				finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);
				finJournalApprovalGroup.setFinJournal(finJournalSales);
				finJournalApprovalGroup
						.setJournalGroupDesc("Refund Journal for :"
								+ sdf.format(new Date()));
				finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// Persist the journal group
				/*
				finJournalApprovalGroup = journalApprovalGroupHome
						.persistFinJournalApprovalGroup(finJournalApprovalGroup);
			    */
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::finJournalApprovalGroup not in attach mode, calling save explicitly.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			} else {
				finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			}

			/*
			 * query to get settlement record id
			 */
			//Locator<Object> locator = null;
			VenSettlementRecord venSettlementRecord = null;
			try {
				//locator = new Locator<Object>();
				/*
				VenSettlementRecordSessionEJBLocal VenSettlementRecordHome = (VenSettlementRecordSessionEJBLocal) locator
						.lookupLocal(VenSettlementRecordSessionEJBLocal.class,
								"VenSettlementRecordSessionEJBBeanLocal");
				List<VenSettlementRecord> VenSettlementRecordList = VenSettlementRecordHome
						.queryByRange(
								"select o from VenSettlementRecord o where o.venOrderItem.orderItemId = "
										+ finSalesRecord.getVenOrderItem()
												.getOrderItemId(), 0, 0);
				*/
				List<VenSettlementRecord> venSettlementRecordList = venSettlementRecordDAO.findByOrderItemId(
						finSalesRecord.getVenOrderItem().getOrderItemId());
				//if (VenSettlementRecordList.size() > 0) {
				if (venSettlementRecordList != null && (!venSettlementRecordList.isEmpty())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: VenSettlementRecord found");
					//venSettlementRecord = VenSettlementRecordList.get(0);
					venSettlementRecord = venSettlementRecordList.get(0);

					if (venSettlementRecord == null) {
						throw new Exception("Settlement record not found");
					}
				}
			} catch (Exception e) {
				String errMsg = "Settlement record not found";
				CommonUtil.logError(this.getClass().getCanonicalName(), errMsg);
				e.printStackTrace();
			}

			String merchantCommisionType = venSettlementRecord
					.getCommissionType();

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: settlement record id: "
					+ venSettlementRecord.getSettlementRecordId());

			BigDecimal incomeForPPNCalculation = new BigDecimal(0);
			/*
			 * POST CREDIT Acc No : 2230001 , Desc : Uang Jaminan Transaksi
			 */
			FinJournalTransaction ujtJournalTransaction = new FinJournalTransaction();
			ujtJournalTransaction
					.setComments("System Credit  Uang Jaminan Transaksi: "
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			ujtJournalTransaction.setCreditDebitFlag(true);
			ujtJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountUangJaminanTransaksi = new FinAccount();
			finAccountUangJaminanTransaksi
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());
			ujtJournalTransaction.setFinAccount(finAccountUangJaminanTransaksi);

			ujtJournalTransaction.setFinSalesRecords(finSalesRecordList);
			ujtJournalTransaction.setFinJournal(finJournalSales);
			ujtJournalTransaction.setFinPeriod(finPeriod);

			FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
			finTransactionStatus
					.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
			ujtJournalTransaction.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeUangJaminanTransaksi = new FinTransactionType();
			finTransactionTypeUangJaminanTransaksi
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id());
			ujtJournalTransaction
					.setFinTransactionType(finTransactionTypeUangJaminanTransaksi);

			ujtJournalTransaction.setTransactionAmount(finSalesRecord
					.getVenOrderItem().getTotal());
			ujtJournalTransaction.setTransactionTimestamp(new Timestamp(System
					.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(ujtJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			ujtJournalTransaction.setFinSalesRecords(finSalesRecordList);
			ujtJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			// Persist the UANG JAMINAN TRANSAKSI journal transaction
			//ujtJournalTransaction = journalTransactionHome.persistFinJournalTransaction(ujtJournalTransaction);
			if (!em.contains(ujtJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::ujtJournalTransaction not in attach mode, calling save explicitly.");
				ujtJournalTransaction = finJournalTransactionDAO.save(ujtJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Uang Jaminan Transaksi AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_2230001.id() + " Amount: "
					+ ujtJournalTransaction.getTransactionAmount());

			/*
			 * Post the CREDIT for Acc no : 6133010, Desc : SM - Promosi
			 * Konsumen
			 * 
			 * Need to check if 'finSalesRecord.getGdnPromotionAmount()' is a
			 * customer promotion (not voucher, voucher CS nor free shipping)
			 * 
			 * other workaround, * get value from
			 * ven_order_item_adjustment.amount w/ order_item_id as key
			 * 
			 * * check if one of the promotions is NOT on the Voucher or
			 * Customer Service Voucher promotion list uploaded from "Promotion"
			 * menu on FInance Module
			 */

			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())
					|| merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())) {

				FinJournalTransaction smPromoJournalTransaction = new FinJournalTransaction();
				smPromoJournalTransaction
						.setComments("System Credit SM Promosi Konsumen:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				smPromoJournalTransaction.setCreditDebitFlag(true);
				smPromoJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountSMPromo = new FinAccount();
				finAccountSMPromo
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133010.id());
				smPromoJournalTransaction.setFinAccount(finAccountSMPromo);
				smPromoJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				smPromoJournalTransaction.setFinJournal(finJournalSales);
				smPromoJournalTransaction.setFinPeriod(finPeriod);
				smPromoJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePromo = new FinTransactionType();
				finTransactionTypePromo
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_PROMOSI_KONSUMEN.id());

				smPromoJournalTransaction
						.setFinTransactionType(finTransactionTypePromo);

				List<VenOrderItemAdjustment> venOrderItemAdjustments = finSalesRecord
						.getVenOrderItem().getVenOrderItemAdjustments();

				BigDecimal smPromoAmount = getNonVoucherAmount(venOrderItemAdjustments);
				smPromoJournalTransaction.setTransactionAmount(smPromoAmount);

				smPromoJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(smPromoJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				smPromoJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				smPromoJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the SM PROMO journal transaction
				//smPromoJournalTransaction = journalTransactionHome.persistFinJournalTransaction(smPromoJournalTransaction);
				if (!em.contains(smPromoJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::smPromoJournalTransaction not attached, calling save explicitly.");
					smPromoJournalTransaction = finJournalTransactionDAO.save(smPromoJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Promosi Konsumen AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_6133010.id() + " Amount: "
						+ smPromoJournalTransaction.getTransactionAmount());

			}

			FinJournalTransaction potPenjualanJournalTransaction = new FinJournalTransaction();

			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {

				/*
				 * Post the CREDIT for Acc no : 5110003, Desc : HPP Biaya Barang
				 * 
				 * value supposed to be from HARGA INVENTORY. Pending until
				 * Inventory Module is done
				 */
				FinJournalTransaction biayaBarangJournalTransaction = new FinJournalTransaction();
				biayaBarangJournalTransaction
						.setComments("System Credit HPP Biaya Barang:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				biayaBarangJournalTransaction.setCreditDebitFlag(true);
				biayaBarangJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finBiayaBarang = new FinAccount();
				finBiayaBarang
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110003.id());
				biayaBarangJournalTransaction.setFinAccount(finBiayaBarang);
				biayaBarangJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				biayaBarangJournalTransaction.setFinJournal(finJournalSales);
				biayaBarangJournalTransaction.setFinPeriod(finPeriod);
				biayaBarangJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBiayaBarang = new FinTransactionType();
				finTransactionTypeBiayaBarang
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HPP_BIAYA_BARANG.id());

				biayaBarangJournalTransaction
						.setFinTransactionType(finTransactionTypeBiayaBarang);
				biayaBarangJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
				biayaBarangJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(biayaBarangJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				biayaBarangJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				biayaBarangJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the HPP biaya barang journal transaction
				//biayaBarangJournalTransaction = journalTransactionHome.persistFinJournalTransaction(biayaBarangJournalTransaction);
				if (!em.contains(biayaBarangJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::biayaBarangJournalTransaction not in attach mode, calling save explicitly.");
					biayaBarangJournalTransaction = finJournalTransactionDAO.save(biayaBarangJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set HPP Biaya Barang AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_5110003.id() + " Amount: "
						+ biayaBarangJournalTransaction.getTransactionAmount());

				/**
				 * 
				 * Post the CREDIT for Acc no : 4900000, Desc : Pot Penjualan
				 * 
				 */
				potPenjualanJournalTransaction
						.setComments("System Credit Pot Penjualan:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				potPenjualanJournalTransaction.setCreditDebitFlag(true);
				potPenjualanJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finPotPenjualan = new FinAccount();
				finPotPenjualan
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_4900000.id());
				potPenjualanJournalTransaction.setFinAccount(finPotPenjualan);
				potPenjualanJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				potPenjualanJournalTransaction.setFinJournal(finJournalSales);
				potPenjualanJournalTransaction.setFinPeriod(finPeriod);
				potPenjualanJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePotPenjualan = new FinTransactionType();
				finTransactionTypePotPenjualan
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_POT_PENJUALAN.id());
				potPenjualanJournalTransaction
						.setFinTransactionType(finTransactionTypePotPenjualan);
				/**
				 * sum if not voucher
				 */
				List<VenOrderItemAdjustment> venOrderItemAdjustments = finSalesRecord
						.getVenOrderItem().getVenOrderItemAdjustments();

				BigDecimal potPenjualanAmount = getNonVoucherAmount(venOrderItemAdjustments);

				potPenjualanJournalTransaction
						.setTransactionAmount(potPenjualanAmount);
				potPenjualanJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(potPenjualanJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				potPenjualanJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				potPenjualanJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the Pot Penjualan journal transaction
				//potPenjualanJournalTransaction = journalTransactionHome.persistFinJournalTransaction(potPenjualanJournalTransaction);
				if (!em.contains(potPenjualanJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::potPenjualanJournalTransaction not in attach, calling save.");
					potPenjualanJournalTransaction = finJournalTransactionDAO.save(potPenjualanJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Pot Penjualan AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_4900000.id() + " Amount: "
						+ potPenjualanJournalTransaction.getTransactionAmount());

			}

			/**
			 * 
			 * POST CREDIT Acc no : 2220001, Desc : Voucher yang belum
			 * direalisasi
			 * 
			 */
			FinJournalTransaction voucherJournalTransaction = new FinJournalTransaction();
			voucherJournalTransaction.setComments("System Credit Voucher:"
					+ finSalesRecord.getVenOrderItem().getWcsOrderItemId());
			voucherJournalTransaction.setCreditDebitFlag(true);
			voucherJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountVoucher = new FinAccount();
			finAccountVoucher.setAccountId(FinAccountConstants.FIN_ACCOUNT_2220001.id());
			voucherJournalTransaction.setFinAccount(finAccountVoucher);

			voucherJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherJournalTransaction.setFinJournal(finJournalSales);
			voucherJournalTransaction.setFinPeriod(finPeriod);
			voucherJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeVoucher = new FinTransactionType();
			finTransactionTypeVoucher
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_VOUCHER_YANG_BELUM_DIREALISASI.id());
			voucherJournalTransaction
					.setFinTransactionType(finTransactionTypeVoucher);
			/**
			 * get value from ven_order_item_adjustment.amount w/ order_item_id
			 * as key
			 * 
			 * check if one of the promotions is on the Voucher promotion list
			 * uploaded from "Promotion" menu on FInance Module
			 * 
			 */
			List<VenOrderItemAdjustment> venOrderItemAdjustments = finSalesRecord.getVenOrderItem().getVenOrderItemAdjustments();

			BigDecimal voucherAmount = getVoucherAmount(venOrderItemAdjustments);

			voucherJournalTransaction.setTransactionAmount(voucherAmount);

			voucherJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

			transactionList.add(voucherJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			voucherJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);

			//voucherJournalTransaction = journalTransactionHome.persistFinJournalTransaction(voucherJournalTransaction);
			if (!em.contains(voucherJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: voucherJournalTransaction not in attach mode, calling save explicitly.");
				voucherJournalTransaction = finJournalTransactionDAO.save(voucherJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Voucher AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_2220001.id() + " Amount: "
					+ voucherJournalTransaction.getTransactionAmount());

			/**
			 * 
			 * POST CREDIT Acc no : 6133020, Desc : SM-Voucher Customer Service
			 * 
			 */
			FinJournalTransaction voucherCSJournalTransaction = new FinJournalTransaction();
			voucherCSJournalTransaction
					.setComments("System Credit Voucher Customer Service:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			voucherCSJournalTransaction.setCreditDebitFlag(true);
			voucherCSJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountVoucherCS = new FinAccount();
			finAccountVoucherCS
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133020.id());
			voucherCSJournalTransaction.setFinAccount(finAccountVoucherCS);

			voucherCSJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherCSJournalTransaction.setFinJournal(finJournalSales);
			voucherCSJournalTransaction.setFinPeriod(finPeriod);
			voucherCSJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeVoucherCS = new FinTransactionType();
			finTransactionTypeVoucherCS
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_VOUCHER_CUSTOMER_SERVICE.id());
			voucherCSJournalTransaction
					.setFinTransactionType(finTransactionTypeVoucherCS);
			/**
			 * get value from ven_order_item_adjustment.amount w/ order_item_id
			 * as key
			 * 
			 * check if one of the promotions is on the Customer Service Voucher
			 * promotion list uploaded from "Promotion" menu on FInance Module
			 * 
			 */
			venOrderItemAdjustments = finSalesRecord.getVenOrderItem()
					.getVenOrderItemAdjustments();

			BigDecimal voucherCSAmount = getVoucherCSAmount(venOrderItemAdjustments);

			voucherCSJournalTransaction.setTransactionAmount(voucherCSAmount);

			voucherCSJournalTransaction.setTransactionTimestamp(new Timestamp(
					System.currentTimeMillis()));

			transactionList.add(voucherCSJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			voucherCSJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherCSJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);

			//voucherCSJournalTransaction = journalTransactionHome.persistFinJournalTransaction(voucherCSJournalTransaction);
			if (!em.contains(voucherCSJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: voucherCSJournalTransaction not attached, calling save explicitly");
				voucherCSJournalTransaction = finJournalTransactionDAO.save(voucherCSJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Voucher Customer Service AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_6133020.id() + " Amount: "
					+ voucherCSJournalTransaction.getTransactionAmount());

			/**
			 * Post the DEBIT for HUTANG MERCHANT Only for Merchant Type
			 * Commision and Rebate
			 */
			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())
					|| merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {

				FinJournalTransaction hutangMerchantJournalTransaction = new FinJournalTransaction();
				hutangMerchantJournalTransaction
						.setComments("System Debit Hutang Merchant:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				hutangMerchantJournalTransaction.setCreditDebitFlag(false);
				hutangMerchantJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountHutangMerchant = new FinAccount();
				finAccountHutangMerchant
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2130001.id());

				hutangMerchantJournalTransaction
						.setFinAccount(finAccountHutangMerchant);
				hutangMerchantJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				hutangMerchantJournalTransaction.setFinJournal(finJournalSales);
				hutangMerchantJournalTransaction.setFinPeriod(finPeriod);
				hutangMerchantJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeHutangMerchant = new FinTransactionType();
				finTransactionTypeHutangMerchant
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HUTANG_MERCHANT.id());
				hutangMerchantJournalTransaction
						.setFinTransactionType(finTransactionTypeHutangMerchant);

				if (venSettlementRecord != null) {

					if (merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())) {
						hutangMerchantJournalTransaction
								.setTransactionAmount(finSalesRecord
										.getVenOrderItem().getTotal());
					}

					if (merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {
						hutangMerchantJournalTransaction
								.setTransactionAmount(finSalesRecord
										.getMerchantPaymentAmount());
					}

				}

				hutangMerchantJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(hutangMerchantJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}
				hutangMerchantJournalTransaction
						.setFinSalesRecords(finSalesRecordList);

				hutangMerchantJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				// Persist the HUTANG MERCHANT journal transaction
				//hutangMerchantJournalTransaction = journalTransactionHome.persistFinJournalTransaction(hutangMerchantJournalTransaction);
				if (!em.contains(hutangMerchantJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: hutangMerchantJournalTransaction not attached, calling save.");
					hutangMerchantJournalTransaction = finJournalTransactionDAO.save(hutangMerchantJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Hutang Merchant AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_2130001.id()
						+ " Amount: "
						+ hutangMerchantJournalTransaction
								.getTransactionAmount());
			}

			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())
					|| merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())) {

				/**
				 * POST DEBIT of Acc No : 4110007, Desc : Penjualan
				 */
				FinJournalTransaction penjualanJournalTransaction = new FinJournalTransaction();
				penjualanJournalTransaction
						.setComments("System Debit Penjualan:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				penjualanJournalTransaction.setCreditDebitFlag(false);
				penjualanJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountPenjualan = new FinAccount();
				finAccountPenjualan
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110007.id());

				penjualanJournalTransaction.setFinAccount(finAccountPenjualan);
				penjualanJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				penjualanJournalTransaction.setFinJournal(finJournalSales);
				penjualanJournalTransaction.setFinPeriod(finPeriod);

				penjualanJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePenjualan = new FinTransactionType();
				finTransactionTypePenjualan
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENJUALAN.id());
				penjualanJournalTransaction
						.setFinTransactionType(finTransactionTypePenjualan);

				if (merchantCommisionType
						.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())) {
					penjualanJournalTransaction
							.setTransactionAmount(finSalesRecord
									.getVenOrderItem()
									.getTotal()
									.subtract(
											voucherCSJournalTransaction
													.getTransactionAmount())
									.divide(GDNPPN_DIVISOR, 2,
											RoundingMode.HALF_UP));
				} else {
					penjualanJournalTransaction
							.setTransactionAmount(finSalesRecord
									.getVenOrderItem()
									.getTotal()
									.divide(GDNPPN_DIVISOR, 2,
											RoundingMode.HALF_UP));
				}

				penjualanJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(penjualanJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				penjualanJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				penjualanJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the Penjualan journal transaction
				//penjualanJournalTransaction = journalTransactionHome.persistFinJournalTransaction(penjualanJournalTransaction);
				if (!em.contains(penjualanJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: penjualanJournalTransaction not attached, calling save.");
					penjualanJournalTransaction = finJournalTransactionDAO.save(penjualanJournalTransaction);
				}

				incomeForPPNCalculation = incomeForPPNCalculation
						.add(penjualanJournalTransaction.getTransactionAmount());

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Penjualan AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_4110007.id() + " Amount: "
						+ penjualanJournalTransaction.getTransactionAmount());

			}

			/**
			 * 
			 * Post the DEBIT for Acc no : 4110001, Desc: PENDAPATAN KOMISI
			 * 
			 */
			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {

				FinJournalTransaction komisiJournalTransaction = new FinJournalTransaction();
				komisiJournalTransaction
						.setComments("System Debit Pendapatan Komisi:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				komisiJournalTransaction.setCreditDebitFlag(false);
				komisiJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountKomisi = new FinAccount();
				finAccountKomisi
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110001.id());

				komisiJournalTransaction.setFinAccount(finAccountKomisi);
				komisiJournalTransaction.setFinSalesRecords(finSalesRecordList);
				komisiJournalTransaction.setFinJournal(finJournalSales);
				komisiJournalTransaction.setFinPeriod(finPeriod);

				komisiJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeKomisi = new FinTransactionType();
				finTransactionTypeKomisi
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_KOMISI.id());
				komisiJournalTransaction
						.setFinTransactionType(finTransactionTypeKomisi);

				komisiJournalTransaction.setTransactionAmount(finSalesRecord
						.getGdnCommissionAmount().divide(GDNPPN_DIVISOR, 2,
								RoundingMode.HALF_UP));
				komisiJournalTransaction.setTransactionTimestamp(new Timestamp(
						System.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(komisiJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				komisiJournalTransaction.setFinSalesRecords(finSalesRecordList);
				komisiJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the KOMISI journal transaction
				//komisiJournalTransaction = journalTransactionHome.persistFinJournalTransaction(komisiJournalTransaction);
				if (!em.contains(komisiJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::komisiJournalTransaction not attached, calling save.");
					komisiJournalTransaction = finJournalTransactionDAO.save(komisiJournalTransaction);
				}

				incomeForPPNCalculation = incomeForPPNCalculation
						.add(komisiJournalTransaction.getTransactionAmount());

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Pendapatan Komisi AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_4110001.id() + " Amount: "
						+ komisiJournalTransaction.getTransactionAmount());

			}

			boolean freeShipping = false;

			venOrderItemAdjustments = finSalesRecord.getVenOrderItem()
					.getVenOrderItemAdjustments();

			for (VenOrderItemAdjustment venOrderItemAdjustment : venOrderItemAdjustments) {
				if (venOrderItemAdjustment.getVenPromotion().getPromotionName()
						.toLowerCase().contains("free shipping")) {
					freeShipping = true;
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: this item is free shipping");
					break;
				}
			}

			if (!freeShipping) {
				/**
				 * 
				 * Post the DEBIT for Acc No : 4110004, Desc : PENDAPATAN
				 * LOGISTIK
				 * 
				 */
				FinJournalTransaction pendapatanLogisticsJournalTransaction = new FinJournalTransaction();
				pendapatanLogisticsJournalTransaction
						.setComments("System Debit Pendapatan Logistics:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				pendapatanLogisticsJournalTransaction.setCreditDebitFlag(false);
				pendapatanLogisticsJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountPendapatanLogistics = new FinAccount();
				finAccountPendapatanLogistics
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110004.id());

				pendapatanLogisticsJournalTransaction
						.setFinAccount(finAccountPendapatanLogistics);
				pendapatanLogisticsJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				pendapatanLogisticsJournalTransaction
						.setFinJournal(finJournalSales);
				pendapatanLogisticsJournalTransaction.setFinPeriod(finPeriod);
				pendapatanLogisticsJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePendapatanLogistics = new FinTransactionType();
				finTransactionTypePendapatanLogistics
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_LOGISTIK.id());
				pendapatanLogisticsJournalTransaction
						.setFinTransactionType(finTransactionTypePendapatanLogistics);

				pendapatanLogisticsJournalTransaction
						.setTransactionAmount(finSalesRecord
								.getTotalLogisticsRelatedAmount());

				pendapatanLogisticsJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(pendapatanLogisticsJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				pendapatanLogisticsJournalTransaction
						.setFinSalesRecords(finSalesRecordList);

				pendapatanLogisticsJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the PENDAPATAN LOGISTICS journal transaction
				//pendapatanLogisticsJournalTransaction = journalTransactionHome.persistFinJournalTransaction(pendapatanLogisticsJournalTransaction);
				if (!em.contains(pendapatanLogisticsJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: pendapatanLogisticsJournalTransaction not attached, calling save.");
					pendapatanLogisticsJournalTransaction = finJournalTransactionDAO.save(pendapatanLogisticsJournalTransaction);
				}

				incomeForPPNCalculation = incomeForPPNCalculation
						.add(pendapatanLogisticsJournalTransaction
								.getTransactionAmount());

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Pendapatan Logistik AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_4110004.id()
						+ " Amount: "
						+ pendapatanLogisticsJournalTransaction
								.getTransactionAmount());

			}

			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {
				/**
				 * 
				 * Post the DEBIT Acc No: 4110003 , Desc: JASA TRANSAKSI
				 * 
				 */
				FinJournalTransaction jasaTransaksiJournalTransaction = new FinJournalTransaction();
				jasaTransaksiJournalTransaction
						.setComments("System Debit Jasa Transaksi:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				jasaTransaksiJournalTransaction.setCreditDebitFlag(false);
				jasaTransaksiJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountJasaTransaksi = new FinAccount();
				finAccountJasaTransaksi
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110003.id());

				jasaTransaksiJournalTransaction
						.setFinAccount(finAccountJasaTransaksi);
				jasaTransaksiJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				jasaTransaksiJournalTransaction.setFinJournal(finJournalSales);
				jasaTransaksiJournalTransaction.setFinPeriod(finPeriod);
				jasaTransaksiJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeProcessFee = new FinTransactionType();
				finTransactionTypeProcessFee
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_JASA_TRANSAKSI.id());
				jasaTransaksiJournalTransaction
						.setFinTransactionType(finTransactionTypeProcessFee);

				jasaTransaksiJournalTransaction
						.setTransactionAmount(finSalesRecord
								.getGdnTransactionFeeAmount()
								.divide(GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
				jasaTransaksiJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(jasaTransaksiJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				jasaTransaksiJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				jasaTransaksiJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the JASA TRANSAKSI journal transaction
				//jasaTransaksiJournalTransaction = journalTransactionHome.persistFinJournalTransaction(jasaTransaksiJournalTransaction);
				if (!em.contains(jasaTransaksiJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postRefundJournalTransactions:: jasaTransaksiJournalTransaction not attached, calling save explicitly.");
					jasaTransaksiJournalTransaction = finJournalTransactionDAO.save(jasaTransaksiJournalTransaction);
				}
				// transactionList.add(jasaTransaksiJournalTransaction);

				incomeForPPNCalculation = incomeForPPNCalculation
						.add(jasaTransaksiJournalTransaction
								.getTransactionAmount());

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Pendapatan Jasa Transaksi AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_4110003.id()
						+ " Amount: "
						+ jasaTransaksiJournalTransaction
								.getTransactionAmount());

			}

			/*
			 * Post the DEBIT for AccNo : 4110005, Desc: JASA HANDLING PELANGGAN
			 */
			FinJournalTransaction jasaHandlingJournalTransaction = new FinJournalTransaction();
			jasaHandlingJournalTransaction
					.setComments("System Debit Jasa Handling:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			jasaHandlingJournalTransaction.setCreditDebitFlag(false);
			jasaHandlingJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountJasaHandling = new FinAccount();
			finAccountJasaHandling
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110005.id());

			jasaHandlingJournalTransaction
					.setFinAccount(finAccountJasaHandling);
			jasaHandlingJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			jasaHandlingJournalTransaction.setFinJournal(finJournalSales);
			jasaHandlingJournalTransaction.setFinPeriod(finPeriod);
			jasaHandlingJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeProcessFee = new FinTransactionType();
			finTransactionTypeProcessFee
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_JASA_HANDLING.id());

			jasaHandlingJournalTransaction
					.setFinTransactionType(finTransactionTypeProcessFee);

			jasaHandlingJournalTransaction.setTransactionAmount(finSalesRecord
					.getGdnHandlingFeeAmount().divide(GDNPPN_DIVISOR, 2,
							RoundingMode.HALF_UP));
			jasaHandlingJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(jasaHandlingJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			jasaHandlingJournalTransaction
					.setFinSalesRecords(finSalesRecordList);

			jasaHandlingJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);

			// Persist the JASA HANDLING journal transaction
			//jasaHandlingJournalTransaction = journalTransactionHome.persistFinJournalTransaction(jasaHandlingJournalTransaction);
			if (!em.contains(jasaHandlingJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: jasaHandlingJournalTransaction not attached, calling save.");
				jasaHandlingJournalTransaction = finJournalTransactionDAO.save(jasaHandlingJournalTransaction);
			}

			incomeForPPNCalculation = incomeForPPNCalculation
					.add(jasaHandlingJournalTransaction.getTransactionAmount());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Pendapatan Jasa Handling AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_4110005.id() + " Amount: "
					+ jasaHandlingJournalTransaction.getTransactionAmount());

			if (pkp) {
				/**
				 * 
				 * Post the DEBIT for Acc No 2180008, Desc: PPN
				 * 
				 */
				FinJournalTransaction ppnJournalTransaction = new FinJournalTransaction();
				ppnJournalTransaction.setComments("System Debit PPN:"
						+ finSalesRecord.getVenOrderItem().getWcsOrderItemId());
				ppnJournalTransaction.setCreditDebitFlag(false);
				ppnJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountPPN = new FinAccount();
				finAccountPPN.setAccountId(FinAccountConstants.FIN_ACCOUNT_2180008.id());

				ppnJournalTransaction.setFinAccount(finAccountPPN);
				ppnJournalTransaction.setFinSalesRecords(finSalesRecordList);
				ppnJournalTransaction.setFinJournal(finJournalSales);
				ppnJournalTransaction.setFinPeriod(finPeriod);
				ppnJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePPN = new FinTransactionType();
				finTransactionTypePPN
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PPN.id());
				ppnJournalTransaction
						.setFinTransactionType(finTransactionTypePPN);

				if (merchantCommisionType
						.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())
						|| merchantCommisionType
								.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())) {
					ppnJournalTransaction
							.setTransactionAmount(incomeForPPNCalculation
									.multiply(new BigDecimal(0.1)).setScale(2,
											RoundingMode.HALF_UP));
				}

				if (merchantCommisionType
						.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
						|| merchantCommisionType
								.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {
					ppnJournalTransaction
							.setTransactionAmount(incomeForPPNCalculation
									.subtract(
											potPenjualanJournalTransaction
													.getTransactionAmount())
									.multiply(new BigDecimal(0.1))
									.setScale(2, RoundingMode.HALF_UP));
				}

				ppnJournalTransaction.setTransactionTimestamp(new Timestamp(
						System.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(ppnJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				ppnJournalTransaction.setFinSalesRecords(finSalesRecordList);
				ppnJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the PPN journal transaction
				//ppnJournalTransaction = journalTransactionHome.persistFinJournalTransaction(ppnJournalTransaction);
				if (!em.contains(ppnJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postRefundJournalTransactions::ppnJournalTransaction not attached, hence calling save.explicitly.");
					ppnJournalTransaction = finJournalTransactionDAO.save(ppnJournalTransaction);
				}
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: set PPN, amount: "
						+ ppnJournalTransaction.getTransactionAmount());

			}

			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {

				/*
				 * Post the DEBIT for Acc no : 1180007, Desc : Inventory Over
				 * Voucher
				 * 
				 * value supposed to be from HARGA INVENTORY. Pending until
				 * Inventory Module is done
				 */
				FinJournalTransaction inventoryOverVoucherJournalTransaction = new FinJournalTransaction();
				inventoryOverVoucherJournalTransaction
						.setComments("System Debit Inventory Over Voucher:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				inventoryOverVoucherJournalTransaction
						.setCreditDebitFlag(false);
				inventoryOverVoucherJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finInventoryOverVoucher = new FinAccount();
				finInventoryOverVoucher
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_1180001.id());
				inventoryOverVoucherJournalTransaction
						.setFinAccount(finInventoryOverVoucher);
				inventoryOverVoucherJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				inventoryOverVoucherJournalTransaction
						.setFinJournal(finJournalSales);
				inventoryOverVoucherJournalTransaction.setFinPeriod(finPeriod);
				inventoryOverVoucherJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeInventoryOverVoucher = new FinTransactionType();
				finTransactionTypeInventoryOverVoucher
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_INVENTORY_OVER_VOUCHER.id());

				inventoryOverVoucherJournalTransaction
						.setFinTransactionType(finTransactionTypeInventoryOverVoucher);
				inventoryOverVoucherJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
				inventoryOverVoucherJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(inventoryOverVoucherJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				inventoryOverVoucherJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				inventoryOverVoucherJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the Inventory Over Voucher journal transaction
				//inventoryOverVoucherJournalTransaction = journalTransactionHome.persistFinJournalTransaction(inventoryOverVoucherJournalTransaction);
				if (!em.contains(inventoryOverVoucherJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postRefundJournalTransactions::inventoryOverVoucherJournalTransaction not attached, calling save explicitly.");
					inventoryOverVoucherJournalTransaction = finJournalTransactionDAO.save(inventoryOverVoucherJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Inventory Over Voucher AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_1180001.id()
						+ " Amount: "
						+ inventoryOverVoucherJournalTransaction
								.getTransactionAmount());

			}

			if (merchantCommisionType
					.equalsIgnoreCase(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {

				/*
				 * Post the CREDIT for Acc no : 1180001, Desc : Inventory
				 * 
				 * value supposed to be from INVENTORY. Pending until Inventory
				 * Module is done
				 */
				FinJournalTransaction inventoryJournalTransaction = new FinJournalTransaction();
				inventoryJournalTransaction
						.setComments("System Credit Inventory:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				inventoryJournalTransaction.setCreditDebitFlag(true);
				inventoryJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finInventory = new FinAccount();
				finInventory.setAccountId(FinAccountConstants.FIN_ACCOUNT_1180001.id());
				inventoryJournalTransaction.setFinAccount(finInventory);
				inventoryJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				inventoryJournalTransaction.setFinJournal(finJournalSales);
				inventoryJournalTransaction.setFinPeriod(finPeriod);
				inventoryJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeInventory = new FinTransactionType();
				finTransactionTypeInventory
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_INVENTORY.id());

				inventoryJournalTransaction
						.setFinTransactionType(finTransactionTypeInventory);
				inventoryJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
				inventoryJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(inventoryJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				inventoryJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				inventoryJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the Inventory journal transaction
				//inventoryJournalTransaction = journalTransactionHome.persistFinJournalTransaction(inventoryJournalTransaction);
				if (!em.contains(inventoryJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: inventoryJournalTransaction not attached, calling save.");
					inventoryJournalTransaction = finJournalTransactionDAO.save(inventoryJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Inventory AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_1180001.id() + " Amount: "
						+ inventoryJournalTransaction.getTransactionAmount());

				/*
				 * Post the DEBIT for Acc no : 2150099, Desc : Barang Diterima
				 * Dimuka
				 * 
				 * value supposed to be from INVENTORY. Pending until Inventory
				 * Module is done
				 */
				FinJournalTransaction barangDiterimaDimukaJournalTransaction = new FinJournalTransaction();
				barangDiterimaDimukaJournalTransaction
						.setComments("System Debit Barang Diterima Dimuka:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				barangDiterimaDimukaJournalTransaction
						.setCreditDebitFlag(false);
				barangDiterimaDimukaJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finBarangDiterimaDimuka = new FinAccount();
				finBarangDiterimaDimuka
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2150099.id());
				barangDiterimaDimukaJournalTransaction
						.setFinAccount(finBarangDiterimaDimuka);
				barangDiterimaDimukaJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				barangDiterimaDimukaJournalTransaction
						.setFinJournal(finJournalSales);
				barangDiterimaDimukaJournalTransaction.setFinPeriod(finPeriod);
				barangDiterimaDimukaJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBarangDiterimaDimuka = new FinTransactionType();
				finTransactionTypeBarangDiterimaDimuka
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BARANG_DITERIMA_DIMUKA.id());

				barangDiterimaDimukaJournalTransaction
						.setFinTransactionType(finTransactionTypeBarangDiterimaDimuka);
				barangDiterimaDimukaJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
				barangDiterimaDimukaJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(barangDiterimaDimukaJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				barangDiterimaDimukaJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				barangDiterimaDimukaJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);

				// Persist the Inventory journal transaction
				//barangDiterimaDimukaJournalTransaction = journalTransactionHome.persistFinJournalTransaction(barangDiterimaDimukaJournalTransaction);
				if (!em.contains(barangDiterimaDimukaJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: barangDiterimaDimukaJournalTransaction not attached, calling save.");
					barangDiterimaDimukaJournalTransaction = finJournalTransactionDAO.save(barangDiterimaDimukaJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: Set Barang Diterima Dimuka AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_2150099.id()
						+ " Amount: "
						+ barangDiterimaDimukaJournalTransaction
								.getTransactionAmount());

			}

			// Set the list of transactions associated with the sales record and
			// merge
			finSalesRecord.setFinJournalTransactions(transactionList);
			//salesRecordHome.mergeFinSalesRecord(finSalesRecord);
			if (!em.contains(finSalesRecord)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions:: finSalesRecord not attached, calling save.");
				finSalesRecord = finSalesRecordDAO.save(finSalesRecord);
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting refund journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransactions::" + " completed in "
				+ duration + "ms");

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postRefundJournalTransaction(Long reconciliationRecordId, Double
	 * refundAmount, Double fee, , int refundType)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postRefundJournalTransaction(Long reconciliationRecordId,
			Double refundAmount, Double fee, int refundType,
			boolean printjournal) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {

			/*
			 * Read all the relevant funds in records from the database and
			 * determine how much has been paid as well as the remaining balance
			 * according to the reconciliation
			 */
			FinArFundsInReconRecord reconciliationRecord = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(reconciliationRecordId);
			
			if (reconciliationRecord == null) {
				throw new EJBException("Reconciliation record not found:" + reconciliationRecordId);
			}
		
			VenOrderPayment venOrderPayment = null;

			/*
			 * For unexpected/unmatched payments there may not be a
			 * VenOrderPayment
			 */
			if (reconciliationRecord.getVenOrderPayment() != null) {
				venOrderPayment = venOrderPaymentDAO.findByOrderPaymentId(reconciliationRecord.getVenOrderPayment().getOrderPaymentId());
			}

			BigDecimal remainingUnallocatedAmount = null;
			remainingUnallocatedAmount = reconciliationRecord.getProviderReportPaidAmount();
			/*
			 * If there is a VenOrderPayment record then... Ascertain how much
			 * of the payment has been allocated to orders already and throw an
			 * exception if there are not enough funds to allocate
			 */

			if (venOrderPayment != null) {
				List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findByOrderPaymentId(
						venOrderPayment.getOrderPaymentId());
				BigDecimal allocatedAmount = new BigDecimal(0);

				/*
				 * Get the amount of funds that have been allocated from the
				 * payment amount. Check that the order has not been cancelled.
				 * If the order has been cancelled then the balance of funds can
				 * be made available for refund.
				 */
				remainingUnallocatedAmount = reconciliationRecord
						.getProviderReportPaidAmount()
						.subtract(allocatedAmount);
				if (orderPaymentAllocationList != null) {
					for (VenOrderPaymentAllocation allocation : orderPaymentAllocationList) {
						VenOrder allocatedOrder = venOrderDAO.findByOrderId(allocation.getId().getOrderId());
						if (allocatedOrder.getVenOrderStatus()
								.getOrderStatusId() != VenOrderStatusConstants.VEN_ORDER_STATUS_X.code()) {
							allocatedAmount = allocatedAmount.add(allocation
									.getAllocationAmount());
						}
					}

					if (remainingUnallocatedAmount.compareTo(new BigDecimal(
							refundAmount - fee)) < 0) {
						throw new EJBException(
								"Insufficient unallocated payment funds available to serve this request");
					}
				}
			}

			// FinAccount finAccountRefundPelanggan = new FinAccount(); //
			// better to use 'finAccountRefund' as var name instead of
			// 'finAccountRefundPelanggan' because it will represent those 2
			// accounts
			FinAccount finAccountRefund = new FinAccount();
			long accountRefund = 0;

			switch (refundType) {
			case FinRefundTypeConstants.Constants.BANK_ID:
				finAccountRefund.setAccountId(FinAccountConstants.FIN_ACCOUNT_2170002.id());
				accountRefund = FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id();
				break;
			case FinRefundTypeConstants.Constants.CUSTOMER_ID:
				finAccountRefund.setAccountId(FinAccountConstants.FIN_ACCOUNT_2170001.id());
				accountRefund = FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_CUSTOMER.id();
				break;
			default:
				break;
			// no action. should produce an error later because of the empty account id
			}

			if (printjournal) {
				/*
				 * Lookup the refund journal approval group for the day If there
				 * is none then create it
				 */
				FinJournal finJournalRefund = new FinJournal();
				finJournalRefund.setJournalId(FinJournalConstants.FIN_JOURNAL_REFUND_OTHERS.id());
				FinJournalApprovalGroup finJournalApprovalGroup = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
				// List<FinJournalApprovalGroup> finJournalApprovalGroupList =
				// journalApprovalGroupHome.queryByRange(
				// "select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Refund Journal for:"
				// + sdf.format(new Date()) + "'" +
				// " and o.finApprovalStatus.approvalStatusId = " +
				// VeniceConstants.FIN_APPROVAL_STATUS_NEW, 0, 0);
				if (finJournalApprovalGroup == null) {
					finJournalApprovalGroup = new FinJournalApprovalGroup();
					FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
					finApprovalStatus.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
					finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);
					finJournalApprovalGroup.setFinJournal(finJournalRefund);
					finJournalApprovalGroup.setJournalGroupDesc("Refund Journal for:" + sdf.format(new Date()));
					finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(System.currentTimeMillis()));

					// Persist the journal group
					//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
					if (!em.contains(finJournalApprovalGroup)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction:: finJournalApprovalGroup not attached, calling save explicitly.");
						finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
					}
				}
				// else {
				// finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
				// }

				String wcsOrderId = reconciliationRecord.getWcsOrderId() != null ? reconciliationRecord
						.getWcsOrderId() : "";

				// Create the DEBIT journal transaction for REFUND against UANG MUKA PELANGGAN
				FinJournalTransaction refundJournalTransaction = new FinJournalTransaction();
				refundJournalTransaction.setComments("Refund Pelanggan DEBIT for Order:" + wcsOrderId
								+ " Payment:" + reconciliationRecord.getProviderReportPaymentId());
				refundJournalTransaction.setCreditDebitFlag(false);
				refundJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountUangMukaPelanggan = new FinAccount();
				if (reconciliationRecord.getFinArReconResult()
						.getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id()) {
					finAccountUangMukaPelanggan.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230002.id());
				} else {
					finAccountUangMukaPelanggan.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());
				}
				refundJournalTransaction.setFinAccount(finAccountUangMukaPelanggan);

				List<FinArFundsInReconRecord> reconRecordList = new ArrayList<FinArFundsInReconRecord>();
				reconRecordList.add(reconciliationRecord);
				
				refundJournalTransaction.setFinArFundsInReconRecords(reconRecordList);

				refundJournalTransaction.setFinJournal(finJournalRefund);
				refundJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());

				FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
				finTransactionStatus.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
				refundJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionType = new FinTransactionType();
				if (reconciliationRecord.getFinArReconResult()
						.getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id()) {
					finTransactionType.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_BELUM_TERIDENTIFIKASI.id());
				} else {
					finTransactionType.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id());
				}
				refundJournalTransaction.setFinTransactionType(finTransactionType);

				// refundJournalTransaction.setTransactionAmount(reconciliationRecord.getProviderReportPaidAmount());
				// // replace with the following line by yauri @Sept 18, 2013
				// 07:11AM
				refundJournalTransaction.setTransactionAmount(new BigDecimal(
						refundAmount + ""));
				refundJournalTransaction.setTransactionTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// String
				// wcsOrderId=reconciliationRecord.getWcsOrderId()!=null?reconciliationRecord.getWcsOrderId():"";
				// // moved onto top by yauri @Sept 18, 2013 07:13AM
				VenBank venBank = reconciliationRecord.getVenOrderPayment() != null ? reconciliationRecord
						.getVenOrderPayment().getVenBank()
						: reconciliationRecord.getFinArFundsInReport()
								.getFinArFundsInReportType().getVenBank();

				refundJournalTransaction.setWcsOrderID(wcsOrderId);
				refundJournalTransaction.setVenBank(venBank);
				// Persist the DEBIT journal transaction for the refund
				//refundJournalTransaction = journalTransactionHome.persistFinJournalTransaction(refundJournalTransaction);
				if (!em.contains(refundJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction:: refundJournalTransaction not in attach mode, calling save.");
					refundJournalTransaction = finJournalTransactionDAO.save(refundJournalTransaction);
				}
				transactionList.add(refundJournalTransaction);

				// Create the CREDIT journal transaction for REFUND against
				// REFUND-------------------------------
				// PELANGGAN
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction:: Creating CREDIT Journal Transaction for REFUND CUSTOMER");
				FinJournalTransaction refundPelangganJournalTransaction = new FinJournalTransaction();
				refundPelangganJournalTransaction
						.setComments("Refund Pelanggan CREDIT for Order:"
								+ wcsOrderId // reconciliationRecord.getWcsOrderId()
								+ " Payment:"
								+ reconciliationRecord
										.getProviderReportPaymentId());
				refundPelangganJournalTransaction.setCreditDebitFlag(true);
				refundPelangganJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				// finAccountRefundPelanggan.setAccountId(new
				// Long(refundType+""));
				finAccountRefund.setAccountId(new Long(refundType + ""));
				// refundPelangganJournalTransaction.setFinAccount(finAccountRefundPelanggan);
				refundPelangganJournalTransaction
						.setFinAccount(finAccountRefund);

				refundPelangganJournalTransaction
						.setFinArFundsInReconRecords(reconRecordList);
				refundPelangganJournalTransaction
						.setFinJournal(finJournalRefund);
				refundPelangganJournalTransaction
						.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
				refundPelangganJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);
				refundPelangganJournalTransaction
						.setFinTransactionType(finTransactionType);
				refundPelangganJournalTransaction
						.setTransactionAmount(new BigDecimal(refundAmount + ""));
				refundPelangganJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				refundPelangganJournalTransaction.setWcsOrderID(wcsOrderId);
				refundPelangganJournalTransaction.setVenBank(venBank);
				// Persist the CREDIT journal transaction for the refund
				//refundPelangganJournalTransaction = journalTransactionHome.persistFinJournalTransaction(refundPelangganJournalTransaction);
				if (!em.contains(refundPelangganJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postRefundJournalTransaction:: refundPelangganJournalTransaction not attached, calling save to attach.");
					refundPelangganJournalTransaction = finJournalTransactionDAO.save(refundPelangganJournalTransaction);
				}
				transactionList.add(refundPelangganJournalTransaction);

				if (fee > 0) {
					// Create the CREDIT journal transaction for fees
					FinJournalTransaction processFeeJournalTransaction = new FinJournalTransaction();
					processFeeJournalTransaction
							.setComments("Processing Fee (Refund) CREDIT for Order:"
									+ wcsOrderId // reconciliationRecord.getWcsOrderId()!=null?reconciliationRecord.getWcsOrderId():""
									+ " Payment:"
									+ reconciliationRecord
											.getProviderReportPaymentId());
					refundJournalTransaction.setCreditDebitFlag(true);
					processFeeJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);

					FinAccount finAccountProcessingFee = new FinAccount();

					/*
					 * The fees are either taken from the bank or the customer
					 * as a processing fee
					 */
					if (refundType == FinRefundTypeConstants.FIN_REFUND_TYPE_BANK.id()) {
						finAccountProcessingFee.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110003.id());
					} else if (refundType == FinRefundTypeConstants.FIN_REFUND_TYPE_CUSTOMER.id()) {
						finAccountProcessingFee.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110007.id());
					}
					processFeeJournalTransaction
							.setFinAccount(finAccountProcessingFee);

					processFeeJournalTransaction
							.setFinArFundsInReconRecords(reconRecordList);

					processFeeJournalTransaction
							.setFinJournal(finJournalRefund);
					processFeeJournalTransaction.setFinPeriod(FinancePeriodUtil
							.getCurrentPeriod());

					processFeeJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType finTransactionTypeProcessFee = new FinTransactionType();
					finTransactionTypeProcessFee.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PROCESS_FEE.id());
					processFeeJournalTransaction.setFinTransactionType(finTransactionTypeProcessFee);

					processFeeJournalTransaction.setTransactionAmount(new BigDecimal(fee));
					processFeeJournalTransaction.setCreditDebitFlag(false);
					processFeeJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

					processFeeJournalTransaction.setWcsOrderID(wcsOrderId);
					processFeeJournalTransaction.setVenBank(venBank);
					// Persist the DEBIT journal transaction for the refund
					//processFeeJournalTransaction = journalTransactionHome.persistFinJournalTransaction(processFeeJournalTransaction);
					if (!em.contains(processFeeJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), 
								"postRefundJournalTransaction:: processFeeJournalTransaction not attached, calling save explicitly to attach.");
						processFeeJournalTransaction = finJournalTransactionDAO.save(processFeeJournalTransaction);
					}
					transactionList.add(processFeeJournalTransaction);
				}

				// Create a refund record
				FinArFundsInRefund finArFundsInRefund = new FinArFundsInRefund();
				finArFundsInRefund
						.setFinArFundsInReconRecord(reconciliationRecord);
				finArFundsInRefund.setApAmount(new BigDecimal(refundAmount));
				finArFundsInRefund.setRefundTimestamp(new Timestamp(System
						.currentTimeMillis()));
				finArFundsInRefund.setRefundType(reconciliationRecord
						.getFinArFundsInActionApplied().getActionAppliedDesc());
				//refundRecordHome.persistFinArFundsInRefund(finArFundsInRefund);
				if (!em.contains(finArFundsInRefund)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction:: finArFundsInRefund not attached, calling save explicitly.");
					finArFundsInRefund = finArFundsInRefundDAO.save(finArFundsInRefund);
				}
			} else {
				reconciliationRecord.setRefundAmount(reconciliationRecord
						.getRefundAmount().add(new BigDecimal(refundAmount)));
				/*
				 * 1) Flag the recon record as being refunded 2) Adjust the
				 * balance to equal the remianing unallocated amount 3) Merge
				 * the funds in record
				 */
				FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
				finArFundsInActionApplied.setActionAppliedId(accountRefund);
				reconciliationRecord.setFinArFundsInActionApplied(finArFundsInActionApplied);

				// FinArReconResult finArReconResult = new FinArReconResult();
				// finArReconResult.setReconResultId(VeniceConstants.FIN_AR_RECON_RESULT_REFUNDED);
				// reconciliationRecord.setFinArReconResult(finArReconResult);

				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
				reconciliationRecord.setFinApprovalStatus(finApprovalStatus);

				// reconciliationRecord.setRemainingBalanceAmount(remainingUnallocatedAmount.subtract(new
				// BigDecimal(refundAmount)));
				// reconciliationRecord.setRemainingBalanceAmount(new
				// BigDecimal(refundAmount).subtract(reconciliationRecord.getProviderReportPaidAmount()).add(reconciliationRecord.getPaymentAmount()!=null?reconciliationRecord.getPaymentAmount():new
				// BigDecimal("0")));
				//reconciliationRecord = fundsInReconRecordHome.mergeFinArFundsInReconRecord(reconciliationRecord);
				if (!em.contains(reconciliationRecord)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postRefundJournalTransaction:: reconciliationRecord not attached, calling save explicitly to make it attached");
					reconciliationRecord = finArFundsInReconRecordDAO.save(reconciliationRecord);
				}

				/*
				 * Note that we do not do anything with the transactionList here
				 */
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting refund journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postRefundJournalTransaction::" + " completed in "
				+ duration + "ms");
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postBalancingJournalTransaction(Long cashReceivedJournalId, Long
	 * salesJournalId)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postBalancingJournalTransaction(Long cashReceivedJournalId,
			Long salesJournalId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postBalancingJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {
			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");
			*/

			/*
			 * Check that the two journal entries for the balancing exist
			 */
			/*
			List<FinJournalTransaction> cashReceivedJournalList = journalTransactionHome
					.queryByRange(
							"select o from FinJournalTransaction o where o.transactionId = "
									+ cashReceivedJournalId, 0, 0);
			if (cashReceivedJournalList.isEmpty()) {
				throw new EJBException(
						"The journal transaction identifier for the cash received journal provided is invalid. No journal transaction exists");
			}

			FinJournalTransaction cashReceivedJournalTransaction = cashReceivedJournalList.get(0);
			*/
			FinJournalTransaction cashReceivedJournalTransaction = finJournalTransactionDAO.findByTransactionId(cashReceivedJournalId);
			if (cashReceivedJournalTransaction == null) {
				throw new EJBException(
						"The journal transaction identifier for the cash received journal provided is invalid. No journal transaction exists");				
			}

			/*
			List<FinJournalTransaction> salesJournalList = journalTransactionHome
					.queryByRange(
							"select o from FinJournalTransaction o where o.transactionId = "
									+ salesJournalId, 0, 0);
			if (salesJournalList.isEmpty()) {
				throw new EJBException(
						"The journal transaction identifier for the sales journal provided is invalid. No journal transaction exists");
			}

			FinJournalTransaction salesJournalTransaction = cashReceivedJournalList
					.get(0);
			*/
			FinJournalTransaction salesJournalTransaction = finJournalTransactionDAO.findByTransactionId(salesJournalId);
			if (salesJournalTransaction == null) {
				throw new EJBException(
						"The journal transaction identifier for the sales journal provided is invalid. No journal transaction exists");				
			}

			/*
			 * Lookup the balancing journal approval group for the day If there
			 * is none then create it
			 */

			FinJournal finJournalRefund = new FinJournal();
			finJournalRefund.setJournalId(FinJournalConstants.FIN_JOURNAL_REFUND_OTHERS.id());
			FinJournalApprovalGroup finJournalApprovalGroup = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			/*
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = journalApprovalGroupHome
					.queryByRange(
							"select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Balancing Journal for:"
									+ sdf.format(new Date())
									+ "'"
									+ " and o.finApprovalStatus.approvalStatusId = "
									+ VeniceConstants.FIN_APPROVAL_STATUS_APPROVED,
							0, 0);
			*/
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = finJournalApprovalGroupDAO.findByDescriptionAndApprovalStatusId(
					"Balancing Journal for:" + sdf.format(new Date()), FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
			if (finJournalApprovalGroupList.isEmpty()) {
				finJournalApprovalGroup = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
				finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);

				finJournalApprovalGroup.setFinJournal(finJournalRefund);

				finJournalApprovalGroup.setJournalGroupDesc("Balancing Journal for:" + sdf.format(new Date()));
				finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(System.currentTimeMillis()));

				// Persist the journal group
				//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postBalancingJournalTransaction:: finJournalApprovalGroup not attached, calling save.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			} else {
				finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			}

			/*
			 * Create the balancing journal transaction for against OVER/LESS
			 * PAYMENT for the sales journal (may be a DEBIT or CREDIT).
			 */
			FinJournalTransaction sjBalancingJournalTransaction = new FinJournalTransaction();
			sjBalancingJournalTransaction
					.setComments("Balancing for SJ Amount:"
							+ salesJournalTransaction.getTransactionAmount()
							+ " CR Amount:"
							+ cashReceivedJournalTransaction
									.getTransactionAmount());
			sjBalancingJournalTransaction
					.setCreditDebitFlag(salesJournalTransaction
							.getTransactionAmount().compareTo(
									cashReceivedJournalTransaction
											.getTransactionAmount()) > 0);
			sjBalancingJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountOverLessPayment = new FinAccount();
			finAccountOverLessPayment.setAccountId(FinAccountConstants.FIN_ACCOUNT_7140001.id());
			sjBalancingJournalTransaction.setFinAccount(finAccountOverLessPayment);

			sjBalancingJournalTransaction.setFinJournal(finJournalRefund);
			sjBalancingJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());

			FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
			finTransactionStatus.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
			sjBalancingJournalTransaction.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionType = new FinTransactionType();
			finTransactionType.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_OVER_LESS_PAYMENT.id());
			sjBalancingJournalTransaction.setFinTransactionType(finTransactionType);

			sjBalancingJournalTransaction
					.setTransactionAmount((salesJournalTransaction
							.getTransactionAmount()
							.subtract(cashReceivedJournalTransaction
									.getTransactionAmount())).abs());
			sjBalancingJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			sjBalancingJournalTransaction
					.setWcsOrderID(cashReceivedJournalTransaction
							.getWcsOrderID() != null ? cashReceivedJournalTransaction
							.getWcsOrderID() : null);
			sjBalancingJournalTransaction
					.setVenBank(cashReceivedJournalTransaction.getVenBank());
			// Persist the SJ balancing journal transaction
			//sjBalancingJournalTransaction = journalTransactionHome.persistFinJournalTransaction(sjBalancingJournalTransaction);
			if (!em.contains(sjBalancingJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postBalancingJournalTransaction:: sjBalancingJournalTransaction not attached, calling save.");
				sjBalancingJournalTransaction = finJournalTransactionDAO.save(sjBalancingJournalTransaction);
			}
			transactionList.add(sjBalancingJournalTransaction);

			/*
			 * Create the balancing journal transaction for against OVER/LESS
			 * PAYMENT for the cash received journal (may be a DEBIT or CREDIT).
			 */
			FinJournalTransaction crBalancingJournalTransaction = new FinJournalTransaction();
			crBalancingJournalTransaction
					.setComments("Balancing for SJ Amount:"
							+ salesJournalTransaction.getTransactionAmount()
							+ " CR Amount:"
							+ cashReceivedJournalTransaction
									.getTransactionAmount());
			crBalancingJournalTransaction
					.setCreditDebitFlag(salesJournalTransaction
							.getTransactionAmount().compareTo(
									cashReceivedJournalTransaction
											.getTransactionAmount()) < 0);
			crBalancingJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);

			crBalancingJournalTransaction.setFinAccount(finAccountOverLessPayment);

			crBalancingJournalTransaction.setFinJournal(finJournalRefund);
			crBalancingJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());

			crBalancingJournalTransaction.setFinTransactionStatus(finTransactionStatus);

			crBalancingJournalTransaction.setFinTransactionType(finTransactionType);

			crBalancingJournalTransaction
					.setTransactionAmount((salesJournalTransaction
							.getTransactionAmount()
							.subtract(cashReceivedJournalTransaction
									.getTransactionAmount())).abs());
			crBalancingJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

			crBalancingJournalTransaction
					.setWcsOrderID(cashReceivedJournalTransaction
							.getWcsOrderID() != null ? cashReceivedJournalTransaction
							.getWcsOrderID() : null);
			crBalancingJournalTransaction
					.setVenBank(cashReceivedJournalTransaction.getVenBank());
			// Persist the SJ balancing journal transaction
			//crBalancingJournalTransaction = journalTransactionHome.persistFinJournalTransaction(crBalancingJournalTransaction);
			if (!em.contains(crBalancingJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postBalancingJournalTransaction:: crBalancingJournalTransaction not attached, calling save explcitly.");
				crBalancingJournalTransaction = finJournalTransactionDAO.save(crBalancingJournalTransaction);
			}
			transactionList.add(crBalancingJournalTransaction);

			/*
			 * Note that we do not do anything with the transactionList here
			 */

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting balancing refund/others journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postBalancingJournalTransaction::" + " completed in "
				+ duration + "ms");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postLogisticsDebtAcknowledgementJournalTransactions(ArrayList<Long>
	 * logInvoiceReconRecordId)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postLogisticsDebtAcknowledgementJournalTransaction(
			FinApInvoice finApInvoice, LogAirwayBill logAirwayBill) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();
		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();
		//StringBuilder sb;

		try {
			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");

			VenOrderItemAdjustmentSessionEJBLocal orderItemAdjustmentHome = (VenOrderItemAdjustmentSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderItemAdjustmentSessionEJBLocal.class,
							"VenOrderItemAdjustmentSessionEJBBeanLocal");

			LogProviderAgreementSessionEJBLocal providerAgreementHome = (LogProviderAgreementSessionEJBLocal) this._genericLocator
					.lookupLocal(LogProviderAgreementSessionEJBLocal.class,
							"LogProviderAgreementSessionEJBBeanLocal");
			*/

			/*
			 * Lookup the logistics debt acknowledgement journal approval group
			 * for the day If there is none then create it
			 */
			FinJournal finJournalLogisticsDebtAcknowledgement = new FinJournal();
			finJournalLogisticsDebtAcknowledgement.setJournalId(FinJournalConstants.FIN_JOURNAL_LOGISTICS_DEBT_ACKNOWLEDGEMENT.id());
			FinJournalApprovalGroup finJournalApprovalGroup = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

			/*
			sb = new StringBuilder();
			sb.append(
					"select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Logistics Debt Acknowledgement Journal for:")
					.append(sdf.format(new Date()))
					.append("' and o.finApprovalStatus.approvalStatusId = ")
					.append(VeniceConstants.FIN_APPROVAL_STATUS_APPROVED);
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = journalApprovalGroupHome
					.queryByRange(sb.toString(), 0, 0);
			*/
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = finJournalApprovalGroupDAO.findByDescriptionAndApprovalStatusId(
					"Logistics Debt Acknowledgement Journal for:" + sdf.format(new Date()), FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());

			if (finJournalApprovalGroupList.isEmpty()) {
				finJournalApprovalGroup = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
				finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);

				finJournalApprovalGroup.setFinJournal(finJournalLogisticsDebtAcknowledgement);
				finJournalApprovalGroup.setJournalGroupDesc("Logistics Debt Acknowledgement Journal for:" + sdf.format(new Date()));
				finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(System.currentTimeMillis()));

				// Persist the journal group
				//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction:: finJournalApprovalGroup not attached. calling save.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			} else {
				finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			}

			/*
			 * case-case yang akan dibuat jurnal invoice nya: AWB level Shipping
			 * ============== ============ 1. Main Airwaybill Free Shipping 2.
			 * Main Airwaybill Normal Shipping 3. Reference Airwaybill Cost by
			 * Merchant 4. Reference Airwaybill Cost by Customer 5. Reference
			 * Airwaybill Cost by Blibli
			 */

			/*
			 * get airwaybill transaction
			 */
			AirwayBillTransaction awbVeniceTran = null;
			String awbLevel = null, logisticProvider = null, gdnRef = null;// ,
																			// penanggung=null;
			AirwayBillEngineConnector awbConn = new AirwayBillEngineClientConnector();

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::getting airwaybill transaction from AWB engine");

			/*
			 * -buat jurnal untuk pembalik dari jurnal sales (harga yang
			 * dibebankan ke customer) -airwaybill pertama cek di
			 * venice/airwaybill engine dengan sequence 1 hasilnya seharusnya
			 * sama, airwaybill terakhir cek di airwaybill engine (jika ada
			 * perubahan logistic provider). -logistic provider airwaybill
			 * pertama pasti yang dipilih customer ketika belanja (tidak mungkin
			 * blibli messenger), kecuali jika ada perubahan provider. -hanya
			 * buat jurnal jika logistic provider bukan blibli messenger.
			 */
			VenOrderItem item = logAirwayBill.getVenOrderItem();
			String wcsOrderItemId = item.getWcsOrderItemId();
			List<AirwayBillTransaction> awbVeniceTranList = awbConn.getAirwayBillTransactionByItem(wcsOrderItemId);

			if (awbVeniceTranList != null && !awbVeniceTranList.isEmpty()) {
				List<FinApInvoice> finApInvoices = new ArrayList<FinApInvoice>();
				finApInvoices.add(finApInvoice);

				awbVeniceTran = awbVeniceTranList.get(awbVeniceTranList.size() - 1);
				/*
				 * check airwaybill level and logistic provider
				 */
				awbLevel = awbVeniceTran.getLevel();
				logisticProvider = awbVeniceTran.getKodeLogistik();
				// penanggung = awbVeniceTran.getPenanggungBiaya();
				gdnRef = awbVeniceTran.getGdnRef();
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::awbLevel: " + awbLevel);
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::logisticProvider: " + logisticProvider);
				/*
				List<LogProviderAgreement> provAgreement = providerAgreementHome
						.queryByRange(
								"select o from LogProviderAgreement o where o.logLogisticsProvider.logisticsProviderCode = '"
										+ logisticProvider + "'", 0, 1);
				*/
				List<LogProviderAgreement> provAgreement = logProviderAgreementDAO.findByLogLogisticsProviderCode(logisticProvider);
				if (logisticProvider != null
						&& !logisticProvider.equalsIgnoreCase("MSG")) {
					boolean freeShippingFlag = false;
					boolean blibliMessengerFlag = false;
					BigDecimal initialTotalCharge, approvedTotalCharge, discountMultiplier, vatMultiplier, chargedPrice;

					discountMultiplier = (BigDecimal.ONE).subtract(BigDecimal
							.valueOf(awbConn
									.getAllLogisticProvider(logisticProvider)
									.getList().get(0).getDiscountPercentage()));
					vatMultiplier = (BigDecimal.valueOf(100).add(provAgreement
							.get(0).getPpnPercentage())).divide(BigDecimal
							.valueOf(100));

					chargedPrice = logAirwayBill.getPackageWeight().multiply(
							logAirwayBill.getPricePerKg());

					/*
					initialTotalCharge = journalTransactionHome
							.queryByRange(
									"select o from FinJournalTransaction o where o.comments like '%"
											+ wcsOrderItemId
											+ "' and o.finTransactionType.transactionTypeId = "
											+ VeniceConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK,
									0, 1).get(0).getTransactionAmount();
					*/
					List<FinJournalTransaction> finJournalTransactions = finJournalTransactionDAO.findByCommentsAndTransactionTypeId(wcsOrderItemId,
							FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
					if (finJournalTransactions == null || finJournalTransactions.isEmpty()) {
						throw new EJBException(
								"The journal transaction with comment contains WCS Order Item ID '" + wcsOrderItemId + "' and Transaction Type 'Biaya Harus Dibayar Logistik' " +
						        "cannot be found.");										
					}
					initialTotalCharge = finJournalTransactions.get(0).getTransactionAmount();

					approvedTotalCharge = (discountMultiplier
							.multiply(chargedPrice).multiply(vatMultiplier))
							.add(logAirwayBill.getInsuranceCharge());

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Total shipping cost: " + approvedTotalCharge);

					/*
					List<VenOrderItemAdjustment> venOrderItemAdjustmentList = orderItemAdjustmentHome
							.queryByRange(
									"select o from VenOrderItemAdjustment o where o.venOrderItem.orderItemId = "
											+ item.getOrderItemId(), 0, 0);
					*/
					List<VenOrderItemAdjustment> venOrderItemAdjustmentList = venOrderItemAdjustmentDAO.findByVenOrderItem(item);

					if (venOrderItemAdjustmentList.size() > 0) {
						for (VenOrderItemAdjustment adjustment : venOrderItemAdjustmentList) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Adjustment: "
									+ adjustment.getVenPromotion()
											.getPromotionId()
									+ ","
									+ adjustment.getVenOrderItem()
											.getOrderItemId() + " : "
									+ adjustment.getAmount());

							// if shipping cost+insurance cost match the
							// adjustment, then it is free shipping
							if (adjustment.getVenPromotion().getPromotionName()
									.toLowerCase().contains("free shipping")) {
								freeShippingFlag = true;
								break;
							}
						}
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::freeShippingFlag: " + freeShippingFlag);

					/*
					 * case 1: AWB level Shipping ============== ============
					 * -Main Airwaybill Free Shipping
					 * 
					 * create debit biaya YMHD create kredit SM Promo
					 * 
					 * create debit SM Promo create kredit Hutang Logistik
					 */
					if ((awbLevel != null && awbLevel.equalsIgnoreCase("OM"))
							&& freeShippingFlag == true) {
						CommonUtil.logInfo(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::case 1: Main Airwaybill & Free Shipping");
						/*
						 * Create the DEBIT journal transaction for against
						 * Biaya YMHD.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the DEBIT journal transaction for against Biaya YMHD");
						FinJournalTransaction biayaYmhdJournalTransaction = new FinJournalTransaction();
						biayaYmhdJournalTransaction
								.setComments("Biaya YMHD DEBIT Amount:"
										+ initialTotalCharge + " for Invoice:"
										+ finApInvoice.getApInvoiceId());
						biayaYmhdJournalTransaction.setCreditDebitFlag(false);
						biayaYmhdJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
						biayaYmhdJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						biayaYmhdJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						biayaYmhdJournalTransaction.setTransactionAmount(initialTotalCharge);
						biayaYmhdJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
						biayaYmhdJournalTransaction.setWcsOrderID(gdnRef);
						biayaYmhdJournalTransaction.setFinApInvoices(finApInvoices);

						FinAccount finAccountYMHD = new FinAccount();
						finAccountYMHD.setAccountId(FinAccountConstants.FIN_ACCOUNT_2210002.id());
						biayaYmhdJournalTransaction.setFinAccount(finAccountYMHD);

						FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
						finTransactionStatus.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
						biayaYmhdJournalTransaction.setFinTransactionStatus(finTransactionStatus);

						FinTransactionType finTransactionTypeYMHD = new FinTransactionType();
						finTransactionTypeYMHD.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
						biayaYmhdJournalTransaction.setFinTransactionType(finTransactionTypeYMHD);

						// biayaYmhdJournalTransaction =
						// journalTransactionHome.persistFinJournalTransaction(biayaYmhdJournalTransaction);
						transactionList.add(biayaYmhdJournalTransaction);

						/*
						 * Create the CREDIT transaction for against SM Promo.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the CREDIT transaction for against SM Promo");
						FinJournalTransaction smPromoJournalTransaction = new FinJournalTransaction();
						smPromoJournalTransaction
								.setComments("SM Promo CREDIT Amount:"
										+ initialTotalCharge + " for Invoice:"
										+ finApInvoice.getApInvoiceId());
						smPromoJournalTransaction.setCreditDebitFlag(true);
						smPromoJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
						smPromoJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						smPromoJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						smPromoJournalTransaction.setFinTransactionStatus(finTransactionStatus);
						smPromoJournalTransaction.setTransactionAmount(initialTotalCharge);
						smPromoJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
						smPromoJournalTransaction.setWcsOrderID(gdnRef);
						smPromoJournalTransaction.setFinApInvoices(finApInvoices);

						FinAccount finAccountSMPromo = new FinAccount();
						finAccountSMPromo.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133018.id());
						smPromoJournalTransaction
								.setFinAccount(finAccountSMPromo);

						FinTransactionType finTransactionTypeSMPromo = new FinTransactionType();
						finTransactionTypeSMPromo.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_BEBAN_PROMOSI_BEBAS_BIAYA_KIRIM.id());
						smPromoJournalTransaction.setFinTransactionType(finTransactionTypeSMPromo);

						// smPromoJournalTransaction =
						// journalTransactionHome.persistFinJournalTransaction(smPromoJournalTransaction);
						transactionList.add(smPromoJournalTransaction);

						/*
						 * Create the DEBIT journal transaction for against SM
						 * Promo.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the DEBIT transaction for against SM Promo");
						FinJournalTransaction smPromoJournalTransaction2 = new FinJournalTransaction();
						smPromoJournalTransaction2
								.setComments("SM Promo DEBIT Amount:"
										+ approvedTotalCharge + " for Invoice:"
										+ finApInvoice.getApInvoiceId());
						smPromoJournalTransaction2.setCreditDebitFlag(false);
						smPromoJournalTransaction2.setFinJournalApprovalGroup(finJournalApprovalGroup);
						smPromoJournalTransaction2.setFinAccount(finAccountSMPromo);
						smPromoJournalTransaction2.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						smPromoJournalTransaction2.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						smPromoJournalTransaction2.setFinTransactionStatus(finTransactionStatus);
						smPromoJournalTransaction2.setFinTransactionType(finTransactionTypeSMPromo);
						smPromoJournalTransaction2.setTransactionAmount(approvedTotalCharge);
						smPromoJournalTransaction2.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
						smPromoJournalTransaction2.setWcsOrderID(gdnRef);
						smPromoJournalTransaction2.setFinApInvoices(finApInvoices);
						// smPromoJournalTransaction2 =
						// journalTransactionHome.persistFinJournalTransaction(smPromoJournalTransaction2);
						transactionList.add(smPromoJournalTransaction2);

						/*
						 * Create the CREDIT transaction for against Hutang
						 * Logistik.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the CREDIT transaction for against Hutang Logistik");
						FinJournalTransaction hutangLogisticJournalTransaction = new FinJournalTransaction();
						hutangLogisticJournalTransaction
								.setComments("Hutang Logistik CREDIT Amount:"
										+ approvedTotalCharge + " for Invoice:"
										+ finApInvoice.getApInvoiceId());
						hutangLogisticJournalTransaction
								.setCreditDebitFlag(true);
						hutangLogisticJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
						hutangLogisticJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						hutangLogisticJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						hutangLogisticJournalTransaction.setFinTransactionStatus(finTransactionStatus);
						hutangLogisticJournalTransaction.setTransactionAmount(approvedTotalCharge);
						hutangLogisticJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
						hutangLogisticJournalTransaction.setWcsOrderID(gdnRef);
						hutangLogisticJournalTransaction.setFinApInvoices(finApInvoices);

						FinAccount finAccountHutangLogistics = new FinAccount();
						finAccountHutangLogistics.setAccountId(FinAccountConstants.FIN_ACCOUNT_2140001.id());
						hutangLogisticJournalTransaction.setFinAccount(finAccountHutangLogistics);

						FinTransactionType finTransactionTypeHutangLogistic = new FinTransactionType();
						finTransactionTypeHutangLogistic.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_LOGISTICS_PAYMENT.id());
						hutangLogisticJournalTransaction.setFinTransactionType(finTransactionTypeHutangLogistic);

						// hutangLogisticJournalTransaction =
						// journalTransactionHome.persistFinJournalTransaction(hutangLogisticJournalTransaction);
						transactionList.add(hutangLogisticJournalTransaction);
					} /*
					 * case 2: AWB level Shipping ============== ============
					 * -Main Airwaybill Normal Shipping
					 * 
					 * create debit biaya YMHD create kredit HPP
					 * 
					 * create debit HPP create kredit Hutang Logistik
					 */else if ((awbLevel != null && awbLevel
							.equalsIgnoreCase("OM"))
							&& freeShippingFlag == false) {
						CommonUtil.logInfo(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::case 2: Main Airwaybill & Normal Shipping");
						/*
						 * Create the DEBIT journal transaction for against
						 * Biaya YMHD.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the DEBIT transaction for against Biaya YMHD");
						FinJournalTransaction biayaYmhdJournalTransaction = new FinJournalTransaction();
						biayaYmhdJournalTransaction
								.setComments("Biaya YMHD DEBIT Amount:"
										+ initialTotalCharge + " for Invoice:"
										+ finApInvoice.getApInvoiceId());
						biayaYmhdJournalTransaction.setCreditDebitFlag(false);
						biayaYmhdJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
						biayaYmhdJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						biayaYmhdJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						biayaYmhdJournalTransaction.setTransactionAmount(initialTotalCharge);
						biayaYmhdJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
						biayaYmhdJournalTransaction.setWcsOrderID(gdnRef);
						biayaYmhdJournalTransaction.setFinApInvoices(finApInvoices);

						FinAccount finAccountYMHD = new FinAccount();
						finAccountYMHD.setAccountId(FinAccountConstants.FIN_ACCOUNT_2210002.id());
						biayaYmhdJournalTransaction.setFinAccount(finAccountYMHD);

						FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
						finTransactionStatus.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
						biayaYmhdJournalTransaction.setFinTransactionStatus(finTransactionStatus);

						FinTransactionType finTransactionTypeYMHD = new FinTransactionType();
						finTransactionTypeYMHD.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
						biayaYmhdJournalTransaction.setFinTransactionType(finTransactionTypeYMHD);

						transactionList.add(biayaYmhdJournalTransaction);

						/*
						 * Create the CREDIT transaction for against HPP.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the CREDIT transaction for against HPP");
						FinJournalTransaction hppJournalTransaction = new FinJournalTransaction();
						hppJournalTransaction.setComments("HPP CREDIT Amount:"
								+ initialTotalCharge + "  for Invoice:"
								+ finApInvoice.getApInvoiceId());
						hppJournalTransaction.setCreditDebitFlag(true);
						hppJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
						hppJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						hppJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
						hppJournalTransaction.setFinTransactionStatus(finTransactionStatus);
						hppJournalTransaction.setTransactionAmount(initialTotalCharge);
						hppJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
						hppJournalTransaction.setWcsOrderID(gdnRef);
						hppJournalTransaction.setFinApInvoices(finApInvoices);

						FinAccount finAccountHpp = new FinAccount();
						finAccountHpp.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110001.id());
						hppJournalTransaction.setFinAccount(finAccountHpp);

						FinTransactionType finTransactionTypeHpp = new FinTransactionType();
						finTransactionTypeHpp.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HPP_BIAYA_BARANG.id());
						hppJournalTransaction.setFinTransactionType(finTransactionTypeHpp);

						transactionList.add(hppJournalTransaction);

						/*
						 * Create the DEBIT journal transaction for against HPP.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the DEBIT transaction for against HPP");
						FinJournalTransaction hppJournalTransaction2 = new FinJournalTransaction();
						hppJournalTransaction2.setComments("HPP DEBIT Amount:"
								+ approvedTotalCharge + " for Invoice:"
								+ finApInvoice.getApInvoiceId());
						hppJournalTransaction2.setCreditDebitFlag(false);
						hppJournalTransaction2
								.setFinJournalApprovalGroup(finJournalApprovalGroup);
						hppJournalTransaction2.setFinAccount(finAccountHpp);
						hppJournalTransaction2
								.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						hppJournalTransaction2.setFinPeriod(FinancePeriodUtil
								.getCurrentPeriod());
						hppJournalTransaction2
								.setFinTransactionStatus(finTransactionStatus);
						hppJournalTransaction2
								.setFinTransactionType(finTransactionTypeHpp);
						hppJournalTransaction2
								.setTransactionAmount(approvedTotalCharge);
						hppJournalTransaction2
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));
						hppJournalTransaction2.setWcsOrderID(gdnRef);
						hppJournalTransaction2.setFinApInvoices(finApInvoices);

						transactionList.add(hppJournalTransaction2);

						/*
						 * Create the CREDIT transaction for against Hutang
						 * Logistik.
						 */
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::Create the CREDIT transaction for against Hutang Logistik");
						FinJournalTransaction hutangLogisticJournalTransaction = new FinJournalTransaction();
						hutangLogisticJournalTransaction
								.setComments("Hutang Logistik CREDIT Amount:"
										+ approvedTotalCharge
										+ "  for Invoice:"
										+ finApInvoice.getApInvoiceId());
						hutangLogisticJournalTransaction
								.setCreditDebitFlag(true);
						hutangLogisticJournalTransaction
								.setFinJournalApprovalGroup(finJournalApprovalGroup);
						hutangLogisticJournalTransaction
								.setFinJournal(finJournalLogisticsDebtAcknowledgement);
						hutangLogisticJournalTransaction
								.setFinPeriod(FinancePeriodUtil
										.getCurrentPeriod());
						hutangLogisticJournalTransaction
								.setFinTransactionStatus(finTransactionStatus);
						hutangLogisticJournalTransaction
								.setTransactionAmount(approvedTotalCharge);
						hutangLogisticJournalTransaction
								.setTransactionTimestamp(new Timestamp(System
										.currentTimeMillis()));
						hutangLogisticJournalTransaction.setWcsOrderID(gdnRef);
						hutangLogisticJournalTransaction
								.setFinApInvoices(finApInvoices);

						FinAccount finAccountHutangLogistics = new FinAccount();
						finAccountHutangLogistics
								.setAccountId(FinAccountConstants.FIN_ACCOUNT_2140001.id());
						hutangLogisticJournalTransaction
								.setFinAccount(finAccountHutangLogistics);

						FinTransactionType finTransactionTypeHutangLogistic = new FinTransactionType();
						finTransactionTypeHutangLogistic
								.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_LOGISTICS_PAYMENT.id());
						hutangLogisticJournalTransaction
								.setFinTransactionType(finTransactionTypeHutangLogistic);

						transactionList.add(hutangLogisticJournalTransaction);
					}

					/*
					 * Comment out first for it's not implemented yet
					 */

					/*
					 * case 3: AWB level Shipping ============== ============
					 * -Reference Airwaybill Cost by Merchant
					 * 
					 * create debit biaya YMHD create kredit HPP
					 * 
					 * create debit HPP create kredit Hutang Logistik
					 */
					// else if((awbLevel !=null &&
					// awbLevel.equalsIgnoreCase("OR")) && (penanggung!=null &&
					// penanggung.equalsIgnoreCase("merchant"))){
					// _log.info("case 3: Reference Airwaybill & Cost by Merchant");
					// /*
					// * Create the DEBIT journal transaction for against Biaya
					// YMHD.
					// */
					// _log.debug("Create the DEBIT transaction for against Biaya YMHD");
					// FinJournalTransaction biayaYmhdJournalTransaction = new
					// FinJournalTransaction();
					// biayaYmhdJournalTransaction.setComments("Biaya YMHD DEBIT Amount:"
					// + finApInvoice.getInvoiceAmount() + " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// biayaYmhdJournalTransaction.setCreditDebitFlag(false);
					// biayaYmhdJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountYMHD = new FinAccount();
					// finAccountYMHD.setAccountId(VeniceConstants.FIN_ACCOUNT_2210002);
					// biayaYmhdJournalTransaction.setFinAccount(finAccountYMHD);
					// biayaYmhdJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// biayaYmhdJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// FinTransactionStatus finTransactionStatus = new
					// FinTransactionStatus();
					// finTransactionStatus.setTransactionStatusId(VeniceConstants.FIN_TRANSACTION_STATUS_NEW);
					// biayaYmhdJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeYMHD = new
					// FinTransactionType();
					// finTransactionTypeYMHD.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK);
					// biayaYmhdJournalTransaction.setFinTransactionType(finTransactionTypeYMHD);
					// biayaYmhdJournalTransaction.setTransactionAmount(finApInvoice.getInvoiceAmount());
					// biayaYmhdJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// biayaYmhdJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // biayaYmhdJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(biayaYmhdJournalTransaction);
					// transactionList.add(biayaYmhdJournalTransaction);
					//
					// /*
					// * Create the CREDIT transaction for against HPP.
					// */
					// _log.debug("Create the CREDIT transaction for against HPP");
					// FinJournalTransaction hppJournalTransaction = new
					// FinJournalTransaction();
					// hppJournalTransaction.setComments("HPP CREDIT Amount:" +
					// finApInvoice.getInvoiceAmount() + "  for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hppJournalTransaction.setCreditDebitFlag(true);
					// hppJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountHpp = new FinAccount();
					// finAccountHpp.setAccountId(VeniceConstants.FIN_ACCOUNT_5110001);
					// hppJournalTransaction.setFinAccount(finAccountHpp);
					// hppJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hppJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hppJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeHpp = new
					// FinTransactionType();
					// finTransactionTypeHpp.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_HPP_BIAYA_BARANG);
					// hppJournalTransaction.setFinTransactionType(finTransactionTypeHpp);
					// hppJournalTransaction.setTransactionAmount(finApInvoice.getInvoiceAmount());
					// hppJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hppJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hppJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(hppJournalTransaction);
					// transactionList.add(hppJournalTransaction);
					//
					// /*
					// * cek ke airwaybill engine, jika ada airwaybill record
					// lain, berarti ada perubahan logistic provider, ambil yang
					// level OR dan tidak di void
					// */
					// _log.debug("check for another airwaybill transaction from airwaybill engine.");
					// List<AirwayBillTransaction> awbList =
					// awbConn.getAirwayBillTransactionByItem(wcsOrderItemId);
					//
					// _log.debug("awb size: "+awbList.size());
					// //jika hanya ada 1 awb di airwaybill engine, berarti
					// tidak ada perubahan logistic provider, tidak perlu buat
					// jurnal lanjutan.
					// if(awbList.size()<=1){
					// _log.info("there is only 1 airwaybill transaction, no need to create another journal transaction.");
					// }else{
					// if(awbList.get(awbList.size()-1).getKodeLogistik().equalsIgnoreCase("MSG")){
					// _log.info("another active main airwaybill found and logistic provider is blibli messenger.");
					// blibliMessengerFlag=true;
					// }else{
					// if(awbList.get(awbList.size()-1).getLevel().equalsIgnoreCase("OR")
					// && awbList.get(awbList.size()-1).getVoidDate()==null){
					// _log.info("another active main airwaybill and logistic provider not blibli messenger found.");
					// blibliMessengerFlag=false;
					//
					// AirwayBillTransaction awbEngineTran =
					// awbList.get(awbList.size()-1);
					// _log.info("create journal entry for another airwaybill from airwaybill engine");
					// /*
					// * Create the DEBIT journal transaction for against HPP.
					// */
					// _log.debug("Create the DEBIT transaction for against HPP");
					// FinJournalTransaction hppJournalTransaction2 = new
					// FinJournalTransaction();
					// hppJournalTransaction2.setComments("HPP DEBIT Amount:" +
					// finApInvoice.getInvoiceAmount() + " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hppJournalTransaction2.setCreditDebitFlag(false);
					// hppJournalTransaction2.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// hppJournalTransaction2.setFinAccount(finAccountHpp);
					// hppJournalTransaction2.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hppJournalTransaction2.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hppJournalTransaction2.setFinTransactionStatus(finTransactionStatus);
					// hppJournalTransaction2.setFinTransactionType(finTransactionTypeHpp);
					// BigDecimal totalCharge =
					// awbEngineTran.getShippingCost().add(awbEngineTran.getInsuranceCost());
					// hppJournalTransaction2.setTransactionAmount(totalCharge);
					// hppJournalTransaction2.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hppJournalTransaction2.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hppJournalTransaction2 =
					// journalTransactionHome.persistFinJournalTransaction(hppJournalTransaction2);
					// transactionList.add(hppJournalTransaction2);
					//
					// /*
					// * Create the CREDIT transaction for against Hutang
					// Logistik.
					// */
					// _log.debug("Create the CREDIT transaction for against Hutang Logistik");
					// FinJournalTransaction hutangLogisticJournalTransaction =
					// new FinJournalTransaction();
					// hutangLogisticJournalTransaction.setComments("Hutang Logistik CREDIT Amount:"
					// + finApInvoice.getInvoiceAmount() + "  for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hutangLogisticJournalTransaction.setCreditDebitFlag(true);
					// hutangLogisticJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountHutangLogistics = new FinAccount();
					// finAccountHutangLogistics.setAccountId(VeniceConstants.FIN_ACCOUNT_2140001);
					// hutangLogisticJournalTransaction.setFinAccount(finAccountHutangLogistics);
					// hutangLogisticJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hutangLogisticJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hutangLogisticJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeHutangLogistic = new
					// FinTransactionType();
					// finTransactionTypeHutangLogistic.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_LOGISTICS_PAYMENT);
					// hutangLogisticJournalTransaction.setFinTransactionType(finTransactionTypeHutangLogistic);
					// hutangLogisticJournalTransaction.setTransactionAmount(totalCharge);
					// hutangLogisticJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hutangLogisticJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hutangLogisticJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(hutangLogisticJournalTransaction);
					// transactionList.add(hutangLogisticJournalTransaction);
					// }
					// }
					// }
					// }
					//
					// /*
					// * case 4:
					// * AWB level Shipping
					// * ============== ============
					// * -Reference Airwaybill Cost by Customer
					// *
					// * create debit biaya YMHD
					// * create kredit HPP
					// *
					// * create debit HPP
					// * create kredit Hutang Logistik
					// */
					// else if((awbLevel !=null &&
					// awbLevel.equalsIgnoreCase("OR")) && (penanggung!=null &&
					// penanggung.equalsIgnoreCase("customer"))){
					// _log.info("case 4: Reference Airwaybill & Cost by Customer");
					// /*
					// * Create the DEBIT journal transaction for against Biaya
					// YMHD.
					// */
					// _log.debug("Create the DEBIT transaction for against Biaya YMHD");
					// FinJournalTransaction biayaYmhdJournalTransaction = new
					// FinJournalTransaction();
					// biayaYmhdJournalTransaction.setComments("Biaya YMHD DEBIT Amount:"
					// + finApInvoice.getInvoiceAmount() + " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// biayaYmhdJournalTransaction.setCreditDebitFlag(false);
					// biayaYmhdJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountYMHD = new FinAccount();
					// finAccountYMHD.setAccountId(VeniceConstants.FIN_ACCOUNT_2210002);
					// biayaYmhdJournalTransaction.setFinAccount(finAccountYMHD);
					// biayaYmhdJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// biayaYmhdJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// FinTransactionStatus finTransactionStatus = new
					// FinTransactionStatus();
					// finTransactionStatus.setTransactionStatusId(VeniceConstants.FIN_TRANSACTION_STATUS_NEW);
					// biayaYmhdJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeYMHD = new
					// FinTransactionType();
					// finTransactionTypeYMHD.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK);
					// biayaYmhdJournalTransaction.setFinTransactionType(finTransactionTypeYMHD);
					// biayaYmhdJournalTransaction.setTransactionAmount(finApInvoice.getInvoiceAmount());
					// biayaYmhdJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// biayaYmhdJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // biayaYmhdJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(biayaYmhdJournalTransaction);
					// transactionList.add(biayaYmhdJournalTransaction);
					//
					// /*
					// * Create the CREDIT transaction for against HPP.
					// */
					// _log.debug("Create the CREDIT transaction for against Biaya HPP");
					// FinJournalTransaction hppJournalTransaction = new
					// FinJournalTransaction();
					// hppJournalTransaction.setComments("HPP CREDIT Amount:" +
					// finApInvoice.getInvoiceAmount() + "  for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hppJournalTransaction.setCreditDebitFlag(true);
					// hppJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountHpp = new FinAccount();
					// finAccountHpp.setAccountId(VeniceConstants.FIN_ACCOUNT_5110001);
					// hppJournalTransaction.setFinAccount(finAccountHpp);
					// hppJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hppJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hppJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeHpp = new
					// FinTransactionType();
					// finTransactionTypeHpp.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_HPP_BIAYA_BARANG);
					// hppJournalTransaction.setFinTransactionType(finTransactionTypeHpp);
					// hppJournalTransaction.setTransactionAmount(finApInvoice.getInvoiceAmount());
					// hppJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hppJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hppJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(hppJournalTransaction);
					// transactionList.add(hppJournalTransaction);
					//
					// /*
					// * cek ke airwaybill engine, jika ada airwaybill record
					// lain, berarti ada perubahan logistic provider, ambil yang
					// level OR dan tidak di void
					// */
					// _log.debug("check for another airwaybill transaction from airwaybill engine.");
					// List<AirwayBillTransaction> awbList =
					// awbConn.getAirwayBillTransactionByItem(wcsOrderItemId);
					//
					// _log.debug("awb size: "+awbList.size());
					// //jika hanya ada 1 awb di airwaybill engine, berarti
					// tidak ada perubahan logistic provider, tidak perlu buat
					// jurnal lanjutan.
					// if(awbList.size()<=1){
					// _log.info("there is only 1 airwaybill transaction, no need to create another journal transaction.");
					// }else{
					// if(awbList.get(awbList.size()-1).getKodeLogistik().equalsIgnoreCase("MSG")){
					// _log.info("another active main airwaybill found and logistic provider is blibli messenger.");
					// blibliMessengerFlag=true;
					// }else{
					// if(awbList.get(awbList.size()-1).getLevel().equalsIgnoreCase("OR")
					// && awbList.get(awbList.size()-1).getVoidDate()==null){
					// _log.info("another active main airwaybill and logistic provider not blibli messenger found.");
					// blibliMessengerFlag=false;
					//
					// AirwayBillTransaction awbEngineTran =
					// awbList.get(awbList.size()-1);
					// _log.info("create journal entry for another airwaybill from airwaybill engine");
					// /*
					// * Create the DEBIT journal transaction for against HPP.
					// */
					// _log.debug("Create the DEBIT transaction for against HPP");
					// FinJournalTransaction hppJournalTransaction2 = new
					// FinJournalTransaction();
					// hppJournalTransaction2.setComments("HPP DEBIT Amount:" +
					// finApInvoice.getInvoiceAmount() + " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hppJournalTransaction2.setCreditDebitFlag(false);
					// hppJournalTransaction2.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// hppJournalTransaction2.setFinAccount(finAccountHpp);
					// hppJournalTransaction2.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hppJournalTransaction2.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hppJournalTransaction2.setFinTransactionStatus(finTransactionStatus);
					// hppJournalTransaction2.setFinTransactionType(finTransactionTypeHpp);
					// BigDecimal totalCharge =
					// awbEngineTran.getShippingCost().add(awbEngineTran.getInsuranceCost());
					// hppJournalTransaction2.setTransactionAmount(totalCharge);
					// hppJournalTransaction2.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hppJournalTransaction2.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hppJournalTransaction2 =
					// journalTransactionHome.persistFinJournalTransaction(hppJournalTransaction2);
					// transactionList.add(hppJournalTransaction2);
					//
					// /*
					// * Create the CREDIT transaction for against Hutang
					// Logistik.
					// */
					// _log.debug("Create the CREDIT transaction for against Hutang Logistik");
					// FinJournalTransaction hutangLogisticJournalTransaction =
					// new FinJournalTransaction();
					// hutangLogisticJournalTransaction.setComments("Hutang Logistik CREDIT Amount:"
					// + finApInvoice.getInvoiceAmount() + "  for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hutangLogisticJournalTransaction.setCreditDebitFlag(true);
					// hutangLogisticJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountHutangLogistics = new FinAccount();
					// finAccountHutangLogistics.setAccountId(VeniceConstants.FIN_ACCOUNT_2140001);
					// hutangLogisticJournalTransaction.setFinAccount(finAccountHutangLogistics);
					// hutangLogisticJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hutangLogisticJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hutangLogisticJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeHutangLogistic = new
					// FinTransactionType();
					// finTransactionTypeHutangLogistic.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_LOGISTICS_PAYMENT);
					// hutangLogisticJournalTransaction.setFinTransactionType(finTransactionTypeHutangLogistic);
					// hutangLogisticJournalTransaction.setTransactionAmount(totalCharge);
					// hutangLogisticJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hutangLogisticJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hutangLogisticJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(hutangLogisticJournalTransaction);
					// transactionList.add(hutangLogisticJournalTransaction);
					// }
					// }
					// }
					// }
					//
					// /*
					// * case 5:
					// * AWB level Shipping
					// * ============== ============
					// * -Reference Airwaybill Cost by Blibli
					// *
					// * create debit biaya YMHD
					// * create kredit SM Promo
					// *
					// * create debit SM Promo
					// * create kredit Hutang Logistik
					// */
					// else if((awbLevel !=null &&
					// awbLevel.equalsIgnoreCase("OR")) && (penanggung!=null &&
					// penanggung.equalsIgnoreCase("gdn"))){
					// _log.info("case 5: Reference Airwaybill & Cost by Blibli");
					// /*
					// * Create the DEBIT journal transaction for against Biaya
					// YMHD.
					// */
					// _log.debug("Create the DEBIT transaction for against Biaya YMHD");
					// FinJournalTransaction biayaYmhdJournalTransaction = new
					// FinJournalTransaction();
					// biayaYmhdJournalTransaction.setComments("Biaya YMHD DEBIT Amount:"
					// + finApInvoice.getInvoiceAmount() + " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// biayaYmhdJournalTransaction.setCreditDebitFlag(false);
					// biayaYmhdJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountYMHD = new FinAccount();
					// finAccountYMHD.setAccountId(VeniceConstants.FIN_ACCOUNT_2210002);
					// biayaYmhdJournalTransaction.setFinAccount(finAccountYMHD);
					// biayaYmhdJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// biayaYmhdJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// FinTransactionStatus finTransactionStatus = new
					// FinTransactionStatus();
					// finTransactionStatus.setTransactionStatusId(VeniceConstants.FIN_TRANSACTION_STATUS_NEW);
					// biayaYmhdJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeYMHD = new
					// FinTransactionType();
					// finTransactionTypeYMHD.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK);
					// biayaYmhdJournalTransaction.setFinTransactionType(finTransactionTypeYMHD);
					// biayaYmhdJournalTransaction.setTransactionAmount(finApInvoice.getInvoiceAmount());
					// biayaYmhdJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// biayaYmhdJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // biayaYmhdJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(biayaYmhdJournalTransaction);
					// transactionList.add(biayaYmhdJournalTransaction);
					//
					// /*
					// * Create the CREDIT transaction for against SM Promo.
					// */
					// _log.debug("Create the CREDIT transaction for against SM Promo");
					// FinJournalTransaction smPromoJournalTransaction = new
					// FinJournalTransaction();
					// smPromoJournalTransaction.setComments("SM Promo CREDIT Amount:"
					// + finApInvoice.getInvoiceAmount() + "  for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// smPromoJournalTransaction.setCreditDebitFlag(true);
					// smPromoJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountSMPromo = new FinAccount();
					// finAccountSMPromo.setAccountId(VeniceConstants.FIN_ACCOUNT_6133018);
					// smPromoJournalTransaction.setFinAccount(finAccountSMPromo);
					// smPromoJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// smPromoJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// smPromoJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeSMPromo = new
					// FinTransactionType();
					// finTransactionTypeSMPromo.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_SM_BEBAN_PROMOSI_BEBAS_BIAYA_KIRIM);
					// smPromoJournalTransaction.setFinTransactionType(finTransactionTypeSMPromo);
					// smPromoJournalTransaction.setTransactionAmount(finApInvoice.getInvoiceAmount());
					// smPromoJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// smPromoJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // smPromoJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(smPromoJournalTransaction);
					// transactionList.add(smPromoJournalTransaction);
					//
					// /*
					// * cek ke airwaybill engine, jika ada airwaybill record
					// lain, berarti ada perubahan logistic provider, ambil yang
					// level OR dan tidak di void
					// */
					// _log.debug("check for another airwaybill transaction from airwaybill engine.");
					// List<AirwayBillTransaction> awbList =
					// awbConn.getAirwayBillTransactionByItem(wcsOrderItemId);
					//
					// _log.debug("awb size: "+awbList.size());
					// //jika hanya ada 1 awb di airwaybill engine, berarti
					// tidak ada perubahan logistic provider, tidak perlu buat
					// jurnal lanjutan.
					// if(awbList.size()<=1){
					// _log.info("there is only 1 airwaybill transaction, no need to create another journal transaction.");
					// }else{
					// if(awbList.get(awbList.size()-1).getKodeLogistik().equalsIgnoreCase("MSG")){
					// _log.info("another active main airwaybill found and logistic provider is blibli messenger.");
					// blibliMessengerFlag=true;
					// }else{
					// if(awbList.get(awbList.size()-1).getLevel().equalsIgnoreCase("OR")
					// && awbList.get(awbList.size()-1).getVoidDate()==null){
					// _log.info("another active main airwaybill and logistic provider not blibli messenger found.");
					// blibliMessengerFlag=false;
					//
					// AirwayBillTransaction awbEngineTran =
					// awbList.get(awbList.size()-1);
					// _log.info("create journal entry for another airwaybill from airwaybill engine");
					// /*
					// * Create the DEBIT journal transaction for against SM
					// Promo.
					// */
					// _log.debug("Create the DEBIT transaction for against SM Promo");
					// FinJournalTransaction smPromoJournalTransaction2 = new
					// FinJournalTransaction();
					// smPromoJournalTransaction2.setComments("SM Promo DEBIT Amount:"
					// + finApInvoice.getInvoiceAmount() + " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// smPromoJournalTransaction2.setCreditDebitFlag(false);
					// smPromoJournalTransaction2.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// smPromoJournalTransaction2.setFinAccount(finAccountSMPromo);
					// smPromoJournalTransaction2.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// smPromoJournalTransaction2.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// smPromoJournalTransaction2.setFinTransactionStatus(finTransactionStatus);
					// smPromoJournalTransaction2.setFinTransactionType(finTransactionTypeSMPromo);
					// BigDecimal totalCharge =
					// awbEngineTran.getShippingCost().add(awbEngineTran.getInsuranceCost());
					// smPromoJournalTransaction2.setTransactionAmount(totalCharge);
					// smPromoJournalTransaction2.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// smPromoJournalTransaction2.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // smPromoJournalTransaction2 =
					// journalTransactionHome.persistFinJournalTransaction(smPromoJournalTransaction2);
					// transactionList.add(smPromoJournalTransaction2);
					//
					// /*
					// * Create the CREDIT transaction for against Hutang
					// Logistik.
					// */
					// _log.debug("Create the CREDIT transaction for against Hutang Logistik");
					// FinJournalTransaction hutangLogisticJournalTransaction =
					// new FinJournalTransaction();
					// hutangLogisticJournalTransaction.setComments("Hutang Logistik CREDIT Amount:"
					// + finApInvoice.getInvoiceAmount()+ " for Invoice:" +
					// finApInvoice.getApInvoiceId());
					// hutangLogisticJournalTransaction.setCreditDebitFlag(true);
					// hutangLogisticJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// FinAccount finAccountHutangLogistics = new FinAccount();
					// finAccountHutangLogistics.setAccountId(VeniceConstants.FIN_ACCOUNT_2140001);
					// hutangLogisticJournalTransaction.setFinAccount(finAccountHutangLogistics);
					// hutangLogisticJournalTransaction.setFinJournal(finJournalLogisticsDebtAcknowledgement);
					// hutangLogisticJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
					// hutangLogisticJournalTransaction.setFinTransactionStatus(finTransactionStatus);
					// FinTransactionType finTransactionTypeHutangLogistic = new
					// FinTransactionType();
					// finTransactionTypeHutangLogistic.setTransactionTypeId(VeniceConstants.FIN_TRANSACTION_TYPE_LOGISTICS_PAYMENT);
					// hutangLogisticJournalTransaction.setFinTransactionType(finTransactionTypeHutangLogistic);
					// hutangLogisticJournalTransaction.setTransactionAmount(totalCharge);
					// hutangLogisticJournalTransaction.setTransactionTimestamp(new
					// Timestamp(System.currentTimeMillis()));
					// hutangLogisticJournalTransaction.setWcsOrderID(finApInvoice.getApInvoiceId()!=null?finApInvoice.getApInvoiceId().toString():null);
					// // hutangLogisticJournalTransaction =
					// journalTransactionHome.persistFinJournalTransaction(hutangLogisticJournalTransaction);
					// transactionList.add(hutangLogisticJournalTransaction);
					// }
					// }
					// }
					// }
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::transactionList size: "
							+ transactionList.size());
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::blibliMessengerFlag: " + blibliMessengerFlag);
					if (!transactionList.isEmpty()) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::merging transactionList");
						//journalTransactionHome.persistFinJournalTransactionList(transactionList);
						for (FinJournalTransaction journalTransaction : transactionList) {
							if (!em.contains(journalTransaction)) {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction:: journalTransaction not attached, calling save.");
								finJournalTransactionDAO.save(journalTransaction);
							}
						}
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::done merging transactionList");
					} else {
						CommonUtil.logInfo(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::no invoice journal created.");
					}
				} else {
					CommonUtil.logInfo(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransaction::logistic provider is blibli messenger, no invoice journal created.");
				}
			}
		} catch (Exception e) {
			String errMsg = "An Exception occured when posting logistics debt acknowledgement journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postLogisticsDebtAcknowledgementJournalTransactions::"
				+ " completed in " + duration + "ms");

		return true;
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private BigDecimal getNonVoucherAmount(
			List<VenOrderItemAdjustment> venOrderItemAdjustments)
			throws Exception {
		/*
		VenPromotionSessionEJBLocal promotionHome = (VenPromotionSessionEJBLocal) this._genericLocator
				.lookupLocal(VenPromotionSessionEJBLocal.class,
						"VenPromotionSessionEJBBeanLocal");
		*/
		BigDecimal nonVoucherAmount = new BigDecimal(0);

		for (VenOrderItemAdjustment venOrderItemAdjustment : venOrderItemAdjustments) {
			/*
			List<VenPromotion> venPromotionList = promotionHome
					.queryByRange(
							"select o from VenPromotion o left join fetch o.venPromotionType where o.promotionId = "
									+ venOrderItemAdjustment.getVenPromotion()
											.getPromotionId(), 0, 1);
			*/
			List<VenPromotion> venPromotionList = venPromotionDAO.findByPromotionIdLeftJoinPromotionType(
					venOrderItemAdjustment.getVenPromotion().getPromotionId());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "getNonVoucherAmount::Promotion Id "
					+ venOrderItemAdjustment.getVenPromotion().getPromotionId());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "getNonVoucherAmount::Promotion Fetched " + venPromotionList.size());

			VenPromotion venPromotion = new VenPromotion();
			if (venPromotionList.size() > 0) {
				venPromotion = venPromotionList.get(0);

				// untuk promo bukan voucher, bisa diupload atau tidak diupload
				if ((venPromotion.getVenPromotionType() != null && venPromotion
						.getVenPromotionType().getPromotionType() == VenPromotionTypeConstants.VEN_PROMOTION_TYPE_NONVOUCHER.id())
						|| venPromotion.getVenPromotionType() == null) {
					nonVoucherAmount = nonVoucherAmount
							.add(venOrderItemAdjustment.getAmount().abs());
				}
			}
		}
		System.out.println("Promotion Fetched nonVoucherAmount "
				+ nonVoucherAmount);
		return nonVoucherAmount;
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private BigDecimal getVoucherAmount(List<VenOrderItemAdjustment> venOrderItemAdjustments) throws Exception {
		/*
		VenPromotionSessionEJBLocal promotionHome = (VenPromotionSessionEJBLocal) this._genericLocator
				.lookupLocal(VenPromotionSessionEJBLocal.class,
						"VenPromotionSessionEJBBeanLocal");
		*/
		BigDecimal voucherAmount = new BigDecimal(0);

		for (VenOrderItemAdjustment venOrderItemAdjustment : venOrderItemAdjustments) {
			/*
			List<VenPromotion> venPromotionList = promotionHome
					.queryByRange(
							"select o from VenPromotion o left join fetch o.venPromotionType where o.promotionId = "
									+ venOrderItemAdjustment.getVenPromotion()
											.getPromotionId(), 0, 1);
			*/
			List<VenPromotion> venPromotionList = venPromotionDAO.findByPromotionIdLeftJoinPromotionType(
					venOrderItemAdjustment.getVenPromotion().getPromotionId());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "getVoucherAmount::Promotion Id "
					+ venOrderItemAdjustment.getVenPromotion().getPromotionId());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "getVoucherAmount::Promotion Fetched " + venPromotionList.size());

			VenPromotion venPromotion = new VenPromotion();
			if (venPromotionList.size() > 0) {
				venPromotion = venPromotionList.get(0);

				if (venPromotion.getVenPromotionType() != null
						&& venPromotion.getVenPromotionType()
								.getPromotionType() == VenPromotionTypeConstants.VEN_PROMOTION_TYPE_VOUCHER.id()) {
					voucherAmount = voucherAmount.add(venOrderItemAdjustment
							.getAmount().abs());
				}
			}
		}
		System.out.println("Promotion Fetched voucherAmount " + voucherAmount);
		return voucherAmount;
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private BigDecimal getVoucherCSAmount(
			List<VenOrderItemAdjustment> venOrderItemAdjustments)
			throws Exception {
		/*
		VenPromotionSessionEJBLocal promotionHome = (VenPromotionSessionEJBLocal) this._genericLocator
				.lookupLocal(VenPromotionSessionEJBLocal.class,
						"VenPromotionSessionEJBBeanLocal");
	    */
		BigDecimal voucherCSAmount = new BigDecimal(0);

		for (VenOrderItemAdjustment venOrderItemAdjustment : venOrderItemAdjustments) {
			/*
			List<VenPromotion> venPromotionList = promotionHome
					.queryByRange(
							"select o from VenPromotion o left join fetch o.venPromotionType where o.promotionId = "
									+ venOrderItemAdjustment.getVenPromotion()
											.getPromotionId(), 0, 1);
			*/
			List<VenPromotion> venPromotionList = venPromotionDAO.findByPromotionIdLeftJoinPromotionType(
					venOrderItemAdjustment.getVenPromotion().getPromotionId());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "getVoucherCSAmount::Promotion Id "
					+ venOrderItemAdjustment.getVenPromotion().getPromotionId());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "getVoucherCSAmount::Promotion Fetched " + venPromotionList.size());

			VenPromotion venPromotion = new VenPromotion();
			if (venPromotionList.size() > 0) {
				venPromotion = venPromotionList.get(0);

				if (venPromotion.getVenPromotionType() != null
						&& venPromotion.getVenPromotionType()
								.getPromotionType() == VenPromotionTypeConstants.VEN_PROMOTION_TYPE_VOUCHERCS.id()) {
					voucherCSAmount = voucherCSAmount
							.add(venOrderItemAdjustment.getAmount().abs());
				}
			}
		}
		System.out.println("Promotion Fetched voucherCSAmount "
				+ voucherCSAmount);
		return voucherCSAmount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postSalesJournalTransactions(ArrayList<Long> finSalesRecordId, Boolean
	 * pkp)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postSalesJournalTransaction(Long finSalesRecordId, Boolean pkp) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();
		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {
			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");

			FinSalesRecordSessionEJBLocal salesRecordHome = (FinSalesRecordSessionEJBLocal) this._genericLocator
					.lookupLocal(FinSalesRecordSessionEJBLocal.class,
							"FinSalesRecordSessionEJBBeanLocal");

			VenOrderItemSessionEJBLocal orderItemHome = (VenOrderItemSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderItemSessionEJBLocal.class,
							"VenOrderItemSessionEJBBeanLocal");
			*/

			FinPeriod finPeriod = FinancePeriodUtil.getCurrentPeriod();

			/*
			List<FinSalesRecord> finSalesRecordList = salesRecordHome
					.queryByRange(
							"select o from FinSalesRecord o inner join fetch o.venOrderItem i left join fetch i.venOrderItemAdjustments where o.salesRecordId = "
									+ finSalesRecordId, 0, 0);
			*/
			List<FinSalesRecord> finSalesRecordList = finSalesRecordDAO.findBySalesRecordIdInnerJoinOrderItemLeftJoinOrderItemAdjustment(finSalesRecordId);
			if (finSalesRecordList.isEmpty()) {
				throw new EJBException(
						"Invalid finSalesRecordId passed to postSalesJournalTransaction. The sales record does not exist!");
			}

			FinSalesRecord finSalesRecord = finSalesRecordList.get(0);

			if (finSalesRecord.getFinJournalTransactions() == null) {
				finSalesRecord.setFinJournalTransactions(transactionList);
			}

			/*
			 * Process the list of Sales Journal Transactions: o create the
			 * grouping record o journal transaction
			 * 
			 * 2230001 Uang Jaminan Transaksi 6133010 SM - Promosi Konsumen
			 * (merchant status "Commission/Rebate") 5110004 HPP - Biaya Barang
			 * (merchant status "Trading/Consignment") 4900000 Pot Penjualan
			 * (merchant status "Trading/Consignment") 2220001 Voucher yang
			 * belum direalisasi 6133020 SM - Voucher Customer Service (Masuk ke
			 * potongan penjualan untuk merchant status "Trading") 2130001
			 * Hutang Merchant (merchant status "Commission/Rebate") 4110007
			 * Penjualan (merchant status "Trading/Consignment") 4110001
			 * Pendapatan Komisi (merchant status "Commission") 4110004
			 * Pendapatan Logistik (only for non free shipping) 4110003
			 * Pendapatan Jasa transaksi (merchant status "Commission/Rebate")
			 * 4110005 Pendapatan jasa handling 2180008 PPN Keluaran 1180001
			 * Inventory Over Voucher (Nilai inventory) (merchant status
			 * "Trading/Consignment") 6133018 SM - Beban promosi bebas biaya
			 * kirim (only for free shipping) 2210002 Biaya harus dibayar -
			 * logistik (only for free shipping) 5110001 HPP - Logistik (only
			 * for non free shipping) 1180001 Inventory (merchant status
			 * "Consignment") 2150099 Barang Diterima Dimuka (merchant status
			 * "Consignment")
			 */

			/*
			 * Lookup the sales journal approval group for the day If there is
			 * none then create it
			 */
			AirwayBillEngineConnector awbConn = new AirwayBillEngineClientConnector();
			boolean itemLogMSG = false;

			CommonUtil.logInfo(this.getClass().getCanonicalName(), "postSalesJournalTransaction::logistic service: "
					+ finSalesRecord.getVenOrderItem().getLogLogisticService()
							.getLogLogisticsProvider()
							.getLogisticsProviderCode());
			if (finSalesRecord.getVenOrderItem().getLogLogisticService()
					.getLogLogisticsProvider().getLogisticsProviderId() != VenLogisticsProviderConstants.VEN_LOGISTICS_PROVIDER_BOPIS.id()
					&& finSalesRecord.getVenOrderItem().getLogLogisticService()
							.getLogLogisticsProvider().getLogisticsProviderId() != VenLogisticsProviderConstants.VEN_LOGISTICS_PROVIDER_BIGPRODUCT.id()) {

				CommonUtil.logInfo(this.getClass().getCanonicalName(), "postSalesJournalTransaction::logistic service not bopis or big product, get airwaybill transaction");
				List<AirwayBillTransaction> awbList = (List<AirwayBillTransaction>) awbConn
						.getAirwayBillTransactionByItem(finSalesRecord
								.getVenOrderItem().getWcsOrderItemId());

				if (awbList == null) {
					throw new EJBException(
							"This order is before airway bill automation, cannot create sales journal. Ops ignore this message");
				}

				for (AirwayBillTransaction ob : awbList) {
					if (ob.getLevel().equals("OM") && ob.getVoidBy() != null
							&& ob.getVoidBy().equals("")
							&& ob.getKodeLogistik().equals("MSG")) {
						itemLogMSG = true;
					}
				}
			}

			FinJournal finJournalSales = new FinJournal();
			finJournalSales.setJournalId(VeniceConstants.FIN_JOURNAL_SALES);
			FinJournalApprovalGroup finJournalApprovalGroup = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			/*
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = journalApprovalGroupHome
					.queryByRange(
							"select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Sales Journal for :"
									+ sdf.format(new Date())
									+ "'"
									+ " and o.finApprovalStatus.approvalStatusId = "
									+ VeniceConstants.FIN_APPROVAL_STATUS_APPROVED,
							0, 0);
			*/
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = finJournalApprovalGroupDAO.findByDescriptionAndApprovalStatusId(
					"Sales Journal for :" + sdf.format(new Date()), FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());

			if (finJournalApprovalGroupList.isEmpty()) {
				finJournalApprovalGroup = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(VeniceConstants.FIN_APPROVAL_STATUS_APPROVED);
				finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);
				finJournalApprovalGroup.setFinJournal(finJournalSales);
				finJournalApprovalGroup
						.setJournalGroupDesc("Sales Journal for :"
								+ sdf.format(new Date()));
				finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// Persist the journal group
				//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postSalesJournalTransaction:: finJournalApprovalGroup not attached, calling save explicitly.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			} else {
				finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			}

			/*
			 * query to get settlement record id
			 */
			Locator<Object> locator = null;
			VenSettlementRecord venSettlementRecord = null;
			try {
				locator = new Locator<Object>();
				VenSettlementRecordSessionEJBLocal VenSettlementRecordHome = (VenSettlementRecordSessionEJBLocal) locator
						.lookupLocal(VenSettlementRecordSessionEJBLocal.class,
								"VenSettlementRecordSessionEJBBeanLocal");
				List<VenSettlementRecord> VenSettlementRecordList = VenSettlementRecordHome
						.queryByRange(
								"select o from VenSettlementRecord o where o.venOrderItem.orderItemId = "
										+ finSalesRecord.getVenOrderItem()
												.getOrderItemId(), 0, 0);
				if (VenSettlementRecordList.size() > 0) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::VenSettlementRecord found");
					venSettlementRecord = VenSettlementRecordList.get(0);

					if (venSettlementRecord == null) {
						throw new Exception("Settlement record not found");
					}
				}
			} catch (Exception e) {
				String errMsg = "Settlement record not found";
				CommonUtil.logError(this.getClass().getCanonicalName(), errMsg);
				e.printStackTrace();
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::settlement record id: "
					+ venSettlementRecord.getSettlementRecordId());

			String merchantCommisionType = venSettlementRecord
					.getCommissionType();
			BigDecimal incomeForPPNCalculation = new BigDecimal(0);

			BigDecimal shippingCost = finSalesRecord.getVenOrderItem()
					.getShippingCost();
			BigDecimal discountLogisticPercentage = new BigDecimal(0);
			BigDecimal discountLogisticAmount = new BigDecimal(0);
			BigDecimal totalShippingCost = shippingCost;

			// get discount for logistic provider from awb engine
			if (finSalesRecord.getVenOrderItem().getLogLogisticService()
					.getLogLogisticsProvider().getLogisticsProviderId() != VenLogisticsProviderConstants.VEN_LOGISTICS_PROVIDER_BOPIS.id()
					&& finSalesRecord.getVenOrderItem().getLogLogisticService()
							.getLogLogisticsProvider().getLogisticsProviderId() != VenLogisticsProviderConstants.VEN_LOGISTICS_PROVIDER_BIGPRODUCT.id()) {

				GetLogisticProviderListResponse response = awbConn
						.getAllLogisticProvider(finSalesRecord
								.getVenOrderItem().getLogLogisticService()
								.getLogLogisticsProvider()
								.getLogisticsProviderCode());
				if (response != null) {
					List<LogisticProviderResource> logisticProviderList = response
							.getList();
					discountLogisticPercentage = new BigDecimal(
							logisticProviderList.get(0).getDiscountPercentage());
					CommonUtil.logInfo(this.getClass().getCanonicalName(), "postSalesJournalTransaction::discountLogisticPercentage from awb engine: "
							+ discountLogisticPercentage);

					if (discountLogisticPercentage.compareTo(new BigDecimal(0)) > 0) {
						discountLogisticAmount = discountLogisticPercentage
								.multiply(shippingCost);

						// sum the total shipping cost
						totalShippingCost = shippingCost.subtract(
								discountLogisticAmount).add(
								finSalesRecord.getVenOrderItem()
										.getInsuranceCost());
					}

					// persist discount to venOrderItem
					VenOrderItem item = finSalesRecord.getVenOrderItem();
					CommonUtil.logInfo(this.getClass().getCanonicalName(), "postSalesJournalTransaction::saving discount percentage and amount for wcs_order_item_id: "
							+ item.getWcsOrderItemId());
					item.setLogisticDiscountPercentage(discountLogisticPercentage);
					item.setLogisticDiscountAmount(discountLogisticAmount);
					//orderItemHome.mergeVenOrderItem(item);
					if (!em.contains(item)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"postSalesJournalTransaction:: item not attached, calling save to attach.");
						item = venOrderItemDAO.save(item);				
					}
				}
			}
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::totalShippingCost: " + totalShippingCost);

			/*
			 * POST DEBIT Acc No : 2230001 , Desc : Uang Jaminan Transaksi
			 */
			FinJournalTransaction ujtJournalTransaction = new FinJournalTransaction();
			ujtJournalTransaction
					.setComments("System Debit  Uang Jaminan Transaksi: "
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			ujtJournalTransaction.setCreditDebitFlag(false);
			ujtJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountUangJaminanTransaksi = new FinAccount();
			finAccountUangJaminanTransaksi
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());
			ujtJournalTransaction.setFinAccount(finAccountUangJaminanTransaksi);

			ujtJournalTransaction.setFinSalesRecords(finSalesRecordList);
			ujtJournalTransaction.setFinJournal(finJournalSales);
			ujtJournalTransaction.setFinPeriod(finPeriod);

			FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
			finTransactionStatus
					.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
			ujtJournalTransaction.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeUangJaminanTransaksi = new FinTransactionType();
			finTransactionTypeUangJaminanTransaksi
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id());
			ujtJournalTransaction
					.setFinTransactionType(finTransactionTypeUangJaminanTransaksi);

			ujtJournalTransaction.setTransactionAmount(finSalesRecord
					.getCustomerDownpayment());
			ujtJournalTransaction.setTransactionTimestamp(new Timestamp(System
					.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(ujtJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			ujtJournalTransaction.setFinSalesRecords(finSalesRecordList);
			ujtJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//ujtJournalTransaction = journalTransactionHome.persistFinJournalTransaction(ujtJournalTransaction);
			if (!em.contains(ujtJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: ujtJournalTransaction not attached, calling save explicitly");
				ujtJournalTransaction = finJournalTransactionDAO.save(ujtJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Uang Jaminan Transaksi AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_2230001.id() + " Amount: "
					+ ujtJournalTransaction.getTransactionAmount());

			/*
			 * Post the DEBIT for Acc no : 6133010, Desc : SM - Promosi Konsumen
			 */
			FinJournalTransaction smPromoJournalTransaction = new FinJournalTransaction();
			smPromoJournalTransaction
					.setComments("System Debit SM Promosi Konsumen:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			smPromoJournalTransaction.setCreditDebitFlag(false);
			smPromoJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountSMPromo = new FinAccount();
			finAccountSMPromo.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133010.id());
			smPromoJournalTransaction.setFinAccount(finAccountSMPromo);
			smPromoJournalTransaction.setFinSalesRecords(finSalesRecordList);
			smPromoJournalTransaction.setFinJournal(finJournalSales);
			smPromoJournalTransaction.setFinPeriod(finPeriod);
			smPromoJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypePromo = new FinTransactionType();
			finTransactionTypePromo
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_PROMOSI_KONSUMEN.id());
			smPromoJournalTransaction
					.setFinTransactionType(finTransactionTypePromo);

			/*
			 * catatan si vina SM promosi konsumen -> tidak upload If
			 * commission/rebate, SM promo kons = nilai promo yang teraplikasi
			 * If Trading/Trading MP potongan penjualan = nilai promo yang
			 * teraplikasi/1,1
			 */
			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())) {
				List<VenOrderItemAdjustment> venOrderItemAdjustments = finSalesRecord
						.getVenOrderItem().getVenOrderItemAdjustments();
				BigDecimal smPromoAmount = getNonVoucherAmount(venOrderItemAdjustments);
				smPromoJournalTransaction.setTransactionAmount(smPromoAmount);
			} else {
				smPromoJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}

			smPromoJournalTransaction.setTransactionTimestamp(new Timestamp(
					System.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(smPromoJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			smPromoJournalTransaction.setFinSalesRecords(finSalesRecordList);
			smPromoJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//smPromoJournalTransaction = journalTransactionHome.persistFinJournalTransaction(smPromoJournalTransaction);
			if (!em.contains(smPromoJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"postSalesJournalTransaction:: smPromoJournalTransaction not attached, calling save to attach.");
				smPromoJournalTransaction = finJournalTransactionDAO.save(smPromoJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Promosi Konsumen AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_6133010.id() + " Amount: "
					+ smPromoJournalTransaction.getTransactionAmount());

			/*
			 * Post the DEBIT for Acc no : 5110004, Desc : HPP Biaya Barang
			 * 
			 * value supposed to be from HARGA INVENTORY. Pending until
			 * Inventory Module is done
			 */
			FinJournalTransaction biayaBarangJournalTransaction = new FinJournalTransaction();
			biayaBarangJournalTransaction
					.setComments("System Debit HPP Biaya Barang:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			biayaBarangJournalTransaction.setCreditDebitFlag(false);
			biayaBarangJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finBiayaBarang = new FinAccount();
			finBiayaBarang.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110004.id());
			biayaBarangJournalTransaction.setFinAccount(finBiayaBarang);
			biayaBarangJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			biayaBarangJournalTransaction.setFinJournal(finJournalSales);
			biayaBarangJournalTransaction.setFinPeriod(finPeriod);
			biayaBarangJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeBiayaBarang = new FinTransactionType();
			finTransactionTypeBiayaBarang
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HPP_BIAYA_BARANG.id());

			biayaBarangJournalTransaction
					.setFinTransactionType(finTransactionTypeBiayaBarang);
			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {
				biayaBarangJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			} else if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {
				biayaBarangJournalTransaction
						.setTransactionAmount((finSalesRecord.getVenOrderItem()
								.getTotal().subtract(finSalesRecord
								.getGdnCommissionAmount())).divide(
								GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
			} else {
				biayaBarangJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}

			biayaBarangJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(biayaBarangJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			biayaBarangJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			biayaBarangJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//biayaBarangJournalTransaction = journalTransactionHome.persistFinJournalTransaction(biayaBarangJournalTransaction);
			if (!em.contains(biayaBarangJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"postSalesJournalTransaction:: biayaBarangJournalTransaction not attached, calling save explicitly.");
				biayaBarangJournalTransaction = finJournalTransactionDAO.save(biayaBarangJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set HPP Biaya Barang AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_5110003.id() + " Amount: "
					+ biayaBarangJournalTransaction.getTransactionAmount());

			/*
			 * 
			 * Post the DEBIT for Acc no : 4900000, Desc : Pot Penjualan
			 */
			FinJournalTransaction potPenjualanJournalTransaction = new FinJournalTransaction();
			potPenjualanJournalTransaction
					.setComments("System Debit Pot Penjualan:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			potPenjualanJournalTransaction.setCreditDebitFlag(false);
			potPenjualanJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finPotPenjualan = new FinAccount();
			finPotPenjualan.setAccountId(FinAccountConstants.FIN_ACCOUNT_4900000.id());
			potPenjualanJournalTransaction.setFinAccount(finPotPenjualan);
			potPenjualanJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			potPenjualanJournalTransaction.setFinJournal(finJournalSales);
			potPenjualanJournalTransaction.setFinPeriod(finPeriod);
			potPenjualanJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypePotPenjualan = new FinTransactionType();
			finTransactionTypePotPenjualan
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_POT_PENJUALAN.id());
			potPenjualanJournalTransaction
					.setFinTransactionType(finTransactionTypePotPenjualan);

			/*
			 * catatan si vina If Trading/Trading MP potongan penjualan = nilai
			 * promo yang teraplikasi/1,1
			 */
			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {

				List<VenOrderItemAdjustment> venOrderItemAdjustments = finSalesRecord
						.getVenOrderItem().getVenOrderItemAdjustments();

				BigDecimal potPenjualanAmount = new BigDecimal(0);
				potPenjualanAmount = getVoucherCSAmount(venOrderItemAdjustments);

				if (potPenjualanAmount.compareTo(new BigDecimal(0)) == 0) {
					potPenjualanAmount = getNonVoucherAmount(venOrderItemAdjustments);
				}

				potPenjualanJournalTransaction
						.setTransactionAmount(potPenjualanAmount.divide(
								GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
			} else {
				potPenjualanJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}
			potPenjualanJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(potPenjualanJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			potPenjualanJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			potPenjualanJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//potPenjualanJournalTransaction = journalTransactionHome.persistFinJournalTransaction(potPenjualanJournalTransaction);
			if (!em.contains(potPenjualanJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: potPenjualanJournalTransaction not attached, calling save explicitly.");
				potPenjualanJournalTransaction = finJournalTransactionDAO.save(potPenjualanJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Pot Penjualan AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_4900000.id() + " Amount: "
					+ potPenjualanJournalTransaction.getTransactionAmount());

			/*
			 * 
			 * POST DEBIT Acc no : 2220001, Desc : Voucher yang belum
			 * direalisasi
			 */
			FinJournalTransaction voucherJournalTransaction = new FinJournalTransaction();
			voucherJournalTransaction
					.setComments("System Debit Voucher belum direalisasi:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			voucherJournalTransaction.setCreditDebitFlag(false);
			voucherJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountVoucher = new FinAccount();
			finAccountVoucher.setAccountId(FinAccountConstants.FIN_ACCOUNT_2220001.id());
			voucherJournalTransaction.setFinAccount(finAccountVoucher);

			voucherJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherJournalTransaction.setFinJournal(finJournalSales);
			voucherJournalTransaction.setFinPeriod(finPeriod);
			voucherJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeVoucher = new FinTransactionType();
			finTransactionTypeVoucher
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_VOUCHER_YANG_BELUM_DIREALISASI.id());
			voucherJournalTransaction
					.setFinTransactionType(finTransactionTypeVoucher);

			/*
			 * get value from ven_order_item_adjustment.amount w/ order_item_id
			 * as key
			 * 
			 * check if one of the promotions is on the Voucher promotion list
			 * uploaded from "Promotion" menu on FInance Module
			 */
			List<VenOrderItemAdjustment> venOrderItemAdjustments = finSalesRecord
					.getVenOrderItem().getVenOrderItemAdjustments();

			/*
			 * catatan si vina Voucher yang belum direalisasi-> kalo upload
			 * Voucher Commission/ Trading/Trading MP/Rebate, Nilai voucher =
			 * nilai voucher yang teraplikasi.
			 */
			BigDecimal voucherAmount = getVoucherAmount(venOrderItemAdjustments);
			voucherJournalTransaction.setTransactionAmount(voucherAmount);
			voucherJournalTransaction.setTransactionTimestamp(new Timestamp(
					System.currentTimeMillis()));

			transactionList.add(voucherJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			voucherJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//voucherJournalTransaction = journalTransactionHome.persistFinJournalTransaction(voucherJournalTransaction);
			if (!em.contains(voucherJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: voucherJournalTransaction not attached, calling save to attach.");
				voucherJournalTransaction = finJournalTransactionDAO.save(voucherJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Voucher belum direalisasi AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_2220001.id() + " Amount: "
					+ voucherJournalTransaction.getTransactionAmount());

			/*
			 * 
			 * POST DEBIT Acc no : 6133020, Desc : SM-Voucher Customer Service
			 */
			FinJournalTransaction voucherCSJournalTransaction = new FinJournalTransaction();
			voucherCSJournalTransaction
					.setComments("System Debit Voucher Customer Service:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			voucherCSJournalTransaction.setCreditDebitFlag(false);
			voucherCSJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountVoucherCS = new FinAccount();
			finAccountVoucherCS
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133020.id());
			voucherCSJournalTransaction.setFinAccount(finAccountVoucherCS);

			voucherCSJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherCSJournalTransaction.setFinJournal(finJournalSales);
			voucherCSJournalTransaction.setFinPeriod(finPeriod);
			voucherCSJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeVoucherCS = new FinTransactionType();
			finTransactionTypeVoucherCS
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_VOUCHER_CUSTOMER_SERVICE.id());
			voucherCSJournalTransaction
					.setFinTransactionType(finTransactionTypeVoucherCS);

			/*
			 * check if one of the promotions is on the Customer Service Voucher
			 * promotion list uploaded from "Promotion" menu on FInance Module
			 */
			venOrderItemAdjustments = finSalesRecord.getVenOrderItem()
					.getVenOrderItemAdjustments();

			BigDecimal voucherCSAmount = new BigDecimal(0);

			/*
			 * catatan si vina SM Voucher CS->kalo upload CS If
			 * commission/rebate, SM voucher CS = nilai promo yang teraplikasi
			 * If Trading/Trading MP, SM Voucher CS = 0, Potongan penjualan =
			 * nilai promo yang teraplikasi/1,1
			 */
			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {
				voucherCSAmount = getVoucherCSAmount(venOrderItemAdjustments);
			} else if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {
				voucherCSAmount = new BigDecimal(0);
			}

			voucherCSJournalTransaction.setTransactionAmount(voucherCSAmount);
			voucherCSJournalTransaction.setTransactionTimestamp(new Timestamp(
					System.currentTimeMillis()));
			transactionList.add(voucherCSJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			voucherCSJournalTransaction.setFinSalesRecords(finSalesRecordList);
			voucherCSJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);

			//voucherCSJournalTransaction = journalTransactionHome.persistFinJournalTransaction(voucherCSJournalTransaction);
			if (!em.contains(voucherCSJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: voucherCSJournalTransaction not attached, calling save explicitly.");
				voucherCSJournalTransaction = finJournalTransactionDAO.save(voucherCSJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Voucher Customer Service AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_6133020.id() + " Amount: "
					+ voucherCSJournalTransaction.getTransactionAmount());

			/*
			 * Post the CREDIT for HUTANG MERCHANT Only for Merchant Type
			 * Commision and Rebate
			 */
			FinJournalTransaction hutangMerchantJournalTransaction = new FinJournalTransaction();
			hutangMerchantJournalTransaction
					.setComments("System Credit Hutang Merchant:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			hutangMerchantJournalTransaction.setCreditDebitFlag(true);
			hutangMerchantJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountHutangMerchant = new FinAccount();
			finAccountHutangMerchant
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_2130001.id());

			hutangMerchantJournalTransaction
					.setFinAccount(finAccountHutangMerchant);
			hutangMerchantJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			hutangMerchantJournalTransaction.setFinJournal(finJournalSales);
			hutangMerchantJournalTransaction.setFinPeriod(finPeriod);
			hutangMerchantJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeHutangMerchant = new FinTransactionType();
			finTransactionTypeHutangMerchant
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HUTANG_MERCHANT.id());
			hutangMerchantJournalTransaction
					.setFinTransactionType(finTransactionTypeHutangMerchant);
			hutangMerchantJournalTransaction
					.setTransactionAmount(new BigDecimal(0));

			if (venSettlementRecord != null) {
				if (merchantCommisionType
						.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())
						|| merchantCommisionType
								.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {
					hutangMerchantJournalTransaction
							.setTransactionAmount(finSalesRecord
									.getMerchantPaymentAmount());
				}
			}

			hutangMerchantJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(hutangMerchantJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}
			hutangMerchantJournalTransaction
					.setFinSalesRecords(finSalesRecordList);

			hutangMerchantJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//hutangMerchantJournalTransaction = journalTransactionHome.persistFinJournalTransaction(hutangMerchantJournalTransaction);
			if (!em.contains(hutangMerchantJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: hutangMerchantJournalTransaction not attached, calling save to attach.");
				hutangMerchantJournalTransaction = finJournalTransactionDAO.save(hutangMerchantJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Hutang Merchant AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_2130001.id() + " Amount: "
					+ hutangMerchantJournalTransaction.getTransactionAmount());

			/*
			 * POST CREDIT of Acc No : 4110007, Desc : Penjualan
			 */
			FinJournalTransaction penjualanJournalTransaction = new FinJournalTransaction();
			penjualanJournalTransaction.setComments("System Credit Penjualan:"
					+ finSalesRecord.getVenOrderItem().getWcsOrderItemId());
			penjualanJournalTransaction.setCreditDebitFlag(true);
			penjualanJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountPenjualan = new FinAccount();
			finAccountPenjualan
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110007.id());

			penjualanJournalTransaction.setFinAccount(finAccountPenjualan);
			penjualanJournalTransaction.setFinSalesRecords(finSalesRecordList);
			penjualanJournalTransaction.setFinJournal(finJournalSales);
			penjualanJournalTransaction.setFinPeriod(finPeriod);

			penjualanJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypePenjualan = new FinTransactionType();
			finTransactionTypePenjualan
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENJUALAN.id());
			penjualanJournalTransaction
					.setFinTransactionType(finTransactionTypePenjualan);

			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {
				penjualanJournalTransaction.setTransactionAmount(finSalesRecord
						.getVenOrderItem().getTotal()
						.divide(GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
			} else {
				penjualanJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}

			penjualanJournalTransaction.setTransactionTimestamp(new Timestamp(
					System.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(penjualanJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			penjualanJournalTransaction.setFinSalesRecords(finSalesRecordList);
			penjualanJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//penjualanJournalTransaction = journalTransactionHome.persistFinJournalTransaction(penjualanJournalTransaction);
			if (!em.contains(penjualanJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"postSalesJournalTransaction:: penjualanJournalTransaction not attached, calling save explicitly.");
				penjualanJournalTransaction = finJournalTransactionDAO.save(penjualanJournalTransaction);
			}

			incomeForPPNCalculation = incomeForPPNCalculation
					.add(penjualanJournalTransaction.getTransactionAmount());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Penjualan AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_4110007.id() + " Amount: "
					+ penjualanJournalTransaction.getTransactionAmount());

			/*
			 * 
			 * Post the CREDIT for Acc no : 4110001, Desc: PENDAPATAN KOMISI
			 */
			FinJournalTransaction komisiJournalTransaction = new FinJournalTransaction();
			komisiJournalTransaction
					.setComments("System Credit Pendapatan Komisi:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			komisiJournalTransaction.setCreditDebitFlag(true);
			komisiJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountKomisi = new FinAccount();
			finAccountKomisi.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110001.id());

			komisiJournalTransaction.setFinAccount(finAccountKomisi);
			komisiJournalTransaction.setFinSalesRecords(finSalesRecordList);
			komisiJournalTransaction.setFinJournal(finJournalSales);
			komisiJournalTransaction.setFinPeriod(finPeriod);

			komisiJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeKomisi = new FinTransactionType();
			finTransactionTypeKomisi
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_KOMISI.id());
			komisiJournalTransaction
					.setFinTransactionType(finTransactionTypeKomisi);
			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())) {
				komisiJournalTransaction.setTransactionAmount(finSalesRecord
						.getGdnCommissionAmount().divide(GDNPPN_DIVISOR, 2,
								RoundingMode.HALF_UP));
			} else {
				komisiJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}

			komisiJournalTransaction.setTransactionTimestamp(new Timestamp(
					System.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(komisiJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			komisiJournalTransaction.setFinSalesRecords(finSalesRecordList);
			komisiJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//komisiJournalTransaction = journalTransactionHome.persistFinJournalTransaction(komisiJournalTransaction);
			if (!em.contains(komisiJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: komisiJournalTransaction not attached, calling save explicitly.");
				komisiJournalTransaction = finJournalTransactionDAO.save(komisiJournalTransaction);
			}
			incomeForPPNCalculation = incomeForPPNCalculation
					.add(komisiJournalTransaction.getTransactionAmount());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Pendapatan Komisi AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_4110001.id() + " Amount: "
					+ komisiJournalTransaction.getTransactionAmount());

			boolean freeShipping = false;

			venOrderItemAdjustments = finSalesRecord.getVenOrderItem()
					.getVenOrderItemAdjustments();

			for (VenOrderItemAdjustment venOrderItemAdjustment : venOrderItemAdjustments) {
				if (venOrderItemAdjustment.getVenPromotion().getPromotionName()
						.toLowerCase().contains("free shipping")) {
					freeShipping = true;
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: this item is free shipping");
					break;
				}
			}

			if (!freeShipping) {
				/*
				 * Post the CREDIT for Acc No : 4110004, Desc : PENDAPATAN
				 * LOGISTIK
				 */
				FinJournalTransaction pendapatanLogisticsJournalTransaction = new FinJournalTransaction();
				pendapatanLogisticsJournalTransaction
						.setComments("System Credit Pendapatan Logistics:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				pendapatanLogisticsJournalTransaction.setCreditDebitFlag(true);
				pendapatanLogisticsJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountPendapatanLogistics = new FinAccount();
				finAccountPendapatanLogistics
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110004.id());

				pendapatanLogisticsJournalTransaction
						.setFinAccount(finAccountPendapatanLogistics);
				pendapatanLogisticsJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				pendapatanLogisticsJournalTransaction
						.setFinJournal(finJournalSales);
				pendapatanLogisticsJournalTransaction.setFinPeriod(finPeriod);
				pendapatanLogisticsJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePendapatanLogistics = new FinTransactionType();
				finTransactionTypePendapatanLogistics
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_LOGISTIK.id());
				pendapatanLogisticsJournalTransaction
						.setFinTransactionType(finTransactionTypePendapatanLogistics);
				pendapatanLogisticsJournalTransaction
						.setTransactionAmount(finSalesRecord
								.getTotalLogisticsRelatedAmount());
				pendapatanLogisticsJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(pendapatanLogisticsJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				pendapatanLogisticsJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				pendapatanLogisticsJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//pendapatanLogisticsJournalTransaction = journalTransactionHome.persistFinJournalTransaction(pendapatanLogisticsJournalTransaction);
				if (!em.contains(pendapatanLogisticsJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postSalesJournalTransaction:: pendapatanLogisticsJournalTransaction not attached, calling save to attach.");
					pendapatanLogisticsJournalTransaction = finJournalTransactionDAO.save(pendapatanLogisticsJournalTransaction);
				}

				incomeForPPNCalculation = incomeForPPNCalculation
						.add(pendapatanLogisticsJournalTransaction
								.getTransactionAmount());

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Pendapatan Logistik AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_4110004.id()
						+ " Amount: "
						+ pendapatanLogisticsJournalTransaction
								.getTransactionAmount());
			}

			/*
			 * Post the CREDIT Acc No: 4110003 , Desc: JASA TRANSAKSI
			 */
			FinJournalTransaction jasaTransaksiJournalTransaction = new FinJournalTransaction();
			jasaTransaksiJournalTransaction
					.setComments("System Credit Jasa Transaksi:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			jasaTransaksiJournalTransaction.setCreditDebitFlag(true);
			jasaTransaksiJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountJasaTransaksi = new FinAccount();
			finAccountJasaTransaksi
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110003.id());

			jasaTransaksiJournalTransaction
					.setFinAccount(finAccountJasaTransaksi);
			jasaTransaksiJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			jasaTransaksiJournalTransaction.setFinJournal(finJournalSales);
			jasaTransaksiJournalTransaction.setFinPeriod(finPeriod);
			jasaTransaksiJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeProcessFee = new FinTransactionType();
			finTransactionTypeProcessFee
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_JASA_TRANSAKSI.id());
			jasaTransaksiJournalTransaction
					.setFinTransactionType(finTransactionTypeProcessFee);

			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE.label())) {
				jasaTransaksiJournalTransaction
						.setTransactionAmount(finSalesRecord
								.getGdnTransactionFeeAmount()
								.divide(GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
			} else {
				jasaTransaksiJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}

			jasaTransaksiJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(jasaTransaksiJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			jasaTransaksiJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			jasaTransaksiJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//jasaTransaksiJournalTransaction = journalTransactionHome.persistFinJournalTransaction(jasaTransaksiJournalTransaction);
			if (!em.contains(jasaTransaksiJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: jasaTransaksiJournalTransaction not attached, calling save explicitly.");
				jasaTransaksiJournalTransaction = finJournalTransactionDAO.save(jasaTransaksiJournalTransaction);
			}

			incomeForPPNCalculation = incomeForPPNCalculation
					.add(jasaTransaksiJournalTransaction.getTransactionAmount());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Pendapatan Jasa Transaksi AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_4110003.id() + " Amount: "
					+ jasaTransaksiJournalTransaction.getTransactionAmount());

			/*
			 * Post the CREDIT for AccNo : 4110005, Desc: JASA HANDLING
			 * PELANGGAN
			 */
			FinJournalTransaction jasaHandlingJournalTransaction = new FinJournalTransaction();
			jasaHandlingJournalTransaction
					.setComments("System Credit Jasa Handling:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			jasaHandlingJournalTransaction.setCreditDebitFlag(true);
			jasaHandlingJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountJasaHandling = new FinAccount();
			finAccountJasaHandling
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110005.id());

			jasaHandlingJournalTransaction
					.setFinAccount(finAccountJasaHandling);
			jasaHandlingJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			jasaHandlingJournalTransaction.setFinJournal(finJournalSales);
			jasaHandlingJournalTransaction.setFinPeriod(finPeriod);
			jasaHandlingJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeProcessFees = new FinTransactionType();
			finTransactionTypeProcessFees
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PENDAPATAN_JASA_HANDLING.id());

			jasaHandlingJournalTransaction
					.setFinTransactionType(finTransactionTypeProcessFees);

			jasaHandlingJournalTransaction.setTransactionAmount(finSalesRecord
					.getGdnHandlingFeeAmount().divide(GDNPPN_DIVISOR, 2,
							RoundingMode.HALF_UP));
			jasaHandlingJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(jasaHandlingJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			jasaHandlingJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			jasaHandlingJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//jasaHandlingJournalTransaction = journalTransactionHome.persistFinJournalTransaction(jasaHandlingJournalTransaction);
			if (!em.contains(jasaHandlingJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: jasaHandlingJournalTransaction not attached, calling save explicitly.");
				jasaHandlingJournalTransaction = finJournalTransactionDAO.save(jasaHandlingJournalTransaction);
			}

			incomeForPPNCalculation = incomeForPPNCalculation
					.add(jasaHandlingJournalTransaction.getTransactionAmount());

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Pendapatan Jasa Handling AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_4110005.id() + " Amount: "
					+ jasaHandlingJournalTransaction.getTransactionAmount());

			/*
			 * 
			 * Post the CREDIT for Acc No 2180008, Desc: PPN
			 */
			FinJournalTransaction ppnJournalTransaction = new FinJournalTransaction();
			ppnJournalTransaction.setComments("System Credit PPN:"
					+ finSalesRecord.getVenOrderItem().getWcsOrderItemId());
			ppnJournalTransaction.setCreditDebitFlag(true);
			ppnJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finAccountPPN = new FinAccount();
			finAccountPPN.setAccountId(FinAccountConstants.FIN_ACCOUNT_2180008.id());

			ppnJournalTransaction.setFinAccount(finAccountPPN);
			ppnJournalTransaction.setFinSalesRecords(finSalesRecordList);
			ppnJournalTransaction.setFinJournal(finJournalSales);
			ppnJournalTransaction.setFinPeriod(finPeriod);
			ppnJournalTransaction.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypePPN = new FinTransactionType();
			finTransactionTypePPN
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PPN.id());
			ppnJournalTransaction.setFinTransactionType(finTransactionTypePPN);

			CommonUtil.logInfo(this.getClass().getCanonicalName(), "postSalesJournalTransaction::pkp: " + pkp);
			if (pkp) {
				if (merchantCommisionType
						.equals(VeniceConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION)
						|| merchantCommisionType
								.equals(VeniceConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE)) {
					ppnJournalTransaction
							.setTransactionAmount(incomeForPPNCalculation
									.multiply(new BigDecimal(0.1)).setScale(2,
											RoundingMode.HALF_UP));
				}

				if (merchantCommisionType
						.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
						|| merchantCommisionType
								.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())
						|| merchantCommisionType
								.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {
					ppnJournalTransaction
							.setTransactionAmount(incomeForPPNCalculation
									.subtract(
											potPenjualanJournalTransaction
													.getTransactionAmount())
									.multiply(new BigDecimal(0.1))
									.setScale(2, RoundingMode.HALF_UP));
				}
			} else {
				ppnJournalTransaction.setTransactionAmount(new BigDecimal(0));
			}

			ppnJournalTransaction.setTransactionTimestamp(new Timestamp(System
					.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(ppnJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			ppnJournalTransaction.setFinSalesRecords(finSalesRecordList);
			ppnJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);

			// Persist the PPN journal transaction
			//ppnJournalTransaction = journalTransactionHome.persistFinJournalTransaction(ppnJournalTransaction);
			if (!em.contains(ppnJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: ppnJournalTransaction not attached, calling save explicitly.");
				ppnJournalTransaction = finJournalTransactionDAO.save(ppnJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: set PPN Acc No "
					+ FinAccountConstants.FIN_ACCOUNT_2180008.id() + ", Amount: "
					+ ppnJournalTransaction.getTransactionAmount());

			/*
			 * Post the DEBIT for Acc no : 1180007, Desc : Inventory Over
			 * Voucher
			 * 
			 * value supposed to be from HARGA INVENTORY. Pending until
			 * Inventory Module is done
			 */
			FinJournalTransaction inventoryOverVoucherJournalTransaction = new FinJournalTransaction();
			inventoryOverVoucherJournalTransaction
					.setComments("System Credit Inventory Over Voucher:"
							+ finSalesRecord.getVenOrderItem()
									.getWcsOrderItemId());
			inventoryOverVoucherJournalTransaction.setCreditDebitFlag(true);
			inventoryOverVoucherJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			FinAccount finInventoryOverVoucher = new FinAccount();
			finInventoryOverVoucher
					.setAccountId(FinAccountConstants.FIN_ACCOUNT_1180001.id());
			inventoryOverVoucherJournalTransaction
					.setFinAccount(finInventoryOverVoucher);
			inventoryOverVoucherJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			inventoryOverVoucherJournalTransaction
					.setFinJournal(finJournalSales);
			inventoryOverVoucherJournalTransaction.setFinPeriod(finPeriod);
			inventoryOverVoucherJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			FinTransactionType finTransactionTypeInventoryOverVoucher = new FinTransactionType();
			finTransactionTypeInventoryOverVoucher
					.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_INVENTORY_OVER_VOUCHER.id());

			inventoryOverVoucherJournalTransaction
					.setFinTransactionType(finTransactionTypeInventoryOverVoucher);
			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING.label())
					|| merchantCommisionType
							.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {
				inventoryOverVoucherJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			} else if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {
				if (pkp) {
					inventoryOverVoucherJournalTransaction
							.setTransactionAmount((finSalesRecord
									.getVenOrderItem().getTotal()
									.subtract(finSalesRecord
											.getGdnCommissionAmount())).divide(
									GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
				} else {
					inventoryOverVoucherJournalTransaction
							.setTransactionAmount((finSalesRecord
									.getVenOrderItem().getTotal()
									.subtract(finSalesRecord
											.getGdnCommissionAmount())));
				}
			} else {
				inventoryOverVoucherJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
			}
			inventoryOverVoucherJournalTransaction
					.setTransactionTimestamp(new Timestamp(System
							.currentTimeMillis()));

			/*
			 * Add the sales record to the list Note that we need a new list
			 * because collections are read only
			 */
			transactionList.add(inventoryOverVoucherJournalTransaction);

			for (FinSalesRecord salesRecord : finSalesRecordList) {
				salesRecord.setFinJournalTransactions(transactionList);
			}

			inventoryOverVoucherJournalTransaction
					.setFinSalesRecords(finSalesRecordList);
			inventoryOverVoucherJournalTransaction
					.setWcsOrderID(finSalesRecord.getVenOrderItem()
							.getVenOrder().getWcsOrderId() != null ? finSalesRecord
							.getVenOrderItem().getVenOrder().getWcsOrderId()
							: null);
			//inventoryOverVoucherJournalTransaction = journalTransactionHome.persistFinJournalTransaction(inventoryOverVoucherJournalTransaction);
			if (!em.contains(inventoryOverVoucherJournalTransaction)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"postSalesJournalTransaction:: inventoryOverVoucherJournalTransaction not attached, calling save explicitly.");
				inventoryOverVoucherJournalTransaction = finJournalTransactionDAO.save(inventoryOverVoucherJournalTransaction);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Inventory Over Voucher AccNo: "
					+ FinAccountConstants.FIN_ACCOUNT_1180001.id()
					+ " Amount: "
					+ inventoryOverVoucherJournalTransaction
							.getTransactionAmount());

			if (freeShipping) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: FREE SHIPPING: YES");
				/*
				 * Post the DEBIT AccNo :6133018, Desc:SM BEBAN PROMOSI BEBAS
				 * BIAYA KIRIM
				 */
				FinJournalTransaction bebanPromosiBebasBiayaKirimJournalTransaction = new FinJournalTransaction();
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setComments("System Debit SM Promo Bebas Biaya Kirim:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setCreditDebitFlag(false);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountBebanPromosiBebasBiayaKirimLogistics = new FinAccount();
				finAccountBebanPromosiBebasBiayaKirimLogistics
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133018.id());

				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinAccount(finAccountBebanPromosiBebasBiayaKirimLogistics);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinJournal(finJournalSales);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinPeriod(finPeriod);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBebanPromosiBebasBiayaKirim = new FinTransactionType();
				finTransactionTypeBebanPromosiBebasBiayaKirim
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_BEBAN_PROMOSI_BEBAS_BIAYA_KIRIM.id());

				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinTransactionType(finTransactionTypeBebanPromosiBebasBiayaKirim);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setTransactionAmount(totalShippingCost);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList
						.add(bebanPromosiBebasBiayaKirimJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				bebanPromosiBebasBiayaKirimJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				bebanPromosiBebasBiayaKirimJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//bebanPromosiBebasBiayaKirimJournalTransaction = journalTransactionHome.persistFinJournalTransaction(bebanPromosiBebasBiayaKirimJournalTransaction);
				if (!em.contains(bebanPromosiBebasBiayaKirimJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postSalesJournalTransaction:: bebanPromosiBebasBiayaKirimJournalTransaction not attached, calling save.");
					bebanPromosiBebasBiayaKirimJournalTransaction = finJournalTransactionDAO.save(bebanPromosiBebasBiayaKirimJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Bebas Biaya Kirim AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_6133018.id()
						+ " Amount: "
						+ bebanPromosiBebasBiayaKirimJournalTransaction
								.getTransactionAmount());

				/*
				 * Post the CREDIT Acc No: 2210002, Desc: BIAYA HARUS DIBAYAR
				 * LOGISTIK
				 */
				FinJournalTransaction biayaLogistikJournalTransaction = new FinJournalTransaction();
				biayaLogistikJournalTransaction
						.setComments("System Credit Biaya Harus Dibayar Logistik:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				biayaLogistikJournalTransaction.setCreditDebitFlag(true);
				biayaLogistikJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountBiayaLogistik = new FinAccount();
				finAccountBiayaLogistik
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2210002.id());

				biayaLogistikJournalTransaction
						.setFinAccount(finAccountBiayaLogistik);
				biayaLogistikJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				biayaLogistikJournalTransaction.setFinJournal(finJournalSales);
				biayaLogistikJournalTransaction.setFinPeriod(finPeriod);
				biayaLogistikJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBiayaLogistik = new FinTransactionType();
				finTransactionTypeBiayaLogistik
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
				biayaLogistikJournalTransaction
						.setFinTransactionType(finTransactionTypeBiayaLogistik);

				biayaLogistikJournalTransaction
						.setTransactionAmount(totalShippingCost);
				biayaLogistikJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(biayaLogistikJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				biayaLogistikJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				biayaLogistikJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//biayaLogistikJournalTransaction = journalTransactionHome.persistFinJournalTransaction(biayaLogistikJournalTransaction);
				if (!em.contains(biayaLogistikJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postSalesJournaltransaction:: biayaLogistikJournalTransaction not attached, calling save.");
					biayaLogistikJournalTransaction = finJournalTransactionDAO.save(biayaLogistikJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Bebas Harus Dibayar Logistik AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_2210002.id()
						+ " Amount: "
						+ biayaLogistikJournalTransaction
								.getTransactionAmount());

				if (itemLogMSG) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::BLIBLI MESSENGER: YES");
					/*
					 * Post the CREDIT Acc No: 2210002, Desc: BIAYA HARUS
					 * DIBAYAR LOGISTIK
					 */
					FinJournalTransaction biayaLogistikMSGJournalTransaction = new FinJournalTransaction();
					biayaLogistikMSGJournalTransaction
							.setComments("System Debit Biaya Harus Dibayar Logistik:"
									+ finSalesRecord.getVenOrderItem()
											.getWcsOrderItemId());
					biayaLogistikMSGJournalTransaction
							.setCreditDebitFlag(false);
					biayaLogistikMSGJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);

					FinAccount finAccountBiayaLogistikMSG = new FinAccount();
					finAccountBiayaLogistikMSG
							.setAccountId(FinAccountConstants.FIN_ACCOUNT_2210002.id());

					biayaLogistikMSGJournalTransaction
							.setFinAccount(finAccountBiayaLogistikMSG);
					biayaLogistikMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					biayaLogistikMSGJournalTransaction
							.setFinJournal(finJournalSales);
					biayaLogistikMSGJournalTransaction.setFinPeriod(finPeriod);
					biayaLogistikMSGJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType finTransactionTypeBiayaLogistikMSG = new FinTransactionType();
					finTransactionTypeBiayaLogistikMSG
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
					biayaLogistikMSGJournalTransaction
							.setFinTransactionType(finTransactionTypeBiayaLogistikMSG);

					biayaLogistikMSGJournalTransaction
							.setTransactionAmount(totalShippingCost);

					biayaLogistikMSGJournalTransaction
							.setTransactionTimestamp(new Timestamp(System
									.currentTimeMillis()));

					/*
					 * Add the sales record to the list Note that we need a new
					 * list because collections are read only
					 */
					transactionList.add(biayaLogistikMSGJournalTransaction);

					for (FinSalesRecord salesRecord : finSalesRecordList) {
						salesRecord.setFinJournalTransactions(transactionList);
					}

					biayaLogistikMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					biayaLogistikMSGJournalTransaction
							.setWcsOrderID(finSalesRecord.getVenOrderItem()
									.getVenOrder().getWcsOrderId() != null ? finSalesRecord
									.getVenOrderItem().getVenOrder()
									.getWcsOrderId()
									: null);
					//biayaLogistikMSGJournalTransaction = journalTransactionHome.persistFinJournalTransaction(biayaLogistikMSGJournalTransaction);
					if (!em.contains(biayaLogistikMSGJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"postSalesJournalTransaction:: biayaLogistikMSGJournalTransaction not attached, calling save.");
						biayaLogistikMSGJournalTransaction = finJournalTransactionDAO.save(biayaLogistikMSGJournalTransaction);
					}

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Biaya Harus Dibayar Logistik AccNo: "
							+ FinAccountConstants.FIN_ACCOUNT_2210002.id()
							+ " Amount: "
							+ biayaLogistikMSGJournalTransaction
									.getTransactionAmount());

					/*
					 * Post the DEBIT AccNo :6133018, Desc:SM BEBAN PROMOSI
					 * BEBAS BIAYA KIRIM
					 */
					FinJournalTransaction bebanPromosiBebasBiayaKirimMSGJournalTransaction = new FinJournalTransaction();
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setComments("System Credit Beban Promosi Bebas Biaya Kirim:"
									+ finSalesRecord.getVenOrderItem()
											.getWcsOrderItemId());
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setCreditDebitFlag(true);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);

					FinAccount finAccountBebanPromosiBebasBiayaKirimLogisticsMSG = new FinAccount();
					finAccountBebanPromosiBebasBiayaKirimLogisticsMSG
							.setAccountId(FinAccountConstants.FIN_ACCOUNT_6133018.id());

					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinAccount(finAccountBebanPromosiBebasBiayaKirimLogisticsMSG);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinJournal(finJournalSales);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinPeriod(finPeriod);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType finTransactionTypeBebanPromosiBebasBiayaKirimMSG = new FinTransactionType();
					finTransactionTypeBebanPromosiBebasBiayaKirimMSG
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_SM_BEBAN_PROMOSI_BEBAS_BIAYA_KIRIM.id());

					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinTransactionType(finTransactionTypeBebanPromosiBebasBiayaKirimMSG);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setTransactionAmount(totalShippingCost);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setTransactionTimestamp(new Timestamp(System
									.currentTimeMillis()));

					/*
					 * Add the sales record to the list Note that we need a new
					 * list because collections are read only
					 */
					transactionList
							.add(bebanPromosiBebasBiayaKirimMSGJournalTransaction);

					for (FinSalesRecord salesRecord : finSalesRecordList) {
						salesRecord.setFinJournalTransactions(transactionList);
					}

					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					bebanPromosiBebasBiayaKirimMSGJournalTransaction
							.setWcsOrderID(finSalesRecord.getVenOrderItem()
									.getVenOrder().getWcsOrderId() != null ? finSalesRecord
									.getVenOrderItem().getVenOrder()
									.getWcsOrderId()
									: null);
					//bebanPromosiBebasBiayaKirimMSGJournalTransaction = journalTransactionHome.persistFinJournalTransaction(bebanPromosiBebasBiayaKirimMSGJournalTransaction);
					if (!em.contains(bebanPromosiBebasBiayaKirimMSGJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(),
								"postSalesJournalTransaction:: bebanPromosiBebasBiayaKirimMSGJournalTransaction not attached, calling save.");
						bebanPromosiBebasBiayaKirimMSGJournalTransaction = finJournalTransactionDAO.save(bebanPromosiBebasBiayaKirimMSGJournalTransaction);
					}

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Bebas Biaya Kirim AccNo: "
							+ FinAccountConstants.FIN_ACCOUNT_6133018.id()
							+ " Amount: "
							+ bebanPromosiBebasBiayaKirimMSGJournalTransaction
									.getTransactionAmount());
				}
			}

			if (!freeShipping) {
				System.out.println("FREE SEHIIPING: NO");
				/*
				 * Post the CREDIT for Acc no : 5110001, Desc : HPP-logistic
				 */
				FinJournalTransaction hPPlogisticJournalTransaction = new FinJournalTransaction();
				hPPlogisticJournalTransaction
						.setComments("System Debit HPP-logistic:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				hPPlogisticJournalTransaction.setCreditDebitFlag(false);
				hPPlogisticJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount hPPlogisticFinAccount = new FinAccount();
				hPPlogisticFinAccount
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110001.id());
				hPPlogisticJournalTransaction
						.setFinAccount(hPPlogisticFinAccount);
				hPPlogisticJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				hPPlogisticJournalTransaction.setFinJournal(finJournalSales);
				hPPlogisticJournalTransaction.setFinPeriod(finPeriod);
				hPPlogisticJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType hppLogistic = new FinTransactionType();
				hppLogistic
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HPP_LOGISTIK.id());

				hPPlogisticJournalTransaction
						.setFinTransactionType(hppLogistic);
				hPPlogisticJournalTransaction
						.setTransactionAmount(totalShippingCost);
				hPPlogisticJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(hPPlogisticJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				hPPlogisticJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				hPPlogisticJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//hPPlogisticJournalTransaction = journalTransactionHome.persistFinJournalTransaction(hPPlogisticJournalTransaction);
				if (!em.contains(hPPlogisticJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"posSalesJournalTransaction:: hPPlogisticJournalTransaction not attached, calling save.");
					hPPlogisticJournalTransaction = finJournalTransactionDAO.save(hPPlogisticJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set  HPP-logistic AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_5110001.id() + " Amount: "
						+ hPPlogisticJournalTransaction.getTransactionAmount());

				/*
				 * Post the CREDIT Acc No: 2210002, Desc: BIAYA HARUS DIBAYAR
				 * LOGISTIK
				 */
				FinJournalTransaction biayaLogistikJournalTransaction = new FinJournalTransaction();
				biayaLogistikJournalTransaction
						.setComments("System Credit Biaya Harus Dibayar Logistik:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				biayaLogistikJournalTransaction.setCreditDebitFlag(true);
				biayaLogistikJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountBiayaLogistik = new FinAccount();
				finAccountBiayaLogistik
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2210002.id());

				biayaLogistikJournalTransaction
						.setFinAccount(finAccountBiayaLogistik);
				biayaLogistikJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				biayaLogistikJournalTransaction.setFinJournal(finJournalSales);
				biayaLogistikJournalTransaction.setFinPeriod(finPeriod);
				biayaLogistikJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBiayaLogistik = new FinTransactionType();
				finTransactionTypeBiayaLogistik
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
				biayaLogistikJournalTransaction
						.setFinTransactionType(finTransactionTypeBiayaLogistik);

				biayaLogistikJournalTransaction
						.setTransactionAmount(totalShippingCost);
				biayaLogistikJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(biayaLogistikJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				biayaLogistikJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				biayaLogistikJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//biayaLogistikJournalTransaction = journalTransactionHome.persistFinJournalTransaction(biayaLogistikJournalTransaction);
				if (!em.contains(biayaLogistikJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postSalesJournalTransaction::biayaLogistikJournalTransaction not attached, calling save.");
					biayaLogistikJournalTransaction = finJournalTransactionDAO.save(biayaLogistikJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Biaya Harus Dibayar Logistik AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_2210002.id()
						+ " Amount: "
						+ biayaLogistikJournalTransaction
								.getTransactionAmount());

				if (itemLogMSG) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::BLIBLI MESSENGER: YES");
					/*
					 * Post the CREDIT Acc No: 2210002, Desc: BIAYA HARUS
					 * DIBAYAR LOGISTIK
					 */
					FinJournalTransaction biayaLogistikMSGJournalTransaction = new FinJournalTransaction();
					biayaLogistikMSGJournalTransaction
							.setComments("System Debit Biaya Harus Dibayar Logistik:"
									+ finSalesRecord.getVenOrderItem()
											.getWcsOrderItemId());
					biayaLogistikMSGJournalTransaction
							.setCreditDebitFlag(false);
					biayaLogistikMSGJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);

					FinAccount finAccountBiayaLogistikMSG = new FinAccount();
					finAccountBiayaLogistikMSG
							.setAccountId(FinAccountConstants.FIN_ACCOUNT_2210002.id());

					biayaLogistikMSGJournalTransaction
							.setFinAccount(finAccountBiayaLogistikMSG);
					biayaLogistikMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					biayaLogistikMSGJournalTransaction
							.setFinJournal(finJournalSales);
					biayaLogistikMSGJournalTransaction.setFinPeriod(finPeriod);
					biayaLogistikMSGJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType finTransactionTypeBiayaLogistikMSG = new FinTransactionType();
					finTransactionTypeBiayaLogistikMSG
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BIAYA_HARUS_DIBAYAR_LOGISTIK.id());
					biayaLogistikMSGJournalTransaction
							.setFinTransactionType(finTransactionTypeBiayaLogistikMSG);

					biayaLogistikMSGJournalTransaction
							.setTransactionAmount(totalShippingCost);
					biayaLogistikMSGJournalTransaction
							.setTransactionTimestamp(new Timestamp(System
									.currentTimeMillis()));

					/*
					 * Add the sales record to the list Note that we need a new
					 * list because collections are read only
					 */
					transactionList.add(biayaLogistikMSGJournalTransaction);

					for (FinSalesRecord salesRecord : finSalesRecordList) {
						salesRecord.setFinJournalTransactions(transactionList);
					}

					biayaLogistikMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					biayaLogistikMSGJournalTransaction
							.setWcsOrderID(finSalesRecord.getVenOrderItem()
									.getVenOrder().getWcsOrderId() != null ? finSalesRecord
									.getVenOrderItem().getVenOrder()
									.getWcsOrderId()
									: null);

					// Persist the BIAYA HARUS DIBAYAR LOGISTIK journal
					// transaction
					//biayaLogistikMSGJournalTransaction = journalTransactionHome.persistFinJournalTransaction(biayaLogistikMSGJournalTransaction);
					if (!em.contains(biayaLogistikMSGJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "postSalesJournalTransaction:: biayaLogistikMSGJournalTransaction not attached, calling save.");
						biayaLogistikMSGJournalTransaction = finJournalTransactionDAO.save(biayaLogistikMSGJournalTransaction);
					}

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Biaya Harus Dibayar Logistik AccNo: "
							+ FinAccountConstants.FIN_ACCOUNT_2210002.id()
							+ " Amount: "
							+ biayaLogistikMSGJournalTransaction
									.getTransactionAmount());

					/*
					 * Post the CREDIT for Acc no : 5110001, Desc : HPP-logistic
					 */
					FinJournalTransaction hPPlogisticMSGJournalTransaction = new FinJournalTransaction();
					hPPlogisticMSGJournalTransaction
							.setComments("System Credit HPP-logistic:"
									+ finSalesRecord.getVenOrderItem()
											.getWcsOrderItemId());
					hPPlogisticMSGJournalTransaction.setCreditDebitFlag(true);
					hPPlogisticMSGJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);

					FinAccount hPPlogisticMSGFinAccount = new FinAccount();
					hPPlogisticMSGFinAccount
							.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110001.id());
					hPPlogisticMSGJournalTransaction
							.setFinAccount(hPPlogisticMSGFinAccount);
					hPPlogisticMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					hPPlogisticMSGJournalTransaction
							.setFinJournal(finJournalSales);
					hPPlogisticMSGJournalTransaction.setFinPeriod(finPeriod);
					hPPlogisticMSGJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType hPPlogisticMSG = new FinTransactionType();
					hPPlogisticMSG
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_HPP_LOGISTIK.id());

					hPPlogisticMSGJournalTransaction
							.setFinTransactionType(hPPlogisticMSG);
					hPPlogisticMSGJournalTransaction
							.setTransactionAmount(totalShippingCost);
					hPPlogisticMSGJournalTransaction
							.setTransactionTimestamp(new Timestamp(System
									.currentTimeMillis()));

					/*
					 * Add the sales record to the list Note that we need a new
					 * list because collections are read only
					 */
					transactionList.add(hPPlogisticMSGJournalTransaction);

					for (FinSalesRecord salesRecord : finSalesRecordList) {
						salesRecord.setFinJournalTransactions(transactionList);
					}

					hPPlogisticMSGJournalTransaction
							.setFinSalesRecords(finSalesRecordList);
					hPPlogisticMSGJournalTransaction
							.setWcsOrderID(finSalesRecord.getVenOrderItem()
									.getVenOrder().getWcsOrderId() != null ? finSalesRecord
									.getVenOrderItem().getVenOrder()
									.getWcsOrderId()
									: null);
					//hPPlogisticMSGJournalTransaction = journalTransactionHome.persistFinJournalTransaction(hPPlogisticMSGJournalTransaction);
					if (!em.contains(hPPlogisticMSGJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), 
								"postSalesJournalTransaction:: hPPlogisticMSGJournalTransaction not attached, calling save explicitly.");
						hPPlogisticMSGJournalTransaction = finJournalTransactionDAO.save(hPPlogisticMSGJournalTransaction);
					}

					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set  HPP-logistic AccNo: "
							+ FinAccountConstants.FIN_ACCOUNT_5110001.id()
							+ " Amount: "
							+ hPPlogisticMSGJournalTransaction
									.getTransactionAmount());
				}
			}

			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT.label())) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction::MERCHANT TYPE: CONSIGNMENT");

				/*
				 * Post the DEBIT for Acc no : 1180001, Desc : Inventory
				 * 
				 * value supposed to be from INVENTORY. Pending until Inventory
				 * Module is done
				 */
				FinJournalTransaction inventoryJournalTransaction = new FinJournalTransaction();
				inventoryJournalTransaction
						.setComments("System Debit Inventory:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				inventoryJournalTransaction.setCreditDebitFlag(false);
				inventoryJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finInventory = new FinAccount();
				finInventory.setAccountId(FinAccountConstants.FIN_ACCOUNT_1180001.id());
				inventoryJournalTransaction.setFinAccount(finInventory);
				inventoryJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				inventoryJournalTransaction.setFinJournal(finJournalSales);
				inventoryJournalTransaction.setFinPeriod(finPeriod);
				inventoryJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeInventory = new FinTransactionType();
				finTransactionTypeInventory
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_INVENTORY.id());

				inventoryJournalTransaction
						.setFinTransactionType(finTransactionTypeInventory);
				inventoryJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
				inventoryJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(inventoryJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				inventoryJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				inventoryJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//inventoryJournalTransaction = journalTransactionHome.persistFinJournalTransaction(inventoryJournalTransaction);
				if (!em.contains(inventoryJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postSalesJournalTransaction:: inventoryJournalTransaction not attached, calling save.");
					inventoryJournalTransaction = finJournalTransactionDAO.save(inventoryJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Inventory AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_1180001.id() + " Amount: "
						+ inventoryJournalTransaction.getTransactionAmount());

				/*
				 * Post the CREDIT for Acc no : 2150099, Desc : Barang Diterima
				 * Dimuka
				 * 
				 * value supposed to be from INVENTORY. Pending until Inventory
				 * Module is done
				 */
				FinJournalTransaction barangDiterimaDimukaJournalTransaction = new FinJournalTransaction();
				barangDiterimaDimukaJournalTransaction
						.setComments("System Credit Barang Diterima Dimuka:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				barangDiterimaDimukaJournalTransaction.setCreditDebitFlag(true);
				barangDiterimaDimukaJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finBarangDiterimaDimuka = new FinAccount();
				finBarangDiterimaDimuka
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2150099.id());
				barangDiterimaDimukaJournalTransaction
						.setFinAccount(finBarangDiterimaDimuka);
				barangDiterimaDimukaJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				barangDiterimaDimukaJournalTransaction
						.setFinJournal(finJournalSales);
				barangDiterimaDimukaJournalTransaction.setFinPeriod(finPeriod);
				barangDiterimaDimukaJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBarangDiterimaDimuka = new FinTransactionType();
				finTransactionTypeBarangDiterimaDimuka
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BARANG_DITERIMA_DIMUKA.id());

				barangDiterimaDimukaJournalTransaction
						.setFinTransactionType(finTransactionTypeBarangDiterimaDimuka);
				barangDiterimaDimukaJournalTransaction
						.setTransactionAmount(new BigDecimal(0));
				barangDiterimaDimukaJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(barangDiterimaDimukaJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				barangDiterimaDimukaJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				barangDiterimaDimukaJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//barangDiterimaDimukaJournalTransaction = journalTransactionHome.persistFinJournalTransaction(barangDiterimaDimukaJournalTransaction);
				if (!em.contains(barangDiterimaDimukaJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postsalesJournalTransaction:: barangDiterimaDimukaJournalTransaction not attached, calling save.");
					barangDiterimaDimukaJournalTransaction = finJournalTransactionDAO.save(barangDiterimaDimukaJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Barang Diterima Dimuka AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_2150099.id()
						+ " Amount: "
						+ barangDiterimaDimukaJournalTransaction
								.getTransactionAmount());
			}

			if (merchantCommisionType
					.equals(VenSettlementRecordCommissionTypeConstants.VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER.label())) {
				System.out.println("MERCHANT TYPE: TRADING MP");

				/*
				 * Post the DEBIT for Acc no : 1180001, Desc : Persediaan Barang
				 * Dagang
				 * 
				 * value supposed to be from INVENTORY. Pending until Inventory
				 * Module is done
				 */
				FinJournalTransaction persediaanBarangDagangJournalTransaction = new FinJournalTransaction();
				persediaanBarangDagangJournalTransaction
						.setComments("System Debit Persediaan Barang Dagang:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				persediaanBarangDagangJournalTransaction
						.setCreditDebitFlag(false);
				persediaanBarangDagangJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finPersediaanBarangDagang = new FinAccount();
				finPersediaanBarangDagang
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_1180001.id());
				persediaanBarangDagangJournalTransaction
						.setFinAccount(finPersediaanBarangDagang);
				persediaanBarangDagangJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				persediaanBarangDagangJournalTransaction
						.setFinJournal(finJournalSales);
				persediaanBarangDagangJournalTransaction
						.setFinPeriod(finPeriod);
				persediaanBarangDagangJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypePersediaanBarangDagang = new FinTransactionType();
				finTransactionTypePersediaanBarangDagang
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PERSEDIAAN_BARANG_DAGANG.id());

				persediaanBarangDagangJournalTransaction
						.setFinTransactionType(finTransactionTypePersediaanBarangDagang);
				persediaanBarangDagangJournalTransaction
						.setTransactionAmount((finSalesRecord.getVenOrderItem()
								.getTotal().subtract(finSalesRecord
								.getGdnCommissionAmount())).divide(
								GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
				persediaanBarangDagangJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(persediaanBarangDagangJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				persediaanBarangDagangJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				persediaanBarangDagangJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//persediaanBarangDagangJournalTransaction = journalTransactionHome.persistFinJournalTransaction(persediaanBarangDagangJournalTransaction);
				if (!em.contains(persediaanBarangDagangJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postSalesJournalTransaction:: persediaanBarangDagangJournalTransaction not attached, calling save.");
					persediaanBarangDagangJournalTransaction = finJournalTransactionDAO.save(persediaanBarangDagangJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Persediaan Barang Dagang AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_1180001.id()
						+ " Amount: "
						+ persediaanBarangDagangJournalTransaction
								.getTransactionAmount());

				/*
				 * Post the CREDIT for Acc no : 2150099, Desc : Barang Diterima
				 * Dimuka
				 * 
				 * value supposed to be from INVENTORY. Pending until Inventory
				 * Module is done
				 */
				FinJournalTransaction barangDiterimaDimukaJournalTransaction = new FinJournalTransaction();
				barangDiterimaDimukaJournalTransaction
						.setComments("System Credit Barang Diterima Dimuka:"
								+ finSalesRecord.getVenOrderItem()
										.getWcsOrderItemId());
				barangDiterimaDimukaJournalTransaction.setCreditDebitFlag(true);
				barangDiterimaDimukaJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finBarangDiterimaDimuka = new FinAccount();
				finBarangDiterimaDimuka
						.setAccountId(FinAccountConstants.FIN_ACCOUNT_2150099.id());
				barangDiterimaDimukaJournalTransaction
						.setFinAccount(finBarangDiterimaDimuka);
				barangDiterimaDimukaJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				barangDiterimaDimukaJournalTransaction
						.setFinJournal(finJournalSales);
				barangDiterimaDimukaJournalTransaction.setFinPeriod(finPeriod);
				barangDiterimaDimukaJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionTypeBarangDiterimaDimuka = new FinTransactionType();
				finTransactionTypeBarangDiterimaDimuka
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_BARANG_DITERIMA_DIMUKA.id());

				barangDiterimaDimukaJournalTransaction
						.setFinTransactionType(finTransactionTypeBarangDiterimaDimuka);
				barangDiterimaDimukaJournalTransaction
						.setTransactionAmount((finSalesRecord.getVenOrderItem()
								.getTotal().subtract(finSalesRecord
								.getGdnCommissionAmount())).divide(
								GDNPPN_DIVISOR, 2, RoundingMode.HALF_UP));
				barangDiterimaDimukaJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				/*
				 * Add the sales record to the list Note that we need a new list
				 * because collections are read only
				 */
				transactionList.add(barangDiterimaDimukaJournalTransaction);

				for (FinSalesRecord salesRecord : finSalesRecordList) {
					salesRecord.setFinJournalTransactions(transactionList);
				}

				barangDiterimaDimukaJournalTransaction
						.setFinSalesRecords(finSalesRecordList);
				barangDiterimaDimukaJournalTransaction
						.setWcsOrderID(finSalesRecord.getVenOrderItem()
								.getVenOrder().getWcsOrderId() != null ? finSalesRecord
								.getVenOrderItem().getVenOrder()
								.getWcsOrderId()
								: null);
				//barangDiterimaDimukaJournalTransaction = journalTransactionHome.persistFinJournalTransaction(barangDiterimaDimukaJournalTransaction);
				if (!em.contains(barangDiterimaDimukaJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postSalesJournalTransaction:: barangDiterimaDimukaJournalTransaction not attached, calling save.");
					barangDiterimaDimukaJournalTransaction = finJournalTransactionDAO.save(barangDiterimaDimukaJournalTransaction);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransaction:: Set Barang Diterima Dimuka AccNo: "
						+ FinAccountConstants.FIN_ACCOUNT_2150099.id()
						+ " Amount: "
						+ barangDiterimaDimukaJournalTransaction
								.getTransactionAmount());
			}

			// Set the list of transactions associated with the sales record and
			// merge
			finSalesRecord.setFinJournalTransactions(transactionList);
			//salesRecordHome.mergeFinSalesRecord(finSalesRecord);
			if (!em.contains(finSalesRecord)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), 
						"postSalesJournalTransaction:: finSalesRecord not attached, calling save.");
				finSalesRecord = finSalesRecordDAO.save(finSalesRecord);
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting sales journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postSalesJournalTransactions::" + " completed in "
				+ duration + "ms");

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postPaymentJournalTransaction(Long finApPaymentId)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postPaymentJournalTransaction(Long finApPaymentId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {
			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");

			FinApPaymentSessionEJBLocal finApPaymentHome = (FinApPaymentSessionEJBLocal) this._genericLocator
					.lookupLocal(FinApPaymentSessionEJBLocal.class,
							"FinApPaymentSessionEJBBeanLocal");

			FinArFundsInRefundSessionEJBLocal refundRecordHome = (FinArFundsInRefundSessionEJBLocal) this._genericLocator
					.lookupLocal(FinArFundsInRefundSessionEJBLocal.class,
							"FinArFundsInRefundSessionEJBBeanLocal");

			VenSettlementRecordSessionEJBLocal settlementHome = (VenSettlementRecordSessionEJBLocal) this._genericLocator
					.lookupLocal(VenSettlementRecordSessionEJBLocal.class,
							"VenSettlementRecordSessionEJBBeanLocal");
			*/
			/*
			 * Get the accounts payable payment record
			 */
			/*
			List<FinApPayment> finApPaymentList = finApPaymentHome
					.queryByRange(
							"select o from FinApPayment o join fetch o.finSalesRecords where o.apPaymentId = "
									+ finApPaymentId, 0, 0);
			*/
			List<FinApPayment> finApPaymentList = finApPaymentDAO.findByApPaymentIdJoinFinSalesRecord(finApPaymentId);
			if (finApPaymentList.isEmpty()) {
				throw new EJBException(
						"The payment ID passed to postPaymentJournalTransaction is invalid. No such payment exists!");
			}

			FinApPayment finApPayment = finApPaymentList.get(0);

			/*
			 * Lookup the payment journal approval group for the day If there is
			 * none then create it
			 */
			FinJournal finJournalPayment = new FinJournal();
			finJournalPayment.setJournalId(VeniceConstants.FIN_JOURNAL_PAYMENT);
			FinJournalApprovalGroup finJournalApprovalGroup = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			/*
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = journalApprovalGroupHome
					.queryByRange(
							"select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Payment Journal for:"
									+ sdf.format(new Date())
									+ "'"
									+ " and o.finApprovalStatus.approvalStatusId = "
									+ VeniceConstants.FIN_APPROVAL_STATUS_NEW,
							0, 0);\
			*/
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = finJournalApprovalGroupDAO.findByDescriptionAndApprovalStatusId(
					"Payment Journal for:" + sdf.format(new Date()), FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
			if (finJournalApprovalGroupList.isEmpty()) {
				finJournalApprovalGroup = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
				finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);

				finJournalApprovalGroup.setFinJournal(finJournalPayment);

				finJournalApprovalGroup
						.setJournalGroupDesc("Payment Journal for:"
								+ sdf.format(new Date()));
				finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// Persist the journal group				
				//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postPaymentJournalTransaction:: finJournalApprovalGroup not attached, calling save to attach.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			} else {
				finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			}

			/*
			 * Post payment journal transaction for to debit the debt account o
			 * If the party is a logistics provider post to 2140001 Hutang
			 * Logistik else if the party is a merchant then post to 2130001
			 * Hutang Merchant
			 */
			FinJournalTransaction paymentJournalTransaction = new FinJournalTransaction();
			paymentJournalTransaction
					.setComments("Payment Journal Debit for Party:"
							+ finApPayment.getVenParty().getFullOrLegalName());
			paymentJournalTransaction.setCreditDebitFlag(false);
			paymentJournalTransaction
					.setFinJournalApprovalGroup(finJournalApprovalGroup);

			Long finAccountId = null;
			Long finTransactionTypeId = null;
			boolean trueOrfalse = true;
			BigDecimal creditAmount = new BigDecimal(0);
			creditAmount.setScale(2, RoundingMode.HALF_UP);

			FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
			finTransactionStatus
					.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
			paymentJournalTransaction
					.setFinTransactionStatus(finTransactionStatus);

			if (finApPayment.getVenParty().getVenPartyType().getPartyTypeId() == VenPartyTypeConstants.VEN_PARTY_TYPE_MERCHANT.code()) {

				finTransactionTypeId = FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_MERCHANT_PAYMENT.id();

				/*
				 * Post payment journal transaction for to debit the pph23
				 * account
				 */
				FinJournalTransaction pph23JournalTransaction = new FinJournalTransaction();
				pph23JournalTransaction
						.setComments("Payment Journal Debit of PPH 23 for Party:"
								+ finApPayment.getVenParty()
										.getFullOrLegalName());
				pph23JournalTransaction.setCreditDebitFlag(false);
				pph23JournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccount = new FinAccount();
				finAccount.setAccountId(new Long(
						FinAccountConstants.FIN_ACCOUNT_1190001.id()));
				pph23JournalTransaction.setFinAccount(finAccount);

				pph23JournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				pph23JournalTransaction.setFinJournal(finJournalPayment);
				pph23JournalTransaction.setFinPeriod(FinancePeriodUtil
						.getCurrentPeriod());
				FinTransactionType finTransactionType = new FinTransactionType();
				finTransactionType.setTransactionTypeId(finTransactionTypeId);
				pph23JournalTransaction
						.setFinTransactionType(finTransactionType);

				BigDecimal pphAmount = finApPayment.getPph23Amount() == null ? new BigDecimal(
						0) : finApPayment.getPph23Amount();
				pphAmount.setScale(2, RoundingMode.HALF_UP);
				pph23JournalTransaction.setTransactionAmount(pphAmount);
				pph23JournalTransaction.setTransactionTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// if (pph23JournalTransaction.getFinApPayments() == null) {
				// pph23JournalTransaction.setFinApPayments(finApPaymentList);
				// }
				pph23JournalTransaction
						.setWcsOrderID(finApPayment.getFinAccount()
								.getAccountNumber() != null ? finApPayment
								.getFinAccount().getAccountNumber().toString()
								: null);

				// Persist the payment journal transaction
				//pph23JournalTransaction = journalTransactionHome.persistFinJournalTransaction(pph23JournalTransaction);
				if (!em.contains(pph23JournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postPaymentJournalTransaction:: pph23JournalTransaction not attached, calling save.");
					pph23JournalTransaction = finJournalTransactionDAO.save(pph23JournalTransaction);
				}
				transactionList.add(pph23JournalTransaction);

				/*
				 * Create a credit transaction for the merchant claim
				 */
				FinJournalTransaction merchantClaimJournalTransaction = new FinJournalTransaction();
				merchantClaimJournalTransaction
						.setComments("Payment Journal Credit of Merchant Claim of Party:"
								+ finApPayment.getVenParty()
										.getFullOrLegalName());
				merchantClaimJournalTransaction.setCreditDebitFlag(true);
				merchantClaimJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finBankAccount = new FinAccount();
				finBankAccount.setAccountId(new Long(
						FinAccountConstants.FIN_ACCOUNT_1140002.id()));
				merchantClaimJournalTransaction.setFinAccount(finBankAccount);

				merchantClaimJournalTransaction
						.setFinJournal(finJournalPayment);
				merchantClaimJournalTransaction.setFinPeriod(FinancePeriodUtil
						.getCurrentPeriod());

				merchantClaimJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				merchantClaimJournalTransaction
						.setFinTransactionType(finTransactionType);

				BigDecimal arAmount = finApPayment.getPenaltyAmount();
				arAmount.setScale(2, RoundingMode.HALF_UP);
				merchantClaimJournalTransaction.setTransactionAmount(arAmount);
				merchantClaimJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				// if (merchantClaimJournalTransaction.getFinApPayments() ==
				// null) {
				// merchantClaimJournalTransaction.setFinApPayments(finApPaymentList);
				// }
				merchantClaimJournalTransaction
						.setWcsOrderID(finApPayment.getFinAccount()
								.getAccountNumber() != null ? finApPayment
								.getFinAccount().getAccountNumber().toString()
								: null);

				// Persist the merchant claim journal transaction
				//merchantClaimJournalTransaction = journalTransactionHome.persistFinJournalTransaction(merchantClaimJournalTransaction);
				if (!em.contains(merchantClaimJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postPaymentJournalTransaction:: merchantClaimJournalTransaction not attached, calling save.");
					merchantClaimJournalTransaction = finJournalTransactionDAO.save(merchantClaimJournalTransaction);
				}
				transactionList.add(merchantClaimJournalTransaction);

				finAccountId = new Long(FinAccountConstants.FIN_ACCOUNT_2130001.id());
				/*
				VenSettlementRecord settlementRecord = settlementHome
						.queryByRange(
								"select o from VenSettlementRecord o where o.venOrderItem.orderItemId = "
										+ finApPayment.getFinSalesRecords()
												.get(0).getVenOrderItem()
												.getOrderItemId(), 0, 1).get(0);
				*/
				VenSettlementRecord settlementRecord = venSettlementRecordDAO.findByOrderItemId(
						finApPayment.getFinSalesRecords().get(0).getVenOrderItem().getOrderItemId()).get(0);

				if (settlementRecord.getCommissionType().equals("CM")) {
					creditAmount = finApPayment.getAmount().add(pphAmount)
							.subtract(arAmount);
				} else if (settlementRecord.getCommissionType().equals("RB")) {
					creditAmount = finApPayment.getAmount().subtract(arAmount);
				}
			} else if (finApPayment.getVenParty().getVenPartyType()
					.getPartyTypeId() == VenPartyTypeConstants.VEN_PARTY_TYPE_LOGISTICS.code()) {
				finAccountId = new Long(FinAccountConstants.FIN_ACCOUNT_2140001.id());
				finTransactionTypeId = FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_LOGISTICS_PAYMENT.id();
				creditAmount = finApPayment.getAmount();
			} else if (finApPayment.getVenParty().getVenPartyType()
					.getPartyTypeId() == VenPartyTypeConstants.VEN_PARTY_TYPE_BANK.code()
					|| finApPayment.getVenParty().getVenPartyType()
							.getPartyTypeId() == VenPartyTypeConstants.VEN_PARTY_TYPE_CUSTOMER.code()
					|| finApPayment.getVenParty().getVenPartyType()
							.getPartyTypeId() == VenPartyTypeConstants.VEN_PARTY_TYPE_RECIPIENT.code()) {
				trueOrfalse = false;
				/*
				List<FinArFundsInRefund> finArFundsInRefundList = refundRecordHome
						.queryByRange(
								"select o from FinArFundsInRefund o where o.finApPayment.apPaymentId = "
										+ finApPaymentId, 0, 0);
				*/
				List<FinArFundsInRefund> finArFundsInRefundList = finArFundsInRefundDAO.findByApPaymentId(finApPaymentId);
				if (finArFundsInRefundList.isEmpty()) {
					throw new EJBException(
							"The finArFundsInRefundId passed to createRefundPayment is invalid. Refund entry does not exist!");
				}
				FinArFundsInRefund finArFundsInRefundItem = finArFundsInRefundList
						.get(0);

				Long type = finArFundsInRefundItem
						.getFinArFundsInReconRecord()
						.getFinArFundsInActionApplied()
						.getActionAppliedId()
						.equals(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id()) ? FinAccountConstants.FIN_ACCOUNT_2170002.id()
						: FinAccountConstants.FIN_ACCOUNT_2170001.id();
				Long transType = finArFundsInRefundItem
						.getFinArFundsInReconRecord()
						.getFinArFundsInActionApplied()
						.getActionAppliedId()
						.equals(FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id()) ? FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_REFUND_BANK.id()
						: FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_REFUND_PELANGGAN.id();
				String wcsOrderId = finArFundsInRefundItem
						.getFinArFundsInReconRecord().getWcsOrderId() != null ? finArFundsInRefundItem
						.getFinArFundsInReconRecord().getWcsOrderId() : "";
				FinTransactionType finTransactionType = new FinTransactionType();
				finTransactionType.setTransactionTypeId(transType);
				/*
				 * Post the Ap payment journal transaction
				 */
				FinJournalTransaction apPaymentJournalTransaction = new FinJournalTransaction();
				apPaymentJournalTransaction.setComments("System Credit Bank :"
						+ wcsOrderId);
				apPaymentJournalTransaction.setCreditDebitFlag(true);
				apPaymentJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountApPayment = new FinAccount();
				finAccountApPayment.setAccountId(finApPayment.getFinAccount()
						.getAccountId());
				apPaymentJournalTransaction.setFinAccount(finAccountApPayment);

				List<FinArFundsInReconRecord> apPAymentFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
				apPAymentFundsInRecordList.add(finArFundsInRefundItem
						.getFinArFundsInReconRecord());
				apPaymentJournalTransaction
						.setFinArFundsInReconRecords(apPAymentFundsInRecordList);
				apPaymentJournalTransaction.setFinJournal(finJournalPayment);
				apPaymentJournalTransaction.setFinPeriod(FinancePeriodUtil
						.getCurrentPeriod());
				apPaymentJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);
				apPaymentJournalTransaction
						.setFinTransactionType(finTransactionType);
				apPaymentJournalTransaction
						.setTransactionAmount(finArFundsInRefundItem
								.getFinArFundsInReconRecord()
								.getProviderReportPaidAmount());
				apPaymentJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				apPaymentJournalTransaction.setWcsOrderID(wcsOrderId);
				apPaymentJournalTransaction
						.setVenBank(finArFundsInRefundItem
								.getFinArFundsInReconRecord()
								.getVenOrderPayment() != null ? finArFundsInRefundItem
								.getFinArFundsInReconRecord()
								.getVenOrderPayment().getVenBank()
								: finArFundsInRefundItem
										.getFinArFundsInReconRecord()
										.getFinArFundsInReport()
										.getFinArFundsInReportType()
										.getVenBank());
				apPaymentJournalTransaction.setGroupJournal(finApPayment
						.getFinAccount().getAccountId());
				// Persist the fee journal transaction
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentJournalTransaction::Save ( AP Payment)");
				//journalTransactionHome.persistFinJournalTransaction(apPaymentJournalTransaction);
				if (!em.contains(apPaymentJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"postPaymentJournalTransaction:: apPaymentJournalTransaction not attached, calling save.");
					apPaymentJournalTransaction = finJournalTransactionDAO.save(apPaymentJournalTransaction);
				}
				transactionList.add(apPaymentJournalTransaction);

				if (finArFundsInRefundItem
						.getFinArFundsInReconRecord()
						.getFinArFundsInActionApplied()
						.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id()) {
					/*
					 * Post the journal transaction for bank fees
					 */
					FinJournalTransaction bankFeeJournalTransaction = new FinJournalTransaction();
					bankFeeJournalTransaction.setComments("System Credit Bank Fee:" + wcsOrderId);
					bankFeeJournalTransaction.setCreditDebitFlag(true);
					bankFeeJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);
					// bankFeeJournalTransaction.setFinAccount(finAccountBank);

					// salah save. seharusnya save account number hpp bank
					// FIN_ACCOUNT_5110003
					FinAccount finAccountBankFee = new FinAccount();
					finAccountBankFee.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110003.id());
					bankFeeJournalTransaction.setFinAccount(finAccountBankFee);

					List<FinArFundsInReconRecord> bankFeeFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
					bankFeeFundsInRecordList.add(finArFundsInRefundItem
							.getFinArFundsInReconRecord());
					bankFeeJournalTransaction
							.setFinArFundsInReconRecords(bankFeeFundsInRecordList);
					bankFeeJournalTransaction.setFinJournal(finJournalPayment);
					bankFeeJournalTransaction.setFinPeriod(FinancePeriodUtil
							.getCurrentPeriod());
					bankFeeJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);
					bankFeeJournalTransaction
							.setFinTransactionType(finTransactionType);
					bankFeeJournalTransaction
							.setTransactionAmount(finArFundsInRefundItem
									.getFinArFundsInReconRecord()
									.getProviderReportFeeAmount());
					bankFeeJournalTransaction
							.setTransactionTimestamp(new Timestamp(System
									.currentTimeMillis()));

					bankFeeJournalTransaction.setWcsOrderID(wcsOrderId);
					bankFeeJournalTransaction
							.setVenBank(finArFundsInRefundItem
									.getFinArFundsInReconRecord()
									.getVenOrderPayment() != null ? finArFundsInRefundItem
									.getFinArFundsInReconRecord()
									.getVenOrderPayment().getVenBank()
									: finArFundsInRefundItem
											.getFinArFundsInReconRecord()
											.getFinArFundsInReport()
											.getFinArFundsInReportType()
											.getVenBank());
					bankFeeJournalTransaction.setGroupJournal(finApPayment
							.getFinAccount().getAccountId());
					// Persist the fee journal transaction
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentJournalTransaction::Save ( HPP BIAYA Bank)");
					//journalTransactionHome.persistFinJournalTransaction(bankFeeJournalTransaction);
					if (!em.contains(bankFeeJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), 
								"postPaymentJournalTransaction:: bankFeeJournalTransaction not attached, calling save.");
						bankFeeJournalTransaction = finJournalTransactionDAO.save(bankFeeJournalTransaction);
					}
					transactionList.add(bankFeeJournalTransaction);
				}
				/*
				 * Post the journal transaction for Refund
				 */
				FinJournalTransaction refundJournalTransaction = new FinJournalTransaction();
				refundJournalTransaction.setComments("System Debit:" + wcsOrderId);
				refundJournalTransaction.setCreditDebitFlag(false);
				refundJournalTransaction.setFinJournalApprovalGroup(finJournalApprovalGroup);
				// bankFeeJournalTransaction.setFinAccount(finAccountBank);

				// salah save. seharusnya save account number hpp bank
				// FIN_ACCOUNT_5110003
				FinAccount finAccountRefrund = new FinAccount();
				finAccountRefrund.setAccountId(type);
				refundJournalTransaction.setFinAccount(finAccountRefrund);

				List<FinArFundsInReconRecord> refundFundsInRecordList = new ArrayList<FinArFundsInReconRecord>();
				refundFundsInRecordList.add(finArFundsInRefundItem.getFinArFundsInReconRecord());
				refundJournalTransaction.setFinArFundsInReconRecords(refundFundsInRecordList);
				refundJournalTransaction.setFinJournal(finJournalPayment);
				refundJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
				refundJournalTransaction.setFinTransactionStatus(finTransactionStatus);

				refundJournalTransaction.setFinTransactionType(finTransactionType);
				refundJournalTransaction.setTransactionAmount(finApPayment.getAmount());
				refundJournalTransaction.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));

				refundJournalTransaction.setWcsOrderID(wcsOrderId);
				refundJournalTransaction
						.setVenBank(finArFundsInRefundItem
								.getFinArFundsInReconRecord()
								.getVenOrderPayment() != null ? finArFundsInRefundItem
								.getFinArFundsInReconRecord()
								.getVenOrderPayment().getVenBank()
								: finArFundsInRefundItem
										.getFinArFundsInReconRecord()
										.getFinArFundsInReport()
										.getFinArFundsInReportType()
										.getVenBank());
				refundJournalTransaction.setGroupJournal(finApPayment
						.getFinAccount().getAccountId());
				// Persist the fee journal transaction
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentJournalTransaction::Save ( REFUND)");
				//journalTransactionHome.persistFinJournalTransaction(refundJournalTransaction);
				if (!em.contains(refundJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentJournalTransaction:: refundJournalTransaction not attached, calling save.");
					refundJournalTransaction = finJournalTransactionDAO.save(refundJournalTransaction);
				}
				transactionList.add(refundJournalTransaction);
			}

			if (trueOrfalse) {
				FinAccount finAccount = new FinAccount();
				finAccount.setAccountId(finAccountId);
				paymentJournalTransaction.setFinAccount(finAccount);

				paymentJournalTransaction.setFinJournal(finJournalPayment);
				paymentJournalTransaction.setFinPeriod(FinancePeriodUtil
						.getCurrentPeriod());
				FinTransactionType finTransactionType = new FinTransactionType();
				finTransactionType.setTransactionTypeId(finTransactionTypeId);
				paymentJournalTransaction
						.setFinTransactionType(finTransactionType);

				BigDecimal debitAmount = finApPayment.getAmount();
				debitAmount.setScale(2, RoundingMode.HALF_UP);
				paymentJournalTransaction.setTransactionAmount(debitAmount);
				paymentJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				// if (paymentJournalTransaction.getFinApPayments() == null) {
				// paymentJournalTransaction.setFinApPayments(finApPaymentList);
				// }
				paymentJournalTransaction
						.setWcsOrderID(finApPayment.getFinAccount()
								.getAccountNumber() != null ? finApPayment
								.getFinAccount().getAccountNumber().toString()
								: null);

				// Persist the payment journal transaction
				//paymentJournalTransaction = journalTransactionHome.persistFinJournalTransaction(paymentJournalTransaction);
				if (!em.contains(paymentJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postPaymentJournalTransaction:: paymentJournalTransaction not attached, calling save.");
					paymentJournalTransaction = finJournalTransactionDAO.save(paymentJournalTransaction);
				}
				transactionList.add(paymentJournalTransaction);

				/*
				 * Create a credit transaction for the bank account of the
				 * payment for the amount of the payment.
				 */
				FinJournalTransaction bankPaymentJournalTransaction = new FinJournalTransaction();
				bankPaymentJournalTransaction
						.setComments("Payment Journal Credit for Bank Account:"
								+ finApPayment.getFinAccount()
										.getAccountNumber() + ":"
								+ finApPayment.getFinAccount().getAccountDesc());
				bankPaymentJournalTransaction.setCreditDebitFlag(true);
				bankPaymentJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finBankAccount = new FinAccount();
				finBankAccount.setAccountId(finApPayment.getFinAccount()
						.getAccountId());
				bankPaymentJournalTransaction.setFinAccount(finBankAccount);

				bankPaymentJournalTransaction.setFinJournal(finJournalPayment);
				bankPaymentJournalTransaction.setFinPeriod(FinancePeriodUtil
						.getCurrentPeriod());

				bankPaymentJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);

				bankPaymentJournalTransaction
						.setFinTransactionType(finTransactionType);

				bankPaymentJournalTransaction
						.setTransactionAmount(creditAmount);
				bankPaymentJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				// if (bankPaymentJournalTransaction.getFinApPayments() == null)
				// {
				// bankPaymentJournalTransaction.setFinApPayments(finApPaymentList);
				// }
				bankPaymentJournalTransaction
						.setWcsOrderID(finApPayment.getFinAccount()
								.getAccountNumber() != null ? finApPayment
								.getFinAccount().getAccountNumber().toString()
								: null);

				// Persist the payment journal transaction
				//bankPaymentJournalTransaction = journalTransactionHome.persistFinJournalTransaction(bankPaymentJournalTransaction);
				if (!em.contains(bankPaymentJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postPaymentJournalTransaction:: bankPaymentJournalTransaction not attached, calling save.");
					bankPaymentJournalTransaction = finJournalTransactionDAO.save(bankPaymentJournalTransaction);
				}
				transactionList.add(bankPaymentJournalTransaction);

				// Set the link to the list of payment journal transactions and
				// merge
				finApPayment.setFinJournalTransactions(transactionList);

				// Set status to Done
				FinApprovalStatus approvalStatus = new FinApprovalStatus();
				approvalStatus.setApprovalStatusId(new Long(4));
				finApPayment.setFinApprovalStatus(approvalStatus);
				//finApPayment = finApPaymentHome.mergeFinApPayment(finApPayment);
				if (!em.contains(finApPayment)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postPaymentJournalTransaction:: finApPayment not attached, calling save.");
					finApPayment = finApPaymentDAO.save(finApPayment);
				}
			}
		} catch (Exception e) {
			String errMsg = "An Exception occured when posting payment journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentJournalTransaction::" + " completed in "
				+ duration + "ms");

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postManualJournalTransaction(String journalApprovalGroupDesc, Long
	 * finAccountId, Double amount, Long finTransactionTypeId, Boolean
	 * creditOrDebit, String comments)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postManualJournalTransaction(
			String journalApprovalGroupDesc,
			Date journalApprovalGroupTimestamp,
			ArrayList<ManualJournalEntryDTO> journalEntryList) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postManualJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {
			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");

			FinApManualJournalTransactionSessionEJBLocal manualJournalTransactionHome = (FinApManualJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(
							FinApManualJournalTransactionSessionEJBLocal.class,
							"FinApManualJournalTransactionSessionEJBBeanLocal");

			VenOrderSessionEJBLocal venOrderHome = (VenOrderSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderSessionEJBLocal.class,
							"VenOrderSessionEJBBeanLocal");

			VenPartySessionEJBLocal venPartyHome = (VenPartySessionEJBLocal) this._genericLocator
					.lookupLocal(VenPartySessionEJBLocal.class,
							"VenPartySessionEJBBeanLocal");
			*/

			// Removed this because manual journals need to be treated as
			// individual vouchers

			/*
			 * Lookup the manual journal approval group for the day If there is
			 * none then create it
			 */
			// FinJournal finJournalManual = new FinJournal();
			// finJournalManual.setJournalId(VeniceConstants.FIN_JOURNAL_MANUAL);
			// FinJournalApprovalGroup finJournalApprovalGroup = null;
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			// List<FinJournalApprovalGroup> finJournalApprovalGroupList =
			// journalApprovalGroupHome
			// .queryByRange(
			// "select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Manual Journal for:"
			// + sdf.format(new Date()) + "'" +
			// " and o.finApprovalStatus.approvalStatusId = " +
			// VeniceConstants.FIN_APPROVAL_STATUS_NEW, 0, 0);
			// if (finJournalApprovalGroupList.isEmpty()) {
			// finJournalApprovalGroup = new FinJournalApprovalGroup();
			// FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
			// finApprovalStatus
			// .setApprovalStatusId(VeniceConstants.FIN_APPROVAL_STATUS_NEW);
			// finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);
			//
			// finJournalApprovalGroup.setFinJournal(finJournalManual);
			//
			// finJournalApprovalGroup
			// .setJournalGroupDesc("Manual Journal for:"
			// + sdf.format(new Date()));
			// finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(
			// System.currentTimeMillis()));
			//
			// // Persist the journal group
			// finJournalApprovalGroup = journalApprovalGroupHome
			// .persistFinJournalApprovalGroup(finJournalApprovalGroup);
			// } else {
			// finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			// }

			FinJournalApprovalGroup finJournalApprovalGroup = new FinJournalApprovalGroup();
			FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
			finApprovalStatus.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
			finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);

			FinJournal finJournalManual = new FinJournal();
			finJournalManual.setJournalId(FinJournalConstants.FIN_JOURNAL_MANUAL.id());
			finJournalApprovalGroup.setFinJournal(finJournalManual);

			finJournalApprovalGroup
					.setJournalGroupDesc(journalApprovalGroupDesc);
			finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(
					journalApprovalGroupTimestamp.getTime()));

			// Persist the journal group
			//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
			if (!em.contains(finJournalApprovalGroup)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "postManualJournalTransaction:: finJournalApprovalGroup not attached, calling save.");
				finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
			}

			for (ManualJournalEntryDTO journalEntry : journalEntryList) {

				/*
				 * Post manual journal transaction
				 */
				FinJournalTransaction manualJournalTransaction = new FinJournalTransaction();
				manualJournalTransaction
						.setComments(journalEntry.getComments());
				manualJournalTransaction.setCreditDebitFlag(journalEntry
						.getCreditOrDebit());
				manualJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccount = new FinAccount();
				finAccount.setAccountId(journalEntry.getFinAccountId());
				manualJournalTransaction.setFinAccount(finAccount);

				manualJournalTransaction.setFinJournal(finJournalManual);
				manualJournalTransaction.setFinPeriod(FinancePeriodUtil
						.getCurrentPeriod());

				FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
				finTransactionStatus.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
				manualJournalTransaction.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionType = new FinTransactionType();
				finTransactionType
						.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_MANUAL_ADJUSTMENT_JOURNAL.id());
				manualJournalTransaction
						.setFinTransactionType(finTransactionType);

				BigDecimal bdAmount = new BigDecimal(journalEntry.getAmount());
				bdAmount.setScale(2, RoundingMode.HALF_UP);
				manualJournalTransaction.setTransactionAmount(bdAmount);
				manualJournalTransaction.setTransactionTimestamp(new Timestamp(
						System.currentTimeMillis()));

				manualJournalTransaction.setWcsOrderID(journalEntry
						.getWcsOrderId() != null ? journalEntry.getWcsOrderId()
						: null);

				// Persist the manual journal transaction
				//manualJournalTransaction = journalTransactionHome.persistFinJournalTransaction(manualJournalTransaction);
				if (!em.contains(manualJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postManualJournalTransaction:: manualJournalTransaction not attached, calling save.");
					manualJournalTransaction = finJournalTransactionDAO.save(manualJournalTransaction);
				}
				transactionList.add(manualJournalTransaction);

				// Create the FinApManualJournalTransaction record if a party or
				// order is provided
				if (journalEntry.getWcsOrderId() != null
						|| journalEntry.getVenPartyId() != null) {
					FinApManualJournalTransaction finApManualJournalTransaction = new FinApManualJournalTransaction();
					finApManualJournalTransaction
							.setFinJournalTransaction(manualJournalTransaction);
					VenOrder venOrder = null;
					if (journalEntry.getWcsOrderId() != null
							&& !journalEntry.getWcsOrderId().isEmpty()) {
						/*
						List<VenOrder> venOrderList = venOrderHome
								.queryByRange(
										"select o from VenOrder o where o.wcsOrderId = '"
												+ journalEntry.getWcsOrderId()
												+ "'", 0, 0);
						*/
						venOrder = venOrderDAO.findByWcsOrderId(
								journalEntry.getWcsOrderId());
						/*
						if (!venOrderList.isEmpty()) {
							venOrder = venOrderList.get(0);
						}
						*/
					}
					if (venOrder != null) {
						finApManualJournalTransaction.setVenOrder(venOrder);
					}

					VenParty venParty = null;

					if (journalEntry.getVenPartyId() != null) {
						/*
						List<VenParty> venPartyList = venPartyHome
								.queryByRange(
										"select o from VenParty o where o.partyId = "
												+ journalEntry.getVenPartyId(),
										0, 0);
						*/
						venParty = venPartyDAO.findByPartyId(journalEntry.getVenPartyId());
						/*
						if (!venPartyList.isEmpty()) {
							venParty = venPartyList.get(0);
						} else {
						*/
						if (venParty == null) {
							String errMsg = "The party id passed to create the manual journal transaction is not valid";
							CommonUtil.logError(this.getClass().getCanonicalName(), errMsg);
							throw new EJBException(errMsg);
						}
						finApManualJournalTransaction.setVenParty(venParty);
					}
					// Persist the manual journal transaction record linking the
					// party and order
					//manualJournalTransactionHome.persistFinApManualJournalTransaction(finApManualJournalTransaction);
					if (!em.contains(finApManualJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "postManualJournalTransaction:: finApManualJournalTransaction not attached, calling save.");
						finApManualJournalTransaction = finApManualJournalTransactionDAO.save(finApManualJournalTransaction);
					}
				}
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting manual journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postManualJournalTransaction::" + " completed in "
				+ duration + "ms");
		return true;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 *      #postPaymentRefundJournalTransaction(FinArFundsInRefund
	 *      finArFundsInRefund)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postPaymentRefundJournalTransaction(
			List<FinArFundsInRefund> finArFundsInRefundList) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {

			/*
			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");
			*/

			/*
			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");

			FinApPaymentSessionEJBLocal finApPaymentHome = (FinApPaymentSessionEJBLocal) this._genericLocator
					.lookupLocal(FinApPaymentSessionEJBLocal.class,
							"FinApPaymentSessionEJBBeanLocal");
			*/

			/*
			 * Lookup the payment journal approval group for the day If there is
			 * none then create it
			 */
			FinJournal finJournalPayment = new FinJournal();
			finJournalPayment.setJournalId(VeniceConstants.FIN_JOURNAL_PAYMENT);
			FinJournalApprovalGroup finJournalApprovalGroup = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			/*
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = journalApprovalGroupHome
					.queryByRange(
							"select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Payment Journal for:"
									+ sdf.format(new Date())
									+ "'"
									+ " and o.finApprovalStatus.approvalStatusId = "
									+ VeniceConstants.FIN_APPROVAL_STATUS_NEW,
							0, 0);
			*/
			List<FinJournalApprovalGroup> finJournalApprovalGroupList = finJournalApprovalGroupDAO.findByDescriptionAndApprovalStatusId(
					"Payment Journal for:" + sdf.format(new Date()), FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
			if (finJournalApprovalGroupList.isEmpty()) {
				finJournalApprovalGroup = new FinJournalApprovalGroup();
				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
				finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatus);

				finJournalApprovalGroup.setFinJournal(finJournalPayment);

				finJournalApprovalGroup
						.setJournalGroupDesc("Payment Journal for:"
								+ sdf.format(new Date()));
				finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// Persist the journal group
				//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
				if (!em.contains(finJournalApprovalGroup)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postPaymentRefundJournalTransaction:: finJournalApprovalGroup not attached, calling save.");
					finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
				}
			} else {
				finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
			}

			Timestamp time = new Timestamp(System.currentTimeMillis());
			for (FinArFundsInRefund finArFundsInRefund : finArFundsInRefundList) {
				BigDecimal amountBiaya = null;
				Long accountIdForBiaya = null;
				Long accountIdRefund = null;
				Long finTransactionTypeID = null;

				long accountNumberBank = 0;
				if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC.id()
						|| finArFundsInRefund.getFinArFundsInReconRecord()
								.getFinArFundsInReport()
								.getFinArFundsInReportType()
								.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120104.id();
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120102.id();
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB.id()
						|| finArFundsInRefund.getFinArFundsInReconRecord()
								.getFinArFundsInReport()
								.getFinArFundsInReportType()
								.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC.id()
						|| finArFundsInRefund.getFinArFundsInReconRecord()
								.getFinArFundsInReport()
								.getFinArFundsInReportType()
								.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120105.id();
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA.id()) {
					accountNumberBank = VeniceConstants.FIN_ACCOUNT_1120301;
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB.id()
						|| finArFundsInRefund.getFinArFundsInReconRecord()
								.getFinArFundsInReport()
								.getFinArFundsInReportType()
								.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120302.id();
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120402.id();
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1120888.id();
				} else if (finArFundsInRefund.getFinArFundsInReconRecord()
						.getFinArFundsInReport().getFinArFundsInReportType()
						.getPaymentReportTypeId() == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB.id()) {
					accountNumberBank = FinAccountConstants.FIN_ACCOUNT_1121001.id();
				} else {
					throw new EJBException(
							"Account number not available for the payment, please add account number to fin account and venice constants");
				}

				if (finArFundsInRefund.getRefundType().contains("Bank")) {
					accountIdRefund = FinAccountConstants.FIN_ACCOUNT_2170002.id();
					finTransactionTypeID = FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id();
					accountIdForBiaya = FinAccountConstants.FIN_ACCOUNT_5110003.id();
					amountBiaya = finArFundsInRefund
							.getFinArFundsInReconRecord()
							.getProviderReportFeeAmount() != null ? finArFundsInRefund
							.getFinArFundsInReconRecord()
							.getProviderReportFeeAmount() : new BigDecimal(0);
				} else {
					accountIdRefund = FinAccountConstants.FIN_ACCOUNT_2170001.id();
					finTransactionTypeID = FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_CUSTOMER.id();
					accountIdForBiaya = FinAccountConstants.FIN_ACCOUNT_7160003.id();
					amountBiaya = finArFundsInRefund.getFeeAmount();
				}
				/**
				 * Post the journal transaction for Refund Transaksi
				 */
			    CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::Save ( Refund Bank Transaksi) ::BEGIN");
				FinJournalTransaction refundBankJournalTransaction = new FinJournalTransaction();
				refundBankJournalTransaction = setJournalTransaction(
						finArFundsInRefund.getFinArFundsInReconRecord(),
						(finArFundsInRefund.getFinArFundsInReconRecord()
								.getWcsOrderId() != null ? finArFundsInRefund
								.getFinArFundsInReconRecord().getWcsOrderId()
								: ""),
						finJournalApprovalGroup,
						accountIdRefund,
						FinJournalConstants.FIN_JOURNAL_PAYMENT.id(),
						FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
						finTransactionTypeID,
						time,
						finArFundsInRefund.getFinArFundsInReconRecord()
								.getVenOrderPayment() != null ? finArFundsInRefund
								.getFinArFundsInReconRecord()
								.getVenOrderPayment().getVenBank()
								: finArFundsInRefund
										.getFinArFundsInReconRecord()
										.getFinArFundsInReport()
										.getFinArFundsInReportType().getVenBank(),
										//.getVenBank(), journalTransactionHome,
						accountNumberBank, finArFundsInRefund.getApAmount(),
						false, "");
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::Save ( Refund Bank Transaks)::END");
				transactionList.add(refundBankJournalTransaction);

				/**
				 * Post the journal transaction for Biaya Transaksi
				 */
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::Save ( Biaya Transaksi) ::BEGIN");
				FinJournalTransaction biayaJournalTransaction = new FinJournalTransaction();
				biayaJournalTransaction = setJournalTransaction(
						finArFundsInRefund.getFinArFundsInReconRecord(),
						(finArFundsInRefund.getFinArFundsInReconRecord()
								.getWcsOrderId() != null ? finArFundsInRefund
								.getFinArFundsInReconRecord().getWcsOrderId()
								: ""),
						finJournalApprovalGroup,
						accountIdForBiaya,
						FinJournalConstants.FIN_JOURNAL_PAYMENT.id(),
						FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
						finTransactionTypeID,
						time,
						finArFundsInRefund.getFinArFundsInReconRecord()
								.getVenOrderPayment() != null ? finArFundsInRefund
								.getFinArFundsInReconRecord()
								.getVenOrderPayment().getVenBank()
								: finArFundsInRefund
										.getFinArFundsInReconRecord()
										.getFinArFundsInReport()
										.getFinArFundsInReportType().getVenBank(),
										//.getVenBank(), journalTransactionHome,
						accountNumberBank, amountBiaya, false, "");
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::Save ( Biaya Transaks)::END");
				transactionList.add(biayaJournalTransaction);

				/**
				 * Post the journal transaction for Bank Transaksi
				 */
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::Save (  Bank Transaksi) ::BEGIN");
				FinJournalTransaction bankJournalTransaction = new FinJournalTransaction();
				bankJournalTransaction = setJournalTransaction(
						finArFundsInRefund.getFinArFundsInReconRecord(),
						(finArFundsInRefund.getFinArFundsInReconRecord()
								.getWcsOrderId() != null ? finArFundsInRefund
								.getFinArFundsInReconRecord().getWcsOrderId()
								: ""),
						finJournalApprovalGroup,
						accountNumberBank,
						FinJournalConstants.FIN_JOURNAL_PAYMENT.id(),
						FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id(),
						finTransactionTypeID,
						time,
						finArFundsInRefund.getFinArFundsInReconRecord()
								.getVenOrderPayment() != null ? finArFundsInRefund
								.getFinArFundsInReconRecord()
								.getVenOrderPayment().getVenBank()
								: finArFundsInRefund
										.getFinArFundsInReconRecord()
										.getFinArFundsInReport()
										.getFinArFundsInReportType().getVenBank(),
										//.getVenBank(), journalTransactionHome,
						accountNumberBank, amountBiaya.add(finArFundsInRefund
								.getApAmount()), true, "");
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::Save ( Bank Transaks)::END");
				transactionList.add(bankJournalTransaction);

			}

			FinApPayment finApPayment = finArFundsInRefundList.get(0)
					.getFinApPayment();
			finApPayment.setFinJournalTransactions(transactionList);
			// Set status to Done
			FinApprovalStatus approvalStatus = new FinApprovalStatus();
			approvalStatus.setApprovalStatusId(new Long(4));
			finApPayment.setFinApprovalStatus(approvalStatus);
			//finApPayment = finApPaymentHome.mergeFinApPayment(finApPayment);
			if (!em.contains(finApPayment)) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "postPaymentRefundJournalTransaction:: finApPayment not attached, calling save.");
				finApPayment = finApPaymentDAO.save(finApPayment);
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting Payment Refund Journal Transaction:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postPaymentRefundJournalTransaction::" + " completed in "
				+ duration + "ms");
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.finance.journal.FinanceJournalPosterSessionEJBRemote
	 * #postRefundJournalTransaction(Long reconciliationRecordId, Double
	 * refundAmount, Double fee, , int refundType)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean postCancelRefundJournalTransaction(Long reconciliationRecordId,
			Double cancelRefundAmount, Double cancelFee, int refundType,
			boolean printjournal) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCancelRefundJournalTransaction::BEGIN");
		Long startTime = System.currentTimeMillis();

		ArrayList<FinJournalTransaction> transactionList = new ArrayList<FinJournalTransaction>();

		try {
			/*
			FinArFundsInReconRecordSessionEJBLocal fundsInReconRecordHome = (FinArFundsInReconRecordSessionEJBLocal) this._genericLocator
					.lookupLocal(FinArFundsInReconRecordSessionEJBLocal.class,
							"FinArFundsInReconRecordSessionEJBBeanLocal");

			VenOrderPaymentAllocationSessionEJBLocal orderPaymentAllocationHome = (VenOrderPaymentAllocationSessionEJBLocal) this._genericLocator
					.lookupLocal(
							VenOrderPaymentAllocationSessionEJBLocal.class,
							"VenOrderPaymentAllocationSessionEJBBeanLocal");

			VenOrderPaymentSessionEJBLocal orderPaymentHome = (VenOrderPaymentSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderPaymentSessionEJBLocal.class,
							"VenOrderPaymentSessionEJBBeanLocal");

			VenOrderSessionEJBLocal orderHome = (VenOrderSessionEJBLocal) this._genericLocator
					.lookupLocal(VenOrderSessionEJBLocal.class,
							"VenOrderSessionEJBBeanLocal");

			FinJournalTransactionSessionEJBLocal journalTransactionHome = (FinJournalTransactionSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalTransactionSessionEJBLocal.class,
							"FinJournalTransactionSessionEJBBeanLocal");

			FinJournalApprovalGroupSessionEJBLocal journalApprovalGroupHome = (FinJournalApprovalGroupSessionEJBLocal) this._genericLocator
					.lookupLocal(FinJournalApprovalGroupSessionEJBLocal.class,
							"FinJournalApprovalGroupSessionEJBBeanLocal");

			FinArFundsInRefundSessionEJBLocal refundRecordHome = (FinArFundsInRefundSessionEJBLocal) this._genericLocator
					.lookupLocal(FinArFundsInRefundSessionEJBLocal.class,
							"FinArFundsInRefundSessionEJBBeanLocal");
			*/

			/*
			 * Read all the relevant funds in records from the database and
			 * determine how much has been paid as well as the remaining balance
			 * according to the reconciliation
			 *
			 */
			FinArFundsInReconRecord reconciliationRecord = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved(reconciliationRecordId);
			if (reconciliationRecord == null) {
				throw new EJBException("Reconciliation record not found:" + reconciliationRecordId);
			}

			VenOrderPayment venOrderPayment = null;

			/*
			 * For unexpected/unmatched payments there may not be a
			 * VenOrderPayment
			 */
			if (reconciliationRecord.getVenOrderPayment() != null) {
				venOrderPayment = venOrderPaymentDAO.findByOrderPaymentId(
						reconciliationRecord.getVenOrderPayment().getOrderPaymentId());
			}

			BigDecimal remainingUnallocatedAmount = null;
			remainingUnallocatedAmount = reconciliationRecord
					.getProviderReportPaidAmount();
			/*
			 * If there is a VenOrderPayment record then... Ascertain how much
			 * of the payment has been allocated to orders already and throw an
			 * exception if there are not enough funds to allocate
			 */

			if (venOrderPayment != null) {
				List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findByOrderPaymentId(
						venOrderPayment.getOrderPaymentId());
				BigDecimal allocatedAmount = new BigDecimal(0);

				/*
				 * Get the amount of funds that have been allocated from the
				 * payment amount. Check that the order has not been cancelled.
				 * If the order has been cancelled then the balance of funds can
				 * be made available for refund.
				 */
				remainingUnallocatedAmount = reconciliationRecord
						.getProviderReportPaidAmount()
						.subtract(allocatedAmount);
				if (orderPaymentAllocationList != null) {
					for (VenOrderPaymentAllocation allocation : orderPaymentAllocationList) {
						VenOrder allocatedOrder = venOrderDAO.findByOrderId(allocation.getId().getOrderId());												
						if (allocatedOrder != null && allocatedOrder.getVenOrderStatus()
								.getOrderStatusId() != VenOrderStatusConstants.VEN_ORDER_STATUS_X.code()) {
							allocatedAmount = allocatedAmount.add(allocation.getAllocationAmount());
						}
					}

					if (remainingUnallocatedAmount.compareTo(new BigDecimal(
							cancelRefundAmount - cancelFee)) < 0) {
						throw new EJBException(
								"Insufficient unallocated payment funds available to serve this request");
					}
				}
			}

			// FinAccount finAccountRefundPelanggan = new FinAccount(); //
			// better to use 'finAccountRefund' as var name instead of
			// 'finAccountRefundPelanggan' because it will represent those 2
			// accounts
			FinAccount finAccountRefund = new FinAccount();
			long accountRefund = 0;

			switch (refundType) {
			case FinRefundTypeConstants.Constants.BANK_ID:
				finAccountRefund.setAccountId(FinAccountConstants.FIN_ACCOUNT_2170002.id());
				accountRefund = FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK.id();
				break;
			case FinRefundTypeConstants.Constants.CUSTOMER_ID:
				finAccountRefund.setAccountId(FinAccountConstants.FIN_ACCOUNT_2170001.id());
				accountRefund = FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_CUSTOMER.id();
				break;
			default:
				break;
			// no action. should produce an error later because of the empty
			// account id
			}

			if (printjournal) {
				/*
				 * Lookup the refund journal approval group for the day If there
				 * is none then create it
				 */
				FinJournal finJournalRefund = new FinJournal();
				finJournalRefund
						.setJournalId(FinJournalConstants.FIN_JOURNAL_CANCEL_REFUND_OTHERS.id());
				FinJournalApprovalGroup finJournalApprovalGroup = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
				// List<FinJournalApprovalGroup> finJournalApprovalGroupList =
				// journalApprovalGroupHome.queryByRange(
				// "select o from FinJournalApprovalGroup o where o.journalGroupDesc = 'Refund Journal for:"
				// + sdf.format(new Date()) + "'" +
				// " and o.finApprovalStatus.approvalStatusId = " +
				// VeniceConstants.FIN_APPROVAL_STATUS_NEW, 0, 0);
				if (finJournalApprovalGroup == null) {
					finJournalApprovalGroup = new FinJournalApprovalGroup();
					FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
					finApprovalStatus
							.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED.id());
					finJournalApprovalGroup
							.setFinApprovalStatus(finApprovalStatus);
					finJournalApprovalGroup.setFinJournal(finJournalRefund);
					finJournalApprovalGroup
							.setJournalGroupDesc("Cancel Refund Journal for:"
									+ sdf.format(new Date()));
					finJournalApprovalGroup
							.setJournalGroupTimestamp(new Timestamp(System
									.currentTimeMillis()));

					// Persist the journal group
					//finJournalApprovalGroup = journalApprovalGroupHome.persistFinJournalApprovalGroup(finJournalApprovalGroup);
					if (!em.contains(finJournalApprovalGroup)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "postCancelRefundJournalTransaction:: finJournalApprovalGroup not attached, calling save.");
						finJournalApprovalGroup = finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
					}
				}
				// else {
				// finJournalApprovalGroup = finJournalApprovalGroupList.get(0);
				// }

				String wcsOrderId = reconciliationRecord.getWcsOrderId() != null ? reconciliationRecord
						.getWcsOrderId() : "";

				// Create the DEBIT journal transaction for REFUND against UANG
				// MUKA
				// PELANGGAN
				FinJournalTransaction cancelRefundJournalTransaction = new FinJournalTransaction();
				cancelRefundJournalTransaction.setComments("Cancel Refund Pelanggan DEBIT for Order:" + wcsOrderId + " Payment:"
								+ reconciliationRecord.getProviderReportPaymentId());
				cancelRefundJournalTransaction.setCreditDebitFlag(true);//
				cancelRefundJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				FinAccount finAccountUangMukaPelanggan = new FinAccount();
				if (reconciliationRecord.getFinArReconResult()
						.getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id()) {
					finAccountUangMukaPelanggan
							.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230002.id());
				} else {
					finAccountUangMukaPelanggan
							.setAccountId(FinAccountConstants.FIN_ACCOUNT_2230001.id());
				}
				cancelRefundJournalTransaction
						.setFinAccount(finAccountUangMukaPelanggan);

				List<FinArFundsInReconRecord> reconRecordList = new ArrayList<FinArFundsInReconRecord>();
				reconRecordList.add(reconciliationRecord);
				cancelRefundJournalTransaction.setFinArFundsInReconRecords(reconRecordList);

				cancelRefundJournalTransaction.setFinJournal(finJournalRefund);
				cancelRefundJournalTransaction.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());

				FinTransactionStatus finTransactionStatus = new FinTransactionStatus();
				finTransactionStatus.setTransactionStatusId(FinTransactionStatusConstants.FIN_TRANSACTION_STATUS_NEW.id());
				cancelRefundJournalTransaction.setFinTransactionStatus(finTransactionStatus);

				FinTransactionType finTransactionType = new FinTransactionType();
				if (reconciliationRecord.getFinArReconResult()
						.getReconResultId() == FinArReconResultConstants.FIN_AR_RECON_RESULT_NOT_RECOGNIZED.id()) {
					finTransactionType
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_BELUM_TERIDENTIFIKASI.id());
				} else {
					finTransactionType
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_UANG_JAMINAN_TRANSAKSI.id());
				}
				cancelRefundJournalTransaction
						.setFinTransactionType(finTransactionType);

				// refundJournalTransaction.setTransactionAmount(reconciliationRecord.getProviderReportPaidAmount());
				// // replace with the following line by yauri @Sept 18, 2013
				// 07:11AM
				cancelRefundJournalTransaction.setTransactionAmount(new BigDecimal(
						cancelRefundAmount + ""));
				cancelRefundJournalTransaction.setTransactionTimestamp(new Timestamp(
						System.currentTimeMillis()));

				// String
				// wcsOrderId=reconciliationRecord.getWcsOrderId()!=null?reconciliationRecord.getWcsOrderId():"";
				// // moved onto top by yauri @Sept 18, 2013 07:13AM
				VenBank venBank = reconciliationRecord.getVenOrderPayment() != null ? reconciliationRecord
						.getVenOrderPayment().getVenBank()
						: reconciliationRecord.getFinArFundsInReport()
								.getFinArFundsInReportType().getVenBank();

						cancelRefundJournalTransaction.setWcsOrderID(wcsOrderId);
						cancelRefundJournalTransaction.setVenBank(venBank);
				// Persist the DEBIT journal transaction for the refund
				//cancelRefundJournalTransaction = journalTransactionHome.persistFinJournalTransaction(cancelRefundJournalTransaction);
				if (!em.contains(cancelRefundJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postCancelRefundJournalTransaction:: cancelRefundJournalTransaction not attached, calling save.");
					cancelRefundJournalTransaction = finJournalTransactionDAO.save(cancelRefundJournalTransaction);
				}
				transactionList.add(cancelRefundJournalTransaction);

				// Create the CREDIT journal transaction for REFUND against
				// REFUND-------------------------------
				// PELANGGAN
				CommonUtil.logInfo(this.getClass().getCanonicalName(), "postCancelRefundJournalTransaction:: Creating CREDIT Journal Transaction for CANCEL REFUND CUSTOMER");
				FinJournalTransaction cancelRefundPelangganJournalTransaction = new FinJournalTransaction();
				cancelRefundPelangganJournalTransaction
						.setComments("Cancel Refund Pelanggan CREDIT for Order:"
								+ wcsOrderId // reconciliationRecord.getWcsOrderId()
								+ " Payment:"
								+ reconciliationRecord
										.getProviderReportPaymentId());
				cancelRefundPelangganJournalTransaction.setCreditDebitFlag(false);//
				cancelRefundPelangganJournalTransaction
						.setFinJournalApprovalGroup(finJournalApprovalGroup);

				// finAccountRefundPelanggan.setAccountId(new
				// Long(refundType+""));
				// refundPelangganJournalTransaction.setFinAccount(finAccountRefundPelanggan);
				cancelRefundPelangganJournalTransaction
						.setFinAccount(finAccountRefund);

				cancelRefundPelangganJournalTransaction
						.setFinArFundsInReconRecords(reconRecordList);
				cancelRefundPelangganJournalTransaction
						.setFinJournal(finJournalRefund);
				cancelRefundPelangganJournalTransaction
						.setFinPeriod(FinancePeriodUtil.getCurrentPeriod());
				cancelRefundPelangganJournalTransaction
						.setFinTransactionStatus(finTransactionStatus);
				cancelRefundPelangganJournalTransaction
						.setFinTransactionType(finTransactionType);
				cancelRefundPelangganJournalTransaction
						.setTransactionAmount(new BigDecimal(cancelRefundAmount + ""));
				cancelRefundPelangganJournalTransaction
						.setTransactionTimestamp(new Timestamp(System
								.currentTimeMillis()));

				cancelRefundPelangganJournalTransaction.setWcsOrderID(wcsOrderId);
				cancelRefundPelangganJournalTransaction.setVenBank(venBank);
				// Persist the CREDIT journal transaction for the refund
				//cancelRefundPelangganJournalTransaction = journalTransactionHome.persistFinJournalTransaction(cancelRefundPelangganJournalTransaction);
				if (!em.contains(cancelRefundPelangganJournalTransaction)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postCancelRefundJournalTransaction:: cancelRefundPelangganJournalTransaction not attached, calling save.");
					cancelRefundPelangganJournalTransaction = finJournalTransactionDAO.save(cancelRefundPelangganJournalTransaction);
				}
				transactionList.add(cancelRefundPelangganJournalTransaction);

				if (cancelFee > 0) {
					// Create the CREDIT journal transaction for fees
					FinJournalTransaction processFeeJournalTransaction = new FinJournalTransaction();
					processFeeJournalTransaction
							.setComments("Processing Fee (Cancel Refund) CREDIT for Order:"
									+ wcsOrderId // reconciliationRecord.getWcsOrderId()!=null?reconciliationRecord.getWcsOrderId():""
									+ " Payment:"
									+ reconciliationRecord
											.getProviderReportPaymentId());
					cancelRefundJournalTransaction.setCreditDebitFlag(false);//
					processFeeJournalTransaction
							.setFinJournalApprovalGroup(finJournalApprovalGroup);

					FinAccount finAccountProcessingFee = new FinAccount();

					/*
					 * The fees are either taken from the bank or the customer
					 * as a processing fee
					 */
					if (refundType == FinRefundTypeConstants.FIN_REFUND_TYPE_BANK.id()) {
						finAccountProcessingFee
								.setAccountId(FinAccountConstants.FIN_ACCOUNT_5110003.id());
					} else if (refundType == FinRefundTypeConstants.FIN_REFUND_TYPE_CUSTOMER.id()) {
						finAccountProcessingFee
								.setAccountId(FinAccountConstants.FIN_ACCOUNT_4110007.id());
					}
					processFeeJournalTransaction
							.setFinAccount(finAccountProcessingFee);

					processFeeJournalTransaction
							.setFinArFundsInReconRecords(reconRecordList);

					processFeeJournalTransaction
							.setFinJournal(finJournalRefund);
					processFeeJournalTransaction.setFinPeriod(FinancePeriodUtil
							.getCurrentPeriod());

					processFeeJournalTransaction
							.setFinTransactionStatus(finTransactionStatus);

					FinTransactionType finTransactionTypeProcessFee = new FinTransactionType();
					finTransactionTypeProcessFee
							.setTransactionTypeId(FinTransactionTypeConstants.FIN_TRANSACTION_TYPE_PROCESS_FEE.id());
					processFeeJournalTransaction
							.setFinTransactionType(finTransactionTypeProcessFee);

					processFeeJournalTransaction
							.setTransactionAmount(new BigDecimal(cancelFee));
					processFeeJournalTransaction.setCreditDebitFlag(true);//
					processFeeJournalTransaction
							.setTransactionTimestamp(new Timestamp(System
									.currentTimeMillis()));

					processFeeJournalTransaction.setWcsOrderID(wcsOrderId);
					processFeeJournalTransaction.setVenBank(venBank);
					// Persist the DEBIT journal transaction for the refund
					//processFeeJournalTransaction = journalTransactionHome.persistFinJournalTransaction(processFeeJournalTransaction);
					if (!em.contains(processFeeJournalTransaction)) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "postCancelRefundJournalTransaction:: processFeeJournalTransaction not attached, calling save.");
						processFeeJournalTransaction = finJournalTransactionDAO.save(processFeeJournalTransaction);
					}
					transactionList.add(processFeeJournalTransaction);
				}

				// Create a refund record
				FinArFundsInRefund finArFundsInRefund = new FinArFundsInRefund();
				finArFundsInRefund
						.setFinArFundsInReconRecord(reconciliationRecord);
				finArFundsInRefund.setApAmount(new BigDecimal(cancelRefundAmount));
				finArFundsInRefund.setRefundTimestamp(new Timestamp(System
						.currentTimeMillis()));
				finArFundsInRefund.setRefundType(reconciliationRecord
						.getFinArFundsInActionApplied().getActionAppliedDesc());
				//refundRecordHome.persistFinArFundsInRefund(finArFundsInRefund);
				if (!em.contains(finArFundsInRefund)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "postCancelRefundJournalTransaction:: finArFundsInRefund not attached, calling save.");
					finArFundsInRefund = finArFundsInRefundDAO.save(finArFundsInRefund);
				}
			} else {
				reconciliationRecord.setRefundAmount(reconciliationRecord
						.getRefundAmount().add(new BigDecimal(cancelRefundAmount)));
				/*
				 * 1) Flag the recon record as being refunded 2) Adjust the
				 * balance to equal the remianing unallocated amount 3) Merge
				 * the funds in record
				 */
				FinArFundsInActionApplied finArFundsInActionApplied = new FinArFundsInActionApplied();
				finArFundsInActionApplied.setActionAppliedId(accountRefund);
				reconciliationRecord
						.setFinArFundsInActionApplied(finArFundsInActionApplied);

				// FinArReconResult finArReconResult = new FinArReconResult();
				// finArReconResult.setReconResultId(VeniceConstants.FIN_AR_RECON_RESULT_REFUNDED);
				// reconciliationRecord.setFinArReconResult(finArReconResult);

				FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
				finApprovalStatus
						.setApprovalStatusId(FinApprovalStatusConstants.FIN_APPROVAL_STATUS_NEW.id());
				reconciliationRecord.setFinApprovalStatus(finApprovalStatus);

				// reconciliationRecord.setRemainingBalanceAmount(remainingUnallocatedAmount.subtract(new
				// BigDecimal(refundAmount)));
				// reconciliationRecord.setRemainingBalanceAmount(new
				// BigDecimal(refundAmount).subtract(reconciliationRecord.getProviderReportPaidAmount()).add(reconciliationRecord.getPaymentAmount()!=null?reconciliationRecord.getPaymentAmount():new
				// BigDecimal("0")));
				//reconciliationRecord = fundsInReconRecordHome.mergeFinArFundsInReconRecord(reconciliationRecord);
				if (!em.contains(reconciliationRecord)) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"postCancelRefundJournalTransaction:: reconciliationRecord not attached, calling save.");
					reconciliationRecord = finArFundsInReconRecordDAO.save(reconciliationRecord);
				}

				/*
				 * Note that we do not do anything with the transactionList here
				 */
			}

		} catch (Exception e) {
			String errMsg = "An Exception occured when posting cancel refund journal transactions:";
			CommonUtil.logError(this.getClass().getCanonicalName(), errMsg + e.getMessage());
			e.printStackTrace();
			throw new EJBException(errMsg + e.getMessage());
		} finally {
			/*
			try {
				if (_genericLocator != null) {
					_genericLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
		}

		Long endTime = System.currentTimeMillis();
		Long duration = endTime - startTime;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "postCancelRefundJournalTransaction::" + " completed in "
				+ duration + "ms");
		return true;

	}	
}