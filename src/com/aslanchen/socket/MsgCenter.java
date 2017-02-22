package com.aslanchen.socket;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aslanchen
 *
 */
public class MsgCenter {
	private Map<Short, MsgCallback> _processor = new HashMap<Short, MsgCallback>();

	private static MsgCenter instance;

	public static MsgCenter Instance() {
		if (instance == null) {
			instance = new MsgCenter();
		}
		return instance;
	}

	public void OnMsg(Short msg_type, byte[] data) {
		if (_processor.containsKey(msg_type)) {
			_processor.get(msg_type).OnMsg(data);
		}
	}

	public void RegisterMsg(Short msg_type, MsgCallback call) {
		_processor.put(msg_type, call);
	}

	public void UnregisterMsg(Short msg_type) {
		_processor.remove(msg_type);
	}

	public interface MsgCallback {
		void OnMsg(byte[] data);
	}
}
