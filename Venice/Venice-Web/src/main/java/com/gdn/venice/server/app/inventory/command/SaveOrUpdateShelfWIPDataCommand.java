package com.gdn.venice.server.app.inventory.command;

import java.util.Date;
import java.util.HashMap;

import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

public class SaveOrUpdateShelfWIPDataCommand implements RafRpcCommand {

	HashMap<String, String> dataMap;
	String username, url;
	ShelfManagementService shelfService;

	public SaveOrUpdateShelfWIPDataCommand(String username, String data) {
		dataMap = Util.formHashMapfromXML(data);
		this.username = username;
	}

	@Override
	public String execute() {
		ShelfWIP shelf;
		ResultWrapper<ShelfWIP> shelfWrapper;
		try {
			shelfService = new ShelfManagementService();
			System.out.println("Masuk ke command save shelf wip");
			if(dataMap.get(DataNameTokens.INV_SHELF_ID) == null){
				System.out.println("set common values");
				shelf = new ShelfWIP();
				shelf.setCode("");				
				shelf.setCreatedBy(username);
				shelf.setCreatedDate(new Date());
				shelf.setDeleted(false);
				shelf.setDiscriminator("shelfInProcess");
				shelf.setApprovalStatus(ApprovalStatus.CREATED);
				
				shelf.setDescription(dataMap.get(DataNameTokens.INV_SHELF_DESCRIPTION));

				if (dataMap.get(DataNameTokens.INV_SHELF_ORIGINID) == null) {
					System.out.println("add new shelf process");
					shelf.setApprovalType(ApprovalStatus.APPROVAL_CREATE);
					shelf.setActive(false);
				} else {
					if (dataMap.get(DataNameTokens.INV_SHELF_ACTIVESTATUS).equalsIgnoreCase("Non Active")) {
						System.out.println("non-active process");
						shelf.setApprovalType(ApprovalStatus.APPROVAL_NON_ACTIVE);
						shelf.setOriginalShelf(Long.parseLong(dataMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
					} else {
						System.out.println("update process");
						shelf.setApprovalType(ApprovalStatus.APPROVAL_UPDATE);
						shelf.setOriginalShelf(Long.parseLong(dataMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
					}
				}
			} else{
				System.out.println("Masuk ke command update shelf wip");
				shelfWrapper = shelfService.findInProcessById(username, dataMap.get(DataNameTokens.INV_SHELF_ID));
				
				if (shelfWrapper != null){
					if (shelfWrapper.isSuccess()) {
						shelf = shelfWrapper.getContent();
						if (dataMap.get(DataNameTokens.INV_SHELF_DESCRIPTION) != null) {
							System.out.println("update exsisting process");
							shelf.setDescription(dataMap.get(DataNameTokens.INV_SHELF_DESCRIPTION));
							shelf.setApprovalStatus(ApprovalStatus.CREATED);
						} else {
							System.out.println(dataMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS));
							shelf.setApprovalStatus(ApprovalStatus.valueOf(dataMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS)));
						}						
					} else {
						return "Failed saving shelf, " + shelfWrapper.getError();
					}
				} else {
					return "Failed saving shelf, error connection";
				}
			}			
			
			shelfWrapper = shelfService.saveOrUpdateShelfInProcess(username, shelf);
			if(shelfWrapper != null){
				if(!shelfWrapper.isSuccess()){
					return shelfWrapper.getError();
				}
			} else {
				return "Failed saving shelf, error connection";
			}
		} catch (Exception e) {
			return "Failed saving shelf, try again later. If error persist please contact administrator";
		}
		return "0";
	}
}
