package zw.ac.uz.dpdms.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Null for NATIONAL_VIEWER and PROVINCIAL_ADMIN.
     * Required for WARD_RECORDER and PROVINCIAL_SUPERVISOR.
     */
    @Enumerated(EnumType.STRING)
    private Hazard hazard;

    /**
     * Only set for WARD_RECORDER - the single ward this user may
     * capture records for. Null for every other role.
     */
    private String ward;

    private String province;

    @Column(nullable = false)
    private boolean enabled = true;
}
