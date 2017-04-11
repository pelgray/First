package concurrentutils;

import netutils.LogMessageErrorWriter;
import netutils.MessageHandler;

import java.util.LinkedList;

/**
 * Created by 1 on 24.03.2017.
 */
public class ThreadPool {
    private final LinkedList<Stoppable> _allWorkers = new LinkedList<>(); // база работников
    private final Channel<Stoppable> _freeWorkers; // те потоки, кот будут исполнять наши задачи. если свободных нет, ждем (свободные рабочие)
    private final int _maxSize;
    private final LogMessageErrorWriter _errorWriter;
    private final Object _lock = new Object();

    public ThreadPool(int maxSize, LogMessageErrorWriter errorWriter){
        _maxSize = maxSize;
        _errorWriter = errorWriter;
        _freeWorkers = new Channel<>(_maxSize, errorWriter);

        WorkerThread worker = new WorkerThread(this, errorWriter);
        _allWorkers.addLast(worker);
        _freeWorkers.put(worker);
    }
    public void execute(Stoppable task){ // его вызывает dispatcher. принимаем задание, инициируем выполнение
        // если нет свободных воркеров и можем создать еще один, создаем, кладем в очередь.
        synchronized (_lock) {
            if (_freeWorkers.getSize() == 0) {
                if (_allWorkers.size() < _maxSize) {
                    WorkerThread worker = new WorkerThread(this, _errorWriter);
                    _allWorkers.addLast(worker);
                    _freeWorkers.put(worker);
                }
            }
        }
        // берем свободного воркера, даем ему задание
        ((WorkerThread) _freeWorkers.take()).execute(task);
    }
    public void onTaskCompleted (WorkerThread workerThread){
        // кладем свободного воркера в очередь
        _freeWorkers.put(workerThread);
    }


    public void stop(){
        int size = _allWorkers.size();
        for (int i = 0; i < size; i++){
            _allWorkers.removeFirst().stop();
        }
    }
}
