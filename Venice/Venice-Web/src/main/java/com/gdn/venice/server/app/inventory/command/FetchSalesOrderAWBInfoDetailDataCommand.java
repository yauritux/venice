/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.SalesOrderAWBInfo;
import com.gdn.inventory.wrapper.HeaderAndDetailWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PackingListService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Maria Olivia
 */
public class FetchSalesOrderAWBInfoDetailDataCommand implements RafDsCommand {

    private String username, awbInfoId;
    PackingListService packingService;

    public FetchSalesOrderAWBInfoDetailDataCommand(String awbInfoId, String username) {
        this.awbInfoId = awbInfoId;
        this.username = username;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            packingService = new PackingListService();
            HeaderAndDetailWrapper<String, SalesOrderAWBInfo> wrapper = packingService.getDetailPackingData(awbInfoId, username);
            if (wrapper != null) {
                if (wrapper.getSuccess()) {
                    //Put result
                    for (SalesOrderAWBInfo awbInfo : wrapper.getDetail()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_SO_ID, awbInfo.getSalesOrder().getId() + "");
                        map.put(DataNameTokens.INV_SO_ORDERID, awbInfo.getSalesOrder().getOrderId());
                        map.put(DataNameTokens.INV_SO_ORDERITEMID, awbInfo.getSalesOrder().getOrderItemId());
                        map.put(DataNameTokens.INV_SO_MERCHANTSKU, awbInfo.getSalesOrder().getMerchantSKU());
                        map.put(DataNameTokens.INV_SO_ITEMID, awbInfo.getSalesOrder().getAssignedItem().getId() + "");
                        map.put(DataNameTokens.INV_SO_ITEMDESC, awbInfo.getSalesOrder().getAssignedItem().getDescription());
                        map.put(DataNameTokens.INV_SO_QUANTITY, awbInfo.getSalesOrder().getQuantity() + "");
                        map.put(DataNameTokens.INV_SO_ITEMUOM, awbInfo.getSalesOrder().getAssignedItem().getItemUnit());
                        map.put(DataNameTokens.INV_SO_ITEMPHOTO, awbInfo.getSalesOrder().getAssignedItem().getImageUrl()==null
                                ?"http://www.blibli.com/wcsstore/Indraprastha/images/gdn/images/logo-blibli.png.pagespeed.ce.atR54FCIld.png"
                                :awbInfo.getSalesOrder().getAssignedItem().getImageUrl());
                        map.put(DataNameTokens.INV_SO_ITEMHASATTRIBUTE, awbInfo.getSalesOrder().getAssignedItem().isHasAttribute() + "");
                        map.put(DataNameTokens.INV_SO_ATTRIBUTE, "");
                        map.put(DataNameTokens.INV_AWB_CLAIMEDBY, wrapper.getHeader());
                        dataList.add(map);
                    }

                    //Set DSResponse's properties
                    rafDsResponse.setStatus(0);
                    rafDsResponse.setStartRow(0);
                    rafDsResponse.setTotalRows(Integer.parseInt(wrapper.getDetail().size() + ""));
                    rafDsResponse.setEndRow(0 + dataList.size());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        //Set data and return
        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
