package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;
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
public class TimesJobsScraperService {

    public List<Job> scrapeJobs(String keyword) {
        List<Job> jobs = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.timesjobs.com/candidate/job-search.html"
                    + "?searchType=personalizedSearch&from=submit"
                    + "&txtKeywords=" + encodedKeyword
                    + "&txtLocation=";

            // ✅ Use Jsoup with proper headers
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .get();

            Elements jobCards = doc.select("li.job-bx");

            for (Element card : jobCards) {
                String jobTitle = card.select("h2.heading-trun a").text();
                String applyLink = card.select("h2.heading-trun a").attr("href");
                String company = card.select("h3.joblist-comp-name").text().replaceAll("\\(More Jobs\\)$", "").trim();
                String location = card.select("li.srp-zindex.location-tru").text();
                String experience = card.select("li.experience").text().replace("Years", " Years").trim();

                if (!jobTitle.isEmpty() && applyLink.contains("timesjobs.com")) {
                    Job job = new Job();
                    job.setJobTitle(jobTitle);
                    job.setCompany(company);
                    job.setLocation(location);
                    job.setExperience(experience);
                    job.setApplyLink(applyLink);
                    job.setPlatform("TimesJobs");
                    jobs.add(job);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }
}
