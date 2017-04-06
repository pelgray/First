package app;

import netutils.MessageHandler;

/**
 * Created by 1 on 31.03.2017.
 */
public class PrintMessageHandler implements MessageHandler {
     @Override
    public void handle(String message) {
        System.out.println(message);
    }
}
