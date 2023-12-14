package ch.geowerkstatt.interlis.testbed.runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

public abstract class MockitoTestBase {
    private MockitoSession mockito;

    @BeforeEach
    public final void startMockitoSession() {
        mockito = Mockito.mockitoSession()
                .initMocks(this)
                .strictness(Strictness.STRICT_STUBS)
                .startMocking();
    }

    @AfterEach
    public final void finishMockitoSession() {
        mockito.finishMocking();
    }
}
