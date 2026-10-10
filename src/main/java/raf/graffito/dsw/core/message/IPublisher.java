package raf.graffito.dsw.core.message;
/*
    Objekat koji reaguje na promene
 */
public interface IPublisher<T> {
    void addSubscriber(ISubscriber<T> subscriber);
    void removeSubscriber(ISubscriber<T> subscriber);
    void notifySubscribers(T object);
}
