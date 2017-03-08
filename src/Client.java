import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    public static void main(String[] args) { // в аргументах: сначала номер порта, потом имя хоста
        try {
            // берем порт из аргументов
            int portNum = Integer.parseInt(args[0]);
            // по хосту подключаемся к тому порту, что указывали на сервере
            Socket socket = new Socket(args[1], portNum);

            DataOutputStream dOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dInputStream = new DataInputStream(socket.getInputStream());

            // контрольная строка: либо есть сообщение от сервера, что он занят, либо нет. Во втором случае продолжаем работу
            String fromServer = dInputStream.readUTF();

            if (fromServer.equals("")) {
                System.out.printf("The connection was created. Your name is (%s:%s)%n", socket.getInetAddress().getHostAddress(), socket.getLocalPort());
                String myMsg = "";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                while (!myMsg.equals("exit")) {
                    try{
                        myMsg = bufferedReader.readLine();
                    dOutputStream.writeUTF(myMsg);
                    System.out.println("Sent");
                    } catch (Exception e){
                        if(e.getMessage().contains("Connection reset")){
                            System.err.println("Server is not connected.");
                            socket.close();
                            System.exit(-1);
                        }
                        else e.printStackTrace();
                    }
                }
                System.out.println("The connection was stopped.");
            }
            else{ // выводим сообщение от сервера, что он не хочет работать с нами
                System.out.println(fromServer);
            }
            socket.close();
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection refused: connect"))
                System.err.println("Server is not connected.");
            else e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
