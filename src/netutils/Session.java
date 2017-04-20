package netutils;

import app.Checksum;
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
    private File _folder;
    private Checksum _hash = new Checksum();

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

        _folder = new File("c:" +
                File.separator + "TEST_FTP_SERVER");
        if (!_folder.exists()) {
            _folder.mkdir();
        }
    }

    void sendFile(){
        String filename;
        try {
            filename = _din.readUTF();
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
        System.out.println("The client (" + _name + ") requested the file " + filename + ".");
        File file = new File(_folder, filename);
        if(!file.exists()){
            try {
                _dout.writeUTF("File Not Found");
            } catch (IOException e) {
                System.err.println("The connection with waiting Client (" + _name + ") was lost.");
                e.printStackTrace();
                return;
            }

            System.out.println("\tThe file " + filename + " not found.");
            return;
        }
        else{ // если существует
            try {
                _dout.writeUTF("READY");
                String temp = _hash.get(file.getAbsolutePath());
                _dout.writeUTF(temp); // отправка хэша
            } catch (IOException e) {
                System.err.println("The connection with waiting Client (" + _name + ") was lost.");
                e.printStackTrace();
                return;
            }
            String option;
            try {
                option = _din.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (option.equals("N")) return;
            FileInputStream fin;
            try {
                fin = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            int ch;
            do{
                try {
                    ch = fin.read();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    _dout.writeUTF(String.valueOf(ch));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while(ch != -1);

            try {
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                _dout.writeUTF("File was received successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("The file " + filename + " was successfully sent to the client (" + _name + ").");
        }
    }

    void receiveFile(){
        String filename = null;
        try {
            filename = _din.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // проверяет, есть ли что принимать
        if(filename.compareTo("File not found")==0)
        {
            System.out.println("The client (" + _name + ") tried to send the file " + filename + ", but it does not exist.");
            return;
        }

        File file = new File(_folder, filename);
        String option;

        if (file.exists()){
            try {
                _dout.writeUTF("File Already Exists");
                String temp = _hash.get(file.getAbsolutePath());
                _dout.writeUTF(temp); // отправка хэша
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                option = _din.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (option.equals("Y"))
                System.out.println("The client (" + _name + ") sent a new version of file " + filename + ".");
        }
        else
        {
            try {
                _dout.writeUTF("SendFile");
            } catch (IOException e) {
                e.printStackTrace();
            }
            option = "Y";
            System.out.println("The client (" + _name + ") sent a new file " + filename + ".");
        }

        if  (option.compareTo("Y") == 0){
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fout;
            try {
                fout = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            int ch;
            String temp;
            do{
                try {
                    temp = _din.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ch = Integer.parseInt(temp);
                if (ch!=-1){
                    try {
                        fout.write(ch);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }while(ch != -1);
            try {
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                _dout.writeUTF("File was sent successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            return;
        }
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
                if(clientMsg.compareTo("#get")==0)
                {
                    //System.out.println("\t'#get' Command Received.");
                    sendFile();
                    continue;
                }
                else if(clientMsg.compareTo("#send")==0)
                {
                    //System.out.println("\t'#send' Command Received.");
                    receiveFile();
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
