package com.chirko.onLine.post.entity;

import com.chirko.onLine.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private User user;
    private String text;
    @Column(nullable = false)
    private Timestamp createdDate;
    @Column(nullable = false)
    private Timestamp modifiedDate;
}
