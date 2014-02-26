/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Currency;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.CurrencyManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Maria Olivia
 */
public class FetchCurrencyDataCommand implements RafDsCommand {

	private RafDsRequest request;
	CurrencyManagementService currencyService;

	public FetchCurrencyDataCommand(RafDsRequest request) {
		this.request = request;
	}

	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

		try {
			currencyService = new CurrencyManagementService();
			InventoryPagingWrapper<Currency> currencysWrapper = currencyService.getCurrencyData(request);
			if(currencysWrapper != null){
				//Put result
				System.out.println(currencysWrapper.getContent().size());
				for(Currency currency : currencysWrapper.getContent()){
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(DataNameTokens.INV_CURRENCY_ID, currency.getId().toString());
					map.put(DataNameTokens.INV_CURRENCY_CURRENCY, currency.getCurrency());
					map.put(DataNameTokens.INV_CURRENCY_RATE, currency.getRate()+"");
					dataList.add(map);
				}

				//Set DSResponse's properties
				rafDsResponse.setStatus(0);
				rafDsResponse.setStartRow(request.getStartRow());
				rafDsResponse.setTotalRows(Integer.parseInt(currencysWrapper.getTotalElements()+""));
				rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}

		//Set data and return
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
