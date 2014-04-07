/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.presenter.command;

import com.gdn.venice.persistence.RafUserRole;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.AuthorizationUtil;

/**
 *
 * @author Maria Olivia
 */
public class FetchUserRoleCommand implements RafRpcCommand {

    String userName;

    /**
     * Basic copy constructor
     *
     * @param userName
     */
    public FetchUserRoleCommand(String userName) {
        super();
        this.userName = userName;
        if (this.userName == null || this.userName.isEmpty()) {
            this.userName = "roland";
        }
    }

    @Override
    public String execute() {
        StringBuilder sb = new StringBuilder();
        for (RafUserRole rafUserRole : AuthorizationUtil.getUserRoleList(userName)) {
            if (!sb.toString().isEmpty()) {
                sb.append(";");
            }
            sb.append(rafUserRole.getRafRole().getRoleName());
        }
        return sb.toString();
    }
}
