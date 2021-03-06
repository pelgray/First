package app;

import netutils.MessageHandler;

/**
 * Created by 1 on 31.03.2017.
 */
public class ServerPrintMessageHandler implements MessageHandler {
     @Override
    public void handle(String name, String message) {
        System.out.println("        msg from (" + name + "): " + message);
    }

    @Override
    public void handle(String message) {
        System.out.println("        msg: " + message);
    }
}
