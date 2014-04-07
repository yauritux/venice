package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.entity.Storage;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

public class SaveOrUpdateShelfWIPDataCommand implements RafRpcCommand {

	HashMap<String, String> shelfMap;
	HashMap<String, String> storageMap;
	String username, url;
	ShelfManagementService shelfService;

	public SaveOrUpdateShelfWIPDataCommand(String username, String data) {
		String[] split = data.split("#");
		System.out.println("split 0: "+split[0]);
		System.out.println("split 1: "+split[1]);
		
		shelfMap = Util.formHashMapfromXML(split[0]);
		storageMap = Util.formHashMapfromXML(split[1]);
		System.out.println("shelfMap size: "+shelfMap.size());
		System.out.println("storageMap size: "+storageMap.size());
		
		this.username = username;
	}

	@Override
	public String execute() {
		ShelfWIP shelf;
		List<Storage> storageList = new ArrayList<Storage>();
		ResultWrapper<ShelfWIP> shelfWrapper;
		try {
			shelfService = new ShelfManagementService();
			System.out.println("Masuk ke command save shelf wip");
			if(shelfMap.get(DataNameTokens.INV_SHELF_ID) == null){
				System.out.println("set common values");
				shelf = new ShelfWIP();
						
				shelf.setCode("");
				shelf.setCreatedBy(username);
				shelf.setCreatedDate(new Date());
				shelf.setDeleted(false);
				shelf.setDiscriminator("shelfInProcess");
				shelf.setApprovalStatus(ApprovalStatus.CREATED);				
				shelf.setDescription(shelfMap.get(DataNameTokens.INV_SHELF_DESCRIPTION));

				if(shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID) == null) {
					System.out.println("add new shelf process");
					shelf.setApprovalType(ApprovalStatus.APPROVAL_CREATE);
					shelf.setActive(false);
				}else{
					if(shelfMap.get(DataNameTokens.INV_SHELF_ACTIVESTATUS).equalsIgnoreCase("Non Active")) {
						System.out.println("non-active process");
						shelf.setApprovalType(ApprovalStatus.APPROVAL_NON_ACTIVE);
						shelf.setOriginalShelf(Long.parseLong(shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
					}else{
						System.out.println("update process");
						shelf.setApprovalType(ApprovalStatus.APPROVAL_UPDATE);
						shelf.setOriginalShelf(Long.parseLong(shelfMap.get(DataNameTokens.INV_SHELF_ORIGINID)));
					}
				}
			}else{
				System.out.println("Masuk ke command update shelf wip");
				shelfWrapper = shelfService.findInProcessById(username, shelfMap.get(DataNameTokens.INV_SHELF_ID));
				
				if(shelfWrapper.isSuccess()){
					shelf = shelfWrapper.getContent();
					if(shelfMap.get(DataNameTokens.INV_SHELF_DESCRIPTION) != null) {
						System.out.println("update exsisting process");
						shelf.setDescription(shelfMap.get(DataNameTokens.INV_SHELF_DESCRIPTION));
						shelf.setApprovalStatus(ApprovalStatus.CREATED);
					}else{
						System.out.println(shelfMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS));
						shelf.setApprovalStatus(ApprovalStatus.valueOf(shelfMap.get(DataNameTokens.INV_SHELF_APPROVALSTATUS)));
					}						
				}else{
					return "Failed saving shelf, " + shelfWrapper.getError();
				}
			}
			
			System.out.println("save storage");		
			for(Map.Entry<String, String> entry : storageMap.entrySet()){
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println("storage key: "+key);
				System.out.println("storage value: "+value);
				
				Storage storage = new Storage();
				HashMap<String, String> map = convertToHashMap(value);
				for(Map.Entry<String, String> e : map.entrySet()){
					String k = e.getKey();
					String v = e.getValue();
					System.out.println("storage k: "+k);
					System.out.println("storage v: "+v);
					
					if(k.equals(DataNameTokens.INV_STORAGE_DESCRIPTION)){
						System.out.println("set description");
						storage.setDescription(v);
					}
					if(k.equals(DataNameTokens.INV_STORAGE_TYPE)){
						System.out.println("set type");
						storage.setType(v);
					}
				}	

				System.out.println("add list");
				storageList.add(storage);
			}
			
			System.out.println("storage size: "+storageList.size());
			
			shelfWrapper = shelfService.saveOrUpdateShelfInProcess(username, shelf, storageList);
			if(!shelfWrapper.isSuccess()){
				return shelfWrapper.getError();
			}

		} catch (Exception e) {
			return "Failed saving shelf, try again later. If error persist please contact administrator";
		}
		return "0";
	}
	
	private HashMap<String, String> convertToHashMap(String s) {
	    String[] arr = s.split(", ");
	    String str = null;
	    HashMap<String, String> map = new HashMap<String, String>();
	    for (int i=0;i<arr.length;i++) {
	    	str = arr[i].replace("{", "").replace("}", "");
	        System.out.println("str: "+str);
	        String[] splited = str.split("=");

	        map.put(splited[0], splited[1]);

	    }
	    
	    return map;
	}
}
