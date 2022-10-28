package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

    @Autowired
    RecommendationRepository recommendationRequest;

    @ApiOperation(value = "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> recs = recommendationRequest.findAll();
        return recs;
    }
    
    @ApiOperation(value = "Get a single recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
        @ApiParam("id") @RequestParam Long id) {
    RecommendationRequest recrequest = recommendationRequest.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recrequest;
    
    }

    @ApiOperation(value = "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationRequest(
        @ApiParam("requesterEmail") @RequestParam String requesterEmail,
        @ApiParam("professorEmail") @RequestParam String professorEmail,
        @ApiParam("explanation") @RequestParam String explanation,
        @ApiParam("dateRequested") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
        @ApiParam("dateNeeded") @RequestParam LocalDateTime dateNeeded,
        @ApiParam("done") @RequestParam boolean done
    ) throws JsonProcessingException{
        log.info("localDateTime={}", dateRequested);

        RecommendationRequest recrequest = new RecommendationRequest();
        recrequest.setDateNeeded(dateNeeded);
        recrequest.setProfessorEmail(professorEmail);
        recrequest.setRequesterEmail(requesterEmail);
        recrequest.setDateRequested(dateRequested);
        recrequest.setExplanation(explanation);
        recrequest.setDone(done);

        RecommendationRequest savedRecRequest = recommendationRequest.save(recrequest);

        return savedRecRequest;
}

@ApiOperation(value = "Delete a RecommendationRequest")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@DeleteMapping("")
public Object deleteRecRequest(
    @ApiParam("id") @RequestParam Long id) {
        RecommendationRequest recrequest = recommendationRequest.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationRequest.delete(recrequest);
        return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
}

@ApiOperation(value = "Update a RecommendationRequest")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@PutMapping("")
public RecommendationRequest updateRecommendationRequest(
    @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequest incoming) {
    
                RecommendationRequest recrequest = recommendationRequest.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

                recrequest.setDateNeeded(incoming.getDateNeeded());
                recrequest.setDateRequested(incoming.getDateRequested());
                recrequest.setDone(incoming.getDone());
                recrequest.setExplanation(incoming.getExplanation());
                recrequest.setProfessorEmail(incoming.getProfessorEmail());
                recrequest.setRequesterEmail(incoming.getRequesterEmail());

                recommendationRequest.save(recrequest);

                return recrequest;
            }
}