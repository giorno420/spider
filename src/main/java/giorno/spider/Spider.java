package giorno.spider;

import giorno.spider.renderer.EntityRenderer;
import giorno.spider.stuff.Chain;
import giorno.spider.stuff.Segment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Husk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static giorno.spider.SpiderMain.follow;

public class Spider extends Husk {

    private final List<Chain> chains;
    private final EntityRenderer renderer;
    private Location location;
    private final Random random;

    private final int limbToHeadOffset = 4; // how far away the tip of the chain is from the zombie feet
    private final double headYOffset = 2;
    private final double horizontalTrigger = 2;

    public Spider(Location spawn, int segmentPerChain){
        super(EntityType.HUSK, ((CraftWorld)spawn.getWorld()).getHandle());
        this.chains = new ArrayList<>();
        this.renderer = new EntityRenderer();
        this.location = spawn;
        this.random = new Random();
        for (int i = 0; i < 8; i++) {
            chains.add(createChain(spawn.clone().add(0, headYOffset, 0), segmentPerChain));
        }
        ((CraftWorld)spawn.getWorld()).getHandle().addFreshEntity(this);
        this.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
        this.craftAttributes.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(this.craftAttributes.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * 1.5);
        this.setInvisible(true);
        this.getBukkitEntity().setVisibleByDefault(false);
        this.registerGoals();

        update();
        Bukkit.broadcastMessage("leg amount: " + chains.size());
    }


    private Chain createChain(Location location, int segmentAmount){
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < segmentAmount; i++) {
            double length = switch (i) {
                case 0 -> 2;
                case 1 -> 1.7;
                case 2 -> 1.3;
                case 3 -> 1.2;
                default -> 0.8;
            };
            Vector position = location.clone().toVector().add(location.clone().toVector().normalize().multiply(length * (i + 1)));
            segments.add(new Segment(length, position));
        }
        return new Chain(location.toVector(), segments);
    }


    private void moveChain(Chain chain, Location to){
        Location origin = this.getBukkitEntity().getLocation().clone().add(0, headYOffset, 0);
        Location target = checkColumn(to.clone());
        Vector direction = target.toVector().subtract(origin.toVector());
        direction.setY(0);
        Vector crossAxis = new Vector(0, 1, 0).crossProduct(direction).normalize();
        direction.rotateAroundAxis(crossAxis, Math.toRadians(-60));
        chain.straightenDirection(direction);
        chain.fabrik(to.toVector());
        chain.lastMoved = this.getBukkitEntity().getWorld().getGameTime() + random.nextInt(10);
        this.location.getWorld().getNearbyEntities(this.location, 10, 10, 10).forEach(entity -> {
            if (entity instanceof Player player){
                player.playSound(this.location, Sound.BLOCK_NETHERITE_BLOCK_FALL, 1, 1);
            }
        });
    }


    private Vector segmentUpVector(Chain chain) {
        Vector direction = chain.getEndEffector().clone().subtract(chain.getRoot());
        return direction.clone().crossProduct(new Vector(0, 1, 0));
    }


    public void render() {
        renderer.beginRender();

        for (int legIndex = 0; legIndex < chains.size(); legIndex++) {
            Chain leg = chains.get(legIndex);

            // Up vector is the cross product of the y-axis and the end-effector direction
            Vector segmentUpVector = segmentUpVector(leg);

            // Render leg segment
            for (int segmentIndex = 0; segmentIndex < leg.getSegments().size(); segmentIndex++) {
                Segment segment = leg.getSegments().get(segmentIndex);
                Vector parent = segmentIndex > 0 ?
                        leg.getSegments().get(segmentIndex - 1).getPosition() :
                        leg.getRoot();

                Vector vector = segment.getPosition().clone().subtract(parent).normalize().multiply(segment.getLength());
                Location location = parent.toLocation(this.getBukkitEntity().getWorld());

                renderer.render(new AbstractMap.SimpleEntry<>(legIndex, segmentIndex), Utilities.lineTemplate(
                        location,
                        vector,
                        segmentUpVector,
                        segmentIndex == 0 ? .2F : (float) ((float) .1 + (.1 * (1/segmentIndex))),
                        1,
                        it -> it.setBlock(Material.AMETHYST_BLOCK.createBlockData()),
                        it -> {}
                ));
            }
        }

        renderer.finishRender();
    }

    public void update(){
        Location loc = this.getBukkitEntity().getLocation();
        int i = 1;
        for (Chain chain : chains) {
            double angle = 30;

            switch (i){
                case 1 -> angle = 30;
                case 2 -> angle = 65;
                case 3 -> angle = 115;
                case 4 -> angle = 150;
                case 5 -> angle = -30;
                case 6 -> angle = -65;
                case 7 -> angle = -115;
                case 8 -> angle = -150;
            }

            Vector vector = loc.getDirection().clone().setY(0).normalize().rotateAroundY(Math.toRadians(angle));

            Location endEffector = loc.clone().add(vector.clone().multiply(limbToHeadOffset));

            if (chain.getEndEffector().toLocation(loc.getWorld()).distance(endEffector) > horizontalTrigger) {
                moveChain(chain, endEffector);
            }

            if (i == chains.size()){
                i = 1;
            }
            i++;
        }
    }

    @Override
    public void tick(){
        super.tick();
        render();

        this.getNavigation().createPath(follow.getX(), follow.getY(), follow.getZ(), 1);

        for (Chain chain : chains){
            chain.root = this.getBukkitEntity().getLocation().add(0, headYOffset, 0).toVector();
            chain.fabrik(chain.getEndEffector());
        }
        if (this.location != this.getBukkitEntity().getLocation()){
            update();
            this.location = this.getBukkitEntity().getLocation();
        }

    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1D, true));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 20F));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.player.Player.class, true));
    }

    private Location checkColumn(Location location){
        Location loc = location.clone();
        if (!loc.add(0, -1, 0).getBlock().isPassable() && loc.getBlock().getType() == Material.AIR){
            return loc;
        }
        for (int i = -4; i < 4; i++){
            if (!loc.add(0, i - 1, 0).getBlock().isPassable() && loc.add(0, i, 0).getBlock().getType() == Material.AIR){
                return loc.add(0, i, 0);
            }
        }
        return loc;


    }


    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity){
        return false;
    }
}
