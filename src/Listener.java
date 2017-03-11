import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 1 on 08.03.2017.
 */
public class Listener implements Runnable{
    public Listener (){
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
                case "how many":
                    System.out.println(Server.getNumOfConn());
                    break;
                case "maxconn":
                    System.out.println(Server.getMaxNumOfConn());
                    break;
                case "state":
                    System.out.println(Server.server.getState());
                    break;
                case "help":
                    System.out.println("    'how many' - number of connections at the moment\n    'maxconn' - maximum number of connections\n    'state' - current state of Server");
                    break;
                default:
                    System.err.println("Listener:   Wrong command. Please use command 'help'.");
                    break;
            }
        }
    }
}
