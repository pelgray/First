import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by 1 on 17.02.2017.
 */
public class Session implements Runnable {
    private Socket _socket;
    private String _name; // имя, сложенное из хоста и порта

    public Session(Socket socket) {
        this._socket = socket;
        this._name = socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort());
    }

    public void run() {
        try {
            String clientMsg = "";
            DataInputStream dInputStream;
            try {
                dInputStream = new DataInputStream(_socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Session: The error of getting the input stream.");
                return;
            }
            while (!clientMsg.equals("exit")) {
                try {
                    clientMsg = dInputStream.readUTF();
                } catch (IOException e) {
                    if (!e.getMessage().equals("Connection reset"))
                        System.err.println("Session: The error of reading from the input stream.");
                    else
                        System.err.println("Connection was reset by Client (" + _name + "). Bye friend!");
                    return;
                }
                System.out.println("        msg from (" + _name + "): " + clientMsg);
            }
            System.out.println("The connection with (" + _name + ") was stopped.");
        }
        finally{
            // уменьшаем счетчик допустимых соединений, так как кто-то завершил работу с сервером
            Server.closeSession();
            try {
                _socket.close();
            } catch (IOException e) {
                System.err.println("closeSession(): The error of closing socket.");
            }
        }
    }

}
