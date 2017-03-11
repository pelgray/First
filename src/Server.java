import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */

public class Server {
    private static int numOfConn = 0; // счетчик количества подключений
    private static int maxNumOfConn;
    private static Thread listener;
    public static final Thread server = Thread.currentThread();

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

        listener = new Thread(new Listener());
        listener.setDaemon(true);
        listener.start();

        mainCycle:
        while (true) {
            Socket socket;
            // принимаем входящее подключение
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Server: The error of incoming connection.");
                return;
            }

            DataOutputStream dOutputStream;
            try {
                dOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("Server: The error of getting the output stream.");
                return;
            }

            if (numOfConn != maxNumOfConn) { // если количество соединений не достигло максимума
                // создаем поток для клиента
                Thread client = new Thread(new Session(socket));

                // обзываем поток
                client.setName(socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort()));

                // чисто для проверки в клиенте: хотят ли с нами работать или нет (здесь: хотят работать)
                try {
                    dOutputStream.writeUTF("");
                } catch (IOException e) {
                    System.err.println("The connection with waiting Client (" + client.getName() + ") was lost.");
                    continue mainCycle;
                }

                // запускаем
                client.start();
                System.out.println("[NEW]   The connection with (" + client.getName() + ") was created.");

                // увеличиваем количество возможных соединений
                synchronized (lock) {
                    numOfConn++;
                    if (numOfConn == maxNumOfConn){
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.err.println("Server: The error of waiting the object.");
                            return;
                        }
                    }
                }
            }
        }
    }


}
