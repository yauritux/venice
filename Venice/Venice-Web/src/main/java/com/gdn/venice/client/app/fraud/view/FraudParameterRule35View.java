package com.gdn.venice.client.app.fraud.view;

import java.util.Date;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.fraud.presenter.FraudParameterRule35Presenter;
import com.gdn.venice.client.app.fraud.view.handlers.FraudParameterRule35UiHandlers;
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
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 * Presenter for Fraud Parameter 35 - Customer Grey List
 * 
 * @author
 */

public class FraudParameterRule35View extends ViewWithUiHandlers<FraudParameterRule35UiHandlers> implements FraudParameterRule35Presenter.MyView {

	RafViewLayout fraudParameterRule35Layout;
	ListGrid fraudParameterRule35ListGrid = new ListGrid();
	
	@Inject
	public FraudParameterRule35View() {
		fraudParameterRule35Layout = new RafViewLayout();
		ToolStrip fraudParameterRule35ToolStrip = new ToolStrip();
		fraudParameterRule35ToolStrip.setWidth100();

		ToolStripButton addButton = new ToolStripButton();
		addButton.setIcon("[SKIN]/icons/add.png");
		addButton.setTooltip("Add to Customer Grey List");
		addButton.setTitle("Add");

		ToolStripButton removeButton = new ToolStripButton();
		removeButton.setIcon("[SKIN]/icons/remove.png");
		removeButton.setTooltip("Remove from Customer Grey List");
		removeButton.setTitle("Remove");

		fraudParameterRule35ToolStrip.addButton(addButton);
		fraudParameterRule35ToolStrip.addButton(removeButton);
				
		fraudParameterRule35ListGrid.setAutoFetchData(false);
		fraudParameterRule35ListGrid.setCanEdit(true);
		fraudParameterRule35ListGrid.setCanResizeFields(true);
		fraudParameterRule35ListGrid.setShowFilterEditor(true);
		fraudParameterRule35ListGrid.setCanSort(true);
		fraudParameterRule35ListGrid.setSelectionType(SelectionStyle.SIMPLE);
		fraudParameterRule35ListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		fraudParameterRule35ListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);
		fraudParameterRule35ListGrid.setShowRowNumbers(true);
		fraudParameterRule35Layout.setMembers(fraudParameterRule35ToolStrip);
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				fraudParameterRule35ListGrid.startEditingNew();
			}
		});		

		fraudParameterRule35ListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				fraudParameterRule35ListGrid.saveAllEdits();
				refreshFraudParameterRule35Data();
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Added/Edited");
				}
			}
		});
		
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){							
							fraudParameterRule35ListGrid.removeSelectedData();
							refreshFraudParameterRule35Data();
							SC.say("Data Removed");
						}
					}
				});
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return fraudParameterRule35Layout;
	}

	protected void bindCustomUiHandlers() {
		fraudParameterRule35ListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshFraudParameterRule35Data();
			}
		});
	}

	@Override
	public void loadFraudParameterRule35Data(DataSource dataSource) {		
		//populate listgrid
		fraudParameterRule35ListGrid.setDataSource(dataSource);
		fraudParameterRule35ListGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));		
		
		fraudParameterRule35ListGrid.setSortField(DataNameTokens.FRDPARAMETERRULE35_ID);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ID).setCanEdit(false);		
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ID).setHidden(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERID).setCanEdit(true);		
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERID).setWidth("70px");
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_NOSURAT).setCanEdit(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_NOSURAT).setWidth("80px");
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERDATE).setCanEdit(true);	
		Date date = new Date();
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERDATE).setDefaultValue(date);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERDATE).setWidth("100px");
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CUSTOMERNAME).setCanEdit(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CUSTOMERNAME).setWidth("150px");
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_EMAIL).setCanEdit(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_EMAIL).setWidth("150px");
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CCNUMBER).setCanEdit(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CCNUMBER).setWidth("150px");
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_REMARKS).setCanEdit(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_REMARKS).setWidth("250px");
		
 		
		//validation
		//required
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERID).setRequired(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERDATE).setRequired(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CUSTOMERNAME).setRequired(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_EMAIL).setRequired(true);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CCNUMBER).setRequired(true);
  
		
		LengthRangeValidator lengthRangeValidator2 = new LengthRangeValidator();  
		lengthRangeValidator2.setMax(100);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_NOSURAT).setValidators(lengthRangeValidator2);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_ORDERID).setValidators(lengthRangeValidator2);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CUSTOMERNAME).setValidators(lengthRangeValidator2);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_EMAIL).setValidators(lengthRangeValidator2);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_CCNUMBER).setValidators(lengthRangeValidator2);
		
		LengthRangeValidator lengthRangeValidator3 = new LengthRangeValidator();  
		lengthRangeValidator3.setMax(1000);
		fraudParameterRule35ListGrid.getField(DataNameTokens.FRDPARAMETERRULE35_REMARKS).setValidators(lengthRangeValidator3);
		
		fraudParameterRule35Layout.addMember(fraudParameterRule35ListGrid);
        bindCustomUiHandlers();
	}
	
	public void refreshFraudParameterRule35Data() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				fraudParameterRule35ListGrid.setData(response.getData());
			}
		};
		fraudParameterRule35ListGrid.getDataSource().fetchData(fraudParameterRule35ListGrid.getFilterEditorCriteria(), callBack);
	}

}
