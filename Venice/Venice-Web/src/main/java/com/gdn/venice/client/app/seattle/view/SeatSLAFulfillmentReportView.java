package com.gdn.venice.client.app.seattle.view;

import com.gdn.venice.client.app.seattle.presenter.SeatSLAFulfillmentReportPresenter;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatSLAFulfillmentUiHandlers;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
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
		
		slaFulfillmentLayout.setMembers(toolStrip,slaFulfillmentListGrid);
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.View#asWidget()
	 */
	@Override
	public Widget asWidget() {
		return slaFulfillmentLayout;
	}

	protected void bindCustomUiHandlers() {
 	
	}

	
}
