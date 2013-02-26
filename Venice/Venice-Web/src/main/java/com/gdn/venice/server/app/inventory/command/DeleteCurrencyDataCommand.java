package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.Currency;

import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.CurrencyManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
*
* @author Maria Olivia
*/
public class DeleteCurrencyDataCommand implements RafRpcCommand {

	// The map of all the parameters passed to the command
	String username, id;
	CurrencyManagementService currencyService;
	/**
	 * Basic constructor with parameters passed in string
	 */
	public DeleteCurrencyDataCommand(String username, String id) {
		this.username = username;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.gdn.venice.server.command.RafRpcCommand#execute()
	 */
	@Override
	public String execute() {
		ResultWrapper<Currency> currencyWrapper;
		try {
			currencyService = new CurrencyManagementService();
			System.out.println("Masuk ke command save warehouse wip");
			currencyWrapper = currencyService.deleteCurrency(username, id);
			if(currencyWrapper != null){
				if(!currencyWrapper.isSuccess()){
					return currencyWrapper.getError();
				}
			} else {
				return "Failed deleting warehouse, error connection";
			}
		} catch (Exception e) {
			return "Failed saving warehouse, try again later. If error persist please contact administrator";
		}
		return "0";
	}
}
