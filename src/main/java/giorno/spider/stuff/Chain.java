package giorno.spider.stuff;

import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.List;

public class Chain {

    @Getter public Vector root;
    @Getter public List<Segment> segments;
    @Getter public boolean forward;
    @Getter public long lastMoved;

    public Chain(Vector root, List<Segment> segments) {
        this.root = root;
        this.segments = segments;
        this.forward = false;
        this.lastMoved = 0;
    }

    public void fabrik(Vector target) {
        double tolerance = 0.01;

        for (int i = 0; i < 10; i++) {
            fabrikForward(target);
            fabrikBackward();

            if (getEndEffector().distance(target) < tolerance) {
                break;
            }
        }
    }

    public void straightenDirection(Vector direction) {
        direction.normalize();
        Vector position = root.clone();
        for (Segment segment : segments) {
            position.add(direction.clone().multiply(segment.length));
            segment.position.copy(position);
        }
    }

    public void fabrikForward(Vector newPosition) {
        Segment lastSegment = segments.get(segments.size() - 1);
        lastSegment.position.copy(newPosition);

        for (int i = segments.size() - 1; i >= 1; i--) {
            Segment previousSegment = segments.get(i);
            Segment segment = segments.get(i - 1);

            moveSegment(segment.position, previousSegment.position, previousSegment.length);
        }
    }

    public void fabrikBackward() {
        moveSegment(segments.get(0).position, root, segments.get(0).length);

        for (int i = 1; i < segments.size(); i++) {
            Segment previousSegment = segments.get(i - 1);
            Segment segment = segments.get(i);

            moveSegment(segment.position, previousSegment.position, segment.length);
        }
    }

    public void moveSegment(Vector point, Vector pullTowards, double segment) {
        Vector direction = pullTowards.clone().subtract(point).normalize();
        point.copy(pullTowards).subtract(direction.multiply(segment));
    }

    public Vector getEndEffector(){
        return segments.get(segments.size() - 1).position;
    }
}
