package co.com.pragma.model.gateway;

import java.util.Optional;

public interface JsonConverter {
  Optional<String> toJson(Object object);
}
