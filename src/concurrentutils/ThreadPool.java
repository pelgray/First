package concurrentutils;

import java.util.LinkedList;

/**
 * Created by 1 on 24.03.2017.
 */
public class ThreadPool { // создать новый канал
    private final LinkedList<Runnable> _allWorkers = new LinkedList<>(); // база работников
    private final Channel<Runnable> _freeWorkers; // те потоки, кот будут исполнять наши задачи. если свободных нет, ждем (свободные рабочие)
    private final int _maxSize;
    private final Object _lock = new Object();

    public ThreadPool(int maxSize){
        _maxSize = maxSize;
        _freeWorkers = new Channel<>(_maxSize);

        WorkerThread worker = new WorkerThread(this);
        _allWorkers.addLast(worker);
        _freeWorkers.put(worker);
    }
    public void execute(Runnable task){ // его вызывает dispatcher. принимаем задание, инициируем выполнение
        // если нет свободных воркеров и можем создать еще один, создаем, кладем в очередь.
        synchronized (_lock) {
            if (_freeWorkers.getSize() == 0) {
                if (_allWorkers.size() < _maxSize) {
                    WorkerThread worker = new WorkerThread(this);
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
}
