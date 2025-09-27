package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FreshersWorldScraperService {
    public List<Job> scrapeJobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();
        WebDriver driver = null;
        try {
            String searchKeyword = jobTitle.trim().replace(" ", "-");
            String url = "https://www.freshersworld.com/jobs/jobsearch/" + searchKeyword;
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
            driver = new ChromeDriver(options);
            driver.get(url);
            Thread.sleep(1000); // Reduced wait time
            List<WebElement> jobCards = driver.findElements(By.cssSelector(".job-desc-block"));
            for (WebElement card : jobCards) {
                String title = "";
                String company = "";
                String location = "";
                String experience = "";
                String applyLink = url; // fallback to search page
                try { title = card.findElement(By.cssSelector(".job-new-title .wrap-title")).getText(); } catch (Exception ignored) {}
                try { company = card.findElement(By.cssSelector("h3.latest-jobs-title.company-name")).getText(); } catch (Exception ignored) {}
                try { location = card.findElement(By.cssSelector(".job-location a")).getText(); } catch (Exception ignored) {}
                try { experience = card.findElement(By.cssSelector(".experience.job-details-span")).getText(); } catch (Exception ignored) {}
                try {
                    WebElement locLink = card.findElement(By.cssSelector(".job-location a"));
                    String href = locLink.getAttribute("href");
                    if (href != null && !href.isEmpty() && !href.startsWith("http")) {
                        // Make absolute URL
                        applyLink = "https://www.freshersworld.com" + href;
                    } else if (href != null && !href.isEmpty()) {
                        applyLink = href;
                    }
                } catch (Exception ignored) {}
                Job job = new Job();
                job.setJobTitle(title);
                job.setCompany(company);
                job.setLocation(location);
                job.setExperience(experience);
                job.setApplyLink(applyLink);
                job.setPlatform("Fresherworld");
                jobs.add(job);
            }
        } catch (Exception e) {
            // Optionally log error
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return jobs;
    }
}
