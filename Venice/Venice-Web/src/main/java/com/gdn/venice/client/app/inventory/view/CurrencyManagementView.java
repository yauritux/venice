/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.CurrencyManagementPresenter;
import com.gdn.venice.client.app.inventory.view.handler.CurrencyManagementUiHandler;
import com.gdn.venice.client.presenter.MainPagePresenter;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 *
 * @author Maria Olivia
 */
public class CurrencyManagementView extends ViewWithUiHandlers<CurrencyManagementUiHandler> implements
		CurrencyManagementPresenter.MyView {

    RafViewLayout currencyLayout;
    ListGrid currencyListGrid;
    Window currencyDetailWindow, addCurrencyWindow;
    
    private static final int NEW_CURRENCY = 0;
    private static final int EXISTING_CURRENCY = 1;
    
    /*
     * The toolstrip objects for the header
     */
    ToolStrip currencyToolStrip;
    ToolStripButton addButton;

    @Inject
    public CurrencyManagementView() {
        currencyToolStrip = new ToolStrip();
        currencyToolStrip.setWidth100();
        currencyToolStrip.setPadding(2);

        addButton = new ToolStripButton();
        addButton.setIcon("[SKIN]/icons/add.png");
        addButton.setTooltip("Add New Currency");
        addButton.setTitle("Add Currency");

        currencyToolStrip.addButton(addButton);

        currencyLayout = new RafViewLayout();

        currencyListGrid = new ListGrid();
        currencyListGrid.setWidth100();
        currencyListGrid.setHeight100();
        currencyListGrid.setShowAllRecords(true);
        currencyListGrid.setSortField(0);

        currencyListGrid.setShowFilterEditor(true);
        currencyListGrid.setCanResizeFields(true);
        currencyListGrid.setShowRowNumbers(true);

        bindCustomUiHandlers();
    }

    private void bindCustomUiHandlers() {
        currencyListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = currencyListGrid.getSelectedRecord();
                buildCurrencyDetailWindow(record, EXISTING_CURRENCY).show();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	buildCurrencyDetailWindow(null, NEW_CURRENCY).show();
            }
        });
        
        currencyListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshAllCurrencyData();
			}
		});
    }

    private Window buildCurrencyDetailWindow(final ListGridRecord record, final int operation) {
        currencyDetailWindow = new Window();
        currencyDetailWindow.setWidth(360);
        currencyDetailWindow.setHeight(170);
        currencyDetailWindow.setShowMinimizeButton(false);
        currencyDetailWindow.setIsModal(true);
        currencyDetailWindow.setShowModalMask(true);
        currencyDetailWindow.centerInPage();

        currencyDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                currencyDetailWindow.destroy();
            }
        });
        	
        VLayout currencyDetailLayout = new VLayout();
        currencyDetailLayout.setHeight100();
        currencyDetailLayout.setWidth100();

        final DynamicForm warehouseDetailForm = new DynamicForm();
        warehouseDetailForm.setPadding(5);

        String errMsg = "Required field";
        final TextItem curCurrency = new TextItem(DataNameTokens.INV_CURRENCY_CURRENCY, "Currency");
        curCurrency.setRequired(Boolean.TRUE);
        curCurrency.setRequiredMessage(errMsg);
        final TextItem curValue = new TextItem(DataNameTokens.INV_CURRENCY_RATE, "Value");
        curValue.setRequired(Boolean.TRUE);
        curValue.setRequiredMessage(errMsg);
        final Label infoLabel = new Label();
        
        curCurrency.addChangedHandler(new ChangedHandler() {			
			@Override
			public void onChanged(ChangedEvent event) {
				infoLabel.setContents("1 "+ curCurrency.getValueAsString() + " = Rp " + curValue.getValueAsString());
			}
		});
        
        curValue.addChangedHandler(new ChangedHandler() {			
			@Override
			public void onChanged(ChangedEvent event) {
				infoLabel.setContents("1 "+ curCurrency.getValueAsString() + " = Rp " + curValue.getValueAsString());
			}
		});
        
        warehouseDetailForm.setFields(curCurrency, curValue);
        
        HLayout buttonSet = new HLayout(5);
        IButton closeButton = new IButton("Close");
        final IButton editButton = new IButton();
        
        if(operation == NEW_CURRENCY){
        	currencyDetailWindow.setTitle("Add New Currency");
        	editButton.setTitle("Save");
            buttonSet.setMembers(closeButton, editButton);
        } else {
            currencyDetailWindow.setTitle("Currency Detail");
            warehouseDetailForm.setDisabled(true);
            curCurrency.setValue(record.getAttribute(DataNameTokens.INV_CURRENCY_CURRENCY));
            curValue.setValue(record.getAttribute(DataNameTokens.INV_CURRENCY_RATE));
        	editButton.setTitle("Edit");
        	IButton deleteButton = new IButton("Delete");
        	
        	deleteButton.addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					getUiHandlers().deleteCurrency(MainPagePresenter.signedInUser, 
							record.getAttribute(DataNameTokens.INV_CURRENCY_ID));
				}
			});
        	
            buttonSet.setMembers(closeButton, editButton, deleteButton);
        }

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currencyDetailWindow.destroy();
            }
        });

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if(editButton.getTitle().equals("Edit")){
            		warehouseDetailForm.setDisabled(false);
            		editButton.setTitle("Save");
            	} else {
            		if(warehouseDetailForm.validate()){
    	                HashMap<String, String> data = new HashMap<String, String>();
    	                if(operation == EXISTING_CURRENCY){
    	                	data.put(DataNameTokens.INV_CURRENCY_ID, 
    	                			record.getAttribute(DataNameTokens.INV_CURRENCY_ID));
    	                }
    	                data.put(DataNameTokens.INV_CURRENCY_CURRENCY, curCurrency.getValueAsString());
    	                data.put(DataNameTokens.INV_CURRENCY_RATE, curValue.getValueAsString());
    	                getUiHandlers().saveOrUpdateCurrencyData(MainPagePresenter.signedInUser, data);
            		}
            	}
            }
        });
        
        buttonSet.setAlign(Alignment.CENTER);

        currencyDetailLayout.setMembers(warehouseDetailForm, buttonSet);
        currencyDetailWindow.addItem(currencyDetailLayout);

        return currencyDetailWindow;
    }

    @Override
    public void loadAllCurrencyData(DataSource dataSource) {
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        currencyListGrid.setDataSource(dataSource);
        currencyListGrid.setAutoFetchData(Boolean.TRUE);
        currencyListGrid.setFields(listGridField);
        currencyListGrid.setDataSource(dataSource);
        currencyListGrid.getField(DataNameTokens.INV_CURRENCY_ID).setHidden(Boolean.TRUE);
        currencyListGrid.setAutoFitData(Autofit.BOTH);

        currencyLayout.setMembers(currencyToolStrip, currencyListGrid);
    }

    @Override
    public void refreshAllCurrencyData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                currencyListGrid.setData(response.getData());
            }
        };

        currencyListGrid.getDataSource().fetchData(currencyListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return currencyLayout;
    }

	@Override
	public Window getDetailWindow() {
		return currencyDetailWindow;
	}
}