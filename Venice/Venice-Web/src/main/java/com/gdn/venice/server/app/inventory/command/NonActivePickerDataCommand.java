package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.Picker;

import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PickerManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 *
 * @author Maria Olivia
 */
public class NonActivePickerDataCommand implements RafRpcCommand {

    // The map of all the parameters passed to the command
    String username, id;
    PickerManagementService pickerService;

    /**
     * Basic constructor with parameters passed in string
     */
    public NonActivePickerDataCommand(String username, String id) {
        this.username = username;
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        ResultWrapper<Picker> pickerWrapper;
        try {
            pickerService = new PickerManagementService();
            System.out.println("Masuk ke command save warehouse wip");
            pickerWrapper = pickerService.nonActivePicker(username, id);
            if (pickerWrapper != null) {
                if (!pickerWrapper.isSuccess()) {
                    return pickerWrapper.getError();
                }
            } else {
                return "Failed deleting warehouse, error connection";
            }
        } catch (Exception e) {
            return "Failed saving warehouse, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}
