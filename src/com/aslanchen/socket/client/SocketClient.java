package com.aslanchen.socket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Aslanchen
 *
 */
public class SocketClient {
	private Selector selector;
	private SocketChannel channel;
	private ClientListner listner;
	private ByteBuffer bufferRead = ByteBuffer.allocate(1024);
	private Boolean isOpen = false;
	private Boolean isStartListen = false;
	private Thread thread;

	public void setListner(ClientListner listner) {
		this.listner = listner;
	}

	/**
	 * 获得一个Socket通道，并对该通道做一些初始化的工作
	 * 
	 * @param ip
	 *            连接的服务器的ip
	 * @param port
	 *            连接的服务器的端口号
	 * @throws IOException
	 */
	public void ConnectServer(String ip, int port) throws IOException {
		if (isOpen == true) {
			return;
		}

		// 获得一个Socket通道
		channel = SocketChannel.open();
		// 设置通道为非阻塞
		channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();

		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
		// 用channel.finishConnect();才能完成连接
		channel.connect(new InetSocketAddress(ip, port));
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
		channel.register(selector, SelectionKey.OP_CONNECT);
		isOpen = true;
	}

	/**
	 * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	 */
	public void StartListen() {
		if (isStartListen == true) {
			return;
		}

		isStartListen = true;
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (isStartListen == true) {
					try {
						selector.select();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// 获得selector中选中的项的迭代器
					Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
					while (ite.hasNext()) {
						SelectionKey key = (SelectionKey) ite.next();
						// 删除已选的key,以防重复处理
						ite.remove();
						// 连接事件发生
						if (key.isConnectable()) {
							channel = (SocketChannel) key.channel();
							// 如果正在连接，则完成连接
							if (channel.isConnectionPending()) {
								try {
									channel.finishConnect();
								} catch (IOException e) {
									e.printStackTrace();
								}

								if (listner != null) {
									listner.ServerConnected();
								}
							}

							// 设置成非阻塞
							try {
								channel.configureBlocking(false);
								// 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
								channel.register(selector, SelectionKey.OP_READ);
							} catch (IOException e) {
								e.printStackTrace();
							}
							// 获得了可读的事件
						} else if (key.isReadable()) {
							OnRead(key);
						}
					}
				}
			}
		});
		thread.start();
	}

	/**
	 * 处理读取服务端发来的信息 的事件
	 * 
	 * @param key
	 */
	public void OnRead(SelectionKey key) {
		// 服务器可读取消息:得到事件发生的Socket通道
		channel = (SocketChannel) key.channel();
		try {
			int read = channel.read(bufferRead);
			if (read == -1) {
				Close();
				if (listner != null) {
					listner.ServerDisconnected();
				}
				return;
			}

			// 保存bytebuffer状态
			int position = bufferRead.position();
			int limit = bufferRead.limit();
			bufferRead.flip();
			// 判断数据长度是否够首部长度
			if (bufferRead.remaining() < 4) {
				bufferRead.position(position);
				bufferRead.limit(limit);
			} else {
				// 高低位
				bufferRead.order(ByteOrder.LITTLE_ENDIAN);
				// 判断bytebuffer中剩余数据是否足够一个包
				int messageLen = bufferRead.getInt();
				if (bufferRead.remaining() >= messageLen) {
					byte[] data = new byte[messageLen];
					bufferRead.get(data);
					bufferRead.compact();
					ByteBuffer byteBuffer = ByteBuffer.wrap(data);
					if (listner != null) {
						listner.DataReceived(byteBuffer);
					}
				} else {
					bufferRead.position(position);
					bufferRead.limit(limit);
				}
			}
		} catch (IOException e) {
			Close();
			if (listner != null) {
				listner.ServerDisconnected();
			}
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param buffer
	 * @throws IOException
	 */
	public void SendMessage(ByteBuffer buffer) throws IOException {
		channel.write(buffer);
	}

	/**
	 * 关闭连接
	 */
	public void Close() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bufferRead.clear();
		thread = null;
		isOpen = false;
		isStartListen = false;
	}
}
