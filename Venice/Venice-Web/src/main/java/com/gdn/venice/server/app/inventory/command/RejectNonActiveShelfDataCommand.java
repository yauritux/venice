package com.gdn.venice.server.app.inventory.command;

import java.util.HashMap;

import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;

public class RejectNonActiveShelfDataCommand implements RafRpcCommand {

	HashMap<String, String> shelfMap;
	String username, url;
	ShelfManagementService shelfService;

	public RejectNonActiveShelfDataCommand(String username, String data) {		
		shelfMap = Util.formHashMapfromXML(data);		
		this.username = username;
	}

	@Override
	public String execute() {
		System.out.println("RejectNonActiveShelfDataCommand");
		ShelfWIP shelf = new ShelfWIP();
		ResultWrapper<ShelfWIP> shelfWrapper = new ResultWrapper<ShelfWIP>();
		try {
			shelfService = new ShelfManagementService();			;		
			shelfWrapper = shelfService.findShelfWIPById(username, shelfMap.get(DataNameTokens.INV_SHELF_ID));

			if (shelfWrapper!=null && shelfWrapper.isSuccess()) {
				shelf = shelfWrapper.getContent();
				shelf.setApprovalType(ApprovalStatus.APPROVAL_NON_ACTIVE);
				shelf.setOriginalShelf(new Long(shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
				shelf.setApprovalStatus(ApprovalStatus.valueOf(shelfMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS)));									
			} else {
				return "Failed reject non active shelf, " + shelfWrapper.getError();
			}

			shelfWrapper = shelfService.rejectNonActiveShelf(username, shelf);

			if(shelfWrapper==null || !shelfWrapper.isSuccess()){
				return shelfWrapper.getError();
			}

		} catch (Exception e) {
			return "Failed reject non active shelf, try again later. If error persist please contact administrator";
		}
		
		return "0";
	}
}
