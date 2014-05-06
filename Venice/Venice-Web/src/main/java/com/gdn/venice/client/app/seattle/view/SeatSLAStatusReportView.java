package com.gdn.venice.client.app.seattle.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.seattle.presenter.SeatSLAStatusReportPresenter;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatSLAStatusUiHandlers;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 * The view class for the SeatSLAStatusReportView screen
 */
public class SeatSLAStatusReportView extends ViewWithUiHandlers<SeatSLAStatusUiHandlers>
		implements SeatSLAStatusReportPresenter.MyView {
	
	RafViewLayout slaStatusLayout;
	
	ListGrid slaStatusListGrid;	

	public Map<String, String> tempMapUoM = new HashMap<String, String>();
	
	/*
	 * Build the view and inject it
	 */
	@Inject
	public SeatSLAStatusReportView() {
		slaStatusLayout = new RafViewLayout();	
	}
	
	@Override
	public void loadData(DataSource dataSource) {
		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		
		slaStatusListGrid = new ListGrid();
		
		slaStatusListGrid.setDataSource(dataSource);
		slaStatusListGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));
		slaStatusListGrid.setAutoFetchData(true);
		slaStatusListGrid.setCanEdit(true);
		slaStatusListGrid.setCanResizeFields(true);
		slaStatusListGrid.setShowFilterEditor(true);
		slaStatusListGrid.setCanSort(true);
		slaStatusListGrid.setSelectionType(SelectionStyle.SIMPLE);
		slaStatusListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		slaStatusListGrid.setShowRowNumbers(true);
		slaStatusListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);	
				
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID).setCanEdit(false);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_ORDERSTATUSDESC).setCanEdit(false);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATRESULTSTATUSTRACKING_DESC).setCanEdit(false);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_DESC).setCanEdit(false);	
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_BYUSER).setCanEdit(false);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_UPDATEDATE).setCanEdit(false);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_PIC).setCanEdit(false);
		
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_ID).setValueMap(tempMapUoM);	
		
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID).setHidden(true);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_DESC).setHidden(true);
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_PIC).setHidden(true);
		
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_MIN).setType(ListGridFieldType.INTEGER); 
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_MAX).setType(ListGridFieldType.INTEGER); 
		slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SLA).setType(ListGridFieldType.INTEGER); 
		//validasi
		 LengthRangeValidator lengthRangeValidator = new LengthRangeValidator();  
		 lengthRangeValidator.setMin(1);  
	     slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_MIN).setValidators(lengthRangeValidator);
	     slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_MAX).setValidators(lengthRangeValidator);
	     slaStatusListGrid.getField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SLA).setValidators(lengthRangeValidator);
	     
	     slaStatusListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
				@Override
				public void onEditComplete(EditCompleteEvent event) {						
						slaStatusListGrid.saveAllEdits();		
						SC.say("Data Added/Updated");							
						refreshSlaStatusData(); 			
					  
					}
			});  
	    		
		slaStatusLayout.setMembers(toolStrip,slaStatusListGrid);
		
		 bindCustomUiHandlers();
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.View#asWidget()
	 */
	@Override
	public Widget asWidget() {
		return slaStatusLayout;
	}

	protected void bindCustomUiHandlers() {
		refreshSlaStatusData();
	}
	
	public void refreshSlaStatusData() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				slaStatusListGrid.setData(response.getData());}
		};		
		slaStatusListGrid.getDataSource().fetchData(slaStatusListGrid.getFilterEditorCriteria(), callBack);
		
	}	
	
	public void setComboBoxUoM(Map <String,String> map){
		this.tempMapUoM=map;
	}	
	


	
}
