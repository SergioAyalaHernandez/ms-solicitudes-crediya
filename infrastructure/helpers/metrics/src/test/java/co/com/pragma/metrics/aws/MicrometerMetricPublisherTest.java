package co.com.pragma.metrics.aws;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricRecord;
import software.amazon.awssdk.metrics.SdkMetric;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MicrometerMetricPublisherTest {

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private MetricCollection metricCollection;

  @Mock
  private Counter counter;

  @Mock
  private Timer timer;

  private MicrometerMetricPublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = new MicrometerMetricPublisher(meterRegistry);
  }


  @Test
  void testPublish_WithEmptyMetricCollection_ShouldNotCallRegistry() throws InterruptedException {
    // Arrange
    when(metricCollection.stream()).thenReturn(Stream.empty());

    // Act
    publisher.publish(metricCollection);

    // Assert
    Thread.sleep(100); // Wait for async execution
    verify(meterRegistry, never()).counter(anyString(), any(List.class));
    verify(meterRegistry, never()).timer(anyString(), any(List.class));
  }

  @Test
  void testBuildTags_WithEmptyCollection_ShouldReturnEmptyList() {
    // Arrange
    when(metricCollection.stream()).thenReturn(Stream.empty());

    // Act
    List<Tag> tags = publisher.buildTags(metricCollection);

    // Assert
    assertThat(tags).isEmpty();
  }

  @Test
  void testClose_ShouldNotThrowException() {
    // Arrange - No setup needed

    // Act & Assert
    publisher.close();
  }

  private MetricRecord createMetricRecord(String metricName, Object value) {
    MetricRecord record = mock(MetricRecord.class);
    SdkMetric metric = mock(SdkMetric.class);

    when(metric.name()).thenReturn(metricName);
    when(record.metric()).thenReturn(metric);
    when(record.value()).thenReturn(value);

    return record;
  }

  @Test
  void testBuildTags_WithSingleStringMetric_ShouldReturnSingleTag() {
    // Arrange
    MetricRecord stringRecord = createMetricRecord("single", "tag");
    when(metricCollection.stream()).thenReturn(Stream.of(stringRecord));

    // Act
    List<Tag> tags = publisher.buildTags(metricCollection);

    // Assert
    assertThat(tags).hasSize(1);
    assertThat(tags.get(0).getKey()).isEqualTo("single");
    assertThat(tags.get(0).getValue()).isEqualTo("tag");
  }

  @Test
  void testBuildTags_WithOnlyBooleanMetrics_ShouldCreateOnlyBooleanTags() {
    // Arrange
    MetricRecord booleanRecord1 = createMetricRecord("success", true);
    MetricRecord booleanRecord2 = createMetricRecord("enabled", false);

    when(metricCollection.stream()).thenReturn(Stream.of(booleanRecord1, booleanRecord2));

    // Act
    List<Tag> tags = publisher.buildTags(metricCollection);

    // Assert
    assertThat(tags).hasSize(2);
    assertThat(tags).extracting(Tag::getKey).containsExactlyInAnyOrder("success", "enabled");
    assertThat(tags).extracting(Tag::getValue).containsExactlyInAnyOrder("true", "false");
  }

  @Test
  void testBuildTags_WithOnlyStringMetrics_ShouldCreateOnlyStringTags() {
    // Arrange
    MetricRecord stringRecord1 = createMetricRecord("service", "test-service");
    MetricRecord stringRecord2 = createMetricRecord("version", "1.0.0");

    when(metricCollection.stream()).thenReturn(Stream.of(stringRecord1, stringRecord2));

    // Act
    List<Tag> tags = publisher.buildTags(metricCollection);

    // Assert
    assertThat(tags).hasSize(2);
    assertThat(tags).extracting(Tag::getKey).containsExactlyInAnyOrder("service", "version");
    assertThat(tags).extracting(Tag::getValue).containsExactlyInAnyOrder("test-service", "1.0.0");
  }

  @Test
  void testPublish_WithMixedMetrics_ShouldProcessBothTypes() {
    // Arrange
    MetricRecord integerRecord = createMetricRecord("test.counter", 5);
    Duration duration = Duration.ofSeconds(2);
    MetricRecord durationRecord = createMetricRecord("test.timer", duration);

    when(metricCollection.stream())
            .thenAnswer(invocation -> Stream.of(integerRecord, durationRecord));

    when(meterRegistry.counter(eq("test.counter"), any(List.class))).thenReturn(counter);
    when(meterRegistry.timer(eq("test.timer"), any(List.class))).thenReturn(timer);

    // Act
    publisher.publish(metricCollection);

    // Assert
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(meterRegistry, times(1)).counter(eq("test.counter"), any(List.class));
      verify(meterRegistry, times(1)).timer(eq("test.timer"), any(List.class));
      verify(counter, times(1)).increment(5);
      verify(timer, times(1)).record(duration);
    });
  }

}