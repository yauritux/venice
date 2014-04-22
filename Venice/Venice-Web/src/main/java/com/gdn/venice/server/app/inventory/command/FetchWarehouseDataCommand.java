/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Warehouse;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Maria Olivia
 */
public class FetchWarehouseDataCommand implements RafDsCommand {

    private RafDsRequest request;
    WarehouseManagementService warehouseService;

    public FetchWarehouseDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            warehouseService = new WarehouseManagementService();
            InventoryPagingWrapper<Warehouse> warehousesWrapper = warehouseService.getWarehouseData(request);
            if (warehousesWrapper != null) {
                if (warehousesWrapper.isSuccess()) {
                    //Put result
                    for (Warehouse warehouse : warehousesWrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_WAREHOUSE_ID, warehouse.getId().toString());
                        map.put(DataNameTokens.INV_WAREHOUSE_NAME, warehouse.getName());
                        map.put(DataNameTokens.INV_WAREHOUSE_CODE, warehouse.getCode());
                        map.put(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, warehouse.getDescription());
                        map.put(DataNameTokens.INV_WAREHOUSE_ADDRESS, warehouse.getAddress());
                        map.put(DataNameTokens.INV_WAREHOUSE_CITY, warehouse.getCity());
                        map.put(DataNameTokens.INV_WAREHOUSE_ZIPCODE, warehouse.getZipCode());
                        map.put(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON, warehouse.getContactPerson());
                        map.put(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE, warehouse.getContactPhone());
                        map.put(DataNameTokens.INV_WAREHOUSE_SPACE, warehouse.getSpace() == null ? "0.0" : warehouse.getSpace() + "");
                        map.put(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE, warehouse.getAvailableSpace() == null ? "0.0" : warehouse.getAvailableSpace() + "");
                        map.put(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS, warehouse.isActive() ? "Active" : "Non Active");
                        map.put(DataNameTokens.INV_WAREHOUSE_APPROVAL_IN_PROCESS, warehouse.isApprovalInProcess() ? "true" : "false");
                        dataList.add(map);
                    }

                    //Set DSResponse's properties
                    rafDsResponse.setStatus(0);
                    rafDsResponse.setStartRow(request.getStartRow());
                    rafDsResponse.setTotalRows(Integer.parseInt(warehousesWrapper.getTotalElements() + ""));
                    rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        //Set data and return
        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}