package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.analyzers.HtmlExtractor;
import com.team3.otboo.domain.clothing.analyzers.VisionAnalyzer;
import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.mapper.AttributeMapper;
import com.team3.otboo.domain.clothing.mapper.ClothingDtoMerger;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.clothing.entity.Attribute;

@ExtendWith(MockitoExtension.class)
class ClothingExtractionServiceImplTest {

    @Mock
    HtmlExtractor htmlExtractor;
    @Mock
    VisionAnalyzer visionAnalyzer;
    @Mock
    ClothingDtoMerger clothingDtoMerger;
    @Mock
    AttributeRepository attributeRepository;
    @Mock
    AttributeMapper attributeMapper;

    @InjectMocks
    ClothingExtractionServiceImpl sut;

    @Test
    @DisplayName("extractFromUrl - HTML/DB → defNames & optionsByDef 구성 → analyze → map → merge")
    void extractFromUrl_success_buildsPromptAndMerges() {
        // given
        String url = "https://store.example/item/42";
        HtmlExtractionResult html = new HtmlExtractionResult(
                "https://img.example/42.jpg",
                "예쁜 셔츠",
                "상세 설명..."
        );
        when(htmlExtractor.extract(url)).thenReturn(html);

        // Attribute/Option 목 구성 (공백/널/정렬/limit 테스트 포함)
        Attribute color = mockAttr("색상", Arrays.asList(" 빨강", "하양", null, "", "검정", "  ", " 파랑 "));
        // 65개 만들어 limit(60) 테스트 (사이즈: 랜덤 순서)
        List<String> sizes = IntStream.rangeClosed(1, 65).mapToObj(i -> "사이즈" + i).collect(
                Collectors.toList());
        Collections.shuffle(sizes, new Random(1234));
        Attribute size = mockAttr("사이즈", sizes);

        when(attributeRepository.findAllWithOptions()).thenReturn(List.of(color, size));

        VisionAnalysisResult vision = mock(VisionAnalysisResult.class);
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenReturn(vision);

        List<ClothesAttributeWithDefDto> mapped = List.of(mock(ClothesAttributeWithDefDto.class));
        when(attributeMapper.mapFromVision(vision)).thenReturn(mapped);

        ClothesDto expected = mock(ClothesDto.class);
        when(clothingDtoMerger.merge(html, vision, mapped)).thenReturn(expected);

        // when
        ClothesDto result = sut.extractFromUrl(url);

        // then
        // HTML 추출 & Vision analyze 호출 파라미터 캡쳐
        ArgumentCaptor<List<String>> defNamesCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> optionsCap = ArgumentCaptor.forClass(Map.class);

        verify(htmlExtractor).extract(url);
        verify(visionAnalyzer).analyze(
                eq(html.imageUrl()),
                eq(html.title()),
                eq(html.description()),
                defNamesCap.capture(),
                optionsCap.capture()
        );

        List<String> defNames = defNamesCap.getValue();
        Map<String, List<String>> optionsByDef = cast(optionsCap.getValue());

        // defNames는 Attribute 이름 목록
        assertThat(defNames).containsExactly("색상", "사이즈");

        // 3) 옵션 전처리 검증: trim + null/blank 제거 + 한국어 정렬 + 60개 제한
        Collator coll = Collator.getInstance(Locale.KOREAN);

        // 색상: ["빨강","하양","검정","파랑"] → 정렬(한국어)
        List<String> expectedColors = Arrays.asList("빨강","하양","검정","파랑").stream()
                .map(String::trim)
                .sorted(coll)
                .collect(Collectors.toList());
        assertThat(optionsByDef.get("색상")).isEqualTo(expectedColors);

        // 사이즈: 65개 중 60개만, 정렬(한국어) 후 limit(60)
        List<String> expectedSizes = sizes.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .sorted(coll)
                .limit(60)
                .collect(Collectors.toList());
        assertThat(optionsByDef.get("사이즈")).isEqualTo(expectedSizes);

        // 4) mapFromVision & merge 호출/결과 검증
        verify(attributeMapper).mapFromVision(vision);
        verify(clothingDtoMerger).merge(html, vision, mapped);
        assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("extractFromUrl - DB에 정의가 없으면 빈 defNames/옵션맵으로 analyze 호출")
    void extractFromUrl_emptyAttributes() {
        // given
        String url = "https://store.example/item/100";
        HtmlExtractionResult html = new HtmlExtractionResult(
                "https://img.example/100.jpg",
                "기본 아이템",
                "설명 없음"
        );
        when(htmlExtractor.extract(url)).thenReturn(html);
        when(attributeRepository.findAllWithOptions()).thenReturn(List.of());

        VisionAnalysisResult vision = mock(VisionAnalysisResult.class);
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenReturn(vision);

        when(attributeMapper.mapFromVision(vision)).thenReturn(List.of());
        ClothesDto expected = mock(ClothesDto.class);
        when(clothingDtoMerger.merge(eq(html), eq(vision), eq(List.of()))).thenReturn(expected);

        // when
        ClothesDto result = sut.extractFromUrl(url);

        // then
        ArgumentCaptor<String> s1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s3 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> defNamesCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> optionsCap = ArgumentCaptor.forClass(Map.class);

        verify(visionAnalyzer).analyze(
                s1.capture(), s2.capture(), s3.capture(),
                defNamesCap.capture(), optionsCap.capture()
        );

        assertThat(defNamesCap.getValue()).isEmpty();
        assertThat(((Map<?, ?>) optionsCap.getValue())).isEmpty();

        verify(attributeMapper).mapFromVision(vision);
        verify(clothingDtoMerger).merge(html, vision, List.of());
        assertThat(result).isSameAs(expected);
    }


    @Test
    @DisplayName("analyze가 예외를 던지면 서비스도 예외 전파")
    void extractFromUrl_analyzeThrows_propagates() {
        // given
        String url = "https://store.example/item/err";
        HtmlExtractionResult html = new HtmlExtractionResult(
                "https://img.example/err.jpg", "타이틀", "설명"
        );
        when(htmlExtractor.extract(url)).thenReturn(html);

        when(attributeRepository.findAllWithOptions()).thenReturn(List.of()); // 프롬프트는 비어도 됨
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenThrow(new RuntimeException("down"));

        // when
        Throwable t = org.assertj.core.api.Assertions.catchThrowable(() -> sut.extractFromUrl(url));

        // then
        assertThat(t)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("down");
        verify(htmlExtractor).extract(url);
        // merge까지는 가지 않음
        verifyNoInteractions(clothingDtoMerger);
    }

    @Test
    @DisplayName("동일한 정의명이 중복될 경우: defNames에는 중복이 유지되고, optionsByDef는 마지막 정의가 덮어쓴다")
    void extractFromUrl_duplicateAttributeNames_lastWins() {
        // given
        String url = "u";
        HtmlExtractionResult html = new HtmlExtractionResult("img", "t", "d");
        when(htmlExtractor.extract(url)).thenReturn(html);

        Attribute firstColor = mockAttr("색상", List.of("빨강", "파랑"));
        Attribute secondColor = mockAttr("색상", List.of("초록", "하양")); // 동일 이름, 나중에 등장
        when(attributeRepository.findAllWithOptions()).thenReturn(List.of(firstColor, secondColor));

        VisionAnalysisResult vision = mock(VisionAnalysisResult.class);
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenReturn(vision);
        when(attributeMapper.mapFromVision(vision)).thenReturn(List.of());
        when(clothingDtoMerger.merge(eq(html), eq(vision), eq(List.of()))).thenReturn(mock(ClothesDto.class));

        // when
        sut.extractFromUrl(url);

        // then
        ArgumentCaptor<String> s1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s3 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> defNamesCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> optionsCap = ArgumentCaptor.forClass(Map.class);

        verify(visionAnalyzer).analyze(
                s1.capture(), s2.capture(), s3.capture(),
                defNamesCap.capture(), optionsCap.capture()
        );

        assertThat(List.of(s1.getValue(), s2.getValue(), s3.getValue()))
                .containsExactlyInAnyOrder(html.imageUrl(), html.title(), html.description());

        List<String> defNames = defNamesCap.getValue();
        Map<String, List<String>> optionsByDef = cast(optionsCap.getValue());

        // defNames: 중복 유지
        assertThat(defNames).containsExactly("색상", "색상");
        // optionsByDef: 마지막 항목으로 덮어씀
        assertThat(new ArrayList<>(optionsByDef.keySet())).containsExactly("색상");
        assertThat(optionsByDef.get("색상")).containsExactlyInAnyOrder("초록", "하양"); // 한국어 정렬 결과(하양, 초록)일 수 있음 → 정렬 반영
    }

    @Test
    @DisplayName("옵션 개수 limit=60 경계값: 59/60/61일 때 정확히 60개까지만 전달")
    void extractFromUrl_optionsLimitBoundary() {
        // given
        HtmlExtractionResult html = new HtmlExtractionResult("img", "t", "d");
        when(htmlExtractor.extract(anyString())).thenReturn(html);

        List<String> sizes61 = java.util.stream.IntStream.rangeClosed(1, 61)
                .mapToObj(i -> "사이즈" + i).toList();
        Attribute a59 = mockAttr("A59", sizes61.subList(0, 59));
        Attribute a60 = mockAttr("A60", sizes61.subList(0, 60));
        Attribute a61 = mockAttr("A61", sizes61);

        when(attributeRepository.findAllWithOptions()).thenReturn(List.of(a59, a60, a61));

        VisionAnalysisResult vision = mock(VisionAnalysisResult.class);
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenReturn(vision);
        when(attributeMapper.mapFromVision(vision)).thenReturn(List.of());
        when(clothingDtoMerger.merge(any(), any(), any())).thenReturn(mock(ClothesDto.class));

        // when
        sut.extractFromUrl("x");

        // then
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> optionsCap = ArgumentCaptor.forClass(Map.class);
        verify(visionAnalyzer).analyze(anyString(), anyString(), anyString(), anyList(), optionsCap.capture());

        Map<String, List<String>> optionsByDef = cast(optionsCap.getValue());
        assertThat(optionsByDef.get("A59")).hasSize(59);
        assertThat(optionsByDef.get("A60")).hasSize(60);
        assertThat(optionsByDef.get("A61")).hasSize(60); // 61 → 60 으로 컷
    }

    @Test
    @DisplayName("한국어 Collator 정렬이 적용되어야 함 (혼합 입력, 공백/널/빈문자 제거 + trim)")
    void extractFromUrl_collationAndSanitization() {
        // given
        HtmlExtractionResult html = new HtmlExtractionResult("img", "t", "d");
        when(htmlExtractor.extract(anyString())).thenReturn(html);

        Attribute brand = mockAttr("브랜드",
                Arrays.asList("  나이키", "아디다스 ", " 푸마", null, "", "   "));
        when(attributeRepository.findAllWithOptions()).thenReturn(List.of(brand));

        VisionAnalysisResult vision = mock(VisionAnalysisResult.class);
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenReturn(vision);
        when(attributeMapper.mapFromVision(vision)).thenReturn(List.of());
        when(clothingDtoMerger.merge(any(), any(), any())).thenReturn(mock(ClothesDto.class));

        // when
        sut.extractFromUrl("x");

        // then
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> optionsCap = ArgumentCaptor.forClass(Map.class);
        verify(visionAnalyzer).analyze(anyString(), anyString(), anyString(), anyList(), optionsCap.capture());

        Map<String, List<String>> optionsByDef = cast(optionsCap.getValue());

        // 기대값: null/blank 제거 + trim + 한국어 정렬
        var coll = java.text.Collator.getInstance(java.util.Locale.KOREAN);
        List<String> expected = java.util.stream.Stream.of("나이키", "아디다스", "푸마")
                .sorted(coll)
                .toList();

        assertThat(optionsByDef.get("브랜드")).isEqualTo(expected);
    }

    @Test
    @DisplayName("정의 순서가 Map key 순서(삽입 순서)로 보존되어야 함 (LinkedHashMap)")
    void extractFromUrl_preservesDefinitionOrder() {
        // given
        HtmlExtractionResult html = new HtmlExtractionResult("img", "t", "d");
        when(htmlExtractor.extract(anyString())).thenReturn(html);

        Attribute def1 = mockAttr("D1", List.of("a"));
        Attribute def2 = mockAttr("D2", List.of("b"));
        Attribute def3 = mockAttr("D3", List.of("c"));
        when(attributeRepository.findAllWithOptions()).thenReturn(List.of(def1, def2, def3));

        VisionAnalysisResult vision = mock(VisionAnalysisResult.class);
        when(visionAnalyzer.analyze(anyString(), anyString(), anyString(), anyList(), anyMap()))
                .thenReturn(vision);
        when(attributeMapper.mapFromVision(vision)).thenReturn(List.of());
        when(clothingDtoMerger.merge(any(), any(), any())).thenReturn(mock(ClothesDto.class));

        // when
        sut.extractFromUrl("x");

        // then
        ArgumentCaptor<List<String>> defNamesCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> optionsCap = ArgumentCaptor.forClass(Map.class);

        verify(visionAnalyzer).analyze(anyString(), anyString(), anyString(), defNamesCap.capture(), optionsCap.capture());

        List<String> defNames = defNamesCap.getValue();
        Map<String, List<String>> optionsByDef = cast(optionsCap.getValue());

        assertThat(defNames).containsExactly("D1", "D2", "D3");
        assertThat(new java.util.ArrayList<>(optionsByDef.keySet())).containsExactly("D1", "D2", "D3");
    }


    // --------- helpers ---------

    private Attribute mockAttr(String name, List<String> optionValues) {
        Attribute a = mock(Attribute.class);
        when(a.getName()).thenReturn(name);
        List<AttributeOption> options = new ArrayList<>();
        for (String v : optionValues) {
            AttributeOption o = mock(AttributeOption.class);
            when(o.getValue()).thenReturn(v);
            options.add(o);
        }
        when(a.getOptions()).thenReturn(options);
        return a;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> cast(Map<?, ?> m) {
        return (Map<K, V>) m;
    }
}
