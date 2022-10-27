//package edu.ucsb.cs156.example.controllers;
//public class UCSBOrganizationsController {
//}
package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
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


@Api(description = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {

    @Autowired
    UCSBOrganizationsRepository ucsbOrganizationsRepository;

    @ApiOperation(value = "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganizations> allUCSBOrganizations() {
        Iterable<UCSBOrganizations> ucsbOrgs = ucsbOrganizationsRepository.findAll();
        return ucsbOrgs;
    }

    @ApiOperation(value = "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganizations getById(
            @ApiParam("orgCode") @RequestParam String orgCode) {
        UCSBOrganizations ucsbOrgs = ucsbOrganizationsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));

        return ucsbOrgs;
    }

    @ApiOperation(value = "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganizations postOrganizations(
        @ApiParam("orgCode") @RequestParam String orgcode,
        @ApiParam("orfTranslation") @RequestParam String orgtranslation,
        @ApiParam("orgTranslationShort") @RequestParam String orgtranslationshort,
        @ApiParam("incavtive") @RequestParam boolean inactive
        )
        {

        UCSBOrganizations ucsbOrgs = new UCSBOrganizations();
        ucsbOrgs.setOrgCode(orgcode);
        ucsbOrgs.setOrgTranslation(orgtranslation);
        ucsbOrgs.setOrgTranslationShort(orgtranslationshort);
        ucsbOrgs.setInactive(inactive);
        //commons.setHasDiningCam(hasDiningCam);
        //commons.setLatitude(latitude);
        //commons.setLongitude(longitude);

        UCSBOrganizations savedOrganizations = ucsbOrganizationsRepository.save(ucsbOrgs);

        return savedOrganizations;
    }

    @ApiOperation(value = "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOranization(
        @ApiParam("orgCode") @RequestParam String orgCode) {
            UCSBOrganizations ucsbOrgs = ucsbOrganizationsRepository.findById(orgCode)
                    .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));
        ucsbOrganizationsRepository.delete(ucsbOrgs);
        return genericMessage("UCSBOrganization with orgcode %s deleted".formatted(orgCode));
    }

    @ApiOperation(value = "Update a single commons")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganizations updateoOrganizations(
            @ApiParam("orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganizations incoming) {

        UCSBOrganizations ucsbOrgs = ucsbOrganizationsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));


        ucsbOrgs.setOrgCode((incoming).getOrgCode());  
        ucsbOrgs.setOrgTranslation(incoming.getOrgTranslation());
        ucsbOrgs.setOrgTranslationShort(incoming.getOrgTranslationShort());
        ucsbOrgs.setInactive(incoming.getInactive());

        ucsbOrganizationsRepository.save(ucsbOrgs);

        return ucsbOrgs;
    }
}