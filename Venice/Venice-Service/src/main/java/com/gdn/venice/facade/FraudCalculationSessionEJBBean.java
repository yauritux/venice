package com.gdn.venice.facade;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.constants.FraudRuleConstants;
import com.gdn.venice.constants.VenContactDetailTypeConstants;
import com.gdn.venice.constants.VenOrderStatusCodeConstants;
import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.facade.spring.fraud.rule.Rule;
import com.gdn.venice.persistence.FrdBlacklistReason;
import com.gdn.venice.persistence.FrdCustomerWhitelistBlacklist;
import com.gdn.venice.persistence.FrdEntityBlacklist;
import com.gdn.venice.persistence.FrdFraudCaseHistory;
import com.gdn.venice.persistence.FrdFraudCaseHistoryPK;
import com.gdn.venice.persistence.FrdFraudCaseStatus;
import com.gdn.venice.persistence.FrdFraudSuspicionCase;
import com.gdn.venice.persistence.FrdFraudSuspicionPoint;
import com.gdn.venice.persistence.FrdRuleConfigTreshold;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderItemContactDetail;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Interceptors(SpringBeanAutowiringInterceptor.class)
@Stateless(mappedName = "FraudCalculationSessionEJBBean")
public class FraudCalculationSessionEJBBean implements FraudCalculationSessionEJBRemote, FraudCalculationSessionEJBLocal {
	private static final String CLASS_NAME = FraudCalculationSessionEJBBean.class.getCanonicalName();
	
	private String duplicateOrderIdReport;
	
	@Autowired
	@Qualifier("Rule1")
	Rule rule1;
	
	@Autowired
	@Qualifier("Rule2")
	Rule rule2;
	
	@Autowired
	@Qualifier("Rule3")
	Rule rule3;
	
	@Autowired
	@Qualifier("Rule4")
	Rule rule4;
	
	@Autowired
	@Qualifier("Rule5")
	Rule rule5;
	
	@Autowired
	@Qualifier("Rule6")
	Rule rule6;
	
	@Autowired
	@Qualifier("Rule7")
	Rule rule7;
	
	@Autowired
	@Qualifier("Rule8")
	Rule rule8;
	
	@Autowired
	@Qualifier("Rule9")
	Rule rule9;
	
	@Autowired
	@Qualifier("Rule10")
	Rule rule10;
	
	@Autowired
	@Qualifier("Rule11")
	Rule rule11;
	
	@Autowired
	@Qualifier("Rule12")
	Rule rule12;
	
	@Autowired
	@Qualifier("Rule13")
	Rule rule13;
	
	@Autowired
	@Qualifier("Rule14")
	Rule rule14;
	
	@Autowired
	@Qualifier("Rule15")
	Rule rule15;
	
	@Autowired
	@Qualifier("Rule16")
	Rule rule16;
	
	@Autowired
	@Qualifier("Rule17")
	Rule rule17;
	
	@Autowired
	@Qualifier("Rule18")
	Rule rule18;
	
	@Autowired
	@Qualifier("Rule19")
	Rule rule19;
	
	@Autowired
	@Qualifier("Rule20")
	Rule rule20;
	
	@Autowired
	@Qualifier("Rule21")
	Rule rule21;
	
	@Autowired
	@Qualifier("Rule22")
	Rule rule22;
	
	@Autowired
	@Qualifier("Rule23")
	Rule rule23;
	
	@Autowired
	@Qualifier("Rule24")
	Rule rule24;
	
	@Autowired
	@Qualifier("Rule25")
	Rule rule25;
	
	@Autowired
	@Qualifier("Rule26")
	Rule rule26;
	
	@Autowired
	@Qualifier("Rule27")
	Rule rule27;
	
	@Autowired
	@Qualifier("Rule28")
	Rule rule28;
	
	@Autowired
	@Qualifier("Rule29")
	Rule rule29;
	
	@Autowired
	@Qualifier("Rule30")
	Rule rule30;
	
	@Autowired
	@Qualifier("Rule31")
	Rule rule31;
	
	@Autowired
	@Qualifier("Rule32")
	Rule rule32;
	
	@Autowired
	@Qualifier("Rule33")
	Rule rule33;
	
	@Autowired
	@Qualifier("Rule34")
	Rule rule34;
	
	@Autowired
	@Qualifier("Rule35")
	Rule rule35;
	
	@Autowired
	@Qualifier("Rule37")
	Rule rule37;
	
	@Autowired
	@Qualifier("Rule38")
	Rule rule38;
	
