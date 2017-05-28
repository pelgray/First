package app;

import netutils.FTPmethods;

import java.io.*;

/**
 * Created by 1 on 26.05.2017.
 */
public class ClientFTPmethods implements FTPmethods {
    private static DataOutputStream _dout;
    private static DataInputStream _din;
    private static BufferedReader _bR;
    private static String _username;
    private static File _folder;
    private static Checksum _hash = new Checksum();
    private static File _controlFile;
    private static boolean _isAuthorized = false;

    ClientFTPmethods(DataOutputStream dout, DataInputStream din, BufferedReader bR){
        _dout = dout;
        _din = din;
        _bR = bR;
    }

    public boolean isAuthorized(){
        return _isAuthorized;
    }

    @Override // пересылка файла file
    public void baseSend(File file) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int ch;
        try {
            do{
                ch = fin.read();
                _dout.writeUTF(String.valueOf(ch));
            }
            while(ch != -1);
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // прием файла file
    public void baseReceive(File file) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int ch;
        String temp;
        try {
            do {
                temp = _din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                }
            } while (ch != -1);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile() {
        try {
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
                option = _bR.readLine().toUpperCase();
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
            baseSend(file);
            System.out.println(_din.readUTF());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveFile() {
        try {
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
            else if(msgFromServer.compareTo("READY")==0) {
                msgFromServer = _din.readUTF();
                System.out.println("Receiving file...");
                File file = new File(_folder, fileName);
                if (file.exists()) {
                    String msg = _hash.get(file.getAbsolutePath());
                    String option;
                    System.out.print("File already exists ");
                    if (msg.equals(msgFromServer)) System.out.print("and the same. ");
                    else System.out.print("and is not the same. ");
                    System.out.println("Do you want to OverWrite (Y/N) ?");
                    option = _bR.readLine().toUpperCase();
                    _dout.writeUTF(option);
                    if (option.equals("N")) {
                        _dout.flush();
                        System.out.println("Receiving canceled.");
                        return;
                    }
                    file.delete();
                    file.createNewFile();
                }
                _dout.writeUTF("Y");

                baseReceive(file);
                System.out.println(_din.readUTF());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // регистрация нового пользователя
    public void registration() {
        try {
            System.out.println("[INFO]\tRegistration started");
            System.out.print("Enter login: ");
            String username = _bR.readLine();
            _dout.writeUTF(username);
            String ans = _din.readUTF();
            while (ans.equals("exist")){
                System.out.println("\t Sorry, this login is busy.");
                System.out.print("Enter login: ");
                username = _bR.readLine();
                _dout.writeUTF(username);
                ans = _din.readUTF();
            }
            boolean flag = true;
            while(flag) {
                System.out.print("Enter your password: ");
                int pass = _bR.readLine().hashCode();
                System.out.print("\nPlease, re-enter the password: ");
                if (_bR.readLine().hashCode() == pass) {
                    _dout.writeUTF(Integer.toString(pass));
                    flag = false;
                } else {
                    System.out.println("\nThe passwords you entered do not match. Please, try again.");
                }
            }
            ans = _din.readUTF();
            System.out.println("[INFO]\tRegistration is " + ans);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // авторизация пользователя
    public void authorization() {
        try {
            boolean flag = true;
            while(flag) {
                System.out.print("Enter your login: ");
                String username = _bR.readLine();
                System.out.print("Enter your password: ");
                int pass = _bR.readLine().hashCode();
                _dout.writeUTF(username);
                _dout.writeUTF(Integer.toString(pass));
                if (_din.readUTF().equals("correct")) {
                    System.out.println("You are authorized, " + username);
                    _isAuthorized = true;
                    _username = username;
                    _folder = new File("C:" +
                            File.separator + "TEST_FTP_CLIENT_" + _username);
                    if (!_folder.exists()) {
                        _folder.mkdir();
                    }

                    flag = false;
                    _dout.writeUTF("end");
                } else {
                    System.out.println("Wrong login or password. Try again? (y/n)");
                    String ans = _bR.readLine();
                    if (ans.equalsIgnoreCase("n")) {
                        flag = false;
                        _dout.writeUTF("end");
                        System.out.println("Authorization failed.");
                    }
                    else
                        _dout.writeUTF("continue");
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override // обновление файла
    public void updateFile() {
        try {
            String ans = _din.readUTF();
            String filename;
            if (ans.equals("notfound")){ // первый запуск
                boolean flag = true;
                while (flag) {
                    System.out.print("Enter file name: ");
                    filename = _bR.readLine();
                    // проверка существования файла
                    _controlFile = new File(_folder, filename);
                    if (!_controlFile.exists()) {
                        System.out.println("File not found. Try again? (y/n)");
                        if(_bR.readLine().equalsIgnoreCase("n")){
                            _dout.writeUTF("end");
                            System.out.println("The transfer was canceled.");
                            return;
                        }
                    }
                    else{
                        flag = false;
                        System.out.println("The transfer in progress.");
                        _dout.writeUTF(filename);
                        baseSend(_controlFile);
                        System.out.println("The transfer was successful.");
                    }
                }
            }
            else{
                filename = _din.readUTF();
                // проверка существования файла
                _controlFile = new File(_folder, filename);
                if (!_controlFile.exists()) {
                    System.out.println("File not found. Did you change the name of file? " +
                            "Anyway, the update failed. Try again.");
                    _dout.writeUTF("end");
                    return;
                }
                else{
                    _dout.writeUTF(_hash.get(_controlFile.getAbsolutePath()));
                    if(_din.readUTF().equals("notrequired")){
                        System.out.println("The update is not required. You have the latest version of the file.");
                        return;
                    }
                    System.out.println("The update in progress.");
                    baseSend(_controlFile);
                    System.out.println("The update was successful.");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // откат версии файла пользователя
    public void rollbackFile() {
        try {
            if (_din.readUTF().equals("start")){
                System.out.println("Please, choose the number of version. If you change your mind, please write 'cancel' or something else.");
                String msg = _din.readUTF();
                while (!msg.equals("end")){
                    System.out.println("\t"+msg);
                    msg = _din.readUTF();
                }
                msg = _bR.readLine(); // ответ пользователя
                if(msg.equalsIgnoreCase("cancel")){
                    _dout.writeUTF("cancel");
                    System.out.println("The version rollback is canceled.");
                    return;
                }
                _dout.writeUTF(msg);
                if(_din.readUTF().equals("wrong")){
                    System.out.println("The version rollback is canceled.");
                    return;
                }
                String filename = _din.readUTF();
                if (_controlFile == null) {
                    _controlFile = new File(_folder, filename);
                    if(_controlFile.exists()) _controlFile.delete();
                    _controlFile.createNewFile();
                }
                baseReceive(_controlFile);
                System.out.println("The rollback of the version was successfully completed.");
            }
            else{
                System.out.println("There are no previous versions of file. The version rollback is canceled.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // выход из системы пользователя
    public void logOut(String name) {
        try {
            System.out.println("Do you really want to log out? (y/n)");
            if(_bR.readLine().equalsIgnoreCase("y")){
                _dout.writeUTF(name);
                System.out.println("Bye-bye, " + _username);
                _isAuthorized = false;
                _username = null;
                _folder = null;
            }
            else{
                _dout.writeUTF("cancel");
                System.out.println("You are still authorized.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
