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
    private static File _folder;
    private static Checksum _hash = new Checksum();

    static void sendFile() throws Exception
    {
        String filename;
        System.out.print("Enter file name: ");
        filename = _bR.readLine();

        File file = new File(_folder, filename);

        if (!file.exists()){
            System.out.println("File does not exists...");
            _dout.writeUTF("File not found");
            return;
        }

        _dout.writeUTF(filename); // отправка имени файла

        String msgFromServer = _din.readUTF();

        if(msgFromServer.compareTo("File Already Exists")==0)
        {
            msgFromServer = _din.readUTF();
            String msg = _hash.get(file.getAbsolutePath());
            String option;
            System.out.print("File already exists ");
            if (msg.equals(msgFromServer)) System.out.print("and the same. ");
            else System.out.print("and is not the same. ");
            System.out.println("Do you want to OverWrite (Y/N) ?");
            option = _bR.readLine();
            if(option.equals("Y"))
            {
                _dout.writeUTF("Y");
            }
            else
            {
                _dout.writeUTF("N");
                System.out.println("Sending canceled.");
                return;
            }
        }

        System.out.println("Sending file...");
        FileInputStream fin = new FileInputStream(file);
        int ch;
        do{
            ch = fin.read();
            _dout.writeUTF(String.valueOf(ch));
        }
        while(ch != -1);
        fin.close();
        System.out.println(_din.readUTF());
    }

    static void receiveFile() throws Exception
    {
        String fileName;
        System.out.print("Enter file name: ");
        fileName = _bR.readLine();
        _dout.writeUTF(fileName);
        String msgFromServer = _din.readUTF();

        if(msgFromServer.compareTo("File Not Found")==0)
        {
            System.out.println("File was not found on the Server...");
            return;
        }
        else if(msgFromServer.compareTo("READY")==0)
        {
            msgFromServer = _din.readUTF();
            System.out.println("Receiving file...");
            File file = new File(_folder, fileName);
            if(file.exists())
            {
                String msg = _hash.get(file.getAbsolutePath());
                String option;
                System.out.print("File already exists ");
                if (msg.equals(msgFromServer)) System.out.print("and the same. ");
                else System.out.print("and is not the same. ");
                System.out.println("Do you want to OverWrite (Y/N) ?");
                option = _bR.readLine();
                _dout.writeUTF(option);
                if(option.equals("N"))
                {
                    _dout.flush();
                    System.out.println("Receiving canceled.");
                    return;
                }
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fout = new FileOutputStream(file);
            int ch;
            String temp;
            do{
                temp = _din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1){
                    fout.write(ch);
                }
            }while(ch != -1);
            fout.close();
            System.out.println(_din.readUTF());
        }
    }

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

        _folder = new File("C:" +
                File.separator + "TEST_FTP_CLIENT");
        if (!_folder.exists()) {
            _folder.mkdir();
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

            System.out.println("[INFO]\tCommands for working with files:\n" +
                                "\t\t'#get' - when you want to get a file from the Server's folder;\n" +
                                "\t\t'#send' - when you want to send a file to the Server from your folder.");

            String myMsg;
            _bR = new BufferedReader(new InputStreamReader(System.in));
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
                            case "#get":
                                try {
                                    receiveFile();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "#send":
                                try {
                                    sendFile();
                                } catch (Exception e) {
                                    e.printStackTrace();
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
        else{ // выводим сообщение от сервера, что он не хочет работать с нами
            System.out.println(fromServer);
        }
    }
}
