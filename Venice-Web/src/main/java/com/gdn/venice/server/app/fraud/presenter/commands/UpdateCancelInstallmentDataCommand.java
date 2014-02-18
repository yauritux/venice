package com.gdn.venice.server.app.fraud.presenter.commands;

import java.sql.Timestamp;
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
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for cancel installment
 * 
 * @author Roland
 */

public class UpdateCancelInstallmentDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateCancelInstallmentDataCommand(RafDsRequest request, String username) {
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
	        
			Date date =new Date(System.currentTimeMillis());
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
					if(key.equals(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTCANCELFLAG)) {
						payment.setInstallmentCancelFlag(new Boolean(data.get(key)));
						payment.setInstallmentCancelDate(new Timestamp(date.getTime()));
					}
				}						
				
				sessionHome.mergeVenOrderPayment(payment);		
			}
			
			VenOrderPaymentInstallmentHistory paymentHistory = new VenOrderPaymentInstallmentHistory();
				
			VenOrderPaymentInstallmentHistoryPK pk = new VenOrderPaymentInstallmentHistoryPK();
			pk.setInstallmentTimestamp(date);
			pk.setOrderPaymentId(payment.getOrderPaymentId());
			
			paymentHistory.setId(pk);
			paymentHistory.setHistoryReason("Update cancel installment flag, updated by "+username);
			
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
