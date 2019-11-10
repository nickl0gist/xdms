package pl.com.xdms.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created on 18.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Component
public class UserControllerFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        log.info(req.getRequestURI());
        res.sendRedirect("/admin/users/role/asc");
        //filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {    }
}
