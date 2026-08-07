package cyou.yuanbaomao.sellersprite.system.permission.config;

import cyou.yuanbaomao.sellersprite.system.permission.security.ApiPermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ApiPermissionWebMvcConfig implements WebMvcConfigurer {

    private static final int API_PERMISSION_INTERCEPTOR_ORDER = 100;

    private final ApiPermissionInterceptor apiPermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiPermissionInterceptor)
                .addPathPatterns("/api/**")
                .order(API_PERMISSION_INTERCEPTOR_ORDER);
    }
}
