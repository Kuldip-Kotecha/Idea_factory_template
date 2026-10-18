package com.ideafactory.gateway.unit;

import com.ideafactory.gateway.filter.RequestLoggingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
    }

    @Test
    @DisplayName("Unit: filter logs request timing on success")
    void filter_logsTimingOnSuccess() {
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        ServerWebExchange exchange = createMockExchange("/api/test", HttpMethod.GET);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("Unit: filter returns chain result")
    void filter_returnsChainResult() {
        Mono<Void> expectedMono = Mono.empty();
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(expectedMono);

        ServerWebExchange exchange = createMockExchange("/api/test", HttpMethod.GET);

        Mono<Void> result = filter.filter(exchange, chain);

        assert result.block() == null;
    }

    @Test
    @DisplayName("Unit: getOrder returns -200")
    void getOrder_returnsMinus200() {
        assert filter.getOrder() == -200;
    }

    private ServerWebExchange createMockExchange(String path, HttpMethod method) {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(exchange.getAttributes()).thenReturn(new java.util.HashMap<>());
        when(request.getMethod()).thenReturn(method);
        when(request.getURI()).thenReturn(URI.create(path));

        return exchange;
    }
}