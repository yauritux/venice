package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.facade.SeatStatusUomSessionEJBRemote;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.server.command.RafRpcCommand;

public class FetchUoMCommandComboBox implements RafRpcCommand {
	
		public String execute() {
			Locator<Object> locator=null;
			HashMap<Integer, String> mapSorted = new HashMap<Integer, String>();
			HashMap<String, String> map = new HashMap<String, String>();
			try{
				locator = new Locator<Object>();			
				SeatStatusUomSessionEJBRemote sessionHome = (SeatStatusUomSessionEJBRemote) locator.lookup(SeatStatusUomSessionEJBRemote.class, "SeatStatusUomSessionEJBBean");			
				List<SeatStatusUom> uoMListSource = null;
				String query = "select o from SeatStatusUom o";
				
				uoMListSource = sessionHome.queryByRange(query, 0, 0);
				
				for(int i=0; i<uoMListSource.size();i++){
					SeatStatusUom list = uoMListSource.get(i);
					mapSorted.put(new Integer(list.getStatusUomId().toString()), list.getStatusUomDesc());		
				}
				map=Util.SortedHashMap(mapSorted, "asc");
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if(locator!=null){
						locator.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return Util.formXMLfromHashMap(map);
		}	
	
}
