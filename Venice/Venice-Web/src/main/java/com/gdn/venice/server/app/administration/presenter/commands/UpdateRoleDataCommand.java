package com.gdn.venice.server.app.administration.presenter.commands;

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
 * Update Command for Role
 * 
 * @author Roland
 */

public class UpdateRoleDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;	
	
	protected static Logger _log = null;
	
	public UpdateRoleDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.UpdateRoleDataCommand");
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
						try
						{
							veniceRole = sessionHome.queryByRange("select o from RafRole o where o.roleId="+new Long(data.get(key)), 0, 1).get(0);
							stockholmRole.setCode(veniceRole.getRoleName());
						}catch(IndexOutOfBoundsException e){
							veniceRole.setRoleId(new Long(data.get(key)));
						}
						break;
					}
				}						
				
				iter = data.keySet().iterator();

				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(DataNameTokens.RAFROLE_ROLEDESC)) {
						veniceRole.setRoleDesc(data.get(key));	
						stockholmRole.setName(data.get(key));
					} else if(key.equals(DataNameTokens.RAFROLE_ADDTOSTOCKHOLM)){
						veniceRole.setAddToStockholm(data.get(key).equals("true")?true:false);
					} 
				}		
			}
			
			if(veniceRole.getAddToStockholm()!=null && veniceRole.getAddToStockholm()==true){
				_log.info("save edit role in Stockholm");
				AddUpdateRoleStockholm updateToStockholm = new AddUpdateRoleStockholm();
				Boolean success = updateToStockholm.addRole(username, stockholmRole);
				if(success==false){
					throw new EJBException("save edit role in Stockholm failed");
				}
			}
			
			if(veniceRole.getAddToStockholm()!=null && veniceRole.getAddToStockholm()==false){
				_log.info("delete role in Stockholm");
				DeleteRoleStockholm deleteInStockholm = new DeleteRoleStockholm();
				Boolean success = deleteInStockholm.deleteRole(username, stockholmRole);
				if(success==false){
					throw new EJBException("delete role in Stockholm failed");
				}
			}
			
			_log.info("save edit role in Venice");
			veniceRole=sessionHome.mergeRafRole(veniceRole);
			
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
