package com.ssafy.ebangjbang.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ebangjbang.model.KakaoProfile;
import com.ssafy.ebangjbang.model.OAuthToken;
import com.ssafy.ebangjbang.model.Dto.UserDto;

import com.ssafy.ebangjbang.model.service.jwt.JwtServiceImpl;
import com.ssafy.ebangjbang.model.service.user.UserService;


@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController {
	
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	
	@Autowired
	private JwtServiceImpl jwtService;
	
	@Autowired
	private UserService userService;
	
	@PutMapping
	public ResponseEntity<String> modifyUser(@RequestBody UserDto userDto){
				
		try {
			if(userService.modifyUser(userDto)) {
				return new ResponseEntity<String>(SUCCESS, HttpStatus.ACCEPTED);				
			} else {
				return new ResponseEntity<String>(FAIL, HttpStatus.ACCEPTED);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(FAIL, HttpStatus.NO_CONTENT);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable("id") String id){
		try {
			if(userService.deleteUser(id)) {
				return new ResponseEntity<String>(SUCCESS, HttpStatus.ACCEPTED);				
			} else {
				return new ResponseEntity<String>(FAIL, HttpStatus.ACCEPTED);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(FAIL, HttpStatus.NO_CONTENT);
		}
	}
	
	@PostMapping("/signup")
	public ResponseEntity<String> signUp(@RequestBody UserDto userDto){
		
		try {
			if(userService.signUp(userDto)) {
				return new ResponseEntity<String>(SUCCESS, HttpStatus.ACCEPTED);				
			} else {
				return new ResponseEntity<String>(FAIL, HttpStatus.ACCEPTED);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(FAIL, HttpStatus.NO_CONTENT);
		}
		
	}
	
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
			@RequestBody UserDto userDto) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		try {
			UserDto loginUser = userService.login(userDto);
						
			if (loginUser != null) {
				String token = jwtService.create(loginUser.getId(), loginUser.getNickName(), loginUser.getAuth(), "access-token");// key, data, subject
				logger.debug("????????? ???????????? : {}", token);
				resultMap.put("access-token", token);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.ACCEPTED;
			}
		} catch (Exception e) {
			logger.error("????????? ?????? : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	@GetMapping("/info/{userid}")
	public ResponseEntity<Map<String, Object>> getInfo(
			@PathVariable("userid") String userid,
			HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		if (jwtService.isUsable(request.getHeader("access-token"))) {
			logger.info("?????? ????????? ??????!!!");
			try {
				UserDto memberDto = userService.userInfo(userid);
				resultMap.put("userInfo", memberDto);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} catch (Exception e) {
				logger.error("???????????? ?????? : {}", e);
				resultMap.put("message", e.getMessage());
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			logger.error("?????? ????????? ??????!!!");
			resultMap.put("message", FAIL);
			status = HttpStatus.ACCEPTED;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	@GetMapping("/validate/{type}/{value}")
	public ResponseEntity<String> validate(@PathVariable("type") String type, @PathVariable("value") String value) throws Exception{
		
		// true??? ????????? ?????? ??????
		if(!userService.validate(type, value)) {
			// ????????? ????????? (?????? ??????)
			return new ResponseEntity<String>(SUCCESS, HttpStatus.ACCEPTED);			
		} else {
			return new ResponseEntity<String>(FAIL, HttpStatus.ACCEPTED);						
		}
	}
	
	
	// ?????? ????????? ?????? ??? ??????
	public UserDto getUser(String id) throws Exception {
		return userService.userInfo(id);
	}
	
	@GetMapping("/auth/kakao")
	 ResponseEntity<Map<String, Object>> kakaoAuthCallBack(@RequestParam("code") String code) throws Exception {
				
		OAuthToken token = this.getKakaoAccessToken(code);
		
		KakaoProfile profile = getProfile(token.getAccess_token());
						
		String id = profile.getKakao_account().getEmail()+"_"+profile.getId();
		String password = profile.getKakao_account().getEmail()+"_"+profile.getId();
		
		UserDto user = new UserDto();
		
		// id??? ?????? ?????? ????????? ??????
		if(this.getUser(id) != null) {
			// ?????? ????????? ??????
			user.setId(id);
			user.setPassword(password);
		} else {
			// ?????? ??????
			// ???????????? ??????
			user.setId(id);
			user.setPassword(password);
			user.setEmail(profile.getKakao_account().getEmail());
			user.setNickName(profile.getProperties().getNickname());
			user.setType("kakao");

			this.signUp(user);
		}
				
		return this.login(user);
	}
	
	private OAuthToken getKakaoAccessToken(String code) {
		// Post ???????????? key-value ???????????? ???????????? ??????, HttpUrlConnection ?????? ????????? ??????
		RestTemplate rt = new RestTemplate();
		
		// ?????? ??????
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");	// ???????????? ??? ?????? ?????????.
		
		// ?????? ????????????
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", "da06767127c0e1f724138ab2bc91e3dc");
		params.add("redirect_uri", "http://localhost:8081/social/kakao");
		params.add("code", code);
		
		// ????????? ????????? ????????? ??????????????? ??????
		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
		
		// exchange??? HttpEntity??? ??????
		ResponseEntity<String> response = rt.exchange(
				"https://kauth.kakao.com/oauth/token",	// ?????? ?????? ??????
				HttpMethod.POST,	// ?????? ?????? ??????
				kakaoTokenRequest,	// ?????? ?????? request
				String.class		// ?????? ?????? ??????
				);
		
		ObjectMapper objMapper = new ObjectMapper();
		OAuthToken oauthToken = null;
		try {
			oauthToken = objMapper.readValue(response.getBody(), OAuthToken.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return oauthToken;
	}
	
	public KakaoProfile getProfile(String accessToken) {
		KakaoProfile profile = null;
		
		// Access-Token?????? ????????? ????????????(GET or POST)
		// url : https://kapi.kakao.com/v2/user/me
		RestTemplate rt = new RestTemplate();
		
		// ?????? ??????
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");	// ???????????? ??? ?????? ?????????.
		headers.add("Authorization", "Bearer " + accessToken);
		
		// ????????? ????????? ????????? ??????????????? ??????
		HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);
		
		// exchange??? HttpEntity??? ??????
		ResponseEntity<String> response = rt.exchange(
				"https://kapi.kakao.com/v2/user/me",	// ?????? ?????? ??????
				HttpMethod.POST,	// ?????? ?????? ??????
				kakaoProfileRequest,	// ?????? ?????? request
				String.class		// ?????? ?????? ??????
				);
				
		// ????????? ????????? ?????? ?????????
		ObjectMapper objMapper = new ObjectMapper();
		try {
			profile = objMapper.readValue(response.getBody(), KakaoProfile.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return profile;
	}
}
