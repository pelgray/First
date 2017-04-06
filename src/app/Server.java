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
        // порт выбираем в интервале  1 025..65 535; 0 - автоматический выбор свободного порта
        int portNum; // номер порта
        try {
            portNum = Integer.parseInt(args[0]); // получение номера порта из аргументов
        } catch (NumberFormatException e) {
            System.err.println("app.Server: Wrong port format. Should be integer. Try again.");
            return;
        }
        int maxNumOfConn;
        try {
            maxNumOfConn = Integer.parseInt(args[1]); // получение максимального количества подключений
        } catch (NumberFormatException e) {
            System.err.println("app.Server: Wrong maximum number of connections format. Should be integer. Try again.");
            return;
        }

        Class classMessageHandlerFactory;
        try {
            classMessageHandlerFactory = Class.forName(args[2]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        MessageHandlerFactory mHF;
        try {
            mHF = (MessageHandlerFactory) classMessageHandlerFactory.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        Channel<Runnable> channel = new Channel<>(maxNumOfConn);
        Thread server = new Thread(new Host(portNum, channel, mHF));
        server.setName("HOST");
        server.start();

        ThreadPool threadPool = new ThreadPool(maxNumOfConn);

        Thread dispatcher = new Thread(new Dispatcher(channel, threadPool));
        dispatcher.setDaemon(true);
        dispatcher.setName("DISPATCHER");
        dispatcher.start();
    }
}
