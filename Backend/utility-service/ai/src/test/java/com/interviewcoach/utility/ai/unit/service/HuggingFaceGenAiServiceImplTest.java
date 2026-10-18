package com.interviewcoach.utility.ai.unit.service;

import com.interviewcoach.utility.ai.dto.GenerationRequest;
import com.interviewcoach.utility.ai.service.impl.HuggingFaceGenAiServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class HuggingFaceGenAiServiceImplTest {

    @Value("${ai.model}")
    private String modelName;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String apiBaseUrl;

    @Test
    void smokeCheckShouldReturnTrimmedResponse() {
        HuggingFaceGenAiServiceImpl testService = createServiceWithMock(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"choices\":[{\"message\":{\"content\":\" OK \"}}]}")
                .build()));

        String response = testService.runSmokeCheck();

        assertThat(response).isEqualTo("OK");
    }

    @Test
    void resolveMaxTokensShouldCapRequestedTokens() {
        HuggingFaceGenAiServiceImpl testService = createServiceWithMock(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"choices\":[{\"message\":{\"content\":\"OK\"}}]}")
                .build()));
        ReflectionTestUtils.setField(testService, "maxTokens", 512);

        Integer capped = ReflectionTestUtils.invokeMethod(testService, "resolveMaxTokens", 768);
        Integer uncapped = ReflectionTestUtils.invokeMethod(testService, "resolveMaxTokens", 256);

        assertThat(capped).isEqualTo(512);
        assertThat(uncapped).isEqualTo(256);
    }

    @Test
    void generateShouldHandleSuccessfulResponse() {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"choices\":[{\"message\":{\"content\":\"Hello from AI\"}}]}")
                .build());

        HuggingFaceGenAiServiceImpl testService = createServiceWithMock(exchangeFunction);
        ReflectionTestUtils.setField(testService, "maxTokens", 512);

        GenerationRequest request = new GenerationRequest();
        request.setPrompt("Hello AI");

        String response = testService.generate(request);

        assertThat(response).isEqualTo("Hello from AI");
    }

    @Test
    void transcribeAudioShouldHandleSuccessfulResponse() {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"text\":\"Hello World\"}")
                .build());

        HuggingFaceGenAiServiceImpl testService = createServiceWithMock(exchangeFunction);

        String response = testService.transcribeAudio(new byte[]{0x01, 0x02});

        assertThat(response).isEqualTo("Hello World");
    }

    private HuggingFaceGenAiServiceImpl createServiceWithMock(ExchangeFunction exchangeFunction) {
        HuggingFaceGenAiServiceImpl testService = new HuggingFaceGenAiServiceImpl(WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build());
        ReflectionTestUtils.setField(testService, "apiKey", apiKey);
        ReflectionTestUtils.setField(testService, "modelName", modelName);
        ReflectionTestUtils.setField(testService, "apiBaseUrl", apiBaseUrl);
        return testService;
    }
}