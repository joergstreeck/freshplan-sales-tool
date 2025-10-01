package de.freshplan.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mock-basierte Unit Tests für PingResource.
 *
 * <p>Sprint 2.1.4: Migriert von @QuarkusTest zu Mockito (~15s Ersparnis).
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PingResource Unit Tests")
class PingResourceMockTest {

  @Mock DataSource dataSource;

  @InjectMocks PingResource resource;

  private Connection mockConnection;
  private Statement mockStatement;
  private ResultSet mockResultSet;

  @BeforeEach
  void setUp() throws Exception {
    mockConnection = mock(Connection.class);
    mockStatement = mock(Statement.class);
    mockResultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.createStatement()).thenReturn(mockStatement);
    when(mockStatement.executeQuery("SELECT current_timestamp")).thenReturn(mockResultSet);
  }

  // ===== GET /api/ping Tests =====

  @Test
  @DisplayName("ping should return 200 with pong message")
  void ping_shouldReturn200WithPong() throws Exception {
    // Given
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getTimestamp(1)).thenReturn(new Timestamp(System.currentTimeMillis()));

    // When
    Response response = resource.ping();

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("message")).isEqualTo("pong");
    assertThat(body.get("timestamp")).isNotNull();
    assertThat(body.get("user")).isEqualTo("test-user");
    assertThat(body.get("dbTime")).isNotNull();

    verify(dataSource, times(1)).getConnection();
  }

  @Test
  @DisplayName("ping should handle DB errors gracefully")
  void ping_withDbError_shouldReturnErrorStatus() throws Exception {
    // Given
    when(dataSource.getConnection()).thenThrow(new RuntimeException("DB connection failed"));

    // When
    Response response = resource.ping();

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("message")).isEqualTo("pong");
    assertThat(body.get("dbStatus")).asString().contains("error");
  }

  @Test
  @DisplayName("ping should include timestamp")
  void ping_shouldIncludeTimestamp() throws Exception {
    // Given
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getTimestamp(1)).thenReturn(new Timestamp(System.currentTimeMillis()));

    // When
    Response response = resource.ping();

    // Then
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("timestamp")).isNotNull();
    assertThat(body.get("timestamp")).asString().matches("\\d{4}-\\d{2}-\\d{2}T.*");
  }
}
