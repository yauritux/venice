package com.gdn.venice.client.app.administration.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.administration.data.AdministrationData;
import com.gdn.venice.client.app.administration.presenter.RoleProfileUserGroupManagementPresenter;
import com.gdn.venice.client.app.administration.view.handlers.RoleProfileUserGroupManagementUiHandlers;
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
import com.smartgwt.client.data.Record;
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.HiddenItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.tree.Tree;

/**
 * View for Role Profile User Department Management
 * 
 * @author Roland
 */

public class RoleProfileUserGroupManagementView extends
ViewWithUiHandlers<RoleProfileUserGroupManagementUiHandlers> implements
RoleProfileUserGroupManagementPresenter.MyView {
	@SuppressWarnings("unused")
	private static final int TITLE_HEIGHT = 20;
	
	RafViewLayout roleProfileUserGroupLayout;
	String status="";
	VLayout profileDetailLayout;
	VLayout userDetailLayout;
	VLayout roleDetailLayout;
	VLayout groupDetailLayout;
	
	Tree roleTree;

	ListGrid profileListGrid = new ListGrid();
	ListGrid profileDetailListGrid = new ListGrid();
	ListGrid userListGrid = new ListGrid();
	ListGrid groupListGrid = new ListGrid();
	ListGrid groupDetailListGrid = new ListGrid();
	ListGrid userDetailGroupListGrid = new ListGrid();
	ListGrid userDetailRoleListGrid = new ListGrid();
	ListGrid roleListGrid = new ListGrid();
	ListGrid roleDetailUserListGrid = new ListGrid();
	ListGrid roleDetailProfileListGrid = new ListGrid();
	
	DynamicForm userForm = new DynamicForm();
	Window userDetailWindow = null;
	
	DynamicForm roleForm = new DynamicForm();
	Window roleDetailWindow = null;

	@Inject
	public RoleProfileUserGroupManagementView() {
		roleProfileUserGroupLayout = new RafViewLayout();
		
		TabSet roleProfileUserGroupTabSet = new TabSet();
		roleProfileUserGroupTabSet.setTabBarPosition(Side.TOP);
		roleProfileUserGroupTabSet.setWidth100();
		roleProfileUserGroupTabSet.setHeight100();
		
		roleProfileUserGroupTabSet.addTab(buildRoleTab());
		roleProfileUserGroupTabSet.addTab(buildProfileTab());
		roleProfileUserGroupTabSet.addTab(buildUserTab());
		roleProfileUserGroupTabSet.addTab(buildGroupTab());
		
		roleProfileUserGroupLayout.setMembers(roleProfileUserGroupTabSet);
	}

	private Tab buildUserTab() {
		Tab userTab = new Tab("User");		
		HLayout userLayout = new HLayout();
		
		VLayout userListLayout = new VLayout();
		userListLayout.setWidth("40%");
		userListLayout.setShowResizeBar(true);
		
		ToolStrip userToolStrip = new ToolStrip();		
		userToolStrip.setWidth100();
		
		ToolStripButton addButton = new ToolStripButton();  
		addButton.setIcon("[SKIN]/icons/user_add.png");  
		addButton.setTooltip("Add New User");
		addButton.setTitle("Add");
		
		ToolStripButton removeButton = new ToolStripButton();  
		removeButton.setIcon("[SKIN]/icons/user_delete.png");  
		removeButton.setTooltip("Delete Current User");
		removeButton.setTitle("Remove");
		
		userToolStrip.addButton(addButton);
		userToolStrip.addButton(removeButton);	
		
		DataSource ds = AdministrationData.getUserData();
			
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
		ListGridField editField = new ListGridField("EDITFIELD", "Edit");  
		editField.setWidth(35);
		editField.setCanFilter(false);
		editField.setCanSort(false);
		editField.setAlign(Alignment.CENTER);
        ListGridField finalListGridField[] = {editField, listGridField[0], listGridField[1], listGridField[2], listGridField[3], listGridField[4]};
               		
		userListGrid = new ListGrid()  {  
			@Override
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {
				if (this.getFieldName(colNum).equalsIgnoreCase("editfield")) {
					ImgButton editImg = new ImgButton();
					editImg.setShowDown(false);
					editImg.setShowRollOver(false);
					editImg.setLayoutAlign(Alignment.CENTER);
					editImg.setSrc("[SKIN]/icons/note_edit.png");
					editImg.setPrompt("Edit User");
					editImg.setHeight(16);
					editImg.setWidth(16);
					editImg.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							HashMap<String, String> params = new HashMap<String, String>();
							params.put(DataNameTokens.RAFUSER_USERID, record.getAttribute(DataNameTokens.RAFUSER_USERID));
							userForm.getDataSource().setDefaultParams(params);
							userForm.fetchData(null, new DSCallback() {
								@Override
								public void execute(DSResponse response, Object rawData, DSRequest request) {									
									userDetailWindow.setTitle("Edit User");									
									userDetailWindow.show();
								}
							});
						}
					});

					return editImg;
				} else {
					return null;
				}
			}
		};
				
		userListGrid.setDataSource(ds);
		userListGrid.setFields(finalListGridField);
		userListGrid.setAutoFetchData(true);
		userListGrid.setShowFilterEditor(true);
		userListGrid.setSelectionType(SelectionStyle.SIMPLE);
		userListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		userListGrid.setShowRowNumbers(true);
		userListGrid.setSortField(1);
		userListGrid.setShowRecordComponents(true);          
		userListGrid.setShowRecordComponentsByCell(true); 
		
		userListGrid.getField(DataNameTokens.RAFUSER_USERID).setHidden(true);
				
		userListGrid.addSelectionChangedHandler(new SelectionChangedHandler() {		
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord[] selectedRecords = userListGrid.getSelection();
				if (selectedRecords.length==1) {
					Record record = selectedRecords[0];
					showUserDetail(record.getAttributeAsString(DataNameTokens.RAFUSER_USERID));
				} else {
					HTMLFlow userDetailFlow = new HTMLFlow();
					userDetailFlow.setAlign(Alignment.CENTER);
					userDetailFlow.setWidth100();
					if (selectedRecords.length==0) {
						userDetailFlow.setContents("<h2 align=\"center\">Please select user to show the user detail</h2>");
					} else if (selectedRecords.length>1) {
						userDetailFlow.setContents("<h2 align=\"center\">More than one user selected, please select only one user to show the user detail</h2>");
					}
					userDetailLayout.setMembers(userDetailFlow);
				}
			}
		});
		
		userListLayout.setMembers(userToolStrip, userListGrid);		
		userDetailLayout = new VLayout();		
		userLayout.setMembers(userListLayout, userDetailLayout);		
		userTab.setPane(userLayout);
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				userDetailWindow.setTitle("Add User");
				userDetailWindow.show();
			}
		});
		
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value) {
							userListGrid.removeSelectedData(new DSCallback() {
								@Override
								public void execute(DSResponse response, Object rawData, DSRequest request) {
									userListGrid.clearCriteria();
									refreshUserData();
									SC.say("Data Removed");
								}
							}, null);
						}
					}
				});
			}
		});
		
		userDetailWindow = buildUserWindow();
									
		return userTab;
	}
	
	private Window buildUserWindow() {
		final Window addEditUserWindow = new Window();
		addEditUserWindow.setWidth(400);
		addEditUserWindow.setHeight(200);
		addEditUserWindow.setShowMinimizeButton(false);
		addEditUserWindow.setIsModal(true);
		addEditUserWindow.setShowModalMask(true);
		addEditUserWindow.centerInPage();
		
		ToolStripButton saveButton = new ToolStripButton();
		saveButton.setIcon("[SKIN]/icons/save.png");
		saveButton.setTitle("Save");
		
		ToolStripButton cancelButton = new ToolStripButton();
		cancelButton.setIcon("[SKIN]/icons/delete.png");
		cancelButton.setTitle("Cancel");

		ToolStrip userDetailToolStrip = new ToolStrip();
		userDetailToolStrip.setWidth100();
		userDetailToolStrip.addButton(saveButton);
		userDetailToolStrip.addButton(cancelButton);

		final DataSource userDetailData = AdministrationData.getUserDetailData();
		
		//Request Department Combo
		RPCRequest requestGroup = new RPCRequest();
		requestGroup.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchGroupComboBoxData&type=RPC");
		requestGroup.setHttpMethod("POST");
		requestGroup.setUseSimpleHttp(true);
		requestGroup.setShowPrompt(false);
		
		final ComboBoxItem deptItem = new ComboBoxItem(DataNameTokens.RAFUSER_DEPARTMENT);
		
		RPCManager.sendRequest(requestGroup, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponseGroup = rawData.toString();
						String xmlDataGroup = rpcResponseGroup;
						
						LinkedHashMap<String, String> deptMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataGroup));
						deptItem.setValueMap(deptMap);
					}
		});
		
		userForm.setDataSource(userDetailData);
		userForm.setUseAllDataSourceFields(false);
		userForm.setNumCols(2);
		
		HiddenItem userIdItem = new HiddenItem(DataNameTokens.RAFUSER_USERID);
		
		TextItem loginNameItem = new TextItem(DataNameTokens.RAFUSER_LOGINNAME);
		TextItem nameItem = new TextItem(DataNameTokens.RAFUSER_NAME);   
				
		RadioGroupItem addToStockholm = new RadioGroupItem(DataNameTokens.RAFUSER_ADDTOSTOCKHOLM);
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();  
		map.put("true", "Yes");
		map.put("false", "No");
		addToStockholm.setWidth("140");
		addToStockholm.setValueMap(map);
				
		userForm.setFields(
				userIdItem,
				loginNameItem,
				nameItem,
				addToStockholm,
				deptItem
			);
		
		//validation
		//length
		LengthRangeValidator lengthRangeValidator = new LengthRangeValidator();  
		lengthRangeValidator.setMax(100);  
		userForm.getField(DataNameTokens.RAFUSER_LOGINNAME).setValidators(lengthRangeValidator);
		userForm.getField(DataNameTokens.RAFUSER_NAME).setValidators(lengthRangeValidator);
		 
		//required
		userForm.getField(DataNameTokens.RAFUSER_LOGINNAME).setRequired(true);
		userForm.getField(DataNameTokens.RAFUSER_NAME).setRequired(true);		
		userForm.getField(DataNameTokens.RAFUSER_DEPARTMENT).setRequired(true);

		 
		VLayout userLayout = new VLayout();
		userLayout.setHeight100();
		userLayout.setWidth100();
		userLayout.setMembers(userDetailToolStrip, userForm);
		addEditUserWindow.addItem(userLayout);

		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to save this data?", new BooleanCallback() {
					@Override
					public void execute(Boolean value) {
						if(value != null && value ==  true) {	
							if(addEditUserWindow.getTitle().toString().toLowerCase().trim().startsWith("add")){
								userForm.setSaveOperationType(DSOperationType.ADD);
							} else {
								userForm.setSaveOperationType(DSOperationType.UPDATE);
							}
							
							userForm.saveData(new DSCallback() {
								@Override
								public void execute(DSResponse response, Object rawData, DSRequest request) {
									//Refresh list gird dan hilangkan detail nya
									DSCallback callBack = new DSCallback() {
										@Override
										public void execute(DSResponse response, Object rawData, DSRequest request) {
											userListGrid.setData(response.getData());
											userForm.clearValues();
											addEditUserWindow.hide();
											refreshUserData();
											if(response.getStatus()==0){
												SC.say("Data Added/Edited");
											}
										}
									};								
									userListGrid.getDataSource().fetchData(userListGrid.getFilterEditorCriteria(), callBack);
								}
							});
						}
					}
				});
			}
		});
		
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				userForm.clearValues();
				addEditUserWindow.hide();
			}
		});
		
		addEditUserWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				userForm.clearValues();
				addEditUserWindow.hide();
			}
		});

		return addEditUserWindow;
	}
	
	private void showUserDetail(final String userId) {
		VLayout layout = new VLayout();
		
		HLayout userDetailHLayout = new HLayout();		
		VLayout userDetailLeftLayout = new VLayout();		
		DynamicForm userDetailForm = new DynamicForm();
				
		ToolStrip assignedGroupToolStrip = new ToolStrip();
		assignedGroupToolStrip.setWidth100();
		
		ToolStrip assignedRoleToolStrip = new ToolStrip();
		assignedRoleToolStrip.setWidth100();
		
		ToolStripButton addAssignedRoleButton = new ToolStripButton();  
		addAssignedRoleButton.setIcon("[SKIN]/icons/business_user_add.png");  
		addAssignedRoleButton.setTooltip("Add");
		addAssignedRoleButton.setTitle("Add");
		
		ToolStripButton removeAssignedRoleButton = new ToolStripButton();  
		removeAssignedRoleButton.setIcon("[SKIN]/icons/business_user_delete.png");  
		removeAssignedRoleButton.setTooltip("Remove");
		removeAssignedRoleButton.setTitle("Remove");
		
		assignedRoleToolStrip.addButton(addAssignedRoleButton);
		assignedRoleToolStrip.addButton(removeAssignedRoleButton);
		
		addAssignedRoleButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				userDetailRoleListGrid.startEditingNew();
			}
		});
		
		userDetailRoleListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				userDetailRoleListGrid.saveAllEdits();
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Added/Edited");
				}
				refreshUserDetailRoleData();
			}
		});
		
		removeAssignedRoleButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){				
							userDetailRoleListGrid.removeSelectedData();
							SC.say("Data Removed");
							refreshUserDetailRoleData();
						}
					}
				});
			}
		});		
		
		//User detail role listgrid
		final DataSource userDetailRoleDataSource=AdministrationData.getUserDetailRoleData(userId);

		//Request User Combo

		RPCRequest requestUser = new RPCRequest();
		requestUser = new RPCRequest();
		requestUser.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchUserComboBoxData&type=RPC");
		requestUser.setHttpMethod("POST");
		requestUser.setUseSimpleHttp(true);
		requestUser.setShowPrompt(false);
		RPCManager.sendRequest(requestUser, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponseUser = rawData.toString();
						String xmlDataUser = rpcResponseUser;
						final Map<String, String> userMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataUser));
						
						//Request Role Combo
						RPCRequest requestRole = new RPCRequest();
						requestRole.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchRoleComboBoxData&type=RPC");
						requestRole.setHttpMethod("POST");
						requestRole.setUseSimpleHttp(true);
						requestRole.setShowPrompt(false);
						
						RPCManager.sendRequest(requestRole, 
								new RPCCallback () {
									public void execute(RPCResponse response,
											Object rawData, RPCRequest request) {
										String rpcResponseRole = rawData.toString();
										String xmlDataRole = rpcResponseRole;
										final Map<String, String> roleMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataRole));

										userDetailRoleListGrid.setDataSource(userDetailRoleDataSource);
										userDetailRoleListGrid.setFields(
												new ListGridField(DataNameTokens.RAFUSER_RAFUSERROLES_USERID),
												new ListGridField(DataNameTokens.RAFUSER_RAFUSERROLES_ROLEID)
										);										
										userDetailRoleListGrid.getField(DataNameTokens.RAFUSER_RAFUSERROLES_USERID).setValueMap(userMap);
										userDetailRoleListGrid.getField(DataNameTokens.RAFUSER_RAFUSERROLES_ROLEID).setValueMap(roleMap);
										userDetailRoleListGrid.getField(DataNameTokens.RAFUSER_RAFUSERROLES_USERID).setCanEdit(false);
										userDetailRoleListGrid.getField(DataNameTokens.RAFUSER_RAFUSERROLES_USERID).setDefaultValue(userId);
										userDetailRoleListGrid.getField(DataNameTokens.RAFUSER_RAFUSERROLES_ROLEID).setRequired(true);
										userDetailRoleListGrid.fetchData();
									}
						});
				}
		});

		userDetailRoleListGrid.setCanEdit(false);
		userDetailRoleListGrid.setSelectionType(SelectionStyle.SIMPLE);
		userDetailRoleListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		userDetailRoleListGrid.setShowRowNumbers(true);
		
		userDetailLeftLayout.setMembers(userDetailForm, assignedRoleToolStrip, userDetailRoleListGrid);
		userDetailHLayout.setMembersMargin(5);
		userDetailHLayout.setMembers(userDetailLeftLayout);
		layout.setMembers(userDetailHLayout);
		userDetailLayout.setMembers(layout);
	}
	
	private Tab buildRoleTab() {
		Tab roleTab = new Tab("Role");		
		HLayout roleLayout = new HLayout();
		
		VLayout roleListLayout = new VLayout();
		roleListLayout.setWidth("40%");
		roleListLayout.setShowResizeBar(true);
		
		ToolStrip roleToolStrip = new ToolStrip();		
		roleToolStrip.setWidth100();
		
		ToolStripButton addButton = new ToolStripButton();  
		addButton.setIcon("[SKIN]/icons/business_user_add.png");  
		addButton.setTooltip("Add New Role");
		addButton.setTitle("Add");
		
		ToolStripButton removeButton = new ToolStripButton();  
		removeButton.setIcon("[SKIN]/icons/business_user_delete.png");  
		removeButton.setTooltip("Delete Current Role");
		removeButton.setTitle("Remove");
		
		roleToolStrip.addButton(addButton);
		roleToolStrip.addButton(removeButton);
		
		DataSource ds = AdministrationData.getRoleData();
		
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
		ListGridField editField = new ListGridField("EDITFIELD", "Edit");  
		editField.setWidth(35);
		editField.setCanFilter(false);
		editField.setCanSort(false);
		editField.setAlign(Alignment.CENTER);
        ListGridField finalListGridField[] = {editField, listGridField[0], listGridField[1], listGridField[2], listGridField[3]};
               		
		roleListGrid = new ListGrid()  {  
			@Override
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {
				if (this.getFieldName(colNum).equalsIgnoreCase("editfield")) {
					ImgButton editImg = new ImgButton();
					editImg.setShowDown(false);
					editImg.setShowRollOver(false);
					editImg.setLayoutAlign(Alignment.CENTER);
					editImg.setSrc("[SKIN]/icons/note_edit.png");
					editImg.setPrompt("Edit Role");
					editImg.setHeight(16);
					editImg.setWidth(16);
					editImg.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							HashMap<String, String> params = new HashMap<String, String>();
							params.put(DataNameTokens.RAFROLE_ROLEID, record.getAttribute(DataNameTokens.RAFROLE_ROLEID));
							roleForm.getDataSource().setDefaultParams(params);
							roleForm.fetchData(null, new DSCallback() {
								@Override
								public void execute(DSResponse response, Object rawData, DSRequest request) {									
									roleDetailWindow.setTitle("Edit Role");									
									roleDetailWindow.show();
								}
							});
						}
					});

					return editImg;
				} else {
					return null;
				}
			}
		};
				
		roleListGrid.setDataSource(ds);
		roleListGrid.setFields(finalListGridField);
		roleListGrid.setAutoFetchData(true);
		roleListGrid.setCanEdit(true);
		roleListGrid.setShowFilterEditor(true);
		roleListGrid.setSelectionType(SelectionStyle.SIMPLE);
		roleListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		roleListGrid.setShowRowNumbers(true);
		roleListGrid.setSortField(1);
		roleListGrid.setShowRecordComponents(true);          
		roleListGrid.setShowRecordComponentsByCell(true); 
		
		roleListGrid.getField(DataNameTokens.RAFROLE_ROLEID).setHidden(true);
		
		roleListGrid.addSelectionChangedHandler(new SelectionChangedHandler() {		
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord[] selectedRecords = roleListGrid.getSelection();

				if (selectedRecords.length==1) {
					Record record = selectedRecords[0];
					showRoleDetail(record.getAttributeAsString(DataNameTokens.RAFROLE_ROLEID));
				} else {
					HTMLFlow roleDetailFlow = new HTMLFlow();
					roleDetailFlow.setAlign(Alignment.CENTER);
					roleDetailFlow.setWidth100();
					if (selectedRecords.length==0) {
						roleDetailFlow.setContents("<h2 align=\"center\">Please select role to show the role detail</h2>");
					} else if (selectedRecords.length>1) {
						roleDetailFlow.setContents("<h2 align=\"center\">More than one role selected, please select only one role to show the role detail</h2>");
					}
					roleDetailLayout.setMembers(roleDetailFlow);
				}
			}
		});
		
		roleListLayout.setMembers(roleToolStrip, roleListGrid);		
		roleDetailLayout = new VLayout();		
		roleLayout.setMembers(roleListLayout, roleDetailLayout);		
		roleTab.setPane(roleLayout);
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				roleDetailWindow.setTitle("Add Role");
				roleDetailWindow.show();
			}
		});		
				
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value) {
							roleListGrid.removeSelectedData(new DSCallback() {
								@Override
								public void execute(DSResponse response, Object rawData, DSRequest request) {
									roleListGrid.clearCriteria();
									refreshRoleData();
									SC.say("Data Removed");
								}
							}, null);
						}
					}
				});
			}
		});

		roleDetailWindow = buildRoleWindow();
		
		return roleTab;
	}
	
	private Window buildRoleWindow() {
		final Window addEditRoleWindow = new Window();
		addEditRoleWindow.setWidth(400);
		addEditRoleWindow.setHeight(200);
		addEditRoleWindow.setShowMinimizeButton(false);
		addEditRoleWindow.setIsModal(true);
		addEditRoleWindow.setShowModalMask(true);
		addEditRoleWindow.centerInPage();
		
		ToolStripButton saveButton = new ToolStripButton();
		saveButton.setIcon("[SKIN]/icons/save.png");
		saveButton.setTitle("Save");
		
		ToolStripButton cancelButton = new ToolStripButton();
		cancelButton.setIcon("[SKIN]/icons/delete.png");
		cancelButton.setTitle("Cancel");

		ToolStrip roleDetailToolStrip = new ToolStrip();
		roleDetailToolStrip.setWidth100();
		roleDetailToolStrip.addButton(saveButton);
		roleDetailToolStrip.addButton(cancelButton);

		DataSource roleDetailData = AdministrationData.getRoleDetailData();
		roleForm.setDataSource(roleDetailData);
		roleForm.setUseAllDataSourceFields(false);
		roleForm.setNumCols(2);
		
		HiddenItem roleIdItem = new HiddenItem(DataNameTokens.RAFROLE_ROLEID);
		
		TextItem codeItem = new TextItem(DataNameTokens.RAFROLE_ROLENAME);
		TextItem nameItem = new TextItem(DataNameTokens.RAFROLE_ROLEDESC);   
				
		RadioGroupItem addToStockholm = new RadioGroupItem(DataNameTokens.RAFROLE_ADDTOSTOCKHOLM);
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();  
		map.put("true", "Yes");
		map.put("false", "No");
		addToStockholm.setWidth("140");
		addToStockholm.setValueMap(map);

		roleForm.setFields(
				roleIdItem,
				codeItem,
				nameItem,
				addToStockholm
			);
		
		//validation
		//length
		LengthRangeValidator lengthRangeValidator = new LengthRangeValidator();  
		lengthRangeValidator.setMax(100);  
		roleForm.getField(DataNameTokens.RAFROLE_ROLENAME).setValidators(lengthRangeValidator);
		roleForm.getField(DataNameTokens.RAFROLE_ROLEDESC).setValidators(lengthRangeValidator);
		 
		//required
		roleForm.getField(DataNameTokens.RAFROLE_ROLENAME).setRequired(true);
		roleForm.getField(DataNameTokens.RAFROLE_ROLEDESC).setRequired(true);
		 
		VLayout roleLayout = new VLayout();
		roleLayout.setHeight100();
		roleLayout.setWidth100();
		roleLayout.setMembers(roleDetailToolStrip, roleForm);
		addEditRoleWindow.addItem(roleLayout);

		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to save this data?", new BooleanCallback() {
					@Override
					public void execute(Boolean value) {
						if(value != null && value ==  true) {	
							if(addEditRoleWindow.getTitle().toString().toLowerCase().trim().startsWith("add")){
								roleForm.setSaveOperationType(DSOperationType.ADD);
							} else {
								roleForm.setSaveOperationType(DSOperationType.UPDATE);
							}
							
							roleForm.saveData(new DSCallback() {
								@Override
								public void execute(DSResponse response, Object rawData, DSRequest request) {
									//Refresh list gird dan hilangkan detail nya
									DSCallback callBack = new DSCallback() {
										@Override
										public void execute(DSResponse response, Object rawData, DSRequest request) {
											roleListGrid.setData(response.getData());
											roleForm.clearValues();
											addEditRoleWindow.hide();
											refreshRoleData();
											if(response.getStatus()==0){
												SC.say("Data Added/Edited");
											}
										}
									};								
									roleListGrid.getDataSource().fetchData(roleListGrid.getFilterEditorCriteria(), callBack);
								}
							});
						}
					}
				});
			}
		});
		
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				roleForm.clearValues();
				addEditRoleWindow.hide();
			}
		});
		
		addEditRoleWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				roleForm.clearValues();
				addEditRoleWindow.hide();
			}
		});

		return addEditRoleWindow;
	}
	
	private void showRoleDetail(final String roleId) {
		ToolStrip roleDetailToolStrip = new ToolStrip();
		roleDetailToolStrip.setWidth100();
		
		ToolStrip profilesToolStrip = new ToolStrip();
		profilesToolStrip.setWidth100();
		
		ToolStripButton addProfilesButton = new ToolStripButton();  
		addProfilesButton.setIcon("[SKIN]/icons/she_user_add.png");  
		addProfilesButton.setTooltip("Add Profile");
		addProfilesButton.setTitle("Add");
		
		ToolStripButton removeProfilesButton = new ToolStripButton();  
		removeProfilesButton.setIcon("[SKIN]/icons/she_user_remove.png");  
		removeProfilesButton.setTooltip("Remove Profile");
		removeProfilesButton.setTitle("Remove");
		
		profilesToolStrip.addButton(addProfilesButton);
		profilesToolStrip.addButton(removeProfilesButton);
		
		addProfilesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				roleDetailProfileListGrid.startEditingNew();
			}
		});
		
		roleDetailProfileListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				roleDetailProfileListGrid.saveAllEdits();
					
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Added/Edited");
				}
				refreshRoleDetailProfileData();
			}
		});
		
		removeProfilesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){				
							roleDetailProfileListGrid.removeSelectedData();
							SC.say("Data Removed");
							refreshRoleDetailProfileData();
						}
					}
				});
			}
		});		
		
		//Role detail user listgrid
		final DataSource userDetailProfileDataSource=AdministrationData.getRoleDetailProfileData(roleId);

		//Request Profile Combo
		RPCRequest requestProfile = new RPCRequest();
		requestProfile.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchProfileComboBoxData&type=RPC");
		requestProfile.setHttpMethod("POST");
		requestProfile.setUseSimpleHttp(true);
		requestProfile.setShowPrompt(false);
		RPCManager.sendRequest(requestProfile, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponseProfile = rawData.toString();
						String xmlDataProfile = rpcResponseProfile;
						final Map<String, String> profileMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataProfile));
						
						//Request Role Combo
						RPCRequest requestRole = new RPCRequest();
						requestRole.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchRoleComboBoxData&type=RPC");
						requestRole.setHttpMethod("POST");
						requestRole.setUseSimpleHttp(true);
						requestRole.setShowPrompt(false);
						
						RPCManager.sendRequest(requestRole, 
								new RPCCallback () {
									public void execute(RPCResponse response,
											Object rawData, RPCRequest request) {
										String rpcResponseRole = rawData.toString();
										String xmlDataRole = rpcResponseRole;
										final Map<String, String> roleMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataRole));

										roleDetailProfileListGrid.setDataSource(userDetailProfileDataSource);
										roleDetailProfileListGrid.setFields(
												new ListGridField(DataNameTokens.RAFROLE_RAFROLEPROFILES_ROLEID),
												new ListGridField(DataNameTokens.RAFROLE_RAFROLEPROFILES_PROFILEID)
										);										
										roleDetailProfileListGrid.getField(DataNameTokens.RAFROLE_RAFROLEPROFILES_PROFILEID).setValueMap(profileMap);
										roleDetailProfileListGrid.getField(DataNameTokens.RAFROLE_RAFROLEPROFILES_ROLEID).setValueMap(roleMap);
										roleDetailProfileListGrid.getField(DataNameTokens.RAFROLE_RAFROLEPROFILES_ROLEID).setCanEdit(false);
										roleDetailProfileListGrid.getField(DataNameTokens.RAFROLE_RAFROLEPROFILES_ROLEID).setDefaultValue(roleId);
										roleDetailProfileListGrid.getField(DataNameTokens.RAFROLE_RAFROLEPROFILES_PROFILEID).setRequired(true);
										roleDetailProfileListGrid.fetchData();
									}
						});
				}
		});

		roleDetailProfileListGrid.setCanEdit(true);
		roleDetailProfileListGrid.setSelectionType(SelectionStyle.SIMPLE);
		roleDetailProfileListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		roleDetailProfileListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);
		roleDetailProfileListGrid.setShowRowNumbers(true);
		
		ToolStrip usersToolStrip = new ToolStrip();
		usersToolStrip.setWidth100();
		
		ToolStripButton addUsersButton = new ToolStripButton();  
		addUsersButton.setIcon("[SKIN]/icons/user_add.png");  
		addUsersButton.setTooltip("Add User");
		addUsersButton.setTitle("Add");
		
		ToolStripButton removeUsersButton = new ToolStripButton();  
		removeUsersButton.setIcon("[SKIN]/icons/user_delete.png");  
		removeUsersButton.setTooltip("Remove User");
		removeUsersButton.setTitle("Remove");
		
		usersToolStrip.addButton(addUsersButton);
		usersToolStrip.addButton(removeUsersButton);
		
		addUsersButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				roleDetailUserListGrid.startEditingNew();
			}
		});
		
		roleDetailUserListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				roleDetailUserListGrid.saveAllEdits();
					
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Added/Edited");
				}
				refreshRoleDetailUserData();
			}
		});
		
		removeUsersButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){				
							roleDetailUserListGrid.removeSelectedData();
							SC.say("Data Removed");
							refreshRoleDetailUserData();
						}
					}
				});
			}
		});		
		
		//Role detail user listgrid
		final DataSource roleDetailUserDataSource=AdministrationData.getRoleDetailUserData(roleId);

		//Request User Combo
		RPCRequest requestUser = new RPCRequest();
		requestUser.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchUserComboBoxData&type=RPC");
		requestUser.setHttpMethod("POST");
		requestUser.setUseSimpleHttp(true);
		requestUser.setShowPrompt(false);
		RPCManager.sendRequest(requestUser, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponseUser = rawData.toString();
						String xmlDataUser = rpcResponseUser;
						final Map<String, String> userMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataUser));
						
						//Request Role Combo
						RPCRequest requestRole = new RPCRequest();
						requestRole.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchRoleComboBoxData&type=RPC");
						requestRole.setHttpMethod("POST");
						requestRole.setUseSimpleHttp(true);
						requestRole.setShowPrompt(false);
						
						RPCManager.sendRequest(requestRole, 
								new RPCCallback () {
									public void execute(RPCResponse response,
											Object rawData, RPCRequest request) {
										String rpcResponseRole = rawData.toString();
										String xmlDataRole = rpcResponseRole;
										final Map<String, String> roleMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataRole));

										roleDetailUserListGrid.setDataSource(roleDetailUserDataSource);
										roleDetailUserListGrid.setFields(
												new ListGridField(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID),
												new ListGridField(DataNameTokens.RAFROLE_RAFUSERROLES_USERID)
										);										
										roleDetailUserListGrid.getField(DataNameTokens.RAFROLE_RAFUSERROLES_USERID).setValueMap(userMap);
										roleDetailUserListGrid.getField(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID).setValueMap(roleMap);
										roleDetailUserListGrid.getField(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID).setCanEdit(false);
										roleDetailUserListGrid.getField(DataNameTokens.RAFROLE_RAFUSERROLES_ROLEID).setDefaultValue(roleId);
										roleDetailUserListGrid.getField(DataNameTokens.RAFROLE_RAFUSERROLES_USERID).setRequired(true);
										roleDetailUserListGrid.fetchData();
									}
						});
				}
		});

		roleDetailUserListGrid.setCanEdit(false);
		roleDetailUserListGrid.setSelectionType(SelectionStyle.SIMPLE);
		roleDetailUserListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		roleDetailUserListGrid.setShowRowNumbers(true);
		
		roleDetailLayout.setMembers(roleDetailToolStrip, profilesToolStrip, roleDetailProfileListGrid, usersToolStrip, roleDetailUserListGrid);
	}
	
	private Tab buildProfileTab() {
		Tab profileTab = new Tab("Profile");
		
		HLayout profileLayout = new HLayout();		
		VLayout profileListLayout = new VLayout();
		profileListLayout.setWidth("40%");
		profileListLayout.setShowResizeBar(true);
		
		ToolStrip profileToolStrip = new ToolStrip();		
		profileToolStrip.setWidth100();
		
		ToolStripButton addButton = new ToolStripButton();  
		addButton.setIcon("[SKIN]/icons/she_user_add.png");  
		addButton.setTooltip("Add New Profile");
		addButton.setTitle("Add");
		
		ToolStripButton removeButton = new ToolStripButton();  
		removeButton.setIcon("[SKIN]/icons/she_user_remove.png");  
		removeButton.setTooltip("Delete Current Profile");
		removeButton.setTitle("Remove");
		
		profileToolStrip.addButton(addButton);
		profileToolStrip.addButton(removeButton);
		
		profileListGrid.setAutoFetchData(true);
		profileListGrid.setCanEdit(true);
		profileListGrid.setShowFilterEditor(true);
		profileListGrid.setSelectionType(SelectionStyle.SIMPLE);
		profileListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		profileListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);
		profileListGrid.setShowRowNumbers(true);

		profileListGrid.addSelectionChangedHandler(new SelectionChangedHandler() {		
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord[] selectedRecords = profileListGrid.getSelection();
				if (selectedRecords.length==1) {
					Record record = selectedRecords[0];
					showProfileDetail(record.getAttributeAsString(DataNameTokens.RAFPROFILE_PROFILEID));
				} else {
					HTMLFlow profileDetailFlow = new HTMLFlow();
					profileDetailFlow.setAlign(Alignment.CENTER);
					profileDetailFlow.setWidth100();
					if (selectedRecords.length==0) {
						profileDetailFlow.setContents("<h2 align=\"center\">Please select profile to show the profile detail</h2>");
					} else if (selectedRecords.length>1) {
						profileDetailFlow.setContents("<h2 align=\"center\">More than one profile selected, please select only one profile to show the profile detail</h2>");
					}
					profileDetailLayout.setMembers(profileDetailFlow);
				}
			}
		});
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				profileListGrid.startEditingNew();
			}
		});		

		profileListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				profileListGrid.saveAllEdits();
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Added/Edited");
				}	
				refreshProfileData();			
			}
		});
		
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){							
							profileListGrid.removeSelectedData();
							refreshProfileData();
							SC.say("Data Removed");
						}
					}
				});
			}
		});
		
		profileListLayout.setMembers(profileToolStrip, profileListGrid);		
		profileDetailLayout = new VLayout();		
		profileLayout.setMembers(profileListLayout, profileDetailLayout);
		profileTab.setPane(profileLayout);
		
		return profileTab;
	}
	
	private void showProfileDetail(final String profileId) {
		VLayout layout = new VLayout();		
		ToolStrip profileDetailToolStrip = new ToolStrip();
		profileDetailToolStrip.setWidth100();
		
		ToolStripButton addButton = new ToolStripButton();  
		addButton.setIcon("[SKIN]/icons/user_add.png");  
		addButton.setTooltip("Add");
		addButton.setTitle("Add");
		
		ToolStripButton removeButton = new ToolStripButton();  
		removeButton.setIcon("[SKIN]/icons/user_delete.png");  
		removeButton.setTooltip("Delete");
		removeButton.setTitle("Remove");
		
		profileDetailToolStrip.addButton(addButton);
		profileDetailToolStrip.addButton(removeButton);
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				profileDetailListGrid.startEditingNew();
			}
		});
		
		profileDetailListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				profileDetailListGrid.saveAllEdits();
					
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Added/Edited");
				}
				refreshProfileDetailData();
			}
		});
		
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Are you sure you want to delete this data?", new BooleanCallback() {					
					@Override
					public void execute(Boolean value) {
						if(value != null && value){				
							profileDetailListGrid.removeSelectedData();
							SC.say("Data Removed");
							refreshProfileDetailData();
						}
					}
				});
			}
		});

		//Profile detail listgrid
		final DataSource profileDetailDataSource=AdministrationData.getProfileDetailData(profileId);
		
		//Request Screen Combo
		RPCRequest requestScreen = new RPCRequest();
		requestScreen.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchScreenComboBoxData&type=RPC");
		requestScreen.setHttpMethod("POST");
		requestScreen.setUseSimpleHttp(true);
		requestScreen.setShowPrompt(false);
		RPCManager.sendRequest(requestScreen, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponseScreen = rawData.toString();
						String xmlDataScreen = rpcResponseScreen;
						final Map<String, String> screenMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataScreen));
						
						//Request Permission Type Combo
						RPCRequest requestPermissionType = new RPCRequest();
						requestPermissionType.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchPermissionComboBoxData&type=RPC");
						requestPermissionType.setHttpMethod("POST");
						requestPermissionType.setUseSimpleHttp(true);
						requestPermissionType.setShowPrompt(false);
						
						RPCManager.sendRequest(requestPermissionType, 
								new RPCCallback () {
									public void execute(RPCResponse response,
											Object rawData, RPCRequest request) {
										String rpcResponsePermissionType = rawData.toString();
										String xmlDataPermissionType = rpcResponsePermissionType;
										final Map<String, String> permissionTypeMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataPermissionType));

										//Request Profile Name Combo
										RPCRequest requestProfile = new RPCRequest();
										requestProfile.setActionURL(GWT.getHostPageBaseURL() + "RoleProfileUserGroupManagementPresenterServlet?method=fetchProfileComboBoxData&type=RPC");
										requestProfile.setHttpMethod("POST");
										requestProfile.setUseSimpleHttp(true);
										requestProfile.setShowPrompt(false);
										
										RPCManager.sendRequest(requestProfile, 
												new RPCCallback () {
													public void execute(RPCResponse response,
															Object rawData, RPCRequest request) {
														String rpcResponseProfile = rawData.toString();
														String xmlDataProfile = rpcResponseProfile;
														final Map<String, String> profileMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataProfile));
														
														profileDetailListGrid.setDataSource(profileDetailDataSource);
														profileDetailListGrid.setFields(
																new ListGridField(DataNameTokens.RAFPROFILEPERMISSION_PROFILEID),
																new ListGridField(DataNameTokens.RAFPROFILEPERMISSION_APPLICATIONOBJECTID),
																new ListGridField(DataNameTokens.RAFPROFILEPERMISSION_PERMISSIONTYPEID)
														);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_PROFILEID).setValueMap(profileMap);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_APPLICATIONOBJECTID).setValueMap(screenMap);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_PERMISSIONTYPEID).setValueMap(permissionTypeMap);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_PROFILEID).setCanEdit(false);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_PROFILEID).setDefaultValue(profileId);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_APPLICATIONOBJECTID).setRequired(true);
														profileDetailListGrid.getField(DataNameTokens.RAFPROFILEPERMISSION_PERMISSIONTYPEID).setRequired(true);
														profileDetailListGrid.fetchData();
													}
										});									
									}
						});
				}
		});

		profileDetailListGrid.setCanEdit(true);
		profileDetailListGrid.setSelectionType(SelectionStyle.SIMPLE);
		profileDetailListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		profileDetailListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);
		profileDetailListGrid.setShowRowNumbers(true);
		
		layout.setMembers(profileDetailToolStrip, profileDetailListGrid);
		profileDetailLayout.setMembers(layout);
	}
	
	private Tab buildGroupTab() {
		Tab groupTab = new Tab("Department");		
		HLayout groupLayout = new HLayout();
		
		VLayout groupListLayout = new VLayout();
		groupListLayout.setWidth("40%");
		groupListLayout.setShowResizeBar(true);
		
		ToolStrip groupToolStrip = new ToolStrip();		
		groupToolStrip.setWidth100();
		
		ToolStripButton addButton = new ToolStripButton();  
		addButton.setIcon("[SKIN]/icons/business_users_add.png");  
		addButton.setTooltip("Add New Department");
		addButton.setTitle("Add");
		addButton.setDisabled(true);
		
		ToolStripButton removeButton = new ToolStripButton();  
		removeButton.setIcon("[SKIN]/icons/business_users_delete.png");  
		removeButton.setTooltip("Delete Current Department");
		removeButton.setTitle("Remove");
		removeButton.setDisabled(true);
						
		groupListGrid.setAutoFetchData(true);
		groupListGrid.setCanEdit(false);
		groupListGrid.setShowFilterEditor(true);
		groupListGrid.setShowRowNumbers(true);
				
		groupListLayout.setMembers(groupToolStrip, groupListGrid);		
		groupDetailLayout = new VLayout();		
		groupLayout.setMembers(groupListLayout, groupDetailLayout);		
		groupTab.setPane(groupLayout);
		
		return groupTab;
	}
		
	@Override
	public Widget asWidget() {
		return roleProfileUserGroupLayout;
	}
	
	protected void bindCustomRoleUiHandlers() {
		roleListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshRoleData();
			}
		});
	}
	
	protected void bindCustomGroupUiHandlers() {
		groupListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshGroupData();
			}
		});
	}
	
	protected void bindCustomUserUiHandlers() {
		userListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshUserData();
			}
		});
	}
	
	protected void bindCustomProfileUiHandlers() {
		profileListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshProfileData();
			}
		});
	}
	
	public void refreshProfileData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				profileListGrid.setData(response.getData());
			}
		};
		
		profileListGrid.getDataSource().fetchData(profileListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshProfileDetailData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				profileDetailListGrid.setData(response.getData());
			}
		};
		
		profileDetailListGrid.getDataSource().fetchData(profileDetailListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshUserData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				userListGrid.setData(response.getData());
			}
		};
		
		userListGrid.getDataSource().fetchData(userListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshUserDetailRoleData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				userDetailRoleListGrid.setData(response.getData());
			}
		};
		
		userDetailRoleListGrid.getDataSource().fetchData(userDetailRoleListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshUserDetailGroupData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				userDetailGroupListGrid.setData(response.getData());
			}
		};
		
		userDetailGroupListGrid.getDataSource().fetchData(userDetailGroupListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshRoleData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				roleListGrid.setData(response.getData());
			}
		};
		
		roleListGrid.getDataSource().fetchData(roleListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshRoleDetailUserData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				roleDetailUserListGrid.setData(response.getData());
			}
		};
		
		roleDetailUserListGrid.getDataSource().fetchData(roleDetailUserListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshRoleDetailProfileData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				roleDetailProfileListGrid.setData(response.getData());
			}
		};
		
		roleDetailProfileListGrid.getDataSource().fetchData(roleDetailProfileListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshGroupData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				groupListGrid.setData(response.getData());
			}
		};
		
		groupListGrid.getDataSource().fetchData(groupListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshGroupDetailData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				groupDetailListGrid.setData(response.getData());
			}
		};
		
		groupDetailListGrid.getDataSource().fetchData(groupDetailListGrid.getFilterEditorCriteria(), callBack);
	}
	
	@Override
	public void loadAdministrationData(DataSource dsProfile, DataSource dsGroup) {	
		//validator
		LengthRangeValidator lengthRangeValidator100 = new LengthRangeValidator();  
		lengthRangeValidator100.setMax(100);  
		LengthRangeValidator lengthRangeValidator300 = new LengthRangeValidator();  
		lengthRangeValidator300.setMax(300);
				
		//populate profile listgrid
		profileListGrid.setDataSource(dsProfile);
		profileListGrid.setFields(
				new ListGridField(DataNameTokens.RAFPROFILE_PROFILENAME),
				new ListGridField(DataNameTokens.RAFPROFILE_PROFILEDESC)
		);
		
		//validation
		//required
		profileListGrid.getField(DataNameTokens.RAFPROFILE_PROFILENAME).setRequired(true);
		profileListGrid.getField(DataNameTokens.RAFPROFILE_PROFILEDESC).setRequired(true);
		
		//length
		profileListGrid.getField(DataNameTokens.RAFPROFILE_PROFILENAME).setValidators(lengthRangeValidator100);
		profileListGrid.getField(DataNameTokens.RAFPROFILE_PROFILEDESC).setValidators(lengthRangeValidator300);
		
		//populate group listgrid
		groupListGrid.setDataSource(dsGroup);
		groupListGrid.setFields(
				new ListGridField(DataNameTokens.RAFGROUP_GROUPNAME),
				new ListGridField(DataNameTokens.RAFGROUP_GROUPDESC)
		);
				
        bindCustomGroupUiHandlers();
        bindCustomUserUiHandlers();
        bindCustomProfileUiHandlers();
        bindCustomRoleUiHandlers();
	}
}