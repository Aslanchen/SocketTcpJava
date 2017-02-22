package com.aslanchen.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Aslanchen
 *
 */
public class SocketServer {
	private Selector selector;
	private ServerListner listner;
	private List<SocketChannel> clients = new ArrayList<SocketChannel>();
	private ByteBuffer bufferRead = ByteBuffer.allocate(1024);
	private Boolean isOpen = false;
	private Boolean isStartListen = false;
	private Lock lock = new ReentrantLock();
	private Thread thread;

	public void setListner(ServerListner listner) {
		this.listner = listner;
	}

	/**
	 * 开启服务
	 * 
	 * @param port
	 * @throws IOException
	 */
	public void OpenServer(int port) throws IOException {
		if (isOpen == true) {
			return;
		}

		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到port端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		isOpen = true;
	}

	/**
	 * 开始监听
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
					// 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
					try {
						selector.select();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// 获得selector中选中的项的迭代器，选中的项为注册的事件
					Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
					while (ite.hasNext()) {
						SelectionKey key = (SelectionKey) ite.next();
						// 删除已选的key,以防重复处理
						ite.remove();
						// 客户端请求连接事件
						if (key.isAcceptable()) {
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							// 获得和客户端连接的通道
							SocketChannel channel = null;
							try {
								channel = server.accept();
							} catch (IOException e) {
								e.printStackTrace();
							}

							if (channel == null) {
								continue;
							}

							lock.lock();
							clients.add(channel);
							lock.unlock();
							if (listner != null) {
								listner.ClientConnected(channel);
							}

							try {
								// 设置成非阻塞
								channel.configureBlocking(false);
								// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
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
	 * 开始接收数据
	 * 
	 * @param key
	 */
	public void OnRead(SelectionKey key) {
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		bufferRead.clear();
		try {
			int read = channel.read(bufferRead);
			if (read == -1) {
				CloseClient(channel);
				if (listner != null) {
					listner.ClientDisconnected(channel);
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
						listner.DataReceived(channel, byteBuffer);
					}
				} else {
					bufferRead.position(position);
					bufferRead.limit(limit);
				}
			}
		} catch (IOException e) {
			CloseClient(channel);
			if (listner != null) {
				listner.ClientDisconnected(channel);
			}
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param channel
	 * @param buffer
	 * @throws IOException
	 */
	public void SendMessage(SocketChannel channel, ByteBuffer buffer) throws IOException {
		channel.write(buffer);
	}

	/**
	 * 停止
	 */
	public void Stop() {
		CloseAllClient();
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

	/**
	 * 关闭客户端
	 * 
	 * @param channel
	 */
	public void CloseClient(SocketChannel channel) {
		lock.lock();
		clients.remove(channel);
		lock.unlock();

		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭所有客户端
	 */
	public void CloseAllClient() {
		while (clients.size() > 0) {
			CloseClient(clients.get(0));
		}
	}
}
