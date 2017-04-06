package app;

import netutils.MessageHandler;
import netutils.MessageHandlerFactory;

/**
 * Created by 1 on 06.04.2017.
 */
public class PrintMessageHandlerFactory implements MessageHandlerFactory{
    @Override
    public MessageHandler create() {
        // создаем новый и возвращаем
        return new PrintMessageHandler();
    }
}
