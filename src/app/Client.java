package app;

import netutils.MessageHandler;
import netutils.MessageHandlerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Created by 14borisova on 10.02.2017.
 */
public class Client {
    private static DataOutputStream _dout;
    private static DataInputStream _din;
    private static BufferedReader _bR;

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

        Class classMessageHandlerFactory;
        try {
            classMessageHandlerFactory = Class.forName(args[2]);
        } catch (ClassNotFoundException e) {
            System.err.println("Wrong name of class MessageHandlerFactory. Class not found. Try again.");
            return;
        }

        MessageHandlerFactory mHF;
        try {
            mHF = (MessageHandlerFactory) classMessageHandlerFactory.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("The error of creating a new instance of class MessageHandlerFactory.");
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

        _bR = new BufferedReader(new InputStreamReader(System.in));

        MessageHandler _mH = mHF.create("client", new ClientFTPmethods(_dout, _din, _bR, socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort()));

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
            String myMsg;

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
                            case "exit":
                                System.out.println("The connection was stopped.");
                                break label;
                            default:
                                _mH.handle(myMsg);
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
