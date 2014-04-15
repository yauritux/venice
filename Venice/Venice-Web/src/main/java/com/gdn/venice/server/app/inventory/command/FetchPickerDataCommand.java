/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Picker;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PickerManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Maria Olivia
 */
public class FetchPickerDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickerManagementService pickerService;

    public FetchPickerDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            pickerService = new PickerManagementService();
            InventoryPagingWrapper<Picker> pickersWrapper = pickerService.getPickerData(request);
            if (pickersWrapper != null) {
                if (pickersWrapper.isSuccess()) {
                    //Put result
                    System.out.println(pickersWrapper.getContent().size());
                    for (Picker picker : pickersWrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_PICKER_ID, picker.getId() + "");
                        map.put(DataNameTokens.INV_PICKER_CODE, picker.getCode());
                        map.put(DataNameTokens.INV_PICKER_NAME, picker.getName());
                        map.put(DataNameTokens.INV_PICKER_WAREHOUSECODE, picker.getWarehouse().getCode());
                        map.put(DataNameTokens.INV_PICKER_WAREHOUSENAME, picker.getWarehouse().getName());
                        map.put(DataNameTokens.INV_PICKER_STATUS, picker.isActive() ? "Active" : "Non Active");
                        dataList.add(map);
                    }

                    //Set DSResponse's properties
                    rafDsResponse.setStatus(0);
                    rafDsResponse.setStartRow(request.getStartRow());
                    rafDsResponse.setTotalRows(Integer.parseInt(pickersWrapper.getTotalElements() + ""));
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
