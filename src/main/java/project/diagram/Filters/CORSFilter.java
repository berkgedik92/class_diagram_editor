package project.diagram.Filters;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//To allow CORS requests
@Component
@Order(1)
class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletResponse response = (HttpServletResponse)res;
        HttpServletRequest request = (HttpServletRequest)req;

        //fullURL: ex: http://127.0.0.1/xyz
        String fullURL = request.getRequestURL().toString();

        /*
            I want to extract "http://127.0.0.1/ from fullURL so I want to find first "/" that comes
            after http:// or https://, then I get until the part "/"
            (so http://127.0.0.1) and then add / at the end¨
        */

        String currentHost = fullURL.substring(0, fullURL.indexOf("/", fullURL.indexOf("//") + 2)) + "/";
        req.setAttribute("hostAddress", currentHost);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Auth");
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {

    }

    public void destroy() {

    }
}
