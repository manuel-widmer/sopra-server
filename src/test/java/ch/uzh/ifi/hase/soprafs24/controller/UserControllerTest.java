package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO; // ADDED
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Date; // ADDED
import java.text.SimpleDateFormat; // ADDED

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  // THIS TEST (ALREADY EXISTING) CHECKS WHETHER A QUERY OF ALL USERS (@GetMapping("/users") IS CARRIED OUT CORRECTLY
    @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  // THIS TEST (ALREADY EXISTING) CHECKS THE REGISTRATION FUNCTIONALITY (@PostMapping("/users/registration")
  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    // NOTE TO MYSELF: Mockito.any() allows for flexible argument matching
    given(userService.createUser(Mockito.any())).willReturn(user);

    // Define method for simulating a POST request with correct registration input to "/users/registration" endpoint
    MockHttpServletRequestBuilder postRequest = post("/users/registration")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // Perform actual test by comparing expected (= mocked) and actual result
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

    // THIS TEST (ADDED BY MYSELF) CHECKS WHETHER TRYING TO ADD AN EXISTING USER FOR THE SECOND TIME WILL THROW A 409 ERROR (@PostMapping("/users/registration")
    @Test
    public void createUser_duplicateUser_conflict() throws Exception {
        // given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Test User");
        existingUser.setUsername("testUsername");
        existingUser.setToken("1");
        existingUser.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("Test User");
        userPostDTO.setUsername("testUsername");

        // Mock the behavior to return an existing user for the given input
        given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "User already exists"));

        // Define method for simulating a POST request with an already existing user to the "/users/registration" endpoint
        MockHttpServletRequestBuilder postRequest = post("/users/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict()); // Expecting Conflict status
    }

    // THIS TEST (ADDED BY MYSELF) CHECKS THE LOGIN FUNCTIONALITY WITH CORRECT LOGIN CREDENTIALS (@PostMapping("/users/login")
    @Test
    public void loginUser_validInput_userLoggedIn() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setName("1234");
        user.setUsername("Michael");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("1234");
        userPostDTO.setUsername("Michael");

        // Mock the behavior for valid login credentials
        given(userService.checkLoginCredentials(Mockito.any())).willReturn(user);

        // Define method for simulating a POST request with correct login credentials to the "/users/login" endpoint
        MockHttpServletRequestBuilder postRequest = post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    // THIS TEST (ADDED BY MYSELF) CHECKS THE LOGIN FUNCTIONALITY WITH NON-EXISTING LOGIN CREDENTIALS (@PostMapping("/users/login")
    @Test
    public void loginUser_notRegisteredUser_notFound() throws Exception {
        // given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("1234");
        userPostDTO.setUsername("NonExistentUser");

        // Mock the behavior for user not found
        given(userService.checkLoginCredentials(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Define method for simulating a POST request with a non-existing user to the "/users/login" endpoint
        MockHttpServletRequestBuilder postRequest = post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    // THIS TEST (ADDED BY MYSELF) CHECKS THE LOGIN FUNCTIONALITY WITH INCORRECT LOGIN CREDENTIALS (@PostMapping("/users/login")
    @Test
    public void loginUser_wrongPassword_unauthorized() throws Exception {
        // given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("CorrectPassword");
        existingUser.setUsername("Michael");
        existingUser.setToken("1");
        existingUser.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("WrongPassword"); // Wrong password

        // Mock the behavior for wrong password
        given(userService.checkLoginCredentials(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password"));

        // Define method for simulating a POST request with incorrect login credentials to the "/users/login" endpoint
        MockHttpServletRequestBuilder postRequest = post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    // THIS TEST (ADDED BY MYSELF) CHECKS WHETHER EXISTING USERS ARE CORRECTLY RETURNED FROM THE DB (@GetMapping("/users/{id}")
    @Test
    public void getUserProfile_existingUser_userProfileReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        // Mock the behavior to return the user for the given ID
        given(userService.getUserById(1L)).willReturn(user);

        // Define method for simulating a GET request to the "/users/{id}" endpoint
        MockHttpServletRequestBuilder getRequest = get("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON);

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    // THIS TEST (ADDED BY MYSELF) CHECKS NON-EXISTING USERS THROW THE EXPECTED 404 ERROR (@GetMapping("/users/{id}")
    @Test
    public void getUserProfile_nonExistingUser_notFound() throws Exception {
        // given
        long nonExistingUserId = 2L;

        // Mock the behavior to throw a 404 Not Found for the non-existing user ID
        given(userService.getUserById(nonExistingUserId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Define method for simulating a GET request with a non-existing user to the "/users/{id}" endpoint
        MockHttpServletRequestBuilder getRequest = get("/users/{id}", nonExistingUserId)
                .contentType(MediaType.APPLICATION_JSON);

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserProfile_existingUser_profileUpdated() throws Exception {
        // Existing user data
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Test User");
        existingUser.setUsername("testUsername");
        existingUser.setBirthDate(null);

        // User data for update
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = sdf.parse("2000-01-01");
        userPutDTO.setBirthDate(birthDate);

        // Mock the behavior to return the existing user
        given(userService.getUserById(1L)).willReturn(existingUser);

        // Mock the behavior to return the updated user after the update
        given(userService.updateUser(Mockito.any())).willReturn(existingUser);

        // Define method for simulating a PUT request for updating an existing user to the "/users/{id}" endpoint
        MockHttpServletRequestBuilder putRequest = put("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateUserProfile_nonExistingUser_notFound() throws Exception {
        // Non-existing user data
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = sdf.parse("2000-01-01");
        userPutDTO.setBirthDate(birthDate);

        // Mock the behavior to return null for a non-existing user
        given(userService.getUserById(1L)).willReturn(null);

        // Define method for simulating a PUT request for updating a non-existing user to the "/users/{id}" endpoint
        MockHttpServletRequestBuilder putRequest = put("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // Perform actual test by comparing expected (= mocked) and actual result
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}