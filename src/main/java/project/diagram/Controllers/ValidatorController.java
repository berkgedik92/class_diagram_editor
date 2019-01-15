package project.diagram.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import project.diagram.Services.AdminUserRepo;
import project.diagram.Users.AdminUser;

import javax.servlet.http.HttpServletRequest;

//Controller that validates tokens sent by users (the main logic is applied in JWTFilter)
@Controller
@RequestMapping("token")
class ValidatorController {

    @Autowired
    private AdminUserRepo adminUserRepo;

    @ModelAttribute("userdata")
    public AdminUser getUserdata(HttpServletRequest request) {
        return (AdminUser) request.getAttribute("userdata");
    }

    @RequestMapping(value = "validate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> validate(@ModelAttribute("userdata") AdminUser userdata) {
        return new ResponseEntity<>(userdata, HttpStatus.OK);
    }
}
