package es.caib.comanda.acl.logic.service;

import es.caib.comanda.acl.persist.repository.AclEntryMapRepository;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AclEntryServiceImplMultiCheckTest {

    private static class FakeService extends AclEntryServiceImpl {
        private final AclAction trueAction;
        private final AclAction falseAction;
        private final AtomicInteger calls = new AtomicInteger(0);

        FakeService(AclAction trueAction, AclAction falseAction) {
            super(null, null);
            this.trueAction = trueAction;
            this.falseAction = falseAction;
        }

        @Override
        public boolean checkPermission(String user, java.util.List<String> roles, ResourceType resourceType, Long resourceId, AclAction action) {
            calls.incrementAndGet();
            if (action == trueAction) return true;
            if (action == falseAction) return false;
            return false;
        }
    }

    private AclEntryServiceImpl service;

    @BeforeEach
    void setUp() {
        // default simple service without side effects
        service = new AclEntryServiceImpl(null, null);
    }

    @Test
    void checkPermissionsAny_shortCircuitsOnFirstTrue() {
        FakeService s = new FakeService(AclAction.READ, AclAction.WRITE);

        boolean allowed = s.checkPermissionsAny(
                "u1",
                Arrays.asList("R1"),
                ResourceType.ENTORN_APP,
                5L,
                Arrays.asList(AclAction.READ, AclAction.WRITE));

        assertThat(allowed).isTrue();
        assertThat(s.calls.get()).isEqualTo(1); // short-circuit after first true
    }

    @Test
    void checkPermissionsAll_shortCircuitsOnFirstFalse() {
        FakeService s = new FakeService(AclAction.WRITE, AclAction.READ); // READ will be false

        boolean allowed = s.checkPermissionsAll(
                null,
                Collections.singletonList("ROLE_A"),
                ResourceType.DASHBOARD,
                9L,
                Arrays.asList(AclAction.READ, AclAction.WRITE));

        assertThat(allowed).isFalse();
        assertThat(s.calls.get()).isEqualTo(1); // short-circuit after first false
    }

    @Test
    void multiChecks_returnFalseOnNullOrEmpty() {
        assertThat(service.checkPermissionsAny("u", Collections.emptyList(), ResourceType.ENTORN_APP, 1L, null)).isFalse();
        assertThat(service.checkPermissionsAny("u", Collections.emptyList(), ResourceType.ENTORN_APP, 1L, Collections.emptyList())).isFalse();
        assertThat(service.checkPermissionsAll("u", Collections.emptyList(), ResourceType.ENTORN_APP, 1L, null)).isFalse();
        assertThat(service.checkPermissionsAll("u", Collections.emptyList(), ResourceType.ENTORN_APP, 1L, Collections.emptyList())).isFalse();
    }
}
