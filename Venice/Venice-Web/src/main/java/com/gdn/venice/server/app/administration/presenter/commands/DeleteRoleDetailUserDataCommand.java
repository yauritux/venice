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
 * Delete Command for Role User Detail
 * 
 * @author Roland
 */

public class DeleteRoleDetailUserDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	protected static Logger _log = null;
	
	public DeleteRoleDetailUserDataCommand(RafDsRequest request,  String username) {
		this.request = request;
		this.username = username;		
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.DeleteRoleDetailUserDataCommand");
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String,String >> dataList = request.getData();
		
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			RafUserRoleSessionEJBRemote sessionHome = (RafUserRoleSessionEJBRemote) locator.lookup(RafUserRoleSessionEJBRemote.class, "RafUserRoleSessionEJBBean");
			RafUserSessionEJBRemote userSessionHome = (RafUserSessionEJBRemote) locator.lookup(RafUserSessionEJBRemote.class, "RafUserSessionEJBBean");
			RafRoleSessionEJBRemote roleSessionHome = (RafRoleSessionEJBRemote) locator.lookup(RafRoleSessionEJBRemote.class, "RafRoleSessionEJBBean");
			
			List<RafUserRole> veniceUserRoleList = new ArrayList<RafUserRole>();	
			List<RafUser> rafUserListCheck = new ArrayList<RafUser>();
			List<RafRole> rafRoleListCheck = new ArrayList<RafRole>();
			RafUserRole veniceUserRole = new RafUserRole();
			String roleId="";
			String userId="";
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter = data.keySet().iterator();
	
				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(DataNameTokens.RAFROLE_RAFUSERROLES_USERID)) {
						RafUser user = new RafUser();
						user.setUserId(new Long(data.get(DataNameTokens.RAFROLE_RAFUSERROLES_USERID)));
						veniceUserRole.setRafUser(user);
						userId=new Long(data.get(DataNameTokens.RAFROLE_RAFUSERROLES_USERID)).toString();
					} else if (key.equals(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID)) {
						RafRole role = new RafRole();
						role.setRoleId(new Long(data.get(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID)));
						veniceUserRole.setRafRole(role);
						roleId=new Long(data.get(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID)).toString();
					} 
				}						
				
				veniceUserRoleList=sessionHome.queryByRange("select o from RafUserRole o where o.rafUser.userId="+userId+" and o.rafRole.roleId="+roleId, 0, 1);
				if(veniceUserRoleList.size()>0){					
					//delete user role in stockholm
					if(veniceUserRoleList.get(i).getRafUser().getAddToStockholm()!=null && veniceUserRoleList.get(i).getRafUser().getAddToStockholm()==true &&
							veniceUserRoleList.get(i).getRafRole().getAddToStockholm()!=null && veniceUserRoleList.get(i).getRafRole().getAddToStockholm()==true){			
						_log.info("delete user role in Stockholm");
						
						rafUserListCheck = userSessionHome.queryByRange("select o from RafUser o where o.userId="+userId, 0, 1);
						rafRoleListCheck = roleSessionHome.queryByRange("select o from RafRole o where o.roleId="+roleId, 0, 1);
						
						User userStockholm = new User();
						userStockholm.setCode(rafUserListCheck.get(0).getLoginName());
						
						Role roleStockholm = new Role();
						roleStockholm.setCode(rafRoleListCheck.get(0).getRoleName());
						
						UserRole userRoleStockholm = new UserRole();
						userRoleStockholm.setUser(userStockholm);
						userRoleStockholm.setRole(roleStockholm);
						
						DeleteUserRoleStockholm deleteInStockholm = new DeleteUserRoleStockholm();
										
						Boolean success = deleteInStockholm.deleteUserRole(username, userRoleStockholm);
						if(success==false){
							throw new EJBException("delete user role in Stockholm failed");
						}
					}
					
					_log.info("delete user role in Venice");
					sessionHome.removeRafUserRoleList((ArrayList<RafUserRole>)veniceUserRoleList);
				}
			}
				
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
