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
            List<WebElement> jobCards = driver.findElements(By.cssSelector("li.job-bx"));
            for (WebElement card : jobCards) {
                WebElement titleElem = null;
                try { titleElem = card.findElement(By.cssSelector("h2.heading-trun a")); } catch (Exception ignored) {}
                String jobTitle = titleElem != null ? titleElem.getText() : "";
                String applyLink = titleElem != null ? titleElem.getAttribute("href") : "";
                WebElement companyElem = null;
                try { companyElem = card.findElement(By.cssSelector("h3.joblist-comp-name")); } catch (Exception ignored) {}
                String company = companyElem != null ? companyElem.getText().trim() : "";
                WebElement locationElem = null;
                try { locationElem = card.findElement(By.cssSelector("li.srp-zindex.location-tru")); } catch (Exception ignored) {}
                String location = locationElem != null ? locationElem.getText().trim() : "";
                WebElement expElem = null;
                try { expElem = card.findElement(By.cssSelector("li.srp-icons.experience, li:has(i.srp-icons.experience)")); } catch (Exception ignored) {}
                String experience = expElem != null ? expElem.getText().replace("Years", " Years").trim() : "";
                Job job = new Job();
                job.setJobTitle(jobTitle);
                job.setCompany(company);
                job.setLocation(location);
                job.setExperience(experience);
                job.setApplyLink(applyLink);
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
