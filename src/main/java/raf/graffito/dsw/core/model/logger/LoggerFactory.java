package raf.graffito.dsw.core.model.logger;

public class LoggerFactory {
    public LoggerFactory() {
    }

    public Logger createLogger(String logType) {
        if (logType.equals("console"))
            return new ConsoleLogger();

        else if (logType.equals("file"))
            return new FileLogger();

        else
            throw new IllegalArgumentException("Nepoznat logger tip: " + logType);
    }
}
