package com.gdn.venice.server.app.administration.presenter.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Department;
import com.gdn.inventory.exchange.entity.User;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.RafUserSessionEJBRemote;
import com.gdn.venice.persistence.RafUser;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for User
 * 
 * @author Anto
 */

public class UpdateUserDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	protected static Logger _log = null;
	
	public UpdateUserDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.UpdateUserDataCommand");
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<HashMap<String,String >> dataList = request.getData();		
						
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			RafUserSessionEJBRemote sessionHome = (RafUserSessionEJBRemote) locator.lookup(RafUserSessionEJBRemote.class, "RafUserSessionEJBBean");

			RafUser veniceUser = new RafUser();
			User stockholmUser = new User();
			stockholmUser.setVisitorSystem("Venice");
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter = data.keySet().iterator();

				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(DataNameTokens.RAFUSER_USERID)) {
						try{
							veniceUser = sessionHome.queryByRange("select o from RafUser o where o.userId="+new Long(data.get(key)), 0, 1).get(0);
							stockholmUser.setCode(veniceUser.getLoginName());
						}catch(IndexOutOfBoundsException e){
							veniceUser.setUserId(new Long(data.get(key)));
						}
					}
				}						
				
				iter = data.keySet().iterator();

				while (iter.hasNext()) {
					String key = iter.next();
					if(key.equals(DataNameTokens.RAFUSER_NAME)){
						veniceUser.setName(data.get(key));
						stockholmUser.setName(data.get(key));
					}else if(key.equals(DataNameTokens.RAFUSER_ADDTOSTOCKHOLM)){
						veniceUser.setAddToStockholm(data.get(key).equals("true")?true:false);
					}else if(key.equals(DataNameTokens.RAFUSER_DEPARTMENT)){
						System.out.println("set department: "+data.get(key));
						veniceUser.setDepartment(data.get(key));
						Department dept = new Department();
						dept.setCode(data.get(key));
						stockholmUser.setDepartment(dept);
					}
				}		
			}
									
			if(veniceUser.getAddToStockholm()!=null && veniceUser.getAddToStockholm()==true){
				_log.info("save edit user in Stockholm");
				AddUpdateUserStockholm updateToStockholm = new AddUpdateUserStockholm();
				Boolean success = updateToStockholm.addUser(username, stockholmUser);
				if(success==false){
					throw new EJBException("save edit user in Stockholm failed");
				}
			}
			
			if(veniceUser.getAddToStockholm()!=null && veniceUser.getAddToStockholm()==false){
				_log.info("delete user in Stockholm");
				DeleteUserStockholm deleteInStockholm = new DeleteUserStockholm();
				Boolean success = deleteInStockholm.deleteUser(username, stockholmUser);
				if(success==false){
					throw new EJBException("delete user in Stockholm failed");
				}
			}
			
			_log.info("save edit user in Venice");
			veniceUser=sessionHome.mergeRafUser(veniceUser);

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
