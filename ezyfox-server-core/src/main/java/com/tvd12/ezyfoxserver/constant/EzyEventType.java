package com.tvd12.ezyfoxserver.constant;

import com.tvd12.ezyfox.constant.EzyConstant;
import com.tvd12.ezyfox.util.EzyEnums;

import lombok.Getter;

public enum EzyEventType implements EzyConstant {

	SERVER_READY(1),
	USER_HANDSHAKE(20),
	USER_LOGIN(21),
    USER_ACCESS_APP(25),
    USER_ADDED(26),
    USER_REMOVED(27),
    SESSION_REMOVED(35);
    
	@Getter
	private final int id;
	
	private EzyEventType(int id) {
		this.id = id;
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
	public static EzyEventType valueOf(int id) {
		return EzyEnums.valueOf(values(), id);
	}
	
}
