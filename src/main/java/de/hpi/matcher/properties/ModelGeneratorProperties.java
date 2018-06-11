package de.hpi.matcher.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("modelgenerator")
@Getter
@Setter
@Primary
public class ModelGeneratorProperties {

    private String uri;
    private String modelRoute;
    private String categoryClassifierRoute;
    private String brandClassifierRoute;

}
