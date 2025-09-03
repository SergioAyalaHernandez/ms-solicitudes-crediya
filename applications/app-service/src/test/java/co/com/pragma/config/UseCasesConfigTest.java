package co.com.pragma.config;

import co.com.pragma.model.gateway.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }

        @Bean
        public co.com.pragma.usecase.gateway.GatewayExposeUser userGateway() {
            return org.mockito.Mockito.mock(co.com.pragma.usecase.gateway.GatewayExposeUser.class);
        }

        @Bean
        public co.com.pragma.model.gateway.JwtProvider jwtProvider() {
            return org.mockito.Mockito.mock(JwtProvider.class);
        }

        @Bean
        public co.com.pragma.model.gateway.CreditTypeGateway creditTypeGateway() {
            return org.mockito.Mockito.mock(co.com.pragma.model.gateway.CreditTypeGateway.class);
        }

        @Bean
        public co.com.pragma.model.gateway.CreditGateway creditGateway() {
            return org.mockito.Mockito.mock(co.com.pragma.model.gateway.CreditGateway.class);
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}