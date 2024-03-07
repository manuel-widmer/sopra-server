package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date; // ADDED


/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    // TEST STATUS: TEST IMPLEMENTED FOR GETTING ALL USERS
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    // TEST STATUS: TEST IMPLEMENTED FOR CORRECT REGISTRATION FUNCTIONALITY
    @PostMapping("/users/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        // Convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // Set creation date
        userInput.setCreationDate(new Date());

        // Create user
        User createdUser = userService.createUser(userInput);

        // Convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }


    // TEST STATUS: TEST IMPLEMENTED FOR SUCCESSFUL LOGIN
    // TEST STATUS: TEST IMPLEMENTED FOR UNSUCCESSFUL LOGIN DUE TO WRONG PASSWORD
    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        User user = userService.checkLoginCredentials(userPostDTO);

        userService.updateStatus(user, UserStatus.ONLINE);

        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PostMapping("/users/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestBody UserPostDTO userPostDTO) {
        Long id = userPostDTO.getId();

        // Fetch the user by userId
        User user = userService.getUserById(id);

        // Update user status to OFFLINE
        userService.updateStatus(user, UserStatus.OFFLINE);
    }

    // TEST STATUS: IMPLEMENTED
    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserProfile(@PathVariable Long id) {
        // Fetch the user by username
        User user = userService.getUserById(id);

        // Convert the user to the API representation
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    // TEST STATUS: IMPLEMENTED
    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<UserGetDTO> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UserPutDTO userPutDTO
    ) {
        // Fetch the user by username
        User existingUser = userService.getUserById(id);

        // Check if the user exists
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Update user properties
        existingUser.setUsername(userPutDTO.getUsername());
        existingUser.setBirthDate(userPutDTO.getBirthDate());

        // Save the updated user
        User updatedUser = userService.updateUser(existingUser);

        // Convert the updated user to the API representation
        return ResponseEntity.noContent().build();
    }


}