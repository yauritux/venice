package com.gdn.venice.server.app.inventory.command;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.entity.Warehouse;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.util.Util;
import java.util.HashMap;

/**
 * Fetch Command for warehouse combo box
 *
 * @author Roland
 */
public class FetchAllWarehouseComboBoxDataCommand implements RafRpcCommand {

    WarehouseManagementService warehouseService;

    /*
     * Edited by Maria Olivia 20140320
     */
    public String execute() {
        HashMap<String, String> map = new HashMap<String, String>(),
                param = new HashMap<String, String>();
        try {
            warehouseService = new WarehouseManagementService();
            RafDsRequest request = new RafDsRequest();
            param.put("username", "olive");
            param.put("page", "1");
            param.put("limit", "0");
            request.setParams(param);
            JPQLAdvancedQueryCriteria criteria = new JPQLAdvancedQueryCriteria("AND");
            JPQLSimpleQueryCriteria simpleCriteria = new JPQLSimpleQueryCriteria();
            simpleCriteria.setFieldName(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS);
            simpleCriteria.setValue("active");
            criteria.add(simpleCriteria);
            request.setCriteria(criteria);
            InventoryPagingWrapper<Warehouse> wrapper = warehouseService.getWarehouseData(request);
            if (wrapper != null && wrapper.isSuccess()) {
                for (Warehouse warehouse : wrapper.getContent()) {
                    map.put("data"+warehouse.getCode(), warehouse.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Util.formXMLfromHashMap(map);
    }
}
