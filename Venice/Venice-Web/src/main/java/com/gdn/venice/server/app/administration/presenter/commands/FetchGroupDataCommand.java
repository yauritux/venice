package com.gdn.venice.server.app.administration.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Department;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.DepartmentManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Fetch Command for Department Maintenance
 * 
 * @author Anto
 */

public class FetchGroupDataCommand implements RafDsCommand {

	RafDsRequest request;
	DepartmentManagementService service;
	
	public FetchGroupDataCommand(RafDsRequest request){
		this.request=request;
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		
		try{
			service = new DepartmentManagementService();
            InventoryPagingWrapper<Department> deptWrapper = service.getAllDepartmentData(1, 20);
            
            for(Department dept : deptWrapper.getContent()){
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(DataNameTokens.RAFGROUP_GROUPID, dept.getId().toString());
                map.put(DataNameTokens.RAFGROUP_GROUPNAME, dept.getCode());
                map.put(DataNameTokens.RAFGROUP_GROUPDESC, dept.getName());
                
                dataList.add(map);
            }

			rafDsResponse.setStatus(0);
			rafDsResponse.setStartRow(request.getStartRow());
			rafDsResponse.setTotalRows(dataList.size());
			rafDsResponse.setEndRow(request.getStartRow()+dataList.size());
		}catch(Exception e){
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
