package org.example.chatapp.security.user;

import jakarta.persistence.*;
import lombok.*;
import org.example.chatapp.model.ChatRoom;
import org.example.chatapp.model.FriendShip;
import org.example.chatapp.model.FriendRequest;
import org.example.chatapp.security.token.Token;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Model representing the User entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_generator")
  @SequenceGenerator(
          name = "users_generator",
          sequenceName = "users_id_seq",
          allocationSize = 1,
          initialValue = 22
  )
  private Long id;

  private String firstname;
  private String lastname;

  @Getter
  private String userName = firstname + " " + lastname;

  private String email;
  private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(mappedBy = "user")
  private List<Token> tokens;

  @OneToMany(mappedBy = "sender")
  private List<ChatRoom> sentChatRooms;

  @OneToMany(mappedBy = "recipient")
  private List<ChatRoom> receivedChatRooms;

  @OneToMany(mappedBy = "user1")
  private List<FriendShip> friendshipsInitiated;

  @OneToMany(mappedBy = "user2")
  private List<FriendShip> friendshipsReceived;

  @OneToMany(mappedBy = "sender")
  private List<FriendRequest> sentFriendRequests;

  @OneToMany(mappedBy = "recipient")
  private List<FriendRequest> receivedFriendRequests;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @PrePersist
  protected void onCreate() {
    if (this.role == null) {
      this.role = Role.USER;
    }
    if (this.userName == null || this.userName.isBlank()) {
      this.userName = (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
      this.userName = this.userName.trim();
    }
  }

  public String getName(){
    return this.userName;
  }


}
