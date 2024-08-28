package giorno.spider.stuff;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

@Getter
public class RendererTemplate<T extends Entity> {

    public final Class<T> clazz;
    public final Location location;
    public final Consumer<T> init;
    public final Consumer<T> update;

    public RendererTemplate(Class<T> clazz, Location location, Consumer<T> init, Consumer<T> update) {
        this.clazz = clazz;
        this.location = location;
        this.init = init;
        this.update = update;
    }
}
