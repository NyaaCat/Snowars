package cat.nyaa.snowars.event;

import cat.nyaa.snowars.SnowarsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Ticker {
    private static Ticker INSTANCE = new Ticker();
    private SnowarsPlugin plugin;
    static BukkitRunnable tickerTask;
    static List<TickTask> tasks;

    private Ticker(){
        tasks = new ArrayList<>();
    }

    public void init(){
        plugin = SnowarsPlugin.plugin;
        if (tickerTask!=null){
            tickerTask.cancel();
        }
        tickerTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<TickTask> toRemove = new ArrayList<>();
                tasks.forEach((task) ->{
                    TickEvent tickEvent = new TickEvent(task.getTickedAndIncrement());
                    task.getRunnable().run();
                    if (task.getPredicate().test(tickEvent)){
                        toRemove.add(task);
                    }
                });
                tasks.removeAll(toRemove);
            }
        };
        tickerTask.runTaskTimer(plugin, 0, 0);
    }

    public static Ticker getInstance(){
        return INSTANCE;
    }

    public void register(BukkitRunnable runnable, Predicate<TickEvent> shouldRemove) {
        tasks.add(new TickTask(runnable, shouldRemove));
    }

    public void stop() {
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
        }
    }
}
