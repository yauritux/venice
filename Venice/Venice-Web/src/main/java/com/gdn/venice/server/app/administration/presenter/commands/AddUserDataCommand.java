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
import com.gdn.inventory.exchange.entity.Department;
import com.gdn.inventory.exchange.entity.User;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.RafUserSessionEJBRemote;
import com.gdn.venice.persistence.RafUser;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Add Command for User Maintenance
 * 
 * @author Anto
 */

public class AddUserDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	protected static Logger _log = null;
	
	public AddUserDataCommand(RafDsRequest request, String username){
		this.request=request;
		this.username = username;
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.AddUserDataCommand");
	}

	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		Locator<Object> locator=null;
		List<HashMap<String, String>> dataList=new ArrayList<HashMap<String,String>>();
		List<RafUser> rafUserListCheck = new ArrayList<RafUser>();

		try{
			locator = new Locator<Object>();
			RafUserSessionEJBRemote sessionHome = (RafUserSessionEJBRemote) locator.lookup(RafUserSessionEJBRemote.class, "RafUserSessionEJBBean");
			dataList=request.getData();
			
			RafUser veniceUser = new RafUser();
			User stockholmUser = new User();
			stockholmUser.setVisitorSystem("Venice");
			
			for(int i=0;i< dataList.size();i++){
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter=data.keySet().iterator();
				
				while(iter.hasNext()){
					String key=iter.next();
					
					//set the party to 7 - Internal Dummy user, because user login info is from ldap and doesn't need party.
					VenParty party = new VenParty();
					party.setPartyId(new Long(7));
					veniceUser.setVenParty(party);
					
					if(key.equals(DataNameTokens.RAFUSER_LOGINNAME)){
						veniceUser.setLoginName(data.get(key));
						stockholmUser.setCode(data.get(key));
					}else if(key.equals(DataNameTokens.RAFUSER_NAME)){
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
			
			//check first if the user already exist in database
			rafUserListCheck = sessionHome.queryByRange("select o from RafUser o where o.loginName='"+veniceUser.getLoginName()+"'", 0, 1);
			if(rafUserListCheck.size()>0){
				//data already exist
				_log.info("user already exist");
				rafDsResponse.setStatus(2);
			}else{
				//data is unique so update the database	
				if(veniceUser.getAddToStockholm()!=null && veniceUser.getAddToStockholm()==true){
					_log.info("save add user in Stockholm");	
					AddUpdateUserStockholm addToStockholm = new AddUpdateUserStockholm();
									
					Boolean success = addToStockholm.addUser(username, stockholmUser);
					if(success==false){
						throw new EJBException("save add user in Stockholm failed");
					}
				}
				
				_log.info("save add user in Venice");
				veniceUser=sessionHome.persistRafUser(veniceUser);
				
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
