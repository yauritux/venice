package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.inbound.Putaway;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.command.RafRpcCommand;

public class SavePutawayDataCommand implements RafRpcCommand {

	List<String> grnNumberList = new ArrayList<String>();
	String username, url;
	PutawayManagementService putawayService;
	protected static Logger _log = null;

	public SavePutawayDataCommand(String username, String data) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.SavePutawayDataCommand");
		this.username = username;
		
        String[] grnNumbersArray = data.split(";");        
        for(int i=0;i<grnNumbersArray.length;i++){
        	grnNumberList.add(grnNumbersArray[i]);
        }
        System.out.println("grnNumberList size: "+grnNumberList.size());
	}

	@Override
	public String execute() {
		System.out.println("SavePutawayDataCommand");
		ResultWrapper<Putaway> putawayWrapper;
		try {
			putawayService = new PutawayManagementService();
																		
			putawayWrapper = putawayService.savePutawayGRN(username, grnNumberList);
			if(putawayWrapper==null || !putawayWrapper.isSuccess()){
				return putawayWrapper.getError();
			}
		} catch (Exception e) {
			return "Failed saving putaway.";
		}
		
		return "0";
	}
}
