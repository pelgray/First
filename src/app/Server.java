package app;

import concurrentutils.Channel;
import concurrentutils.Dispatcher;
import concurrentutils.Stoppable;
import concurrentutils.ThreadPool;
import netutils.*;

import static netutils.MessageErrorType.DETAILED;
import static netutils.MessageErrorType.STANDARD;

/**
 * Created by 1 on 18.03.2017.
 */
public class Server {

    public static void stop(){
        System.exit(0);
    }

    public static void main(String[] args) { // в аргументах: номер порта, количество возможных подключений

        Class classMessageHandlerFactory;
        try {
            classMessageHandlerFactory = Class.forName(args[2]);
        } catch (ClassNotFoundException e) {
            System.err.println("app.Server: Wrong name of class MessageHandlerFactory. Class not found. Try again.");
            return;
        }

        MessageHandlerFactory mHF;
        try {
            mHF = (MessageHandlerFactory) classMessageHandlerFactory.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("app.Server: The error of creating a new instance of class MessageHandlerFactory.");
            return;
        }

        LogMessageErrorWriter errorWriter = (new LogMessageErrorFactoryMethod()).getWriter("default", STANDARD);
        if (errorWriter == null){
            System.err.println("app.Server: The error of getting the errorWriter. Check the name or type.");
            return;
        }

        // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // получение номера порта из аргументов
        } catch (NumberFormatException e) {
            errorWriter.write("Wrong port format. Should be integer. Try again.");
            return;
        }
        int maxNumOfConn;
        try {
            maxNumOfConn = Integer.parseInt(args[1]); // получение максимального количества подключений
        } catch (NumberFormatException e) {
            errorWriter.write("Wrong maximum number of connections format. Should be integer. Try again.");
            return;
        }

        Channel<Stoppable> channel = new Channel<>(maxNumOfConn, errorWriter);
        Host classHost = new Host(portNum, channel, mHF, errorWriter);
        Thread host = new Thread(classHost);
        host.setName("HOST");
        host.start();

        Listener classListener = new Listener(channel, classHost, maxNumOfConn, errorWriter);
        Thread listener = new Thread(classListener);
        listener.setName("LISTENER");
        listener.start();

        ThreadPool threadPool = new ThreadPool(maxNumOfConn, errorWriter);

        Dispatcher classDispatch = new Dispatcher(channel, threadPool);
        Thread dispatcher = new Thread(classDispatch);
        dispatcher.setName("DISPATCHER");
        dispatcher.start();

        System.out.println("Ready for work.");
        System.out.println("[INFO]   Please turn off the server properly: use the 'stop' command. Thank you.");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Shutting down...");
            classHost.stop();
            classDispatch.stop();
            threadPool.stop();
            classListener.stop();
            System.out.println("The Server gonna be stop now. Bye-bye!");
        }

        ));
    }
}

