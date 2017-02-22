package com.aslanchen.socket;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.aslanchen.socket.model.DataModel;

/**
 * @Description: 接收线程
 * @author Aslanchen
 * 
 */
public class SocketInThread extends Thread {
	private Boolean RUN = false;
	protected ConcurrentLinkedQueue<DataModel> clQueue = new ConcurrentLinkedQueue<DataModel>();

	public SocketInThread() {
		setDaemon(true);
		setName("SocketInThread");
	}

	/**
	 * 加入队列
	 */
	public boolean enqueue(DataModel message) {
		boolean result = clQueue.add(message);
		return result;
	}

	/**
	 * 队列取出
	 */
	private DataModel dequeue() {
		DataModel m = clQueue.poll();
		return m;
	}

	/**
	 * 唤醒消息处理线程
	 */
	public void wakeup() {
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public synchronized void start() {
		RUN = true;
		super.start();
	}

	public void Stop() {
		RUN = false;
		try {
			this.interrupt();
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			this.notifyAll();
		}

		while (RUN) {
			HandleEvent();
			WaitMsg();
		}
	}

	private void WaitMsg() {
		synchronized (this) {
			try {
				this.wait();
			} catch (java.lang.InterruptedException e) {

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void HandleEvent() {
		DataModel model = null;
		while (true) {
			model = dequeue();
			if (model == null) {
				return;
			}

			SocketManager.Instance().OnMsg(model);
		}
	}
}
