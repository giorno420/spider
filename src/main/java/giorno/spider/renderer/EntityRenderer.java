package giorno.spider.renderer;

import giorno.spider.Utilities;
import giorno.spider.stuff.RendererTemplate;
import org.bukkit.entity.Entity;

import java.io.Closeable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityRenderer implements Closeable {

    Map<Object, Entity> rendered = new HashMap<>();

    Set<Object> used = new HashSet<>();

    @Override
    public void close() {
        for (Entity entity : rendered.values()) {
            entity.remove();
        }
        rendered.clear();
        used.clear();
    }

    public void beginRender() {
        if (!used.isEmpty()) {
            throw new IllegalStateException("beginRender called without finishRender");
        }
    }

    public void keepAlive(Object id) {
        used.add(id);
    }

    public void finishRender() {
        Set<Object> toRemove = new HashSet<>(rendered.keySet());
        toRemove.removeAll(used);
        for (Object key : toRemove) {
            Entity entity = rendered.get(key);
            if (entity != null) {
                entity.remove();
                rendered.remove(key);
            }
        }
        used.clear();
    }

    public <T extends Entity> void render(Object id, RendererTemplate<T> template) {
        used.add(id);

        Entity oldEntity = rendered.get(id);
        if (oldEntity != null) {
        // check if the entity is of the same type
            if (oldEntity.getType().getEntityClass() == template.getClazz()) {
                oldEntity.teleport(template.getLocation());
                template.update.accept((T) oldEntity);
                return;
            }

            oldEntity.remove();
            rendered.remove(id);
        }

        Entity entity = Utilities.spawnEntity(template.getLocation(), template.getClazz(), it -> {
            template.getInit().accept(it);
            template.getUpdate().accept(it);
        });
        rendered.put(id, entity);
    }

    public void renderList(Object id, List<RendererTemplate<?>> list) {
        for (int i = 0; i < list.size(); i++) {
            RendererTemplate<?> template = list.get(i);
            render(Arrays.asList(id, i), template);
        }
    }

}