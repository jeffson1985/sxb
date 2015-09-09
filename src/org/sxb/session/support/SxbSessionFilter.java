package org.sxb.session.support;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sxb.data.redis.connection.jedis.JedisConnectionFactory;
import org.sxb.session.data.redis.RedisOperationsSessionRepository;
import org.sxb.session.web.http.SessionRepositoryFilter;

/**
 * Servlet Filter implementation class SxbSessionFilter
 */
public class SxbSessionFilter implements Filter {

	private ServletContext servletContext;
    /**
     * Default constructor. 
     */
    public SxbSessionFilter() {
       
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// place your code here
		JedisConnectionFactory   redisConn = new JedisConnectionFactory();
		redisConn.setHostName("localhost");
		redisConn.setPort(6379);
		redisConn.setUsePool(true);
		redisConn.setPassword("admin99");
		redisConn.afterPropertiesSet();
		RedisOperationsSessionRepository  rosr = new RedisOperationsSessionRepository(redisConn);
		rosr.setDefaultMaxInactiveInterval(180);
		@SuppressWarnings({ "rawtypes", "unchecked"})
		SessionRepositoryFilter<?> filter = new SessionRepositoryFilter(rosr);
		filter.setServletContext(this.servletContext);
		filter.doFilter(request, response, chain);
		
		
		// pass the request along the filter chain
		//chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		this.servletContext = fConfig.getServletContext();
	}

}
