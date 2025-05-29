package com.example.springsocial.controller;

import com.example.springsocial.model.AccessToken;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import com.example.springsocial.model.UserProfile;
import com.example.springsocial.payload.ApiResponse;
import com.example.springsocial.payload.AuthResponse;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.CustomUserDetailsService;
import com.example.springsocial.security.TokenProvider;
import com.example.springsocial.util.HttpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/oauth2/")
public class AuthRedirectController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${google.userinfo}")
    private String googleUserInfo;

    @Value("${google.token}")
    private String googleToken;

    @Value("${spring.security.oauth2.client.registration.google.clientId}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.clientSecret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirectUri}")
    private String redirectUri;

    @GetMapping("redirect/{type}")
    public ResponseEntity<?> authenticateUser(@PathVariable String type, String code) {
        log.info("type: {}; code: {}", type, code);
        if ("google".equals(type)) {
            // https://www.tmmtmm.online/api/oauth2/redirect/google?state=4j3h6pgxWbN_YdkiaXnrQRq0Bouk4AU8UJkbdmygbLc%3D&code=4%2F0AUJR-x7rAgGSWnewIrBlMr8eqNJ3krnTpHI0ivVWnM8e58wR3nk6we1uHdRLVRHO_r3WeA&scope=email+profile+openid+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&authuser=0&prompt=none
            // code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7&
            //client_id=your_client_id&
            //client_secret=your_client_secret&
            //redirect_uri=https%3A//oauth2.example.com/code&
            //grant_type=authorization_code
            redirectUri = "https://www.tmmtmm.online/api/oauth2/redirect/" + type;
            String param = "code=" + code + "&client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + redirectUri + "&grant_type=authorization_code";
            String s = HttpUtils.sendPostNew(googleToken, param);
            ObjectMapper mapper = new ObjectMapper();
            UserProfile userProfile = null;
            AccessToken accessToken = null;
            try {
                accessToken = mapper.readValue(s, AccessToken.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException: {}", e.getMessage());
            }
            String accessToken1 = accessToken.getAccessToken();
            String userParam = googleUserInfo + accessToken1;
            String s1 = HttpUtils.sendGet(userParam);

            try {
                userProfile = mapper.readValue(s1, UserProfile.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException: {}", e.getMessage());
            }
            String email = userProfile.getEmail();
            if (userRepository.existsByEmail(email)) {
                Optional<User> byEmail = userRepository.findByEmail(email);
                User user = byEmail.get();
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                "yangcc_4837"
                        )
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                String token = tokenProvider.createToken(authentication);
                return ResponseEntity.ok(new AuthResponse(token));
            } else {
                //注册登陆
                User user = new User();
                user.setName(userProfile.getName());
                user.setEmail(userProfile.getEmail());
                user.setPassword("123456");
                user.setProvider(AuthProvider.local);

                user.setPassword(passwordEncoder.encode(user.getPassword()));

                User result = userRepository.save(user);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentContextPath().path("/user/me")
                        .buildAndExpand(result.getId()).toUri();

                return ResponseEntity.created(location)
                        .body(new ApiResponse(true, "User registered successfully@"));
            }
        }
        return ResponseEntity.ok(type);
    }

    public static void main(String[] args) throws JsonProcessingException {
        String jsonString = "{\"id\":\"110811289260314625756\",\"email\":\"yangccchqw@gmail.com\",\"verified_email\":true,\"name\":\"杨存草\",\"given_name\":\"存草\",\"family_name\":\"杨\",\"picture\":\"https://lh3.googleusercontent.com/a/ACg8ocLXxJe9RmNv2ruCOJrIfitH0NuRRHu94TR6UOolzCFUnLQf6w=s96-c\"}";

        ObjectMapper mapper = new ObjectMapper();
        UserProfile user = mapper.readValue(jsonString, UserProfile.class);
        System.out.println(user.getEmail());

    }
}
