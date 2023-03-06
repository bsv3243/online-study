package seong.onlinestudy;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import seong.onlinestudy.interceptor.AuthenticationInterceptor;
import seong.onlinestudy.interceptor.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /* jwt 검증 인터셉터
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/login", "/api/v1/logout", "/api/v1/members");
    }
    */

    /*
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/login, /api/v1/logout", "/api/v1/members");
    }
     */

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowCredentials(true);
//                .allowedOrigins("http://localhost:8081");
    }
}
