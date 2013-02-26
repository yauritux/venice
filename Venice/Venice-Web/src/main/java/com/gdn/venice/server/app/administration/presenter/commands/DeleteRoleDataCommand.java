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
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Delete Command for Role
 * 
 * @author Roland
 */

public class DeleteRoleDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	protected static Logger _log = null;
	
	public DeleteRoleDataCommand(RafDsRequest request,  String username) {
		this.request = request;
		this.username = username;		
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.DeleteRoleDataCommand");
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<HashMap<String,String >> dataList = request.getData();	
		
		Locator<Object> locator = null;
		
		try {
			locator = new Locator<Object>();
			RafRoleSessionEJBRemote sessionHome = (RafRoleSessionEJBRemote) locator.lookup(RafRoleSessionEJBRemote.class, "RafRoleSessionEJBBean");

			RafRole veniceRole = new RafRole();
			Role stockholmRole = new Role();
			stockholmRole.setVisitorSystem("Venice");
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter = data.keySet().iterator();

				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(DataNameTokens.RAFROLE_ROLEID)) {
						veniceRole = sessionHome.queryByRange("select o from RafRole o where o.roleId="+new Long(data.get(key)), 0, 1).get(0); 							
					} 
				}	
								
				//delete role in stockholm
				if(veniceRole.getAddToStockholm()!=null && veniceRole.getAddToStockholm()==true){					
					_log.info("delete role in Stockholm");
					stockholmRole.setCode(veniceRole.getRoleName());
					
					DeleteRoleStockholm deleteInStockholm = new DeleteRoleStockholm();
									
					Boolean success = deleteInStockholm.deleteRole(username, stockholmRole);
					if(success==false){
						throw new EJBException("delete role in Stockholm failed");
					}
				}
				
				_log.info("delete role in Venice");
				sessionHome.removeRafRole(veniceRole);
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
