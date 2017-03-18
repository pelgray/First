import java.util.LinkedList;

/**
 * Created by 1 on 17.03.2017.
 */
public class Channel<T> {

    private final int _maxCount; // максимальное количество сессий, которое мы готовы принять на канал
    private final LinkedList<T> _queue = new LinkedList<>();

    private final Object _lock = new Object();

    public Channel(int maxCount) {
        _maxCount = maxCount;
    }

    public int getSizeOfQueue(){
        return _queue.size();
    }

    public void put(T x){
        synchronized (_lock) {
            while(_maxCount <= _queue.size()) {
                try {
                    _lock.wait();
                } catch (InterruptedException e) {
                    System.err.println("Channel: The error of waiting in 'put'-condition.");
                }
            }
            _queue.addLast(x);
            _lock.notifyAll();
        }
    }
    public T take(){
        synchronized (_lock) {
            while (_queue.isEmpty()){
                try {
                    _lock.wait();
                } catch (InterruptedException e) {
                    System.err.println("Channel: The error of waiting in 'take'-condition.");
                }
            }
            _lock.notifyAll();
            return _queue.removeFirst();
        }
    }

}
