package com.team3.otboo.domain.clothing.analyzers;

import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import java.util.List;
import java.util.Map;

public interface VisionAnalyzer {
    VisionAnalysisResult analyze(String imageUrl, String title, String description, List<String> definitionNames, Map<String, List<String>> optionsByDef);
}