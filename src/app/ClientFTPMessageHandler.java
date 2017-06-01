package app;

import netutils.FTPmethods;
import netutils.MessageHandler;

/**
 * Created by 1 on 31.03.2017.
 */
public class ClientFTPMessageHandler implements MessageHandler {
    ClientFTPmethods _ftp;
    private boolean _isAuthorized = false;

    ClientFTPMessageHandler(FTPmethods ftp){
        _ftp = (ClientFTPmethods) ftp;
        System.out.println("\t\t\t[The client is started to work with a VCS.]");
        System.out.println("[INFO]\tIf you wanna work with files, use next commands:\n" +
                "\t\t'#reg' - if you are a new user;\n" +
                "\t\t'#log' - if you want to log in to system.");
    }
     @Override
    public void handle(String name, String message) {
         handle(message);
    }

    @Override
    public void handle(String message) {
        switch (message) {
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
                    _ftp.logOut();
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
                break;
            default:
                System.err.println("Wrong command.");
                break;
        }
    }
}
