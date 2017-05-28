package app;

import concurrentutils.Channel;
import concurrentutils.Stoppable;
import netutils.Host;
import netutils.LogMessageErrorWriter;
import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 1 on 08.03.2017.
 */
public class Listener implements Stoppable{
    private Channel<Stoppable> _channel;
    private Host _host;
    private int _maxNumOfConn;
    private LogMessageErrorWriter _errorWriter;
    private boolean _isActive;
    public Listener (Channel<Stoppable> ch, Host host, int maxNumOfConn, LogMessageErrorWriter errorWriter){
        _channel = ch;
        _host = host;
        _maxNumOfConn = maxNumOfConn;
        _isActive = true;
        _errorWriter = errorWriter;
    }
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (_isActive){
            String command;
            try {
                command = bufferedReader.readLine();
            } catch (IOException e) {
                _errorWriter.write("The error of reading from the system input stream.");
                return;
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
                case "stop":
                    System.out.println("Are you sure? (y/n)");
                    try {
                        command = bufferedReader.readLine().toLowerCase();
                    } catch (IOException e) {
                        _errorWriter.write("The error of reading from the system input stream.");
                        return;
                    }
                    switch (command) {
                        case "y":
                            Server.stop();
                            break;
                        case "n":
                            System.out.println("The server was not stopped.");
                            break;
                        default:
                            System.err.println("Wrong answer! :)\nTry again.");
                            break;
                    }
                    break;
                case "help":
                    System.out.println("    'count' - number of request at the moment" +
                            "\n    'max' - maximum number of connections" +
                            "\n    'queue' - current size of the channel queue" +
                            "\n    'stop' - stop the server");
                    break;
                default:
                    System.err.println("Wrong command. Please use command 'help' for find out more.");
                    break;
            }
        }
    }

    @Override
    public void stop() {
        if(_isActive){
            _isActive = false;
            System.out.println("\tThe listener was stopped.");
        }
    }
}
