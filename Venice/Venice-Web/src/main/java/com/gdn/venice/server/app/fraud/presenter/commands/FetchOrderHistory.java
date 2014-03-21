package com.gdn.venice.server.app.fraud.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.VenMerchantProductSessionEJBRemote;
import com.gdn.venice.facade.VenOrderContactDetailSessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentAllocationSessionEJBRemote;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenProductCategory;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.util.VeniceConstants;

/**
 * Fetch filter order
 * 
 * @author Daniel
 */

public class FetchOrderHistory implements RafDsCommand {

	RafDsRequest request;
	
	public FetchOrderHistory(RafDsRequest request){
		this.request=request;
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		Locator<Object> locator=null;
		
		try{
			locator = new Locator<Object>();					
			VenOrderContactDetailSessionEJBRemote venOrderContactDetailSessionHome = (VenOrderContactDetailSessionEJBRemote) locator.lookup(VenOrderContactDetailSessionEJBRemote.class, "VenOrderContactDetailSessionEJBBean");		
			VenOrderPaymentAllocationSessionEJBRemote orderAllocationSessionHome = (VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");		
			VenMerchantProductSessionEJBRemote venMerchantProductSessionHome = (VenMerchantProductSessionEJBRemote) locator.lookup(VenMerchantProductSessionEJBRemote.class, "VenMerchantProductSessionEJBBean");		
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();		
			
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			String mobile="",email="",department="";
			
			if (criteria == null) {
				String query="select o from VenOrderPaymentAllocation o join fetch o.venOrder oi join fetch oi.venOrderItems oe  ";			
				venOrderPaymentAllocationList = orderAllocationSessionHome.queryByRange(query,0, 50);
			} else {
				String query="select o from VenOrderPaymentAllocation o join fetch o.venOrder oi join fetch oi.venOrderItems oe where ";	
				List<JPQLSimpleQueryCriteria> simpleCriteriaList = criteria.getSimpleCriteria();
				for (int i=0;i<simpleCriteriaList.size();i++) {
					if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERCONTACTDETAIL_EMAIL)) {
						query = query + " oi.orderId in (select u.venOrder.orderId from VenOrderContactDetail u where upper(u.venContactDetail.contactDetail) like upper('%"+simpleCriteriaList.get(i).getValue()+"%') and u.venContactDetail.venContactDetailType.contactDetailTypeId = 3)";
					} else if (simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_VENPARTY_FULLORLEGALNAME)) {
						query = query + " upper(oi.venCustomer.venParty.fullOrLegalName) like upper('%"+simpleCriteriaList.get(i).getValue()+"%')";
					}else if(simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENADDRESS_STREETADDRESS1)){
						query = query + " upper(oe.venAddress.streetAddress1) like upper('%"+simpleCriteriaList.get(i).getValue()+"%')";
					}else if(simpleCriteriaList.get(i).getFieldName().equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERCONTACTDETAIL_MOBILE)){
						query = query + " oi.orderId in (select u.venOrder.orderId from VenOrderContactDetail u where u.venContactDetail.contactDetail like '%"+simpleCriteriaList.get(i).getValue()+"%' and u.venContactDetail.venContactDetailType.contactDetailTypeId = 1)";
					}
					if(i!=simpleCriteriaList.size()-1){
						query=query+" and ";
					}
				}
				venOrderPaymentAllocationList = orderAllocationSessionHome.queryByRange(query,0, 0);
			}
			
			for(int i=0;i<venOrderPaymentAllocationList.size();i++){
				if(venOrderPaymentAllocationList.get(i).getVenOrder()!=null){
					
					List<VenOrderContactDetail> venOrderContactDetailAllocationList = new ArrayList<VenOrderContactDetail>();		
					String query="select ou from VenOrderContactDetail ou where ou.venOrder.orderId = "+ venOrderPaymentAllocationList.get(i).getVenOrder().getOrderId();	
					venOrderContactDetailAllocationList = venOrderContactDetailSessionHome.queryByRange(query,0, 0);
					
		    		 for(VenOrderContactDetail itemVenCont : venOrderContactDetailAllocationList){			    			
		    			 if(itemVenCont.getVenContactDetail().getVenContactDetailType().getContactDetailTypeId()==VeniceConstants.VEN_CONTACT_TYPE_MOBILE && mobile.equals("")){								
								mobile=itemVenCont.getVenContactDetail().getContactDetail();
							}else if(itemVenCont.getVenContactDetail().getVenContactDetailType().getContactDetailTypeId()==VeniceConstants.VEN_CONTACT_TYPE_EMAIL && email.equals("")){
								email=itemVenCont.getVenContactDetail().getContactDetail();
							}
		    		 }
				}
				
			    for(VenOrderItem orderItem : venOrderPaymentAllocationList.get(i).getVenOrder().getVenOrderItems()){
					HashMap<String, String> map = new HashMap<String, String>();

					List<VenMerchantProduct> venMerchantProductList = new ArrayList<VenMerchantProduct>();		
					String query=" select o from VenMerchantProduct o join fetch o.venProductCategories oi where o.productId = "+orderItem.getVenMerchantProduct().getProductId();
					venMerchantProductList = venMerchantProductSessionHome.queryByRange(query,0, 0);
					
					if(venMerchantProductList!=null){
						for(VenProductCategory itemDepartment: venMerchantProductList.get(0).getVenProductCategories()){
							if(itemDepartment.getLevel().equals(2)){
			    				 department =itemDepartment.getProductCategory().contains("(")?itemDepartment.getProductCategory().split("\\(")[0]:itemDepartment.getProductCategory();
			    			 }
						}
					}
					
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_ORDERDATE, venOrderPaymentAllocationList.get(i).getVenOrder().getOrderDate()!=null?venOrderPaymentAllocationList.get(i).getVenOrder().getOrderDate().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID, venOrderPaymentAllocationList.get(i).getVenOrder().getWcsOrderId()!=null?venOrderPaymentAllocationList.get(i).getVenOrder().getWcsOrderId():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_ORDERITEMID, orderItem.getOrderItemId()!=null? orderItem.getOrderItemId().toString():"");	
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERCONTACTDETAIL_EMAIL, email);
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_VENPARTY_FULLORLEGALNAME, venOrderPaymentAllocationList.get(i).getVenOrder().getVenCustomer().getCustomerUserName()!=null?venOrderPaymentAllocationList.get(i).getVenOrder().getVenCustomer().getVenParty().getFullOrLegalName():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENMERCHANTPRODUCT_WCSPRODUCTNAME, orderItem.getVenMerchantProduct().getWcsProductName()!=null? orderItem.getVenMerchantProduct().getWcsProductName():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENADDRESS_VENCITY_CITYID, orderItem.getVenAddress().getVenCity().getCityId().toString()!=null?orderItem.getVenAddress().getVenCity().getCityId().toString():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_TOTAL, orderItem.getTotal()!=null?orderItem.getTotal().toString():"");	
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENMERCHANTPRODUCT_VENPRODUCTCATEGORIES_PRODUCTCATEGORY, department);
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTCODE, venOrderPaymentAllocationList.get(i).getVenOrderPayment().getVenWcsPaymentType().getWcsPaymentTypeCode()!=null?venOrderPaymentAllocationList.get(i).getVenOrderPayment().getVenWcsPaymentType().getWcsPaymentTypeCode():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_MASKEDCREDITCARDNUMBER, venOrderPaymentAllocationList.get(i).getVenOrderPayment().getMaskedCreditCardNumber()!=null?venOrderPaymentAllocationList.get(i).getVenOrderPayment().getMaskedCreditCardNumber():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENBANK_BANKSHORTNAME, venOrderPaymentAllocationList.get(i).getVenOrderPayment().getVenBank().getBankShortName()!=null?venOrderPaymentAllocationList.get(i).getVenOrderPayment().getVenBank().getBankShortName():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_THREEDSSECURITYLEVELAUTH, venOrderPaymentAllocationList.get(i).getVenOrderPayment().getThreeDsSecurityLevelAuth()!=null?venOrderPaymentAllocationList.get(i).getVenOrderPayment().getThreeDsSecurityLevelAuth():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENADDRESS_STREETADDRESS1, orderItem.getVenAddress().getStreetAddress1()!=null?orderItem.getVenAddress().getStreetAddress1():"");
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERCONTACTDETAIL_MOBILE, mobile);
					map.put(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERSTATUS_ORDERSTATUSCODE, venOrderPaymentAllocationList.get(i).getVenOrder().getVenOrderStatus().getOrderStatusCode()!=null?venOrderPaymentAllocationList.get(i).getVenOrder().getVenOrderStatus().getOrderStatusCode():"");
					
					dataList.add(map);
			    }
			}

			rafDsResponse.setStatus(0);
			rafDsResponse.setStartRow(request.getStartRow());
			rafDsResponse.setTotalRows(dataList.size());
			rafDsResponse.setEndRow(request.getStartRow()+dataList.size());
		}catch(Exception e){
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}finally{
			try{
				if(locator!=null){
					locator.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
