package com.gdn.venice.finance.services.impl;

import org.springframework.stereotype.Service;

import com.gdn.venice.constants.FinAccountConstants;
import com.gdn.venice.constants.FinArFundsInReportTypeConstants;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.exception.AccountNumberNotAvailableException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.finance.services.FinAccountService;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
public class FinAccountServiceImpl implements FinAccountService {

	@Override
	public long getAccountNumberBank(long paymentReportTypeId) throws VeniceInternalException {
		if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_CC.id()
				|| paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_IB.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120104.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BCA_VA.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120102.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_IB.id()
				|| paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAY_CC.id()
				|| paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_KLIKPAYINST_CC.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120105.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_VA.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120301.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRI_IB.id()
				|| paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_MANDIRIINSTALLMENT_CC.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120302.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_NIAGA_IB.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120402.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_XL_IB.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1120888.id();
		} else if (paymentReportTypeId == FinArFundsInReportTypeConstants.FIN_AR_FUNDS_IN_REPORT_TYPE_BRI_IB.id()) {
			return FinAccountConstants.FIN_ACCOUNT_1121001.id();
		} else {
			throw CommonUtil.logAndReturnException(new AccountNumberNotAvailableException(
					"Account number not available for the payment, please add account number to fin_account and FinAccountConstants", 
					VeniceExceptionConstants.VEN_EX_400005), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
	}

}
