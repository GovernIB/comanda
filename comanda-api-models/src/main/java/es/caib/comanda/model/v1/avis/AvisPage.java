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
    private List<Link> links;


    @Builder
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private long number;      // número de pàgina
        private long size;        // mida de pàgina
        private long totalElements;
        private long totalPages;
    }

    @Builder
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        private String rel;
        private String href;
    }
}
