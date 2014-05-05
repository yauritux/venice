package com.gdn.venice.client.app.seattle.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.seattle.data.SeattleData;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatETDUiHandlers;
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
 * Presenter for SeatETDPresenter
 * 
 * @author Arifin
 */
public class SeatETDPresenter extends Presenter<SeatETDPresenter.MyView, SeatETDPresenter.MyProxy>implements SeatETDUiHandlers {
	private final DispatchAsync dispatcher;
	public final static String seatETDPresenterServlet = "SeatETDPresenterServlet";

	/**
	 * {@link SeatETDPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.seattleETDViewer)
	public interface MyProxy extends Proxy<SeatETDPresenter>, Place {
	}

	/**
	 * {@link SeatETDPresenter}'s view.
	 */
	public interface MyView extends View, HasUiHandlers<SeatETDUiHandlers> {
		void loadData(DataSource dataSource);
		
	}

	@Inject
	public SeatETDPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadData(SeattleData.getSKUData());
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea,
				this);
	}
	
}
