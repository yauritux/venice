package com.gdn.venice.server.app.inventory.command;

import java.util.HashMap;

import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;

public class SaveOrUpdateStatusShelfWIPDataCommand implements RafRpcCommand {

	HashMap<String, String> shelfMap;
	String username, url;
	ShelfManagementService shelfService;

	public SaveOrUpdateStatusShelfWIPDataCommand(String username, String data) {		
		shelfMap = Util.formHashMapfromXML(data);		
		this.username = username;
	}

	@Override
	public String execute() {
		ShelfWIP shelf = new ShelfWIP();
		ResultWrapper<ShelfWIP> shelfWrapper;
		try {
			shelfService = new ShelfManagementService();			
			System.out.println("Masuk ke command update shelf wip");
			
			if (shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID) != null) {
				if (shelfMap.get(DataNameTokens.INV_SHELF_ACTIVESTATUS).equalsIgnoreCase("Non Active")) {
					System.out.println("non-active process");
					shelf.setApprovalType(ApprovalStatus.APPROVAL_NON_ACTIVE);
					shelf.setOriginalShelf(Long.parseLong(shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
				} else {
					System.out.println("update process");
					shelf.setApprovalType(ApprovalStatus.APPROVAL_UPDATE);
					shelf.setOriginalShelf(Long.parseLong(shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
				}
				
				shelfWrapper = shelfService.findById(username, shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID));
			}else{			
				shelfWrapper = shelfService.findInProcessById(username, shelfMap.get(DataNameTokens.INV_SHELF_ID));
			}
			
			if (shelfWrapper.isSuccess()) {
				shelf = shelfWrapper.getContent();
				
				System.out.println(shelfMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS));
				shelf.setApprovalStatus(ApprovalStatus.valueOf(shelfMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS)));
									
			} else {
				return "Failed saving shelf, " + shelfWrapper.getError();
			}
						
			shelfWrapper = shelfService.saveOrUpdateStatusShelfInProcess(username, shelf);
			if(!shelfWrapper.isSuccess()){
				return shelfWrapper.getError();
			}

		} catch (Exception e) {
			return "Failed saving shelf, try again later. If error persist please contact administrator";
		}
		return "0";
	}
}
