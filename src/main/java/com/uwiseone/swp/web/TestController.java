package com.uwiseone.swp.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class TestController {

	@RequestMapping(value = "/index")
	@ResponseBody
	public ResponseEntity<Map<String, String>> index(HttpServletRequest request, Model model) throws Exception {	
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("code", "1");
		resultMap.put("message", "You're awesome man");
		
		return new ResponseEntity<Map<String,String>>(resultMap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/welcome")
	@ResponseBody
	public ResponseEntity<Map<String, String>> test(HttpServletRequest request, Model model) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("result", "This is public result");
		
		return new ResponseEntity<Map<String,String>>(resultMap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/callback")
	@ResponseBody
	public ResponseEntity<String> callback(@RequestParam("code") String code) throws IOException {
		ResponseEntity<String> response = null;
		System.out.println("Authorization Ccode------" + code);

		RestTemplate restTemplate = new RestTemplate();

		String credentials = "my-client-with-registered-redirect:secret";
		String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "Basic " + encodedCredentials);

		HttpEntity<String> request = new HttpEntity<String>(headers);

		String access_token_url = "http://localhost:8080/oauth/token";
		access_token_url += "?code=" + code;
		access_token_url += "&grant_type=authorization_code";
		access_token_url += "&redirect_uri=http://localhost:8080/callback";

		response = restTemplate.exchange(access_token_url, HttpMethod.POST, request, String.class);

		System.out.println("Access Token Response---------" + response.getBody());

		// Get the Access Token From the recieved JSON response
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(response.getBody());
		String token = node.path("access_token").asText();

		String url = "http://localhost:8080/index";
		System.out.println("===============================TOKEN");
		System.out.println(token);
		System.out.println("===============================");
		
		// Use the access token for authentication
		HttpHeaders headers1 = new HttpHeaders();
		headers1.add("Authorization", "Bearer " + token);
		HttpEntity<String> entity = new HttpEntity<>(headers1);

		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		
		return result;
	}
	
}
