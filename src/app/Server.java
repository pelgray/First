package app;

import concurrentutils.Channel;
import concurrentutils.Dispatcher;
import concurrentutils.ThreadPool;
import netutils.Host;
import netutils.MessageHandler;
import netutils.MessageHandlerFactory;

/**
 * Created by 1 on 18.03.2017.
 */
public class Server {

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
        MessageHandler messageHandler = mHF.create();

        // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // получение номера порта из аргументов
        } catch (NumberFormatException e) {
            messageHandler.handleError("Wrong port format. Should be integer. Try again.");
            return;
        }
        int maxNumOfConn;
        try {
            maxNumOfConn = Integer.parseInt(args[1]); // получение максимального количества подключений
        } catch (NumberFormatException e) {
            messageHandler.handleError("Wrong maximum number of connections format. Should be integer. Try again.");
            return;
        }

        Channel<Runnable> channel = new Channel<>(maxNumOfConn, messageHandler);
        Host classHost = new Host(portNum, channel, messageHandler);
        Thread server = new Thread(classHost);
        server.setName("HOST");
        server.start();

        Thread listener = new Thread(new Listener(channel, classHost, maxNumOfConn));
        listener.setDaemon(true);
        listener.setName("LISTENER");
        listener.start();

        ThreadPool threadPool = new ThreadPool(maxNumOfConn, messageHandler);

        Thread dispatcher = new Thread(new Dispatcher(channel, threadPool));
        dispatcher.setDaemon(true);
        dispatcher.setName("DISPATCHER");
        dispatcher.start();
    }
}
