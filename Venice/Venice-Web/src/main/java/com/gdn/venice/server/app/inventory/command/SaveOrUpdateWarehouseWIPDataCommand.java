package com.gdn.venice.server.app.inventory.command;

import java.util.Date;
import java.util.HashMap;

import com.gdn.inventory.exchange.entity.WarehouseWIP;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 *
 * @author Maria Olivia
 */
public class SaveOrUpdateWarehouseWIPDataCommand implements RafRpcCommand {

    // The map of all the parameters passed to the command
    HashMap<String, String> dataMap;
    String username, url;
    WarehouseManagementService warehouseService;

    /**
     * Basic constructor with parameters passed in XML string
     *
     * @param parameter a list of the parameters for the form in XML
     */
    public SaveOrUpdateWarehouseWIPDataCommand(String username, String data) {
        System.out.println("SaveOrUpdateWarehouseWIPDataCommand");
        dataMap = Util.formHashMapfromXML(data);
        System.out.println(dataMap.toString());
        this.username = username;
    }

    /* (non-Javadoc)
     * @see com.gdn.venice.server.command.RafRpcCommand#execute()
     */
    @Override
    public String execute() {
        WarehouseWIP warehouse;
        ResultWrapper<WarehouseWIP> warehouseWrapper;
        try {
            warehouseService = new WarehouseManagementService();
            System.out.println("Masuk ke command save warehouse wip");
            if (dataMap.get(DataNameTokens.INV_WAREHOUSE_ID) == null) {
                System.out.println("set common values");
                warehouse = new WarehouseWIP();
                warehouse.setCode(dataMap.get(DataNameTokens.INV_WAREHOUSE_CODE));
                warehouse.setCreatedBy(username);
                warehouse.setCreatedDate(new Date());
                warehouse.setDeleted(false);
                warehouse.setDiscriminator("warehouseInProcess");
                warehouse.setApprovalStatus(ApprovalStatus.CREATED);

                warehouse.setAddress(dataMap.get(DataNameTokens.INV_WAREHOUSE_ADDRESS));
                warehouse.setCity(dataMap.get(DataNameTokens.INV_WAREHOUSE_CITY));
                warehouse.setDescription(dataMap.get(DataNameTokens.INV_WAREHOUSE_DESCRIPTION));
                warehouse.setName(dataMap.get(DataNameTokens.INV_WAREHOUSE_NAME));
                warehouse.setZipCode(dataMap.get(DataNameTokens.INV_WAREHOUSE_ZIPCODE));
                warehouse.setContactPerson(dataMap.get(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON));
                warehouse.setContactPhone(dataMap.get(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE));
                warehouse.setSpace(Double.parseDouble(dataMap.get(DataNameTokens.INV_WAREHOUSE_SPACE)));
                warehouse.setAvailableSpace(Double.parseDouble(dataMap.get(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE)));

                if (dataMap.get(DataNameTokens.INV_WAREHOUSE_ORIGINALID) == null) {
                    System.out.println("add new warehouse process");
                    warehouse.setApprovalType(ApprovalStatus.APPROVAL_CREATE);
                    warehouse.setActive(false);
                } else {
                    if (dataMap.get(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS).equalsIgnoreCase("Non Active")) {
                        System.out.println("non-active process");
                        warehouse.setApprovalType(ApprovalStatus.APPROVAL_NON_ACTIVE);
                        warehouse.setOriginalWarehouse(Long.parseLong(dataMap.get(DataNameTokens.INV_WAREHOUSE_ORIGINALID)));
                    } else {
                        System.out.println("update process");
                        warehouse.setApprovalType(ApprovalStatus.APPROVAL_UPDATE);
                        warehouse.setOriginalWarehouse(Long.parseLong(dataMap.get(DataNameTokens.INV_WAREHOUSE_ORIGINALID)));
                    }
                }
            } else {
                System.out.println("Masuk ke command update warehouse wip");
                warehouseWrapper = warehouseService.findInProcessById(username, dataMap.get(DataNameTokens.INV_WAREHOUSE_ID));

                if (warehouseWrapper != null) {
                    if (warehouseWrapper.isSuccess()) {
                        warehouse = warehouseWrapper.getContent();
                        if (dataMap.get(DataNameTokens.INV_WAREHOUSE_NAME) != null) {
                            System.out.println("update exsisting process");
                            warehouse.setAddress(dataMap.get(DataNameTokens.INV_WAREHOUSE_ADDRESS));
                            warehouse.setCity(dataMap.get(DataNameTokens.INV_WAREHOUSE_CITY));
                            warehouse.setCode(dataMap.get(DataNameTokens.INV_WAREHOUSE_CODE));
                            warehouse.setDescription(dataMap.get(DataNameTokens.INV_WAREHOUSE_DESCRIPTION));
                            warehouse.setName(dataMap.get(DataNameTokens.INV_WAREHOUSE_NAME));
                            warehouse.setZipCode(dataMap.get(DataNameTokens.INV_WAREHOUSE_ZIPCODE));
                            warehouse.setContactPerson(dataMap.get(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON));
                            warehouse.setContactPhone(dataMap.get(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE));
                            warehouse.setSpace(Double.parseDouble(dataMap.get(DataNameTokens.INV_WAREHOUSE_SPACE)));
                            warehouse.setAvailableSpace(Double.parseDouble(dataMap.get(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE)));
                            warehouse.setApprovalStatus(ApprovalStatus.CREATED);
                        } else {
                            System.out.println(dataMap.get(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS));
                            warehouse.setApprovalStatus(ApprovalStatus.valueOf(dataMap.get(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS)));
                        }
                    } else {
                        return "Failed saving warehouse, " + warehouseWrapper.getError();
                    }
                } else {
                    return "Failed saving warehouse, error connection";
                }
            }

            warehouseWrapper = warehouseService.saveOrUpdateWarehouseInProcess(username, warehouse);
            if (warehouseWrapper != null) {
                if (!warehouseWrapper.isSuccess()) {
                    return warehouseWrapper.getError();
                }
            } else {
                return "Failed saving warehouse, error connection";
            }
        } catch (Exception e) {
            return "Failed saving warehouse, try again later. If error persist please contact administrator";
        }
        return "0";
    }
}
