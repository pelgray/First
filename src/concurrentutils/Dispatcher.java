package concurrentutils;

/**
 * Created by 1 on 17.03.2017.
 */
public class Dispatcher implements Runnable {
    private Channel<Runnable> _channel;
    private ThreadPool _threadPool;
    private boolean _isActive = false;

    public Dispatcher(Channel<Runnable> channel, ThreadPool threadPool){
        _channel = channel;
        _threadPool = threadPool;
        _isActive = true;
    }

    public void run() {
        while(_isActive) {
            Runnable task = _channel.take();
            _threadPool.execute(task);
        }
    }
}
