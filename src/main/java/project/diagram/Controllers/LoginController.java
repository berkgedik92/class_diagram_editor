package project.diagram.Controllers;

import project.diagram.Login.UserLoginMessage;
import project.diagram.Services.AdminUserRepo;
import project.diagram.Services.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.diagram.Users.AdminUser;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Controller
@RequestMapping("login")
class LoginController {

    @Autowired private TokenManager tokenManager;
    @Autowired private AdminUserRepo adminUserRepo;

    @ModelAttribute("hostAddress")
    public String getUserdata(HttpServletRequest request) {
        return (String) request.getAttribute("hostAddress");
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody UserLoginMessage login) {
        try {
            if (login.getUsername() == null || login.getPassword() == null)
                return new ResponseEntity<>("Empty username or password", HttpStatus.UNAUTHORIZED);

            AdminUser user = adminUserRepo.findUser(login.getUsername(), login.getPassword());

            if (user == null)
                return new ResponseEntity<>("Wrong username or password", HttpStatus.UNAUTHORIZED);

            String token = tokenManager.produce(login.getUsername());

            HashMap<String, Object> response = new HashMap<>();
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Server exception", HttpStatus.BAD_REQUEST);
        }
    }
}