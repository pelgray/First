package concurrentutils;

import netutils.LogMessageErrorWriter;

import java.util.LinkedList;

/**
 * Created by 1 on 17.03.2017.
 */
public class Channel<T> {// здесь очередь незапущенных ждущих клиентов
    // сюда из сервера, отсюда - на диспетчер
    // диспетчер занимается запуском потока

    private final int _maxObjects; // максимальное количество сессий, которое мы готовы принять на канал
    private final LinkedList<T> _queue = new LinkedList<>();
    private LogMessageErrorWriter _errorWriter;

    private final Object _lock = new Object();

    public Channel(int maxCount, LogMessageErrorWriter errorWriter) {
        _errorWriter = errorWriter;
        _maxObjects = maxCount;
    }

    public int getSize() {
        synchronized (_lock) {
            return _queue.size();
        }
    }

    public void put(T x){
        synchronized (_lock) {
            while(_queue.size() == _maxObjects) {
                try {
                    _lock.wait();
                } catch (InterruptedException e) {
                    _errorWriter.write("The error of waiting in 'put'-condition.");
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
                    _errorWriter.write("The error of waiting in 'take'-condition.");
                }
            }
            _lock.notifyAll();
            return _queue.removeFirst();
        }
    }

}
