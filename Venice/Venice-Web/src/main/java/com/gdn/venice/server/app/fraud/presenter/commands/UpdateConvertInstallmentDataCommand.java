package com.gdn.venice.server.app.fraud.presenter.commands;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentSessionEJBRemote;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory;
import com.gdn.venice.persistence.VenOrderPaymentInstallmentHistoryPK;
import com.gdn.venice.persistence.VenWcsPaymentType;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for convert installment
 * 
 * @author Roland
 */

public class UpdateConvertInstallmentDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateConvertInstallmentDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
				
		List<HashMap<String,String >> dataList = request.getData();		
		VenOrderPayment payment = new VenOrderPayment();
				
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			VenOrderPaymentSessionEJBRemote sessionHome = (VenOrderPaymentSessionEJBRemote) locator.lookup(VenOrderPaymentSessionEJBRemote.class, "VenOrderPaymentSessionEJBBean");
			VenOrderPaymentInstallmentHistorySessionEJBRemote paymentHistoryHome = (VenOrderPaymentInstallmentHistorySessionEJBRemote) locator.lookup(VenOrderPaymentInstallmentHistorySessionEJBRemote.class, "VenOrderPaymentInstallmentHistorySessionEJBBean");
	        
			String reason = "";
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter = data.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_ORDERPAYMENTID)) {
						try{
							payment = sessionHome.queryByRange("select o from VenOrderPayment o where o.orderPaymentId="+new Long(data.get(key)), 0, 1).get(0);
						}catch(IndexOutOfBoundsException e){
							payment.setOrderPaymentId(new Long(data.get(key)));
						}
						break;
					}
				}						
				
				iter = data.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					if(key.equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID)) {
						VenWcsPaymentType paymentType = new VenWcsPaymentType();
						paymentType.setWcsPaymentTypeId(new Long(data.get(key)));
						payment.setVenWcsPaymentType(paymentType);
						
						reason +="payment type";
					}else if(key.equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR)){
						payment.setTenor(new Integer(data.get(key)));
						
						reason +="tenor";
					}
				}						
				
				sessionHome.mergeVenOrderPayment(payment);		
			}
			
			VenOrderPaymentInstallmentHistory paymentHistory = new VenOrderPaymentInstallmentHistory();
				
			VenOrderPaymentInstallmentHistoryPK pk = new VenOrderPaymentInstallmentHistoryPK();
			pk.setInstallmentTimestamp(new Date(System.currentTimeMillis()));
			pk.setOrderPaymentId(payment.getOrderPaymentId());
			
			paymentHistory.setId(pk);
			paymentHistory.setHistoryReason("Update "+reason+", updated by "+username);
			
			paymentHistoryHome.persistVenOrderPaymentInstallmentHistory(paymentHistory);
				
			rafDsResponse.setStatus(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			rafDsResponse.setStatus(-1);
		} finally {
			try {
				if(locator!=null){
					locator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
