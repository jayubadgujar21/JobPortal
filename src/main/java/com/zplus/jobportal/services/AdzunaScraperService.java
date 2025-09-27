package com.zplus.jobportal.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zplus.jobportal.dto.response.JobResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdzunaScraperService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String APP_ID = "4bfdc790";
    private static final String APP_KEY = "77627923112654439b45b47bb9f24f7f";

    private static final String BASE_URL =
            "https://api.adzuna.com/v1/api/jobs/in/search/";

    public List<JobResponseDTO> scrapeJobs(String jobTitle) {
        List<JobResponseDTO> allJobs = new ArrayList<>();
        try {
            int totalPages = 2; // 2 pages × 100 results = 200 jobs

            for (int i = 1; i <= totalPages; i++) {
                String url = BASE_URL + i
                        + "?app_id=" + APP_ID
                        + "&app_key=" + APP_KEY
                        + "&results_per_page=100"
                        + "&what=" + jobTitle; // search by job title

                ResponseEntity<String> response =
                        restTemplate.getForEntity(url, String.class);

                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode results = root.path("results");

                for (JsonNode job : results) {
                    String title = job.path("title").asText();
                    String company = job.path("company").path("display_name").asText();
                    String location = job.path("location").path("display_name").asText();
//                    Double salaryMin = job.path("salary_min").isMissingNode() ? null : job.path("salary_min").asDouble();
//                    Double salaryMax = job.path("salary_max").isMissingNode() ? null : job.path("salary_max").asDouble();
                    String applyLink = job.path("redirect_url").asText();
                    String description = job.path("description").asText();

                    // ✅ Extract experience from description
                    String experience = extractExperience(description);

                    allJobs.add(new JobResponseDTO(
                            title, company, location, applyLink,experience,"Adzuna"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching jobs: " + e.getMessage());
        }
        return allJobs;
    }

    private String extractExperience(String description) {
        if (description == null || description.isEmpty()) return "Not Specified";

        // Regex patterns to catch common formats like "2 years", "3+ years", "0-1 years"
        Pattern pattern = Pattern.compile("(\\d+\\s?\\+?\\s?years?)|(\\d+-\\d+\\s?years?)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Specified";
    }
}
