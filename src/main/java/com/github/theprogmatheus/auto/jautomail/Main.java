package com.github.theprogmatheus.auto.jautomail;

import com.github.theprogmatheus.auto.jautomail.services.APIService;
import com.github.theprogmatheus.auto.jautomail.services.CertificateService;
import com.github.theprogmatheus.auto.jautomail.services.EmailService;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Main {

    public static final Logger log = Logger.getLogger(Main.class.getName());
    public static final long CHECK_DELAY = 60000L;

    public static void main(String[] args) throws InterruptedException {
        checkEnvVariables();

        while (true) {
            try {
                executeTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Verificando novamente em %d segundos. IDLE.".formatted(CHECK_DELAY / 1000L));
            Thread.sleep(60000L);
        }
    }

    public static void executeTask() throws Exception {
        List<Participant> participants = APIService.fetchParticipants();
        List<Certificate> certificates = CertificateService.generateCertificates(participants, false);
        List<Certificate> certificatesSent = EmailService.sendCertificatesEmail(certificates);
    }

    public static void checkEnvVariables() {
        log.info("Verificando variáveis de ambiente.");

        Objects.requireNonNull(System.getenv("JAUTOMAIL_SPREADSHEET_ID"), "The JAUTOMAIL_SPREADSHEET_ID env variable can't be null.");
        Objects.requireNonNull(System.getenv("JAUTOMAIL_EMAIL_EMAIL"), "The JAUTOMAIL_EMAIL_EMAIL env variable can't be null.");
        Objects.requireNonNull(System.getenv("JAUTOMAIL_EMAIL_PASSWORD"), "The JAUTOMAIL_EMAIL_PASSWORD env variable can't be null.");

        log.info("Variáveis de ambiente verificados com sucesso.");
    }
}
