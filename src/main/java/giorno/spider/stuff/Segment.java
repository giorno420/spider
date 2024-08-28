package giorno.spider.stuff;

import lombok.Getter;
import org.bukkit.util.Vector;

public class Segment {

    @Getter public double length;
    @Getter public Vector position;

    public Segment(double length, Vector position) {
        this.length = length;
        this.position = position;
    }
}
