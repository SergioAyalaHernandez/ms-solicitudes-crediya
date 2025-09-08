package co.com.pragma.r2dbc.helper;

import co.com.pragma.model.gateway.JsonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JacksonJsonConverterTest {

  private ObjectMapper objectMapper;
  private JsonConverter jsonConverter;

  @BeforeEach
  void setUp() {
    objectMapper = mock(ObjectMapper.class);
    jsonConverter = new JacksonJsonConverter(objectMapper);
  }

  @Test
  void toJson_ShouldReturnJsonString_WhenObjectIsValid() throws JsonProcessingException {
    // Arrange
    Object testObject = new Object();
    String expectedJson = "{\"key\":\"value\"}";
    when(objectMapper.writeValueAsString(testObject)).thenReturn(expectedJson);

    // Act
    Optional<String> result = jsonConverter.toJson(testObject);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(expectedJson, result.get());
    verify(objectMapper, times(1)).writeValueAsString(testObject);
  }

  @Test
  void toJson_ShouldReturnEmptyOptional_WhenJsonProcessingExceptionOccurs() throws JsonProcessingException {
    // Arrange
    Object testObject = new Object();
    when(objectMapper.writeValueAsString(testObject)).thenThrow(new JsonProcessingException("Error") {});

    // Act
    Optional<String> result = jsonConverter.toJson(testObject);

    // Assert
    assertFalse(result.isPresent());
    verify(objectMapper, times(1)).writeValueAsString(testObject);
  }
}