package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDinningCommonsMenu;
import edu.ucsb.cs156.example.repositories.UCSBDinningCommonsMenuRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDinningCommonsMenuController.class)
@Import(TestConfig.class)
public class UCSBDinningCommonsMenuControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDinningCommonsMenuRepository ucsbMenuRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdates/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdinningcommonsmenu/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdinningcommonsmenu/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdinningcommonsmenu?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdates/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdinningcommonsmenu/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdinningcommonsmenu/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBDinningCommonsMenu ucsbMenu = UCSBDinningCommonsMenu.builder()
                                .name("salad")
                                .station("Entrees")
                                .diningCommonsCode("ortega")
                                .build();

                when(ucsbMenuRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbMenu));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdinningcommonsmenu?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbMenuRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbMenu);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbMenuRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdinningcommonsmenu?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbMenuRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDinningCommonsMenu with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbmenus() throws Exception {

                // arrange

                UCSBDinningCommonsMenu ucsbMenu1 = UCSBDinningCommonsMenu.builder()
                                .name("salad")
                                .station("entrees")
                                .diningCommonsCode("ortega")
                                .build();

                UCSBDinningCommonsMenu ucsbMenu2 = UCSBDinningCommonsMenu.builder()
                                .name("soup")
                                .station("special entrees")
                                .diningCommonsCode("ortega")
                                .build();

                ArrayList<UCSBDinningCommonsMenu> expectedMenus = new ArrayList<>();
                expectedMenus.addAll(Arrays.asList(ucsbMenu1, ucsbMenu2));

                when(ucsbMenuRepository.findAll()).thenReturn(expectedMenus);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdinningcommonsmenu/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbMenuRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMenus);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
//start here next time
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbmenu() throws Exception {
                // arrange


                UCSBDinningCommonsMenu ucsbMenu1 = UCSBDinningCommonsMenu.builder()
                                .name("salad")
                                .station("Entrees")
                                .diningCommonsCode("ortega")
                                .build();

                when(ucsbMenuRepository.save(eq(ucsbMenu1))).thenReturn(ucsbMenu1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdinningcommonsmenu/post?name=salad&station=Entrees&diningCommonsCode=ortega")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbMenuRepository, times(1)).save(ucsbMenu1);
                String expectedJson = mapper.writeValueAsString(ucsbMenu1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_menu() throws Exception {
                // arrange

                

                UCSBDinningCommonsMenu ucsbMenu1 = UCSBDinningCommonsMenu.builder()
                                .name("salad")
                                .station("Entrees")
                                .diningCommonsCode("ortega")
                                .build();

                when(ucsbMenuRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbMenu1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdinningcommonsmenu?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbMenuRepository, times(1)).findById(15L);
                verify(ucsbMenuRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDinningCommonMenu with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbmenu_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbMenuRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdinningcommonsmenu?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbMenuRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDinningCommonsMenu with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbmenu() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                UCSBDinningCommonsMenu ucsbMenu1 = UCSBDinningCommonsMenu.builder()
                                .name("salad")
                                .station("Entrees")
                                .diningCommonsCode("ortega")
                                .build();

                UCSBDinningCommonsMenu ucsbMenuEdited = UCSBDinningCommonsMenu.builder()
                                .name("soup")
                                .station("special entree")
                                .diningCommonsCode("ortega")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbMenuEdited);

                when(ucsbMenuRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbMenu1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdinningcommonsmenu?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbMenuRepository, times(1)).findById(67L);
                verify(ucsbMenuRepository, times(1)).save(ucsbMenuEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbmenu_that_does_not_exist() throws Exception {
                // arrange


                UCSBDinningCommonsMenu ucsbMenuEdited = UCSBDinningCommonsMenu.builder()
                                .name("soup")
                                .station("special entree")
                                .diningCommonsCode("ortega")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbMenuEdited);

                when(ucsbMenuRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdinningcommonsmenu?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbMenuRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDinningCommonsMenu with id 67 not found", json.get("message"));

        }
}

