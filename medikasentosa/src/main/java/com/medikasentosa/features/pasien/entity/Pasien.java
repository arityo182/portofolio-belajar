package com.medikasentosa.features.pasien.entity;

import com.medikasentosa.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity yang merepresentasikan profil pasien rumah sakit.
 * Setiap pasien terhubung ke satu akun {@link User} (relasi satu-ke-satu) dan
 * memiliki nomor rekam medis unik yang digenerate otomatis.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "pasien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Pasien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true)
    private String noRekamMedis;

    @Column(nullable = false)
    private String nik;

    @Column(nullable = false)
    private LocalDate tanggalLahir;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JenisKelamin jenisKelamin;

    @Column(columnDefinition = "TEXT")
    private String alamat;

    @Column(nullable = false)
    private String noHp;

    private String golDarah;
}
