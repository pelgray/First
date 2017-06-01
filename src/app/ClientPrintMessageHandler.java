package app;

import netutils.MessageHandler;

/**
 * Created by 1 on 31.03.2017.
 */
public class ClientPrintMessageHandler implements MessageHandler {
    ClientPrintMessageHandler(){
        System.out.println("\t\t\t[The client can send messages.]");
    }
     @Override
    public void handle(String name, String message) {
        System.out.println("Sent");
    }

    @Override
    public void handle(String message) {
        System.out.println("Sent");
    }
}
