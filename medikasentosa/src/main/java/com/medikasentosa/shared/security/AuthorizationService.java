package com.medikasentosa.shared.security;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.billing.entity.Billing;
import com.medikasentosa.features.billing.repository.BillingRepository;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaloperasi.entity.JadwalOperasi;
import com.medikasentosa.features.jadwaloperasi.repository.JadwalOperasiRepository;
import com.medikasentosa.features.laboratorium.entity.LabOrder;
import com.medikasentosa.features.laboratorium.repository.LabOrderRepository;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.features.radiologi.entity.Radiologi;
import com.medikasentosa.features.radiologi.repository.RadiologiRepository;
import com.medikasentosa.features.rawatinap.entity.RawatInap;
import com.medikasentosa.features.rawatinap.repository.RawatInapRepository;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.features.resep.entity.Resep;
import com.medikasentosa.features.resep.repository.ResepRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pemeriksaan otorisasi tingkat resource (ownership / care relationship).
 *
 * <p>SecurityConfig hanya mengatur otorisasi kasar berbasis URL. Service ini
 * melengkapi dengan pemeriksaan kepemilikan data agar pasien tidak dapat
 * mengakses rekam medis, tagihan, atau appointment milik pasien lain, dan
 * dokter hanya dapat mengakses pasien yang pernah ditanganinya.</p>
 *
 * <p>Seluruh method bersifat {@code readOnly} transaksional agar akses relasi
 * JPA (pasien → user, rekam medis → dokter) tetap aman dari lazy-loading
 * exception saat dipanggil dari controller.</p>
 */
@Service
@Transactional(readOnly = true)
public class AuthorizationService {

    private final UserRepository userRepository;
    private final PasienRepository pasienRepository;
    private final DokterRepository dokterRepository;
    private final AppointmentRepository appointmentRepository;
    private final RekamMedisRepository rekamMedisRepository;
    private final BillingRepository billingRepository;
    private final LabOrderRepository labOrderRepository;
    private final RadiologiRepository radiologiRepository;
    private final ResepRepository resepRepository;
    private final RawatInapRepository rawatInapRepository;
    private final JadwalOperasiRepository jadwalOperasiRepository;

    public AuthorizationService(UserRepository userRepository,
                                PasienRepository pasienRepository,
                                DokterRepository dokterRepository,
                                AppointmentRepository appointmentRepository,
                                RekamMedisRepository rekamMedisRepository,
                                BillingRepository billingRepository,
                                LabOrderRepository labOrderRepository,
                                RadiologiRepository radiologiRepository,
                                ResepRepository resepRepository,
                                RawatInapRepository rawatInapRepository,
                                JadwalOperasiRepository jadwalOperasiRepository) {
        this.userRepository = userRepository;
        this.pasienRepository = pasienRepository;
        this.dokterRepository = dokterRepository;
        this.appointmentRepository = appointmentRepository;
        this.rekamMedisRepository = rekamMedisRepository;
        this.billingRepository = billingRepository;
        this.labOrderRepository = labOrderRepository;
        this.radiologiRepository = radiologiRepository;
        this.resepRepository = resepRepository;
        this.rawatInapRepository = rawatInapRepository;
        this.jadwalOperasiRepository = jadwalOperasiRepository;
    }

