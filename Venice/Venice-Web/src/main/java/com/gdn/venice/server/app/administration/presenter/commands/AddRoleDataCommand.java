package com.gdn.venice.server.app.administration.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Role;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.RafRoleSessionEJBRemote;
import com.gdn.venice.persistence.RafRole;
import com.gdn.venice.persistence.RafUser;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Add Command for Role
 * 
 * @author Roland
 */

public class AddRoleDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	protected static Logger _log = null;
	
	public AddRoleDataCommand(RafDsRequest request,  String username){
		this.request=request;
		this.username = username;
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.AddRoleDataCommand");
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		Locator<Object> locator=null;
		List<HashMap<String, String>> dataList=new ArrayList<HashMap<String,String>>();
		List<RafRole> rafRoleListCheck = new ArrayList<RafRole>();
		try{
			locator = new Locator<Object>();
			RafRoleSessionEJBRemote sessionHome = (RafRoleSessionEJBRemote) locator.lookup(RafRoleSessionEJBRemote.class, "RafRoleSessionEJBBean");
			dataList=request.getData();
			
			RafRole veniceRole = new RafRole();
			Role stockholmRole = new Role();
			stockholmRole.setVisitorSystem("Venice");
			 
			for(int i=0;i< dataList.size();i++){
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter=data.keySet().iterator();
				
				while(iter.hasNext()){
					String key=iter.next();
					if(key.equals(DataNameTokens.RAFROLE_ROLEID)){
						veniceRole.setRoleId(new Long(data.get(key)));
					} else if(key.equals(DataNameTokens.RAFROLE_ROLENAME)){
						veniceRole.setRoleName(data.get(key));
						stockholmRole.setCode(data.get(key));
					} else if(key.equals(DataNameTokens.RAFROLE_ROLEDESC)){
						veniceRole.setRoleDesc(data.get(key));
						stockholmRole.setName(data.get(key));
					} else if(key.equals(DataNameTokens.RAFROLE_ADDTOSTOCKHOLM)){
						veniceRole.setAddToStockholm(data.get(key).equals("true")?true:false);
					} 
				}
			}
			
			//check first if the role already exist in database
			rafRoleListCheck = sessionHome.queryByRange("select o from RafRole o where o.roleName='"+veniceRole.getRoleName()+"'", 0, 1);
			if(rafRoleListCheck.size()>0){
				//data already exist
				_log.info("user already exist");
				rafDsResponse.setStatus(2);
			}else{			
				//data is unique so update the database	
				if(veniceRole.getAddToStockholm()!=null && veniceRole.getAddToStockholm()==true){
					_log.info("save add role in Stockholm");	
					AddUpdateRoleStockholm addToStockholm = new AddUpdateRoleStockholm();
									
					Boolean success = addToStockholm.addRole(username, stockholmRole);
					if(success==false){
						throw new EJBException("save add role in Stockholm failed");
					}
				}
				
				_log.info("save add role in Venice");
				veniceRole=sessionHome.persistRafRole(veniceRole);
				
				rafDsResponse.setStatus(0);
			}
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
