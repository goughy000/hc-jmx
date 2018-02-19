package com.testingsyndicate.hc.jmx;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

public class HcJmxTest {

    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

    private PoolingHttpClientConnectionManager mockConnectionManager;
    private PoolStats mockPoolStats;
    private HcJmx sut;

    @Before
    public void setUp() throws MalformedObjectNameException {

        mockPoolStats = mock(PoolStats.class);
        mockConnectionManager = mock(PoolingHttpClientConnectionManager.class);

        given(mockConnectionManager.getTotalStats()).willReturn(mockPoolStats);

        sut = new HcJmx(mockConnectionManager);
    }

    @After
    public void after() throws JMException {
        ObjectName query = new ObjectName("org.apache.httpcomponents.httpclient:name=*,type=PoolingHttpClientConnectionManager");
        Set<ObjectName> names = SERVER.queryNames(query, null);

        for (ObjectName name : names) {
            SERVER.unregisterMBean(name);
        }
    }

    @Test
    public void returnsMaxTotal() {
        // given
        given(mockConnectionManager.getMaxTotal()).willReturn(99);

        // when
        int actual = sut.getMaxTotal();

        // then
        then(mockConnectionManager).should().getMaxTotal();
        assertThat(actual).isEqualTo(99);
    }

    @Test
    public void returnsDefaultMaxPerRoute() {
        // given
        given(mockConnectionManager.getDefaultMaxPerRoute()).willReturn(100);

        // when
        int actual = sut.getDefaultMaxPerRoute();

        // then
        then(mockConnectionManager).should().getDefaultMaxPerRoute();
        assertThat(actual).isEqualTo(100);
    }

    @Test
    public void returnsLeased() {
        // given
        given(mockPoolStats.getLeased()).willReturn(101);

        // when
        int actual = sut.getLeased();

        // then
        then(mockPoolStats).should().getLeased();
        assertThat(actual).isEqualTo(101);
    }

    @Test
    public void returnsPending() {
        // given
        given(mockPoolStats.getPending()).willReturn(102);

        // when
        int actual = sut.getPending();

        // then
        then(mockPoolStats).should().getPending();
        assertThat(actual).isEqualTo(102);
    }

    @Test
    public void returnsAvailable() {
        // given
        given(mockPoolStats.getAvailable()).willReturn(103);

        // when
        int actual = sut.getAvailable();

        // then
        then(mockPoolStats).should().getAvailable();
        assertThat(actual).isEqualTo(103);
    }

    @Test
    public void returnsMax() {
        // given
        given(mockPoolStats.getMax()).willReturn(104);

        // when
        int actual = sut.getMax();

        // then
        then(mockPoolStats).should().getMax();
        assertThat(actual).isEqualTo(104);
    }

    @Test
    public void returnsRoutesTotal() {
        // given
        Set<HttpRoute> mockRoutes = mock(Set.class);
        given(mockRoutes.size()).willReturn(3);
        given(mockConnectionManager.getRoutes()).willReturn(mockRoutes);

        // when
        int actual = sut.getRoutesTotal();

        // then
        then(mockConnectionManager).should().getRoutes();
        then(mockRoutes).should().size();
        assertThat(actual).isEqualTo(3);
    }

    @Test
    public void registersMBeanWithName() throws JMException {
        // given
        ObjectName name = HcJmx.register(mockConnectionManager, "wibble");

        // when
        MBeanInfo actual = SERVER.getMBeanInfo(name);

        // then
        assertThat(name.toString())
                .isEqualTo("org.apache.httpcomponents.httpclient:name=wibble,type=PoolingHttpClientConnectionManager");
        assertThat(actual).isNotNull();
    }

    @Test
    public void providesDefaultNameOnRegistration() throws JMException {
        // given

        // when
        ObjectName actual = HcJmx.register(mockConnectionManager);

        // then
        assertThat(actual.toString())
                .matches("org\\.apache\\.httpcomponents\\.httpclient:name=default-[0-9]+,type=PoolingHttpClientConnectionManager");
    }

    @Test
    public void unregistersMBean() throws JMException {
        // given
        ObjectName name = HcJmx.register(mockConnectionManager);

        // when
        HcJmx.unregister(name);
        boolean actual = SERVER.isRegistered(name);

        // then
        assertThat(actual).isFalse();
    }
}
