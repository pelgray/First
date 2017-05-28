package netutils;

import app.Checksum;
import app.ServerFTPmethods;
import concurrentutils.Stoppable;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by 1 on 17.02.2017.
 */
public class Session implements Stoppable {
    private Host _host;
    private Socket _socket;
    private String _name; // имя, сложенное из хоста и порта
    private MessageHandler _msgH;
    private LogMessageErrorWriter _errorWriter;

    private DataOutputStream _dout;
    private DataInputStream _din;

    private ServerFTPmethods _ftp;
    private boolean _isAuthorized = false;

    public Session(Socket socket, Host host, MessageHandler msgH, LogMessageErrorWriter errorWriter) {
        this._host = host;
        this._socket = socket;
        this._name = socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort());
        this._msgH = msgH;
        this._errorWriter = errorWriter;

        try {
            _dout = new DataOutputStream(_socket.getOutputStream());
        } catch (IOException e) {
            _errorWriter.write("The error of getting the output stream.");
            return;
        }
        try {
            _din = new DataInputStream(_socket.getInputStream());
        } catch (IOException e) {
            _errorWriter.write("The error of getting the input stream.");
            return;
        }

        _ftp = new ServerFTPmethods(_din, _dout, _name, _errorWriter);
    }


    public void run() {
        try {
            // чисто для проверки в клиенте: хотят ли с нами работать или нет (здесь: хотят работать)
            try {
                _dout.writeUTF("SERVER is active");
            } catch (IOException e) {
                System.err.println("The connection with waiting Client (" + _name + ") was lost.");
                return;
            }

            System.out.println("[NEW]   The connection with (" + _name + ") was created.");
            String clientMsg = "";

            while (!clientMsg.equals("exit")) {
                try {
                    clientMsg = _din.readUTF();
                } catch(SocketException e) {
                    if (e.getMessage().equals("Socket closed"))
                        return;
                    else
                        if(e.getMessage().equals("Connection reset"))
                        System.err.println("The connection was reset by Client (" + _name + "). Bye friend!");
                    else e.printStackTrace();
                    return;
                } catch (IOException e) {
                    if (!e.getMessage().equals("Connection reset"))
                        _errorWriter.write("The error of reading from the input stream.");
                    else
                        System.err.println("The connection was reset by Client (" + _name + "). Bye friend!");
                    return;
                }
                switch (clientMsg) {
                    case "#update":
                        if(_isAuthorized)
                            _ftp.updateFile();
                        continue;
                    case "#rollback":
                        if(_isAuthorized)
                            _ftp.rollbackFile();
                        continue;
                    case "#reg":
                        _ftp.registration();
                        continue;
                    case "#log":
                        _ftp.authorization();
                        if (_ftp.isAuthorized()) {
                            _name = _ftp.get_name();
                            _isAuthorized = true;
                        }
                        continue;
                    case "#logout":
                        if(_isAuthorized) {
                            _ftp.logOut(null);
                            if (!_ftp.isAuthorized()) {
                                _name = _ftp.get_name();
                                _isAuthorized = false;
                            }
                        }
                        continue;
                }
                if (!clientMsg.equals("exit")) _msgH.handle(_name, clientMsg);
            }
            System.out.println("The connection with (" + _name + ") was stopped.");
        }
        finally{
            // уменьшаем счетчик допустимых соединений, так как кто-то завершил работу с сервером
            _host.closeSession();
            try {
                _socket.close();
            } catch (IOException e) {
                _errorWriter.write("The error of closing socket.");
            }
        }
    }

    @Override
    public void stop() {
        if (_socket != null){
            try {
                _dout.writeUTF("SERVER is stopped");
            } catch (IOException e) {
                System.err.println("The connection with waiting Client (" + _name + ") was lost. The error of informing to the client during the process termination.");
                return;
            }
            System.out.println("\t\tA message to the Client (" + _name + ") was sent.");
            try {
                _socket.close();
            } catch (IOException e) {
                _errorWriter.write("The error of closing socket during the process termination.");
            }
            System.out.println("\tThe session with the Client (" + _name + ") was stopped.");
        }
    }
}
