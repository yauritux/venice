package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PackingListService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 *
 * @author Maria Olivia
 */
public class SavePackingDataCommand implements RafRpcCommand {

    String pickPackageId, username, awbNumber;
    PackingListService packingService;

    public SavePackingDataCommand(String username, String pickPackageId, String awbNumber) {
        this.username = username;
        this.pickPackageId = pickPackageId;
        this.awbNumber = awbNumber;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        ResultWrapper<AWBInfo> wrapper;
        try {
            packingService = new PackingListService();

            wrapper = packingService.savePacking(username, pickPackageId, awbNumber);
            if (wrapper != null) {
                if (!wrapper.isSuccess()) {
                    return wrapper.getError();
                }
            } else {
                return "Failed saving packing data, error connection";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed saving packing data, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}