package com.e_commerce.users;

import com.e_commerce.users.exceptions.DuplicatedUsernameException;
import com.e_commerce.users.exceptions.UserNotFoundException;
import com.e_commerce.users.exceptions.UsernameNotFoundException;
import com.e_commerce.users.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebMvcTest
public class UserControllerTests {
    private static class ValidUserTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            new User("username", "username@email.com", "a merchant", "Password1@")),
                    Arguments.of(
                            new User("username", "username@email.com", null, "Password1@"))
            );
        }
    }

    private static class InvalidUserTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            new User(null, "username@email.com", "a merchant", "Password1@"),
                            List.of("Username must be provided.")
                    ),
                    Arguments.of(
                            new User("", "username@email.com", "a merchant", "Password1@"),
                            List.of("Username must be provided.")
                    ),
                    Arguments.of(
                            new User("a".repeat(65), "username@email.com", "a merchant", "Password1@"),
                            List.of("Username can only contain up to 64 characters.")
                    ),
                    Arguments.of(
                            new User("user@name", "username@email.com", "a merchant", "Password1@"),
                            List.of("Username can only contain lower case letters, numbers, dots and underscores.")
                    ),
                    Arguments.of(
                            new User("username..", "username@email.com", "a merchant", "Password1@"),
                            List.of("Username cannot contain repeated dots.")
                    ),
                    Arguments.of(
                            new User("username", null, "a merchant", "Password1@"),
                            List.of("Email must be provided.")
                    ),
                    Arguments.of(
                            new User("username", "", "a merchant", "Password1@"),
                            List.of("Email must be provided.")
                    ),
                    Arguments.of(
                            new User("username", "username@", "a merchant", "Password1@"),
                            List.of("Email is invalid.")
                    ),
                    Arguments.of(
                            new User("username", "email.com", "a merchant", "Password1@"),
                            List.of("Email is invalid.")
                    ),
                    Arguments.of(
                            new User("username", "@email.com", "a merchant", "Password1@"),
                            List.of("Email is invalid.")
                    ),
                    Arguments.of(
                            new User("username", "username@email.com", "", "Password1@"),
                            List.of("Description must contain between 1 and 200 characters.")
                    ),
                    Arguments.of(
                            new User("username", "username@email.com", "a".repeat(201), "Password1@"),
                            List.of("Description must contain between 1 and 200 characters.")
                    ),
                    Arguments.of(
                            new User("username", "username@email.com", "a merchant", "Passw1"),
                            List.of("Password must be 8 or more characters in length.",
                                    "Password must contain 1 or more special characters.")
                    )
            );
        }
    }

    private static class InvalidUsernamesTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("a".repeat(65), "Username can only contain up to 64 characters.")
            );
        }
    }

    private static class InvalidPasswordChangeTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials("username", "Password1@"), "newPassword1"),
                            "Password must contain 1 or more special characters."),
                    Arguments.of(
                            new PasswordChange(null, "new_Password1"),
                            "Credentials must be provided."
                    ),
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials(null, "Password1@"), "new_Password1"),
                            "Username must be provided."
                    ),
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials("", "Password1@"), "new_Password1"),
                            "Username must be provided."
                    ),
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials("a".repeat(65), "Password1@"), "new_Password1"),
                            "Username can only contain up to 64 characters."
                    ),
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials("username", null), "new_Password1"),
                            "Password must be provided."
                    ),
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials("username", ""), "new_Password1"),
                            "Password must be provided."
                    ),
                    Arguments.of(
                            new PasswordChange(
                                    new UserCredentials("username", "a".repeat(17)), "new_Password1"),
                            "Password can only contain up to 16 characters."
                    )
            );
        }
    }

    private static class InvalidEmailChangeTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("username", "Password1@"), null),
                            "Email must be provided."),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("username", "Password1@"), ""),
                            "Email must be provided."),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("username", "Password1@"), "@email.com"),
                            "Email is invalid."),
                    Arguments.of(
                            new EmailChange(null, "new_email@email.com"),
                            "Credentials must be provided."
                    ),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials(null, "Password1@"), "new_email@email.com"),
                            "Username must be provided."
                    ),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("", "Password1@"), "new_email@email.com"),
                            "Username must be provided."
                    ),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("a".repeat(65), "Password1@"), "new_email@email.com"),
                            "Username can only contain up to 64 characters."
                    ),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("username", null), "new_email@email.com"),
                            "Password must be provided."
                    ),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("username", ""), "new_email@email.com"),
                            "Password must be provided."
                    ),
                    Arguments.of(
                            new EmailChange(
                                    new UserCredentials("username", "a".repeat(17)), "new_email@email.com"),
                            "Password can only contain up to 16 characters."
                    )
            );
        }
    }

    private static class InvalidUserInfoChangeTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            new UserInfoChange(
                                    null, new UserInfo("a")),
                            "Username must be provided."),
                    Arguments.of(
                            new UserInfoChange(
                                    "", new UserInfo("a")),
                            "Username must be provided."),
                    Arguments.of(
                            new UserInfoChange(
                                    "a".repeat(65), new UserInfo("a")),
                            "Username can only contain up to 64 characters."),
                    Arguments.of(
                            new UserInfoChange(
                                    "username", null),
                            "NewUserInfo must be provided."),
                    Arguments.of(
                            new UserInfoChange(
                                    "username", new UserInfo(null)),
                            "Description must be provided."),
                    Arguments.of(
                            new UserInfoChange(
                                    "username", new UserInfo("")),
                            "Description must be provided."),
                    Arguments.of(
                            new UserInfoChange(
                                    "username", new UserInfo("a".repeat(201))),
                            "Description must contain up to 200 characters.")
            );
        }
    }

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private UserService service;

    @ParameterizedTest
    @ArgumentsSource(ValidUserTestParameters.class)
    public void registersValidUser(User user) throws Exception {
        assertThat(mockMvc.post().uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .hasStatus(HttpStatus.CREATED)
                .body()
                .isEmpty();

        verify(service, times(1)).registerUser(user);
    }

    @Test
    public void failsToRegisterDuplicatedUser() throws Exception {
        User user = new User("_username19.", "username@email.com", "a merchant", "Password1@");

        doThrow(new DuplicatedUsernameException("_username19.")).when(service).registerUser(user);

        assertThat(mockMvc.post().uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .hasStatus(HttpStatus.CONFLICT)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.CONFLICT.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Duplicated Username"))
                .hasPathSatisfying("$.detail",
                path -> assertThat(path).isEqualTo("Username '_username19.' is already being used."));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUserTestParameters.class)
    public void failsToRegisterInvalidUser(User user, List<String> reasons) throws Exception {
        assertThat(mockMvc.post().uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Invalid Content"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("request content is invalid"))
                .hasPathSatisfying("$.reasons",
                        path -> assertThat(path)
                                .isInstanceOf(List.class).asArray()
                                .containsExactlyInAnyOrderElementsOf(reasons));

        verify(service, times(0)).registerUser(user);
    }

    @Test
    public void retrievesUserInfo() {
        UserInfo userInfo = new UserInfo("a merchant");

        when(service.retrieveUserInfo("username")).thenReturn(userInfo);

        assertThat(mockMvc.get().uri("/users/username/info"))
                .hasStatusOk()
                .hasHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(UserInfo.class)
                .isEqualTo(userInfo);

        verify(service, times(1)).retrieveUserInfo("username");
    }

    @Test
    public void failsToRetrieveInfoOfUnknownUser() {
        when(service.retrieveUserInfo("username")).thenThrow(new UsernameNotFoundException("username"));

        assertThat(mockMvc.get().uri("/users/username/info"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.NOT_FOUND.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Username Not Found"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("Username username wasn't found."));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUsernamesTestParameters.class)
    public void failsToRetrieveUserInfoWithInvalidUsername(String username, String reason) {

        assertThat(mockMvc.get().uri(String.format("/users/%s/info", username)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Invalid Content"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("request content is invalid"))
                .hasPathSatisfying("$.reasons",
                        path -> assertThat(path)
                                .isInstanceOf(List.class).asArray()
                                .hasSize(1)
                                .contains(reason));

        verify(service, times(0)).retrieveUserInfo(username);
    }

    @Test
    public void retrievesUserEmail() {
        UserEmail email = new UserEmail("username@email.com");

        when(service.retrieveUserEmail("username")).thenReturn(email);

        assertThat(mockMvc.get().uri("/users/username/email"))
                .hasStatusOk()
                .hasHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(UserEmail.class)
                .isEqualTo(email);

        verify(service, times(1)).retrieveUserEmail("username");
    }

    @Test
    public void failsToRetrieveEmailOfUnknownUser() {
        when(service.retrieveUserEmail("username")).thenThrow(new UsernameNotFoundException("username"));

        assertThat(mockMvc.get().uri("/users/username/email"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.NOT_FOUND.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Username Not Found"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("Username username wasn't found."));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUsernamesTestParameters.class)
    public void failsToRetrieveUserEmailWithInvalidUsername(String username, String reason) {
        assertThat(mockMvc.get().uri(String.format("/users/%s/email", username)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Invalid Content"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("request content is invalid"))
                .hasPathSatisfying("$.reasons",
                        path -> assertThat(path)
                                .isInstanceOf(List.class).asArray()
                                .hasSize(1)
                                .contains(reason));

        verify(service, times(0)).retrieveUserEmail(username);
    }

    @Test
    public void changesUserPassword() throws Exception {
        PasswordChange change = new PasswordChange(
                new UserCredentials("username", "Password1@"), "new_Password1@");

        assertThat(mockMvc.put().uri("/users/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.NO_CONTENT)
                .body()
                .isEmpty();

        verify(service, times(1)).updateUserPassword(change);
    }

    @Test
    public void failsToChangePasswordOfInvalidUser() throws Exception {
        PasswordChange change = new PasswordChange(
                new UserCredentials("username", "Password1@"), "new_Password1@");

        doThrow(new UserNotFoundException()).when(service).updateUserPassword(change);

        assertThat(mockMvc.put().uri("/users/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("User Not Found"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("Provided user wasn't found."));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidPasswordChangeTestParameters.class)
    public void failsToUpdatePasswordWithInvalidChange(PasswordChange change, String reason) throws Exception {
        assertThat(mockMvc.put().uri("/users/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Invalid Content"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("request content is invalid"))
                .hasPathSatisfying("$.reasons",
                        path -> assertThat(path)
                                .isInstanceOf(List.class).asArray()
                                .hasSize(1)
                                .contains(reason));
    }

    @Test
    public void changesUserEmail() throws Exception {
        EmailChange change = new EmailChange(
                new UserCredentials("username", "Password1@"), "new_email@email.com");

        assertThat(mockMvc.put().uri("/users/update/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.NO_CONTENT)
                .body()
                .isEmpty();

        verify(service, times(1)).updateUserEmail(change);
    }

    @Test
    public void failsToChangeEmailOfInvalidUser() throws Exception {
        EmailChange change = new EmailChange(
                new UserCredentials("username", "Password1@"), "new_email@email.com");

        doThrow(new UserNotFoundException()).when(service).updateUserEmail(change);

        assertThat(mockMvc.put().uri("/users/update/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("User Not Found"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("Provided user wasn't found."));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidEmailChangeTestParameters.class)
    public void failsToUpdateEmailWithInvalidChange(EmailChange change, String reason) throws Exception {
        assertThat(mockMvc.put().uri("/users/update/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Invalid Content"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("request content is invalid"))
                .hasPathSatisfying("$.reasons",
                        path -> assertThat(path)
                                .isInstanceOf(List.class).asArray()
                                .hasSize(1)
                                .contains(reason));
    }

    @Test
    public void changesUserInfo() throws Exception {
        UserInfoChange change = new UserInfoChange(
                "username", new UserInfo("new description"));

        assertThat(mockMvc.put().uri("/users/update/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.NO_CONTENT)
                .body()
                .isEmpty();

        verify(service, times(1)).updateUserInfo(change);
    }

    @Test
    public void failsToChangeInfoOfUnknownUser() throws Exception {
        UserInfoChange change = new UserInfoChange(
                "username", new UserInfo("new description"));

        doThrow(new UsernameNotFoundException("username")).when(service).updateUserInfo(change);

        assertThat(mockMvc.put().uri("/users/update/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.NOT_FOUND.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Username Not Found"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("Username username wasn't found."));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUserInfoChangeTestParameters.class)
    public void failsToUpdateUserInfoWithInvalidChange(UserInfoChange change, String reason) throws Exception {
        assertThat(mockMvc.put().uri("/users/update/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(change)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .bodyJson()
                .hasPathSatisfying("$.status",
                        path -> assertThat(path).isEqualTo(HttpStatus.BAD_REQUEST.value()))
                .hasPathSatisfying("$.title",
                        path -> assertThat(path).isEqualTo("Invalid Content"))
                .hasPathSatisfying("$.detail",
                        path -> assertThat(path).isEqualTo("request content is invalid"))
                .hasPathSatisfying("$.reasons",
                        path -> assertThat(path)
                                .isInstanceOf(List.class).asArray()
                                .hasSize(1)
                                .contains(reason));
    }
}
