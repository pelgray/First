package app;

import netutils.FTPmethods;
import netutils.MessageHandler;
import netutils.MessageHandlerFactory;

/**
 * Created by 1 on 06.04.2017.
 */
public class FTPMessageHandlerFactory implements MessageHandlerFactory{
    @Override
    public MessageHandler create(String parent, FTPmethods ftp) {
        // создаем новый и возвращаем
        if (parent.equals("server")) return new ServerFTPMessageHandler(ftp);
        if (parent.equals("client")) return new ClientFTPMessageHandler(ftp);
        return null;
    }
}
