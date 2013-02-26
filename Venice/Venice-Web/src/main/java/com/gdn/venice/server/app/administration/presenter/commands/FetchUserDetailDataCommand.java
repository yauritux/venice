package com.gdn.venice.server.app.administration.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.RafUserSessionEJBRemote;
import com.gdn.venice.persistence.RafUser;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Fetch Command for User Detail
 * 
 * @author Roland
 */

public class FetchUserDetailDataCommand implements RafDsCommand {

	RafDsRequest request;
	
	public FetchUserDetailDataCommand(RafDsRequest request){
		this.request=request;
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		Locator<Object> locator=null;
		try{
			locator = new Locator<Object>();			
			RafUserSessionEJBRemote sessionHome = (RafUserSessionEJBRemote) locator.lookup(RafUserSessionEJBRemote.class, "RafUserSessionEJBBean");			
			List<RafUser> rafUser = null;
			
			System.out.println("user id di command: "+request.getParams().get(DataNameTokens.RAFUSER_USERID));
			
			String query = "select o from RafUser o where o.userId="+request.getParams().get(DataNameTokens.RAFUSER_USERID);
			rafUser = sessionHome.queryByRange(query, 0, 1);
			
			System.out.println("query user size: "+rafUser.size());
			
			for(int i=0; i<rafUser.size();i++){				
				HashMap<String, String> map = null;				
				RafUser list = rafUser.get(i);				
				
				map=new HashMap<String, String>();
				map.put(DataNameTokens.RAFUSER_USERID, Util.isNull(list.getUserId(), "").toString());
				map.put(DataNameTokens.RAFUSER_LOGINNAME, Util.isNull(list.getLoginName(), "").toString());
				map.put(DataNameTokens.RAFUSER_NAME, Util.isNull(list.getName(), "").toString());
				map.put(DataNameTokens.RAFUSER_ADDTOSTOCKHOLM, Util.isNull(list.getAddToStockholm(), "").toString());
				map.put(DataNameTokens.RAFUSER_DEPARTMENT, Util.isNull(list.getDepartment(), "").toString());		

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
