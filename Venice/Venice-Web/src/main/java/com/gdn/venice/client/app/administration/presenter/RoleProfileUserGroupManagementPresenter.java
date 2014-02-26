package com.gdn.venice.client.app.administration.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.administration.data.AdministrationData;
import com.gdn.venice.client.app.administration.view.handlers.RoleProfileUserGroupManagementUiHandlers;
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
 * Presenter for Role Profile User Group Management
 * 
 * @author Henry Chandra
 */
public class RoleProfileUserGroupManagementPresenter
		extends
		Presenter<RoleProfileUserGroupManagementPresenter.MyView, RoleProfileUserGroupManagementPresenter.MyProxy>
		implements RoleProfileUserGroupManagementUiHandlers {
	@SuppressWarnings("unused")
	private final DispatchAsync dispatcher;

	public final static String roleProfileUserGroupManagementPresenterServlet = "RoleProfileUserGroupManagementPresenterServlet";
	
	/**
	 * {@link RoleProfileUserGroupManagementPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.adminRoleProfileUserGroup)
	public interface MyProxy extends Proxy<RoleProfileUserGroupManagementPresenter>, Place {
	}

	/**
	 * {@link RoleProfileUserGroupManagementPresenter}'s view.
	 */
	public interface MyView extends View,
			HasUiHandlers<RoleProfileUserGroupManagementUiHandlers> {
		public void loadAdministrationData(DataSource dsProfile, DataSource dsGroup);
		void refreshProfileData();
		void refreshUserData();
		void refreshGroupData();
		void refreshRoleData();
	}

	@Inject
	public RoleProfileUserGroupManagementPresenter(EventBus eventBus, MyView view,
			MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadAdministrationData(AdministrationData.getProfileData(), AdministrationData.getGroupData());
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
}
