package es.caib.comanda.estadistica.logic.helper;

//@Slf4j
//@Component
//@RequiredArgsConstructor
public class ConsultaEstadisticaAsyncHelper {

//    private final ConsultaEstadisticaHelper consultaEstadisticaHelper;
//    private final DashboardItemRepository dashboardItemRepository;
//    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
//    private final ApplicationEventPublisher eventPublisher;
//    private final EntityManagerFactory entityManagerFactory;
//
//
//    @Async("asyncTaskExecutor")
//    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
//    public void generateAsyncData(DashboardItemEntity dashboardItem) {
//        DashboardItemEntity item = dashboardItemRepository.findById(dashboardItem.getId()).orElseThrow();
//        DashboardEntity dashboard = item.getDashboard();
//        try {
//            Long inici = System.currentTimeMillis();
//
//            InformeWidgetItem loadedItem = consultaEstadisticaHelper.getDadesWidget(item);
//            DashboardLoadedEvent dashboardLoadedEvent = DashboardLoadedEvent.builder()
//                    .dashboardId(dashboard.getId())
//                    .dashboardItemId(item.getId())
//                    .informeWidgetItem(loadedItem)
//                    .tempsCarrega(System.currentTimeMillis() - inici)
//                    .build();
//            eventPublisher.publishEvent(dashboardLoadedEvent);
//            log.info("Dashboard {}: Item {} loaded", dashboard.getId(), item.getId());
//        } catch (Exception e) {
//            log.error("Error generant informe widget. Item {}: {}", item.getId(), e.getMessage(), e);
//            handleWidgetError(dashboard, item, e);
//        }
//    }
//
//    private void handleWidgetError(DashboardEntity dashboard, DashboardItemEntity dashboardItem, Exception e) {
//        EstadisticaWidgetEntity widget = dashboardItem.getWidget();
//        InformeWidgetItem loadingErrorItem = InformeWidgetItem.builder()
//                .dashboardItemId(dashboardItem.getId())
//                .titol(widget.getTitol())
//                .tipus(consultaEstadisticaHelper.determineWidgetType(dashboardItem))
//                .posX(dashboardItem.getPosX())
//                .posY(dashboardItem.getPosY())
//                .width(dashboardItem.getWidth())
//                .height(dashboardItem.getHeight())
//                .error(true)
//                .errorMsg("Error processing item " + dashboardItem.getId() + ": " + e.getMessage())
//                .errorTrace(ExceptionUtils.getStackTrace(e))
//                .build();
//
//        DashboardLoadindErrorEvent dashboardLoadindErrorEvent = DashboardLoadindErrorEvent.builder()
//                .dashboardId(dashboard.getId())
//                .dashboardItemId(dashboardItem.getId())
//                .informeWidgetItem(loadingErrorItem)
//                .build();
//
//        eventPublisher.publishEvent(dashboardLoadindErrorEvent);
//        log.info("Dashboard {}: Loading item {} error", dashboard.getId(), dashboardItem.getId());
//    }

}
