package com.gdn.venice.server.app.administration.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.RafRoleSessionEJBRemote;
import com.gdn.venice.persistence.RafRole;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Fetch Command for Role Detail
 * 
 * @author Roland
 */

public class FetchRoleDetailDataCommand implements RafDsCommand {

	RafDsRequest request;
	
	public FetchRoleDetailDataCommand(RafDsRequest request){
		this.request=request;
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		Locator<Object> locator=null;
		try{
			locator = new Locator<Object>();			
			RafRoleSessionEJBRemote sessionHome = (RafRoleSessionEJBRemote) locator.lookup(RafRoleSessionEJBRemote.class, "RafRoleSessionEJBBean");			
			List<RafRole> rafRole = null;		
			
			String query = "select o from RafRole o where o.roleId="+request.getParams().get(DataNameTokens.RAFROLE_ROLEID);
			rafRole = sessionHome.queryByRange(query, 0, 1);
			
			
			for(int i=0; i<rafRole.size();i++){				
				HashMap<String, String> map = null;				
				RafRole list = rafRole.get(i);				
				
				map=new HashMap<String, String>();
				map.put(DataNameTokens.RAFROLE_ROLEID, Util.isNull(list.getRoleId(), "").toString());
				map.put(DataNameTokens.RAFROLE_ROLENAME, Util.isNull(list.getRoleName(), "").toString());
				map.put(DataNameTokens.RAFROLE_ROLEDESC, Util.isNull(list.getRoleDesc(), "").toString());
				map.put(DataNameTokens.RAFROLE_ADDTOSTOCKHOLM, Util.isNull(list.getAddToStockholm(), "").toString());			

				dataList.add(map);				
			}
			rafDsResponse.setStatus(0);
			rafDsResponse.setStartRow(request.getStartRow());
			rafDsResponse.setTotalRows(dataList.size());
			rafDsResponse.setEndRow(request.getStartRow()+dataList.size());
		}catch(Exception e){
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}finally{
			try{
				if(locator!=null){
					locator.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}
