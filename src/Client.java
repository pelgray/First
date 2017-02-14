import java.io.*;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    private static int portNum = Const.port;
    private  static String host = "localhost";
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(host, portNum); // по локальному хосту подключаемся к тому порту, что указывали на сервере
            DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());
            dOutputStream.writeUTF("Hi! Your port is " + portNum);
            System.out.println("The connection was created.");
            String myMsg = "";
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(!myMsg.equals("exit")) {
                myMsg = bufferedReader.readLine();
                dOutputStream.writeUTF(myMsg);
                System.out.println("Sent");
            }
            System.out.println("The connection was stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
