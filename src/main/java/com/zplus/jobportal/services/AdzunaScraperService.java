package com.zplus.jobportal.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zplus.jobportal.dto.response.JobResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.*;

@Service
public class AdzunaScraperService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String APP_ID = "4bfdc790";
    private static final String APP_KEY = "77627923112654439b45b47bb9f24f7f";
    private static final String BASE_URL = "https://api.adzuna.com/v1/api/jobs/in/search/";

    public List<JobResponseDTO> scrapeJobs(String jobTitle) {
        List<JobResponseDTO> jobs = new ArrayList<>();
        try {
            for (int i = 1; i <= 2; i++) {
                String url = BASE_URL + i + "?app_id=" + APP_ID + "&app_key=" + APP_KEY
                        + "&results_per_page=50&what=" + jobTitle;

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode root = mapper.readTree(response.getBody()).path("results");

                for (JsonNode job : root) {
                    String title = job.path("title").asText();
                    String company = job.path("company").path("display_name").asText();
                    String location = job.path("location").path("display_name").asText();
                    String applyLink = job.path("redirect_url").asText();
                    String desc = job.path("description").asText();
                    String exp = extractExperience(desc);

                    jobs.add(new JobResponseDTO(title, company, location, applyLink, exp, "Adzuna"));
                }
            }
        } catch (Exception e) {
            System.err.println("[AdzunaScraperService] Error: " + e.getMessage());
        }
        return jobs;
    }

    private String extractExperience(String desc) {
        if (desc == null || desc.isEmpty()) return "Not Specified";
        Pattern p = Pattern.compile("(\\d+\\s?\\+?\\s?years?)|(\\d+-\\d+\\s?years?)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(desc);
        return m.find() ? m.group() : "Not Specified";
    }
}
