package java201.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
                .antMatchers("/admin").hasRole("ADMIN")
                .and()
            .formLogin().loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll()
                .and()
                .authorizeRequests();
    }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth

                    .ldapAuthentication()
                    .userSearchFilter("(uid={0})")
                    .groupSearchBase("ou=groups")
                    .contextSource(contextSourceLdap())
                    .passwordCompare()
                    .passwordAttribute("userPassword");
        }

//  TODO in the future this embeded LDAP should be switched to Accenture LDAP or to adop cartridge LDAP
//
            @Bean
        public DefaultSpringSecurityContextSource contextSourceLdap() {
            return  new DefaultSpringSecurityContextSource(Arrays.asList("ldap://localhost:10389/"), "dc=ldap,dc=example,dc=com");
//            return  new DefaultSpringSecurityContextSource(Arrays.asList("ldap://javax.accenture.lv:389/"), "ou=people,dc=dir,dc=svc,dc=accenture,dc=com");
//            return  new DefaultSpringSecurityContextSource(Arrays.asList("ldap://54.154.224.21:389/"), "dc=ldap,dc=example,dc=com");

        }


}
