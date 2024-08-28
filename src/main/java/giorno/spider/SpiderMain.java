package giorno.spider;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

public final class SpiderMain extends JavaPlugin {

    public static SpiderMain instance;
    public static Location follow;
    public static Player me;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new LegDemo(), this);
        getCommand("spider").setExecutor(new LegDemo());

        me = getServer().getPlayer("th3giorn0");

        new BukkitRunnable() {
            @Override
            public void run() {
                RayTraceResult result = me.getLocation().getWorld().rayTraceBlocks(me.getEyeLocation(), me.getEyeLocation().getDirection(), 100, FluidCollisionMode.NEVER, true);
                if (result != null) {
                    follow = result.getHitPosition().toLocation(me.getWorld());
                }

                me.getWorld().spawnParticle(Particle.FLAME, follow, 2, 0, 0, 0, 0.01);

            }
        }.runTaskTimer(this, 0, 1);

        instance = this;
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


}


