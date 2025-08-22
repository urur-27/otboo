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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
    // 사용자의 로그인 성공 시 새로운 JWT 세션을 등록
    public AccessRefreshToken registerJwtSession(User user) {

        JwtObject accessJwtObject = generateJwtObject(user, accessTokenValiditySeconds);
        JwtObject refreshJwtObject = generateJwtObject(user, refreshTokenValiditySeconds);

        JwtSession jwtSession = new JwtSession(
                user.getId(),
                refreshJwtObject.token(),
                refreshJwtObject.expirationTime()
        );

        jwtSessionRepository.save(jwtSession);
        log.info("session 저장 완료");
        return new AccessRefreshToken(accessJwtObject.token(), refreshJwtObject.token());
    }

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

    private void invalidate(JwtSession jwtSession) {
        jwtSessionRepository.delete(jwtSession);
        log.debug("session 삭제 완료");
    }

    @Transactional
    public void invalidateJwtSession(String refreshToken) {
        log.info("존재할 경우 삭제 시작");
        jwtSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(this::invalidate);
    }

    @Transactional
    public void invalidateJwtSession(UUID userId) {
        jwtSessionRepository.findByUserId(userId)
                .ifPresent(this::invalidate);
    }

    // access token 기간 만료 및 me 조회 요청에 따른 재발급
    public AccessRefreshToken meJwtRefreshToken(String refreshToken) {
        if (!validate(refreshToken)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 유효하지 않습니다.");
        }
        JwtSession session = jwtSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰을 찾을 수 없습니다."));
        if(session.isExpired()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 이미 만료되었습니다.");
        }

        User user = parse(refreshToken).user();
        JwtObject accessJwtObject = generateJwtObject(user, accessTokenValiditySeconds);

        return new AccessRefreshToken(accessJwtObject.token(), refreshToken);
    }

    // refresh token 기간 만료 및 재발급
    @Transactional
    public AccessRefreshToken refreshJwtSession(String refreshToken) {
        if (!validate(refreshToken)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 유효하지 않습니다.");
        }
        JwtSession session = jwtSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰을 찾을 수 없습니다."));

        User user = parse(refreshToken).user();

        JwtObject accessJwtObject = generateJwtObject(user, accessTokenValiditySeconds);
        JwtObject refreshJwtObject = generateJwtObject(user, refreshTokenValiditySeconds);

        session.update(
                user.getId(),
                refreshJwtObject.token(),
                refreshJwtObject.expirationTime()
        );
        return new AccessRefreshToken(accessJwtObject.token(), refreshJwtObject.token());
    }
}