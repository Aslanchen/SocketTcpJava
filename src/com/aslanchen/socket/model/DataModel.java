package com.aslanchen.socket.model;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DataModel {
	private SocketChannel channel;
	private ByteBuffer buffer;

	public DataModel(SocketChannel channel, ByteBuffer buffer) {
		this.channel = channel;
		this.buffer = buffer;
	}

	public DataModel(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
}
