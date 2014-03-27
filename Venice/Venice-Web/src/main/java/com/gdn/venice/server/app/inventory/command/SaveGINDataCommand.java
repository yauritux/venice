package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.Warehouse;
import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.exchange.entity.module.outbound.GoodIssuedNote;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.GINService;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Maria Olivia
 */
public class SaveGINDataCommand implements RafRpcCommand {

    String username;
    Map<String, String> dataMap;
    GINService ginService;
    WarehouseManagementService warehouseService;

    public SaveGINDataCommand(String username, String data) {
        dataMap = Util.formHashMapfromXML(data);
        this.username = username;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        ResultWrapper<GoodIssuedNote> wrapper;
        try {
            ginService = new GINService();
            warehouseService = new WarehouseManagementService();
            ResultWrapper<Warehouse> whWrapper = warehouseService.findByCode(dataMap.get(DataNameTokens.INV_GIN_WAREHOUSECODE));
            if (whWrapper != null && whWrapper.isSuccess() && whWrapper.getContent() != null) {
                GoodIssuedNote gin = new GoodIssuedNote();
                gin.setCreatedDate(new Date());
                gin.setGinNumber(dataMap.get(DataNameTokens.INV_GIN_NO));
                gin.setLogistic(dataMap.get(DataNameTokens.INV_GIN_LOGISTIC));
                gin.setSpecialNote(dataMap.get(DataNameTokens.INV_GIN_NOTE));
                gin.setWarehouse(whWrapper.getContent());
                wrapper = ginService.saveGIN(username, gin, dataMap.get(DataNameTokens.INV_GIN_AWB_NO));
                if (wrapper != null) {
                    if (!wrapper.isSuccess()) {
                        return wrapper.getError();
                    }
                } else {
                    return "Failed saving GIN, error connection";
                }
            } else {
                return "Failed saving GIN, try again later. If error persist please contact administrator";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed saving GIN, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}