package project.diagram.Filters;

import project.diagram.Services.TokenManager;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import project.diagram.Users.AdminUser;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//Checks token sent by user, if token is invalid, throws exception and do not send the request to controllers
@Component
@Order(2)
class JWTFilter implements Filter {

    @Autowired private TokenManager tokenManager;

    private final List<String> tokenRequired = Arrays.asList("api", "token");

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        String requestedURI = request.getRequestURI();

        //For preflight requests, do not look for auth token
        if ("OPTIONS".equals(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String[] requestURIParts = requestedURI.substring(1).split("/");

        //If the user tries to make a request to a place where a token is required, check its token
        if (tokenRequired.contains(requestURIParts[0])) {

            String authHeader = request.getHeader("Auth");
            Claims claims;

            try {
                //1) If the user does not have a token, reject the request
                if (authHeader == null)
                    throw new ServletException("Missing Authorization header");

                //2) Check if the token is valid, if so, check if the owner user of the token still
                //exists

                AdminUser user = tokenManager.check(authHeader);

                //3) Check if this user has the right to access to the service that (s)he requested
                if (!user.hasAccessRight(requestURIParts[0]))
                    throw new ServletException("The user does not have right to access this resource");

                request.setAttribute("userdata", user);
            }
            catch (Exception e) {
                HttpServletResponse httpResponse = (HttpServletResponse) res;
                httpResponse.sendError(401, e.getMessage());
                return;
            }
        }

        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {

    }

    public void destroy() {

    }
}