package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.logic.intf.service.MonitorService;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MonitorControllerTest {

    @Mock
    private MonitorService monitorService;
    @Mock
    private ResourceApiService resourceApiService;
    @Mock
    private SmartValidator validator;

    @InjectMocks
    private MonitorController monitorController;

    private MockMvc mockMvc;
    private Monitor monitor;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(monitorController)
                .setCustomArgumentResolvers(new PageableHandlerMethodResolver())
                .build();

        // Setup Monitor
        monitor = new Monitor();
        monitor.setId(1L);
        monitor.setEntornAppId(1L);
        monitor.setModul(ModulEnum.SALUT);
        monitor.setTipus(AccioTipusEnum.ENTRADA);
        monitor.setData(LocalDateTime.now());
        monitor.setUrl("http://test.com/api");
        monitor.setOperacio("Test Operation");
        monitor.setTempsResposta(100L);
        monitor.setEstat(EstatEnum.OK);
        monitor.setCodiUsuari("testuser");
    }

    private static class PageableHandlerMethodResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return Pageable.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            int page = webRequest.getParameter("page") != null ? Integer.parseInt(webRequest.getParameter("page")) : 0;
            int size = webRequest.getParameter("size") != null ? Integer.parseInt(webRequest.getParameter("size")) : 10;
            String[] sortParams = webRequest.getParameterValues("sort");
            Sort sort = Sort.unsorted();
            if (sortParams != null) {
                for (String sortParam : sortParams) {
                    String[] parts = sortParam.split(",");
                    if (parts.length == 2) {
                        sort = sort.and(Sort.by(Sort.Direction.fromString(parts[1].toUpperCase()), parts[0]));
                    }
                }
            }
            return PageRequest.of(page, size, sort);
        }
    }

    @Test
    void testFind() throws Exception {
        // Arrange
        Page<Monitor> monitorPage = new PageImpl<>(Arrays.asList(monitor));
        when(monitorService.findPage(any(), any(), any(), any(), any())).thenReturn(monitorPage);
        ResourcePermissions resourcePermissions = ResourcePermissions.builder().readGranted(true).writeGranted(true).createGranted(true).deleteGranted(true).build();
        when(resourceApiService.permissionsCurrentUser(any(), any())).thenReturn(resourcePermissions);

        // Act & Assert
        mockMvc.perform(get(BaseConfig.API_PATH + "/monitors")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].entornAppId").value(1))
                .andExpect(jsonPath("$.content[0].modul").value(ModulEnum.SALUT.toString()))
                .andExpect(jsonPath("$.content[0].tipus").value(AccioTipusEnum.ENTRADA.toString()))
                .andExpect(jsonPath("$.content[0].operacio").value("Test Operation"))
                .andExpect(jsonPath("$.content[0].estat").value(EstatEnum.OK.toString()))
                .andExpect(jsonPath("$.content[0].codiUsuari").value("testuser"));
    }

    @Test
    void testGetOne() throws Exception {
        // Arrange
        when(monitorService.getOne(eq(1L), any())).thenReturn(monitor);
        ResourcePermissions resourcePermissions = ResourcePermissions.builder().readGranted(true).writeGranted(true).createGranted(true).deleteGranted(true).build();
        when(resourceApiService.permissionsCurrentUser(any(), any())).thenReturn(resourcePermissions);

        // Act & Assert
        mockMvc.perform(get(BaseConfig.API_PATH + "/monitors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.entornAppId").value(1))
                .andExpect(jsonPath("$.modul").value(ModulEnum.SALUT.toString()))
                .andExpect(jsonPath("$.tipus").value(AccioTipusEnum.ENTRADA.toString()))
                .andExpect(jsonPath("$.operacio").value("Test Operation"))
                .andExpect(jsonPath("$.estat").value(EstatEnum.OK.toString()))
                .andExpect(jsonPath("$.codiUsuari").value("testuser"));
    }

    @Test
    void testCreate() throws Exception {
        // Arrange
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        when(monitorService.create(any(Monitor.class), eq(answers))).thenReturn(monitor);
        ResourcePermissions resourcePermissions = ResourcePermissions.builder().readGranted(true).writeGranted(true).createGranted(true).deleteGranted(true).build();
        when(resourceApiService.permissionsCurrentUser(any(), any())).thenReturn(resourcePermissions);

        // Convert Monitor to JSON
        String monitorJson = "{"
                + "\"entornAppId\": 1,"
                + "\"modul\": \"SALUT\","
                + "\"tipus\": \"ENTRADA\","
                + "\"data\": \"" + monitor.getData() + "\","
                + "\"url\": \"http://test.com/api\","
                + "\"operacio\": \"Test Operation\","
                + "\"tempsResposta\": 100,"
                + "\"estat\": \"OK\","
                + "\"codiUsuari\": \"testuser\""
                + "}";

        // Act & Assert
        mockMvc.perform(post(BaseConfig.API_PATH + "/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(monitorJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.entornAppId").value(1))
                .andExpect(jsonPath("$.modul").value(ModulEnum.SALUT.toString()))
                .andExpect(jsonPath("$.tipus").value(AccioTipusEnum.ENTRADA.toString()))
                .andExpect(jsonPath("$.operacio").value("Test Operation"))
                .andExpect(jsonPath("$.estat").value(EstatEnum.OK.toString()))
                .andExpect(jsonPath("$.codiUsuari").value("testuser"));

        // Verify that monitorService.create was called
        verify(monitorService).create(any(Monitor.class), eq(answers));
    }

    @Test
    void testUpdate() throws Exception {
        // Arrange
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        when(monitorService.update(eq(1L), any(Monitor.class), eq(answers))).thenReturn(monitor);
        ResourcePermissions resourcePermissions = ResourcePermissions.builder().readGranted(true).writeGranted(true).createGranted(true).deleteGranted(true).build();
        when(resourceApiService.permissionsCurrentUser(any(), any())).thenReturn(resourcePermissions);

        // Convert Monitor to JSON
        String monitorJson = "{"
                + "\"id\": 1,"
                + "\"entornAppId\": 1,"
                + "\"modul\": \"SALUT\","
                + "\"tipus\": \"ENTRADA\","
                + "\"data\": \"" + monitor.getData() + "\","
                + "\"url\": \"http://test.com/api\","
                + "\"operacio\": \"Test Operation\","
                + "\"tempsResposta\": 100,"
                + "\"estat\": \"OK\","
                + "\"codiUsuari\": \"testuser\""
                + "}";

        // Act & Assert
        mockMvc.perform(put(BaseConfig.API_PATH + "/monitors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(monitorJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.entornAppId").value(1))
                .andExpect(jsonPath("$.modul").value(ModulEnum.SALUT.toString()))
                .andExpect(jsonPath("$.tipus").value(AccioTipusEnum.ENTRADA.toString()))
                .andExpect(jsonPath("$.operacio").value("Test Operation"))
                .andExpect(jsonPath("$.estat").value(EstatEnum.OK.toString()))
                .andExpect(jsonPath("$.codiUsuari").value("testuser"));

        // Verify that monitorService.update was called
        verify(monitorService).update(eq(1L), any(Monitor.class), eq(answers));
    }

    @Test
    void testDelete() throws Exception {
        // Arrange
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        // Act & Assert
        mockMvc.perform(delete(BaseConfig.API_PATH + "/monitors/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify that monitorService.delete was called
        verify(monitorService).delete(eq(1L), eq(answers));
    }
}
