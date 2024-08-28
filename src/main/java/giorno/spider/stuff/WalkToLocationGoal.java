package giorno.spider.stuff;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;

public class WalkToLocationGoal extends Goal {

    private double speed;
    private Zombie entity;
    private Location loc;
    private PathNavigation navigation;

    public WalkToLocationGoal(Zombie entity, Location loc, double speed) {
        this.entity = entity;
        this.loc = loc;
        this.navigation = this.entity.getNavigation();
        this.speed = speed;
    }


    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        Path path = this.navigation.createPath(loc.getX(), loc.getY(), loc.getZ(), 1);
        this.navigation.moveTo(path, speed);
    }
}
