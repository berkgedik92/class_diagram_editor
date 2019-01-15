package project.diagram.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import project.diagram.Users.AdminUser;

import javax.servlet.ServletException;
import java.util.Date;

@Service
public class TokenManager {

    @Value("${JWTKey}")
    private String JWTKey;

    @Autowired private AdminUserRepo adminUserRepo;

    public String produce (String username) {
        Claims claims = Jwts.claims();
        claims.setSubject("logintoken");
        claims.put("username", username);

        return Jwts.builder().setSubject("logintoken")
                .setIssuedAt(new Date())
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, JWTKey)
                .compact();
    }

    public AdminUser check (String token) throws ServletException {

        Claims claims;

        //1) If the token is invalid (corrupted or fake), reject the request
        try {
            claims = Jwts.parser().setSigningKey(JWTKey).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new ServletException("Invalid token");
        }

        //2) If the owner of token is not in DB (maybe we removed this user), reject the request
        String username = (String) claims.get("username");
        AdminUser user = adminUserRepo.findUser(username);

        if (user == null)
            throw new ServletException("The owner of this token does no longer exist");

        return user;
    }
}
