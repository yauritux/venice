package com.gdn.venice.facade.spring.fraud.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule10DAO;
import com.gdn.venice.dao.FrdRuleConfigTresholdDAO;
import com.gdn.venice.dao.VenBinCreditLimitEstimateDAO;
import com.gdn.venice.dao.VenOrderPaymentDAO;
import com.gdn.venice.persistence.FrdParameterRule10;
import com.gdn.venice.persistence.FrdRuleConfigTreshold;
import com.gdn.venice.persistence.VenBinCreditLimitEstimate;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Multiple transactions on one card over a very short period of time
 *
 */
@Service("Rule10")
public class Rule10Impl implements Rule{
	private static final String CLASS_NAME = Rule10Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	
	@Autowired
	VenBinCreditLimitEstimateDAO venBinCreditLimitEstimateDAO;
	@Autowired
	FrdRuleConfigTresholdDAO frdRuleConfigTresholdDAO;
	@Autowired
	FrdParameterRule10DAO frdParameterRule10DAO;
	@Autowired
	VenOrderPaymentDAO venOrderPaymentDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		BigDecimal paymentSum = getPaymentSum(order);
		
		if(paymentSum.compareTo(new BigDecimal(0)) == 1){
			totalRiskPoint = 0;
		}else{
			totalRiskPoint = getRiskPoint(order, paymentSum);
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPoint(VenOrder order, BigDecimal paymentSum){
		BigDecimal creditLimitEstimation = getCreditLimitEstimation(getMaskedCreditCard(order));
		int amountUsagePersentage = getAmountUsagePercentage(paymentSum, creditLimitEstimation);
		FrdParameterRule10 rule = frdParameterRule10DAO.findByPercentageUsage(amountUsagePersentage);
		
		return rule.getRiskPoint();
	}

	public BigDecimal getPaymentSum(VenOrder order) {
		String maskedCreditCardNumber = getMaskedCreditCard(order);
		Date endRangeDate = getOrderPaymentTimestamp(order);
		Date startRangeDate = getStartRangeDate(getOrderPaymentTimestamp(order));
		
		return venOrderPaymentDAO.getOrderPaymentAmountSumByCreditCardNumberPaymentTimeRange(maskedCreditCardNumber, startRangeDate, endRangeDate);
	}
	
	public BigDecimal getCreditLimitEstimation(String maskedCreditCardNumber){
		String binNumber = getBinNumber(maskedCreditCardNumber);
		VenBinCreditLimitEstimate binCreditLimitEstimate = venBinCreditLimitEstimateDAO.findByActiveAndBinNumber(binNumber);
		
		if(binCreditLimitEstimate != null){
			return binCreditLimitEstimate.getCreditLimitEstimate();
		}else{
			return new BigDecimal(0);
		}
	}
	
	public String getBinNumber(String maskedCreditCardNumber){
		return maskedCreditCardNumber.substring(0, 6);
	}
	
	public int getDaySpanFromDBConfig(){
		FrdRuleConfigTreshold config = frdRuleConfigTresholdDAO.findByKeyDaySpanForFraudParameter10();
		return new Integer(config.getValue());
	}
	
	public int getAmountUsagePercentage(BigDecimal amount, BigDecimal creditLimit){
		CommonUtil.logInfo(CLASS_NAME, "Amount : " + amount + ", Credit Limit : " + creditLimit);
		BigDecimal usage = (amount.divide(creditLimit, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
		
		return usage.intValue();
	}
	
	public String getMaskedCreditCard(VenOrder order){
		return order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getMaskedCreditCardNumber();
	}
	
	public Date getStartRangeDate(Timestamp timestamp){
		int daySpan = getDaySpanFromDBConfig();
		Date startRangeDate = DateUtils.addDays(timestamp, -daySpan);
		
		return startRangeDate;
	}
	
	public Timestamp getOrderPaymentTimestamp(VenOrder order){
		return order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getPaymentTimestamp();
	}
	
}