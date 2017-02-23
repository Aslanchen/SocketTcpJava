package com.aslanchen.socket;

import java.io.IOException;

import com.aslanchen.socket.MsgCenter.MsgCallback;
import com.aslanchen.socket.model.protocl.TestModel;
import com.google.protobuf.InvalidProtocolBufferException;

public class MainTest {

	public static void main(String[] args) {
		MsgCenter.Instance().RegisterMsg((short) 100, new MsgCallback() {

			@Override
			public void OnMsg(byte[] data) {
				TestModel.Test model = null;
				try {
					model = TestModel.Test.parseFrom(data);
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

				if (model == null) {
					return;
				}

				System.out.println("收-100 数据-" + model.toString());
			}
		});

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
