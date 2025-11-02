package com.github.theprogmatheus.auto.jautomail.services;

import com.github.theprogmatheus.auto.jautomail.Certificate;
import com.github.theprogmatheus.auto.jautomail.Participant;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.theprogmatheus.auto.jautomail.Main.log;

public class CertificateService {

    public static final File CERTIFICATE_TEMPLATE_FILE;
    public static final Page.PdfOptions PDF_OPTIONS;
    public static final File OUTPUT_FOLDER;

    static {
        CERTIFICATE_TEMPLATE_FILE = new File("certificate-template.html");

        PDF_OPTIONS = new Page.PdfOptions();
        PDF_OPTIONS.format = "A4";
        PDF_OPTIONS.landscape = true;
        PDF_OPTIONS.scale = 0.8;

        OUTPUT_FOLDER = new File("certificates");
        OUTPUT_FOLDER.mkdirs();
    }

    public static List<Certificate> generateCertificates(List<Participant> participants, boolean regenerateIfExists) throws IOException {
        log.info("Gerando certificados para %d participantes...".formatted(participants.size()));

        List<Certificate> certificates = new ArrayList<>();

        Objects.requireNonNull(participants, "The participants list can't be null.");
        if (participants.isEmpty())
            return certificates;

        if (!CERTIFICATE_TEMPLATE_FILE.exists())
            throw new RuntimeException("The certificate template file does not exists.");

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(true).setChromiumSandbox(false))) {

            Page page = browser.newPage();
            page.navigate(CERTIFICATE_TEMPLATE_FILE.toURI().toString());
            final String htmlContent = Files.readString(CERTIFICATE_TEMPLATE_FILE.toPath());

            for (Participant participant : participants) {
                String fileName = "%s - (%s).pdf".formatted(participant.getName(), participant.getEmail());
                File outFile = new File(OUTPUT_FOLDER, fileName);
                if (outFile.exists() && !regenerateIfExists) {
                    certificates.add(new Certificate(participant, outFile));
                    log.info("Certificado recuperado para %s.".formatted(participant.getName()));
                    continue;
                }
                String html = htmlContent
                        .replace("%participant_name%", participant.getName())
                        .replace("%participant_registration%", participant.getRegistration())
                        .replace("%participant_email%", participant.getEmail());
                page.setContent(html);

                byte[] buffer = page.pdf(PDF_OPTIONS);
                Files.write(outFile.toPath(), buffer);

                log.info("Certificado gerado para %s.".formatted(participant.getName()));
                certificates.add(new Certificate(participant, outFile));
            }
        }

        log.info("Foram gerados/recuperados %d certificados.".formatted(certificates.size()));
        return certificates;
    }


}
