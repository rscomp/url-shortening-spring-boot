package com.xcode.urlshortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.hash.Hashing;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@RestController
public class UrlShortenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerApplication.class, args);
	}

	@Autowired
	private StringRedisTemplate redisConn;

	@GetMapping("/greeting")
	public String helloWorld() {
		return "Welcome to XCode";
	}

	@PostMapping("/urls/add")
	public ResponseEntity<String> save(HttpServletRequest req) {
		final String queryParams = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
		final String url = (req.getRequestURI() + queryParams).substring(1);
		final String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
		redisConn.opsForValue().set(id, url);
		return new ResponseEntity<String>("http://localhost:8080/"+id, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public void redirect(@PathVariable String id, HttpServletResponse resp) throws Exception {
		final String url = redisConn.opsForValue().get(id);
		if (url != null)
			resp.sendRedirect(url);
		else
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
