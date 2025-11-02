package com.github.theprogmatheus.auto.jautomail.services;

import com.github.theprogmatheus.auto.jautomail.Certificate;
import com.github.theprogmatheus.auto.jautomail.Participant;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.theprogmatheus.auto.jautomail.Main.log;

public class EmailService {

    public static final String EMAIL;
    public static final String PASSWORD;
    public static final File EMAILS_SENT_FILE;
    public static final Properties EMAIL_PROPERTIES;

    static {
        EMAIL = System.getenv("JAUTOMAIL_EMAIL_EMAIL");
        PASSWORD = System.getenv("JAUTOMAIL_EMAIL_PASSWORD");

        EMAILS_SENT_FILE = new File("emails_sent.txt");

        EMAIL_PROPERTIES = new Properties();
        EMAIL_PROPERTIES.put("mail.smtp.auth", "true");
        EMAIL_PROPERTIES.put("mail.smtp.starttls.enable", "true");
        EMAIL_PROPERTIES.put("mail.smtp.host", "smtp.gmail.com");
        EMAIL_PROPERTIES.put("mail.smtp.port", "587");
    }

    private static Set<String> loadSentEmails() throws IOException {
        if (!EMAILS_SENT_FILE.exists())
            return new HashSet<>();
        String fileContent = Files.readString(EMAILS_SENT_FILE.toPath(), StandardCharsets.UTF_8);
        return Arrays.stream(fileContent.split(System.lineSeparator()))
                .filter(line -> !line.isBlank())
                .collect(Collectors.toSet());
    }

    private static void addSentEmail(String email) throws IOException {
        Objects.requireNonNull(email, "The email can't be null.");
        if (email.isBlank())
            return;

        if (!EMAILS_SENT_FILE.exists())
            EMAILS_SENT_FILE.createNewFile();

        Files.write(EMAILS_SENT_FILE.toPath(), (email.concat(System.lineSeparator()).toLowerCase()).getBytes("utf-8"), StandardOpenOption.APPEND);
    }

    public static List<Certificate> sendCertificatesEmail(List<Certificate> certificates) throws MessagingException, IOException {
        log.info("Enviando certificados por e-mail...");
        Objects.requireNonNull(EMAIL, "The env e-mail can't be null");
        Objects.requireNonNull(PASSWORD, "The env password can't be null.");

        Objects.requireNonNull(certificates, "The certificates list can't be null.");
        if (certificates.isEmpty())
            return certificates;

        Set<String> sentEmails = loadSentEmails();
        certificates = certificates.stream()
                .filter(certificate -> !sentEmails.contains(certificate.getOwner().getEmail().toLowerCase()))
                .toList();

        if (certificates.isEmpty()) {
            log.info("Nenhum novo certificado para enviar.");
            return certificates;
        }

        Session session = Session.getInstance(EMAIL_PROPERTIES, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        final StringBuilder emailContent = new StringBuilder();
        emailContent.append("Olá %participant_name%,");
        emailContent.append(System.lineSeparator());
        emailContent.append("Segue seu certificado de participação em anexo.");
        emailContent.append(System.lineSeparator());
        emailContent.append("Abraço!");

        final String emailSubject = "Certificado de Participação - Webinar: Planejamento de Carreira em TI";

        for (Certificate certificate : certificates) {
            Participant participant = certificate.getOwner();

            String emailContentParsed = emailContent.toString()
                    .replace("%participant_name%", participant.getName())
                    .replace("%participant_registration%", participant.getRegistration())
                    .replace("%participant_email%", participant.getEmail());

            MimeBodyPart emailBody = new MimeBodyPart();
            emailBody.setText(emailContentParsed, "utf-8");

            MimeBodyPart emailAttachment = new MimeBodyPart();
            emailAttachment.attachFile(certificate.getFile());

            MimeMultipart mimeMultipart = new MimeMultipart();

            mimeMultipart.addBodyPart(emailBody);
            mimeMultipart.addBodyPart(emailAttachment);

            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(EMAIL);
            mimeMessage.setRecipients(Message.RecipientType.TO, participant.getEmail());
            mimeMessage.setSubject(emailSubject);
            mimeMessage.setContent(mimeMultipart);

            Transport.send(mimeMessage);
            addSentEmail(participant.getEmail());
            log.info("Enviado certificado de %s para o e-mail %s.".formatted(participant.getName(), participant.getEmail()));
        }

        log.info("Certificados enviados com sucesso.");
        return certificates;
    }
}
