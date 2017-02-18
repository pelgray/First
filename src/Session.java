import java.io.DataInputStream;
import java.net.Socket;

/**
 * Created by 1 on 17.02.2017.
 */
public class Session implements Runnable {
    Socket _socket;
    String _name; // имя, сложенное из хоста и порта

    public Session(Socket socket) {
        this._socket = socket;
        this._name = socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort());
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
            // увеличиваем счетчик допустимых соединений, так как кто-то завершил работу с сервером
            Server.numOfConn++;
        } catch(Exception e) {
            if (e.getMessage().equals("Connection reset")){
                Server.numOfConn++;
                System.err.println("Connection was reset by Client (" + _name + "). Bye friend!");
            }
            else System.err.println("Session.run() -> Exception : " + e);
        }
    }

}
