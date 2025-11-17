package com.e_commerce.users;

import com.e_commerce.users.events.EventSender;
import com.e_commerce.users.events.EmailUpdate;
import com.e_commerce.users.exceptions.DuplicatedUsernameException;
import com.e_commerce.users.exceptions.UserNotFoundException;
import com.e_commerce.users.exceptions.UsernameNotFoundException;
import com.e_commerce.users.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EventSender eventSender;

    public UserService(UserRepository repository,
                       PasswordEncoder passwordEncoder,
                       EventSender eventSender) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.eventSender = eventSender;
    }

    @Transactional
    public void registerUser(User user) {
        if (repository.existsUserByUsername(user.getUsername())) {
            throw new DuplicatedUsernameException(user.getUsername());
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        repository.save(user);

        log.info("{} was registered with success", user);
    }

    public UserInfo retrieveUserInfo(String username) {
        return repository.findOptionalUserInfoByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public UserEmail retrieveUserEmail(String username) {
        return repository.findOptionalUserEmailByUsername(username)
                .map(UserEmail::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public void updateUserPassword(PasswordChange change) {
        UserCredentials credentials = change.credentials();

        String encodedCurrentPassword = passwordEncoder.encode(credentials.password());
        String encodedNewPassword = passwordEncoder.encode(change.password());

        if (repository.updateUserPassword(credentials.username(), encodedCurrentPassword, encodedNewPassword) == 0) {
            throw new UserNotFoundException();
        }

        log.info("Password of user {} password was updated with success", credentials.username());
    }

    @Transactional(rollbackFor = {AmqpException.class})
    public void updateUserEmail(EmailChange change) {
        UserCredentials credentials = change.credentials();

        String encodedPassword = passwordEncoder.encode(credentials.password());

        if (repository.updateUserEmail(credentials.username(), encodedPassword, change.email()) == 0) {
            throw new UserNotFoundException();
        }

        EmailUpdate update = new EmailUpdate(credentials.username(), change.email());

        eventSender.sendEmailUpdate(update);

        log.info("Email of user {} was updated with success", credentials.username());
    }

    public void updateUserInfo(UserInfoChange change) {
        if (repository.updateUserInfo(change.username(), change.userInfo()) == 0) {
            throw new UsernameNotFoundException(change.username());
        }

        log.info("Info of user {} was updated with success", change.username());
    }
}
