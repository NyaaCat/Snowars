package cat.nyaa.snowars.event;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class TickTask extends BukkitRunnable{
    private int tickedTimes = 0;
    private Predicate<TickEvent> predicate;

    public TickTask(Predicate<TickEvent> shouldRemove) {
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

    @Override
    public void run() {
        int ticked = getTicked();
        this.run(ticked);
    }

    public abstract void run(int ticked);

    private int getTicked() {
        return tickedTimes;
    }
}
