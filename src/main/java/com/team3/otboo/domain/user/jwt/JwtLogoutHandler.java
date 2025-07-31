package com.team3.otboo.domain.user.jwt;

public class JwtLogoutHandler {
    // 사용자가 로그아웃을 요청할 때, 현재 요청의 Access Token을 무효화한다.
    // 요청 헹더에서 Access Token을 꺼내 블랙리스트에 추가
    // 블랙리스트에 저장할 때는 토큰의 남은 유효기간만큼만 저장하여 데이터가 불필요하게 쌓이지 않게 한다.
    // 서버에 저장된 refresh token도 함께 삭제한다.

}
