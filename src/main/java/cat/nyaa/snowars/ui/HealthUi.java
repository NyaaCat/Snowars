package cat.nyaa.snowars.ui;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.event.Ticker;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class HealthUi {
    private static final HealthUi INSTANCE = new HealthUi();
    private UiRefresher tickTask;
    private int currentTick = 0;

    Map<UUID, HealthRecord> healthMap;
    Queue<Player> playerQueue;

    private HealthUi() {
        healthMap = new HashMap<>();
        playerQueue = new LinkedList<>();
        Ticker.getInstance().register(new TickTask(tickEvent -> false) {
            @Override
            public void run(int ticked) {
                currentTick = ticked;
                healthMap.forEach((uuid, healthRecord) -> {
                    healthRecord.update(ticked);
                });
            }
        });
    }

    public static HealthUi getInstance() {
        return INSTANCE;
    }

    public void start() {
        stop();
        if (tickTask == null) {
            tickTask = new UiRefresher();
        }
        tickTask.runTaskTimer(SnowarsPlugin.plugin, 0, 1);
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private void refreshPlayer(Player poll) {
        Message message = new Message("");
        buildPlayerHealth(message, poll);
        split(message);
        buildPlayerScore(message, poll);
        message.send(poll, Message.MessageType.ACTION_BAR);
    }

    private void split(Message message) {
        message.append(colored("  &e|  "));
    }

    private void buildPlayerScore(Message message, Player poll) {
        ScoreManager scoreManager = ScoreManager.getInstance();
        double score = scoreManager.getScore(poll);
        double teamScore = scoreManager.getTeamScore(poll);
        StringBuilder sb = new StringBuilder();
        sb.append("&b&lScore ").append(String.format("&a%4.0f ", score));
        sb.append("&e&lTeam ").append(String.format("&6%4.0f ", teamScore));
        message.append(colored(sb.toString()));
    }

    private void buildPlayerHealth(Message message, Player poll) {
        double maxHealth = 100d;
        HealthRecord health = healthMap.computeIfAbsent(poll.getUniqueId(), uuid -> new HealthRecord(poll, 100d, 0, 0));
        double total = maxHealth / 5;
        int barCount = (int) Math.ceil(health.health / 5);
        message.append(colored("&c&lHEALTH "));
        StringBuilder healthbar = new StringBuilder("&");
        if (barCount <= 6) healthbar.append("c");
        if (barCount <= 12 && barCount > 6) healthbar.append("6");
        if (barCount > 12) healthbar.append("a");
        healthbar.append("&l");
        for (int i = 0; i < barCount; i++) {
            healthbar.append("❱");
        }
        double damaged = health.damaged;
        int damagedSig = (int) Math.ceil(Math.min(damaged / 5, total - barCount));
        healthbar.append("&4&l");
        for (int i = 0; i < damagedSig; i++) {
            healthbar.append("❱");
        }
        healthbar.append("&0&l");
        for (int i = 0; i < total - barCount - damagedSig; i++) {
            healthbar.append("❱");
        }
        message.append(colored(healthbar.toString()));
    }

    private String colored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public void damage(Entity entity, double damage) {
        if (damage <= 0) return;
        UUID uuid = entity.getUniqueId();
        HealthRecord currentHealth = healthMap.computeIfAbsent(uuid, uuid1 -> new HealthRecord(entity, 100d, 0, 0));
        currentHealth.damage(damage);
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).damage(0.01);
            ((LivingEntity) entity).addPotionEffect(PotionEffectType.REGENERATION.createEffect(1, 10));
        }
    }

    private void onPlayerOverdamaged(Entity entity) {
        Utils.teleportHome(entity, entity.getWorld());
    }

    class UiRefresher extends BukkitRunnable {

        @Override
        public void run() {
            if (playerQueue.isEmpty()) {
                fillPlayerQueue();
            }
            int online = SnowarsPlugin.plugin.getServer().getOnlinePlayers().size();
            int playersPerTick = Math.max(1, online / 20);
            for (int i = 0; i < playersPerTick; i++) {
                if (playerQueue.isEmpty()) {
                    break;
                }
                Player poll = playerQueue.poll();
                Team team = Utils.getTeam(poll);
                if (team != null) {
                    refreshPlayer(poll);
                }
            }
        }

        private void fillPlayerQueue() {
            Collection<? extends Player> onlines = SnowarsPlugin.plugin.getServer().getOnlinePlayers();
            playerQueue.addAll(onlines);
        }
    }

    class HealthRecord {
        Entity related;
        double health;
        double damaged;
        long lastDamaged;

        public HealthRecord(Entity related, double health, double damaged, long lastDamaged) {
            this.related = related;
            this.health = health;
            this.damaged = damaged;
            this.lastDamaged = lastDamaged;
        }

        public void update(int ticked) {
            long x = ticked - lastDamaged;
            regeneration(x);
            damaged = Math.max(0, damaged - Math.max(0, Math.min(5, (0.004761904761904764 * x * x * x - 0.21428571428571414 * x * x + 3.195238095238093 * x - 15.285714285714278))));
        }

        private void regeneration(long x) {
            if (lastDamaged == 0) return;
            double reg = Math.max(0, Math.min(2, 1.2451221120785042e-7 * x * x * x * x * x - 0.00004011664261729989 * x * x * x * x + 0.00501746603911755 * x * x * x - 0.3044655910144579 * x * x + 8.995939114771266 * x - 103.86189079863767));
            health = Math.min(100d, health + reg);
        }

        public void damage(double damage) {
            if (damage >= health) {
                onPlayerOverdamaged(related);
                revive(related);
            } else {
                health -= damage;
                damaged += damage;
                lastDamaged = currentTick;
            }
        }

        private void revive(Entity related) {
            health = 100d;
            damaged = 0;
            lastDamaged = currentTick;
        }
    }
}
