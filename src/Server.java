import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Server {
    public static int numOfConn = 0; // счетчик количества подключений
    public static int maxNumOfConn;
    private static Thread listener;
    public static void closeSession(){
        numOfConn--;
    }

    // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
    public static void main(String[] args) { // в аргументах: номер порта, количество возможных подключений
        try {
            int portNum = Integer.parseInt(args[0]); // получение номера порта из аргументов
            maxNumOfConn = Integer.parseInt(args[1]); // получение максимального количества подключений
            ServerSocket serverSocket = new ServerSocket(portNum);
            System.out.println("Server started on port: " + portNum);

            listener = new Thread(new Listener());
            listener.setDaemon(true);
            listener.start();

            while (true) {
                Socket socket = serverSocket.accept(); // принимаем входящее подключение
                DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());

                if (numOfConn != maxNumOfConn) { // если количество соединений не достигло максимума
                    // создаем поток для клиента
                    Thread client = new Thread(new Session(socket, serverSocket));

                    // обзываем поток
                    client.setName(socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort()));

                    // запускаем
                    client.start();

                    // чисто для проверки в клиенте: хотят ли с нами работать или нет (здесь: хотят работать)
                    dOutputStream.writeUTF("");
                    System.out.println("[NEW]   The connection with (" + client.getName() + ") was created.");
                    // увеличиваем количество возможных соединений
                    numOfConn++;
                }
                else { // когда максимальное количество подключений достигнуто
                    dOutputStream.writeUTF("Hi! Sorry, I'm busy now. Please try later.");
                    System.out.println("    [INFO]  The connection with a new client was rejected.");
                    socket.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
