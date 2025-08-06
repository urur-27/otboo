package com.team3.otboo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // Common Errors (Cxxx)
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "요청 값의 타입이 올바르지 않습니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),
  INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "C006", "지원하지 않는 정렬 기준입니다."),

  // User Errors (Uxxx)
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
  PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "해당 프로필을 찾을 수 없습니다."),

  // Roles Errors (Rxxx)
  ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "해당 권한은 존재하지 않습니다."),

  // Clothing Errors
  CLOTHING_NOT_FOUND(HttpStatus.NOT_FOUND, "CL001", "해당 의상을 찾을 수 없습니다."),

  // Clothing Mapper Errors
  CLOTHING_MAPPER_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CL_M001", "DTO 변환에 실패했습니다."),

  // Clothing Extraction Errors
  CLOTHING_EXTACTION_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "CE001", "의상 정보 HTML 파싱 실패"),

  // Attribute Errors
  ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTR001", "해당 속성을 찾을 수 없습니다."),
  ATTRIBUTE_NAME_DUPLICATED(HttpStatus.CONFLICT, "ATTR002", "이미 존재하는 속성명입니다."),
  ATTRIBUTE_OPTION_EMPTY(HttpStatus.BAD_REQUEST, "ATTR003", "옵션 값은 최소 1개 이상이어야 합니다."),

  // AttributeOption Errors
  ATTRIBUTEOPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "AO001", "해당 속성 값을 찾을 수 없습니다."),

  // Image Errors
  IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG001", "이미지 업로드에 실패했습니다."),
  IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG002", "이미지 삭제에 실패했습니다."),
  INVALID_IMAGE_PATH(HttpStatus.FORBIDDEN, "IMG003", "유효하지 않은 이미지 경로입니다."),

  // LLM Errors
  LLM_JSON_NOT_FOUND(HttpStatus.BAD_REQUEST, "LLM001", "응답에 JSON이 포함되어 있지 않습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

}
