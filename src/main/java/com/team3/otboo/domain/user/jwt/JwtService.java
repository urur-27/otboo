package com.team3.otboo.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.team3.otboo.domain.user.dto.AccessRefreshToken;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    // 사용할 비밀키
    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.access-token-validity-seconds}")
    private long accessTokenValiditySeconds;
    @Value("${security.jwt.refresh-token-validity-seconds}")
    private long refreshTokenValiditySeconds;

    private final JwtSessionRepository jwtSessionRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    // redis template
    private final RedisTemplate<String, Object> redisTemplate;
    // redis key
    private static final String USER_TOKEN_KEY_PREFIX = "user:";
    private static final String BLACKLIST_KEYU_PREFIX = "blacklist:";

    /*
        <nimbus-jose-jwt 라이브러리 구성 요소 설명>
        JWTClaimSet             -> Payload(내용)
        페이로드 = 토큰에 담고자 하는 정보(JSON)
        JWSHeader               -> Header
        SignedJWT               -> 서명된 내용
        (JWS(JSON Web Signature)을 나타내는 객체)
        MACSigner,              -> 서명에 사용할 펜
        JWSVerifier             -> JWT 서명 검증
        MACVerifier             -> HMAC 알고리즘(HS256)으로 서명된 JWT를 검증
        JWSObject               -> JWT 문자열 -> 헤더, 페이로드, 시그니쳐 로 나누어 변환하는 역할
     */

    // JWT 객체 생성
    private JwtObject generateJwtObject(User user, long tokenValiditySeconds) {

        Instant issueTime = Instant.now();
        Instant expirationTime = issueTime.plus(Duration.ofSeconds(tokenValiditySeconds));

        // JWT Payload 설정
        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())                               // 사용자 고유 식별자로 email
                .claim("name", user.getUsername())                // 필요한 정보만 주입
                .claim("role", user.getRole())
                .claim("userId", user.getId())
                .issueTime(new Date(issueTime.toEpochMilli()))           // 발급 시간
                .expirationTime(new Date(expirationTime.toEpochMilli())) // 만료 시간
                .build();

        // JWT Header 설정
        // 토큰 암호화 및 서명 방식에 대한 데이터 안내(HS256 알고리즘으로 서명됨)
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        // SignedJWT
        // Payload + Header + Sign = JWT 완성
        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimSet);

        try {
            signedJWT.sign(new MACSigner(secret));
        } catch (JOSEException e) {
            log.error(e.getMessage());
            new BusinessException(ErrorCode.SESSION_ERROR, "jwt 서명에 실패했습니다.");
        }

        // 직렬화하여 최종적으로 token 생성
        String token = signedJWT.serialize();
        return new JwtObject(issueTime, expirationTime, user, token);
    }

    @Transactional
    // 사용자의 로그인 성공 시 새로운 JWT 토큰 발급 및 redis에 저장
    public AccessRefreshToken registerJwtSession(User user) {
        // 만들고
        JwtObject accessJwtObject = generateJwtObject(user, accessTokenValiditySeconds);
        JwtObject refreshJwtObject = generateJwtObject(user, refreshTokenValiditySeconds);

        // redis에 토큰 저장
        saveTokensToRedis(user.getId(), accessJwtObject.token(), refreshJwtObject.token());

        log.info("redis에 저장 완료");
        return new AccessRefreshToken(accessJwtObject.token(), refreshJwtObject.token());
    }

    // 사용자의 토큰 2개를 redis hash 자료구조에 저장하는 메서드
    private void saveTokensToRedis(UUID userId, String accessToken, String refreshToken) {
        // redis의 hash 자료구조를 사용하기 위한 hashoperation 객체를 가져온다.
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        // key 생성
        String key = USER_TOKEN_KEY_PREFIX + userId.toString();

        // 저장할 데이터를 map형태로 만든다
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        // redis에 hash 데이터를 저장
        hashOperations.putAll(key, tokenMap);

        // key 자체에 만료 시간을 설정한다.
        // TTL이 지나면 이 key에 해당하는 hash테이터 전체가 삭제된다.
        // TTL은 refresh token의 만료시간으로 설정한다.
        redisTemplate.expire(key, refreshTokenValiditySeconds, TimeUnit.SECONDS);
    }

    // 검증하는 메서드
    public boolean validate(String token) {
        boolean verified;
        try {
            // 서명 검증
            JWSVerifier verifier = new MACVerifier(secret);
            // JWT token(문자열 형태) -> Header, Payload, Signature로 분리
            JWSObject jwsObject = JWSObject.parse(token);
            // 검증해달라고 전달 받은 token을 생성할 때 사용한 secret key -> JWSObject 에 들어있음
            // 서버에서 만들어놓은 secret key를 확인해주는 역할 -> verifier
            verified = jwsObject.verify(verifier);

            // 만료 시간 검증
            if (verified) {
                log.info("서명이 유효함");
                JwtObject jwtObject = parse(token);
                verified = !jwtObject.isExpired();
            }
        } catch (JOSEException | ParseException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "토큰 검증에 실패했습니다.");
        }
        return verified;
    }

    public JwtObject parse(String token) {
        try {
            // token -> JWSObject(헤더, 페이로드, 서명)
            JWSObject jwsObject = JWSObject.parse(token);
            Payload payload = jwsObject.getPayload();
            // Payload -> Map형태의 json 객체로 변환
            Map<String, Object> jsonObject = payload.toJSONObject();

            String email = (String) jsonObject.get("sub");
            String name = (String) jsonObject.get("name");
            Role role = Role.valueOf((String) jsonObject.get("role"));

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            if(!user.getUsername().equals(name) || !user.getRole().equals(role)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "토큰의 사용자 정보가 일치하지 않습니다.");
            }

            return new JwtObject(
                    objectMapper.convertValue(jsonObject.get("iat"), Instant.class),
                    objectMapper.convertValue(jsonObject.get("exp"), Instant.class),
                    user,
                    token
            );
        } catch (ParseException e) {
            log.error("파싱 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "JWT 토큰 파싱에 실패했습니다.");
        }
    }

    // access token이 블랙리스트에 있는지 확인하는 메서드
    public boolean isTokenblacklisted(String accessToken) {
        String key = BLACKLIST_KEYU_PREFIX + accessToken;
        // redis에 해당 키가 존재하면 true 반환
        return redisTemplate.hasKey(key);
    }

    // 로그아웃 처리 (redis에서 삭제 및 블랙리스트에 추가)
    @Transactional
    public void logout(String token){
        // 사용자 Id 추출
        JwtObject parsedToken = parse(token);
        UUID userId = parsedToken.user().getId();

        logout(userId);
    }

    // Id로 로그아웃할 경우
    @Transactional
    public void logout(UUID userId) {
        // 실제로 존재하는지 확인
        if(!userLoggedIn(userId)){
            log.warn("로그아웃 상태 또는 존재하지 않는 사용자입니다.");
            return;
        }

        // redis에서 토큰 삭제 = refresh token 무효화
        String userTokenKey = USER_TOKEN_KEY_PREFIX + userId.toString();

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String accessToken = hashOps.get(userTokenKey, "accessToken");

        if(accessToken != null){
            // 만료시간 계산
            long remainingTime = parse(accessToken).expirationTime().toEpochMilli() - System.currentTimeMillis();

            // access token을 블랙리스트에 추가하여 즉시 사용 불가능하게 만든다.
            // 토큰의 남은 유효시간을 계산하여 TTL로 설정 -> 자동삭제되도록 하기 위함
            if(remainingTime > 0){
                String blacklistKey = BLACKLIST_KEYU_PREFIX + accessToken;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
            }
        }
        redisTemplate.delete(userTokenKey);
        log.info("로그아웃 처리 및 블랙리스트 추가 완료, access token: {}", accessToken);
    }

    // access, refresh token 재발급 메서드 (refresh api 요청)
    @Transactional
    public AccessRefreshToken reIssueToken(String refreshToken) {
        // refresh token의 서명과 만료시간 1차 검증
        if(!validate(refreshToken)){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 유효하지 않습니다.");
        }

        // redis에 저장되어있는 refresh token과 일치하는지 2차 검증
        User user = parse(refreshToken).user();
        String userTokenKey = USER_TOKEN_KEY_PREFIX + user.getId().toString();
        // <key, access, refresh>
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        // "accessToken" : ~~,
        // "refreshToken" : ~~
        // <access, refresh>
        Map<String, String> storedTokens = hashOperations.entries(userTokenKey);

        // 만약 redis의 refresh token과 다르다면 강제 로그아웃 처리(redis에서 삭제)
        String storedRefreshToken = storedTokens.get("refreshToken");
        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            redisTemplate.delete(userTokenKey);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 일치하지 않습니다.");
        }

        // 기존의 Access token을 블랙리스트에 등록하여 사용하지 못하도록 한다.
        String oldAccessToken = storedTokens.get("accessToken");
        if(oldAccessToken != null){
            String blacklistKey = BLACKLIST_KEYU_PREFIX + oldAccessToken;
            long remainingTime = parse(oldAccessToken).expirationTime().toEpochMilli() - System.currentTimeMillis();
            if(remainingTime > 0){
                // redis 블랙리스트에 저장하면서 해당 데이터가 자동으로 사라진 시간 TTL 설정
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
            }
        }

        // 새로운 access token과 refresh token을 생성
        JwtObject newAccessJwt = generateJwtObject(user, accessTokenValiditySeconds);
        JwtObject newRefreshJwt = generateJwtObject(user, refreshTokenValiditySeconds);

        // 새로 생성된 토큰들을 redis에 다시 저장
        saveTokensToRedis(user.getId(), newAccessJwt.token(), newRefreshJwt.token());

        return new AccessRefreshToken(newAccessJwt.token(), newRefreshJwt.token());
    }

    // key 존재 여부 체크
    @Transactional
    public boolean userLoggedIn(UUID userId){
        String userTokenKey = USER_TOKEN_KEY_PREFIX + userId.toString();
        return redisTemplate.hasKey(userTokenKey);
    }

    // access token 반환해주는 메서드
    @Transactional
    public String getAccessToken(String refreshToken){
        String userKey = USER_TOKEN_KEY_PREFIX + parse(refreshToken).user().getId().toString();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> tokens = hashOperations.entries(userKey);
        String accessToken = tokens.get("accessToken");
        return accessToken;
    }
}