package com.zplus.jobportal.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zplus.jobportal.dto.response.JobResponseDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ReedScraperService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Your API Key
    private static final String API_KEY = "ce4ea96a-f93e-4f0e-9d79-ee3f9c9857ad";
    private static final String BASE_URL = "https://www.reed.co.uk/api/1.0/search";

    public List<JobResponseDTO> scrapeJobs(String jobTitle) {
        List<JobResponseDTO> jobs = new ArrayList<>();

        try {
            String encodedJob = URLEncoder.encode(jobTitle, StandardCharsets.UTF_8);
            String url = BASE_URL + "?keywords=" + encodedJob + "&resultsToTake=50&page=1";

            // --- Basic Auth (username = API_KEY, password = "")
            String auth = API_KEY + ":";
            String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + base64Auth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.path("results");

            for (JsonNode job : results) {
                String title = job.path("jobTitle").asText();
                String company = job.path("employerName").asText();
                String location = job.path("locationName").asText();
                Double salaryMin = job.hasNonNull("minimumSalary") ? job.path("minimumSalary").asDouble() : null;
                Double salaryMax = job.hasNonNull("maximumSalary") ? job.path("maximumSalary").asDouble() : null;
                String description = job.path("jobDescription").asText();
                String applyLink = job.path("jobUrl").asText();

                String experience = extractExperience(description);

                jobs.add(new JobResponseDTO(
                        title, company, location,experience, applyLink, "Reed"
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error scraping Reed: " + e.getMessage(), e);
        }
        return jobs;
    }

    private String extractExperience(String description) {
        if (description == null || description.isBlank()) return "Not Specified";
        String lower = description.toLowerCase();
        if (lower.contains("fresher")) return "Fresher";
        if (lower.contains("0-2 years")) return "0-2 years";
        if (lower.contains("2+ years")) return "2+ years";
        if (lower.contains("5+ years")) return "5+ years";
        return "Not Specified";
    }
}
