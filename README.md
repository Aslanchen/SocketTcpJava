# SocketTcp

语言：Java

工具：Eclipse

Socket TCP JAVA版本，客户端以及服务端的简单封装。请参考MainTest.Java

1：客户端发送和接收。

2：服务端发送和接收。

3：发送和接收队列处理。

4：数据的分发，MsgCenter.java，根据具体情况做参考。

5：Tcp粘包处理，本程序默认以数据前4位作为长度判断，请根据实际情况修改 SocketClient 和 SocketServer 的 OnReceive 函数。

6：生成数据的时候，请注意数字类型的高低位。C++和JAVA是由低到高，C#是由高到低，请自行修改。

#问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流

* 作者: 陈恒飞
* 邮件(122560007@qq.com)

