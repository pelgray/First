package app;

import netutils.FTPmethods;
import netutils.MessageHandler;

/**
 * Created by 1 on 31.03.2017.
 */
public class ServerFTPMessageHandler implements MessageHandler {
    ServerFTPmethods _ftp;

    private boolean _isAuthorized = false;

    ServerFTPMessageHandler(FTPmethods ftp){
        _ftp = (ServerFTPmethods) ftp;
    }

     @Override
    public void handle(String name, String message) {
         handle(message);
    }

    @Override
    public void handle(String message) {
        switch (message) {
            case "#update":
                if(_isAuthorized)
                    _ftp.updateFile();
                break;
            case "#rollback":
                if(_isAuthorized)
                    _ftp.rollbackFile();
                break;
            case "#reg":
                _ftp.registration();
                break;
            case "#log":
                _ftp.authorization();
                if (_ftp.isAuthorized()) {
                    //_ftp.get_name();
                    _isAuthorized = true;
                }
                break;
            case "#logout":
                if(_isAuthorized) {
                    _ftp.logOut();
                    if (!_ftp.isAuthorized()) {
                        //_name = _ftp.get_name();
                        _isAuthorized = false;
                    }
                }
                break;
        }
    }
}
