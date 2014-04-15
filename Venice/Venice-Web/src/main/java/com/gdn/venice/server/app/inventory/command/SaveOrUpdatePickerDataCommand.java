package com.gdn.venice.server.app.inventory.command;

import java.util.Date;
import java.util.HashMap;

import com.gdn.inventory.exchange.entity.Picker;
import com.gdn.inventory.exchange.entity.Warehouse;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.server.app.inventory.service.PickerManagementService;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 *
 * @author Maria Olivia
 */
public class SaveOrUpdatePickerDataCommand implements RafRpcCommand {

    // The map of all the parameters passed to the command
    HashMap<String, String> dataMap;
    String username, url;
    PickerManagementService pickerService;
    WarehouseManagementService warehouseService;

    /**
     * Basic constructor with parameters passed in XML string
     *
     * @param parameter a list of the parameters for the form in XML
     */
    public SaveOrUpdatePickerDataCommand(String username, String data) {
        this.dataMap = Util.formHashMapfromXML(data);
        this.username = username;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        Picker picker;
        ResultWrapper<Picker> pickerWrapper;
        try {
            pickerService = new PickerManagementService();
            warehouseService = new WarehouseManagementService();
            ResultWrapper<Warehouse> whWrapper = warehouseService.findByCode(dataMap.get(DataNameTokens.INV_PICKER_WAREHOUSECODE));
            if (whWrapper != null && whWrapper.isSuccess() && whWrapper.getContent() != null) {
                picker = new Picker();
                picker.setName(dataMap.get(DataNameTokens.INV_PICKER_NAME));
                picker.setWarehouse(whWrapper.getContent());
                picker.setActive(true);
                picker.setDeleted(false);
                if (dataMap.get(DataNameTokens.INV_PICKER_ID) == null) {
                    System.out.println("Create");
                    picker.setCreatedBy(username);
                    picker.setCreatedDate(new Date());
                } else {
                    System.out.println("Edit");
                    picker.setId(Long.parseLong(dataMap.get(DataNameTokens.INV_PICKER_ID)));
                    picker.setCode(dataMap.get(DataNameTokens.INV_PICKER_CODE));
                    picker.setUpdatedBy(username);
                    picker.setUpdatedDate(new Date());
                }

                pickerWrapper = pickerService.saveOrUpdatePicker(username, picker);
                if (pickerWrapper != null) {
                    if (!pickerWrapper.isSuccess()) {
                        return pickerWrapper.getError();
                    }
                } else {
                    return "Failed saving picker, error connection";
                }
            } else {
                return "Failed saving picker, warehouse not found";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed saving picker, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}
