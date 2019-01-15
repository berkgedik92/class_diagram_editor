package project.diagram;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import project.diagram.Services.AdminUserRepo;
import project.diagram.Users.AdminUser;
import project.diagram.Users.UserRole;

@SpringBootApplication
@Configuration
@EnableScheduling
public class ServerApplication implements CommandLineRunner, WebMvcConfigurer {

	@Autowired private AdminUserRepo adminUserRepo;

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Override
	public void run(String... args) {

        UserRole fullAdmin = new UserRole();
        fullAdmin.addRight("api");
        fullAdmin.addRight("token");

        if (adminUserRepo.findUser("user1") == null)
            adminUserRepo.save(new AdminUser("user1", "user1", "User1", "user1.gif", fullAdmin));

        if (adminUserRepo.findUser("user2") == null)
            adminUserRepo.save(new AdminUser("user2", "user2", "User2", "user2.gif", fullAdmin));
    }
}

