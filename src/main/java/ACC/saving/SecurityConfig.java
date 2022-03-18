package ACC.saving;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and().csrf().disable().antMatcher("/**").authorizeRequests()
        		.antMatchers("/info").permitAll()        
        		.antMatchers("/save").permitAll()
                .antMatchers("/send").permitAll()
                .antMatchers("/setAutoSaveKey").permitAll()
                .antMatchers("/setAutoSaveActivity").permitAll()
                .antMatchers("/getMobileSession").permitAll()
                .antMatchers("/getMobileSessionList").permitAll()
                .antMatchers("/importTeamLap").permitAll()
                .antMatchers("/acc/*").permitAll()
                .antMatchers("/", "/login.html").authenticated()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().permitAll()
                .and().
                logout().logoutSuccessUrl("/");
        
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
       return source;
    }
}