/**
 * Created by 1 on 17.03.2017.
 */
public class Dispatcher implements Runnable {
    private Channel<Runnable> _channel;

    public Dispatcher( Channel<Runnable> channel){
        _channel = channel;
    }

    public void run() {
        while(true) {
            Session session = (Session) _channel.take();

            // создаем поток для клиента
            Thread client = new Thread(session);

            // обзываем поток
            client.setName(session.getName());

            // запускаем
            client.start();
        }

    }
}
