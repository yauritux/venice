package com.gdn.venice.client.app.inventory.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.ASNData;
import com.gdn.venice.client.app.inventory.view.ASNListView;
import com.gdn.venice.client.app.inventory.view.handler.ASNListUiHandler;
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

/**
 * Presenter for ASN List
 * 
 * @author Roland
 */
public class ASNListPresenter extends Presenter<ASNListPresenter.MyView, ASNListPresenter.MyProxy>
		implements ASNListUiHandler {
	
	ASNListView view;
	
	public static final String asnManagementPresenterServlet = "ASNManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.asnListPage)
	public interface MyProxy extends Proxy<ASNListPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<ASNListUiHandler> {
		public void loadASNData(DataSource dataSource);
		public void refreshASNData();
	}

	@Inject
	public ASNListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadASNData(ASNData.getASNData(1, 20));
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
}