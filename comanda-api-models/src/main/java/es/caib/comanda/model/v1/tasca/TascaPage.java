package es.caib.comanda.model.v1.tasca;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TascaPage")
public class TascaPage {
    @Schema(description = "Contingut de la pàgina")
    private List<Tasca> content;

    @Schema(description = "Metadades de paginació")
    private PageMetadata page;

    @Schema(description = "Enllaços HATEOAS")
    private List<TascaPageLink> links;


    @Builder
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "TascaPageMetadata")
    public static class PageMetadata {
        @Schema(description = "Número de pàgina", example = "0")
        private long number;      // número de pàgina
        @Schema(description = "Mida de pàgina", example = "20")
        private long size;        // mida de pàgina
        @Schema(description = "Total d'elements", example = "1")
        private long totalElements;
        @Schema(description = "Total de pàgines", example = "1")
        private long totalPages;
    }

    @Builder
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "TascaPageLink")
    public static class TascaPageLink {
        @Schema(description = "Relació de l'enllaç", example = "self")
        private String rel;
        @Schema(description = "URL de l'enllaç", example = "https://dev.caib.es/comanda/api/v1/tasca?page=0&size=20")
        private String href;
    }
}
