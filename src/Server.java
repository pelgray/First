import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Server {
    public static void main(String[] args) {
        int portNum = 1050;
        try {
            ServerSocket serverSocket = new ServerSocket(portNum);
            Socket socket = serverSocket.accept(); // возвращает экземпляр клиента, кот. подключился к серверу
            //InputStream is = socket.getInputStream(); // байтовый поток
            DataInputStream dInputStream = new DataInputStream(socket.getInputStream());
            String message = dInputStream.readUTF();
            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
