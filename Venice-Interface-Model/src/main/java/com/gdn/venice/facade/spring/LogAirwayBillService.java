package com.gdn.venice.facade.spring;

import java.util.Date;

import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.VenOrderItem;

public interface LogAirwayBillService {

	public void addDummyLogAirwayBillForNewlyFPOrderItem(VenOrderItem orderItem);

	public boolean reconcileActualPickupDate(Long logAirwayBillId, Date actualPickupDateFromLogistic,Date actualPickupDateFromMTA);

	public boolean reconcileService(Long logAirwayBillId, String providerCodeFromLogistic, String serviceFromLogistic, String serviceFromMTA);

	public boolean reconcileRecipient(Long logAirwayBillId, String recipientFromLogistic, String recipientFromMTA);

	public LogAirwayBill reconcileAirwayBill(LogAirwayBill providerAirwayBill, LogAirwayBill mtaAirwayBill,boolean isMcx);

}
