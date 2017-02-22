package com.aslanchen.socket;

import java.io.IOException;

public class MainTest {

	public static void main(String[] args) {
		TestClient();
		TestServer();
	}

	private static void TestClient() {
		try {
			SocketManager.Instance().InitClient("127.0.0.1", 6666);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SocketManager.Instance().StartListen();
	}

	private static void TestServer() {
		try {
			SocketManager.Instance().InitServer(7777);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SocketManager.Instance().StartListen();
	}
}
