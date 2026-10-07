# Java JUnit 5 & Mockito Test Class Structural Template

```java
package com.example.service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
@ExtendWith(MockitoExtension.class)
@DisplayName("TargetService Unit Tests")
class TargetServiceTest {
    @Mock
    private ExternalRepository externalRepository;
    @Mock
    private NotificationClient notificationClient;
    @InjectMocks
    private TargetService sut;
    @Nested
    @DisplayName("processOrder Method Tests")
    class ProcessOrderTests {
        @Test
        @DisplayName("Given valid order when processOrder then return confirmed status")
        void givenValidOrder_whenProcessOrder_thenReturnConfirmedStatus() {
            // Arrange
            String orderId = "ORD-001";
            Order mockOrder = OrderFixture.createValidOrder(orderId);
            given(externalRepository.findById(orderId)).willReturn(Optional.of(mockOrder));
            given(notificationClient.sendNotice(any())).willReturn(true);
            // Act
            OrderResult result = sut.processOrder(orderId);
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo("CONFIRMED");
            then(externalRepository).should(times(1)).findById(orderId);
            then(notificationClient).should(times(1)).sendNotice(any());
        }
        @Test
        @DisplayName("Given non-existent order when processOrder then throw OrderNotFoundException")
        void givenNonExistentOrder_whenProcessOrder_thenThrowException() {
            // Arrange
            String orderId = "INVALID";
            given(externalRepository.findById(orderId)).willReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> sut.processOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
            then(notificationClient).shouldHaveNoInteractions();
        }
    }
    // Helper Test Data Builders / Fixtures
    private static final class OrderFixture {
        static Order createValidOrder(String id) {
            return new Order(id, BigDecimal.valueOf(100.0), "PENDING");
        }
    }
}
```
