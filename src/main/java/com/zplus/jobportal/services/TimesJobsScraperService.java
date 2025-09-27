package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimesJobsScraperService {

    public List<Job> scrapeJobs(String keyword) {
        List<Job> jobs = new ArrayList<>();
        WebDriver driver = null;

        try {
            String quotedKeyword = "%22" + keyword + "%22"; // URL-encoded quotes
            String encodedQuotedKeyword = URLEncoder.encode(quotedKeyword, StandardCharsets.UTF_8);

            String url = "https://www.timesjobs.com/candidate/job-search.html?searchType=personalizedSearch&from=submit&searchTextSrc=ft"
                    + "&searchTextText=" + encodedQuotedKeyword
                    + "&txtKeywords=" + encodedQuotedKeyword + "%2C"
                    + "&txtLocation=";

            // ✅ ChromeOptions for server deployment
            ChromeOptions options = new ChromeOptions();
            options.setBinary("/snap/bin/chromium"); // snap-installed chromium
            options.addArguments("--headless"); // headless mode for servers
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.get(url);

            Thread.sleep(2000); // wait for page load

            List<WebElement> jobCards = driver.findElements(By.cssSelector("li.job-bx"));

            for (WebElement card : jobCards) {
                String jobTitle = "";
                String applyLink = "";
                String company = "";
                String location = "";
                String experience = "";

                try {
                    WebElement titleElem = card.findElement(By.cssSelector("h2.heading-trun a"));
                    jobTitle = titleElem.getText();
                    applyLink = titleElem.getAttribute("href");
                } catch (Exception ignored) {}

                try {
                    WebElement companyElem = card.findElement(By.cssSelector("h3.joblist-comp-name"));
                    company = companyElem.getText().trim();
                } catch (Exception ignored) {}

                try {
                    WebElement locationElem = card.findElement(By.cssSelector("li.srp-zindex.location-tru"));
                    location = locationElem.getText().trim();
                } catch (Exception ignored) {}

                try {
                    WebElement expElem = card.findElement(By.cssSelector("li.srp-icons.experience, li:has(i.srp-icons.experience)"));
                    experience = expElem.getText().replace("Years", " Years").trim();
                } catch (Exception ignored) {}

                Job job = new Job();
                job.setJobTitle(jobTitle);
                job.setCompany(company);
                job.setLocation(location);
                job.setExperience(experience);
                job.setApplyLink(applyLink);
                job.setPlatform("TimesJobs");

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace(); // ✅ helpful for debugging on server logs
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return jobs;
    }
}