	@Autowired
	@Qualifier("Rule39")
	Rule rule39;
	
	@Autowired
	@Qualifier("Rule40")
	Rule rule40;
	
	/*@Autowired
	@Qualifier("Rule41")
	Rule rule41;*/
	
	@Autowired
	@Qualifier("Rule42")
	Rule rule42;
	
	@Autowired
	@Qualifier("Rule43")
	Rule rule43;
	
	@Autowired
	@Qualifier("Rule44")
	Rule rule44;
	
	@Autowired
	@Qualifier("Rule45")
	Rule rule45;
	
	@Autowired
	@Qualifier("Rule46")
	Rule rule46;
	
	@Autowired
	@Qualifier("Rule47")
	Rule rule47;
	
	@Autowired
	@Qualifier("Rule48")
	Rule rule48;
	
	public boolean calculateFraudRules(VenOrder venOrder){
		CommonUtil.logInfo(CLASS_NAME, "Check whitelist and blacklist");
		
		Boolean isFP=false;
		Boolean isFC=false;
		String descResultCal="Calculated by System";
				
		if(isIPAddressWhitelistBlacklist(venOrder.getIpAddress(), "blacklist")){
			isFC=true;
			descResultCal=descResultCal + "\nIP BlackList";
		}
		
		if(isIPAddressWhitelistBlacklist(venOrder.getIpAddress(), "whitelist")){
			isFP=true;
			descResultCal=descResultCal + "\nIP WhiteList";
		}
		
		ArrayList<String>reasonList =isCustomerWhitelistBlacklist(venOrder);
		if(reasonList.size()>0){
			isFC=true;
			for(String itemReason : reasonList){
				if(!descResultCal.contains(itemReason)){
					descResultCal=descResultCal + "\n" +itemReason;
				}				
			}
		}		
				
		CommonUtil.logInfo(CLASS_NAME, "Calculate fraud rules");
		
		boolean isSuccessCalculate=true;
		int totalFraudPoints=0;
		int pointRule1=0, pointRule2=0, pointRule3=0, pointRule4=0, pointRule5=0, pointRule6=0, pointRule7=0, pointRule8=0, pointRule9=0, pointRule10=0, pointRule11=0, pointRule12=0, 
		pointRule13=0, pointRule14=0, pointRule15=0, pointRule16=0, pointRule17=0, pointRule18=0, pointRule19=0, pointRule20=0, pointRule21=0, pointRule22=0, pointRule23=0, 
		pointRule24=0, pointRule25=0, pointRule26=0, pointRule27=0, pointRule28=0, pointRule29=0, pointRule30=0, pointRule31=0, pointRule32=0, pointRule33=0, pointRule34=0,
		pointRule35=0, pointRule36=0, pointRule37=0, pointRule38=0, pointRule39=0, pointRule40=0, pointRule41=0, pointRule42=0, pointRule43=0, pointRule44=0, pointRule45=0,
		pointRule46=0, pointRule47=0, pointRule48=0;
		
		Locator<Object> locator = null;
		try{
			
			pointRule1=rule1.getRiskPoint(venOrder);
			
			pointRule2=rule2.getRiskPoint(venOrder);	
			
			pointRule3=rule3.getRiskPoint(venOrder);	
			
			pointRule4=rule4.getRiskPoint(venOrder);	
			
			pointRule5=rule5.getRiskPoint(venOrder);
			
			pointRule6=rule6.getRiskPoint(venOrder);
			
			pointRule7=rule7.getRiskPoint(venOrder);
			
			pointRule8=rule8.getRiskPoint(venOrder);
			
			pointRule9=rule9.getRiskPoint(venOrder);
			
			pointRule10=rule10.getRiskPoint(venOrder);
			
			pointRule11=rule11.getRiskPoint(venOrder);
			
			pointRule12=rule12.getRiskPoint(venOrder);
			
			pointRule13=rule13.getRiskPoint(venOrder);
			
			pointRule14=rule14.getRiskPoint(venOrder);
			
			pointRule15=rule15.getRiskPoint(venOrder);			
			
			pointRule16=rule16.getRiskPoint(venOrder);			
			
			pointRule17=rule17.getRiskPoint(venOrder);		
			
			pointRule18=rule18.getRiskPoint(venOrder);	
			
			pointRule19=rule19.getRiskPoint(venOrder);	
			
			pointRule20=rule20.getRiskPoint(venOrder);	
			
			pointRule21=rule21.getRiskPoint(venOrder);	
			
			pointRule22=rule22.getRiskPoint(venOrder);	
			
			pointRule23=rule23.getRiskPoint(venOrder);			
			
			pointRule24=rule24.getRiskPoint(venOrder);
			
			pointRule25=rule25.getRiskPoint(venOrder);
			pointRule25=pointRule25>0?(pointRule6+pointRule9+pointRule11+pointRule17+pointRule18+pointRule19)*-1:0;
			
			pointRule26=rule26.getRiskPoint(venOrder);
			
			pointRule27=rule27.getRiskPoint(venOrder);
			
			pointRule28=rule28.getRiskPoint(venOrder);
			
			pointRule29=rule29.getRiskPoint(venOrder);
			
			pointRule30=rule30.getRiskPoint(venOrder);
			
			pointRule31=rule31.getRiskPoint(venOrder);
			pointRule31=pointRule31>0?(pointRule7+pointRule8+pointRule10+pointRule16)*-1:0;
			
			pointRule32=rule32.getRiskPoint(venOrder);		
			
			if(isFC) pointRule36=500;
			
			pointRule33=rule33.getRiskPoint(venOrder);
			if(pointRule33<0) { 
				pointRule33=pointRule33-pointRule7-pointRule8-pointRule10-pointRule16; 
				/*
				 * jika cc tidak terdaftar di bin (yaitu pointRule15>0) dan eci 5
				 * maka status order di SF dengan cara menginisialisasi FP dan FC adalah true
				 */
				if(pointRule15>0) isFC=true;						
				isFP=true;
				descResultCal=descResultCal + " : E-Commerce Indicator 5";
			}else if(pointRule33>0){ 
				isFC=true;	
				descResultCal=descResultCal + " : E-Commerce Indicator 7";	
			}
			
			pointRule34=rule34.getRiskPoint(venOrder);
			
			pointRule35=rule35.getRiskPoint(venOrder);
			if(pointRule35==50){
				locator = new Locator<Object>();
				FrdBlacklistReasonSessionEJBRemote blacklistReasonSessionHome = (FrdBlacklistReasonSessionEJBRemote) locator.lookup(FrdBlacklistReasonSessionEJBRemote.class, "FrdBlacklistReasonSessionEJBBean");
				List<FrdBlacklistReason>greyListReasonList = blacklistReasonSessionHome.queryByRange("select o from FrdBlacklistReason o where o.orderId="+venOrder.getOrderId()+" and o.blacklistReason like '%grey list%'", 0, 0);
					
				if(greyListReasonList.size()>0){
					ArrayList<String> greyListReason=new ArrayList<String>();
					for(int i=0;i<greyListReasonList.size();i++){
						greyListReason.add(greyListReasonList.get(i).getBlacklistReason());
					}
					
					for(String reason : greyListReason){
						if(!descResultCal.contains(reason)){
							descResultCal=descResultCal + "\n" +reason;
						}				
					}
				}
			}
			
			pointRule37=rule37.getRiskPoint(venOrder);		
			
			pointRule38=rule38.getRiskPoint(venOrder);		
			
			pointRule39=rule39.getRiskPoint(venOrder);
			
			pointRule40=rule40.getRiskPoint(venOrder);
			
			//attempt rule
		//	 pointRule41=rule41.getRiskPoint(venOrder);
			 
			 //status approve >1
			 pointRule42=rule42.getRiskPoint(venOrder);
			 
			 //total payment >1
			 pointRule43=rule43.getRiskPoint(venOrder);
			 
			 //slow moving category product
			 pointRule44=rule44.getRiskPoint(venOrder);

			 pointRule45=rule45.getRiskPoint(venOrder);

			 pointRule46=rule46.getRiskPoint(venOrder);

			 pointRule47=rule47.getRiskPoint(venOrder);

			 pointRule48=rule48.getRiskPoint(venOrder);
			
			totalFraudPoints = pointRule1+pointRule2+pointRule3+pointRule4+pointRule5+pointRule6+pointRule7+pointRule8+pointRule9+pointRule10+pointRule11+pointRule12+pointRule13+
			pointRule14+pointRule15+pointRule16+pointRule17+pointRule18+pointRule19+pointRule20+pointRule21+pointRule22+pointRule23+pointRule24+pointRule25+pointRule26+pointRule27+
			pointRule28+pointRule29+pointRule30+pointRule31+pointRule32+pointRule33+pointRule34+pointRule35+pointRule36+pointRule37+pointRule38+pointRule39+pointRule40+pointRule41+
			pointRule42+pointRule43+pointRule44+pointRule45+pointRule46+pointRule47+pointRule48;
			CommonUtil.logInfo(CLASS_NAME, "Done calculate fraud rules, total fraud points is: "+totalFraudPoints);
		}catch(Exception e){
			CommonUtil.logError(CLASS_NAME,"Fraud calculate failed for wcs order id: "+venOrder.getWcsOrderId());
			isSuccessCalculate=false;			
			e.printStackTrace();
		}finally{
			try {
				if(locator!=null){
					locator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//insert data to fraud suspicion case
		if(isSuccessCalculate){
			CommonUtil.logInfo(CLASS_NAME, "insert fraud suspicion case record");
			List<FrdFraudSuspicionCase> fraudCaseList =null;
			
			try {
				locator = new Locator<Object>();
				FrdFraudSuspicionCaseSessionEJBRemote fraudCaseSessionHome = (FrdFraudSuspicionCaseSessionEJBRemote) locator.lookup(FrdFraudSuspicionCaseSessionEJBRemote.class, "FrdFraudSuspicionCaseSessionEJBBean");
				FrdFraudSuspicionPointSessionEJBRemote fraudPointSessionHome = (FrdFraudSuspicionPointSessionEJBRemote) locator.lookup(FrdFraudSuspicionPointSessionEJBRemote.class, "FrdFraudSuspicionPointSessionEJBBean");
				FrdFraudCaseHistorySessionEJBRemote fraudCaseHistorySessionHome = (FrdFraudCaseHistorySessionEJBRemote) locator.lookup(FrdFraudCaseHistorySessionEJBRemote.class, "FrdFraudCaseHistorySessionEJBBean");
				FrdRuleConfigTresholdSessionEJBRemote configSessionHome = (FrdRuleConfigTresholdSessionEJBRemote) locator.lookup(FrdRuleConfigTresholdSessionEJBRemote.class, "FrdRuleConfigTresholdSessionEJBBean");
				
				String queryMinimalPoint = "select o from FrdRuleConfigTreshold o where o.key = 'FRAUD_PASS_MIN_RISK_POINT'";
				List<FrdRuleConfigTreshold> configList = configSessionHome.queryByRange(queryMinimalPoint, 0, 1);
				int minimalPoint=0;
				if(configList.size()>0){
					minimalPoint = new Integer (configList.get(0).getValue());
				}
				
				String fraudStatus="";
				FrdFraudCaseStatus frdFraudCaseStatus= new FrdFraudCaseStatus();
				if(isFP==true && isFC==false){
					fraudStatus=VenOrderStatusCodeConstants.VEN_ORDER_STATUS_FP.code();
					frdFraudCaseStatus.setFraudCaseStatusId(VenOrderStatusConstants.VEN_ORDER_STATUS_FP.code());
				}else if(isFP==false && isFC==true){
					fraudStatus=VenOrderStatusCodeConstants.VEN_ORDER_STATUS_FC.code();
					frdFraudCaseStatus.setFraudCaseStatusId(VenOrderStatusConstants.VEN_ORDER_STATUS_FC.code());
				}else if(isFP==true && isFC==true){
					fraudStatus=VenOrderStatusCodeConstants.VEN_ORDER_STATUS_SF.code();
					frdFraudCaseStatus.setFraudCaseStatusId(VenOrderStatusConstants.VEN_ORDER_STATUS_SF.code());
				}else if(isFP==false && isFC==false){
					if(totalFraudPoints>minimalPoint){
						CommonUtil.logInfo(CLASS_NAME, "totalFraudPoints > minimalPoint, set status to SF");
						fraudStatus=VenOrderStatusCodeConstants.VEN_ORDER_STATUS_SF.code();
						frdFraudCaseStatus.setFraudCaseStatusId(VenOrderStatusConstants.VEN_ORDER_STATUS_SF.code());
					}else if(totalFraudPoints<=minimalPoint){
						CommonUtil.logInfo(CLASS_NAME, "totalFraudPoints <= minimalPoint, set status to FP");
						fraudStatus=VenOrderStatusCodeConstants.VEN_ORDER_STATUS_FP.code();
						frdFraudCaseStatus.setFraudCaseStatusId(VenOrderStatusConstants.VEN_ORDER_STATUS_FP.code());
					}
				}
				
				CommonUtil.logInfo(CLASS_NAME, "Fraud status is: "+fraudStatus);	
				
				FrdFraudSuspicionCase  fraudCase = new FrdFraudSuspicionCase();
				fraudCase.setVenOrder(venOrder);
				fraudCase.setFraudCaseDateTime(new Timestamp(System.currentTimeMillis()));
				fraudCase.setFraudTotalPoints(totalFraudPoints);
				fraudCase.setIlogFraudStatus(fraudStatus);				
				fraudCase.setFrdFraudCaseStatus(frdFraudCaseStatus);
				fraudCase.setFraudCaseDesc(descResultCal);
				fraudCase.setSuspicionReason("Calculated by System");
				fraudCase.setFraudSuspicionNotes("Calculated by System");
				
				String selectFraudCase = "select o from FrdFraudSuspicionCase o where o.venOrder.orderId = " + venOrder.getOrderId();
				fraudCaseList = fraudCaseSessionHome.queryByRange(selectFraudCase, 0, 1);
				
				if(fraudCaseList==null || fraudCaseList.size()==0){	
					try{
						fraudCase = fraudCaseSessionHome.persistFrdFraudSuspicionCase(fraudCase);
					}catch(Exception e){
						isSuccessCalculate=false;
						CommonUtil.logError(CLASS_NAME, "Fraud calculate failed when persisting fraud points or order id is already exist for wcs order id: "+venOrder.getWcsOrderId());
						CommonUtil.logError(CLASS_NAME, e);
						e.printStackTrace();
						duplicateOrderIdReport="(calculating another process)";
					}
						//insert data to fraud suspicion points
						CommonUtil.logInfo(CLASS_NAME, "insert fraud suspicion point");		
						
						FrdFraudSuspicionPoint fraudPoint = new FrdFraudSuspicionPoint();
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_1.title());
						fraudPoint.setRiskPoints(pointRule1);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);				
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_2.title());
						fraudPoint.setRiskPoints(pointRule2);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_3.title());
						fraudPoint.setRiskPoints(pointRule3);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_4.title());
						fraudPoint.setRiskPoints(pointRule4);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_5.title());
						fraudPoint.setRiskPoints(pointRule5);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_6.title());
						fraudPoint.setRiskPoints(pointRule6);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_7.title());
						fraudPoint.setRiskPoints(pointRule7);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_8.title());
						fraudPoint.setRiskPoints(pointRule8);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_9.title());
						fraudPoint.setRiskPoints(pointRule9);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_10.title());
						fraudPoint.setRiskPoints(pointRule10);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_11.title());
						fraudPoint.setRiskPoints(pointRule11);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_12.title());
						fraudPoint.setRiskPoints(pointRule12);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_13.title());
						fraudPoint.setRiskPoints(pointRule13);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_14.title());
						fraudPoint.setRiskPoints(pointRule14);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);		
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_15.title());
						fraudPoint.setRiskPoints(pointRule15);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_16.title());
						fraudPoint.setRiskPoints(pointRule16);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_17.title());
						fraudPoint.setRiskPoints(pointRule17);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
		
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_18.title());
						fraudPoint.setRiskPoints(pointRule18);				
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_19.title());
						fraudPoint.setRiskPoints(pointRule19);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_20.title());
						fraudPoint.setRiskPoints(pointRule20);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_21.title());
						fraudPoint.setRiskPoints(pointRule21);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_22.title());
						fraudPoint.setRiskPoints(pointRule22);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_23.title());
						fraudPoint.setRiskPoints(pointRule23);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_24.title());
						fraudPoint.setRiskPoints(pointRule24);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_25.title());
						fraudPoint.setRiskPoints(pointRule25);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);	
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_26.title());
						fraudPoint.setRiskPoints(pointRule26);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);		
		
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_27.title());
						fraudPoint.setRiskPoints(pointRule27);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);				
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_28.title());
						fraudPoint.setRiskPoints(pointRule28);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);				
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_29.title());
						fraudPoint.setRiskPoints(pointRule29);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);				
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_30.title());
						fraudPoint.setRiskPoints(pointRule30);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);				
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_31.title());
						fraudPoint.setRiskPoints(pointRule31);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);				
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_32.title());
						fraudPoint.setRiskPoints(pointRule32);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_33.title());
						fraudPoint.setRiskPoints(pointRule33);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);

						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_34.title());
						fraudPoint.setRiskPoints(pointRule34);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_35.title());
						fraudPoint.setRiskPoints(pointRule35);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_36.title());
						fraudPoint.setRiskPoints(pointRule36);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);						
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_37.title());
						fraudPoint.setRiskPoints(pointRule37);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_38.title());
						fraudPoint.setRiskPoints(pointRule38);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_39.title());
						fraudPoint.setRiskPoints(pointRule39);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_40.title());
						fraudPoint.setRiskPoints(pointRule40);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						/*fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_41.title());
						fraudPoint.setRiskPoints(pointRule41);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);*/
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_42.title());
						fraudPoint.setRiskPoints(pointRule42);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_43.title());
						fraudPoint.setRiskPoints(pointRule43);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_44.title());
						fraudPoint.setRiskPoints(pointRule44);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_45.title());
						fraudPoint.setRiskPoints(pointRule45);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_46.title());
						fraudPoint.setRiskPoints(pointRule46);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_47.title());
						fraudPoint.setRiskPoints(pointRule47);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						fraudPoint.setFrdFraudSuspicionCase(fraudCase);
						fraudPoint.setFraudRuleName(FraudRuleConstants.FRAUD_RULE_48.title());
						fraudPoint.setRiskPoints(pointRule48);
						fraudPointSessionHome.persistFrdFraudSuspicionPoint(fraudPoint);
						
						//add fraud case history
						CommonUtil.logInfo(CLASS_NAME, "add fraud case history");
						FrdFraudCaseHistoryPK frdFraudCaseHistoryPK = new FrdFraudCaseHistoryPK();
						frdFraudCaseHistoryPK.setFraudSuspicionCaseId(fraudCase.getFraudSuspicionCaseId());
						frdFraudCaseHistoryPK.setFraudCaseHistoryDate(new Timestamp(System.currentTimeMillis()));
						
						FrdFraudCaseHistory entityFraudHistory = new FrdFraudCaseHistory();
						entityFraudHistory.setFrdFraudCaseStatus(frdFraudCaseStatus);
						entityFraudHistory.setId(frdFraudCaseHistoryPK);
						entityFraudHistory.setFraudCaseHistoryNotes("Calculated by System");
						fraudCaseHistorySessionHome.persistFrdFraudCaseHistory(entityFraudHistory);
				}else{
					isSuccessCalculate=false;
					CommonUtil.logError(CLASS_NAME, "Persist Fraud Suspicion Case OrderId : "+venOrder.getWcsOrderId() + " already exist");
				}							
			}catch (Exception e) {
				isSuccessCalculate=false;
				CommonUtil.logError(CLASS_NAME, "Fraud calculate failed when persisting fraud points for wcs order id: "+venOrder.getWcsOrderId());
				CommonUtil.logError(CLASS_NAME, e);
				e.printStackTrace();
			}finally{
				try {
					if(locator!=null){
						locator.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return isSuccessCalculate;
	}
	
	
	public boolean isIPAddressWhitelistBlacklist(String ipAddress, String type){
		Boolean result=false;
		Locator<Object> locator = null;		
		try {
			locator = new Locator<Object>();
			FrdEntityBlacklistSessionEJBRemote sessionHome = (FrdEntityBlacklistSessionEJBRemote) locator.lookup(FrdEntityBlacklistSessionEJBRemote.class, "FrdEntityBlacklistSessionEJBBean");
			List<FrdEntityBlacklist> ipBlacklistList = sessionHome.queryByRange("select o from FrdEntityBlacklist o where o.blackOrWhiteList = upper('"+type+"') and o.blacklistString = '"+ipAddress+"'", 0, 0);
			if(ipBlacklistList.size()>0){
				CommonUtil.logInfo(CLASS_NAME, "ip "+type+" found");
				result=true;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if(locator!=null){
					locator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return result;
	}
	
	public ArrayList<String> isCustomerWhitelistBlacklist(VenOrder venOrder){
		Boolean result=false;
		Locator<Object> locator = null;	
		ArrayList<String> blacklistReason=new ArrayList<String>();
		try {			
			locator = new Locator<Object>();
			FrdCustomerWhitelistBlacklistSessionEJBRemote sessionHome = (FrdCustomerWhitelistBlacklistSessionEJBRemote) locator.lookup(FrdCustomerWhitelistBlacklistSessionEJBRemote.class, "FrdCustomerWhitelistBlacklistSessionEJBBean");
			FrdBlacklistReasonSessionEJBRemote blacklistReasonSessionHome = (FrdBlacklistReasonSessionEJBRemote) locator.lookup(FrdBlacklistReasonSessionEJBRemote.class, "FrdBlacklistReasonSessionEJBBean");
			VenOrderAddressSessionEJBRemote orderAddressSessionHome = (VenOrderAddressSessionEJBRemote) locator.lookup(VenOrderAddressSessionEJBRemote.class, "VenOrderAddressSessionEJBBean");
			VenOrderContactDetailSessionEJBRemote orderContactDetailSessionHome = (VenOrderContactDetailSessionEJBRemote) locator.lookup(VenOrderContactDetailSessionEJBRemote.class, "VenOrderContactDetailSessionEJBBean");
			VenOrderItemContactDetailSessionEJBRemote orderItemContactDetailSessionHome = (VenOrderItemContactDetailSessionEJBRemote) locator.lookup(VenOrderItemContactDetailSessionEJBRemote.class, "VenOrderItemContactDetailSessionEJBBean");
			VenAddressSessionEJBRemote shippingAddressSessionHome = (VenAddressSessionEJBRemote) locator.lookup(VenAddressSessionEJBRemote.class, "VenAddressSessionEJBBean");
			VenOrderPaymentAllocationSessionEJBRemote allocationSessionHome = (VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");
			
			
			List<FrdCustomerWhitelistBlacklist> customerNameBlacklistList = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where upper(o.customerFullName)<>'ANONYMOUS' and upper(o.customerFullName) = '"+venOrder.getVenCustomer().getVenParty().getFullOrLegalName().toUpperCase()+"'", 0, 0);
			if(customerNameBlacklistList.size()>0){
				CommonUtil.logInfo(CLASS_NAME, "customer name blacklist found");
				result=true;
				
				blacklistReason.add("Customer name blacklist");
			}
			
			List<FrdCustomerWhitelistBlacklist> customerAddressBlacklistList = null;
			List<VenOrderAddress> orderAddressBlacklistList = orderAddressSessionHome.queryByRange("select o from VenOrderAddress o where o.venOrder.orderId ="+venOrder.getOrderId(), 0, 1);
			
			if(orderAddressBlacklistList.size()>0){
				if(orderAddressBlacklistList.get(0).getVenAddress().getStreetAddress1()!=null){
					customerAddressBlacklistList = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where upper(o.address) like '%"+(orderAddressBlacklistList.get(0).getVenAddress().getStreetAddress1().toUpperCase())+"%'", 0, 0);
					if(customerAddressBlacklistList.size()>0){
						CommonUtil.logInfo(CLASS_NAME, "customer address blacklist found");
						result=true;
													
						blacklistReason.add("Customer address blacklist");
					}
				}
			}
			
			List<FrdCustomerWhitelistBlacklist> emailBlacklistList = null;
			List<VenOrderContactDetail> contactDetailEmailBlacklistList = orderContactDetailSessionHome.queryByRange("select o from VenOrderContactDetail o where o.venOrder.orderId = "+venOrder.getOrderId()+" and o.venContactDetail.venContactDetailType.contactDetailTypeId ="+VenContactDetailTypeConstants.VEN_CONTACT_DETAIL_ID_EMAIL.id(), 0, 1);
			if(contactDetailEmailBlacklistList.size()>0){
				if(contactDetailEmailBlacklistList.get(0).getVenContactDetail().getContactDetail()!=null){
					emailBlacklistList = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where upper(o.email) like '"+contactDetailEmailBlacklistList.get(0).getVenContactDetail().getContactDetail().toUpperCase()+"'", 0, 0);
					if(emailBlacklistList.size()>0){
						CommonUtil.logInfo(CLASS_NAME, "customer email blacklist found");
						result=true;
						
						blacklistReason.add("Customer email blacklist");
					}
				}
			}
			
			List<FrdCustomerWhitelistBlacklist> PhoneBlacklistList = null;
			List<VenOrderContactDetail> contactDetailPhoneBlacklistList = orderContactDetailSessionHome.queryByRange("select o from VenOrderContactDetail o where o.venOrder.orderId = "+venOrder.getOrderId()+" and (o.venContactDetail.venContactDetailType.contactDetailTypeId ="+VenContactDetailTypeConstants.VEN_CONTACT_DETAIL_ID_PHONE.id()+" or o.venContactDetail.venContactDetailType.contactDetailTypeId ="+VenContactDetailTypeConstants.VEN_CONTACT_DETAIL_ID_MOBILE.id()+")", 0, 0);
			if(contactDetailPhoneBlacklistList.size()>0){
				for(int i=0;i<contactDetailPhoneBlacklistList.size();i++){
					PhoneBlacklistList = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where o.phoneNumber like '"+contactDetailPhoneBlacklistList.get(i).getVenContactDetail().getContactDetail()+"' or o.handphoneNumber like '"+contactDetailPhoneBlacklistList.get(i).getVenContactDetail().getContactDetail()+"'", 0, 0);
					if(PhoneBlacklistList.size()>0){
						CommonUtil.logInfo(CLASS_NAME, "customer phone blacklist found");
						result=true;
						
						blacklistReason.add("Customer phone blacklist");
					}
				}
			}			
			
			/**
			 *  cek blacklist shipping phone and hancphone
			 */
			List<FrdCustomerWhitelistBlacklist> shippingPhoneBlacklistList = null;
			List<VenOrderItemContactDetail> shippingContactDetailPhoneBlacklistList = orderItemContactDetailSessionHome.queryByRange("select o from VenOrderItemContactDetail o where o.venOrderItem.venOrder.orderId = "+venOrder.getOrderId()+" and (o.venContactDetail.venContactDetailType.contactDetailTypeId ="+VenContactDetailTypeConstants.VEN_CONTACT_DETAIL_ID_PHONE.id()+" or o.venContactDetail.venContactDetailType.contactDetailTypeId ="+VenContactDetailTypeConstants.VEN_CONTACT_DETAIL_ID_MOBILE.id()+")", 0, 0);
			if(shippingContactDetailPhoneBlacklistList.size()>0){
				for(int i=0;i<shippingContactDetailPhoneBlacklistList.size();i++){
					shippingPhoneBlacklistList = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where o.shippingPhoneNumber like '"+shippingContactDetailPhoneBlacklistList.get(i).getVenContactDetail().getContactDetail()+"' or o.shippingHandphoneNumber like '"+shippingContactDetailPhoneBlacklistList.get(i).getVenContactDetail().getContactDetail()+"'", 0, 0);
					if(shippingPhoneBlacklistList.size()>0){
						CommonUtil.logInfo(CLASS_NAME, "Shipping phone blacklist found");
						result=true;
						
						blacklistReason.add("Shipping phone blacklist");
					}
				}
			}			
			
			/**
			 * cek blacklist shipping address
			 */
			List<FrdCustomerWhitelistBlacklist> shippingAddress = null;
			List<VenAddress> shippingAddressBlacklistList = shippingAddressSessionHome.queryByRange("select o from VenAddress o where o.addressId in (select u.venAddress.addressId from VenOrderItem u where u.venOrder.orderId="+venOrder.getOrderId()+")", 0, 1);
			if(shippingAddressBlacklistList.size()>0){
				for(int i=0;i<shippingAddressBlacklistList.size();i++){
					shippingAddress = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where o.shippingAddress like '"+(shippingAddressBlacklistList.get(i).getStreetAddress1() == null?"":shippingAddressBlacklistList.get(i).getStreetAddress1())+"'", 0, 0);
					if(shippingAddress.size()>0){
						CommonUtil.logInfo(CLASS_NAME, "Shipping Address blacklist found");
						result=true;
						
						blacklistReason.add("Shipping Address blacklist");
					}
				}
			}			
			
			/**
			 *  cek blacklist credit card
			 */			
			List<FrdCustomerWhitelistBlacklist> allocationBlacklistList = null;
			List<VenOrderPaymentAllocation> ccBlacklistList = allocationSessionHome.queryByRange("select o from VenOrderPaymentAllocation o where o.venOrder.orderId = "+venOrder.getOrderId(), 0, 0);
			if(ccBlacklistList.size()>0){				
				for(int i=0;i<ccBlacklistList.size();i++){
					if(ccBlacklistList.get(i).getVenOrderPayment().getMaskedCreditCardNumber()!=null){
							allocationBlacklistList = sessionHome.queryByRange("select o from FrdCustomerWhitelistBlacklist o where o.ccNumber like '"+ccBlacklistList.get(i).getVenOrderPayment().getMaskedCreditCardNumber()+"' ", 0, 0);
							if(allocationBlacklistList.size()>0){
								CommonUtil.logInfo(CLASS_NAME, "Credit Card blacklist found");
								result=true;
								
								blacklistReason.add("Credit Card blacklist");
						}
					}
				}
			}			
			
			//insert blacklist reason if it is blacklisted
			if(result==true){
				FrdBlacklistReason reason = new FrdBlacklistReason();
				reason.setOrderId(venOrder.getOrderId());
				reason.setWcsOrderId(venOrder.getWcsOrderId());
				for(int i=0;i<blacklistReason.size();i++){
					reason.setBlacklistReason(blacklistReason.get(i));
					blacklistReasonSessionHome.persistFrdBlacklistReason(reason);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if(locator!=null){
					locator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return blacklistReason;
	}
	
	public String getDuplicateOrderReport(){
		return this.duplicateOrderIdReport;
	}
	
}
