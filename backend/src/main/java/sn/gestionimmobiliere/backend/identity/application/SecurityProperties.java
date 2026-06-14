package sn.gestionimmobiliere.backend.identity.application;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("app.security")public record SecurityProperties(String jwtSecret,long tokenValidityMinutes,String bootstrapEmail,String bootstrapPassword){}
