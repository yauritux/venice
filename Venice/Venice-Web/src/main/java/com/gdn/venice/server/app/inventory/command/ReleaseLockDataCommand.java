package com.gdn.venice.server.app.inventory.command;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

public class ReleaseLockDataCommand implements RafRpcCommand {

	String username, warehouseId;
	PickingListManagementService pickingListService;
	protected static Logger _log = null;

	public ReleaseLockDataCommand(String username, String warehouseId) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.ReleaseLockDataCommand");
		
		this.username = username;
		this.warehouseId=warehouseId;
	}

	@Override
	public String execute() {
		_log.info("ReleaseLockDataCommand");
		ResultWrapper<PickingListDetail> plWrapper;
		try {
			pickingListService = new PickingListManagementService();
			
			plWrapper = pickingListService.releasePickingLock(username, warehouseId);
			
			if(plWrapper != null){
				if(!plWrapper.isSuccess()){
					return plWrapper.getError();
				}
			} else {
				return "Failed release lock, error connection";
			}
		} catch (Exception e) {
			return "Failed release lock, try again later. If error persist please contact administrator";
		}
		
		return "0";
	}
}
