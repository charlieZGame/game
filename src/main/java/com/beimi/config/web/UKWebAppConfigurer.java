package com.beimi.config.web;

import com.beimi.web.interceptor.BackManagerInterceptorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.beimi.web.interceptor.CrossInterceptorHandler;
import com.beimi.web.interceptor.UserInterceptorHandler;

@Configuration
public class UKWebAppConfigurer 
        extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
    	registry.addInterceptor(new UserInterceptorHandler()).addPathPatterns("/**").excludePathPatterns("/login.html").excludePathPatterns("/tokens").
    	//registry.addInterceptor(new UserInterceptorHandler()).addPathPatterns("/**").excludePathPatterns("/tokens").
                excludePathPatterns("/api/**").excludePathPatterns("/dealFlow/**").excludePathPatterns("/houseCard/**").excludePathPatterns("/userCase/**").
                excludePathPatterns("/userManager/**").excludePathPatterns("/pay/**").excludePathPatterns("/wechart/login").excludePathPatterns("/wap/index.html").
                excludePathPatterns("/clearData/**").excludePathPatterns("/getData/**").
                excludePathPatterns("/appWebLoginData/**").excludePathPatterns("/appWebLogin/**");
    	registry.addInterceptor(new CrossInterceptorHandler()).addPathPatterns("/**");
        registry.addInterceptor(new BackManagerInterceptorHandler()).addPathPatterns("/dealFlow/**").addPathPatterns("/houseCard/**").
                addPathPatterns("/userManager/**");
        super.addInterceptors(registry);
    }
}