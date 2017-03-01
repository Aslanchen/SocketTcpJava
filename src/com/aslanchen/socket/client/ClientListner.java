package com.aslanchen.socket.client;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ClientListner {
	void ServerConnected();

	void ServerConnectedException(IOException ex);

	void ServerDisconnected();

	void DataReceived(ByteBuffer buffer);
}
