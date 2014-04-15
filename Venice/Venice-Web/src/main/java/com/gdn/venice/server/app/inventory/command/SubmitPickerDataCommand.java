package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

public class SubmitPickerDataCommand implements RafRpcCommand {
	String url, pickerId;
	PickingListManagementService pickingService;
	protected static Logger _log = null;
	List<String> packageIdList = new ArrayList<String>();

	public SubmitPickerDataCommand(String data, String pickerId) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.SubmitPickerDataCommand");
        
        this.pickerId = pickerId;
        System.out.println("packageIds: "+data);
        String[] packageId = data.split(";");        
        for(String id : packageId){
        	packageIdList.add(id);
        }
	}

	@Override
	public String execute() {
		System.out.println("SubmitPickerDataCommand");
		ResultWrapper<PickPackage> packageWrapper;
		try {
			pickingService = new PickingListManagementService();
									
	        System.out.println("packageIdList size: "+packageIdList.size());
			System.out.println("pickerId: "+pickerId);
	        packageWrapper = pickingService.submitPicker(packageIdList, pickerId);
			if(packageWrapper==null || !packageWrapper.isSuccess()){
				return packageWrapper.getError();
			}
		} catch (Exception e) {
			return "Failed assigning picker, try again later. If error persist please contact administrator";
		}
		
		return "0";
	}
}
