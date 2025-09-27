package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimplyHiredScraperService {

    public List<Job> scrapeJobs(String jobTitle, String location) {
        List<Job> jobs = new ArrayList<>();
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.setBinary("/snap/bin/chromium"); // ✅ set Chromium path (important for server)
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);

            // Build the search URL
            String baseUrl = "https://www.simplyhired.co.in/search?q=" + jobTitle.replace(" ", "+");
            if (location != null && !location.isEmpty()) {
                baseUrl += "&l=" + location.replace(" ", "+");
            }

            driver.get(baseUrl);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
            Thread.sleep(1500);

            List<WebElement> jobCards = driver.findElements(By.cssSelector("li.css-0 > div[data-testid='searchSerpJob']"));
            int count = 0;
            for (WebElement card : jobCards) {
                if (count++ >= 10) break;

                String title = "";
                String company = "";
                String locationText = "";
                String experience = "";
                String applyLink = baseUrl;

                try { title = card.findElement(By.cssSelector("h2[data-testid='searchSerpJobTitle'] a")).getText(); } catch (Exception ignored) {}
                try { company = card.findElement(By.cssSelector("[data-testid='companyName']")).getText(); } catch (Exception ignored) {}
                try { locationText = card.findElement(By.cssSelector("[data-testid='searchSerpJobLocation']")).getText(); } catch (Exception ignored) {}
                try { experience = card.findElement(By.cssSelector("[data-testid='searchSerpJobSalaryConfirmed']")).getText(); } catch (Exception ignored) {}
                try {
                    WebElement linkElem = card.findElement(By.cssSelector("h2[data-testid='searchSerpJobTitle'] a"));
                    String href = linkElem.getAttribute("href");
                    if (href != null && !href.isEmpty() && !href.startsWith("http")) {
                        applyLink = "https://www.simplyhired.co.in" + href;
                    } else if (href != null && !href.isEmpty()) {
                        applyLink = href;
                    }
                } catch (Exception ignored) {}

                Job job = new Job();
                job.setJobTitle(title);
                job.setCompany(company);
                job.setLocation(locationText);
                job.setExperience(experience);
                job.setApplyLink(applyLink);
                job.setPlatform("SimplyHired");

                jobs.add(job);
            }

        } catch (Exception e) {
            System.err.println("[SimplyHiredScraperService] Error scraping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return jobs;
    }
}
