package com.gdn.venice.exportimport.inventory.dataimport;

import com.gdn.venice.hssf.PojoInterface;

public class PickingListSO implements PojoInterface {
	
	String packageId;
	String merchantCode;
	String merchantStore;
	String pickerName;
	String keterangan;	
	String salesOrderId;
	String warehouseSkuId;
	String itemName;
	String qty;
	String shelfCode;
	String storageCode;
	String qtyStorage;
	String qtyPicked;
	String containerId;
	
	public String getPackageId() {
		return packageId;
	}
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	public String getMerchantCode() {
		return merchantCode;
	}
	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}
	public String getMerchantStore() {
		return merchantStore;
	}
	public void setMerchantStore(String merchantStore) {
		this.merchantStore = merchantStore;
	}
	public String getPickerName() {
		return pickerName;
	}
	public void setPickerName(String pickerName) {
		this.pickerName = pickerName;
	}
	public String getKeterangan() {
		return keterangan;
	}
	public void setKeterangan(String keterangan) {
		this.keterangan = keterangan;
	}
	public String getSalesOrderId() {
		return salesOrderId;
	}
	public void setSalesOrderId(String salesOrderId) {
		this.salesOrderId = salesOrderId;
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
	public String getQtyPicked() {
		return qtyPicked;
	}
	public void setQtyPicked(String qtyPicked) {
		this.qtyPicked = qtyPicked;
	}
	public String getContainerId() {
		return containerId;
	}
	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}
}
