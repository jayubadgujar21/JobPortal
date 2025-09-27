package com.zplus.jobportal.services;

import com.zplus.jobportal.dto.response.JobResponseDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class UpdazzScraperService {

    private static final String BASE_URL = "https://www.updazz.com/c/";

    public List<JobResponseDTO> scrapeJobs(String jobTitle) {
        List<JobResponseDTO> jobs = new ArrayList<>();

        try {
            // encode job title for URL
            String encodedJob = URLEncoder.encode(jobTitle, StandardCharsets.UTF_8);
            String url = BASE_URL + encodedJob + "-jobs-1.html";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .referrer("https://www.google.com")
                    .timeout(15000)
                    .get();

            // ✅ Updated selectors after inspecting Updazz job listings
            Elements jobCards = doc.select("div.job_container"); // each job block

            for (Element card : jobCards) {
                String title = card.select("h3 a").text();
                String applyLink = card.select("h3 a").attr("href");
                if (!applyLink.startsWith("http")) {
                    applyLink = "https://www.updazz.com" + applyLink;
                }

                String company = card.select(".job_company a").text();
                String location = card.select(".job_location").text();
                String experience = card.select(".job_experience").text();

                if (title.isEmpty()) continue;

                jobs.add(new JobResponseDTO(
                        title,
                        company,
                        location,
                        applyLink,
                        experience,
                        "Updazz"
                ));
            }

        } catch (Exception e) {
            System.err.println("[UpdazzScraperService] Error scraping: " + e.getMessage());
        }

        return jobs;
    }
}
