package com.gdn.venice.server.app.fraud.presenter.commands;

import java.sql.Timestamp;
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
 * Add Command for Parameter Rule 35 - Customer Grey List
 * 
 * @author
 */

public class AddFraudParameterRule35DataCommand implements RafDsCommand {

	RafDsRequest request;
	
	public AddFraudParameterRule35DataCommand(RafDsRequest request){
		this.request=request;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		Locator<Object> locator=null;
		List<HashMap<String, String>> dataList=new ArrayList<HashMap<String,String>>();
		try{
			locator = new Locator<Object>();
			FrdParameterRule35SessionEJBRemote sessionHome = (FrdParameterRule35SessionEJBRemote) locator.lookup(FrdParameterRule35SessionEJBRemote.class, "FrdParameterRule35SessionEJBBean");
			dataList=request.getData();
			
			FrdParameterRule35 entityRule35 = new FrdParameterRule35();
			for(int i=0;i< dataList.size();i++){
				Map<String, String> data = dataList.get(i);
				Iterator<String> iter=data.keySet().iterator();
				
				while(iter.hasNext()){
					String key=iter.next();
					if(key.equals(DataNameTokens.FRDPARAMETERRULE35_ID)){
						entityRule35.setId(new Long(data.get(key)));
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_ORDERID)){
						entityRule35.setOrderId(data.get(key));		
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_NOSURAT)){
						entityRule35.setNoSurat(data.get(key));				
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_ORDERDATE)){
						entityRule35.setOrderDate(new Timestamp(System.currentTimeMillis()));
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_CUSTOMERNAME)){
						entityRule35.setCustomerName(data.get(key));				
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_EMAIL)){
						entityRule35.setEmail(data.get(key));
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_CCNUMBER)){
						entityRule35.setCcNumber(data.get(key));				
					}else if(key.equals(DataNameTokens.FRDPARAMETERRULE35_REMARKS)){
						entityRule35.setRemarks(data.get(key));
					}
				}
			}
			sessionHome.persistFrdParameterRule35(entityRule35);

			rafDsResponse.setStatus(0);
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
