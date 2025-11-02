package com.github.theprogmatheus.auto.jautomail;

import com.github.theprogmatheus.auto.jautomail.services.APIService;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        List<Participant> participants = APIService.fetchParticipants();

        for (Participant participant : participants) {
            System.out.println(participant);
        }
    }
}
