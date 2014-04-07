package com.gdn.venice.inbound.commands;

import com.gdn.venice.exception.VeniceInternalException;

/**
 * 
 * @author yauritux
 *
 */
public interface Command {
	
	public void execute() throws VeniceInternalException;
}
