package com.gdn.venice.exportimport.inventory.dataexport;

import java.util.List;

import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.InventoryRequest;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.exchange.entity.module.outbound.SalesOrder;

/**
 *
 * @author Roland
 */
public class PickingListPrint {
	private InventoryRequest inventoryRequest;
	private SalesOrder salesOrder;
	private PickPackage pickPackage;
	private String warehouseSkuId;
	private String itemName;
	private String qty;
	private String shelfCode;
	private String storageCode;
	private String qtyStorage;	
	private WarehouseItem warehouseItem;
    private List<WarehouseItemStorageStock> whItemStorageStock;
    
	public SalesOrder getSalesOrder() {
		return salesOrder;
	}
	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}
	public InventoryRequest getInventoryRequest() {
		return inventoryRequest;
	}
	public void setInventoryRequest(InventoryRequest inventoryRequest) {
		this.inventoryRequest = inventoryRequest;
	}
	public PickPackage getPickPackage() {
		return pickPackage;
	}
	public void setPickPackage(PickPackage pickPackage) {
		this.pickPackage = pickPackage;
	}
	public WarehouseItem getWarehouseItem() {
		return warehouseItem;
	}
	public void setWarehouseItem(WarehouseItem warehouseItem) {
		this.warehouseItem = warehouseItem;
	}
	public List<WarehouseItemStorageStock> getWhItemStorageStock() {
		return whItemStorageStock;
	}
	public void setWhItemStorageStock(List<WarehouseItemStorageStock> whItemStorageStock) {
		this.whItemStorageStock = whItemStorageStock;
	}
	public String getWarehouseSkuId() {
		return warehouseSkuId;
	}
	public void setWarehouseSkuId(String warehouseSkuId) {
		this.warehouseSkuId = warehouseSkuId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getQty() {
		return qty;
	}
	public void setQty(String qty) {
		this.qty = qty;
	}
	public String getShelfCode() {
		return shelfCode;
	}
	public void setShelfCode(String shelfCode) {
		this.shelfCode = shelfCode;
	}
	public String getStorageCode() {
		return storageCode;
	}
	public void setStorageCode(String storageCode) {
		this.storageCode = storageCode;
	}
	public String getQtyStorage() {
		return qtyStorage;
	}
	public void setQtyStorage(String qtyStorage) {
		this.qtyStorage = qtyStorage;
	}
}
