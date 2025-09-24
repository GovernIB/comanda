package es.caib.comanda.estadistica.logic.helper;

/**
 * Proves unitàries centrades en el mètode d'agrupació per dimensions normalitzades.
 * Comentaris en català i estructura AAA.
 */
//@ExtendWith(MockitoExtension.class)
public class AgrupacioDimensionsHelperTest {

//    @Mock DimensioValorRepository dimensioValorRepository; // no s'usa directament aquí
//    @Mock FetRepository fetRepository; // no s'usa directament aquí
//    @Mock IndicadorRepository indicadorRepository; // no s'usa directament aquí
//
//    @InjectMocks CompactacioHelper helper;
//
//    private FetEntity fet(Long entornId, LocalDate data, Map<String,String> dims){
//        FetEntity f = new FetEntity();
//        TempsEntity t = new TempsEntity();
//        t.setData(data);
//        f.setTemps(t);
//        f.setEntornAppId(entornId);
//        f.setDimensionsJson(dims==null?null:new HashMap<>(dims));
//        return f;
//    }
//
//    @BeforeEach
//    void setup() {}
//
//    @Test
//    void agruparFetsAmbDimensionsNormalitzades_canviaIDonaGrups() {
//        // Arrange
//        Long entorn = 7L; LocalDate d = LocalDate.of(2024, 6, 10);
//        var f1 = fet(entorn, d, Map.of("D1","a","D2","x"));
//        var f2 = fet(entorn, d, Map.of("D1","a","D2","x"));
//        var f3 = fet(entorn, d, Map.of("D1","b","D2","x"));
//        // Reemplaç: D1 a->A, b->A  (tot es normalitza a A i, per tant, 3 elements en un sol grup)
//        Map<String, Map<String,String>> reemplaCos = new HashMap<>();
//        reemplaCos.put("D1", Map.of("a","A","b","A"));
//
//        // Act
//        LinkedHashMap<?, ?> grups = helper.agruparFetsAmbDimensionsNormalitzades(entorn, List.of(f1,f2,f3), reemplaCos);
//
//        // Assert
//        assertEquals(1, grups.size(), "Tots tres fets s'haurien d'agrupat en un sol grup");
//        var llista = grups.values().iterator().next();
//        assertEquals(3, ((List<?>)llista).size(), "El grup ha de contenir els 3 fets");
//    }
//
//    @Test
//    void agruparFetsAmbDimensionsNormalitzades_capCanvi_retornBuit() {
//        // Arrange
//        Long entorn = 7L; LocalDate d = LocalDate.of(2024, 6, 10);
//        var f1 = fet(entorn, d, Map.of("D1","A","D2","x"));
//        var f2 = fet(entorn, d, Map.of("D1","A","D2","y"));
//        // Reemplaç que no aplica ni canvia res
//        Map<String, Map<String,String>> reemplaCos = new HashMap<>();
//        reemplaCos.put("D1", Map.of("A","A"));
//
//        // Act
//        var grups = helper.agruparFetsAmbDimensionsNormalitzades(entorn, List.of(f1,f2), reemplaCos);
//
//        // Assert
//        assertTrue(grups.isEmpty(), "Quan no hi ha cap canvi de dimensió, no s'ha d'agrupat cap element");
//    }
//
//    @Test
//    void agruparFetsAmbDimensionsNormalitzades_dimsNulls_ignoraISegur() {
//        // Arrange
//        Long entorn = 7L; LocalDate d = LocalDate.of(2024, 6, 10);
//        var f1 = fet(entorn, d, null);
//        var f2 = fet(entorn, d, Map.of("D1","a"));
//        Map<String, Map<String,String>> reemplaCos = new HashMap<>();
//        reemplaCos.put("D1", Map.of("a","A"));
//
//        // Act
//        var grups = helper.agruparFetsAmbDimensionsNormalitzades(entorn, List.of(f1,f2), reemplaCos);
//
//        // Assert
//        // Només el segon canvia; per tant, hi hauria d'haver 1 grup amb 1 element
//        assertEquals(1, grups.size());
//        var llista = grups.values().iterator().next();
//        assertEquals(1, ((List<?>)llista).size());
//    }
}
