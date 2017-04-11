package concurrentutils;

import netutils.LogMessageErrorWriter;

/**
 * Created by 1 on 24.03.2017.
 */
public class WorkerThread implements Stoppable {
    private final Thread _thread;
    private final ThreadPool _threadPool;
    private Stoppable _currentTask = null;
    private LogMessageErrorWriter _errorWriter;
    private boolean _isActive;

    private final Object _lock = new Object();

    public WorkerThread(ThreadPool pool, LogMessageErrorWriter errorWriter){
        _threadPool = pool;
        _isActive = true;
        _errorWriter = errorWriter;
        _thread = new Thread(this);
        _thread.start();
    }
    public void execute(Stoppable task){
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
            while(_isActive) {
                while (_currentTask == null) {
                    try {
                        _lock.wait();
                    } catch (InterruptedException e) {
                        if (!_isActive) return;
                        _errorWriter.write("The error of waiting.");
                    }
                }
                try { // если пришла задача
                    _currentTask.run();
                }catch(RuntimeException e){
                    _errorWriter.write("The error of running a new task.");
                }
                finally {
                    // в конце
                    _currentTask = null;
                    _threadPool.onTaskCompleted(this);
                }
            }
        }
    }

    @Override
    public void stop() {
        if (_isActive){
            _isActive = false;
            _thread.interrupt();
            if (_currentTask != null) _currentTask.stop();
            System.out.println("\tThe workerThread (" + _thread.getName() + ") was stopped.");
        }
    }
}
