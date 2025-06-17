package com.example.springsocial.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.springsocial.cache.AuthStateRedisCache;
import com.example.springsocial.domain.ApiResult;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.CustomUserDetailsService;
import com.example.springsocial.security.TokenProvider;
import com.example.springsocial.service.UserService;
import com.xkcoding.http.config.HttpConfig;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.scope.*;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.utils.AuthScopeUtils;
import me.zhyd.oauth.utils.AuthStateUtils;
import me.zhyd.oauth.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

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
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.clientSecret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirectUri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.clientId}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.clientSecret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirectUri}")
    private String facebookRedirectUri;

    @Autowired
    private AuthStateRedisCache stateRedisCache;

    @Autowired
    private UserService userService;

    /**
     * oauth平台中配置的授权回调地址，以本项目为例，在创建github授权应用时的回调地址应为：http://127.0.0.1:8443/oauth/callback/github
     */
    @RequestMapping("callback/{source}")
    public ModelAndView login(@PathVariable("source") String source, AuthCallback callback, HttpServletRequest request) {
        log.info("进入callback：" + source + " callback params：" + JSONObject.toJSONString(callback));
        AuthRequest authRequest = getAuthRequest(source);
        me.zhyd.oauth.model.AuthResponse<AuthUser> response = authRequest.login(callback);
        log.info(JSONObject.toJSONString(response));

        if (response.ok()) {
            userService.save(response.getData());
            return new ModelAndView("redirect:/users");
        }

        Map<String, Object> map = new HashMap<>(1);
        map.put("errorMsg", response.getMsg());

        return new ModelAndView("error", map);
    }

    @RequestMapping("/render/{source}")
    @ResponseBody
    public void renderAuth(@PathVariable("source") String source, HttpServletResponse response) throws IOException {
        log.info("进入render：" + source);
        AuthRequest authRequest = getAuthRequest(source);
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        log.info(authorizeUrl);
        response.sendRedirect(authorizeUrl);
    }

    @RequestMapping("redirect/{type}")
    public ApiResult<?> authenticateUser(@PathVariable String type, AuthCallback callback, HttpServletRequest request) {
        ApiResult<String> apiResult = ApiResult.newInstance();
        log.info("redirect type：" + type + " callback params：" + JSONObject.toJSONString(callback));
        AuthRequest authRequest = getAuthRequest(type);
        me.zhyd.oauth.model.AuthResponse<AuthUser> response = authRequest.login(callback);
        log.info("response:{}", JSONObject.toJSONString(response));
        if (response.ok()) {
            String email = "";
            String username = "";
            if ("google".equals(type)) {
                email = response.getData().getEmail();
                username = response.getData().getUsername();
            } else if ("facebook".equals(type)) {
                email = response.getData().getEmail();
                username = response.getData().getUsername();
            }
            userService.save(response.getData());
            if (StringUtils.isNotEmpty(email) && userRepository.existsByEmail(email)) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // 2. 创建无密码的认证对象（已认证状态）
                List<GrantedAuthority> authorities = userDetails.getAuthorities().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList());
//                Optional<User> byEmail = userRepository.findByEmail(email);
//                User user = byEmail.get();
                // 使用无密码构造器（需传入authorities标识已认证）
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        email,   // 或自定义的principal
                        null,               // 密码为null（跳过验证）
                        authorities        // 权限列表
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = tokenProvider.createToken(authentication);
                return apiResult.succeed().data(token);
            } else if (StringUtils.isNotEmpty(email)) {
                //注册登陆
                User user = new User();
                user.setName(username);
                user.setEmail(email);
                user.setPassword("123456");
                user.setProvider(AuthProvider.local);
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                User result = userRepository.save(user);
                // 2. 创建无密码的认证对象（已认证状态）
                List<GrantedAuthority> authorities = new ArrayList<>();
//                Optional<User> byEmail = userRepository.findByEmail(email);
//                User user = byEmail.get();
                // 使用无密码构造器（需传入authorities标识已认证）
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        email,   // 或自定义的principal
                        null,               // 密码为null（跳过验证）
                        authorities        // 权限列表
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = tokenProvider.createToken(authentication);
                return apiResult.succeed().data(token);
            }
        }
        return apiResult.failed().data(type);
    }

    @RequestMapping("test/{email}")
    public ApiResult<?> authenticateUser(@PathVariable String email, HttpServletRequest request) {
        ApiResult<String> apiResult = ApiResult.newInstance();
        log.info("进入test：" + email);
        if (userRepository.existsByEmail(email)) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // 2. 创建无密码的认证对象（已认证状态）
            List<GrantedAuthority> authorities = userDetails.getAuthorities().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                    .collect(Collectors.toList());

//                Optional<User> byEmail = userRepository.findByEmail(email);
//                User user = byEmail.get();
            // 使用无密码构造器（需传入authorities标识已认证）
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    email,   // 或自定义的principal
                    null,               // 密码为null（跳过验证）
                    authorities        // 权限列表
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = tokenProvider.createToken(authentication);
            return apiResult.succeed().data(token);
        }
        return apiResult.succeed().data(email);
    }

    /**
     * 根据具体的授权来源，获取授权请求工具类
     *
     * @param source
     * @return
     */
    private AuthRequest getAuthRequest(String source) {
        AuthRequest authRequest = null;
        switch (source.toLowerCase()) {
            case "dingtalk":
                authRequest = new AuthDingTalkRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/dingtalk")
                        .build());
                break;
            case "baidu":
                authRequest = new AuthBaiduRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/baidu")
                        .scopes(Arrays.asList(
                                AuthBaiduScope.BASIC.getScope(),
                                AuthBaiduScope.SUPER_MSG.getScope(),
                                AuthBaiduScope.NETDISK.getScope()
                        ))
//                        .clientId("")
//                        .clientSecret("")
//                        .redirectUri("http://localhost:9001/oauth/baidu/callback")
                        .build());
                break;
            case "github":
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/github")
                        .scopes(AuthScopeUtils.getScopes(AuthGithubScope.values()))
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build(), stateRedisCache);
                break;
            case "gitee":
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://127.0.0.1:8443/oauth/callback/gitee")
                        .scopes(AuthScopeUtils.getScopes(AuthGiteeScope.values()))
                        .build(), stateRedisCache);
                break;
            case "weibo":
                authRequest = new AuthWeiboRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/weibo")
                        .scopes(Arrays.asList(
                                AuthWeiboScope.EMAIL.getScope(),
                                AuthWeiboScope.FRIENDSHIPS_GROUPS_READ.getScope(),
                                AuthWeiboScope.STATUSES_TO_ME_READ.getScope()
                        ))
                        .build());
                break;
            case "coding":
                authRequest = new AuthCodingRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/coding")
                        .domainPrefix("")
                        .scopes(Arrays.asList(
                                AuthCodingScope.USER.getScope(),
                                AuthCodingScope.USER_EMAIL.getScope(),
                                AuthCodingScope.USER_PHONE.getScope()
                        ))
                        .build());
                break;
            case "oschina":
                authRequest = new AuthOschinaRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/oschina")
                        .build());
                break;
            case "alipay":
                // 支付宝在创建回调地址时，不允许使用localhost或者127.0.0.1，所以这儿的回调地址使用的局域网内的ip
                authRequest = new AuthAlipayRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .alipayPublicKey("")
                        .redirectUri("https://www.zhyd.me/oauth/callback/alipay")
                        .build());
                break;
            case "qq":
                authRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/qq")
                        .build());
                break;
            case "wechat_open":
                authRequest = new AuthWeChatOpenRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://www.zhyd.me/oauth/callback/wechat")
                        .build());
                break;
            case "csdn":
                authRequest = new AuthCsdnRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/csdn")
                        .build());
                break;
            case "taobao":
                authRequest = new AuthTaobaoRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/taobao")
                        .build());
                break;
            case "google":
                log.info("googleClientId: {}", googleClientId);
                log.info("googleClientSecret: {}", googleClientSecret);
                log.info("googleRedirectUri: {}", googleRedirectUri);
                authRequest = new AuthGoogleRequest(AuthConfig.builder()
                        .clientId(googleClientId)
                        .clientSecret(googleClientSecret)
                        .redirectUri(googleRedirectUri)
//                        .redirectUri("http://localhost:8443/oauth/callback/google")
                        .scopes(AuthScopeUtils.getScopes(AuthGoogleScope.USER_EMAIL, AuthGoogleScope.USER_PROFILE, AuthGoogleScope.USER_OPENID))
                        .build());
                break;
            case "facebook":
                log.info("facebookClientId: {}", facebookClientId);
                log.info("facebookClientSecret: {}", facebookClientSecret);
                log.info("facebookRedirectUri: {}", facebookRedirectUri);
                authRequest = new AuthFacebookRequest(AuthConfig.builder()
                        .clientId(facebookClientId)
                        .clientSecret(facebookClientSecret)
                        .redirectUri(facebookRedirectUri)
                        .scopes(AuthScopeUtils.getScopes(AuthFacebookScope.EMAIL))
                        .build());
                break;
            case "douyin":
                authRequest = new AuthDouyinRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/douyin")
                        .build());
                break;
            case "linkedin":
                authRequest = new AuthLinkedinRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/linkedin")
                        .scopes(null)
                        .build());
                break;
            case "microsoft":
                authRequest = new AuthMicrosoftRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/microsoft")
                        .scopes(Arrays.asList(
                                AuthMicrosoftScope.USER_READ.getScope(),
                                AuthMicrosoftScope.USER_READWRITE.getScope(),
                                AuthMicrosoftScope.USER_READBASIC_ALL.getScope(),
                                AuthMicrosoftScope.USER_READ_ALL.getScope(),
                                AuthMicrosoftScope.USER_READWRITE_ALL.getScope(),
                                AuthMicrosoftScope.USER_INVITE_ALL.getScope(),
                                AuthMicrosoftScope.USER_EXPORT_ALL.getScope(),
                                AuthMicrosoftScope.USER_MANAGEIDENTITIES_ALL.getScope(),
                                AuthMicrosoftScope.FILES_READ.getScope()
                        ))
                        .build());
                break;
            case "mi":
                authRequest = new AuthMiRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/mi")
                        .build());
                break;
            case "toutiao":
                authRequest = new AuthToutiaoRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/toutiao")
                        .build());
                break;
            case "teambition":
                authRequest = new AuthTeambitionRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://127.0.0.1:8443/oauth/callback/teambition")
                        .build());
                break;
            case "pinterest":
                authRequest = new AuthPinterestRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("https://eadmin.innodev.com.cn/oauth/callback/pinterest")
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build());
                break;
            case "renren":
                authRequest = new AuthRenrenRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://127.0.0.1:8443/oauth/callback/teambition")
                        .build());
                break;
            case "stack_overflow":
                authRequest = new AuthStackOverflowRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("((")
                        .redirectUri("http://localhost:8443/oauth/callback/stack_overflow")
                        .stackOverflowKey("")
                        .build());
                break;
            case "huawei":
                authRequest = new AuthHuaweiRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://127.0.0.1:8443/oauth/callback/huawei")
                        .scopes(Arrays.asList(
                                AuthHuaweiScope.BASE_PROFILE.getScope(),
                                AuthHuaweiScope.MOBILE_NUMBER.getScope(),
                                AuthHuaweiScope.ACCOUNTLIST.getScope(),
                                AuthHuaweiScope.SCOPE_DRIVE_FILE.getScope(),
                                AuthHuaweiScope.SCOPE_DRIVE_APPDATA.getScope()
                        ))
                        .build());
                break;
            case "wechat_enterprise":
                authRequest = new AuthWeChatEnterpriseQrcodeRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://justauth.cn/oauth/callback/wechat_enterprise")
                        .agentId("1000003")
                        .build());
                break;
            case "kujiale":
                authRequest = new AuthKujialeRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/kujiale")
                        .build());
                break;
            case "gitlab":
                authRequest = new AuthGitlabRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/gitlab")
                        .scopes(AuthScopeUtils.getScopes(AuthGitlabScope.values()))
                        .build());
                break;
            case "meituan":
                authRequest = new AuthMeituanRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/meituan")
                        .build());
                break;
            case "eleme":
                authRequest = new AuthElemeRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://dblog-web.zhyd.me/oauth/callback/eleme")
                        .build());
                break;
            case "mygitlab":
                authRequest = new AuthGitlabRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://127.0.0.1:8443/oauth/callback/mygitlab")
                        .build());
                break;
            case "twitter":
                authRequest = new AuthTwitterRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("https://threelogin.31huiyi.com/oauth/callback/twitter")
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build());
                break;
            case "wechat_mp":
                authRequest = new AuthWeChatMpRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("")
                        .build());
                break;
            case "aliyun":
                authRequest = new AuthAliyunRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/aliyun")
                        .build());
                break;
            case "xmly":
                authRequest = new AuthXmlyRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/xmly")
                        .build());
                break;
            case "feishu":
                authRequest = new AuthFeishuRequest(AuthConfig.builder()
                        .clientId("")
                        .clientSecret("")
                        .redirectUri("http://localhost:8443/oauth/callback/feishu")
                        .build());
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException("未获取到有效的Auth配置");
        }
        return authRequest;
    }
}
