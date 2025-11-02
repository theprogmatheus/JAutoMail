package com.github.theprogmatheus.auto.jautomail.services;

import com.github.theprogmatheus.auto.jautomail.Participant;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class APIService {

    public static final String API_URL = "https://docs.google.com/spreadsheets/d/%s/gviz/tq?tqx=out:csv";
    public static final String SPREADSHEET_ID = System.getenv("JAUTOMAIL_SPREADSHEET_ID");
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static List<Participant> fetchParticipants() throws IOException, InterruptedException {
        Objects.requireNonNull(SPREADSHEET_ID, "The spreadsheet id can't be null.");

        String formatedApiUrl = API_URL.formatted(SPREADSHEET_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(formatedApiUrl))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new RuntimeException("Unable to connect to the API. StatusCode: %d".formatted(response.statusCode()));

        String data = response.body();
        if (data == null || data.isBlank())
            throw new RuntimeException("The response body is empty.");

        List<Participant> participants = new ArrayList<>();
        try (CsvReader<CsvRecord> reader = CsvReader.builder().ofCsvRecord(data)) {
            reader.skipLines(1);
            for (CsvRecord record : reader) {
                Participant participant = new Participant();
                participant.setEmail(record.getField(1));
                participant.setCode(record.getField(2));
                participant.setName(record.getField(3));
                participant.setRegistration(record.getField(4));
                participants.add(participant);
            }
        }
        return participants;
    }

}
