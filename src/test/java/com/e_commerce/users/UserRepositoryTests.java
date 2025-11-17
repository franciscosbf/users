package com.e_commerce.users;

import com.e_commerce.users.model.User;
import com.e_commerce.users.model.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(TestcontainersConfiguration.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.jakarta.persistence.validation.mode=none"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTests {
    @Autowired
    private UserRepository repository;

    private Long insertedUserId;

    @BeforeEach
    public void validUserIsInserted() {
        User user = new User("username", "username@email.com", "a merchant", "Password1@");
        insertedUserId = repository.save(user).getId();
    }

    @Test
    public void userInfoFoundByUsername() {
        assertThat(repository.findOptionalUserInfoByUsername("username"))
                .hasValue(new UserInfo("a merchant"));
    }

    @Test
    public void userEmailFoundByUsername() {
        assertThat(repository.findOptionalUserEmailByUsername("username"))
                .hasValue("username@email.com");
    }

    @Test
    public void passwordIsUpdated() {
        assertThat(repository.updateUserPassword("username", "Password1@", "new_Password1@"))
                .isEqualTo(1);

        User user = repository.getReferenceById(insertedUserId);
        assertThat(user.getPassword()).isEqualTo("new_Password1@");
    }

    @Test
    public void emailIsUpdated() {
        assertThat(repository.updateUserEmail("username", "Password1@", "new_username@email.com"))
                .isEqualTo(1);

        User user = repository.getReferenceById(insertedUserId);
        assertThat(user.getEmail()).isEqualTo("new_username@email.com");
    }

    @Test
    public void userInfoIsUpdated() {
        UserInfo userInfo = new UserInfo("blablabla");

        assertThat(repository.updateUserInfo("username", userInfo)).isEqualTo(1);

        User user = repository.getReferenceById(insertedUserId);
        assertThat(user.getDescription()).isEqualTo(userInfo.description());
    }

    @Test
    public void usernameMustBeUnique() {
        repository.save(new User("username", "username@email.com", "a merchant", "Password1@"));
        assertThrows(DataIntegrityViolationException.class, repository::flush);
    }

    @Test
    public void emailCannotBeNull() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(new User("new_username", null, "a merchant", "Password1@")));
    }

    @Test
    public void passwordCannotBeNull() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(new User("new_username", "username@email.com", "a merchant", null)));
    }

    @Test
    public void descriptionCanBeNull() {
        repository.save(new User("new_username", "username@email.com", null, "Password1@"));
    }
}
