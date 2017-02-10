import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    public static void main(String[] args) {
        int portNum = 1050;
        try {
            Socket socket = new Socket("localhost", portNum); // по локальному хосту подключаемся к тому порту, что указывали на сервере
            DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());
            dOutputStream.writeUTF("Hi! My port is " + portNum);
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
