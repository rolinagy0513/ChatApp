package org.example.chatapp.security.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;

/**
 * Repository interface for managing User entities.
 * Provides methods to search the user by email, ID.
 * And a flexible search query that can retrieve users from their usernames
 */
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  List<User> findAllByIdNot(Long id);

  @Query(
          value = """
    SELECT * FROM _user 
    WHERE 
        id <> :currentUserId AND (
            LOWER(REPLACE(CONCAT(firstname, lastname), ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:query, ' ', '')))
            OR
            LOWER(REPLACE(CONCAT(lastname, firstname), ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:query, ' ', '')))
        )
    """,
          nativeQuery = true
  )
  List<User> flexibleSearch(
          @Param("query") String query,
          @Param("currentUserId") Long currentUserId
  );


}
