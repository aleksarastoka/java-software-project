package raf.graffito.dsw.core.model.logger;

import raf.graffito.dsw.core.message.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
    Ispisuje poruku u konzoli
 */
public class ConsoleLogger implements Logger{
    @Override
    public void update(Message notification) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
        String date = sdf.format(new Date());

        System.out.println("[" + notification.getType() + "]" + "[" + date + "] " + notification.getContent());
    }
}
