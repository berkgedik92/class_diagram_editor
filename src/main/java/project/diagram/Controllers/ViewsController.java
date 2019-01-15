package project.diagram.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;

//Controller that returns views (JSP pages)
@Controller
@RequestMapping("")
class ViewsController {

    @ModelAttribute("hostAddress")
    public String getUserdata(HttpServletRequest request) {
        return (String) request.getAttribute("hostAddress");
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLogin(@ModelAttribute("hostAddress") String hostAddress) {
        return ControllerUtils.getPage("login.jsp", hostAddress);
    }

    @RequestMapping(value = "diagram", method = RequestMethod.GET)
    public ModelAndView getDiagramPage(@ModelAttribute("hostAddress") String hostAddress) {
        return ControllerUtils.getPage("index.jsp", hostAddress);
    }
}