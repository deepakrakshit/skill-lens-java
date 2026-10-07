# Spring Boot 3 Java Architecture & Fixture Patterns

## 1. Pure Unit Testing Constructor Injection

Use constructor injection for lightweight, fast tests without loading the Spring ApplicationContext:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentClient paymentClient;
    @InjectMocks
    private OrderService sut; // or instantiate directly: new OrderService(orderRepository, paymentClient)
}
```

## 2. Modern Java 17/21 Records as Test Fixtures

Leverage Java Records for immutable, zero-boilerplate DTOs and test fixtures:
```java
public record OrderDto(String id, BigDecimal amount, String currency) {
    public static OrderDto validFixture(String id) {
        return new OrderDto(id, BigDecimal.valueOf(99.99), "USD");
    }
}
```

## 3. WebClient & RestClient Mocking Patterns

Mocking `WebClient` or `RestClient`:
```java
@Mock
private WebClient webClient;
@Mock
private WebClient.RequestBodyUriSpec requestBodyUriSpec;
@Mock
private WebClient.ResponseSpec responseSpec;

@BeforeEach
void setUpWebClient() {
    given(webClient.post()).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.bodyValue(any())).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.retrieve()).willReturn(responseSpec);
}
```
