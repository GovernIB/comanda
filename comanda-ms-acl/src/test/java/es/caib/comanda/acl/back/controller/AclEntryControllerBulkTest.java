package es.caib.comanda.acl.back.controller;

class AclEntryControllerBulkTest {

    /*private MockMvc mockMvc;
    private AclEntryService aclEntryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        aclEntryService = Mockito.mock(AclEntryService.class);
        AclEntryController controller = new AclEntryController();
        try {
            // Inject services and validator similarly to CRUD test
            Field f = BaseReadonlyResourceController.class.getDeclaredField("readonlyResourceService");
            f.setAccessible(true);
            f.set(controller, aclEntryService);

            ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
            ResourcePermissions perms = Mockito.mock(ResourcePermissions.class);
            when(perms.isWriteGranted()).thenReturn(true);
            when(perms.isDeleteGranted()).thenReturn(true);
            when(resourceApiService.permissionsCurrentUser(Mockito.any(), Mockito.any())).thenReturn(perms);
            Field rfs = BaseReadonlyResourceController.class.getDeclaredField("resourceApiService");
            rfs.setAccessible(true);
            rfs.set(controller, resourceApiService);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();
            Field v = BaseMutableResourceController.class.getDeclaredField("validator");
            v.setAccessible(true);
            v.set(controller, validator);

            // Also set the autowired field for bulk methods
            Field s = AclEntryController.class.getDeclaredField("aclEntryService");
            s.setAccessible(true);
            s.set(controller, aclEntryService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private AclEntry sample(String user, long id) {
        AclEntry r = new AclEntry();
        r.setId(id);
        r.setSubjectType(SubjectType.USER);
        r.setSubjectValue(user);
        r.setResourceType(ResourceType.ENTORN_APP);
        r.setResourceId(123L);
        r.setAction(AclAction.READ);
        r.setEffect(AclEffect.ALLOW);
        return r;
    }

    @Test
    void createBulk_returnsList_and_callsService() throws Exception {
        List<AclEntry> payload = Arrays.asList(sample("u1", 0), sample("u2", 0));
        List<AclEntry> created = Arrays.asList(sample("u1", 10), sample("u2", 11));
        when(aclEntryService.createAll(anyList())).thenReturn(created);

        String body = objectMapper.writeValueAsString(payload);
        String response = mockMvc.perform(post("/api/acl/entries/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<List<AclEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(aclEntryService).createAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(response).contains("\"id\":10").contains("\"id\":11");
    }

    @Test
    void updateBulk_returnsList_and_callsService() throws Exception {
        List<AclEntry> payload = Arrays.asList(sample("u1", 10), sample("u2", 11));
        when(aclEntryService.updateAll(anyList())).thenReturn(payload);

        String response = mockMvc.perform(put("/api/acl/entries/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        verify(aclEntryService).updateAll(anyList());
        assertThat(response).contains("\"id\":10").contains("\"id\":11");
    }

    @Test
    void deleteBulk_returnsOk_and_callsService() throws Exception {
        doNothing().when(aclEntryService).deleteAll(anyList());

        mockMvc.perform(delete("/api/acl/entries/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(5L))))
                .andExpect(status().isOk());

        verify(aclEntryService).deleteAll(anyList());
    }*/

}
