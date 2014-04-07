package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.AttributeName;
import java.util.List;

import com.gdn.venice.server.app.inventory.service.PackingListService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 *
 * @author Maria Olivia
 */
public class FetchAttributeNameDataCommand implements RafRpcCommand {

    String itemId, username;

    public FetchAttributeNameDataCommand(String itemId, String username) {
        this.itemId = itemId;
        this.username = username;
    }

    @Override
    public String execute() {
        PackingListService packingListService = new PackingListService();
        StringBuilder sb = new StringBuilder();
        try {
            List<AttributeName> list = packingListService.getAttributeNameListByItemId(itemId, username);
            for (AttributeName attributeName : list) {
                if (!sb.toString().isEmpty()) {
                    sb.append(";");
                }
                sb.append(attributeName.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("attribute name: " + sb.toString());
        return sb.toString();
    }
}
