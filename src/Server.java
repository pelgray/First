import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */

public class Server implements Runnable{
    private int _numOfConn = 0; // счетчик количества подключений
    private int _maxNumOfConn;
    private int _portNum; // номер порта
    private ServerSocket _serverSocket;

    private final Object lock = new Object();

    public Server (ServerSocket serverSocket, int portNum, int maxNumOfConn){
        _serverSocket = serverSocket;
        _portNum = portNum;
        _maxNumOfConn = maxNumOfConn;
    }
    public void closeSession(){
        synchronized (lock) {
            _numOfConn--;
            lock.notify();
        }
    }

    public int get_maxNumOfConn() { return _maxNumOfConn; }
    public int get_numOfConn() { return _numOfConn; }

    public void run(){
        System.out.println("Server started on port: " + _portNum);
        System.out.println("    with maximum number of connections = " + _maxNumOfConn);

        Channel<Runnable> channel = new Channel<>(_maxNumOfConn);

        Thread listener = new Thread(new Listener(channel, Thread.currentThread(), Server.this));
        listener.setDaemon(true);
        listener.setName("LISTENER");
        listener.start();

        Thread dispatcher = new Thread(new Dispatcher(channel));
        dispatcher.setDaemon(true);
        dispatcher.setName("DISPATCHER");
        dispatcher.start();

        while (true) {
            Socket socket;
            // принимаем входящее подключение
            try {
                socket = _serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Server: The error of incoming connection.");
                return;
            }
            // увеличиваем количество возможных соединений
            synchronized (lock) {
                while (_numOfConn == _maxNumOfConn){ // while вместо if
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        System.err.println("Server: The error of waiting the object.");
                        return;
                    }
                }
                _numOfConn++;
            }
            // отправляем в очередь
            channel.put(new Session(socket, Server.this));
        }
    }
}
