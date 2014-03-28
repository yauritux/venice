package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PackingListService;
import com.gdn.venice.server.command.RafRpcCommand;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Maria Olivia
 */
public class RejectPackingDataCommand implements RafRpcCommand {

    String salesOrderId, username;
    PackingListService packingService;

    public RejectPackingDataCommand(String username, String salesOrderId) {
        this.username = username;
        this.salesOrderId = salesOrderId;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        ResultWrapper<AWBInfo> wrapper;
        try {
            packingService = new PackingListService();
            wrapper = packingService.rejectPacking(username, salesOrderId);
            if (wrapper != null) {
                if (!wrapper.isSuccess()) {
                    return wrapper.getError();
                }
            } else {
                return "Failed rejecting packing, error connection";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed rejecting packing, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}