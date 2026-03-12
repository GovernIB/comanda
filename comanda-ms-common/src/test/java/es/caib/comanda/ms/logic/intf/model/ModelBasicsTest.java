package es.caib.comanda.ms.logic.intf.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelBasicsTest {

    @Test
    void baseResourceAndReference_workAsExpected() {
        // Verifica el comportament bàsic de BaseResource i ResourceReference.
        BaseResource<Long> base = new BaseResource<>();
        base.setId(4L);
        assertThat(base.getId()).isEqualTo(4L);

        ResourceReference<BaseResource<Long>, Long> ref = ResourceReference.toResourceReference(4L, "desc");
        assertThat(ref.getId()).isEqualTo(4L);
        assertThat(ref.getDescription()).isEqualTo("desc");
        assertThat(ref).isEqualTo(ResourceReference.toResourceReference(4L, "other"));
        assertThat(ref.toString()).contains("id=4");
    }

    @Test
    void downloadableAndSimpleModels_coverGetters() {
        // Exercita getters i valors simples dels models de domini més bàsics.
        DownloadableFile downloadableFile = new DownloadableFile("a.txt", "text/plain", new byte[]{1, 2, 3});
        assertThat(downloadableFile.getContentLength()).isEqualTo(3L);
        downloadableFile.setContent(null);
        assertThat(downloadableFile.getContentLength()).isNull();

        ExportField exportField = new ExportField("name", "Name");
        assertThat(exportField.getName()).isEqualTo("name");
        assertThat(exportField.getLabel()).isEqualTo("Name");

        FieldOption option = new FieldOption("V", "Value");
        assertThat(option.getValue()).isEqualTo("V");
        assertThat(option.getDescription()).isEqualTo("Value");

        FileReference fileReference = new FileReference("n", new byte[]{1}, "ct", 1);
        assertThat(fileReference.getName()).isEqualTo("n");

        ResourceArtifact artifact = new ResourceArtifact(ResourceArtifactType.ACTION, "RUN", true, String.class);
        assertThat(artifact.isFormClassActive()).isTrue();
        artifact.setFormClass(null);
        assertThat(artifact.isFormClassActive()).isFalse();

        OnChangeEvent<Long> event = new OnChangeEvent<>();
        event.setId(9L);
        event.setFieldName("name");
        assertThat(event.getId()).isEqualTo(9L);
        assertThat(event.getFieldName()).isEqualTo("name");

        assertThat(ResourceArtifactType.valueOf("ACTION")).isEqualTo(ResourceArtifactType.ACTION);
        assertThat(ReportFileType.valueOf("PDF")).isEqualTo(ReportFileType.PDF);
        assertThat(FieldArtifactType.valueOf("DOWNLOAD")).isEqualTo(FieldArtifactType.DOWNLOAD);
    }

    @Test
    void unpagedButSorted_behaviour_isStable() {
        // Comprova el comportament estable de la implementació unpaged però ordenada.
        UnpagedButSorted unpaged = new UnpagedButSorted(Sort.by("name"));

        assertThat(unpaged.isPaged()).isFalse();
        assertThat(unpaged.getSort().isSorted()).isTrue();
        assertThat(unpaged.previousOrFirst()).isSameAs(unpaged);
        assertThat(unpaged.next()).isSameAs(unpaged);
        assertThat(unpaged.first()).isSameAs(unpaged);
        assertThat(unpaged.withPage(0)).isSameAs(unpaged);
        assertThat(unpaged.hasPrevious()).isFalse();
        assertThatThrownBy(unpaged::getPageNumber).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(unpaged::getPageSize).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(unpaged::getOffset).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> unpaged.withPage(1)).isInstanceOf(UnsupportedOperationException.class);
    }
}
