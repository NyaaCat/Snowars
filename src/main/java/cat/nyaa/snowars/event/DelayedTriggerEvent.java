package cat.nyaa.snowars.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

public class DelayedTriggerEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Entity from;
    private Entity related;
    private final int delay;

    private boolean canceled = false;

    public DelayedTriggerEvent(Entity from, Entity related, int delay) {
        super();
        this.from = from;
        this.related = related;
        this.delay = delay;
    }

    public Entity getFrom() {
        return from;
    }

    public Entity getRelated() {
        return related;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }
}
