package ACC.saving;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().antMatcher("/**").authorizeRequests()
                .antMatchers("/save").permitAll()
                .antMatchers("/send").permitAll()
                .antMatchers("/getMobileSession").permitAll()
                .antMatchers("/getMobileSessionList").permitAll()
                .antMatchers("/acc/*").permitAll()
                .antMatchers("/", "/login.html").authenticated()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().permitAll()
                .and().
                logout().logoutSuccessUrl("/");
        
    }
}