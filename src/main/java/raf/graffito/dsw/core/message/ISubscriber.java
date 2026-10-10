package raf.graffito.dsw.core.message;
/*
    Objekat koji emituje promene
 */
public interface ISubscriber<T> {
    void update(T notification);
}
