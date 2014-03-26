package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.exchange.type.AWBInfoStatus;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.GINService;
import com.gdn.venice.server.app.inventory.service.PackingListService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 *
 * @author Maria Olivia
 */
public class CheckAwbNumberCommand implements RafRpcCommand {

    String awbNumber, logistic, warehouseCode;
    GINService ginService;

    public CheckAwbNumberCommand(String awbNumber, String logistic, String warehouseCode) {
        this.awbNumber = awbNumber;
        this.logistic = logistic;
        this.warehouseCode = warehouseCode;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        ResultWrapper<AWBInfo> wrapper;
        try {
            ginService = new GINService();

            wrapper = ginService.getAwbDetail(awbNumber);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    AWBInfo awb = wrapper.getContent();
                    if (awb != null && awb.getStatus() == AWBInfoStatus.COMPLETED) {
                        if (!awb.getLogisticCode().equalsIgnoreCase(logistic)) {
                            return "Attended for other logistic";
                        } else {
                            if (!awb.getWarehouse().getCode().equalsIgnoreCase(warehouseCode)) {
                                return "Attended for other warehouse";
                            }
                        }
                    } else {
                        return "AWB number not found";
                    }
                } else {
                    return wrapper.getError();
                }
            } else {
                return "Failed checking AWB No, error connection";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed saving attribute, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}