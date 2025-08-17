package de.freshplan.test.helpers;

import de.freshplan.domain.permission.entity.Permission;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for PermissionHelperPg race-condition safety.
 * 
 * Verifies that the helper correctly handles concurrent permission creation
 * without race conditions or duplicate entries.
 */
@QuarkusTest
class PermissionHelperPgTest {
    
    @Inject
    PermissionHelperPg permissionHelper;
    
    @Test
    @TestTransaction
    void findOrCreatePermission_withValidCode_createsPermission() {
        // When
        Permission permission = permissionHelper.findOrCreatePermission(
            "test:read", 
            "Read test data"
        );
        
        // Then
        assertThat(permission).isNotNull();
        assertThat(permission.getPermissionCode()).isEqualTo("test:read");
        assertThat(permission.getResource()).isEqualTo("test");
        assertThat(permission.getAction()).isEqualTo("read");
        assertThat(permission.getDescription()).isEqualTo("[TEST] Read test data");
    }
    
    @Test
    @TestTransaction
    void findOrCreatePermission_calledTwice_returnsSamePermission() {
        // When - Create first time
        Permission first = permissionHelper.findOrCreatePermission(
            "test:write", 
            "Write test data"
        );
        
        // When - Create second time with same code
        Permission second = permissionHelper.findOrCreatePermission(
            "test:write", 
            "Different description" // Should be ignored
        );
        
        // Then
        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(first.getPermissionCode()).isEqualTo(second.getPermissionCode());
        // Description should remain from first creation
        assertThat(second.getDescription()).isEqualTo("[TEST] Write test data");
    }
    
    @Test
    @TestTransaction
    void findOrCreatePermission_withInvalidCode_throwsException() {
        // Test null code
        assertThatThrownBy(() -> 
            permissionHelper.findOrCreatePermission(null, "Description")
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must follow format");
        
        // Test code without colon
        assertThatThrownBy(() -> 
            permissionHelper.findOrCreatePermission("invalid", "Description")
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must follow format");
        
        // Test code with multiple colons
        assertThatThrownBy(() -> 
            permissionHelper.findOrCreatePermission("too:many:colons", "Description")
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exactly one");
    }
    
    @Test
    @TestTransaction
    void findOrCreatePermission_withoutDescription_usesDefault() {
        // When
        Permission permission = permissionHelper.findOrCreatePermission("test:delete");
        
        // Then
        assertThat(permission).isNotNull();
        assertThat(permission.getDescription()).isEqualTo("[TEST] Test permission: test:delete");
    }
    
    @Test
    @TestTransaction
    void findOrCreatePermissions_createsMultiple() {
        // When
        Permission[] permissions = permissionHelper.findOrCreatePermissions(
            "test:create",
            "test:update",
            "test:list"
        );
        
        // Then
        assertThat(permissions).hasSize(3);
        assertThat(permissions[0].getPermissionCode()).isEqualTo("test:create");
        assertThat(permissions[1].getPermissionCode()).isEqualTo("test:update");
        assertThat(permissions[2].getPermissionCode()).isEqualTo("test:list");
    }
    
    @Test
    @TestTransaction
    void deleteTestPermissions_onlyDeletesTestPermissions() {
        // Given - Create test permissions
        permissionHelper.findOrCreatePermission("cleanup:test1", "Test 1");
        permissionHelper.findOrCreatePermission("cleanup:test2", "Test 2");
        
        // When - Delete test permissions
        int deleted = permissionHelper.deleteTestPermissions();
        
        // Then - At least our 2 permissions were deleted
        assertThat(deleted).isGreaterThanOrEqualTo(2);
        
        // Verify they're gone by trying to create again
        // (would return existing if not deleted)
        Permission newPerm = permissionHelper.findOrCreatePermission("cleanup:test1", "Test 1 New");
        assertThat(newPerm.getDescription()).isEqualTo("[TEST] Test 1 New");
    }
    
    /**
     * Critical test: Verify race-condition safety with concurrent threads.
     * This test simulates the CI environment where multiple tests run in parallel.
     */
    @Test
    void concurrent_permission_creation_is_safe() throws Exception {
        String code = "concurrent:test";
        int threadCount = 16;
        int attemptsPerThread = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Permission>> futures = new ArrayList<>();
        
        // Create tasks that will all try to create the same permission
        for (int thread = 0; thread < threadCount; thread++) {
            final int threadNum = thread;
            Future<Permission> future = executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();
                    
                    // Try to create the same permission multiple times
                    Permission result = null;
                    for (int i = 0; i < attemptsPerThread; i++) {
                        result = permissionHelper.findOrCreatePermission(
                            code, 
                            "Created by thread " + threadNum
                        );
                    }
                    return result;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        
        // Start all threads at the same time
        startLatch.countDown();
        
        // Collect all results
        List<Permission> results = new ArrayList<>();
        for (Future<Permission> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Verify: All threads got the same permission (same ID)
        assertThat(results).hasSize(threadCount);
        UUID firstId = results.get(0).getId();
        assertThat(results)
            .extracting(Permission::getId)
            .containsOnly(firstId);
        
        // Verify: Permission code is consistent
        assertThat(results)
            .extracting(Permission::getPermissionCode)
            .containsOnly(code);
    }
    
    /**
     * Test high-volume concurrent creation of different permissions.
     * This simulates a more realistic scenario where different tests
     * create different permissions simultaneously.
     */
    @Test
    void concurrent_different_permissions_creation() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        List<Callable<Permission>> tasks = IntStream.range(0, 50)
            .<Callable<Permission>>mapToObj(i -> () -> 
                permissionHelper.findOrCreatePermission(
                    "resource" + (i % 10) + ":action" + (i % 5),
                    "Test permission " + i
                )
            )
            .toList();
        
        // Execute all tasks
        List<Future<Permission>> futures = executor.invokeAll(tasks);
        
        // Verify all completed successfully
        for (Future<Permission> future : futures) {
            Permission permission = future.get(5, TimeUnit.SECONDS);
            assertThat(permission).isNotNull();
            assertThat(permission.getId()).isNotNull();
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}