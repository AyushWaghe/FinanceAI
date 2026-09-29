package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtil {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationUtil.class);
    public static Integer getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        log.info("User id from Authentication Util is",(Integer) authentication.getPrincipal());
        return (Integer) authentication.getPrincipal();
    }
}
