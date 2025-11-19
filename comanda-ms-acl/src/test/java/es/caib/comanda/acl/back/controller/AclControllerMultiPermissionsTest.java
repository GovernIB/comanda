package es.caib.comanda.acl.back.controller;

class AclControllerMultiPermissionsTest {

    /*private MockMvc mockMvc;
    private AclEntryService aclEntryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        aclEntryService = Mockito.mock(AclEntryService.class);
        AclController controller = new AclController(aclEntryService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void check_multi_any_returnsTrue_ifAnyGranted() throws Exception {
        when(aclEntryService.checkPermissionsAny(anyString(), anyList(), any(), anyLong(), anyList())).thenReturn(true);

        AclCheckRequest req = new AclCheckRequest();
        req.setUser("user1");
        req.setRoles(Arrays.asList("ROLE_A"));
        req.setResourceType(ResourceType.ENTORN_APP);
        req.setResourceId(11L);
        req.setActions(Arrays.asList(AclAction.READ, AclAction.WRITE));
        req.setMode(AclCheckRequest.Mode.ANY);

        String response = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AclCheckResponse body = objectMapper.readValue(response, AclCheckResponse.class);
        assertThat(body.isAllowed()).isTrue();
    }

    @Test
    void check_multi_all_returnsTrue_onlyIfAllGranted() throws Exception {
        when(aclEntryService.checkPermissionsAll(anyString(), anyList(), any(), anyLong(), anyList())).thenReturn(true);

        AclCheckRequest req = new AclCheckRequest();
        req.setUser("user1");
        req.setRoles(Arrays.asList("ROLE_A"));
        req.setResourceType(ResourceType.DASHBOARD);
        req.setResourceId(77L);
        req.setActions(Arrays.asList(AclAction.READ, AclAction.WRITE));
        req.setMode(AclCheckRequest.Mode.ALL);

        String response = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AclCheckResponse body = objectMapper.readValue(response, AclCheckResponse.class);
        assertThat(body.isAllowed()).isTrue();
    }

    @Test
    void check_multi_emptyActions_returnsFalse() throws Exception {
        AclCheckRequest req = new AclCheckRequest();
        req.setRoles(Collections.singletonList("ROLE_X"));
        req.setResourceType(ResourceType.ENTORN_APP);
        req.setResourceId(12L);
        req.setActions(Collections.emptyList());
        // mode omitted → default ANY, but actions empty → false

        String response = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AclCheckResponse body = objectMapper.readValue(response, AclCheckResponse.class);
        assertThat(body.isAllowed()).isFalse();
    }*/

}
