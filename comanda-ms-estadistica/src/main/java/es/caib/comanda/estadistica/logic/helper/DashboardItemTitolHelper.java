package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Lògica per a {@link DashboardItem} i {@link DashboardTitol}.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardItemTitolHelper {

    public static final int GRID_COLUMNS = 24;

    private final DashboardItemRepository dashboardItemRepository;
    private final DashboardTitolRepository dashboardTitolRepository;

    @lombok.Value
    public static class GridPosition {
        int posX;
        int posY;
    }

    @lombok.Value
    private static class Rectangle {
        int x;
        int y;
        int w;
        int h;

        boolean intersects(int otherX, int otherY, int otherW, int otherH) {
            return x < otherX + otherW
                && x + w > otherX
                && y < otherY + otherH
                && y + h > otherY;
        }
    }

    /**
     * Cerca el primer espai lliure 2D disponible a la graella de 24 columnes,
     * escanejant de dalt a baix (y = 0..maxBottom) i d'esquerra a dreta (x = 0..24-width).
     */
    public GridPosition findFirstAvailableSpace(Long dashboardId, int width, int height) {
        int w = Math.max(1, Math.min(width, GRID_COLUMNS));
        int h = Math.max(1, height);

        if (dashboardId == null) {
            return new GridPosition(0, 0);
        }

        List<Rectangle> occupied = getOccupiedRectangles(dashboardId);
        if (occupied.isEmpty()) {
            return new GridPosition(0, 0);
        }

        int maxBottom = occupied.stream()
            .mapToInt(r -> r.getY() + r.getH())
            .max()
            .orElse(0);

        for (int y = 0; y <= maxBottom; y++) {
            for (int x = 0; x <= GRID_COLUMNS - w; x++) {
                final int curX = x;
                final int curY = y;
                boolean collides = occupied.stream().anyMatch(r -> r.intersects(curX, curY, w, h));
                if (!collides) {
                    return new GridPosition(curX, curY);
                }
            }
        }

        return new GridPosition(0, maxBottom);
    }

    private List<Rectangle> getOccupiedRectangles(Long dashboardId) {
        if (dashboardId == null) {
            return java.util.Collections.emptyList();
        }
        List<Rectangle> list = new java.util.ArrayList<>();
        List<es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity> items = dashboardItemRepository.findByDashboardId(dashboardId);
        if (items != null) {
            for (es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item : items) {
                list.add(new Rectangle(item.getPosX(), item.getPosY(), Math.max(1, item.getWidth()), Math.max(1, item.getHeight())));
            }
        }
        List<es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity> titols = dashboardTitolRepository.findByDashboardId(dashboardId);
        if (titols != null) {
            for (es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity titol : titols) {
                list.add(new Rectangle(titol.getPosX(), titol.getPosY(), Math.max(1, titol.getWidth()), Math.max(1, titol.getHeight())));
            }
        }
        return list;
    }

    /** Assigna al {@link DashboardItem} el valor {@link DashboardItem#posX PosX} i {@link DashboardItem#posY PosY} si són nulls **/
    public void completeResourceItemLogic(DashboardItem resource) {
        if (Objects.nonNull(resource.getDashboard()) && Objects.nonNull(resource.getDashboard().getId())) {
            Long dashboardId = resource.getDashboard().getId();
            if (resource.getPosY() == null) {
                int w = resource.getWidth() > 0 ? resource.getWidth() : 3;
                int h = resource.getHeight() > 0 ? resource.getHeight() : 3;
                GridPosition pos = findFirstAvailableSpace(dashboardId, w, h);
                resource.setPosX(pos.getPosX());
                resource.setPosY(pos.getPosY());
            }
        }
    }

    /** Assigna al {@link DashboardTitol} el valor {@link DashboardTitol#posX PosX} i {@link DashboardTitol#posY PosY} si són nulls **/
    public void completeResourceTitolLogic(DashboardTitol resource) {
        if (Objects.nonNull(resource.getDashboard()) && Objects.nonNull(resource.getDashboard().getId())) {
            Long dashboardId = resource.getDashboard().getId();
            if (resource.getPosY() == null) {
                int w = resource.getWidth() > 0 ? resource.getWidth() : GRID_COLUMNS;
                int h = resource.getHeight() > 0 ? resource.getHeight() : 1;
                GridPosition pos = findFirstAvailableSpace(dashboardId, w, h);
                resource.setPosX(pos.getPosX());
                resource.setPosY(pos.getPosY());
            }
        }
    }

    private Integer getPosYValue(Long dashboardId, Integer posY) {
        if (Objects.nonNull(posY) || Objects.isNull(dashboardId)){
            return posY;
        }
        Integer maxItems = dashboardItemRepository.findMaxBottomPositionByDashboardId(dashboardId);
        Integer maxTitols = dashboardTitolRepository.findMaxBottomPositionByDashboardId(dashboardId);
        Integer max = Stream.of(maxItems, maxTitols)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
        return max;
    }

}
