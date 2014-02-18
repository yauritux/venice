package com.gdn.venice.server.app.fraud.presenter.commands;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.VenMigsUploadMasterSessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentAllocationSessionEJBRemote;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

public class FetchMIGSUploadMaster implements RafDsCommand {
	RafDsRequest request;
	
	public FetchMIGSUploadMaster(RafDsRequest request){
		this.request = request;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		Locator<Object> locator = null;
		
			try {
						//Lookup into EJB
						locator = new Locator<Object>();
						VenMigsUploadMasterSessionEJBRemote sessionHome = (VenMigsUploadMasterSessionEJBRemote) locator.lookup(VenMigsUploadMasterSessionEJBRemote.class, "VenMigsUploadMasterSessionEJBBean");
						VenOrderPaymentAllocationSessionEJBRemote venOrderPaymentAllocationSessionHome=(VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");
						List<VenMigsUploadMaster> venMigsUploadMasterList = null;
						List<VenOrderPaymentAllocation> venOrderPaymentAllocationList=null;
						
						String wcsOrderId=request.getParams().get(DataNameTokens.FRDFRAUDSUSPICIONCASE_VENORDER_WCSORDERID);
						
						//Build query
						String query = "select o from VenOrderPaymentAllocation o where o.venOrder.wcsOrderId like '"+wcsOrderId+"'";
						venOrderPaymentAllocationList=venOrderPaymentAllocationSessionHome.queryByRange(query, 0, 0);
							
						if(venOrderPaymentAllocationList.size()>0){
						query = "select o from VenMigsUploadMaster o where (o.merchantTransactionReference like '"+wcsOrderId+"-%' or o.merchantTransactionReference like '"+wcsOrderId+"') and o.authorisationCode like '" + venOrderPaymentAllocationList.get(0).getVenOrderPayment().getReferenceId() + "'";
						venMigsUploadMasterList = sessionHome.queryByRange(query, 0, 0);
				
							if(venMigsUploadMasterList!=null){
								for(VenMigsUploadMaster migs: venMigsUploadMasterList){
									HashMap<String, String> map = new HashMap<String, String>();
									map.put(DataNameTokens.MIGSMASTER_MERCHANTTRANSACTIONREFERENCE, migs.getMerchantTransactionReference()!=null ?migs.getMerchantTransactionReference().toString():"");
									map.put(DataNameTokens.MIGSMASTER_RESPONSECODE, migs.getResponseCode()!=null ?migs.getResponseCode().toString():"");
									map.put(DataNameTokens.MIGSMASTER_DIALECTCSCRESULTCODE, migs.getDialectCscResultCode()!=null ?migs.getDialectCscResultCode().toString():"");
									map.put(DataNameTokens.MIGSMASTER_CARDNUMBER, migs.getCardNumber()!=null? migs.getCardNumber().toString():"");
									map.put(DataNameTokens.MIGSMASTER_VENORDERPAYMENT_THREEDSSECURITYLEVELAUTH, venOrderPaymentAllocationList.get(0).getVenOrderPayment().getThreeDsSecurityLevelAuth().toString()!=null?venOrderPaymentAllocationList.get(0).getVenOrderPayment().getThreeDsSecurityLevelAuth().toString():"");
									dataList.add(map);
								}
							}
						}
						//Set DSResponse's properties
						rafDsResponse.setStatus(0);
						rafDsResponse.setStartRow(request.getStartRow());
						rafDsResponse.setTotalRows(venMigsUploadMasterList.size());
						rafDsResponse.setEndRow(request.getStartRow() + dataList.size());		
			} catch (Exception e) {
				e.printStackTrace();
				rafDsResponse.setStatus(-1);
			} finally {
				try {
					if (locator != null) {
						locator.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	
			//Set data and return
			rafDsResponse.setData(dataList);
			return rafDsResponse;
		}

}
