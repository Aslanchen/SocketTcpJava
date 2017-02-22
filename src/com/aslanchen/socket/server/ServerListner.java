package com.aslanchen.socket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ServerListner {
	void ClientConnected(SocketChannel channel);

	void ClientDisconnected(SocketChannel channel);

	void DataReceived(SocketChannel channel, ByteBuffer buffer);

	void OtherException(SocketChannel channel, IOException ex);
}
