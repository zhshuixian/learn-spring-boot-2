package org.xian.shiro.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.xian.shiro.MyResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author xian
 */
public class ShiroAuthFilter extends BasicHttpAuthenticationFilter {

    /**
     * // 存储Token的H Headers Key
     */
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Token 的开头部分
     */
    protected static final String BEARER = "Bearer ";

    private String token;


    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        // 设置 主题
        // 自动调用 ShiroRealm 进行 Token 检查
        this.getSubject(request, response).login(new ShiroAuthToken(this.token));
        return true;
    }

    /**
     * 是否运行访问
     *
     * @param request     Request
     * @param response    Response
     * @param mappedValue mapperValue
     * @return true 表示允许放翁
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // Request 中存在 Token
        if (this.getAuthzHeader(request) != null) {
            try {
                executeLogin(request, response);
                // 刷新 Token 1, Token 未过期，每次都调用 refreshToken 判断是否需要刷新 Token
                TokenUtils tokenUtils = new TokenUtils();
                String refreshToken = tokenUtils.refreshToken(this.token);
                if (refreshToken != null) {
                    this.token = refreshToken;
                    shiroAuthResponse(response, true);
                }
                return true;
            } catch (Exception e) {
                // 刷新 Token 2  Token 已经过期，如果过期是在规定时间内则刷新 Token
                TokenUtils tokenUtils = new TokenUtils();
                String refreshToken = tokenUtils.refreshToken(this.token);
                if (refreshToken != null) {
                    this.token = refreshToken.substring(BEARER.length());
                    // 重新调用 executeLogin 授权
                    executeLogin(request, response);
                    shiroAuthResponse(response, true);
                    return true;
                } else {
                    // Token 刷新失败没得救或者非法 Token
                    shiroAuthResponse(response, false);
                    return false;
                }
            }
        } else {
            // Token 不存在，返回未授权信息
            shiroAuthResponse(response, false);
            return false;
        }

    }


    /**
     * Token 预处理，从 Request 的 Header 取得 Token
     *
     * @param request ServletRequest
     * @return token or null
     */
    @Override
    protected String getAuthzHeader(ServletRequest request) {
        try {
            // header 是否存在 Token
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            this.token = httpRequest.getHeader(AUTHORIZATION_HEADER).substring(BEARER.length());
            return this.token;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 未授权访问
     *
     * @param response Response
     * @param refresh  是否是刷新 Token
     */
    private void shiroAuthResponse(ServletResponse response, boolean refresh) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (refresh) {
            // 刷新 Token，设置返回的头部
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
            httpServletResponse.addHeader(AUTHORIZATION_HEADER, BEARER + this.token);
        } else {
            // 设置 HTTP 状态码为 401
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            // 设置 Json 格式返回
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            try {
                // PrintWriter 输出 Response 返回信息
                PrintWriter writer = httpServletResponse.getWriter();
                ObjectMapper mapper = new ObjectMapper();
                MyResponse myResponse = new MyResponse("error", "非授权访问");
                // 将对象输出为 JSON 格式。可以通过重写 MyResponse 的 toString() ，直接通过 myResponse.toString() 即可
                writer.write(mapper.writeValueAsString(myResponse));
            } catch (IOException e) {
                // 打印日志
            }
        }
    }


}
