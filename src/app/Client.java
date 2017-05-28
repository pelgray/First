package app;

import java.io.*;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    private static DataOutputStream _dout;
    private static DataInputStream _din;
    private static BufferedReader _bR;

    private static ClientFTPmethods _ftp;
    private static boolean _isAuthorized = false;

    public static void main(String[] args) { // в аргументах: сначала номер порта, потом имя хоста
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // берем порт из аргументов
        } catch (NumberFormatException e){
            System.err.println("Wrong port format. Should be integer. Try again.");
            return;
        }

        // по хосту подключаемся к тому порту, что указывали на сервере
        Socket socket;
        try {
            socket = new Socket(args[1], portNum);
        } catch (IOException e) {
            System.err.println("The error of creating a new socket. Please check arguments. PortNum = " + portNum + "; Host = " + args[1] + ".\nOr server may be is not available.");
            return;
        }


        try {
            _dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("The error of getting the output stream.");
            return;
        }
        try {
            _din = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("The error of getting the input stream.");
            return;
        }


        System.out.println("Please wait. Connecting to the Host...");

        // контрольная строка: либо есть сообщение от сервера, что он занят, либо нет. Во втором случае продолжаем работу
        String fromServer;
        try {
            fromServer = _din.readUTF();
        } catch (IOException e) {
            System.err.println("The error of reading from the input stream. The server may be is not available.");
            return;
        }

        if (fromServer.equals("SERVER is active")) {
            System.out.printf("The connection was created. Your name is (%s:%s)%n", socket.getInetAddress().getHostAddress(), socket.getLocalPort());
            System.out.println("[INFO]\tIf you wanna work with files, use next commands:\n" +
                    "\t\t'#reg' - if you are a new user;\n" +
                    "\t\t'#log' - if you want to log in to system.");

            String myMsg;
            _bR = new BufferedReader(new InputStreamReader(System.in));
            _ftp = new ClientFTPmethods(_dout, _din, _bR);

            label:
            while (true) {
                try {
                    if (_bR.ready()) {
                        try {
                            myMsg = _bR.readLine();
                        } catch (IOException e) {
                            if (e.getMessage().contains("Connection reset")) {
                                System.err.println("The Host is not connected.");
                            } else {
                                System.err.println("The error of reading from the system input stream.");
                            }
                            return;
                        }
                        try {
                            _dout.writeUTF(myMsg);
                        } catch (IOException e) {
                            if (e.getMessage().contains("Connection reset")) {
                                System.err.println("The Host is not connected.");
                            } else {
                                System.err.println("The error of writing in the output stream.");
                            }
                            return;
                        }
                        switch (myMsg) {
                            case "#reg":
                                if (!_isAuthorized) _ftp.registration();
                                else System.out.println("To start the registration, you need to log out. Use command '#logout'.");
                                break;
                            case "#log":
                                if (!_isAuthorized) {
                                    _ftp.authorization();
                                    if (_ftp.isAuthorized()) {
                                        _isAuthorized = true;
                                        System.out.println("[INFO]\tCommands for working with VCS:\n" +
                                                "\t\t'#update' - when you want to update a file on the Server;\n" +
                                                "\t\t'#rollback' - if you want to rollback of file version;\n" +
                                                "\t\t'#logout' - if you want to log out.");
                                    }
                                }
                                else System.out.println("To log in with a different name, you need to log out. Use command '#logout'.");
                                break;
                            case "#update":
                                    if (_isAuthorized) {
                                        _ftp.updateFile();
                                    }
                                    else{
                                        System.out.println("You are not authorized. Use commands:\n" +
                                                "\t\t'#reg' - if you are a new user;\n" +
                                                "\t\t'#log' - if you want to log in to system.");
                                    }
                                break;
                            case "#rollback":
                                if (_isAuthorized) {
                                    _ftp.rollbackFile();
                                }
                                else{
                                    System.out.println("You are not authorized. Use commands:\n" +
                                            "\t\t'#reg' - if you are a new user;\n" +
                                            "\t\t'#log' - if you want to log in to system.");
                                }
                                break;
                            case "#logout":
                                if (_isAuthorized) {
                                    _ftp.logOut(socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());
                                    if (!_ftp.isAuthorized()) _isAuthorized = false;
                                }
                                else{
                                    System.out.println("You are not authorized. Use commands:\n" +
                                            "\t\t'#reg' - if you are a new user;\n" +
                                            "\t\t'#log' - if you want to log in to system.");
                                }
                                break;
                            case "exit":
                                System.out.println("The connection was stopped.");
                                break label;
                            default:
                                System.out.println("Sent");
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // _bR.ready()
                }
                try {
                    if (_din.available() != 0) {
                        try {
                            fromServer = _din.readUTF();
                        } catch (IOException e) {
                            System.err.println("The error of reading from the input stream. The server maybe is not connected.");
                            return;
                        }
                        System.out.println("Server:\t" + fromServer);
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // _din.available()
                }
            }
        }
        else{ // выводим сообщение от сервера
            System.out.println(fromServer);
        }
    }
}
