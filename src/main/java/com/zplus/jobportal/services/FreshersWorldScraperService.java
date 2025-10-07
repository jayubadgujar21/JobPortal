package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;
import io.github.bonigarcia.wdm.WebDriverManager;
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
            WebDriverManager.chromedriver().setup();

            String searchKeyword = jobTitle.trim().replace(" ", "-");
            String url = "https://www.freshersworld.com/jobs/jobsearch/" + searchKeyword;

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                    "--window-size=1920,1080", "--disable-gpu",
                    "--disable-blink-features=AutomationControlled",
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            // ✅ For server environments
            String chromeBinary = System.getenv("GOOGLE_CHROME_SHIM");
            if (chromeBinary != null) {
                options.setBinary(chromeBinary);
            }

            driver = new ChromeDriver(options);
            driver.get(url);
            Thread.sleep(1500);

            List<WebElement> jobCards = driver.findElements(By.cssSelector(".job-desc-block"));
            for (WebElement card : jobCards) {
                String title = safeGetText(card, ".job-new-title .wrap-title");
                String company = safeGetText(card, "h3.latest-jobs-title.company-name");
                String location = safeGetText(card, ".job-location a");
                String experience = safeGetText(card, ".experience.job-details-span");

                String applyLink = url;
                try {
                    String href = card.findElement(By.cssSelector(".job-location a")).getAttribute("href");
                    if (href != null && !href.isEmpty()) {
                        applyLink = href.startsWith("http") ? href : "https://www.freshersworld.com" + href;
                    }
                } catch (Exception ignored) {}

                Job job = new Job();
                job.setJobTitle(title);
                job.setCompany(company);
                job.setLocation(location);
                job.setExperience(experience);
                job.setApplyLink(applyLink);
                job.setPlatform("FreshersWorld");
                jobs.add(job);
            }

        } catch (Exception e) {
            System.err.println("[FreshersWorldScraperService] Error: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }
        return jobs;
    }

    private String safeGetText(WebElement parent, String css) {
        try {
            return parent.findElement(By.cssSelector(css)).getText();
        } catch (Exception e) {
            return "";
        }
    }
}
