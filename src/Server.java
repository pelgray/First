import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */

public class Server {
    private static int numOfConn = 0; // счетчик количества подключений
    private static int maxNumOfConn;

    private static final Object lock = new Object();

    public static void closeSession(){
        synchronized (lock) {
            numOfConn--;
            lock.notify();
        }
    }

    public static int getMaxNumOfConn() { return maxNumOfConn; }
    public static int getNumOfConn() { return numOfConn; }

    // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
    public static void main(String[] args) { // в аргументах: номер порта, количество возможных подключений
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // получение номера порта из аргументов
        } catch (NumberFormatException e){
            System.err.println("Server: Wrong port format. Should be integer. Try again.");
            return;
        }
        try {
            maxNumOfConn = Integer.parseInt(args[1]); // получение максимального количества подключений
        } catch (NumberFormatException e){
            System.err.println("Server: Wrong maximum number of connections format. Should be integer. Try again.");
            return;
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(portNum);
        } catch (IOException e) {
            System.err.println("Server: The port " + portNum + " is busy.");
            return;
        }
        System.out.println("Server started on port: " + portNum);
        System.out.println("    with maximum number of connections = " + maxNumOfConn);

        Channel<Runnable> channel = new Channel<>(maxNumOfConn);

        Thread listener = new Thread(new Listener(channel, Thread.currentThread()));
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
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Server: The error of incoming connection.");
                return;
            }
            // увеличиваем количество возможных соединений
            synchronized (lock) {
                while (numOfConn == maxNumOfConn){ // while вместо if
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        System.err.println("Server: The error of waiting the object.");
                        return;
                    }
                }
                numOfConn++;
            }
            // отправляем в очередь
            channel.put(new Session(socket));
        }
    }


}
