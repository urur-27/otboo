package com.team3.otboo.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.team3.otboo.domain.user.dto.AccessRefreshToken;
import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import jakarta.persistence.Access;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";

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
    private final UserMapper userMapper;

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

    // 사용자 정보와 유효 시간을 바탕으로 JWT 객체를 생성
    private JwtObject generateJwtObject(
            // JWT payload에 담을 사용자 정보
            UserDto userDto,
            // token의 유효 시간
            long tokenValiditySeconds
    ) {
        // 토큰 발급 시간, 만료 시간 설정
        Instant issueTime = Instant.now();
        Instant expirationTime = issueTime.plus(Duration.ofSeconds(tokenValiditySeconds));

        // JWT Payload 설정(보낼 내용들)
        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .subject(userDto.name())                                  // 사용자 고유 식별자로 이름
                .claim("userDto", userDto)                          // 사용자 정보 전체
                .issueTime(new Date(issueTime.toEpochMilli()))            // 발급 시간
                .expirationTime(new Date(expirationTime.toEpochMilli()))  // 만료 시간
                .build();

        // JWT Header 설정
        // 토큰 암호화 및 서명 방식에 대한 데이터 안내(HS256 알고리즘으로 서명됨)
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        // SignedJWT
        // Payload + Header + Sign = JWT 완성
        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimSet);

        // secret key를 이용해 token에 서명
        try {
            // token에 서명
            signedJWT.sign(new MACSigner(secret));
        } catch (JOSEException e) {
            log.error(e.getMessage());
            new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "jwt 서명에 실패했습니다.");
        }

        // 직렬화하여 최종적으로 token 생성
        String token = signedJWT.serialize();
        return new JwtObject(issueTime, expirationTime, userDto, token);
    }

    @Transactional
    // 사용자의 로그인 성공 시 새로운 JWT 세션을 등록
    // Access, Refresh Token을 모두 생성하고 DB에 저장한다.
    public AccessRefreshToken registerJwtSession(UserDto userDto) {
        // Access, Refresh Token 생성
        JwtObject accessJwtObject = generateJwtObject(userDto, accessTokenValiditySeconds);
        JwtObject refreshJwtObject = generateJwtObject(userDto, refreshTokenValiditySeconds);

        JwtSession jwtSession = new JwtSession(
                userDto.id(),
                refreshJwtObject.token(),
                accessJwtObject.expirationTime()
        );
        // DB에 저장하여 Refresh Token을 관리
        jwtSessionRepository.save(jwtSession);

        return new AccessRefreshToken(accessJwtObject.token(), refreshJwtObject.token());
    }

    // 주어진 토큰의 유효성을 종합적으로 검증
    // JwtAuthenticationFilter 에서 모든 요청마다 호출
    public boolean validate(String token) {
        boolean verified;

        try {
            // 서명 검증
            // verifier의 역할은 토큰 생성 시 동일한 Secrete Key를 활용하여 생성하는지 확인하는 역할
            JWSVerifier verifier = new MACVerifier(secret);
            // JWT token(문자열 형태) -> Header, Payload, Signature로 분리
            JWSObject jwsObject = JWSObject.parse(token);
            // 검증해달라고 전달 받은 token을 생성할 때 사용한 secret key -> JWSObject 에 들어있음
            // 서버에서 만들어놓은 secret key를 확인해주는 역할 -> verifier
            // 이 두개가 서로 같다면 서명검증 성공, 다르다면 변조된 token이라는 뜻
            verified = jwsObject.verify(verifier);

            // 만료 시간 검증 (서명이 유효한 경우)
            if (verified) {
                // token을 분석하여 JwtObject를 생성
                JwtObject jwtObject = parse(token);
                // 만료 시간을 넘었는지 확인
                verified = !jwtObject.isExpired();
            }
        } catch (JOSEException | ParseException e) {
            // parsing 또는 서명 검증 실패 시
            log.error("토큰 검증 실패: {}", e.getMessage());
            verified = false;
        }

        return verified;
    }

    // token -> JwtObject 매핑 (token 분해 후 넣어준다.)
    public JwtObject parse(String token) {
        try {
            // token -> JWSObject(헤더, 페이로드, 서명)
            // 파싱 실패 시 -> ParseException 발생
            JWSObject jwsObject = JWSObject.parse(token);
            Payload payload = jwsObject.getPayload();
            // Payload -> Map형태의 json 객체로 변환
            // 페이로드를 key-value 형태로 쌍으로 접근가능
            Map<String, Object> jsonObject = payload.toJSONObject();
            // iat, exp는 정해져있는 타임스탬프
            return new JwtObject(
                    objectMapper.convertValue(jsonObject.get("iat"), Instant.class),
                    objectMapper.convertValue(jsonObject.get("exp"), Instant.class),
                    objectMapper.convertValue(jsonObject.get("userDto"), UserDto.class),
                    token
            );
        } catch (ParseException e) {
            log.error("파싱 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "JWT 토큰 파싱에 실패했습니다.");
        }
    }

    // JWT Session을 무효화 하는 메서드
    private void invalidate(JwtSession jwtSession) {
        // DB에 저장된 Refresh Token 세션 정보 삭제
        jwtSessionRepository.delete(jwtSession);

        // Access Token의 유효기간이 아직 남아있다면 즉시 무효화시키기 위해 블랙리스트에 추가
        // 하지만 redis에 access token을 저장해놓고 관리할 정도로 그렇게 중요한 정보들을 다루는 서비스를 구현하는게 아니기 때문에
        // access token의 만료시간을 짧게 가져가는 방향으로 구현하는게 오히려 효율적일 수 있다는 판단
    }

    // 사용자의 Refresh Token을 기반으로 해당 세션을 무효화
    // 일반적인 로그아웃
    @Transactional
    public void invalidateJwtSession(String refreshToken) {
        // session을 찾아 invalidate 메서드 호출
        jwtSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(this::invalidate);
    }

    // 사용자 ID를 기반으로 사용자의 모든 세션을 무효화
    // 관리자에 의한 강제 로그아웃
    @Transactional
    public void invalidateJwtSession(UUID userId) {
        jwtSessionRepository.findByUserId(userId)
                .ifPresent(this::invalidate);
    }

    public List<JwtSession> getActiveJwtSessions() {
        return jwtSessionRepository.findAllByExpirationTimeAfter(Instant.now());
    }

    public JwtSession getJwtSession(String refreshToken) {
        return jwtSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰을 찾을 수 없습니다."));
    }

    // access token 기간 만료 및 me 조회 요청에 따른 재발급
    public AccessRefreshToken meJwtRefreshToken(String refreshToken) {
        // 토큰 유효성 검사
        if (!validate(refreshToken)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 유효하지 않습니다.");
        }

        // 리프레시 토큰으로 세션 조회
        JwtSession session = jwtSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰을 찾을 수 없습니다."));

        // refresh token이 만료되었다면
        if(session.isExpired()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 이미 만료되었습니다.");
        }

        // 토큰에서 사용자 ID 파싱 및 사용자 조회
        UUID userId = parse(refreshToken).userDto().id();
        UserDto userDto = userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("ID: " + userId));

        // 새로운 JWT 객체 생성
        JwtObject accessJwtObject = generateJwtObject(userDto, accessTokenValiditySeconds);

        return new AccessRefreshToken(accessJwtObject.token(), refreshToken);
    }

    // refresh token 기간 만료에 따른 재발급
    @Transactional
    public AccessRefreshToken refreshJwtSession(String refreshToken) {
        // 토큰 유효성 검사
        if (!validate(refreshToken)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 유효하지 않습니다.");
        }

        // 리프레시 토큰으로 세션 조회
        JwtSession session = jwtSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰을 찾을 수 없습니다."));

        // 토큰에서 사용자 ID 파싱 및 사용자 조회
        UUID userId = parse(refreshToken).userDto().id();
        UserDto userDto = userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("ID: " + userId));

        // 새로운 JWT 객체 생성
        JwtObject accessJwtObject = generateJwtObject(userDto, accessTokenValiditySeconds);
        JwtObject refreshJwtObject = generateJwtObject(userDto, refreshTokenValiditySeconds);

        session.update(
                userId,
                refreshJwtObject.token(),
                refreshJwtObject.expirationTime()
        );

        return new AccessRefreshToken(accessJwtObject.token(), refreshJwtObject.token());
    }
}