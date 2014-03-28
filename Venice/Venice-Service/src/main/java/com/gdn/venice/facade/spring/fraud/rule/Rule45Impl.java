package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.dao.FrdParameterRule44DAO;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.facade.VenOrderAddressSessionEJBRemote;
import com.gdn.venice.facade.VenOrderContactDetailSessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentAllocationSessionEJBRemote;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service("Rule45")
public class Rule45Impl implements Rule  {


    private static final String VEN_CONTACT_DETAIL_ID_PHONE = "0";
    private static final String VEN_CONTACT_DETAIL_ID_MOBILE = "1";
    private static final String VEN_CONTACT_DETAIL_ID_EMAIL = "3";
	
	private static final String CLASS_NAME = Rule45Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule44DAO frdParameterRule45DAO;
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	@Autowired
	VenMerchantProductDAO venMerchantProductDAO;
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
    	  Locator<Object> locator = null;
          
          try {
              locator = new Locator<Object>();
    	
	    	 VenOrderAddressSessionEJBRemote orderAddressSessionHome = (VenOrderAddressSessionEJBRemote) locator.lookup(VenOrderAddressSessionEJBRemote.class, "VenOrderAddressSessionEJBBean");
	         VenOrderContactDetailSessionEJBRemote orderContactDetailSessionHome = (VenOrderContactDetailSessionEJBRemote) locator.lookup(VenOrderContactDetailSessionEJBRemote.class, "VenOrderContactDetailSessionEJBBean");
	         VenOrderPaymentAllocationSessionEJBRemote allocationSessionHome = (VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");
	
	    	 List<VenOrderAddress> orderHistoryEci5Address;
	         List<VenOrderAddress> orderEci5Address = orderAddressSessionHome.queryByRange("select o from VenOrderAddress o where o.venOrder.orderId =" + venOrder.getOrderId(), 0, 1);
	         if(orderEci5Address.size()>0){
	         	orderHistoryEci5Address=orderAddressSessionHome.queryByRange("select o from VenOrderAddress o where upper(o.venAddress.streetAddress1) like '%" + orderEci5Address.get(0).getVenAddress().getStreetAddress1().toUpperCase() + "%' and o.venOrder.orderId != " + venOrder.getOrderId(), 0, 1);
	         	if(orderHistoryEci5Address.size()>0){
	         		List<VenOrderContactDetail> orderHistoryEci5ContactDetail;	
	         		List<VenOrderContactDetail> orderEci5ContactDetail=orderContactDetailSessionHome.queryByRange("select o from VenOrderContactDetail o where o.venOrder.orderId = " + venOrder.getOrderId() + " and (o.venContactDetail.venContactDetailType.contactDetailTypeId =" + VEN_CONTACT_DETAIL_ID_PHONE + " or o.venContactDetail.venContactDetailType.contactDetailTypeId =" + VEN_CONTACT_DETAIL_ID_MOBILE + " or o.venContactDetail.venContactDetailType.contactDetailTypeId =" + VEN_CONTACT_DETAIL_ID_EMAIL+")", 0, 1);	         		
	         		List<VenOrderPaymentAllocation> orderHistoryEci5PaymentAllocation;
	         		if(orderEci5ContactDetail.size()>0){
		         		for(int i=0;i<orderHistoryEci5Address.size();i++){
		         			for(int j=0;j<orderEci5ContactDetail.size();j++){			         			
		         				orderHistoryEci5ContactDetail=orderContactDetailSessionHome.queryByRange("select o from VenOrderContactDetail o where o.venOrder.orderId = "+ orderEci5Address.get(i).getVenOrder().getOrderId() +" and o.venContactDetail.contactDetail = '" + orderEci5ContactDetail.get(j).getVenContactDetail().getContactDetail()+"'", 0, 1);          
			         			orderHistoryEci5PaymentAllocation=allocationSessionHome.queryByRange("select o from VenOrderPaymentAllocation o where o.venOrder.orderId = '"+ orderHistoryEci5ContactDetail.get(i).getVenOrder().getOrderId() +"' and venOrderPayment.threeDsSecurityLevelAuth != '07'", 0, 1);
			         			if(orderHistoryEci5PaymentAllocation.size()>0){
			                         result = true;            				
			         			}
		         			}
		         		}
	         		}
	         	}
	         }
          } catch (Exception ex) {
              ex.printStackTrace();
          } finally {
              try {
                  if (locator != null) {
                      locator.close();
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
	    	return result;
    }
	
}
