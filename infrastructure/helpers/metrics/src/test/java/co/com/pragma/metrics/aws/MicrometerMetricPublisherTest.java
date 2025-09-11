package co.com.pragma.metrics.aws;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.metrics.*;
import software.amazon.awssdk.metrics.internal.DefaultMetricCollection;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MicrometerMetricPublisherTest {

  @Mock
  private MeterRegistry registry;

  @Mock
  private Timer timer;

  @Mock
  private Counter counter;

  private MicrometerMetricPublisher publisher;

  private <T> MetricRecord<T> metricRecord(SdkMetric<T> sdkMetric, T value) {
    return new MetricRecord<>() {
      @Override
      public SdkMetric<T> metric() {
        return sdkMetric;
      }

      @Override
      public T value() {
        return value;
      }
    };
  }

  private MetricCollection collectionOf(MetricRecord<?>... records) {
    return new DefaultMetricCollection("test",
            java.util.stream.Stream.of(records)
                    .collect(java.util.stream.Collectors.groupingBy(MetricRecord::metric)),
            java.util.List.of());
  }

  @BeforeEach
  void setUp() {
    publisher = new MicrometerMetricPublisher(registry);
  }

  @Test
  void shouldRecordTimerWhenDurationMetric() throws InterruptedException {
    // Arrange
    Duration duration = Duration.ofMillis(500);
    SdkMetric<Duration> sdkMetric = SdkMetric.create("test.duration", Duration.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    MetricRecord<Duration> record = metricRecord(sdkMetric, duration);
    MetricCollection collection = collectionOf(record);

    when(registry.timer(eq("test.duration"), any(List.class))).thenReturn(timer);

    // Act
    publisher.publish(collection);

    // Wait for async execution
    Thread.sleep(100);

    // Assert
    verify(timer).record(duration);
    verify(registry).timer(eq("test.duration"), any(List.class));
  }

  @Test
  void shouldIncrementCounterWhenIntegerMetric() throws InterruptedException {
    // Arrange
    Integer value = 3;
    SdkMetric<Integer> sdkMetric = SdkMetric.create("test.counter", Integer.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    MetricRecord<Integer> record = metricRecord(sdkMetric, value);
    MetricCollection collection = collectionOf(record);

    when(registry.counter(eq("test.counter"), any(List.class))).thenReturn(counter);

    // Act
    publisher.publish(collection);

    // Wait for async execution
    Thread.sleep(100);

    // Assert
    verify(counter).increment(value);
    verify(registry).counter(eq("test.counter"), any(List.class));
  }

  @Test
  void shouldBuildTagsFromStringAndBooleanValues() throws Exception {
    // Arrange
    SdkMetric<String> tag1 = SdkMetric.create("env2", String.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    SdkMetric<Boolean> tag2 = SdkMetric.create("enabled", Boolean.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    MetricRecord<String> record1 = metricRecord(tag1, "prod");
    MetricRecord<Boolean> record2 = metricRecord(tag2, true);
    MetricCollection collection = collectionOf(record1, record2);

    // Use reflection to access private method
    Method buildTagsMethod = MicrometerMetricPublisher.class
            .getDeclaredMethod("buildTags", MetricCollection.class);
    buildTagsMethod.setAccessible(true);

    // Act
    @SuppressWarnings("unchecked")
    List<Tag> tags = (List<Tag>) buildTagsMethod.invoke(publisher, collection);

    // Assert
    assertThat(tags).hasSize(2);
    assertThat(tags).extracting(Tag::getKey)
            .containsExactlyInAnyOrder("env2", "enabled");
    assertThat(tags).extracting(Tag::getValue)
            .containsExactlyInAnyOrder("prod", "true");
  }

  @Test
  void shouldIgnoreUnsupportedMetricTypes() throws InterruptedException {
    // Arrange
    SdkMetric<Double> doubleMetric = SdkMetric.create("test.double", Double.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    SdkMetric<Long> longMetric = SdkMetric.create("test.long", Long.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);

    MetricRecord<Double> doubleRecord = metricRecord(doubleMetric, 3.14);
    MetricRecord<Long> longRecord = metricRecord(longMetric, 1000L);
    MetricCollection collection = collectionOf(doubleRecord, longRecord);

    // Act
    publisher.publish(collection);

    // Wait for async execution
    Thread.sleep(100);

    // Assert
    verifyNoInteractions(registry);
  }

  @Test
  void shouldHandleMixedMetricTypes() throws InterruptedException {
    // Arrange
    Duration duration = Duration.ofSeconds(1);
    Integer counterValue = 5;
    String tagValue = "test-env";

    SdkMetric<Duration> timerMetric = SdkMetric.create("test.timer", Duration.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    SdkMetric<Integer> counterMetric = SdkMetric.create("test.counter2", Integer.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    SdkMetric<String> tagMetric = SdkMetric.create("environment", String.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);

    MetricRecord<Duration> timerRecord = metricRecord(timerMetric, duration);
    MetricRecord<Integer> counterRecord = metricRecord(counterMetric, counterValue);
    MetricRecord<String> tagRecord = metricRecord(tagMetric, tagValue);

    MetricCollection collection = collectionOf(timerRecord, counterRecord, tagRecord);

    when(registry.timer(eq("test.timer"), any(List.class))).thenReturn(timer);
    when(registry.counter(eq("test.counter2"), any(List.class))).thenReturn(counter);

    // Act
    publisher.publish(collection);

    // Wait for async execution
    Thread.sleep(100);

    // Assert
    verify(timer).record(duration);
    verify(counter).increment(counterValue);

    // Verify tags are built correctly
    ArgumentCaptor<List<Tag>> timerTagsCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<List<Tag>> counterTagsCaptor = ArgumentCaptor.forClass(List.class);

    verify(registry).timer(eq("test.timer"), timerTagsCaptor.capture());
    verify(registry).counter(eq("test.counter2"), counterTagsCaptor.capture());

    List<Tag> timerTags = timerTagsCaptor.getValue();
    List<Tag> counterTags = counterTagsCaptor.getValue();

    assertThat(timerTags).hasSize(1);
    assertThat(counterTags).hasSize(1);
    assertThat(timerTags.get(0).getKey()).isEqualTo("environment");
    assertThat(timerTags.get(0).getValue()).isEqualTo("test-env");
  }

  @Test
  void shouldHandleEmptyMetricCollection() throws InterruptedException {
    // Arrange
    MetricCollection emptyCollection = collectionOf();

    // Act
    publisher.publish(emptyCollection);

    // Wait for async execution
    Thread.sleep(100);

    // Assert
    verifyNoInteractions(registry);
  }

  @Test
  void shouldHandleNullValues() throws InterruptedException {
    // Arrange
    SdkMetric<String> stringMetric = SdkMetric.create("test.string", String.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    MetricRecord<String> nullRecord = metricRecord(stringMetric, null);
    MetricCollection collection = collectionOf(nullRecord);

    // Act
    publisher.publish(collection);

    // Wait for async execution
    Thread.sleep(100);

    // Assert
    verifyNoInteractions(registry);
  }

  @Test
  void shouldExecutePublishAsynchronously() {
    // Arrange
    Duration duration = Duration.ofMillis(100);
    SdkMetric<Duration> sdkMetric = SdkMetric.create("test.async", Duration.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    MetricRecord<Duration> record = metricRecord(sdkMetric, duration);
    MetricCollection collection = collectionOf(record);

    when(registry.timer(eq("test.async"), any(List.class))).thenReturn(timer);

    // Act
    long startTime = System.currentTimeMillis();
    publisher.publish(collection);
    long endTime = System.currentTimeMillis();

    // Assert - Should return immediately (async execution)
    assertThat(endTime - startTime).isLessThan(50); // Should be very fast
  }

  @Test
  void closeShouldNotThrow() {
    // Act & Assert
    assertThatCode(() -> publisher.close()).doesNotThrowAnyException();
  }

  @Test
  void shouldCreatePublisherWithValidRegistry() {
    // Arrange & Act
    MicrometerMetricPublisher newPublisher = new MicrometerMetricPublisher(registry);

    // Assert
    assertThat(newPublisher).isNotNull();
  }

  @Test
  void shouldHandleBooleanTags() throws Exception {
    // Arrange

    SdkMetric<Boolean> booleanMetric = SdkMetric.create("is.enabled", Boolean.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);

    MetricRecord<Boolean> booleanRecord = metricRecord(booleanMetric, false);
    MetricCollection collection = collectionOf(booleanRecord);

    Method buildTagsMethod = MicrometerMetricPublisher.class
            .getDeclaredMethod("buildTags", MetricCollection.class);
    buildTagsMethod.setAccessible(true);

    // Act
    @SuppressWarnings("unchecked")
    List<Tag> tags = (List<Tag>) buildTagsMethod.invoke(publisher, collection);

    // Assert
    assertThat(tags).hasSize(1);
    assertThat(tags.get(0).getKey()).isEqualTo(booleanMetric.name());
    assertThat(tags.get(0).getValue()).isEqualTo("false");
  }

  @Test
  void shouldFilterOutNonStringNonBooleanValuesFromTags() throws Exception {
    // Arrange
    // Arrange
    SdkMetric<Integer> intMetric = SdkMetric.create("count", Integer.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);
    SdkMetric<String> stringMetric = SdkMetric.create("env", String.class,
            MetricLevel.INFO, MetricCategory.CUSTOM);

    MetricRecord<Integer> intRecord = metricRecord(intMetric, 42);
    MetricRecord<String> stringRecord = metricRecord(stringMetric, "production");

    MetricCollection collection = collectionOf(intRecord, stringRecord);

    Method buildTagsMethod = MicrometerMetricPublisher.class
            .getDeclaredMethod("buildTags", MetricCollection.class);
    buildTagsMethod.setAccessible(true);

    // Act
    @SuppressWarnings("unchecked")
    List<Tag> tags = (List<Tag>) buildTagsMethod.invoke(publisher, collection);

    // Assert
    assertThat(tags).hasSize(1);
    assertThat(tags.get(0).getKey()).isEqualTo(stringMetric.name());
    assertThat(tags.get(0).getValue()).isEqualTo("production");
  }
}