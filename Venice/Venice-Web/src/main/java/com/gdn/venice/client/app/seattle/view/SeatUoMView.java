package com.gdn.venice.client.app.seattle.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.seattle.presenter.SeatUoMPresenter;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatUoMUiHandlers;
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
import com.smartgwt.client.widgets.form.validator.IntegerRangeValidator;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 * The view class for the SeatUoMView screen
 */
public class SeatUoMView extends ViewWithUiHandlers<SeatUoMUiHandlers>
		implements SeatUoMPresenter.MyView {
	
	RafViewLayout slaUoMLayout;
	
	ListGrid slaUoMListGrid;
	/*
	 * Build the view and inject it
	 */
	@Inject
	public SeatUoMView() {
		slaUoMLayout = new RafViewLayout();	
	}
	
	@Override
	public void loadData(DataSource dataSource) {
		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		
		slaUoMListGrid = new ListGrid();
		
		slaUoMListGrid.setDataSource(dataSource);
		slaUoMListGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));
		slaUoMListGrid.setAutoFetchData(true);
		slaUoMListGrid.setCanEdit(true);
		slaUoMListGrid.setCanResizeFields(true);
		slaUoMListGrid.setShowFilterEditor(true);
		slaUoMListGrid.setCanSort(true);
		slaUoMListGrid.setSelectionType(SelectionStyle.SIMPLE);
		slaUoMListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		slaUoMListGrid.setShowRowNumbers(true);
		slaUoMListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);		
		
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_ID).setCanEdit(false);
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_STATUSUOMDESC).setCanEdit(false);
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_BYUSER).setCanEdit(false);
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_UPDATEDATE).setCanEdit(false);
		
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_ID).setHidden(true);
		
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_STATUSUOMFROM).setType(ListGridFieldType.INTEGER); 
		slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_STATUSUOMEND).setType(ListGridFieldType.INTEGER); 
		//validasi
		LengthRangeValidator lengthRangeValidator = new LengthRangeValidator();  
		 lengthRangeValidator.setMin(1);  
	     slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_STATUSUOMFROM).setValidators(lengthRangeValidator);
	     slaUoMListGrid.getField(DataNameTokens.SEATSTATUSUOM_STATUSUOMEND).setValidators(lengthRangeValidator);
	     
	     slaUoMListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
				@Override
				public void onEditComplete(EditCompleteEvent event) {									
						slaUoMListGrid.saveAllEdits();		
						SC.say("Data Added/Updated");							
						refreshUoMtData(); 					
					}
			});  
		slaUoMLayout.setMembers(toolStrip,slaUoMListGrid);
		
		 bindCustomUiHandlers();
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.View#asWidget()
	 */
	@Override
	public Widget asWidget() {
		return slaUoMLayout;
	}

	protected void bindCustomUiHandlers() {
		refreshUoMtData();
	}

	public void refreshUoMtData() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				slaUoMListGrid.setData(response.getData());}
		};		
		slaUoMListGrid.getDataSource().fetchData(slaUoMListGrid.getFilterEditorCriteria(), callBack);
		
	}	
	
}
