package cat.nyaa.snowars.utils;

import cat.nyaa.snowars.SnowarsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class Utils {
    static final Random random = new Random();
    static final Vector x_axis = new Vector(1,0,0);
    static final Vector y_axis = new Vector(0,1,0);
    static final Vector z_axis = new Vector(0,0,1);


    public static Vector cone(Vector direction, double cone){
        double phi = Utils.random() * 360;
        double theta = Utils.random() * cone;
        Vector clone = direction.clone();
        Vector crossP;

        if (clone.length() == 0) return direction;

        if (clone.getX() != 0 && clone.getZ() != 0) {
            crossP = clone.getCrossProduct(y_axis);
        } else if (clone.getX() != 0 && clone.getY() != 0) {
            crossP = clone.getCrossProduct(z_axis);
        } else {
            crossP = clone.getCrossProduct(x_axis);
        }
        crossP.normalize();

        clone.add(crossP.multiply(Math.tan(Math.toRadians(theta))));
        clone.rotateAroundAxis(direction, Math.toRadians(phi));
        return clone;
    }

    public static double random() {
        return random.nextDouble();
    }

    public static void removeLater(Entity splitedSnowball, int delay) {
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!splitedSnowball.isDead()){
                    splitedSnowball.remove();
                }
            }
        }.runTaskLater(SnowarsPlugin.plugin, delay);
    }
}
