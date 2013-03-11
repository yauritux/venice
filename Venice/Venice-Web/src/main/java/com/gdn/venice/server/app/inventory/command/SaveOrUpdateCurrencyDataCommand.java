package com.gdn.venice.server.app.inventory.command;

import java.util.Date;
import java.util.HashMap;

import com.gdn.inventory.exchange.entity.Currency;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.server.app.inventory.service.CurrencyManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
*
* @author Maria Olivia
*/
public class SaveOrUpdateCurrencyDataCommand implements RafRpcCommand {

	// The map of all the parameters passed to the command
	String data;
	HashMap<String, String> dataMap;
	String username, url;
	CurrencyManagementService currencyService;
	/**
	 * Basic constructor with parameters passed in XML string
	 *
	 * @param parameter a list of the parameters for the form in XML
	 */
	public SaveOrUpdateCurrencyDataCommand(String username, String data) {
		this.data = data;
		this.username = username;
	}

	/* (non-Javadoc)
	 * @see com.gdn.venice.server.command.RafRpcCommand#execute()
	 */
	@Override
	public String execute() {
		Currency currency;
		ResultWrapper<Currency> currencyWrapper;
		try {
			dataMap = Util.formHashMapfromXML(data);
			currencyService = new CurrencyManagementService();
			currency = new Currency();
			currency.setCurrency(dataMap.get(DataNameTokens.INV_CURRENCY_CURRENCY).toUpperCase());
			currency.setRate(Double.parseDouble(dataMap.get(DataNameTokens.INV_CURRENCY_RATE)));
			if(dataMap.get(DataNameTokens.INV_CURRENCY_ID) == null){
				System.out.println("Create");
				currency.setCreatedBy(username);
				currency.setCreatedDate(new Date());
				currency.setActive(true);
				currency.setDeleted(false);
			} else {
				System.out.println("Edit");
				currency.setId(Long.parseLong(dataMap.get(DataNameTokens.INV_CURRENCY_ID)));
				currency.setUpdatedBy(username);
				currency.setUpdatedDate(new Date());
			}
			
			currencyWrapper = currencyService.saveOrUpdateCurrency(username, currency);
			if(currencyWrapper != null){
				if(!currencyWrapper.isSuccess()){
					return currencyWrapper.getError();
				}
			} else {
				return "Failed saving currency, error connection";
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "Failed saving currency, try again later. If error persist please contact administrator";
		}
		return "0";
	}
}
