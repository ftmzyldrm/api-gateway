package com.example.apigateway.filter;




import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;


@SpringBootTest

@ExtendWith(MockitoExtension.class)
public class CustomGlobalFilterTest  {


    private ServerWebExchange exchange;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private CustomGlobalFilter customGlobalFilter;


    @Test
    public void shouldAllowThePublicRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test").build();
        exchange = MockServerWebExchange.from(request);
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(captor.capture())).thenReturn(Mono.empty());

        customGlobalFilter.filter(exchange, chain);
        ServerWebExchange webExchange = captor.getValue();

        verify(chain).filter(exchange);
        verifyNoMoreInteractions(chain);
    }
    @RepeatedTest(5)
    public void testRewriteFilter() {
        URI url = UriComponentsBuilder.fromUriString("http://localhost" + "/test").build(true).toUri();
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, url).build();
        exchange = MockServerWebExchange.from(request);

       // ServerWebExchange exchange = MockServerWebExchange.from(request);

        //GatewayFilterChain filterChain = mock(GatewayFilterChain.class);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        when(chain.filter(captor.capture())).thenReturn(Mono.empty());

        customGlobalFilter.filter(exchange, chain);

        ServerWebExchange webExchange = captor.getValue();
        verify(chain).filter(exchange);
        verifyNoMoreInteractions(chain);

        System.out.println(webExchange.getResponse().getStatusCode());

    }



}
