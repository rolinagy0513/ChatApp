package org.example.chatapp.repository;

import org.example.chatapp.model.FriendShip;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FriendShip entities.
 * Provides methods to check existence and retrieve pending friend requests.
 */
public interface FriendShipRepository extends JpaRepository<FriendShip, Long> {

    /**
     * Retrieves all friendships involving the given user ID, eagerly fetching the associated users.
     *
     * @param userId the ID of the user whose friendships are being retrieved
     * @return a list of FriendShip entities where the user is either user1 or user2
     */
    @EntityGraph(attributePaths = {"user1", "user2"})
    @Query("SELECT f FROM FriendShip f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<FriendShip> findByUserIdWithUsers(@Param("userId") Long userId);

    /**
     * Retrieves the IDs of all friends for the given user.
     * Returns the opposite user in each friendship.
     *
     * @param userId the ID of the user for whom to find friend IDs
     * @return a list of user IDs representing the user's friends
     */
    @Query("SELECT CASE WHEN f.user1.id = :userId THEN f.user2.id ELSE f.user1.id END " +
            "FROM FriendShip f WHERE :userId IN (f.user1.id, f.user2.id)")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);

    /**
     * Finds any existing friendship between two users, regardless of the order in which they are stored.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return a list containing the FriendShip between the two users if it exists
     */
    @Query("SELECT f FROM FriendShip f WHERE " +
            "(f.user1.id = :userId1 AND f.user2.id = :userId2) OR " +
            "(f.user1.id = :userId2 AND f.user2.id = :userId1)")
    List<FriendShip> findFriendshipBetweenUsers(@Param("userId1") Long userId1,
                                                    @Param("userId2") Long userId2);
    /**
     * Checks whether a friendship exists between two users.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return true if a friendship exists, false otherwise
     */
    @Query("SELECT COUNT(f) > 0 FROM FriendShip f WHERE " +
            "(f.user1.id = :userId1 AND f.user2.id = :userId2) OR " +
            "(f.user1.id = :userId2 AND f.user2.id = :userId1)")
    boolean existsFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Retrieves the timestamp of when the friendship was created between two users.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return an Optional containing the LocalDateTime of friendship creation, or empty if not found
     */
    @Query("SELECT f.createdAt FROM FriendShip f WHERE " +
            "(f.user1.id = :userId1 AND f.user2.id = :userId2) OR " +
            "(f.user1.id = :userId2 AND f.user2.id = :userId1)")
    Optional<LocalDateTime> findFriendshipCreatedAtBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

}


