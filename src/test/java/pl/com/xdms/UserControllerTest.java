package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.xdms.domain.user.Role;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.service.RoleService;
import pl.com.xdms.service.UserService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created on 05.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = {"/sql_scripts/createValuesInDBforTests.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Test
    public void getAllUsersTest() throws Exception {
        this.mockMvc.perform(get("/admin/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void getUsersByIdTestStatusOk() throws Exception {
        int id = 1;
        this.mockMvc.perform(get("/admin/users/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void getUsersByIdTestStatusNOk() throws Exception {
        int id = 100;
        this.mockMvc.perform(get("/admin/users/" + id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void updateUserTestStatusOk() throws Exception {
        Long id = 1L;
        User user = userService.getUserById(id);
        user.setUsername("Booo");

        //to avoid com.fasterxml.jackson.databind.JsonMappingException
        user.setWarehouses(null);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(user);
        this.mockMvc.perform(put("/admin/users").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateUserTestStatusBadEntity() throws Exception {
        Long id = 1L;
        User user = userService.getUserById(id);
        user.setUsername(null);
        user.setPassword("Fooo");

        //to avoid com.fasterxml.jackson.databind.JsonMappingException
        user.setWarehouses(null);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(user);
        this.mockMvc.perform(put("/admin/users").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().exists("user-username_NotNull"))
                .andExpect(header().exists("user-username_NotBlank"))
                .andExpect(header().exists("user-password_Size"));
    }

    @Test
    public void getUsersByIdTestString() throws Exception {
        String id = "string";
        this.mockMvc.perform(get("/admin/users/" + id))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void createUserTestStatusOk() throws Exception {
        User user = new User();
        Role role = roleService.getRoleById(1L);
        user.setUsername("USerName");
        user.setFirstName("kokoko");
        user.setLastName("KUKUKUKUKU");
        user.setPassword("trewsdfgtr");
        user.setRole(role);
        user.setEmail("email@email.em");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(user);
        this.mockMvc.perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));

    }

    @Test
    public void createUserTestStatusNOk() throws Exception {
        User user = new User();
        user.setLastName("KUKUKUKUKU");
        user.setPassword("trs");
        user.setEmail("email@email.em");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(user);
        this.mockMvc.perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().exists("user-username_NotNull"))
                .andExpect(header().exists("user-username_NotBlank"))
                .andExpect(header().exists("user-password_Size"))
                .andExpect(header().exists("user-firstName_NotBlank"))
                .andExpect(header().exists("user-role_NotNull"))
                .andExpect(header().exists("user-firstName_NotNull"));
    }

    @Test
    public void createUserWithEmptyRoleTestStatusCreated() throws Exception {
        User user = new User();
        Role role = new Role();

        user.setUsername("USerName");
        user.setFirstName("kokoko");
        user.setLastName("KUKUKUKUKU");
        user.setPassword("trewsdfgtr");
        user.setRole(role);
        user.setEmail("email@email.em");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(user);
        this.mockMvc.perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));

        Assert.assertEquals(3, userService.getUsers("default", "default").size());
    }

    @Test
    public void createUserWithNotExistedRoleTestStatusCreated() throws Exception {
        User user = new User();
        Role role = new Role();
        role.setId(15L);

        user.setUsername("USerName");
        user.setFirstName("kokoko");
        user.setLastName("KUKUKUKUKU");
        user.setPassword("trewsdfgtr");
        user.setRole(role);
        user.setEmail("email@email.em");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(user);
        this.mockMvc.perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));

        Assert.assertEquals(3, userService.getUsers("default", "default").size());
    }

    @Test
    public void deleteUserByIdStatusOk() throws Exception{
        Long id = 1L;
        this.mockMvc.perform(delete("/admin/users/"+id))
                .andDo(print())
                .andExpect(content().string("deleted"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserByIdStringParameter() throws Exception{
        String id = "test";
        this.mockMvc.perform(delete("/admin/users/"+id))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteUserByIdNotFound() throws Exception{
        Long id = 10L;
        this.mockMvc.perform(delete("/admin/users/"+id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    private String jsonClean(String json){
        return json.replaceAll("^ +| +$|\\R |, +|\\{ ", "")
                .replace(" : ", ":")
                .replaceAll("   \"| \"", "\"")
                .replaceAll("}", "").trim() + "}}";
    }

}
