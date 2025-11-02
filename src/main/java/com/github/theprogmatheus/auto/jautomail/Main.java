package com.github.theprogmatheus.auto.jautomail;

import com.github.theprogmatheus.auto.jautomail.services.APIService;
import com.github.theprogmatheus.auto.jautomail.services.CertificateService;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        List<Participant> participants = APIService.fetchParticipants();
        List<Certificate> certificates = CertificateService.generateCertificates(participants, false);
        certificates.forEach(System.out::println);
    }
}
