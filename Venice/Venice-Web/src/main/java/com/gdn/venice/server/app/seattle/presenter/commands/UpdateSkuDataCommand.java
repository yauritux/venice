package com.gdn.venice.server.app.seattle.presenter.commands;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote;
import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for UpdateSkuDataCommand
 * 
 * @author Roland
 */

public class UpdateSkuDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateSkuDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<SeatOrderEtd> entitySkuList = new ArrayList<SeatOrderEtd>();		
		List<HashMap<String,String >> dataList = request.getData();		
		
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			SeatOrderEtdSessionEJBRemote sessionHome = (SeatOrderEtdSessionEJBRemote) locator.lookup(SeatOrderEtdSessionEJBRemote.class, "SeatOrderEtdSessionEJBBean");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
						
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);				
				try{
					entitySkuList = sessionHome.queryByRange("select o from SeatOrderEtd o where o.sku in (select u.wcsProductSku from VenMerchantProduct u where u.productId = "+new Long(data.get(DataNameTokens.VENMERCHANTPRODUCT_PRODUCTID))+")", 0, 0);
				}catch(NumberFormatException e){
					
				}				
				for (int j=0;j<entitySkuList.size();j++) {
					
					
					if(data.get(DataNameTokens.SEAT_ORDER_ETD_NEW)!=null){	
						Date newDate = getTimeafterAddDay(formatter.parse(data.get(DataNameTokens.SEAT_ORDER_ETD_NEW)),new Integer(entitySkuList.get(j).getLogisticsEtd()+""));
						if(newDate.compareTo(entitySkuList.get(j).getEtdMax())==new Long(1)){							
							entitySkuList.get(j).setDiffEtd(new BigDecimal((Math.abs(newDate.getTime()-entitySkuList.get(j).getEtdMax().getTime())) / (24 * 60 * 60 * 1000)));											
						}else {							
							entitySkuList.get(j).setDiffEtd(new BigDecimal(0));											
						}
						entitySkuList.get(j).setEtdNew(formatter.parse(data.get(DataNameTokens.SEAT_ORDER_ETD_NEW)));
					}
					if(data.get(DataNameTokens.SEAT_ORDER_ETD_START)!=null){
						entitySkuList.get(j).setStartDate(formatter.parse(data.get(DataNameTokens.SEAT_ORDER_ETD_START)));		
					}		
					if(data.get(DataNameTokens.SEAT_ORDER_ETD_END)!=null){
						entitySkuList.get(j).setEndDate(formatter.parse(data.get(DataNameTokens.SEAT_ORDER_ETD_END)));		
					}		
					entitySkuList.get(j).setByUser(username);
					entitySkuList.get(j).setUpdateEtdDate(new Timestamp(System.currentTimeMillis()));
				}				
			}			
			if(entitySkuList.size()>0){
				sessionHome.mergeSeatOrderEtdList((ArrayList<SeatOrderEtd>)entitySkuList);
			}
			
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
	
	private Date getTimeafterAddDay(Date dateTime, int addDay){
		 Calendar cal = Calendar.getInstance();
		    cal.setTime(dateTime);
		    cal.add(Calendar.DATE, addDay);		 
		    return new Date(cal.getTime().getTime());
	 }
}
