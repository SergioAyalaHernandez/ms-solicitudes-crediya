package co.com.pragma.sqs.sender.config;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;

import java.net.URI;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQSSenderConfigTest {

  @Test
  void testGetProviderChain() {
    // Arrange
    SQSSenderConfig config = new SQSSenderConfig();

    // Act
    AwsCredentialsProviderChain providerChain = config.getProviderChain();

    // Assert
    assertNotNull(providerChain);
  }

  @Test
  void testResolveEndpointWithValidEndpoint() {
    // Arrange
    SQSSenderProperties properties = mock(SQSSenderProperties.class);
    SQSSenderConfig config = new SQSSenderConfig();

    when(properties.endpoint()).thenReturn("http://localhost:4566");

    // Act
    URI endpoint = config.resolveEndpoint(properties);

    // Assert
    assertNotNull(endpoint);
    assertEquals("http://localhost:4566", endpoint.toString());
  }

  @Test
  void testResolveEndpointWithNullEndpoint() {
    // Arrange
    SQSSenderProperties properties = mock(SQSSenderProperties.class);
    SQSSenderConfig config = new SQSSenderConfig();

    when(properties.endpoint()).thenReturn(null);

    // Act
    URI endpoint = config.resolveEndpoint(properties);

    // Assert
    assertNull(endpoint);
  }

  @Test
  void configSqs_ShouldReturnMockedClient() {
    SQSSenderProperties props = mock(SQSSenderProperties.class);
    when(props.region()).thenReturn("us-east-1");
    when(props.endpoint()).thenReturn("http://localhost:4566");

    MetricPublisher publisher = mock(MetricPublisher.class);

    // Mock cliente y builder
    SqsAsyncClient mockClient = mock(SqsAsyncClient.class);
    SqsAsyncClientBuilder mockBuilder = mock(SqsAsyncClientBuilder.class);

    when(mockBuilder.endpointOverride(any())).thenReturn(mockBuilder);
    when(mockBuilder.region(any())).thenReturn(mockBuilder);
    when(mockBuilder.overrideConfiguration(Mockito.<Consumer<ClientOverrideConfiguration.Builder>>any()))
            .thenReturn(mockBuilder);

    when(mockBuilder.credentialsProvider(Mockito.any(AwsCredentialsProvider.class)))
            .thenReturn(mockBuilder);
    when(mockBuilder.build()).thenReturn(mockClient);

    try (MockedStatic<SqsAsyncClient> mocked = Mockito.mockStatic(SqsAsyncClient.class)) {
      mocked.when(SqsAsyncClient::builder).thenReturn(mockBuilder);

      SQSSenderConfig config = new SQSSenderConfig() {
        @Override
        protected URI resolveEndpoint(SQSSenderProperties p) {
          return URI.create(p.endpoint());
        }

      };

      SqsAsyncClient client = config.configSqs(props, publisher);

      assertNotNull(client);
      assertSame(mockClient, client);
    }
  }


}