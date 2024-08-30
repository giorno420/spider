package giorno.spider;

import giorno.spider.stuff.RendererTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class Utilities {

    public static Vector DOWN_VECTOR = new Vector(0, -1, 0);
    public static Vector UP_VECTOR = new Vector(0, 1, 0);

    public static RendererTemplate<BlockDisplay> blockTemplate(Location loc, Consumer<BlockDisplay> init, Consumer<BlockDisplay> update){
        return new RendererTemplate<>(BlockDisplay.class, loc, init, update);
    }

    public static RendererTemplate<BlockDisplay> lineTemplate(Location location, Vector vector, Vector upVector, float thickness, int interpolation, Consumer<BlockDisplay> init, Consumer<BlockDisplay> update) {
        if (upVector == null) {
            upVector = (vector.getX() + vector.getZ() != 0.0) ? UP_VECTOR : new Vector(0, 0, 1);
        }

        float finalThickness = thickness != 0.0f ? thickness : 0.1f;
        int finalInterpolation = interpolation != 0 ? interpolation : 1;

        Vector finalUpVector = upVector;
        return blockTemplate(
                location,
                block -> {
                    block.setTeleportDuration(finalInterpolation);
                    block.setInterpolationDuration(finalInterpolation);
                    init.accept(block);
                },
                block -> {
                    Matrix4f matrix = new Matrix4f().rotateTowards(vector.toVector3f(), finalUpVector.toVector3f())
                            .translate(-finalThickness / 2, -finalThickness / 2, 0f)
                            .scale(finalThickness, finalThickness, (float) vector.length());

                    applyTransformationWithInterpolation(block, matrix);
                    update.accept(block);
                }
        );
    }

    public static void applyTransformationWithInterpolation(BlockDisplay entity, Transformation transformation) {
        if (!entity.getTransformation().equals(transformation)) {
            entity.setTransformation(transformation);
            entity.setInterpolationDelay(0);
            Bukkit.broadcastMessage("applying interpolation transformation (check if spider moves)");
        }
    }

    public static void applyTransformationWithInterpolation(BlockDisplay entity, Matrix4f matrix) {
        applyTransformationWithInterpolation(entity, transformFromMatrix(matrix));
    }

    public static Transformation transformFromMatrix(Matrix4f matrix) {
        Vector3f translation = matrix.getTranslation(new Vector3f());
        Quaternionf rotation = matrix.getUnnormalizedRotation(new Quaternionf());
        Vector3f scale = matrix.getScale(new Vector3f());

        return new Transformation(translation, rotation, scale, new Quaternionf());
    }

    public static <T extends Entity> T spawnEntity(Location location, Class<T> clazz, Consumer<T> initializer) {
        return location.getWorld().spawn(location, clazz, initializer);
    }
}
