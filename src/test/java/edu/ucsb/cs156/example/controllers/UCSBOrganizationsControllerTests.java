package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationsRepository ucsbOrganizationsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdiningcommons/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations?orgCode=ABC"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdiningcommons/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganizations orgs = UCSBOrganizations.builder()
                                .orgCode("RMA")
                                //.code("carrillo")
                                .inactive(true)
                                .orgTranslation("Real Madrid Football Club")
                                //.hasDiningCam(true)
                                //.latitude(34.409953)
                                .orgTranslationShort("RealMadrid")
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("RMA"))).thenReturn(Optional.of(orgs));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=RMA"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("RMA"));
                String expectedJson = mapper.writeValueAsString(orgs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationsRepository.findById(eq("FCB"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=FCB"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("FCB"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganizations with id FCB not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganizations orgs = UCSBOrganizations.builder()
                                .orgCode("RMA")
                                .orgTranslation("Real Madrid Football Club")
                                .orgTranslationShort("RealMadrid")
                                .inactive(false)
                                .build();

                UCSBOrganizations orgs_1 = UCSBOrganizations.builder()
                                .orgCode("FCB")
                                .orgTranslation("Barcelona Football Club")
                                .orgTranslationShort("Barcelona")
                                .inactive(false)
                                .build();

                ArrayList<UCSBOrganizations> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(orgs,orgs_1 ));

                when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_orgs() throws Exception {
                // arrange

                UCSBOrganizations org = UCSBOrganizations.builder()
                                .orgCode("ABC")
                                .orgTranslation("B")
                                .orgTranslationShort("C")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationsRepository.save(eq(org))).thenReturn(org);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganizations/post?orgcode=ABC&orgtranslation=B&orgtranslationshort=C&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).save(org);
                String expectedJson = mapper.writeValueAsString(org);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                UCSBOrganizations portola = UCSBOrganizations.builder()
                                .orgCode("Por")
                                .orgTranslation("Portola Dinning Common")
                                .orgTranslationShort("Portola")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("Por"))).thenReturn(Optional.of(portola));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgCode=Por")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("Por");
                verify(ucsbOrganizationsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with orgcode Por deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationsRepository.findById(eq("UCB"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgCode=UCB")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("UCB");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id UCB not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                UCSBOrganizations carrillo = UCSBOrganizations.builder()
                                .orgCode("Car")
                                .orgTranslation("Carillo Dinning Commons")
                                .orgTranslationShort("Carrillo")
                                .inactive(false)
                                .build();

                UCSBOrganizations carrilloEdited = UCSBOrganizations.builder()
                                .orgCode("Car")
                                .orgTranslation("Carillo Dinning Commons")
                                .orgTranslationShort("Car")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(carrilloEdited);

                when(ucsbOrganizationsRepository.findById(eq("Car"))).thenReturn(Optional.of(carrillo));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?orgCode=Car")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("Car");
                verify(ucsbOrganizationsRepository, times(1)).save(carrilloEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganizations editedCommons = UCSBOrganizations.builder()
                                .orgCode("ARS")
                                .orgTranslation("Arsenal Football Club")
                                .orgTranslationShort("Arsenal")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(editedCommons);

                when(ucsbOrganizationsRepository.findById(eq("ARS"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?orgCode=ARS")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("ARS");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id ARS not found", json.get("message"));

        }
}