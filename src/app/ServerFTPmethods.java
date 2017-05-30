package app;

import netutils.FTPmethods;
import netutils.LogMessageErrorWriter;

import java.io.*;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by 1 on 26.05.2017.
 */
public class ServerFTPmethods implements FTPmethods {
    private String _name; // имя, сложенное из хоста и порта, либо username
    private LogMessageErrorWriter _errorWriter;

    private DataOutputStream _dout;
    private DataInputStream _din;
    private File _folder;
    private File _users;
    private File _files;
    private Checksum _hash = new Checksum();
    private boolean _isAuthorized = false;

    public ServerFTPmethods(DataInputStream din, DataOutputStream dout, String name, LogMessageErrorWriter errorWriter){
        _din = din;
        _dout = dout;
        _name = name;
        _errorWriter = errorWriter;

        _folder = new File("c:" +
                File.separator + "TEST_FTP_SERVER");
        if (!_folder.exists()) {
            _folder.mkdir();
        }
        _users = new File(_folder, "users.txt");
        if (!_users.exists()) try {
            _users.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _files = new File(_folder, "files.txt");
        if (!_files.exists()) try {
            _files.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get_name(){
        return _name;
    }

    @Override // пересылка файла file
    public void baseSend(File file) {
        FileInputStream fin;
        try {
            fin = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        int ch;
        do {
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
        while (ch != -1);

        try {
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // прием файла file
    public void baseReceive(File file) {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        int ch;
        String temp;
        try {
            do{
                temp = _din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch!=-1){
                    fout.write(ch);
                }
            }
            while(ch != -1);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile() {
        String filename;
        try {
            filename = _din.readUTF();
        } catch (SocketException e) {
            if (e.getMessage().equals("Socket closed"))
                return;
            else if (e.getMessage().equals("Connection reset"))
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
        if (!file.exists()) {
            try {
                _dout.writeUTF("File Not Found");
            } catch (IOException e) {
                System.err.println("The connection with waiting Client (" + _name + ") was lost.");
                e.printStackTrace();
                return;
            }

            System.out.println("\tThe file " + filename + " not found.");
            return;
        } else { // если существует
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

            baseSend(file);

            try {
                _dout.writeUTF("File was received successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("The file " + filename + " was successfully sent to the client (" + _name + ").");
        }
    }

    @Override
    public void receiveFile() {
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
                baseReceive(file);
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

    @Override // регистрация нового пользователя
    public void registration() {
        try {
            String username = _din.readUTF();
            while(checkUsername(username)){
                _dout.writeUTF("exist");
                username = _din.readUTF();
            }
            _dout.writeUTF("good");
            String pass = _din.readUTF();
            FileWriter fout = new FileWriter(_users, true);
            fout.write(username+" "+pass+System.lineSeparator());
            fout.close();
            _folder = new File("c:" +
                    File.separator + "TEST_FTP_SERVER" +
                    File.separator + username);
            if (!_folder.exists()) {
                _folder.mkdir();
            }
            _dout.writeUTF("finished");
            System.out.println("New user '"+username+"'!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // авторизация пользователя
    public void authorization() {
        try {
            boolean finished = false;
            while(!finished) {
                boolean flag = false;
                String control_name = _din.readUTF();
                String control_pass = _din.readUTF();
                BufferedReader fin = new BufferedReader(new FileReader(_users));
                while (fin.ready()) {
                    if ((control_name+" "+control_pass).equalsIgnoreCase(fin.readLine())) {
                        _dout.writeUTF("correct");
                        _folder = new File("c:" +
                                File.separator + "TEST_FTP_SERVER" +
                                File.separator + control_name);
                        if (!_folder.exists()) {
                            _folder.mkdir();
                        }

                        _name = control_name;
                        System.out.println("User '"+_name+"' authorized!");
                        _isAuthorized = true;
                        flag = true;
                        break;
                    }
                }
                if (!flag) _dout.writeUTF("wrong");
                fin.close();
                if (_din.readUTF().equals("end")) finished = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // обновление файла
    public void updateFile() {
        try {
            String filename = getNameFile(_name);
            if (filename.equals("false")) { // еще не добавлено ни одной версии
                _dout.writeUTF("notfound");
                filename = _din.readUTF();
                if(filename.equals("end")){
                    return;
                }
                FileWriter fout = new FileWriter(_files, true);
                fout.write(_name+" "+filename+System.lineSeparator());
                fout.close();
            }
            else { //название файла найдено
                _dout.writeUTF("found");
                _dout.writeUTF(filename); // отправка имени
                if(filename.equals("end")){
                    return;
                }
                // здесь прием хеша и сверка
                if(_din.readUTF().equals(getHashLastVers())){
                    _dout.writeUTF("notrequired");
                    return;
                }
                _dout.writeUTF("required");
            }

            String format = filename.substring(filename.lastIndexOf("."));
            String newFilename = DateFormat.getDateTimeInstance().format(new Date()).replace(' ','_').replaceAll(":","") + format;
            System.out.println("Uploaded a new version ["+newFilename+"] of file ["+ filename +"] from user '"+_name+"'");
            File controlFile = new File(_folder, newFilename);
            controlFile.createNewFile();
            baseReceive(controlFile);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override // откат версии файла пользователя
    public void rollbackFile() {
        try {
            File[] listFiles = _folder.listFiles();
            if (listFiles.length != 0) {
                _dout.writeUTF("start");
                int i = 0;
                for (File file : listFiles) {
                    _dout.writeUTF(i + ") " + new Date(file.lastModified()).toString());
                    i++;
                }
                _dout.writeUTF("end");
                String t = _din.readUTF();
                if (t.equals("cancel")) return;
                int ans;
                try {
                    ans = Integer.parseInt(t);
                }
                catch(NumberFormatException e){
                    _dout.writeUTF("wrong");
                    return;
                }
                if (ans < 0 || ans >= i) {
                    _dout.writeUTF("wrong");
                    return;
                }


                _dout.writeUTF("ready");
                String filename = getNameFile(_name);
                _dout.writeUTF(filename); // отправка имени
                baseSend(listFiles[ans]);
            }
            else{
                _dout.writeUTF("nofiles");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // выход из системы пользователя
    public void logOut(String name) {
        try {
            String temp = _din.readUTF();
            if (!temp.equals("cancel")) {
                System.out.println("User '"+_name+"' logged out.");
                _name = temp;
                _folder = null;
                _isAuthorized = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAuthorized() {
        return _isAuthorized;
    }

    // проверка наличия ника в системе
    private boolean checkUsername(String name){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(_users);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] content = new byte[0];
        try {
            content = new byte[fis.available()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.read(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] lines = new String(content).split("\n");
        for (String line : lines) {
            String[] words = line.split(" ");
            int j = 1;
            for (String word : words) {
                if (word.equalsIgnoreCase(name) && j == 1) {
                    return true; // такой пользователь уже есть
                }
                j++;
            }
        }
        return false; // пользователя с таким именем нет
    }

    // получение имени файла клиента, для которого поддерживается контроль версий
    private String getNameFile(String username){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(_files);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] content = new byte[0];
        try {
            content = new byte[fis.available()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.read(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] lines = new String(content).split("\n");
        boolean flag = false;
        for (String line : lines) {
            String[] words = line.split(" ");
            int j = 1;
            for (String word : words) {
                if (flag) return word.replaceAll("\r","");
                if (word.equalsIgnoreCase(username) && j == 1) {
                    flag = true; // нужный пользователь, дальше будет название
                }
                j++;
            }
        }
        return "false"; // пользователя с таким именем нет
    }

    // получение хеш-значения последней версии файла на сервере
    private String getHashLastVers(){
        File[] listFiles = _folder.listFiles();
        String hash = _hash.get(listFiles[0].getAbsolutePath());
        long temp = listFiles[0].lastModified();
        for (File file: listFiles) {
            if (temp<file.lastModified())  {
                temp = file.lastModified();
                hash = _hash.get(file.getAbsolutePath());
            }
        }
        return hash;
    }

}
