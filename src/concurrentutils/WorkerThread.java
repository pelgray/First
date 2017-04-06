package concurrentutils;

/**
 * Created by 1 on 24.03.2017.
 */
public class WorkerThread implements Runnable {
    private final Thread _thread;
    private final ThreadPool _threadPool;
    private Runnable _currentTask = null;

    private final Object _lock = new Object();

    public WorkerThread(ThreadPool pool){
        _threadPool = pool;
        _thread = new Thread(this);
        _thread.start();
    }
    public void execute(Runnable task){
        synchronized (_lock) {
            if (_currentTask != null){
                new IllegalStateException();
            }
            _currentTask = task;
            _lock.notifyAll();
        }
    }
    public void run(){ // если нет задачи на выполнения, ждем
        // если есть, выполняем
        synchronized (_lock) {
            while(true) {
                while (_currentTask == null) {
                    try {
                        _lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try { // если пришла задача
                    _currentTask.run();
                }catch(RuntimeException e){
                    e.printStackTrace();
                }
                finally {
                    // в конце
                    _currentTask = null;
                    _threadPool.onTaskCompleted(this);
                }
            }
        }
    }
}
