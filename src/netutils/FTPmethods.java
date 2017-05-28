package netutils;

import java.io.File;

/**
 * Created by 1 on 26.05.2017.
 */
public interface FTPmethods {
    void baseSend(File file);
    void baseReceive(File file);
    void sendFile();
    void receiveFile();
    void registration();
    void authorization();
    void updateFile();
    void rollbackFile();
    void logOut(String name);
    boolean isAuthorized();
}
