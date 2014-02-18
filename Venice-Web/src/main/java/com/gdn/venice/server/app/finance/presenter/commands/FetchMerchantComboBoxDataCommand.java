package com.gdn.venice.server.app.finance.presenter.commands;

import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.facade.VenMerchantSessionEJBRemote;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.client.util.Util;

/**
 * Fetch Command for Bank
 * 
 * @author Roland
 */

public class FetchMerchantComboBoxDataCommand implements RafRpcCommand{
	
	public String execute() {
		Locator<Object> locator=null;

		HashMap<String, String> mapSort = new HashMap<String, String>();
		
		try{
			locator = new Locator<Object>();			
			VenMerchantSessionEJBRemote sessionHome = (VenMerchantSessionEJBRemote) locator.lookup(VenMerchantSessionEJBRemote.class, "VenMerchantSessionEJBBean");			
			List<VenMerchant> venMerchantList = null;
			String query = "select o from VenMerchant o order by o.venParty.fullOrLegalName asc";
			venMerchantList = sessionHome.queryByRange(query, 0, 0);
			
			for(int i=0 ;i<venMerchantList.size() ;i++){
				VenMerchant venMerchantProductItem= venMerchantList.get(i);
				if(venMerchantProductItem.getVenParty()!=null){
					mapSort.put(venMerchantProductItem.getWcsMerchantId()+"",venMerchantProductItem.getWcsMerchantId()+"");							
				}
			}			
			mapSort=Util.SortedHashMap(mapSort, "asc");
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
		return Util.formXMLfromHashMap(mapSort);
	}	
}
