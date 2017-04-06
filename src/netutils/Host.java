package netutils;

import app.Listener;
import concurrentutils.Channel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */

public class Host implements Runnable{
    private int _numOfConn = 0; // счетчик количества запросов
    private int _portNum; // номер порта
    private ServerSocket _serverSocket;
    private Channel<Runnable> _channel;
    private MessageHandler _msgH = null;

    private final Object _lock = new Object();

    public Host(int portNum, Channel<Runnable> channel, MessageHandlerFactory mHF){
        _portNum = portNum;
        try {
            _serverSocket = new ServerSocket(_portNum);
        } catch (IOException e) {
            System.err.println("Host: The port " + _portNum + " is busy.");
            return;
        }
        _channel = channel;
        _msgH = mHF.create();
    }
    public void closeSession(){
        synchronized (_lock) {
            _numOfConn--;
            _lock.notify();
        }
    }

    public int get_numOfConn() { return _numOfConn; }

    public void run(){
        System.out.println("Host started on port: " + _portNum);

        Thread listener = new Thread(new Listener(_channel, Thread.currentThread(), Host.this));
        listener.setDaemon(true);
        listener.setName("LISTENER");
        listener.start();

        while (true) {
            Socket socket;
            // принимаем входящее подключение
            try {
                socket = _serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Host: The error of incoming connection.");
                return;
            }
            // увеличиваем количество запросов
            synchronized (_lock) {
                _numOfConn++;
            }
            // отправляем в очередь
            _channel.put(new Session(socket, Host.this, _msgH));
        }
    }
}
