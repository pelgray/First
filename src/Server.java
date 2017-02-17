import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Server {
    public static int numOfConn = 2; // счетчик количества допустимых подключений

    // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
    public static void main(String[] args) { // в аргументах: номер порта
        try {
            int portNum = Integer.parseInt(args[0]);
            ServerSocket serverSocket = new ServerSocket(portNum);
            System.out.println("Server started on port: " + portNum);

            // вариант для множества подключений
            while (true) {
                Socket socket = serverSocket.accept(); // принимаем входящее подключение
                DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());

                if (numOfConn > 0) { // если количество допустимых соединений не достигло максимума
                // создаем
                Thread client = new Thread(new Session(socket));
                // обзываем его
                client.setName(socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort()));
                // запускаем
                client.start();
                // чисто для проверки в клиенте: хотят ли с нами работать или нет (здесь: хотят работать)
                dOutputStream.writeUTF("");
                System.out.println("[NEW]   The connection with (" + client.getName() + ") was created.");
                // уменьшаем количество возможных соединений
                numOfConn--;
                }
                else { // когда максимальное количество подключений достигнуто
                    dOutputStream.writeUTF("Hi! Sorry, I'm busy now. Please try later.");
                    System.out.println("    [INFO]  The connection with a new client was rejected.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
