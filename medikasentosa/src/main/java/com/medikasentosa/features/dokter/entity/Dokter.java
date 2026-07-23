package com.medikasentosa.features.dokter.entity;

import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.poli.entity.Poli;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity yang merepresentasikan profil dokter rumah sakit.
 * Setiap dokter terhubung ke satu akun {@link User} berperan DOKTER dan satu {@link Poli}.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "dokter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Dokter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "poli_id")
    private Poli poli;

    @Column(nullable = false)
    private String noStr;

    @Column(nullable = false)
    private String spesialisasi;

    @Column(nullable = false)
    private String noHp;

    @Column(nullable = false)
    private Boolean isActive;
}
