package com.gdn.venice.server.app.administration.presenter.commands;

import java.util.HashMap;

import com.gdn.inventory.exchange.entity.Department;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.server.app.inventory.service.DepartmentManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;

/**
 * Fetch Command for Department
 * 
 * @author Roland
 */

public class FetchGroupComboBoxDataCommand implements RafRpcCommand{
	
	DepartmentManagementService service;
	
	public String execute() {
		HashMap<String, String> map = new HashMap<String, String>();
		try{
			
			service = new DepartmentManagementService();
            InventoryPagingWrapper<Department> deptWrapper = service.getAllDepartmentData(1, 20);
			
            if(deptWrapper != null){
                for(Department dept : deptWrapper.getContent()){
                    
                    map.put("data"+Util.isNull(dept.getCode(), "").toString(), Util.isNull(dept.getName(),"").toString());
                    
                }
            }
		}catch(Exception e){
			e.printStackTrace();
		}
		return Util.formXMLfromHashMap(map);
	}	
}
