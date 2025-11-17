package com.e_commerce.users;

import com.e_commerce.users.events.EmailUpdate;
import com.e_commerce.users.events.EventSender;
import com.e_commerce.users.exceptions.DuplicatedUsernameException;
import com.e_commerce.users.exceptions.UserNotFoundException;
import com.e_commerce.users.exceptions.UsernameNotFoundException;
import com.e_commerce.users.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventSender eventSender;

    @InjectMocks
    private UserService service;

    @Test
    public void registersUser() {
        when(repository.existsUserByUsername("username")).thenReturn(false);

        User user = new User("username", "username@email.com", "a merchant", "password");
        service.registerUser(user);

        verify(repository, times(1)).existsUserByUsername("username");
        verify(passwordEncoder, times(1)).encode("password");
        verify(repository, times(1)).save(user);
    }

    @Test
    public void userAlreadyExists() {
        when(repository.existsUserByUsername("username")).thenReturn(true);

        User user = new User("username", "username@email.com", "a merchant", "password");
        assertThrows(DuplicatedUsernameException.class, () -> service.registerUser(user));

        verify(repository, times(1)).existsUserByUsername("username");
        verify(passwordEncoder, times(0)).encode("password");
        verify(repository, times(0)).save(user);
    }

    @Test
    public void retrieveUserInfo() {
        var userInfo = new UserInfo("nobody");

        when(repository.findOptionalUserInfoByUsername("username")).thenReturn(Optional.of(userInfo));

        assertThat(service.retrieveUserInfo("username")).isEqualTo(userInfo);

        verify(repository, times(1)).findOptionalUserInfoByUsername("username");
    }

    @Test
    public void userIsNotRegisteredWhenReturningHisInfo() {
        when(repository.findOptionalUserInfoByUsername("username")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.retrieveUserInfo("username"));

        verify(repository, times(1)).findOptionalUserInfoByUsername("username");
    }

    @Test
    public void retrieveUserEmail() {
        when(repository.findOptionalUserEmailByUsername("username")).thenReturn(Optional.of("username@email.com"));

        assertThat(service.retrieveUserEmail("username")).isEqualTo(new UserEmail("username@email.com"));

        verify(repository, times(1)).findOptionalUserEmailByUsername("username");
    }

    @Test
    public void userIsNotRegisteredWhenReturningHisEmail() {
        when(repository.findOptionalUserEmailByUsername("username")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.retrieveUserEmail("username"));

        verify(repository, times(1)).findOptionalUserEmailByUsername("username");
    }

    @Test
    public void userPasswordIsUpdated() {
        UserCredentials credentials = new UserCredentials("username", "password");

        when(passwordEncoder.encode("password")).thenReturn("gibberish");
        when(passwordEncoder.encode("new_password")).thenReturn("new_gibberish");
        when(repository.updateUserPassword("username", "gibberish", "new_gibberish"))
                .thenReturn(1);

        service.updateUserPassword(new PasswordChange(credentials, "new_password"));

        verify(passwordEncoder, times(1)).encode("password");
        verify(passwordEncoder, times(1)).encode("new_password");
        verify(repository, times(1))
                .updateUserPassword("username", "gibberish", "new_gibberish");
    }

    @Test
    public void userPasswordUpdateFailsOnInvalidUser() {
        UserCredentials credentials = new UserCredentials("username", "password");

        when(passwordEncoder.encode("password")).thenReturn("gibberish");
        when(passwordEncoder.encode("new_password")).thenReturn("new_gibberish");
        when(repository.updateUserPassword("username", "gibberish", "new_gibberish"))
                .thenReturn(0);

        assertThrows(UserNotFoundException.class,
                () -> service.updateUserPassword(new PasswordChange(credentials, "new_password")));

        verify(passwordEncoder, times(1)).encode("password");
        verify(passwordEncoder, times(1)).encode("new_password");
        verify(repository, times(1))
                .updateUserPassword("username", "gibberish", "new_gibberish");
    }

    @Test
    public void userEmailIsUpdated() {
        UserCredentials credentials = new UserCredentials("username", "password");

        when(passwordEncoder.encode("password")).thenReturn("gibberish");
        when(repository.updateUserEmail("username", "gibberish", "new_username@email.com"))
                .thenReturn(1);

        service.updateUserEmail(new EmailChange(credentials, "new_username@email.com"));

        verify(passwordEncoder, times(1)).encode("password");
        verify(repository, times(1))
                .updateUserEmail("username", "gibberish", "new_username@email.com");
        verify(eventSender, times (1))
                .sendEmailUpdate(new EmailUpdate("username", "new_username@email.com"));
    }

    @Test
    public void userEmailUpdateFailsOnInvalidUser() {
        UserCredentials credentials = new UserCredentials("username", "password");

        when(passwordEncoder.encode("password")).thenReturn("gibberish");
        when(repository.updateUserEmail("username", "gibberish", "new_username@email.com"))
                .thenReturn(0);

        assertThrows(UserNotFoundException.class,
                () -> service.updateUserEmail(new EmailChange(credentials, "new_username@email.com")));

        verify(passwordEncoder, times(1)).encode("password");
        verify(repository, times(1))
                .updateUserEmail("username", "gibberish", "new_username@email.com");
        verify(eventSender, times (0))
                .sendEmailUpdate(new EmailUpdate("username", "new_username@email.com"));
    }

    @Test
    public void userInfoIsUpdated() {
        UserInfo userInfo = new UserInfo("nobody");

        when(repository.updateUserInfo("username", userInfo)).thenReturn(1);

        service.updateUserInfo(new UserInfoChange("username", userInfo));

        verify(repository, times(1)).updateUserInfo("username", userInfo);
    }

    @Test
    public void userInfoUpdateFailsOnUnknownUsername() {
        UserInfo userInfo = new UserInfo("I'm nobody");

        when(repository.updateUserInfo("username", userInfo)).thenReturn(0);

        assertThrows(UsernameNotFoundException.class, () -> service.updateUserInfo(new UserInfoChange("username", userInfo)));

        verify(repository, times(1)).updateUserInfo("username", userInfo);
    }
}
