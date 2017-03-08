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
                e.printStackTrace();
            }
            switch(command) {
                case "how many":
                    System.out.println(Server.numOfConn);
                    break;
                case "maxconn":
                    System.out.println(Server.maxNumOfConn);
                    break;
                case "help":
                    System.out.println("    'how many' - number of connections at the moment\n    'maxconn' - maximum number of connections allowed");
                    break;
                default:
                    break;
            }
        }
    }
}
