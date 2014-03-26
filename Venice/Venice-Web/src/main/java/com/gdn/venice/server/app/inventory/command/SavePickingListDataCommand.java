package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Item;
import com.gdn.inventory.exchange.entity.Shelf;
import com.gdn.inventory.exchange.entity.Storage;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.PickingList;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.exchange.entity.module.outbound.SalesOrder;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

public class SavePickingListDataCommand implements RafRpcCommand {

	HashMap<String, String> itemMap;
	HashMap<String, String> salesMap;
	HashMap<String, String> storageMap;
	int totalQtyPicked;
	String username, url;
	PickingListManagementService pickingListService;
	protected static Logger _log = null;

	public SavePickingListDataCommand(String username, String data) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.SavePickingListDataCommand");
		
		String[] split = data.split("#");
		System.out.println("split 0: "+split[0]);
		System.out.println("split 1: "+split[1]);
		System.out.println("split 2: "+split[2]);
		System.out.println("split 3: "+split[3]);
		
		itemMap = Util.formHashMapfromXML(split[0]);
		salesMap = Util.formHashMapfromXML(split[1]);
		storageMap = Util.formHashMapfromXML(split[2]);
		totalQtyPicked = Integer.parseInt(split[3]);
		this.username = username;
	}

	@Override
	public String execute() {
		System.out.println("SavePickingListDataCommand");
		PickingListDetail pld= new PickingListDetail();

		
		ResultWrapper<PickingListDetail> pldWrapper;
		try {
			pickingListService = new PickingListManagementService();

			List<SalesOrder> salesList = new ArrayList<SalesOrder>();
			List<WarehouseItemStorageStock> storageList = new ArrayList<WarehouseItemStorageStock>();

			PickingList pl = new PickingList();	
			pl.setQuantity(totalQtyPicked);
			pld.setQtyPicked(totalQtyPicked);
			
			Item item = new Item();
			WarehouseItem whi = new WarehouseItem();
			
			for(Map.Entry<String, String> entry : itemMap.entrySet()){
				HashMap<String, String> map = InventoryUtil.convertToHashMap(entry.getValue());
				for(Map.Entry<String, String> itemMapEntry : map.entrySet()){
					String key = itemMapEntry.getKey();
					String value = itemMapEntry.getValue();
											
					if(key.equals(DataNameTokens.INV_PICKINGLIST_ITEMID)){	
						item.setId(new Long(value));
						whi.setItem(item);
						System.out.println("item id: "+value);
					}					
					if(key.equals(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID)){	
						whi.setId(new Long(value));
						pl.setWarehouseItem(whi);
						System.out.println("warehouse item id: "+value);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME)){
						item.setCode(value);
						System.out.println("item sku: "+value);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_DIMENSION)){
						System.out.println("dimension: "+value);
						String[] split = value.split("x");
						item.setWidth(Float.parseFloat(split[0]));
						item.setHeight(Float.parseFloat(split[1]));
						item.setLength(Float.parseFloat(split[2]));
						System.out.println("width: "+split[0]);
						System.out.println("height: "+split[1]);
						System.out.println("length: "+split[2]);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_WEIGHT)){
						item.setWeight(Float.parseFloat(value));
						System.out.println("weight: "+value);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_UOM)){
						item.setItemUnit(value);
						System.out.println("item unit: "+value);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_ATTRIBUTE)){
						item.setHasAttribute(Boolean.parseBoolean(value));
						System.out.println("has attribute: "+value);
					}
				}
			}
			
			for(Map.Entry<String, String> entry : salesMap.entrySet()){
				HashMap<String, String> map = InventoryUtil.convertToHashMap(entry.getValue());
				SalesOrder so = new SalesOrder();
				
				for(Map.Entry<String, String> salesMapEntry : map.entrySet()){
					String key = salesMapEntry.getKey();
					String value = salesMapEntry.getValue();					

					if(key.equals(DataNameTokens.INV_PICKINGLIST_SALESORDERID)){	
						so.setId(new Long(value));
						System.out.println("sales order id: "+value);
					}
					
					if(key.equals(DataNameTokens.INV_PICKINGLIST_SALESORDERNUMBER)){	
						so.setSalesOrderNumber(value);
						System.out.println("sales order number: "+value);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_SALESORDERQTY)){
						so.setQuantity(Integer.parseInt(value));
						pld.setQtySales(Integer.parseInt(value));
						System.out.println("quantity: "+value);
					}	
					if(key.equals(DataNameTokens.INV_PICKINGLIST_SALESORDERTIPEPENANGANAN)){
						so.setTipePenanganan(value);
						System.out.println("penanganan: "+value);
					}
				}
				salesList.add(so);
			}
			System.out.println("sales list size: "+salesList.size());
			
			for(Map.Entry<String, String> entry : storageMap.entrySet()){
				HashMap<String, String> map = InventoryUtil.convertToHashMap(entry.getValue());
				WarehouseItemStorageStock wiss = new WarehouseItemStorageStock();
				for(Map.Entry<String, String> storageMapEntry : map.entrySet()){
					String key = storageMapEntry.getKey();
					String value = storageMapEntry.getValue();

					if(key.equals(DataNameTokens.INV_PICKINGLIST_WAREHOUSESTORAGEID)){	
						wiss.setId(new Long(value));
						System.out.println("storage id: "+value);
					}					
					if(key.equals(DataNameTokens.INV_PICKINGLIST_SHELFCODE)){	
						Shelf sh = new Shelf();
						sh.setCode(value);

						Storage st = new Storage();
						st.setShelf(sh);
						
						wiss.setStorage(st);
						System.out.println("shelf number: "+value);
					}
					if(key.equals(DataNameTokens.INV_PICKINGLIST_QTY)){
						wiss.setQuantity(Integer.parseInt(value));
						System.out.println("quantity: "+value);
					}	
				}
				storageList.add(wiss);
			}
			System.out.println("storage list size: "+storageList.size());
			
			pld.setPickingList(pl);
			pld.setItem(item);
			pld.setSalesOrder(salesList);
			pld.setWhItemStorageStock(storageList);	
			
			pldWrapper = pickingListService.submitPickingList(username, pld);
			
			if(pldWrapper != null){
				if(!pldWrapper.isSuccess()){
					return pldWrapper.getError();
				}
			} else {
				return "Failed saving list, error connection";
			}
		} catch (Exception e) {
			return "Failed saving list, try again later. If error persist please contact administrator";
		}
		
		return "0";
	}
}
