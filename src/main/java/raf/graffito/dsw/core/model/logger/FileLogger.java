package raf.graffito.dsw.core.model.logger;

import raf.graffito.dsw.core.message.Message;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
    Loguje poruku u log.txt, gde se cuvaju
 */
public class FileLogger implements Logger{


    @Override
    public void update(Message notification) {
        String filePath = "src/main/resources/log.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(notification.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
