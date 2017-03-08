import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 1 on 17.02.2017.
 */
public class Session implements Runnable {
    Socket _socket;
    String _name; // имя, сложенное из хоста и порта
    ServerSocket _serverSocket;

    public Session(Socket socket, ServerSocket serverSocket) {
        this._socket = socket;
        this._name = socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort());
        this._serverSocket = serverSocket;
    }

    public void run() {
        try {
            String clientMsg = "";
            DataInputStream dInputStream = new DataInputStream(_socket.getInputStream());
            while (!clientMsg.equals("exit")) {
                clientMsg = dInputStream.readUTF();
                System.out.println("        msg from (" + _name + "): " + clientMsg);
            }
            System.out.println("The connection with (" + _name + ") was stopped.");
            _socket.close();
        } catch(Exception e) {
            if (!e.getMessage().equals("Connection reset")) System.err.println("Session.run() -> Exception : " + e);
            else
                System.err.println("Connection was reset by Client (" + _name + "). Bye friend!");
        } finally{
            // уменьшаем счетчик допустимых соединений, так как кто-то завершил работу с сервером
            Server.closeSession();
        }
    }

}
