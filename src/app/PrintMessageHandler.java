package app;

import netutils.MessageHandler;

/**
 * Created by 1 on 31.03.2017.
 */
public class PrintMessageHandler implements MessageHandler {
     @Override
    public void handle(String name, String message) {
        System.out.println("        msg from (" + name + "): " + message);
    }

    @Override
    public void handleError(String error) {
        try {
            throw new Exception("Who called me?");
        }
        catch( Exception e )
        {
            System.err.println(e.getStackTrace()[1].getClassName() + ": " + error);
        }
    }
}
