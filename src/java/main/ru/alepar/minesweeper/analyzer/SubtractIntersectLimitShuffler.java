package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.model.Region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubtractIntersectLimitShuffler implements LimitShuffler {

    public Set<Limit> shuffleLimitsPair(Limit first, Limit second) {
        if (first.region.contains(second.region)) {
            return subtractRegion(first, second);
        }

        if (second.region.contains(first.region)) {
            return subtractRegion(second, first);
        }

        Region intersect = first.region.intersect(second.region);
        if (!intersect.points().isEmpty()) {
            Set<Limit> result = new HashSet<Limit>();
            result.add(
                    new Limit(intersect,
                            Math.max(
                                    startLimitForIntersection(first, intersect),
                                    startLimitForIntersection(second, intersect)
                            ),
                            Math.min(
                                    endLimitForIntersection(first, intersect),
                                    endLimitForIntersection(second, intersect)
                            )
                    )
            );
            return result;
        }

        return Collections.emptySet();
    }

    private static Set<Limit> subtractRegion(Limit outer, Limit inner) {
        Region subtractRegion = outer.region.subtract(inner.region);
        if (subtractRegion.points().isEmpty()) {
            return Collections.emptySet();
        }

        Limit substraction = new Limit(
                subtractRegion,
                Math.max(0, outer.min - inner.max),
                Math.max(0, outer.max - inner.min)
        );

        return Collections.singleton(substraction);
    }

    private static int startLimitForIntersection(Limit src, Region dst) {
        return Math.max(0, src.min - src.region.points().size() + dst.points().size());
    }

    private static int endLimitForIntersection(Limit src, Region dst) {
        return Math.min(src.max, dst.points().size());
    }

}
