package netutils;

import concurrentutils.Channel;
import concurrentutils.Stoppable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by 14borisova on 10.02.2017.
 */

public class Host implements Stoppable{
    private int _numOfConn = 0; // счетчик количества запросов
    private int _portNum; // номер порта
    private ServerSocket _serverSocket;
    private Channel<Stoppable> _channel;
    private MessageHandler _msgH;
    private LogMessageErrorWriter _errorWriter;
    private Thread _thread;
    private volatile boolean _isActive;

    private final Object _lock = new Object();

    public Host(int portNum, Channel<Stoppable> channel, MessageHandler mH, LogMessageErrorWriter errorWriter){
        _msgH = mH;
        _errorWriter = errorWriter;
        _portNum = portNum;
        try {
            _serverSocket = new ServerSocket(_portNum);
        } catch (IOException e) {
            _errorWriter.write("The port " + _portNum + " is busy.");
            return;
        }
        _channel = channel;
        _thread = Thread.currentThread();
        _isActive = true;
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

        while (_isActive) {
            Socket socket;
            // принимаем входящее подключение
            try {
                socket = _serverSocket.accept();
            } catch(SocketException e) {
                if (e.getMessage().equals("socket closed"))
                    return;
                else e.printStackTrace();
                return;
            }
            catch (IOException e) {
                _errorWriter.write("The error of incoming connection.");
                stop();
                return;
            }
            // увеличиваем количество запросов
            synchronized (_lock) {
                _numOfConn++;
            }
            // отправляем в очередь
            _channel.put(new Session(socket, Host.this, _msgH, _errorWriter));
        }
    }

    @Override
    public void stop() {
        if (_isActive) {
            _isActive = false;
            _thread.interrupt();
            try {
                _serverSocket.close();
            } catch (IOException e) {
                _errorWriter.write("The error of closing serverSocket.");
            }
            System.out.println("\tThe host was stopped.");
        }
    }
}
