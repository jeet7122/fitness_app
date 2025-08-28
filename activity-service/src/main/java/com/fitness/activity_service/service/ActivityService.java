package com.fitness.activity_service.service;

import com.fitness.activity_service.dto.ActivityRequest;
import com.fitness.activity_service.dto.ActivityResponse;
import com.fitness.activity_service.model.Activity;
import com.fitness.activity_service.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final RabbitTemplate rabbitTemplate;
    private final UserValidationService userValidationService;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final ActivityRepository activityRepository;
    public ActivityResponse trackActivity(ActivityRequest request) {

        boolean isValidUser = userValidationService.validate(request.getUserId());
        if (!isValidUser) {
            throw new RuntimeException("Invalid user id " + request.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getActivityType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalProperties(request.getMetrics())
                .build();
        activityRepository.save(activity);

        //Publish to Rabbit MQ for AI Processing

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, activity);
        }
        catch (Exception e) {
            log.error("Failed to publish activity to RabbitMQ: ", e);
        }


        return mapToResponse(activity);


    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse activityResponse = new ActivityResponse();
        activityResponse.setId(activity.getId());
        activityResponse.setUserId(activity.getUserId());
        activityResponse.setType(activity.getType());
        activityResponse.setDuration(activity.getDuration());
        activityResponse.setCaloriesBurned(activity.getCaloriesBurned());
        activityResponse.setStartTime(activity.getStartTime());
        activityResponse.setAdditionalProperties(activity.getAdditionalProperties());
        return activityResponse;
    }

    public List<ActivityResponse> getAllActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);

        return activities
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new RuntimeException("Activity with id: " + activityId + " not found"));
        return mapToResponse(activity);
    }
}
