package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // set creation date
        userInput.setCreationDate(new Date());

        // create user
        User createdUser = userService.createUser(userInput);
        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }
    
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        User user = userService.checkLoginCredentials(userPostDTO);
        if(user==null || !user.getName().equals(userPostDTO.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @GetMapping("/users/{username}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserProfile(@PathVariable String username) {
        // Fetch the user by username
        User user = userService.getUserByUsername(username);

        // Check if the user exists, throw a 404 if not
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Convert the user to the API representation
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PutMapping("/users/{username}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO updateUserProfile(
            @PathVariable String username,
            @RequestBody UserPutDTO userPutDTO
    ) {
        // Fetch the user by username
        User existingUser = userService.getUserByUsername(username);

        // Check if the user exists, throw a 404 if not
        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Update user properties
        existingUser.setBirthDate(userPutDTO.getBirthDate());

        // Save the updated user
        User updatedUser = userService.updateUser(existingUser);

        // Convert the updated user to the API representation
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
    }

}