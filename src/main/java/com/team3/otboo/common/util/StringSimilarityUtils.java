package com.team3.otboo.common.util;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;

public class StringSimilarityUtils {

    // 거리 → 유사도(0~1)로 변환해서 기준치 비교하기 편하게
    public static double similarity(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        if (na.isEmpty() && nb.isEmpty()) return 1.0;
        int dist = levenshteinDistance(na, nb);
        int maxLen = Math.max(na.length(), nb.length());
        return (maxLen == 0) ? 1.0 : 1.0 - ((double) dist / maxLen);
    }

    public static String findBestBySimilarity(String input, List<String> candidates) {
        if (input == null || candidates == null || candidates.isEmpty()) return input;
        String in = normalize(input);

        return candidates.stream()
                .max(Comparator.comparingDouble(opt -> similarity(in, normalize(opt))))
                .orElse(input);
    }

    // 한글/영문 통합 정규화 + 공백/특수문자 정리
    public static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFKC)
                .toLowerCase()
                .replaceAll("\\s+", "")         // 공백 제거
                .replaceAll("[()\\[\\]{}]", "") // 괄호 제거
                .replaceAll("[-_./]", "");      // 구분자 제거
        // 색상 동의어 같은 간단 매핑을 추가 가능
        n = n.replace("검정", "블랙").replace("까망", "블랙")
                .replace("하양", "화이트").replace("흰색", "화이트")
                .replace("회색", "그레이").replace("회", "그레이");
        return n;
    }

    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }
}