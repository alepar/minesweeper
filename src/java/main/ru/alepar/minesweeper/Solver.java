package ru.alepar.minesweeper;

import java.util.HashSet;
import java.util.Set;

public class Solver {

    private final FieldApi fieldApi;
    private final PointFactory pointFactory;

    public Solver(FieldApi fieldApi) {
        this.fieldApi = fieldApi;
        pointFactory = new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height());
    }

    public FieldState solve() {
        try {
            Set<Limit> limits = createLimits();
            limits = shuffleLimits(limits);
            openDeterminedLimits(limits);
            return fieldApi.getCurrentField();
        } catch (SteppedOnABomb e) {
            throw new RuntimeException("solver has blown up", e);
        }
    }

    private Set<Limit> shuffleLimits(Set<Limit> limits) {
        return limits;
    }

    private void openDeterminedLimits(Set<Limit> limits) throws SteppedOnABomb {
        for (Limit limit : limits) {
            if(limit.min == 0 && limit.max == 0) {
                for (Point p : limit.points) {
                    fieldApi.open(p);
                }
            } else if(limit.min == limit.max) {
                for (Point p : limit.points) {
                    fieldApi.markBomb(p);
                }
            }
        }
    }

    private Set<Limit> createLimits() {
        Set<Limit> result = new HashSet<Limit>();
        for(Point p: pointFactory.allPoints()) {
            Cell cell = fieldApi.getCurrentField().cellAt(p);
            if(cell.isOpened()) {
                result.add(new Limit(pointFactory.adjacentTo(p), cell.value, cell.value));
            }
        }
        return result;
    }

}
