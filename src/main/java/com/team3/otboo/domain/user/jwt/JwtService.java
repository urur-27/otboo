package com.team3.otboo.domain.user.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.team3.otboo.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    // 사용할 비밀키
    @Value("${security.jwt.secret}")
    private String secret;

    /*
        JWTObject               -> 정보를 담아놓은 객체
        (발급 시간, 만료 시간, 사용자 정보(UserDto), 토큰)

        <nimbus-jose-jwt> 라이브러리 구성 요소 설명
        JWTClaimSet             -> Payload(내용)
        JWSHeader               -> Header
        SignedJWT               -> 서명된 내용
        (JWS(JSON Web Signature)을 나타내는 객체)
        MACSigner,              -> 서명에 사용할 펜
        MACVerifier             -> 서명 감별
     */

    // 지정된 사용자 정보와 유효 시간을 바탕으로 JWT 객체를 생성
    private JwtObject generateJwtObject(
            // JWT payload에 담을 사용자 정보
            UserDto userDto,
            // token의 유효 시간
            long tokenValiditySeconds
    ){
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
            // error 처리?
        }

        // 직렬화하여 최종적으로 token 생성
        String token = signedJWT.serialize();
        return new JwtObject(issueTime, expirationTime, userDto, token);
    }

    @Transactional
    public JwtSession registerJwtSession(UserDto userDto) {
        // Access, Refresh Token 생성
        JwtObject accessJwtObject = generateJwtObject(userDto, accessTokenValiditySeconds);
        JwtObject refreshJwtObject = generateJwtObject(userDto, refreshTokenValiditySeconds);

        // 생성된 토큰 정보를 바탕으로 Session 객체 생성

        // 생성된 세션을 DB에 저장하여 Refresh Token을 관리
        // todo: jwtSessionRepository.save
    }

}
