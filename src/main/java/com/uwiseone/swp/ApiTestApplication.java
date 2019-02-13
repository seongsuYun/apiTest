package com.uwiseone.swp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class ApiTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiTestApplication.class, args);
	}
	
	@EnableResourceServer
	protected static class ResourceServer extends ResourceServerConfigurerAdapter { 
		@Override 
		public void configure(HttpSecurity http) throws Exception { 
			// @formatter:off 
			http.requestMatchers()
				.antMatchers("/admin/beans", "/index").and().authorizeRequests() 
				.anyRequest().access("#oauth2.hasScope('read')")
			; 
		} 
		
		@Override 
		public void configure(ResourceServerSecurityConfigurer resources) throws Exception { 
			resources.resourceId("sparklr"); 
		} 
	}
	
	@Configuration 
	@EnableAuthorizationServer 
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter { 
		
		@Autowired 
		private AuthenticationManager authenticationManager; 
		
		@Override public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception { 
			endpoints.authenticationManager(authenticationManager); 
		} 
		
		@Override 
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			
			// @formatter:off 
			clients.inMemory() 
				.withClient("my-trusted-client") 
					.authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit") 
					.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT") 
					.scopes("read", "write", "trust") 
					.resourceIds("sparklr")
					.accessTokenValiditySeconds(60)
					.autoApprove(true)
			.and() 
				.withClient("my-client-with-registered-redirect") 
					.authorizedGrantTypes("authorization_code") 
					.authorities("ROLE_CLIENT")
					.scopes("read", "trust")
					.resourceIds("sparklr") 
					.redirectUris("http://localhost:8080/callback")
					.secret(encoder.encode("secret"))
					.autoApprove(true)
			.and()
				.withClient("my-client-with-secret") 
					.authorizedGrantTypes("client_credentials", "password") 
					.authorities("ROLE_CLIENT") 
					.scopes("read")
					.resourceIds("sparklr") 
					.secret(encoder.encode("secret"))
					; 
			// @formatter:on 
		} 
	}
	
	@Configuration
	@EnableWebSecurity
	protected static class SecurityConfig extends WebSecurityConfigurerAdapter {
		
		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring()
				.antMatchers("/welcome");
		}

		@Override 
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			auth.inMemoryAuthentication().withUser("min").password(encoder.encode("min")).roles("USER");
		}
		
		
		@Bean
		@Override 
		public AuthenticationManager authenticationManagerBean() throws Exception { 
			return super.authenticationManagerBean(); 
		}
	}

}

