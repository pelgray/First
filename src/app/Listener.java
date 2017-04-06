package app;

import concurrentutils.Channel;
import netutils.Host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 1 on 08.03.2017.
 */
public class Listener implements Runnable{
    private Channel<Runnable> _channel;
    private Host _host;
    private int _maxNumOfConn;
    public Listener (Channel<Runnable> ch, Host host, int maxNumOfConn){
        _channel = ch;
        _host = host;
        _maxNumOfConn = maxNumOfConn;
    }
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String command = "";
            try {
                command = bufferedReader.readLine();
            } catch (IOException e) {
                System.err.println("app.Listener: The error of reading from the system input stream.");
                System.exit(-1);
            }
            switch(command) {
                case "count":
                    int num = _host.get_numOfConn();
                    String ans = "";
                    if (num > _maxNumOfConn) ans+=", real conn = " + _maxNumOfConn;
                    if (num == _maxNumOfConn) ans+=" (max real conn)";
                    System.out.println(num + ans);
                    break;
                case "max":
                    System.out.println(_maxNumOfConn);
                    break;
                case "queue":
                    System.out.println(_channel.getSize());
                    break;
                case "help":
                    System.out.println("    'count' - number of request at the moment" +
                            "\n    'max' - maximum number of connections" +
                            "\n    'queue' - current size of the channel queue");
                    break;
                default:
                    System.err.println("app.Listener:   Wrong command. Please use command 'help' for find out more.");
                    break;
            }
        }
    }
}
