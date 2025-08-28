package com.fitness.ai_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.ai_service.model.Activity;
import com.fitness.ai_service.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("AI Response: {}", aiResponse);

        return processAiResponse(activity, aiResponse);
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");
            String jsonContent = textNode.asText().replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "").trim();
            log.info("parsed response: {}", jsonContent);

            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysis = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysis, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysis, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysis, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysis, "caloriesBurned", "Calories Burned:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safetyGuidelines = extractSafetyGuidelines(analysisJson.path("safety"));
            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safetyMeasures(safetyGuidelines)
                    .createdAt(LocalDateTime.now())
                    .build();


        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safetyGuidelines = new ArrayList<>();
        if(safetyNode.isArray()) {
            safetyNode.forEach(item -> safetyGuidelines.add(item.asText()));
        }
        return safetyGuidelines.isEmpty() ? List.of("No safety guidelines provided") : safetyGuidelines;
    }

    private List<String> extractSuggestions(JsonNode suggestionNode) {
        List<String> suggestions = new ArrayList<>();
        if(suggestionNode.isArray()) {
            suggestionNode.forEach(suggestion -> {
                String workout = suggestionNode.path("workout").asText();
                String details = suggestionNode.path("description").asText();
                suggestions.add(String.format("%s\n%s", workout, details));
            });
        }
        return suggestions.isEmpty() ? List.of("No suggestions provided") : suggestions;

    }

    private List<String> extractImprovements(JsonNode improvements) {
        List<String> improvementList = new ArrayList<>();
        if(improvements.isArray()){
            improvements.forEach(improvement->{
                String area = improvement.path("area").asText();
                String detail =  improvement.path("recommendation").asText();
                improvementList.add(String.format("%s: \n %s: ", area, detail));
            });

        }
        return improvementList.isEmpty() ? List.of("No Specific Recommendations") : improvementList;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysis, String key, String prefix) {
        if(!analysis.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysis.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                Analyze the fitness activity and provide detailed recommendations in th following EXACT JSON format:
                {
                    "analysis": {
                        "overall": "Overall analysis here",
                        "pace": "Pace analysis here",
                        "heartRate": "Heart rate analysis here",
                        "caloriesBurned": "Calories burned analysis here",
                    },
                    "improvement": [
                        {
                            "area": "Area name",
                            "recommendation": "Detailed Recommendation"
                        }
                    ],
                    "suggestions": [
                        {
                        "workout": "Workout name",
                        "description": "Detailed workout description"
                        }
                    ],
                    "safety": [
                    "Safety point 1",
                    "Safety point 2"
                }
                
                Analyze this activity:
                Activity Type: %s
                Duration: %d minutes
                Calories Burned: %d
                Additional Metrics: %s
                
                Provide detailed analysis focusing on performance, improvements, next workout suggestions and safety guidelines.
                Ensure the response follows the EXACT JSON format shown above.
                """, activity.getType(), activity.getDuration(), activity.getCaloriesBurned(), activity.getAdditionalProperties());
    }
}
