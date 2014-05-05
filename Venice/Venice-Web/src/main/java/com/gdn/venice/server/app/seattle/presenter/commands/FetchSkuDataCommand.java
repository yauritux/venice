package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote;
import com.gdn.venice.facade.VenMerchantProductSessionEJBRemote;
import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

public class FetchSkuDataCommand implements RafDsCommand {

	RafDsRequest request;
	String userName;	
	
	public FetchSkuDataCommand(RafDsRequest request, String userName){
		this.request=request;
		this.userName = userName;
		
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();			
		Locator<Object> locator = null;
		try{		
			List<VenMerchantProduct> venProductList = null;			
			List<SeatOrderEtd> itemEtd = null;
			locator = new Locator<Object>();
			VenMerchantProductSessionEJBRemote sessionHome = (VenMerchantProductSessionEJBRemote) locator.lookup(VenMerchantProductSessionEJBRemote.class, "VenMerchantProductSessionEJBBean");
			SeatOrderEtdSessionEJBRemote sessionEtdHome = (SeatOrderEtdSessionEJBRemote) locator.lookup(SeatOrderEtdSessionEJBRemote.class, "SeatOrderEtdSessionEJBBean");
			
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			if (criteria != null) {				
				VenMerchantProduct merchantProduct = new VenMerchantProduct();
				venProductList = sessionHome.findByVenMerchantProductLike(merchantProduct, criteria, 0, 0);		
			
			}
			if(venProductList!=null){
				for(int i=0; i<venProductList.size();i++){
					HashMap<String, String> map = new HashMap<String, String>();
					VenMerchantProduct list = venProductList.get(i);
					
						
	
					map.put(DataNameTokens.VENMERCHANTPRODUCT_PRODUCTID, list.getProductId().toString());
					map.put(DataNameTokens.VENMERCHANTPRODUCT_WCSPRODUCTSKU, Util.isNull(list.getWcsProductSku(),"").toString());	
					map.put(DataNameTokens.VENMERCHANTPRODUCT_WCSPRODUCTNAME, Util.isNull(list.getWcsProductName(),"").toString());			
					
					String sql ="select o from SeatOrderEtd o where o.sku='"+Util.isNull(list.getWcsProductSku(),"").toString()+"'";
					itemEtd = sessionEtdHome.queryByRange(sql, 0, 0);	
					if(!itemEtd.isEmpty() && itemEtd.size()>0){
						map.put(DataNameTokens.SEAT_ORDER_ETD_NEW,itemEtd.get(0).getEtdNew().toString());
						map.put(DataNameTokens.SEAT_ORDER_ETD_START, Util.isNull(itemEtd.get(0).getStartDate(),"").toString());	
						map.put(DataNameTokens.SEAT_ORDER_ETD_END, Util.isNull(itemEtd.get(0).getEndDate(),"").toString());	
					}			
										
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