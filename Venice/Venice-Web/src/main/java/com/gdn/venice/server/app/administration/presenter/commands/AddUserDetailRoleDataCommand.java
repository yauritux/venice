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
import com.gdn.inventory.exchange.entity.User;
import com.gdn.inventory.exchange.entity.UserRole;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.RafRoleSessionEJBRemote;
import com.gdn.venice.facade.RafUserRoleSessionEJBRemote;
import com.gdn.venice.facade.RafUserSessionEJBRemote;
import com.gdn.venice.persistence.RafRole;
import com.gdn.venice.persistence.RafUser;
import com.gdn.venice.persistence.RafUserRole;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Add Command for User Role Detail
 * 
 * @author Roland
 */

public class AddUserDetailRoleDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	protected static Logger _log = null;
	
	public AddUserDetailRoleDataCommand(RafDsRequest request,  String username){
		this.request=request;		
		this.username = username;
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.AddUserDetailRoleDataCommand");
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		Locator<Object> locator=null;
		List<RafUserRole> rafUserRoleListCheck = new ArrayList<RafUserRole>();
		List<RafUser> rafUserListCheck = new ArrayList<RafUser>();
		List<RafRole> rafRoleListCheck = new ArrayList<RafRole>();
		List<HashMap<String, String>> dataList=new ArrayList<HashMap<String,String>>();	
		String roleId="";
		
		//because only role id can be changed in screen, so only role id sent from servlet, user id must be sent as parameter.
		String userId=request.getParams().get(DataNameTokens.RAFUSER_USERID).toString();
		try{
			locator = new Locator<Object>();
			RafUserRoleSessionEJBRemote sessionHome = (RafUserRoleSessionEJBRemote) locator.lookup(RafUserRoleSessionEJBRemote.class, "RafUserRoleSessionEJBBean");
			RafUserSessionEJBRemote userSessionHome = (RafUserSessionEJBRemote) locator.lookup(RafUserSessionEJBRemote.class, "RafUserSessionEJBBean");
			RafRoleSessionEJBRemote roleSessionHome = (RafRoleSessionEJBRemote) locator.lookup(RafRoleSessionEJBRemote.class, "RafRoleSessionEJBBean");
			dataList=request.getData();
			
			RafUserRole userRoleVenice = new RafUserRole();
			
			for(int i=0;i< dataList.size();i++){
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter=data.keySet().iterator();
				
				while(iter.hasNext()){
					String key=iter.next();
					if(key.equals(DataNameTokens.RAFUSER_RAFUSERROLES_USERID)){
						RafUser user = new RafUser();
						user.setUserId(new Long(data.get(key)));
						userRoleVenice.setRafUser(user);
					} else if(key.equals(DataNameTokens.RAFUSER_RAFUSERROLES_ROLEID)){
						RafRole role =  new RafRole();
						role.setRoleId(new Long(data.get(key)));
						userRoleVenice.setRafRole(role);
						roleId=new Long(data.get(DataNameTokens.RAFUSER_RAFUSERROLES_ROLEID)).toString();
					}
				}
			}
			
			//check first if the user and role already exist in database
			rafUserRoleListCheck = sessionHome.queryByRange("select o from RafUserRole o where o.rafUser.userId="+userId+" and o.rafRole.roleId="+roleId, 0, 1);
			if(rafUserRoleListCheck.size()>0){
				//data already exist
				_log.info("user role already exist");
				rafDsResponse.setStatus(2);
			}else{
				//data is unique so update the database				
				rafUserListCheck = userSessionHome.queryByRange("select o from RafUser o where o.userId="+userId, 0, 1);
				rafRoleListCheck = roleSessionHome.queryByRange("select o from RafRole o where o.roleId="+roleId, 0, 1);
								
				if(rafUserListCheck.size()>0 && rafRoleListCheck.size()>0){
					if(rafUserListCheck.get(0).getAddToStockholm()!=null && rafUserListCheck.get(0).getAddToStockholm()==true 
							&& rafRoleListCheck.get(0).getAddToStockholm()!=null && rafRoleListCheck.get(0).getAddToStockholm()==true){
						_log.info("save add role user in Stockholm");
						System.out.println("save add role user in Stockholm");
						
						User userStockholm = new User();
						userStockholm.setCode(rafUserListCheck.get(0).getLoginName());
						
						Role roleStockholm = new Role();
						roleStockholm.setCode(rafRoleListCheck.get(0).getRoleName());
						
						UserRole userRoleStockholm = new UserRole();
						userRoleStockholm.setUser(userStockholm);
						userRoleStockholm.setRole(roleStockholm);
						
						AddUpdateUserRoleStockholm addToStockholm = new AddUpdateUserRoleStockholm();
										
						Boolean success = addToStockholm.addUserRole(username, userRoleStockholm);
						if(success==false){
							throw new EJBException("save add user role in Stockholm failed");
						}
					}
				}
				
				_log.info("save add role user in Venice");
				userRoleVenice=sessionHome.persistRafUserRole(userRoleVenice);
				
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
