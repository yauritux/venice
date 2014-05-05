package com.gdn.venice.client.app.seattle.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.seattle.presenter.SeatETDPresenter;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatETDUiHandlers;
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
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 * The view class for the SeatETDView screen
 */
public class SeatETDView extends ViewWithUiHandlers<SeatETDUiHandlers>
		implements SeatETDPresenter.MyView {
	
	RafViewLayout etdLayout;
	
	ListGrid etdListGrid;
	/*
	 * Build the view and inject it
	 */
	@Inject
	public SeatETDView() {
		etdLayout = new RafViewLayout();	
	}
	
	@Override
	public void loadData(DataSource dataSource) {
		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		
		etdListGrid = new ListGrid();
		
		etdListGrid.setDataSource(dataSource);
		etdListGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));
		etdListGrid.setAutoFetchData(false);
		etdListGrid.setCanEdit(true);
		etdListGrid.setCanResizeFields(true);
		etdListGrid.setShowFilterEditor(true);
		etdListGrid.setCanSort(true);
		etdListGrid.setSelectionType(SelectionStyle.SIMPLE);
		etdListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		etdListGrid.setShowRowNumbers(true);
		etdListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);				
		
		etdListGrid.getField(DataNameTokens.VENMERCHANTPRODUCT_PRODUCTID).setCanEdit(false);
		etdListGrid.getField(DataNameTokens.VENMERCHANTPRODUCT_WCSPRODUCTSKU).setCanEdit(false);
		etdListGrid.getField(DataNameTokens.VENMERCHANTPRODUCT_WCSPRODUCTNAME).setCanEdit(false);
		
		etdListGrid.getField(DataNameTokens.SEAT_ORDER_ETD_NEW).setCanFilter(false);
		etdListGrid.getField(DataNameTokens.SEAT_ORDER_ETD_START).setCanFilter(false);
		etdListGrid.getField(DataNameTokens.SEAT_ORDER_ETD_END).setCanFilter(false);
		etdListGrid.getField(DataNameTokens.VENMERCHANTPRODUCT_PRODUCTID).setCanFilter(false);
		
		etdListGrid.getField(DataNameTokens.VENMERCHANTPRODUCT_PRODUCTID).setHidden(true);
		
		etdListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {				
				etdListGrid.saveAllEdits();		
					SC.say("Data Added/Updated");							
					refreshEtdData(); 
				}
		});

		etdLayout.setMembers(toolStrip,etdListGrid);
		
		bindCustomUiHandlers();
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.View#asWidget()
	 */
	@Override
	public Widget asWidget() {
		return etdLayout;
	}

	protected void bindCustomUiHandlers() {
		refreshEtdData();
	}
	
	public void refreshEtdData() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				etdListGrid.setData(response.getData());}
		};		
		etdListGrid.getDataSource().fetchData(etdListGrid.getFilterEditorCriteria(), callBack);
		
	}	

	
}
