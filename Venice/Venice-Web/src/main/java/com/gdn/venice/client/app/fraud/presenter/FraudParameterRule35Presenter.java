package com.gdn.venice.client.app.fraud.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.fraud.data.FraudData;
import com.gdn.venice.client.app.fraud.view.handlers.FraudParameterRule35UiHandlers;
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
 * Presenter for Fraud Parameter 35 - Customer Grey List
 *
 */

public class FraudParameterRule35Presenter extends Presenter<FraudParameterRule35Presenter.MyView, FraudParameterRule35Presenter.MyProxy>
		implements FraudParameterRule35UiHandlers {
	@SuppressWarnings("unused")
	private final DispatchAsync dispatcher;

	public final static String fraudParameterRule35PresenterServlet = "FraudParameterRule35PresenterServlet";
	/**
	 * {@link FraudParameterRule35Presenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.fraudParameterRule35Page)
	public interface MyProxy extends Proxy<FraudParameterRule35Presenter>, Place {
	}

	/**
	 * {@link FraudParameterRule35Presenter}'s view.
	 */
	public interface MyView extends View, HasUiHandlers<FraudParameterRule35UiHandlers> {
		public void loadFraudParameterRule35Data(DataSource dataSource);
		void refreshFraudParameterRule35Data();
	}

	@Inject
	public FraudParameterRule35Presenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadFraudParameterRule35Data(FraudData.getFraudParameterRule35Data());
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
}
