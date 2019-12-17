package com.saferycom.config;

import com.saferycom.util.config.ConfigLoad;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"com.saferycom"},
        excludeFilters = {@ComponentScan.Filter(classes = {EnableWebMvc.class}),
                @ComponentScan.Filter(type = FilterType.REGEX,
                        pattern = {"com.saferycom.sfs.*", "com.saferycom.controller.*"})})
@Import(value = {ManagerInitBeanConfig.class})
public class RootConfig {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        final String[] ignores = ConfigLoad.getStringArray("spring.profiles.ignore");
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + WebAppInitializer.ACTIVE_PROFILE
                + "/*.properties");
        List<Resource> resourceList = new ArrayList<>();
        CollectionUtils.addAll(resourceList, resources);
        CollectionUtils.filter(resourceList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                if (o instanceof Resource) {
                    Resource resource = (Resource) o;
                    return !ArrayUtils.contains(ignores, resource.getFilename());
                }
                return false;
            }
        });
        configurer.setLocations(resourceList.toArray(new Resource[0]));
        return configurer;
    }
}
