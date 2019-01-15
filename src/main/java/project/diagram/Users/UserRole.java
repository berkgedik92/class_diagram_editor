package project.diagram.Users;

import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public
class UserRole {

    private Set<String> rights;

    public UserRole() {
        this.rights = new HashSet<>();
    }

    public void addRight(String URL) {
        rights.add(URL);
    }

    public boolean hasAccessRight(String URL) {
        return rights.contains(URL);
    }
}
