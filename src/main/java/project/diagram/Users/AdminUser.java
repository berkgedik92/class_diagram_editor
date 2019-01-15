package project.diagram.Users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class AdminUser {

    @Id
    private String id;
    private String username;
    private String realName;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private UserRole roles;

    private String pictureURL;

    public AdminUser(String username, String password, String realName, String pictureURL, UserRole roles) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.pictureURL = pictureURL;
        this.roles = roles;
    }

    public boolean hasAccessRight(String URL) {
        return roles.hasAccessRight(URL);
    }
}
