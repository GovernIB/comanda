package es.caib.comanda.model.v1.avis;

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
@Schema(name = "AvisPage")
public class AvisPage {
    @Schema(description = "Contingut de la pàgina")
    private List<Avis> content;

    @Schema(description = "Metadades de paginació")
    private PageMetadata page;

    @Schema(description = "Enllaços HATEOAS")
    private List<AvisPageLink> links;


    @Builder
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AvisPageMetadata")
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
    @Schema(name = "AvisPageLink")
    public static class AvisPageLink {
        @Schema(description = "Relació de l'enllaç", example = "self")
        private String rel;
        @Schema(description = "URL de l'enllaç", example = "https://dev.caib.es/comanda/api/v1/avis?page=0&size=20")
        private String href;
    }
}
