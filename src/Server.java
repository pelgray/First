import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Server {
    private static int portNum = Const.port; // выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(portNum);
            System.out.println("Server started on " + portNum);
            Socket socket = serverSocket.accept(); // возвращает экземпляр клиента, кот. подключился к серверу
            //InputStream is = socket.getInputStream(); // байтовый поток
            String clientMsg = "";
            DataInputStream dInputStream = new DataInputStream(socket.getInputStream());
            System.out.println("The connection was created.");
            while(!clientMsg.equals("exit")) {
                clientMsg = dInputStream.readUTF();
                System.out.println("msg from client: " + clientMsg);
            }
            System.out.println("The connection was stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
