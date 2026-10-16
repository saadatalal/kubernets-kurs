package mohammad.development.praxis.modules.admin;

import lombok.RequiredArgsConstructor;
import mohammad.development.praxis.repos.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-password}")
    private String adminDefaultPassword;

    @Override
    public void run(String... args) {
        Admin admin = adminRepository.findByUsername("admin").orElse(null);

        if (admin == null) {
            admin = new Admin();
            admin.setUsername("admin");
            admin.setRoles(Set.of("ADMIN"));
            admin.setEnabled(true);
        }

        admin.setEnabled(true);
        admin.setRoles(Set.of("ADMIN"));

        String encodedPassword = passwordEncoder.encode(adminDefaultPassword);
        if (admin.getPasswordHash() == null || !passwordEncoder.matches(adminDefaultPassword, admin.getPasswordHash())) {
            admin.setPasswordHash(encodedPassword);
            adminRepository.save(admin);
            System.out.println("✅ Synced admin password from ADMIN_DEFAULT_PASSWORD");
            return;
        }

        adminRepository.save(admin);
        System.out.println("✅ Admin account is valid and ready for login");
    }
}
