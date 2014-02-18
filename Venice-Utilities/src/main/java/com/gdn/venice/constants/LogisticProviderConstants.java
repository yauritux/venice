package com.gdn.venice.constants;

public enum LogisticProviderConstants {
	JNE(2),
	NCS(1),
	RPX(0),
	MSG(4);
	
	private int code;
	
	private LogisticProviderConstants(int code){
		this.code = code;
	}
	
	public int code(){
		return code;
	}
	
}
