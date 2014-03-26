package com.gdn.venice.exportimport.inventory.dataexport;

import java.util.List;

import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;

/**
 *
 * @author Roland
 */
public class PickingListPrint {
	private WarehouseItem warehouseItem;
    private List<WarehouseItemStorageStock> whItemStorageStock;
    
	public WarehouseItem getWarehouseItem() {
		return warehouseItem;
	}
	public void setWarehouseItem(WarehouseItem warehouseItem) {
		this.warehouseItem = warehouseItem;
	}
	public List<WarehouseItemStorageStock> getWhItemStorageStock() {
		return whItemStorageStock;
	}
	public void setWhItemStorageStock(
			List<WarehouseItemStorageStock> whItemStorageStock) {
		this.whItemStorageStock = whItemStorageStock;
	}

    
}
