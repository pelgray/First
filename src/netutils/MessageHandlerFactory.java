package netutils;

/**
 * Created by 1 on 06.04.2017.
 */
public interface MessageHandlerFactory {
    MessageHandler create(String parent, FTPmethods ftp);
}

/*
*
*
*
* MessageHandlerFactory{
*   MessageHandler  (String type){MsgHandler;
*   String type = ab;
*   switch (type){
*       case ab: msgHandler = new PrintHandler();
*       case bc: msgHandler = new AnotherHandler();
*   }
*
*   Factory.create(type);
*   }
*   return MessageHandler
*
* наш клиент фабрики - сервер. хэндлеры - продукты
*
*
*
* */