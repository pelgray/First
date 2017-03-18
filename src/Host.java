import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by 1 on 18.03.2017.
 */
public class Host {

    public static void main(String[] args) { // в аргументах: номер порта, количество возможных подключений
        // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // получение номера порта из аргументов
        } catch (NumberFormatException e) {
            System.err.println("Server: Wrong port format. Should be integer. Try again.");
            return;
        }
        int maxNumOfConn;
        try {
            maxNumOfConn = Integer.parseInt(args[1]); // получение максимального количества подключений
        } catch (NumberFormatException e) {
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

        Thread server = new Thread(new Server(serverSocket, portNum, maxNumOfConn));
        server.setName("SERVER");
        server.start();
    }
}
