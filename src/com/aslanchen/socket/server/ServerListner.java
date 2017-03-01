package com.aslanchen.socket.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ServerListner {
	void ClientConnected(SocketChannel channel);

	void ClientDisconnected(SocketChannel channel);

	void DataReceived(SocketChannel channel, ByteBuffer buffer);
}
