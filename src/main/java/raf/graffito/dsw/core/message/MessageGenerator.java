package raf.graffito.dsw.core.message;

import java.util.ArrayList;
import java.util.List;

public class MessageGenerator implements IPublisher<Message>{
    ///  Centralni publisher koji emituje poruke svim registrovanim subscriberima (logger i UI)
    List<ISubscriber<Message>> subscribers;

    @Override
    public void addSubscriber(ISubscriber<Message> subscriber) {
        if(subscriber == null)
            return;
        if(this.subscribers ==null)
            this.subscribers = new ArrayList<>();
        if(this.subscribers.contains(subscriber))
            return;
        this.subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(ISubscriber<Message> sub) {
        if(sub == null || this.subscribers == null || !this.subscribers.contains(sub))
            return;
        this.subscribers.remove(sub);
    }

    @Override
    public void notifySubscribers(Message message) {
        if(message == null || this.subscribers == null || this.subscribers.isEmpty())
            return;

        for(ISubscriber<Message> subscriber : subscribers){
            subscriber.update(message);
        }
    }

    // prima event, pravi Message i salje obavestenje
    public void generate(String eventCode) {
        Message m = switch (eventCode) {
            case "NODE_CANNOT_BE_ADDED" -> new Message("Please use the right panel to add Elements to the Slide", Type.WARNING);
            case "NODE_CANNOT_BE_DELETED" -> new Message("Workspace cannot be deleted", Type.ERROR);
            case "NAME_CANNOT_BE_EMPTY" -> new Message("Came cannot be empty", Type.WARNING);
            case "NODE_CANNOT_BE_RENAMED" -> new Message("Workspace cannot be renamed", Type.WARNING);
            case "NODE_IS_NOT_PROJECT" -> new Message("Selected node is not Project", Type.ERROR);
            case "NO_NODE_SELECTED" -> new Message("Select a node first", Type.WARNING);
            case "DELETE_FAILED" -> new Message("Delete failed", Type.ERROR);
            case "DUPLICATE_NODE_NAME" -> new Message("Duplicate node name", Type.ERROR);
            case "CHOOSE_SLIDE" -> new Message("You must first choose a slide", Type.ERROR);
            case "CANNOT_DELETE_SLIDE_ELEMENT_VIA_TOOL_BAR" -> new Message("Please use the right panel to remove Elements from the Slide", Type.WARNING);
            case "SLIDE_SPACE_FULL" -> new Message("Slide space full", Type.ERROR);
            default -> new Message("Unknown event", Type.INFO);
        };

        notifySubscribers(m);
    }
}
