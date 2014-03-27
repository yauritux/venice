package com.gdn.venice.server.app.fraud.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FrdParameterRule35SessionEJBRemote;
import com.gdn.venice.persistence.FrdParameterRule35;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Remove Command for Parameter Rule 35 - Customer Grey List
 * 
 * @author
 */

public class DeleteFraudParameterRule35DataCommand implements RafDsCommand {
	RafDsRequest request;
	
	public DeleteFraudParameterRule35DataCommand(RafDsRequest request) {
		this.request = request;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<FrdParameterRule35> list = new ArrayList<FrdParameterRule35>();		
		List<HashMap<String,String >> dataList = request.getData();		
		FrdParameterRule35 entityRule35 = new FrdParameterRule35();
		
		for (int i=0;i<dataList.size();i++) {
			Map<String, String> data = dataList.get(i);
			Iterator<String> iter = data.keySet().iterator();

			while (iter.hasNext()) {
				String key = iter.next();
				if(key.equals(DataNameTokens.FRDPARAMETERRULE35_ID)){
					entityRule35.setId(new Long(data.get(key)));
				} 
			}						
			list.add(entityRule35);			
		}
				
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			FrdParameterRule35SessionEJBRemote sessionHome = (FrdParameterRule35SessionEJBRemote) locator.lookup(FrdParameterRule35SessionEJBRemote.class, "FrdParameterRule35SessionEJBBean");
			sessionHome.removeFrdParameterRule35List((ArrayList<FrdParameterRule35>)list);
			
			rafDsResponse.setStatus(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			rafDsResponse.setStatus(-1);
		} finally {
			try {
				if(locator!=null){
					locator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
