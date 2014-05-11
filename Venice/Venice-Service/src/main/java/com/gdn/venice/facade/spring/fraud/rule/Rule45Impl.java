package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule45DAO;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service("Rule45")
public class Rule45Impl implements Rule  {


    private static final long VEN_CONTACT_DETAIL_ID_PHONE = 0;
    private static final long VEN_CONTACT_DETAIL_ID_MOBILE = 1;
    private static final long VEN_CONTACT_DETAIL_ID_EMAIL = 3;
	
	private static final String CLASS_NAME = Rule45Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule45DAO frdParameterRule45DAO;
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	@Autowired
	VenMerchantProductDAO venMerchantProductDAO;
	@Autowired
	VenOrderAddressDAO venOrderAddressDAO;
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		int riskPoint = getRiskPointValue();
		
		List<VenOrderPaymentAllocation> orderPaymentAllocationList = getOrderPaymentAllocationByOrder(order);	
		String eciCode = orderPaymentAllocationList.get(0).getVenOrderPayment().getThreeDsSecurityLevelAuth() != null ? orderPaymentAllocationList.get(0).getVenOrderPayment().getThreeDsSecurityLevelAuth() : "";
    
        if(isEci5HistoryOrder(order) && eciCode.equals("05") ){
			totalRiskPoint = riskPoint;
        }
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);

		return totalRiskPoint;
	}
	
	public int getRiskPointValue(){
		return frdParameterRule45DAO.findByDescription("Risk Point").getValue();
	}
		
	public List<VenOrderPaymentAllocation> getOrderPaymentAllocationByOrder(VenOrder order){
		return venOrderPaymentAllocationDAO.findByVenOrder(order);
	}

    
    public boolean isEci5HistoryOrder(VenOrder venOrder) {
    	boolean result=false;  	
    	List<VenOrderAddress> orderHistoryEci5Address;
        List<VenOrderAddress> orderEci5Address = venOrderAddressDAO.findWithVenAddressesByVenOrder(venOrder);
        if(orderEci5Address.size()>0){
    		CommonUtil.logInfo(CLASS_NAME, "orderEci5Address>0" );
         orderHistoryEci5Address=venOrderAddressDAO.findByVenAddressStreetAddress1AndVenOrderOrderId(orderEci5Address.get(0).getVenAddress().getStreetAddress1(),venOrder);
        	if(orderHistoryEci5Address.size()>0){
        		CommonUtil.logInfo(CLASS_NAME, "orderHistoryEci5Address>0" );
        		List<VenOrderContactDetail> orderHistoryEci5ContactDetail;	
        		
        		List<VenOrderContactDetail> orderEci5ContactDetail=venOrderContactDetailDAO.findByVenOrderOrderIdAndVenContactDetailVenContactDetailTypeContactDetailTypeId(venOrder,VEN_CONTACT_DETAIL_ID_PHONE,VEN_CONTACT_DETAIL_ID_MOBILE,VEN_CONTACT_DETAIL_ID_EMAIL);
        		List<VenOrderPaymentAllocation> orderHistoryEci5PaymentAllocation;
        		
        		if(orderEci5ContactDetail.size()>0){
            		CommonUtil.logInfo(CLASS_NAME, "orderEci5ContactDetail>0" );
	        		for(int i=0;i<orderHistoryEci5Address.size();i++){
	            		CommonUtil.logInfo(CLASS_NAME, "orderHistoryEci5Address>0" );
	        			for(int j=0;j<orderEci5ContactDetail.size();j++){		
	                		CommonUtil.logInfo(CLASS_NAME, "orderEci5ContactDetail>0" );	         			
	        				orderHistoryEci5ContactDetail=venOrderContactDetailDAO.findByVenOrderOrderIdAndVenContactDetailContactDetail(orderHistoryEci5Address.get(i).getVenOrder(),orderEci5ContactDetail.get(j).getVenContactDetail().getContactDetail());
		        			if(orderHistoryEci5ContactDetail.size()>0){
		        				orderHistoryEci5PaymentAllocation=venOrderPaymentAllocationDAO.findByVenOrderAndVenOrderPaymentThreeDsSecurityAuthIsNot(orderHistoryEci5ContactDetail.get(0).getVenOrder(),"07");
		        				if(orderHistoryEci5PaymentAllocation.size()>0){
			                		CommonUtil.logInfo(CLASS_NAME, "orderHistoryEci5PaymentAllocation>0" );
			                        result = true;            				
			        			}
		        			}
	        			}
	        		}
        		}
        	}
        }
    	return result;
    }	
}
