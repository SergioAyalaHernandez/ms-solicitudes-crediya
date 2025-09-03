package co.com.pragma.api.credit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

class CreditRouterRestTest {

  @Mock
  private CreditHandler creditHandler;

  private WebTestClient webTestClient;
  private CreditRouterRest routerRest;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    routerRest = new CreditRouterRest();
    RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction(creditHandler);
    webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();

    // Configurar respuestas mock para el handler
    Mono<ServerResponse> response = Mono.just(Objects.requireNonNull(ServerResponse.ok().build().block()));
    doReturn(response).when(creditHandler).createCredit(any());
    doReturn(response).when(creditHandler).getCreditsList(any());
  }

  @Test
  void shouldRouteCreateCreditRequest() {
    // Ejecutar
    webTestClient.post()
            .uri("/api/v1/solicitud")
            .exchange()
            .expectStatus().isOk();

    // Verificar
    verify(creditHandler).createCredit(any());
  }

  @Test
  void shouldRouteGetCreditsListRequest() {
    // Ejecutar
    webTestClient.get()
            .uri("/api/v1/solicitudes")
            .exchange()
            .expectStatus().isOk();

    // Verificar
    verify(creditHandler).getCreditsList(any());
  }
}