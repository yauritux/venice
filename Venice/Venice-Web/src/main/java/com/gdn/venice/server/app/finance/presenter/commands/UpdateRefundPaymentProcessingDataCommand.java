package com.gdn.venice.server.app.finance.presenter.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.SetRootRule;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FinArFundsInRefundSessionEJBRemote;
import com.gdn.venice.persistence.FinArFundsInRefund;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

public class UpdateRefundPaymentProcessingDataCommand implements RafDsCommand {

	/*
	 * This is the data source request that is passed to 
	 * the command in the constructor. Originally it is
	 * built from the request body of the servlet using
	 * the XML parameters that are passed to the servlet
	 */
	RafDsRequest request;
	
	public UpdateRefundPaymentProcessingDataCommand(RafDsRequest request) {
		this.request = request;
	}
	/* (non-Javadoc)
	 * @see com.gdn.venice.server.command.RafDsCommand#execute()
	 */
	@Override
	public RafDsResponse execute() {	
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<FinArFundsInRefund> finArFundsInRefundList = new ArrayList<FinArFundsInRefund>();		
		List<HashMap<String,String >> dataList = request.getData();		
		FinArFundsInRefund finArFundsInRefund = new FinArFundsInRefund();
				
		Locator<FinArFundsInRefundSessionEJBRemote> FinArFundsInRefundLocator = null;
		
		try {
			FinArFundsInRefundLocator = new Locator<FinArFundsInRefundSessionEJBRemote>();
			
			FinArFundsInRefundSessionEJBRemote sessionHome = (FinArFundsInRefundSessionEJBRemote) FinArFundsInRefundLocator
			.lookup(FinArFundsInRefundSessionEJBRemote.class, "FinArFundsInRefundSessionEJBBean");
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);		
				
				Iterator<String> iter = data.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					if(key.equals(DataNameTokens.FINARFUNDSINREFUND_REFUNDRECORDID)){
						try{
							finArFundsInRefund = sessionHome.queryByRange("select o from FinArFundsInRefund o where o.refundRecordId="+new Long(data.get(key)), 0, 1).get(0); 
						}catch(IndexOutOfBoundsException e){
							finArFundsInRefund.setRefundRecordId(new Long(data.get(key)));
						}
						break;
					}
				}	
				
				iter = data.keySet().iterator();			
				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(DataNameTokens.FINARFUNDSINREFUND_BANKFEE)) {
						finArFundsInRefund.setFeeAmount(data.get(key)!=null?new BigDecimal(data.get(key)):new BigDecimal(data.get(0)));
						break;
					} 
				}						
				
				finArFundsInRefundList.add(finArFundsInRefund);			
			}
					
			sessionHome.mergeFinArFundsInRefundList((ArrayList<FinArFundsInRefund>)finArFundsInRefundList);			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			rafDsResponse.setStatus(-1);
		} finally {
			try {
				if(FinArFundsInRefundLocator!=null){
					FinArFundsInRefundLocator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}

}
