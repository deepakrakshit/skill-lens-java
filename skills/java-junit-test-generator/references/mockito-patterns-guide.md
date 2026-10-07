# Mockito 5 & BDD Testing Best Practices Guide

## 1. BDDMockito Idiomatic Patterns

Prefer BDDMockito (`given`, `willReturn`, `willThrow`, `then`) to align with BDD / AAA test structure:
```java
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThat;

@Test
@DisplayName("Given valid order ID when findOrder then return order entity")
void givenValidOrderId_whenFindOrder_thenReturnOrder() {
    // Arrange
    String orderId = "ORD-001";
    Order mockOrder = new Order(orderId, BigDecimal.valueOf(100.0), OrderStatus.PENDING);
    given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));
    // Act
    Order result = sut.findOrder(orderId);
    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(orderId);
    then(orderRepository).should(times(1)).findById(orderId);
}
```

## 2. Argument Captors

Capture parameters passed to external dependencies for deep payload verification:
```java
@Captor
private ArgumentCaptor<EmailNotification> notificationCaptor;

@Test
@DisplayName("Given valid user when registerUser then dispatch welcome email")
void givenValidUser_whenRegisterUser_thenDispatchEmail() {
    User user = new User("usr-1", "user@example.com");
    sut.registerUser(user);
    then(notificationClient).should().sendEmail(notificationCaptor.capture());
    EmailNotification captured = notificationCaptor.getValue();
    assertThat(captured.getRecipient()).isEqualTo("user@example.com");
}
```

## 3. Mocking Static Methods (MockedStatic)

Scope static mocks (like `Instant.now()`) inside a try-with-resources block for thread-safe cleanup:
```java
@Test
@DisplayName("Given fixed timestamp when generateToken then token contains fixed date")
void givenFixedTimestamp_whenGenerateToken_thenUseFixedInstant() {
    Instant fixedInstant = Instant.parse("2026-01-01T00:00:00Z");
    try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
        mockedInstant.when(Instant::now).thenReturn(fixedInstant);
        String token = sut.generateToken("user-1");
        assertThat(token).contains("20260101");
    }
}
```

## 4. Resource & Lifecycle Management

Utilize `@ExtendWith(MockitoExtension.class)` or explicit `@AfterEach` teardown:
```java
private AutoCloseable closeable;

@BeforeEach
void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
}

@AfterEach
void tearDown() throws Exception {
    closeable.close();
}
```
