package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PackingListService;
import com.gdn.venice.server.command.RafRpcCommand;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Maria Olivia
 */
public class SaveAttributeDataCommand implements RafRpcCommand {

    String salesOrderId, username, attributes;
    PackingListService packingService;

    public SaveAttributeDataCommand(String username, String salesOrderId, String attributes) {
        System.out.println(attributes);
        this.attributes = attributes;
        this.username = username;
        this.salesOrderId = salesOrderId;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        Set<String> attribute = new HashSet<String>();
        ResultWrapper<Map<String, String>> wrapper;
        try {
            packingService = new PackingListService();
            String[] attr = attributes.split(";");

            for (int i = 0; i < attr.length; i++) {
                attribute.add(attr[i]);
            }
            wrapper = packingService.saveAttributes(username, salesOrderId, attribute);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    StringBuilder sb = new StringBuilder();
                    for (String key : wrapper.getContent().keySet()) {
                        if (!sb.toString().isEmpty()) {
                            sb.append("\n");
                        }
                        sb.append(key + ", ERROR: " + wrapper.getContent().get(key));
                    }

                    if (!sb.toString().isEmpty()) {
                        return sb.toString();
                    }
                } else {
                    return wrapper.getError();
                }
            } else {
                return "Failed saving attribute, error connection";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Failed saving attribute, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}