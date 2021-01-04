package com.kevinklintdavis.coronavirustracker.services;

import com.kevinklintdavis.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    public static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public static String getVirusDataUrl() {
        return VIRUS_DATA_URL;
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    //runs the method once every hour
    @Scheduled(cron = "* 1 * * * *")
    public void fetchVirusData() throws IOException, InterruptedException {

        List<LocationStats> newStats = new ArrayList<>();
        //request the information and stores the response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Use Apache Commons to parse CSV data
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();

            locationStat.setState(record.get("Province/State"));
            if (locationStat.getState() =="")
                locationStat.setState("N/A");

            locationStat.setCountry(record.get("Country/Region"));
            if (locationStat.getCountry() =="")
                locationStat.setCountry("N/A");

            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevCases = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPreviousDay(latestCases - prevCases);
            newStats.add(locationStat);
        }//for




        //to maintain availability use a separate list for execution
        //then copy to the other list for users to view
        this.allStats = newStats;




    }//fetchVirusData
}
