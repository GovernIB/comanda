package es.caib.comanda.api.controller;

//@RestController
//@RequestMapping({BaseConfig.API_PATH + "/monitor", BaseConfig.API_PATH + "/interna/v1/monitor"})
//@Tag(name = "COMANDA → APP / Monitoratge", description = "Consulta d'estat de l'APP: salut, ús de disc i memòria. Aquesta API la implementen les APPs i COMANDA en fa la consulta.")
public class MonitorRestController { // extends BaseController {

//    @GetMapping
//    @Operation(
//            summary = "Llista d'opcions de monitoratge disponibles",
//            description = "Índex d'enllaços a les operacions de monitoratge disponibles: salut, disc i memòria.")
//    public ResponseEntity<CollectionModel<?>> index() {
//        List<Link> indexLinks = new ArrayList<>();
//        indexLinks.add(linkTo(methodOn(getClass()).health()).withRel("health"));
//        indexLinks.add(linkTo(methodOn(getClass()).disk(null)).withRel("disk"));
//        indexLinks.add(linkTo(methodOn(getClass()).memory()).withRel("memory"));
//        CollectionModel<?> resources = CollectionModel.of(
//                Collections.emptySet(),
//                indexLinks.toArray(Link[]::new));
//        return ResponseEntity.ok(resources);
//    }
//
//    @GetMapping("/health")
//    @Operation(
//            summary = "Estat de salut bàsic de l'APP",
//            description = "Retorna l'estat global (p. ex. UP/DOWN) i, opcionalment, detalls per components.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Operació correcta",
//                    content = @Content(schema = @Schema(implementation = HealthStatus.class))),
//            @ApiResponse(responseCode = "500", description = "Error intern")
//    })
//    public ResponseEntity<HealthStatus> health() {
//        HealthStatus status = HealthStatus.builder()
//                .status("UP")
//                .build();
//        return ResponseEntity.ok(status);
//    }
//
//    @GetMapping("/disk")
//    @Operation(
//            summary = "Ús de disc",
//            description = "Retorna l'ús de disc per al camí indicat. Si no s'especifica, s'usa el directori arrel del sistema.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Operació correcta",
//                    content = @Content(schema = @Schema(implementation = DiskUsage.class))),
//            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
//            @ApiResponse(responseCode = "500", description = "Error intern")
//    })
//    public ResponseEntity<DiskUsage> disk(
//            @Parameter(description = "Camí a comprovar", example = "/data")
//            @RequestParam(name = "path", required = false) String path) {
//        File f = new File(path == null || path.isBlank() ? "/" : path);
//        long total = f.getTotalSpace();
//        long free = f.getUsableSpace();
//        long used = total > 0 ? (total - free) : 0;
//        double usedPct = total > 0 ? (used * 100.0 / total) : 0.0;
//        DiskUsage usage = DiskUsage.builder()
//                .path(f.getAbsolutePath())
//                .totalBytes(total)
//                .freeBytes(free)
//                .usedBytes(used)
//                .usedPercent(Math.round(usedPct * 100.0) / 100.0)
//                .build();
//        return ResponseEntity.ok(usage);
//    }
//
//    @GetMapping("/memory")
//    @Operation(
//            summary = "Ús de memòria",
//            description = "Retorna informació d'ús de memòria de la JVM i, si és possible, del sistema.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Operació correcta",
//                    content = @Content(schema = @Schema(implementation = MemoryUsage.class))),
//            @ApiResponse(responseCode = "500", description = "Error intern")
//    })
//    public ResponseEntity<MemoryUsage> memory() {
//        Runtime rt = Runtime.getRuntime();
//        long jvmTotal = rt.totalMemory();
//        long jvmFree = rt.freeMemory();
//        long jvmUsed = jvmTotal - jvmFree;
//
//        MemoryUsage.MemoryUsageBuilder b = MemoryUsage.builder()
//                .jvmTotalBytes(jvmTotal)
//                .jvmFreeBytes(jvmFree)
//                .jvmUsedBytes(jvmUsed)
//                .jvmUsedPercent(jvmTotal > 0 ? (jvmUsed * 100.0 / jvmTotal) : 0.0);
//
//        // Informació del sistema (opcional)
//        try {
//            com.sun.management.OperatingSystemMXBean os =
//                    (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
//            b.systemTotalBytes(os.getTotalPhysicalMemorySize());
//            b.systemFreeBytes(os.getFreePhysicalMemorySize());
//        } catch (Throwable ignore) {
//            // Si la classe no està disponible al JDK en execució, simplement ometem camps de sistema.
//        }
//
//        return ResponseEntity.ok(b.build());
//    }
//
//    @Override
//    protected Link getIndexLink() {
//        return linkTo(methodOn(getClass()).index()).withRel("monitor");
//    }
}
