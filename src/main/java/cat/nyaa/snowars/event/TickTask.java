package cat.nyaa.snowars.event;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class TickTask {
    private final BukkitRunnable runnable;
    private BukkitRunnable task;
    private int tickedTimes = 0;
    private Predicate<TickEvent> predicate;

    public TickTask(BukkitRunnable runnable, Predicate<TickEvent> shouldRemove) {
        this.runnable = runnable;
        predicate = shouldRemove;
    }

    public int getTickedAndIncrement() {
        int tickedTimes = this.tickedTimes;
        this.tickedTimes = this.tickedTimes +1;
        return tickedTimes;
    }

    public Predicate<TickEvent> getPredicate() {
        return predicate;
    }

    public BukkitRunnable getRunnable() {
        return runnable;
    }
}
