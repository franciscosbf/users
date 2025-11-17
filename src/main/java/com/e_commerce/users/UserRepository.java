package com.e_commerce.users;

import com.e_commerce.users.model.User;
import com.e_commerce.users.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByUsername(String username);

    Optional<UserInfo> findOptionalUserInfoByUsername(String username);

    @Query("select email from User where username = ?1")
    Optional<String> findOptionalUserEmailByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.password = ?3 where u.username = ?1 and u.password = ?2")
    int updateUserPassword(String username, String oldPassword, String newPassword);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.email = ?3 where u.username = ?1 and u.password = ?2")
    int updateUserEmail(String username, String password, String email);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.description = :#{#userInfo.description()} where u.username = ?1")
    int updateUserInfo(String username, @Param("userInfo") UserInfo userInfo);
}
