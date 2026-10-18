package mohammad.development.praxis.modules.admin;

import mohammad.development.praxis.repos.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        adminSeeder = new AdminSeeder(adminRepository, passwordEncoder);
        ReflectionTestUtils.setField(adminSeeder, "adminDefaultPassword", "test-password");
    }

    @Test
    void run_noExistingAdmin_createsAdminWithConfiguredPassword() {
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("test-password")).thenReturn("encoded-password");

        adminSeeder.run();

        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());

        Admin savedAdmin = adminCaptor.getValue();
        assertEquals("admin", savedAdmin.getUsername());
        assertEquals("encoded-password", savedAdmin.getPasswordHash());
        assertTrue(savedAdmin.getRoles().contains("ADMIN"));
        assertTrue(savedAdmin.isEnabled());
    }

    @Test
    void run_existingAdminWithMatchingPassword_keepsPasswordHash() {
        Admin existingAdmin = existingAdmin("encoded-password");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));
        when(passwordEncoder.matches("test-password", "encoded-password")).thenReturn(true);

        adminSeeder.run();

        verify(passwordEncoder, never()).encode(anyString());
        verify(adminRepository).save(existingAdmin);
        assertEquals("encoded-password", existingAdmin.getPasswordHash());
        assertEquals(Set.of("ADMIN"), existingAdmin.getRoles());
        assertTrue(existingAdmin.isEnabled());
    }

    @Test
    void run_existingAdminWithDifferentPassword_updatesPasswordHash() {
        Admin existingAdmin = existingAdmin("old-encoded-password");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));
        when(passwordEncoder.matches("test-password", "old-encoded-password")).thenReturn(false);
        when(passwordEncoder.encode("test-password")).thenReturn("new-encoded-password");

        adminSeeder.run();

        verify(adminRepository).save(existingAdmin);
        assertEquals("new-encoded-password", existingAdmin.getPasswordHash());
    }

    private Admin existingAdmin(String passwordHash) {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordHash);
        admin.setRoles(Set.of("ADMIN"));
        admin.setEnabled(true);
        return admin;
    }
}
