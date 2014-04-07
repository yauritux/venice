package com.gdn.venice.server.app.inventory.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gdn.inventory.exchange.entity.Attribute;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

/**
 *
 * @author Roland
 */
public class SaveGrnAttributeDataCommand implements RafRpcCommand {

    String asnItemId, username, attributes;
    GRNManagementService grnService;

    public SaveGrnAttributeDataCommand(String username, String asnItemId, String attributes) {
        this.attributes = attributes;
        this.username = username;
        this.asnItemId = asnItemId;
    }

    @Override
    public String execute() {
        Set<String> attribute = new HashSet<String>();
        ResultWrapper<List<Attribute>> wrapper;
        try {
        	grnService = new GRNManagementService();
            String[] attr = attributes.split(";");

            for (int i = 0; i < attr.length; i++) {
                attribute.add(attr[i]);
            }            
            System.out.println("start save attribute to cache");
            wrapper = grnService.saveAttributesToCache(username, asnItemId, attribute);
            System.out.println("done save attribute to cache");
            if (wrapper==null || !wrapper.isSuccess()) {                
                return wrapper.getError();
            }            	
        } catch (Exception e) {
            return "Failed saving attribute, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}