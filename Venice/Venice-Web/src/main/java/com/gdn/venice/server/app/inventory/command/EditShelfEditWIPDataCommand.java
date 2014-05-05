package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.entity.Storage;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

public class EditShelfEditWIPDataCommand implements RafRpcCommand {

	HashMap<String, String> shelfMap;
	HashMap<String, String> storageMap;
	String username, url;
	ShelfManagementService shelfService;

	public EditShelfEditWIPDataCommand(String username, String data) {
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
		System.out.println("EditShelfAddWIPDataCommand");
		ShelfWIP shelf = new ShelfWIP();
		List<Storage> storageList = new ArrayList<Storage>();
		ResultWrapper<ShelfWIP> shelfWrapper = new ResultWrapper<ShelfWIP>();		
		try {
			shelfService = new ShelfManagementService();
			shelf = new ShelfWIP();	
			shelf.setId(new Long(shelfMap.get(DataNameTokens.INV_SHELF_ID)));
			shelf.setDescription(shelfMap.get(DataNameTokens.INV_SHELF_DESCRIPTION));	
			
			System.out.println("save storage");		
			for(Map.Entry<String, String> entry : storageMap.entrySet()){
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println("storage key: "+key);
				System.out.println("storage value: "+value);
				
				Storage storage = new Storage();
				HashMap<String, String> map = InventoryUtil.convertToHashMap(value);
				for(Map.Entry<String, String> e : map.entrySet()){
					String k = e.getKey();
					String v = e.getValue();
					System.out.println("storage k: "+k);
					System.out.println("storage v: "+v);
					
					if(k.equals(DataNameTokens.INV_STORAGE_ID)){
						System.out.println("set id");
						storage.setId(new Long(v));
					}
					
					if(k.equals(DataNameTokens.INV_STORAGE_CODE)){
						System.out.println("set code");
						storage.setCode(v);
					}
					
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
			
			shelfWrapper = shelfService.editShelfEditWIP(username, shelf, storageList);
			if(shelfWrapper==null || !shelfWrapper.isSuccess()){
				return shelfWrapper.getError();
			}

		} catch (Exception e) {
			return "Failed saving edit shelf, try again later. If error persist please contact administrator";
		}
		return "0";
	}
}
