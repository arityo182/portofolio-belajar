package com.medikasentosa.shared.audit;

import com.medikasentosa.shared.security.ClientIpResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Audit log untuk akses data medis sensitif.
 *
 * <p>Ditulis ke logger terpisah {@code AUDIT} agar dapat diarahkan ke berkas
 * append-only atau SIEM tanpa bercampur dengan log aplikasi biasa. Setiap
 * entri memuat aktor, aksi, jenis resource, ID, dan alamat IP pemanggil.</p>
 */
@Component
public class AuditLogger {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void record(String action, String resourceType, Object resourceId) {
        String actor = "anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            actor = authentication.getName();
        }
        audit.info("action={} resource={} id={} actor={} ip={}",
                action, resourceType, resourceId, actor, clientIp());
    }

    private String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return ClientIpResolver.resolve(attributes.getRequest());
        }
        return "-";
    }
}
