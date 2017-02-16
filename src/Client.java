import java.io.*;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    public static void main(String[] args) { // в аргументах: сначала номер порта, потом имя хоста
        try {
            int portNum = Integer.parseInt(args[0]);
            Socket socket = new Socket(args[1], portNum); // по локальному хосту подключаемся к тому порту, что указывали на сервере
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
