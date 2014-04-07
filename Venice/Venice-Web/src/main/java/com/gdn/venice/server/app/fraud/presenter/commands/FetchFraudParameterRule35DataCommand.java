package com.gdn.venice.server.app.fraud.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FrdParameterRule35SessionEJBRemote;
import com.gdn.venice.persistence.FrdParameterRule35;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;
import com.smartgwt.client.util.SC;

/**
 * Fetch Command for Parameter Rule 35 - Customer Grey List
 * 
 * @author
 */

public class FetchFraudParameterRule35DataCommand implements RafDsCommand {

	RafDsRequest request;
	
	public FetchFraudParameterRule35DataCommand(RafDsRequest request){
		this.request=request;
	}
	@Override
	public RafDsResponse execute() {
		
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();
		Locator<Object> locator=null;
		
		try{
			locator = new Locator<Object>();
			FrdParameterRule35SessionEJBRemote sessionHome = (FrdParameterRule35SessionEJBRemote) locator.lookup(FrdParameterRule35SessionEJBRemote.class, "FrdParameterRule35SessionEJBBean");
			List<FrdParameterRule35> parameterRule35List = null;
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			if (criteria == null) {
				String query = "select o from FrdParameterRule35 o";			
				parameterRule35List = sessionHome.queryByRange(query, 0, 50);
			} else {
				FrdParameterRule35 bl = new FrdParameterRule35();
				parameterRule35List = sessionHome.findByFrdParameterRule35Like(bl, criteria, 0, 0);
			}
			
			for(int i=0; i<parameterRule35List.size();i++){
				HashMap<String, String> map = new HashMap<String, String>();
				FrdParameterRule35 list = parameterRule35List.get(i);
				
				map.put(DataNameTokens.FRDPARAMETERRULE35_ID, Util.isNull(list.getId(), "").toString());
				map.put(DataNameTokens.FRDPARAMETERRULE35_ORDERID, Util.isNull(list.getOrderId(), "").toString());
				map.put(DataNameTokens.FRDPARAMETERRULE35_NOSURAT, Util.isNull(list.getNoSurat(), "").toString());	
				map.put(DataNameTokens.FRDPARAMETERRULE35_ORDERDATE, Util.isNull(list.getOrderDate(), "").toString());
				map.put(DataNameTokens.FRDPARAMETERRULE35_CUSTOMERNAME, Util.isNull(list.getCustomerName(), "").toString());
				map.put(DataNameTokens.FRDPARAMETERRULE35_EMAIL, Util.isNull(list.getEmail(), "").toString());
				map.put(DataNameTokens.FRDPARAMETERRULE35_CCNUMBER, Util.isNull(list.getCcNumber(), "").toString());
				map.put(DataNameTokens.FRDPARAMETERRULE35_REMARKS, Util.isNull(list.getRemarks(), "").toString());
				
				
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
