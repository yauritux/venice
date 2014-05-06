package com.gdn.venice.client.app.seattle.view;

import org.apache.commons.beanutils.locale.converters.IntegerLocaleConverter;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.seattle.presenter.SeatSLAFulfillmentReportPresenter;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatSLAFulfillmentUiHandlers;
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
 * The view class for the SeaSLAFulfillmentReportView screen
 */
public class SeatSLAFulfillmentReportView extends ViewWithUiHandlers<SeatSLAFulfillmentUiHandlers>
		implements SeatSLAFulfillmentReportPresenter.MyView {
	
	RafViewLayout slaFulfillmentLayout;
	
	ListGrid slaFulfillmentListGrid;
	/*
	 * Build the view and inject it
	 */
	@Inject
	public SeatSLAFulfillmentReportView() {
		slaFulfillmentLayout = new RafViewLayout();	
	}
	
	@Override
	public void loadData(DataSource dataSource) {
		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		
		slaFulfillmentListGrid = new ListGrid();
		
		slaFulfillmentListGrid.setDataSource(dataSource);
		slaFulfillmentListGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));
		slaFulfillmentListGrid.setAutoFetchData(true);
		slaFulfillmentListGrid.setCanEdit(true);
		slaFulfillmentListGrid.setCanResizeFields(true);
		slaFulfillmentListGrid.setShowFilterEditor(true);
		slaFulfillmentListGrid.setCanSort(true);
		slaFulfillmentListGrid.setSelectionType(SelectionStyle.SIMPLE);
		slaFulfillmentListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		slaFulfillmentListGrid.setShowRowNumbers(true);
		slaFulfillmentListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);		
		
		
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID).setCanEdit(false);
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_ORDERSTATUSDESC).setCanEdit(false);
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATRESULTSTATUSTRACKING_DESC).setCanEdit(false);
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_BYUSER).setCanEdit(false);
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_UPDATEDATE).setCanEdit(false);
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_PIC).setCanEdit(false);
		
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID).setHidden(true);
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_PIC).setHidden(true);
		
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MIN).setType(ListGridFieldType.INTEGER); 
		slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MAX).setType(ListGridFieldType.INTEGER); 
		//validasi
		 LengthRangeValidator lengthRangeValidator = new LengthRangeValidator();  
		 lengthRangeValidator.setMin(1);  
	     slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MIN).setValidators(lengthRangeValidator);
	     slaFulfillmentListGrid.getField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MAX).setValidators(lengthRangeValidator);
	     
	     slaFulfillmentListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
				@Override
				public void onEditComplete(EditCompleteEvent event) {						
						slaFulfillmentListGrid.saveAllEdits();		
						SC.say("Data Added/Updated");							
						refreshSlaFulfillmentData(); 										  
					}
			});  

		
		
	   slaFulfillmentLayout.setMembers(toolStrip,slaFulfillmentListGrid);
	   
	   bindCustomUiHandlers();
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.View#asWidget()
	 */
	@Override
	public Widget asWidget() {
		return slaFulfillmentLayout;
	}

	protected void bindCustomUiHandlers() {
		
		refreshSlaFulfillmentData();
 	
	}
	
	public void refreshSlaFulfillmentData() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				slaFulfillmentListGrid.setData(response.getData());}
		};		
		slaFulfillmentListGrid.getDataSource().fetchData(slaFulfillmentListGrid.getFilterEditorCriteria(), callBack);
		
	}	

	
}
