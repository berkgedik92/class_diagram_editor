package project.diagram.Controllers;

import org.springframework.web.servlet.ModelAndView;

/*
    Populates meta properties for HTML pages. Those properties are used by JavaScript codes
    on the client side.

    hostAddress     =   The origin that has been used in browser by the user (example: http://localhost:7000/)
                        It is needed because it is prepended to all URLs to make user able to access resources

    loginURL        =   The absolute URL where a user can get logged in. If user is not able to send a token,
                        we redirect her to this URL.

    tokenGiver      =   The absolute URL where a user can send POST request to (with credentials) to get a token
                        User saves the token on his browser localstorage and uses it everytime he makes a JSON request

    tokenValidator  =   The absolute URL that gets a POST request with token and returns if the token is valid.
                        User validates his token and if it is not valid, the token is removed from localstorage and
                        user will be redirected to login page

    indexURL       =   The absolute URL of the index page (the page after user logs in). The user will be redirected
                        into that page after a successful login
 */
public class ControllerUtils {

    public static ModelAndView getPage(String url, String hostAddress) {
        ModelAndView view = new ModelAndView(url);
        view.addObject("hostAddress", hostAddress);
        view.addObject("loginURL", hostAddress + "login");
        view.addObject("tokenGiver", hostAddress + "login/token");
        view.addObject("tokenValidator", hostAddress + "token/validate");
        view.addObject("indexURL", hostAddress + "diagram");
        return view;
    }
}
