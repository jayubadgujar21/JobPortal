package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                    "--window-size=1920,1080", "--disable-gpu",
                    "--disable-blink-features=AutomationControlled",
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            // For VPS (Heroku/Ubuntu)
            String chromeBinary = System.getenv("GOOGLE_CHROME_SHIM");
            if (chromeBinary != null) {
                options.setBinary(chromeBinary);
            }

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

            String searchUrl = "https://www.simplyhired.co.in/search?q=" + jobTitle.replace(" ", "+");
            if (location != null && !location.isEmpty()) {
                searchUrl += "&l=" + location.replace(" ", "+");
            }

            driver.get(searchUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[data-testid='searchSerpJob']")));

            List<WebElement> jobCards = driver.findElements(By.cssSelector("div[data-testid='searchSerpJob']"));
            int count = 0;

            for (WebElement card : jobCards) {
                if (count++ >= 10) break;

                String title = safeGet(card, "h2[data-testid='searchSerpJobTitle'] a");
                String company = safeGet(card, "[data-testid='companyName']");
                String loc = safeGet(card, "[data-testid='searchSerpJobLocation']");
                String experience = safeGet(card, "[data-testid='searchSerpJobSalaryConfirmed']");
                String jobDetailUrl = getHref(card, "h2[data-testid='searchSerpJobTitle'] a");

                Job job = new Job();
                job.setJobTitle(title);
                job.setCompany(company);
                job.setLocation(loc);
                job.setExperience(experience);
                job.setApplyLink(jobDetailUrl);
                job.setPlatform("SimplyHired");
                jobs.add(job);
            }

        } catch (Exception e) {
            System.err.println("[SimplyHiredScraperService] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }

        System.out.println("✅ Scraped " + jobs.size() + " jobs from SimplyHired");
        return jobs;
    }

    private String safeGet(WebElement el, String css) {
        try {
            return el.findElement(By.cssSelector(css)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    private String getHref(SearchContext context, String css) {
        try {
            String href = context.findElement(By.cssSelector(css)).getAttribute("href");
            return href.startsWith("http") ? href : "https://www.simplyhired.co.in" + href;
        } catch (Exception e) {
            return "";
        }
    }
}
