package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 * @author Maria Olivia
 */
public class SaveOpnameAdjustmentDataCommand implements RafRpcCommand {

    String opnameId, username;
    OpnameService opnameService;

    public SaveOpnameAdjustmentDataCommand(String opnameId, String username) {
        this.opnameId = opnameId;
        this.username = username;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        ResultWrapper<String> wrapper;
        try {
            opnameService = new OpnameService();
            wrapper = opnameService.submitOpnameAdjustment(username, opnameId);
            if (wrapper != null) {
                if (!wrapper.isSuccess()) {
                    return wrapper.getError();
                }
            } else {
                return "Failed saving Opname, error connection";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed saving Opname, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}