    // ─── Principal helpers ────────────────────────────────────────────────

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Autentikasi diperlukan");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Pengguna tidak ditemukan"));
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    public String currentUserEmail() {
        return currentUser().getEmail();
    }

    public boolean isAdmin() {
        return currentUser().getRole() == Role.ADMIN;
    }

    public boolean isDokter() {
        return currentUser().getRole() == Role.DOKTER;
    }

    /** ID profil dokter milik pengguna yang sedang login. */
    public Long currentDokterId() {
        return currentDokter().getId();
    }

    public boolean isPasien() {
        return currentUser().getRole() == Role.PASIEN;
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Aksi ini hanya untuk admin");
        }
    }

    public void requireStaff() {
        Role role = currentUser().getRole();
        if (role != Role.ADMIN && role != Role.DOKTER) {
            throw new AccessDeniedException("Aksi ini hanya untuk tenaga medis");
        }
    }

    /** Pemilik akun sendiri atau admin. */
    public void requireSelfOrAdmin(Long userId) {
        if (isAdmin()) {
            return;
        }
        if (!currentUserId().equals(userId)) {
            throw new AccessDeniedException("Anda hanya dapat mengakses akun sendiri");
        }
    }

    /** Profil dokter sendiri atau admin. */
    public void requireDokterSelfOrAdmin(Long dokterId) {
        if (isAdmin()) {
            return;
        }
        Dokter dokter = currentDokter();
        if (!dokter.getId().equals(dokterId)) {
            throw new AccessDeniedException("Anda hanya dapat mengakses data dokter sendiri");
        }
    }

    // ─── Pasien ───────────────────────────────────────────────────────────

    /** Admin, pemilik profil, atau dokter yang pernah menangani pasien. */
    public void requirePasienAccess(Long pasienId) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        Pasien pasien = pasienRepository.findById(pasienId)
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));

        if (user.getRole() == Role.PASIEN) {
            if (pasien.getUser() != null && user.getId().equals(pasien.getUser().getId())) {
                return;
            }
            throw new AccessDeniedException("Anda tidak berhak mengakses data pasien ini");
        }

        if (user.getRole() == Role.DOKTER) {
            Dokter dokter = currentDokter();
            if (hasCareRelationship(pasienId, dokter.getId())) {
                return;
            }
            throw new AccessDeniedException("Pasien ini bukan pasien Anda");
        }

        throw new AccessDeniedException("Akses ditolak");
    }

    /** Hanya admin atau pasien pemilik profil (dokter tidak boleh mengubah demografi). */
    public void requirePasienWrite(Long pasienId) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        Pasien pasien = pasienRepository.findById(pasienId)
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));
        if (user.getRole() == Role.PASIEN && pasien.getUser() != null
                && user.getId().equals(pasien.getUser().getId())) {
            return;
        }
        throw new AccessDeniedException("Anda tidak berhak mengubah data pasien ini");
    }

    /** Validasi bahwa userId pada body adalah milik sendiri (kecuali admin). */
    public void requirePasienCreationForUser(Long userId) {
        if (isAdmin()) {
            return;
        }
        if (!currentUserId().equals(userId)) {
            throw new AccessDeniedException("Anda hanya dapat membuat profil pasien untuk akun sendiri");
        }
    }

    public void requirePasienOfUserAccess(Long userId) {
        if (isAdmin()) {
            return;
        }
        if (!currentUserId().equals(userId)) {
            throw new AccessDeniedException("Anda hanya dapat mengakses profil pasien akun sendiri");
        }
    }

    // ─── Appointment / antrian ───────────────────────────────────────────

    public void requireAppointmentAccess(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PASIEN) {
            if (appointment.getPasien() != null && appointment.getPasien().getUser() != null
                    && user.getId().equals(appointment.getPasien().getUser().getId())) {
                return;
            }
            throw new AccessDeniedException("Appointment ini bukan milik Anda");
        }
        if (user.getRole() == Role.DOKTER) {
            Dokter dokter = currentDokter();
            if (appointment.getDokter() != null && dokter.getId().equals(appointment.getDokter().getId())) {
                return;
            }
            throw new AccessDeniedException("Appointment ini bukan milik Anda");
        }
        throw new AccessDeniedException("Akses ditolak");
    }

    /** Pembuatan appointment: admin atau pasien untuk dirinya sendiri. */
    public void requireAppointmentCreate(Long pasienId) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PASIEN) {
            Pasien own = pasienRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new AccessDeniedException("Profil pasien tidak ditemukan"));
            if (own.getId().equals(pasienId)) {
                return;
            }
            throw new AccessDeniedException("Anda hanya dapat membuat appointment untuk diri sendiri");
        }
        throw new AccessDeniedException("Hanya pasien atau admin yang dapat membuat appointment");
    }

    /** Update status: admin, dokter penanggung jawab, atau pasien (hanya BATAL). */
    public void requireAppointmentStatusChange(Long appointmentId, StatusAppointment newStatus) {
        Appointment appointment = findAppointment(appointmentId);
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.DOKTER) {
            Dokter dokter = currentDokter();
            if (appointment.getDokter() != null && dokter.getId().equals(appointment.getDokter().getId())) {
                return;
            }
            throw new AccessDeniedException("Anda bukan dokter penanggung jawab appointment ini");
        }
        if (user.getRole() == Role.PASIEN) {
            if (appointment.getPasien() == null || appointment.getPasien().getUser() == null
                    || !user.getId().equals(appointment.getPasien().getUser().getId())) {
                throw new AccessDeniedException("Appointment ini bukan milik Anda");
            }
            if (newStatus != StatusAppointment.BATAL) {
                throw new AccessDeniedException("Pasien hanya dapat membatalkan appointment sendiri");
            }
            return;
        }
        throw new AccessDeniedException("Akses ditolak");
    }

    /** Dokter penanggung jawab appointment atau admin (untuk menulis rekam medis). */
    public void requireAppointmentWrite(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.DOKTER
                && appointment.getDokter() != null
                && currentDokter().getId().equals(appointment.getDokter().getId())) {
            return;
        }
        throw new AccessDeniedException("Hanya dokter penanggung jawab yang dapat mengubah appointment ini");
    }

    // ─── Rekam medis & penunjang ──────────────────────────────────────────

    public void requireRekamMedisAccess(Long rekamMedisId) {
        RekamMedis rm = findRekamMedis(rekamMedisId);
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PASIEN) {
            if (rm.getPasien() != null && rm.getPasien().getUser() != null
                    && user.getId().equals(rm.getPasien().getUser().getId())) {
                return;
            }
            throw new AccessDeniedException("Rekam medis ini bukan milik Anda");
        }
        if (user.getRole() == Role.DOKTER) {
            if (rm.getDokter() != null && currentDokter().getId().equals(rm.getDokter().getId())) {
                return;
            }
            throw new AccessDeniedException("Rekam medis ini bukan milik Anda");
        }
        throw new AccessDeniedException("Akses ditolak");
    }

    public void requireRekamMedisWrite(Long rekamMedisId) {
        RekamMedis rm = findRekamMedis(rekamMedisId);
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.DOKTER
                && rm.getDokter() != null
                && currentDokter().getId().equals(rm.getDokter().getId())) {
            return;
        }
        throw new AccessDeniedException("Hanya dokter penanggung jawab yang dapat mengubah rekam medis ini");
    }

    public void requireRekamMedisOfPasien(Long pasienId) {
        requirePasienAccess(pasienId);
    }

    public void requireLabOrderAccess(Long labOrderId) {
        requireRekamMedisAccess(findLabOrder(labOrderId).getRekamMedis().getId());
    }

    public void requireLabOrderWrite(Long labOrderId) {
        requireRekamMedisWrite(findLabOrder(labOrderId).getRekamMedis().getId());
    }

    public void requireRadiologiAccess(Long radiologiId) {
        requireRekamMedisAccess(findRadiologi(radiologiId).getRekamMedis().getId());
    }

    public void requireRadiologiWrite(Long radiologiId) {
        requireRekamMedisWrite(findRadiologi(radiologiId).getRekamMedis().getId());
    }

    public void requireResepAccess(Long resepId) {
        requireRekamMedisAccess(findResep(resepId).getRekamMedis().getId());
    }

    public void requireResepWrite(Long resepId) {
        requireRekamMedisWrite(findResep(resepId).getRekamMedis().getId());
    }

    public void requireResepOfRekamMedis(Long rekamMedisId) {
        requireRekamMedisAccess(rekamMedisId);
    }

    // ─── Billing / pembayaran ─────────────────────────────────────────────

    public void requireBillingAccess(Long billingId) {
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing tidak ditemukan"));
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PASIEN && billing.getPasien() != null
                && billing.getPasien().getUser() != null
                && user.getId().equals(billing.getPasien().getUser().getId())) {
            return;
        }
        throw new AccessDeniedException("Tagihan ini bukan milik Anda");
    }

    public void requireBillingOfPasien(Long pasienId) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        Pasien own = pasienRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Profil pasien tidak ditemukan"));
        if (!own.getId().equals(pasienId)) {
            throw new AccessDeniedException("Anda hanya dapat mengakses tagihan sendiri");
        }
    }

    public void requireBillingPayment(Long billingId) {
        requireBillingAccess(billingId);
    }

    // ─── Rawat inap / operasi ─────────────────────────────────────────────

    public void requireRawatInapAccess(Long rawatInapId) {
        RawatInap rawatInap = rawatInapRepository.findById(rawatInapId)
                .orElseThrow(() -> new ResourceNotFoundException("Rawat inap tidak ditemukan"));
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PASIEN && rawatInap.getPasien() != null
                && rawatInap.getPasien().getUser() != null
                && user.getId().equals(rawatInap.getPasien().getUser().getId())) {
            return;
        }
        if (user.getRole() == Role.DOKTER && rawatInap.getDokter() != null
                && currentDokter().getId().equals(rawatInap.getDokter().getId())) {
            return;
        }
        throw new AccessDeniedException("Data rawat inap ini bukan milik Anda");
    }

    public void requireRawatInapWrite(Long rawatInapId) {
        RawatInap rawatInap = rawatInapRepository.findById(rawatInapId)
                .orElseThrow(() -> new ResourceNotFoundException("Rawat inap tidak ditemukan"));
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.DOKTER && rawatInap.getDokter() != null
                && currentDokter().getId().equals(rawatInap.getDokter().getId())) {
            return;
        }
        throw new AccessDeniedException("Hanya dokter penanggung jawab yang dapat mengubah rawat inap ini");
    }

    public void requireRawatInapOfPasien(Long pasienId) {
        requirePasienAccess(pasienId);
    }

    public void requireOperasiAccess(Long operasiId) {
        JadwalOperasi operasi = jadwalOperasiRepository.findById(operasiId)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal operasi tidak ditemukan"));
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PASIEN && operasi.getPasien() != null
                && operasi.getPasien().getUser() != null
                && user.getId().equals(operasi.getPasien().getUser().getId())) {
            return;
        }
        if (user.getRole() == Role.DOKTER && operasi.getDokter() != null
                && currentDokter().getId().equals(operasi.getDokter().getId())) {
            return;
        }
        throw new AccessDeniedException("Jadwal operasi ini bukan milik Anda");
    }

    public void requireOperasiOfPasien(Long pasienId) {
        requirePasienAccess(pasienId);
    }

    public void requireOperasiOfDokter(Long dokterId) {
        requireDokterSelfOrAdmin(dokterId);
    }

    // ─── Internal helpers ─────────────────────────────────────────────────

    private Dokter currentDokter() {
        return dokterRepository.findByUserId(currentUserId())
                .orElseThrow(() -> new AccessDeniedException("Profil dokter tidak ditemukan"));
    }

    private boolean hasCareRelationship(Long pasienId, Long dokterId) {
        return appointmentRepository.existsByPasienIdAndDokterId(pasienId, dokterId)
                || rekamMedisRepository.existsByPasienIdAndDokterId(pasienId, dokterId)
                || rawatInapRepository.existsByPasienIdAndDokterId(pasienId, dokterId);
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));
    }

    private RekamMedis findRekamMedis(Long id) {
        return rekamMedisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rekam medis tidak ditemukan"));
    }

    private LabOrder findLabOrder(Long id) {
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order laboratorium tidak ditemukan"));
        if (order.getRekamMedis() == null) {
            throw new AccessDeniedException("Order laboratorium tidak memiliki rekam medis");
        }
        return order;
    }

    private Radiologi findRadiologi(Long id) {
        Radiologi radiologi = radiologiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order radiologi tidak ditemukan"));
        if (radiologi.getRekamMedis() == null) {
            throw new AccessDeniedException("Order radiologi tidak memiliki rekam medis");
        }
        return radiologi;
    }

    private Resep findResep(Long id) {
        Resep resep = resepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resep tidak ditemukan"));
        if (resep.getRekamMedis() == null) {
            throw new AccessDeniedException("Resep tidak memiliki rekam medis");
        }
        return resep;
    }
}
