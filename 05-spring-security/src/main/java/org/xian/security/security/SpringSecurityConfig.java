package org.xian.security.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

/**
 * @author xian
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource
    private MyUserDetailsServiceImpl userDetailsService;

    @Override
    public void configure(WebSecurity web) {
        // 忽略前端静态资源 css js 等
        web.ignoring().antMatchers("/css/**");
        web.ignoring().antMatchers("/js/**");
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 设置密码加密方式
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCryptPasswordEncoder
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 允许无授权访问 "/login"、"/register" "/register-save"
        // 其他地址的访问均需验证权限
        http.authorizeRequests()
                .antMatchers("/login", "/register", "/register-save", "/error").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                // 用户名和用户密码参数名称
                .passwordParameter("password")
                .usernameParameter("username")
                // 指定登录页面
                .loginPage("/login")
                // 登录错误跳转到 /login-error
                .failureUrl("/login-error")
                .permitAll()
                .and()
                // 设置退出登录的 URL 和退出成功后跳转页面
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login");
    }
}
