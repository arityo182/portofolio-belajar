package com.medikasentosa.features.billing.service;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.billing.dto.BillingItemRequest;
import com.medikasentosa.features.billing.dto.BillingResponse;
import com.medikasentosa.features.billing.entity.Billing;
import com.medikasentosa.features.billing.entity.BillingItem;
import com.medikasentosa.features.billing.entity.StatusBilling;
import com.medikasentosa.features.billing.repository.BillingItemRepository;
import com.medikasentosa.features.billing.repository.BillingRepository;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.poli.entity.Poli;
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
class BillingServiceTest {

    @Mock
    private BillingRepository billingRepo;
    @Mock
    private BillingItemRepository itemRepo;
    @Mock
    private AppointmentRepository appointmentRepo;

    @InjectMocks
    private BillingService billingService;

    private Appointment appointment;
    private Pasien pasien;
    private Billing billing;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(9L).nama("Agus Salim").email("agus@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(8L).nama("dr. Fatimah Zahra").email("fatimah.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(9L).nama("Poli Mata").deskripsi("Spesialis Mata").isActive(true).build();
        Dokter dokter = Dokter.builder().id(16L).user(userDokter).poli(poli)
                .noStr("STR-339900112").spesialisasi("Spesialis Mata").noHp("082299003311").isActive(true).build();

        pasien = Pasien.builder().id(7L).user(userPasien).noRekamMedis("RM-2024-000007")
                .nik("3275012311990007").tanggalLahir(LocalDate.of(1980, 1, 15))
                .jenisKelamin(JenisKelamin.L).alamat("Jl. Flamboyan No. 2, Malang")
                .noHp("081388990011").golDarah("B").build();

        appointment = Appointment.builder().id(400L).pasien(pasien).dokter(dokter)
                .tanggal(LocalDate.of(2024, 7,26)).status(StatusAppointment.SELESAI).build();

        billing = Billing.builder().id(1L).pasien(pasien).totalHarga(BigDecimal.valueOf(150000))
                .status(StatusBilling.BELUM_BAYAR).tanggalTagihan(LocalDate.now()).build();
    }

    @Test
    void generateDariAppointment_createsBillingWithItem() {
        when(appointmentRepo.findById(400L)).thenReturn(Optional.of(appointment));
        when(billingRepo.save(any(Billing.class))).thenAnswer(inv -> {
            Billing b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(itemRepo.save(any(BillingItem.class))).thenAnswer(inv -> inv.getArgument(0));

        BillingResponse response = billingService.generateDariAppointment(400L);

        assertThat(response.status()).isEqualTo(StatusBilling.BELUM_BAYAR);
        assertThat(response.totalHarga()).isEqualTo(BigDecimal.valueOf(150000));
        verify(billingRepo).save(any(Billing.class));
        verify(itemRepo).save(any(BillingItem.class));
    }

    @Test
    void tambahItem_addsItemAndRecalculates() {
        BillingItemRequest itemReq = new BillingItemRequest("Obat Tetes Mata", 2, BigDecimal.valueOf(35000));
        when(billingRepo.findById(1L)).thenReturn(Optional.of(billing));
        when(itemRepo.save(any(BillingItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepo.findByBillingId(1L)).thenReturn(List.of(
                BillingItem.builder().id(1L).billing(billing).deskripsi("Konsultasi")
                        .jumlah(1).hargaSatuan(BigDecimal.valueOf(150000)).subtotal(BigDecimal.valueOf(150000)).build(),
                BillingItem.builder().id(2L).billing(billing).deskripsi("Obat Tetes Mata")
                        .jumlah(2).hargaSatuan(BigDecimal.valueOf(35000)).subtotal(BigDecimal.valueOf(70000)).build()
        ));

        BillingResponse response = billingService.tambahItem(1L, itemReq);

        assertThat(response.totalHarga()).isEqualTo(BigDecimal.valueOf(220000));
    }

    @Test
    void getAll_returnsAllBilling() {
        when(billingRepo.findAll()).thenReturn(List.of(billing));
        when(itemRepo.findByBillingId(1L)).thenReturn(List.of());

        List<BillingResponse> result = billingService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getByPasien_returnsPasienBilling() {
        when(billingRepo.findByPasienId(7L)).thenReturn(List.of(billing));
        when(itemRepo.findByBillingId(1L)).thenReturn(List.of());

        List<BillingResponse> result = billingService.getByPasien(7L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPasien()).isEqualTo("Agus Salim");
    }

    @Test
    void batalkan_setsStatusDibatalkan() {
        when(billingRepo.findById(1L)).thenReturn(Optional.of(billing));

        BillingResponse response = billingService.batalkan(1L);

        assertThat(response.status()).isEqualTo(StatusBilling.DIBATALKAN);
        verify(billingRepo).save(billing);
    }
}
