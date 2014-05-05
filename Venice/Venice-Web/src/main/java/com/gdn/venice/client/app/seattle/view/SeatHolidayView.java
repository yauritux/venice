package com.gdn.venice.client.app.seattle.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.seattle.presenter.SeatHolidayPresenter;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatHolidayUiHandlers;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Encoding;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.UploadItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 * The view class for the SeatHolidayView screen
 */
public class SeatHolidayView extends ViewWithUiHandlers<SeatHolidayUiHandlers>
		implements SeatHolidayPresenter.MyView {
	
	RafViewLayout holidayLayout;
	
	ListGrid holidayListGrid;
	Window uploadWindow;
	/*
	 * Build the view and inject it
	 */
	@Inject
	public SeatHolidayView() {
		holidayLayout = new RafViewLayout();	
	}
	
	@Override
	public void loadData(DataSource dataSource) {
		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		
		ToolStripButton btnAdd = new ToolStripButton();
		btnAdd.setIcon("[SKIN]/icons/add.png");
		btnAdd.setTooltip("Add to Holiday");
		btnAdd.setTitle("Add");
		
		ToolStripButton btnRemove = new ToolStripButton();
		btnRemove.setIcon("[SKIN]/icons/remove.png");
		btnRemove.setTooltip("Remove from Holiday");
		btnRemove.setTitle("Remove");
		
		ToolStripButton uploadButton = new ToolStripButton();
		uploadButton.setIcon("[SKIN]/icons/pages_add.png");
		uploadButton.setTitle("Upload");
		uploadButton.setTooltip("Upload New MIGS Report");
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				buildUploadWindow().show();
			}
		});
		
		uploadButton.setVisible(false);
		
		holidayListGrid = new ListGrid();
		
		holidayListGrid.setDataSource(dataSource);
		holidayListGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));
		holidayListGrid.setAutoFetchData(false);
		holidayListGrid.setCanEdit(true);
		holidayListGrid.setCanResizeFields(true);
		holidayListGrid.setShowFilterEditor(true);
		holidayListGrid.setCanSort(true);
		holidayListGrid.setSelectionType(SelectionStyle.SIMPLE);
		holidayListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		holidayListGrid.setShowRowNumbers(true);
		holidayListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);		
		
		holidayListGrid.getField(DataNameTokens.HOLIDAY_ID).setHidden(true);
		
		btnAdd.addClickHandler(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {
				holidayListGrid.startEditingNew();		
				
			}
		});
		
		holidayListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {				
				holidayListGrid.saveAllEdits();		
					SC.say("Data Added/Updated");							
					refreshHolidayData(); 
				}
		});
		
		btnRemove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(holidayListGrid.getSelection().length!=0){
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){
							holidayListGrid.removeSelectedData();
							SC.say("Data Removed");							
							refreshHolidayData(); 
							}
						}
					});
				}else
					SC.say("Please select the data to be Removed");
				}
			});
		
		toolStrip.addButton(uploadButton);
		toolStrip.addButton(btnAdd);
		toolStrip.addButton(btnRemove);
		
		holidayLayout.setMembers(toolStrip,holidayListGrid);
		
		bindCustomUiHandlers();
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.View#asWidget()
	 */
	@Override
	public Widget asWidget() {
		return holidayLayout;
	}

	protected void bindCustomUiHandlers() {
		refreshHolidayData();
 	
	}

	public void refreshHolidayData() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				holidayListGrid.setData(response.getData());}
		};		
		holidayListGrid.getDataSource().fetchData(holidayListGrid.getFilterEditorCriteria(), callBack);
		
	}	
	
	private Window buildUploadWindow() {
		uploadWindow = new Window();
		uploadWindow.setWidth(320);
		uploadWindow.setHeight(150);
		uploadWindow.setTitle("Upload Holiday Report");
		uploadWindow.setShowMinimizeButton(false);
		uploadWindow.setIsModal(true);
		uploadWindow.setShowModalMask(true);
		uploadWindow.centerInPage();
		uploadWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				uploadWindow.destroy();
			}
		});
		
		VLayout uploadLayout = new VLayout();
		uploadLayout.setHeight100();
		uploadLayout.setWidth100();

		final DynamicForm uploadForm = new DynamicForm();
		uploadForm.setPadding(5);
		uploadForm.setEncoding(Encoding.MULTIPART);
		uploadForm.setTarget("upload_frame");
		
		UploadItem reportFileItem = new UploadItem();
		reportFileItem.setTitle("Holiday File");
		uploadForm.setItems(reportFileItem);
		
		HLayout buttonLayout = new HLayout(5);
		IButton buttonUpload = new IButton("Upload");
		IButton buttonCancel = new IButton("Cancel");
		

		
		buttonUpload.addClickHandler(new ClickHandler() {
			@Override	
			public void onClick(ClickEvent event) {
				String host = GWT.getHostPageBaseURL();				
				uploadForm.setAction(host + "SeatHolidayPresenterServlet");								
				uploadForm.submitForm();
				
				uploadWindow.destroy();
			}
		});
		
		buttonCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				uploadWindow.destroy();
			}
		});
		
		buttonLayout.setAlign(Alignment.CENTER);
		buttonLayout.setMembers(buttonUpload, buttonCancel);
		uploadLayout.setMembers(uploadForm, buttonLayout);
		uploadWindow.addItem(uploadLayout);
		
		return uploadWindow;
	}
	
}
