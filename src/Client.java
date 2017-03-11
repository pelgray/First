import java.io.*;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    public static void main(String[] args) { // в аргументах: сначала номер порта, потом имя хоста
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // берем порт из аргументов
        } catch (NumberFormatException e){
            System.err.println("Client: Wrong port format. Should be integer. Try again.");
            return;
        }

        // по хосту подключаемся к тому порту, что указывали на сервере
        Socket socket;
        try {
            socket = new Socket(args[1], portNum);
        } catch (IOException e) {
            System.err.println("Client: The error of creating a new socket. Please check arguments.");
            return;
        }

        DataOutputStream dOutputStream;
        try {
            dOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Client: The error of getting the output stream.");
            return;
        }
        DataInputStream dInputStream;
        try {
            dInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Client: The error of getting the input stream.");
            return;
        }

        System.out.println("Please wait. Connecting to the Server...");

        // контрольная строка: либо есть сообщение от сервера, что он занят, либо нет. Во втором случае продолжаем работу
        String fromServer;
        try {
            fromServer = dInputStream.readUTF();
        } catch (IOException e) {
            System.err.println("Client: The error of reading from the input stream.");
            return;
        }
        if (fromServer.equals("")) {
            System.out.printf("The connection was created. Your name is (%s:%s)%n", socket.getInetAddress().getHostAddress(), socket.getLocalPort());
            String myMsg = "";
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (!myMsg.equals("exit")) {
                try {
                    myMsg = bufferedReader.readLine();
                } catch (IOException e) {
                    if (e.getMessage().contains("Connection reset")) {
                        System.err.println("Server is not connected.");
                    } else {
                        System.err.println("Client: The error of reading from the system input stream.");
                    }
                    return;
                }
                try {
                    dOutputStream.writeUTF(myMsg);
                } catch (IOException e) {
                    if (e.getMessage().contains("Connection reset")) {
                        System.err.println("Server is not connected.");
                    } else {
                        System.err.println("Client: The error of writing in the output stream.");
                    }
                    return;
                }
                System.out.println("Sent");
            }
            System.out.println("The connection was stopped.");
        }
        else{ // выводим сообщение от сервера, что он не хочет работать с нами
            System.out.println(fromServer);
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Client: The error of closing socket.");
        }
    }
}
