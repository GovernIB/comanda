package es.caib.comanda.usuaris.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarisClientHelperTest {

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock
    private MonitorServiceClient monitorServiceClient;
    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    @InjectMocks
    private UsuarisClientHelper helper;

    private static final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    @Test
    void entornAppFindById_quanExisteix_retornaEntornApp() {
        EntornApp entornApp = EntornApp.builder().id(1L).build();
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(entornApp));

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void entornAppFindById_quanNoExisteix_retornaNull() {
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(null);

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isNull();
    }

    @Test
    void entornAppFindById_quanNotFound_retornaNull() {
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenThrow(FeignException.NotFound.class);

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isNull();
    }

    @Test
    void entornAppFindByEntornAndApp_quanTroba_retornaEntornApp() {
        EntornApp entornApp = EntornApp.builder().id(1L).build();
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(isNull(), contains("entorn.id:1"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(AUTH_HEADER))).thenReturn(pagedModel);

        Optional<EntornApp> result = helper.entornAppFindByEntornAndApp(1L, 2L);

        assertThat(result).isPresent();
    }
}
