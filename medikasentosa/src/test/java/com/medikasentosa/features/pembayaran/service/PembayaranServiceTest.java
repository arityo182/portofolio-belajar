package com.medikasentosa.features.pembayaran.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.billing.entity.Billing;
import com.medikasentosa.features.billing.entity.StatusBilling;
import com.medikasentosa.features.billing.repository.BillingRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pembayaran.dto.PembayaranRequest;
import com.medikasentosa.features.pembayaran.dto.PembayaranResponse;
import com.medikasentosa.features.pembayaran.entity.MetodePembayaran;
import com.medikasentosa.features.pembayaran.entity.Pembayaran;
import com.medikasentosa.features.pembayaran.repository.PembayaranRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PembayaranServiceTest {

    @Mock
    private PembayaranRepository repo;
    @Mock
    private BillingRepository billingRepo;

    @InjectMocks
    private PembayaranService pembayaranService;

    private Billing billing;
    private Pembayaran pembayaran;
    private PembayaranRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(10L).nama("Novi Anggraini").email("novi@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        Pasien pasien = Pasien.builder().id(8L).user(userPasien).noRekamMedis("RM-2024-000008")
                .nik("3201016003920008").tanggalLahir(LocalDate.of(1992, 3, 6))
                .jenisKelamin(JenisKelamin.P).alamat("Jl. Teratai No. 11, Bandung")
                .noHp("081394455667").golDarah("AB").build();

        billing = Billing.builder().id(1L).pasien(pasien).totalHarga(BigDecimal.valueOf(250000))
                .status(StatusBilling.BELUM_BAYAR).tanggalTagihan(LocalDate.now()).build();

        pembayaran = Pembayaran.builder().id(1L).billing(billing)
                .metodePembayaran(MetodePembayaran.TRANSFER).jumlahBayar(BigDecimal.valueOf(250000))
                .buktiTransfer("bukti-transfer.jpg").build();

        request = new PembayaranRequest(1L, MetodePembayaran.TRANSFER, BigDecimal.valueOf(250000), "bukti-transfer.jpg");
    }

    @Test
    void proses_success_setsBillingLunas() {
        when(billingRepo.findById(1L)).thenReturn(Optional.of(billing));
        when(repo.save(any(Pembayaran.class))).thenAnswer(inv -> {
            Pembayaran p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PembayaranResponse response = pembayaranService.proses(request);

        assertThat(response.metodePembayaran()).isEqualTo(MetodePembayaran.TRANSFER);
        assertThat(billing.getStatus()).isEqualTo(StatusBilling.LUNAS);
        verify(billingRepo).save(billing);
        verify(repo).save(any(Pembayaran.class));
    }

    @Test
    void proses_fails_billingAlreadyLunas() {
        billing.setStatus(StatusBilling.LUNAS);
        when(billingRepo.findById(1L)).thenReturn(Optional.of(billing));

        assertThatThrownBy(() -> pembayaranService.proses(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Billing sudah LUNAS");

        verify(repo, never()).save(any());
    }

    @Test
    void proses_fails_amountLessThanTotal() {
        PembayaranRequest kurang = new PembayaranRequest(1L, MetodePembayaran.TUNAI,
                BigDecimal.valueOf(100000), null);
        when(billingRepo.findById(1L)).thenReturn(Optional.of(billing));

        assertThatThrownBy(() -> pembayaranService.proses(kurang))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Jumlah bayar kurang dari total tagihan (Rp 250000)");

        verify(repo, never()).save(any());
    }

    @Test
    void getByBilling_returnsPembayaran() {
        when(repo.findByBillingId(1L)).thenReturn(Optional.of(pembayaran));

        PembayaranResponse response = pembayaranService.getByBilling(1L);

        assertThat(response.jumlahBayar()).isEqualTo(BigDecimal.valueOf(250000));
    }

    @Test
    void getAll_returnsAllPembayaran() {
        when(repo.findAll()).thenReturn(List.of(pembayaran));

        List<PembayaranResponse> result = pembayaranService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPasien()).isEqualTo("Novi Anggraini");
    }
}
