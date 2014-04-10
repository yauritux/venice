package com.gdn.venice.client.app.inventory.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.GRNData;
import com.gdn.venice.client.app.inventory.view.GRNListView;
import com.gdn.venice.client.app.inventory.view.handler.GRNListUiHandler;
import com.gdn.venice.client.presenter.MainPagePresenter;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.client.DispatchAsync;
import com.gwtplatform.mvp.client.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.grid.ListGrid;

/**
 * Presenter for GRN List
 * 
 * @author Roland
 */
public class GRNListPresenter extends Presenter<GRNListPresenter.MyView, GRNListPresenter.MyProxy>
		implements GRNListUiHandler {
	
	GRNListView view;
	
	public static final String grnManagementPresenterServlet = "GRNManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.grnListPage)
	public interface MyProxy extends Proxy<GRNListPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<GRNListUiHandler> {
		public void loadGRNData(DataSource dataSource);
		public void refreshGRNData();
		public ListGrid getAttributeGrid();
		public Window getAttributeWindow();
		public Window buildAttributeWindow(String asnItemId, String itemId, DataSourceField[] dataSourceFields);
	}

	@Inject
	public GRNListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadGRNData(GRNData.getGRNData(1, 20));
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
}