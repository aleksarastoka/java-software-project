package raf.graffito.dsw.core.message;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/*
    Objekat koji predstavlja poruku
 */
public class Message {
    @Getter
    private String content;
    @Getter
    private Type type;
    private LocalDateTime timestamp;

    public Message(String content, Type type) {
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
        return String.format("[%s][%s] %s", type, timestamp.format(formatter), content);
    }

}
