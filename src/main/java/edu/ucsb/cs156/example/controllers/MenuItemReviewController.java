package edu.ucsb.cs156.example.controllers;

import java.time.LocalDateTime;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Api(description = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {
    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @ApiOperation(value="List all menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value="/all")
    public Iterable<MenuItemReview> getAll() {
        Iterable<MenuItemReview> menuItemReviews = menuItemReviewRepository.findAll();
        return menuItemReviews;
    }


    @ApiOperation(value = "Get a single review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @ApiParam("id") @RequestParam Long id) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemReview;
    }

    @ApiOperation(value = "Update a single review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateMenuItemReview(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid MenuItemReview incoming) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
        menuItemReview.setDateReviewed(incoming.getDateReviewed());
        menuItemReview.setItemId(incoming.getItemId());
        menuItemReview.setStars(incoming.getStars());
        menuItemReview.setComments(incoming.getComments());

        menuItemReviewRepository.save(menuItemReview);
        return menuItemReview;
    }

    @ApiOperation(value="Create a new menu item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value="/post")
    public MenuItemReview postMenuItemReview(
        @ApiParam("ID of item being reviewed") @RequestParam int itemId,
        @ApiParam("Email of reviewer") @RequestParam String reviewerEmail,
        @ApiParam("Number of stars in review (0-5)") @RequestParam int stars,
        @ApiParam("Comments from reviewer") @RequestParam String comments,
        @ApiParam("date reviewed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed
    ) throws JsonProcessingException {
        log.info("localDateTime={}", dateReviewed);

        MenuItemReview menuItemReview = new MenuItemReview();
        menuItemReview.setItemId(itemId);
        menuItemReview.setReviewerEmail(reviewerEmail);
        menuItemReview.setStars(stars);
        menuItemReview.setComments(comments);
        menuItemReview.setDateReviewed(dateReviewed);

       MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);
       return savedMenuItemReview;
    }

    @ApiOperation(value="Delete a single menu item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value="")
    public Object deleMenuItemReview(
        @ApiParam("ID of menu item review to delete") @RequestParam long id
    ) throws JsonProcessingException {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));
        
        menuItemReviewRepository.delete(menuItemReview);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }

    

}

