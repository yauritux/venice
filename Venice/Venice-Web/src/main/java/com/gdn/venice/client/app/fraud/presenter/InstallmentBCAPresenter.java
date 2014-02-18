package com.gdn.venice.client.app.fraud.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.fraud.data.FraudData;
import com.gdn.venice.client.app.fraud.view.handlers.InstallmentBCAUiHandlers;
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
 * Presenter for installment BCA
 * 
 * @author Roland
 */

public class InstallmentBCAPresenter
		extends
		Presenter<InstallmentBCAPresenter.MyView, InstallmentBCAPresenter.MyProxy>
		implements InstallmentBCAUiHandlers {
	@SuppressWarnings("unused")
	private final DispatchAsync dispatcher;

	public final static String installmentBCAPresenterServlet = "InstallmentBCAPresenterServlet";
	/**
	 * {@link InstallmentBCAPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.fraudInstallmentBCA)
	public interface MyProxy extends Proxy<InstallmentBCAPresenter>, Place {
	}

	/**
	 * {@link InstallmentBCAPresenter}'s view.
	 */
	public interface MyView extends View,
			HasUiHandlers<InstallmentBCAUiHandlers> {
		public void loadInstallmentBCAData(DataSource convertInstallmentDS, DataSource cancelInstallmentDS);
		void refreshConvertInstallmentData();
	}

	@Inject
	public InstallmentBCAPresenter(EventBus eventBus, MyView view,
			MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadInstallmentBCAData(FraudData.getConvertInstallmentData(), FraudData.getCancelInstallmentData());
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
}
