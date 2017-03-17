import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 1 on 08.03.2017.
 */
public class Listener implements Runnable{
    private Channel<Runnable> _channel;
    private Thread _server;
    public Listener (Channel<Runnable> ch, Thread serv){
        _channel = ch;
        _server = serv;
    }
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String command = "";
            try {
                command = bufferedReader.readLine();
            } catch (IOException e) {
                System.err.println("Listener: The error of reading from the system input stream.");
                System.exit(-1);
            }
            switch(command) {
                case "count":
                    String ans = "";
                    if (Server.getNumOfConn() == Server.getMaxNumOfConn()) ans = " (max)";
                    System.out.println(Server.getNumOfConn() + ans);
                    break;
                case "maxconn":
                    System.out.println(Server.getMaxNumOfConn());
                    break;
                case "state":
                    System.out.println(_server.getState());
                    break;
                case "queue":
                    System.out.println(_channel.getSizeOfQueue());
                    break;
                case "help":
                    System.out.println("    'count' - number of connections at the moment" +
                            "\n    'maxconn' - maximum number of connections" +
                            "\n    'state' - current state of Server" +
                            "\n    'queue' - current size of the channel queue");
                    break;
                default:
                    System.err.println("Listener:   Wrong command. Please use command 'help'.");
                    break;
            }
        }
    }
}
