package co.com.pragma.consumer;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import java.io.IOException;


class RestConsumerTest {

  private static RestConsumer restConsumer;
  private static MockWebServer mockBackEnd;

  @BeforeAll
  static void setUp() throws IOException {
    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
    var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
    restConsumer = new RestConsumer(webClient);
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockBackEnd.shutdown();
  }

  @Test
  @DisplayName("Debe devolver el ID cuando el usuario existe")
  void findByDocument_WhenUserExists_ReturnsId() {
    // Arrange
    Long document = 123456789L;
    mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody("{\"id\": 42, \"state\": \"ok\"}"));

    // Act
    var response = restConsumer.findByDocument(document);

    // Assert
    StepVerifier.create(response)
            .expectNext(42L)
            .verifyComplete();
  }

  @Test
  @DisplayName("Debe devolver 0L cuando el usuario no existe (404)")
  void findByDocument_WhenUserDoesNotExist_ReturnsZero() {
    // Arrange
    Long document = 123456789L;
    mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.NOT_FOUND.value())
            .setBody("User not found"));

    // Act
    var response = restConsumer.findByDocument(document);

    // Assert
    StepVerifier.create(response)
            .expectNext(0L)
            .verifyComplete();
  }

  @Test
  @DisplayName("Debe devolver Mono vacío cuando hay un error genérico")
  void findByDocument_WhenGenericError_ReturnsEmptyMono() {
    // Arrange
    Long document = 123456789L;
    mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .setBody("Internal server error"));

    // Act
    var response = restConsumer.findByDocument(document);

    // Assert
    StepVerifier.create(response)
            .verifyComplete();
  }

  @Test
  @DisplayName("Debe manejar respuesta null correctamente")
  void findByDocument_WhenResponseIsNull_ReturnsEmptyMono() {
    // Arrange
    Long document = 123456789L;
    mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody("null"));

    // Act
    var response = restConsumer.findByDocument(document);

    // Assert
    StepVerifier.create(response)
            .verifyComplete();
  }
}