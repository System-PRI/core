package pl.edu.amu.wmi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.naming.Context;
import java.util.Arrays;
import java.util.Hashtable;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity(
        securedEnabled = true
)
public class WebSecurityConfig {

//    @Value("${ldap.url}")
//    private String ldapUrl;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private CustomLdapUserDetailsMapper customLdapUserDetailsMapper;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//
//        return authProvider;
//    }


//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//
//        return authConfig.getAuthenticationManager();
//    }

//        @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//
//        return authConfig.getAuthenticationManager();
//    }

    @Autowired
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider("labs.wmi.amu.edu.pl", "ldap://labs.wmi.amu.edu.pl/", "DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl");
//        provider.setSearchFilter("sAMAccountName={0}");
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);

        Hashtable env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_PROTOCOL,"tls");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, "ldap://labs.wmi.amu.edu.pl/");
        provider.setContextEnvironmentProperties(env);
        provider.setUserDetailsContextMapper(customLdapUserDetailsMapper);
        return provider;
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Arrays.asList(activeDirectoryLdapAuthenticationProvider()));
    }

//    @Bean
//    public AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) throws Exception {
//        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
//        factory.setUserDnPatterns("sAMAccountName={0}");
////        factory.setUserDnPatterns("cn={0},OU=Students,OU=People");
////        factory.setUserSearchFilter("sAMAccountName={0}");
////        factory.setUserSearchFilter("(&(objectCategory=person)(sAMAccountName={0}))");
////        factory.setUserSearchBase("OU=Students,OU=People,DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl");
//        factory.setUserDetailsContextMapper(customLdapUserDetailsMapper);
//        return factory.createAuthenticationManager();
//    }

//        @Bean
//    public AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) throws Exception {
//        LdapPasswordComparisonAuthenticationManagerFactory factory = new LdapPasswordComparisonAuthenticationManagerFactory(contextSource, passwordEncoder());
////        factory.setUserDnPatterns("sAMAccountName={0}");
//        factory.setUserDnPatterns("uid={0}");
////        factory.setUserDnPatterns("cn={0},OU=Students,OU=People");
//        factory.setUserSearchFilter("sAMAccountName={0}");
////        factory.setUserSearchFilter("(&(objectCategory=person)(sAMAccountName={0}))");
////        factory.setUserSearchBase("OU=Students,OU=People,DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl");
//        factory.setUserDetailsContextMapper(customLdapUserDetailsMapper);
//        return factory.createAuthenticationManager();
//    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

//        @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new LdapShaPasswordEncoder();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(antMatcher("/auth/**")).permitAll()
                                .requestMatchers(antMatcher("pri/auth/**")).permitAll()
                                .anyRequest().authenticated()
                );

//        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Autowired
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .ldapAuthentication()
//                .userDetailsContextMapper(customLdapUserDetailsMapper)
////                .userDnPatterns("sAMAccountName={0},objectCategory=person")
////                .userDnPatterns("sAMAccountName={0}")
//                .userDnPatterns("uid={0}")
////                .userSearchBase("DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl")
////                .userSearchFilter("(&(objectCategory=person)(sAMAccountName={0}))")
////                .groupSearchBase("objectCategory=person")
//                .contextSource(contextSource())
////                .url("ldap://labs.wmi.amu.edu.pl:636/DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl")
////                .and()
//                .passwordCompare()
//                .passwordEncoder(new LdapShaPasswordEncoder())
//                .passwordAttribute("passwd");
////                .url("ldap://labs.wmi.amu.edu.pl")
////                .port(636);
////                .and()
////                .passwordCompare()
////                .passwordEncoder(new BCryptPasswordEncoder())
////                .passwordAttribute("userPassword");
//    }
//
//    @Autowired
//    public DefaultSpringSecurityContextSource contextSource() {
////        return new DefaultSpringSecurityContextSource(Arrays.asList("ldap://labs.wmi.amu.edu.pl:636/"), "DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl");
//        return new DefaultSpringSecurityContextSource("ldap://labs.wmi.amu.edu.pl:636/DC=labs,DC=wmi,DC=amu,DC=edu,DC=pl");
//    }
//
//    @Bean
//    ActiveDirectoryLdapAuthenticationProvider authenticationProvider() {
//        return new ActiveDirectoryLdapAuthenticationProvider("labs.wmi.amu.edu.pl", "ldap://labs.wmi.amu.edu.pl/");
//    }

}
