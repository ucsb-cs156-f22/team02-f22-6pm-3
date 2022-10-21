package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDinningCommonsMenu;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDinningCommonsMenuRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "UCSBDinningCommonsMenu")
@RequestMapping("/api/ucsbdinningcommonsmenu")
@RestController
@Slf4j
public class UCSBDinningCommonsMenuController extends ApiController {

    @Autowired
    UCSBDinningCommonsMenuRepository ucsbMenuRepository;

    @ApiOperation(value = "List all ucsb dinning commons menu")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDinningCommonsMenu> allUCSBDinningCommonsMenu() {
        Iterable<UCSBDinningCommonsMenu> menu = ucsbMenuRepository.findAll();
        return menu;
    }

    @ApiOperation(value = "Get a single menu")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDinningCommonsMenu getById(
            @ApiParam("id") @RequestParam Long id) {
        UCSBDinningCommonsMenu ucsbMenu = ucsbMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDinningCommonsMenu.class, id));

        return ucsbMenu;
    }

    @ApiOperation(value = "Create a new menu")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDinningCommonsMenu postUCSBDinningCommonsMenu(
            @ApiParam("diningCommonsCode") @RequestParam String diningCommonsCode,
            @ApiParam("name") @RequestParam String name,
            @ApiParam("station") @RequestParam String station
         ){

        

        UCSBDinningCommonsMenu ucsbMenu = new UCSBDinningCommonsMenu();
        ucsbMenu.setDiningCommonsCode(diningCommonsCode);
        ucsbMenu.setName(name);
        ucsbMenu.setStation(station);

        UCSBDinningCommonsMenu savedUcsbMenu = ucsbMenuRepository.save(ucsbMenu);

        return savedUcsbMenu;
    }

    @ApiOperation(value = "Delete a UCSBMenu")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBMenu(
            @ApiParam("id") @RequestParam Long id) {
        UCSBDinningCommonsMenu ucsbMenu = ucsbMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDinningCommonsMenu.class, id));

        ucsbMenuRepository.delete(ucsbMenu);
        return genericMessage("UCSBDinningCommonMenu with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single menu")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDinningCommonsMenu updateUCSBDinningCommonsMenu(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid UCSBDinningCommonsMenu incoming) {

        UCSBDinningCommonsMenu ucsbMenu = ucsbMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDinningCommonsMenu.class, id));

        ucsbMenu.setDiningCommonsCode(incoming.getDiningCommonsCode());
        ucsbMenu.setName(incoming.getName());
        ucsbMenu.setStation(incoming.getStation());

        ucsbMenuRepository.save(ucsbMenu);

        return ucsbMenu;
    }
}