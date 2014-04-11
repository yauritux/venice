package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchShelfInProcessDataCommand implements RafDsCommand {

	private RafDsRequest request;
	ShelfManagementService shelfService;

	public FetchShelfInProcessDataCommand(RafDsRequest request) {
		this.request = request;
	}

	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

		try {
			shelfService = new ShelfManagementService();
			InventoryPagingWrapper<ShelfWIP> shelfsWrapper = shelfService.getShelfInProcessData(request);
			if(shelfsWrapper!=null && shelfsWrapper.isSuccess()){
				for(ShelfWIP shelf:shelfsWrapper.getContent()){
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(DataNameTokens.INV_SHELF_ID, shelf.getId().toString());
					map.put(DataNameTokens.INV_SHELF_CODE, shelf.getCode());
					map.put(DataNameTokens.INV_SHELF_DESCRIPTION, shelf.getDescription());
					map.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, shelf.getApprovalStatus() == ApprovalStatus.CREATED
							? "New":shelf.getApprovalStatus() == ApprovalStatus.APPROVED
							? "Approved":shelf.getApprovalStatus() == ApprovalStatus.NEED_CORRECTION
							? "Need Correction":shelf.getApprovalStatus() == ApprovalStatus.REJECTED
							? "Rejected":"");
					dataList.add(map);
					
					System.out.println("code: "+shelf.getCode());
				}

				rafDsResponse.setStatus(0);
				rafDsResponse.setStartRow(request.getStartRow());
				rafDsResponse.setTotalRows((int) shelfsWrapper.getTotalElements());
				rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}

		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
