package concurrentutils;

/**
 * Created by 1 on 17.03.2017.
 */
public class Dispatcher implements Stoppable {
    private Channel<Stoppable> _channel;
    private ThreadPool _threadPool;
    private boolean _isActive = false;
    private Thread _thread;

    public Dispatcher(Channel<Stoppable> channel, ThreadPool threadPool){
        _channel = channel;
        _threadPool = threadPool;
        _isActive = true;
        _thread = Thread.currentThread();
    }

    public void run() {
        while(_isActive) {
            Stoppable task = _channel.take();
            _threadPool.execute(task);
        }
    }

    @Override
    public void stop() {
        if (_isActive){
            _isActive = false;
            _thread.interrupt();
            System.out.println("\tThe dispatcher was stopped.");
        }
    }
}
