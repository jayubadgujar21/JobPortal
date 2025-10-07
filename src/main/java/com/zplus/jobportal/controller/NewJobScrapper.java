package com.zplus.jobportal.controller;

import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.dto.response.JobResponseDTO;
import com.zplus.jobportal.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@RestController
public class NewJobScrapper {

    @Autowired private ShineScrapeService shineScraperService;
    @Autowired private TimesJobsScraperService timesJobsScraperService;
    @Autowired private FreshersWorldScraperService freshersworldScraperService;
    @Autowired private SimplyHiredScraperService simplyHiredScraperService;
    @Autowired private AdzunaScraperService adzunaScraperService;
    @Autowired private org.springframework.web.client.RestTemplate restTemplate;
    @Autowired private ReedScraperService reedScraperService;

    @GetMapping("/reedJobs/{jobTitle}")
    List<JobResponseDTO> getReedJobs(@RequestParam String jobTitle){
        return reedScraperService.scrapeJobs(jobTitle);
    }

    // Python API URLs
    private static final String PYTHON_SCRAPER_BASE_URL = "https://job-scrapper-python.onrender.com";
    private static final String PYTHON_SCRAPER_LINKEDIN = "https://linkedin-scrapper-p7jy.onrender.com";

    // Executor for multithreading
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {

        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout

        CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> tasks = new ArrayList<>();

                // 🔹 1. Python Naukri Scraper
                tasks.add(runScraper(emitter, "Naukri", () -> {
                    String url = PYTHON_SCRAPER_BASE_URL + "/scrape/?keyword=" + encode(jobTitle);
                    ResponseEntity<Job[]> res = restTemplate.getForEntity(url, Job[].class);
                    return Arrays.asList(Optional.ofNullable(res.getBody()).orElse(new Job[0]));
                }));

                // 🔹 2. Shine
                tasks.add(runScraper(emitter, "Shine", () -> shineScraperService.scrape(jobTitle)));

                // 🔹 3. TimesJobs
                tasks.add(runScraper(emitter, "TimesJobs", () -> timesJobsScraperService.scrapeJobs(jobTitle)));

                // 🔹 4. FreshersWorld
                tasks.add(runScraper(emitter, "FreshersWorld", () -> freshersworldScraperService.scrapeJobs(jobTitle)));

                // 🔹 5. Adzuna
                tasks.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<JobResponseDTO> jobs = adzunaScraperService.scrapeJobs(jobTitle);
                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
                        logSuccess("Adzuna", jobs.size());
                    } catch (Exception e) {
                        logError("Adzuna", e);
                    }
                }, executor));

                // 🔹 6. SimplyHired
                tasks.add(runScraper(emitter, "SimplyHired", () -> simplyHiredScraperService.scrapeJobs(jobTitle, null)));

                // 🔹 7. Python LinkedIn Scraper
                tasks.add(runScraper(emitter, "LinkedIn", () -> {
                    String url = PYTHON_SCRAPER_LINKEDIN + "/scrape?keyword=" + encode(jobTitle);
                    ResponseEntity<Job[]> res = restTemplate.getForEntity(url, Job[].class);
                    List<Job> jobs = Arrays.asList(Optional.ofNullable(res.getBody()).orElse(new Job[0]));
                    jobs.forEach(j -> j.setPlatform("LinkedIn"));
                    return jobs;
                }));

                // 8 --- Reed ---
//                tasks.add(runScraperDTO(emitter, "Reed", () -> reedScraperService.scrapeJobs(jobTitle)));

                // ✅ Wait for all scrapers
                CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).whenComplete((r, ex) -> {
                    try {
                        if (ex != null) {
                            emitter.send(SseEmitter.event().name("error").data("Some scrapers failed"));
                        }
                        // --- Now run Reed scraper last ---
                        List<JobResponseDTO> reedJobs = reedScraperService.scrapeJobs(jobTitle);
                        if (!reedJobs.isEmpty()) {
                            emitter.send(SseEmitter.event().name("jobs").data(reedJobs));
                        }

                        // ✅ Complete SSE
                        emitter.send(SseEmitter.event().name("complete").data("✅ All job searches completed"));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, executor);

        return emitter;
    }

    // 🔸 Helper: Generic scraper runner
    private CompletableFuture<Void> runScraper(SseEmitter emitter, String platform, Callable<List<Job>> scraper) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<Job> jobs = scraper.call();
                sendInChunks(emitter, jobs, platform);
                logSuccess(platform, jobs.size());
            } catch (Exception e) {
                logError(platform, e);
            }
        }, executor);
    }

    private CompletableFuture<Void> runScraperDTO(SseEmitter emitter, String platform, Callable<List<JobResponseDTO>> scraper) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<JobResponseDTO> jobs = scraper.call();
                emitter.send(SseEmitter.event().name("jobs").data(jobs));
                logSuccess(platform, jobs.size());
            } catch (Exception e) {
                logError(platform, e);
            }
        }, executor);
    }

    // 🔸 Helper: send jobs in chunks
    private void sendInChunks(SseEmitter emitter, List<Job> jobs, String platform) throws IOException {
        int chunkSize = 20;
        for (int i = 0; i < jobs.size(); i += chunkSize) {
            List<Job> chunk = jobs.subList(i, Math.min(i + chunkSize, jobs.size()));
            chunk.forEach(job -> job.setPlatform(platform));
            emitter.send(SseEmitter.event().name("jobs").data(chunk));
        }
    }

    // 🔸 Helper: Encoding
    private String encode(String text) {
        return text.replace(" ", "%20");
    }

    // 🔸 Helper: Logging
    private void logSuccess(String platform, int count) {
        System.out.println("✅ " + platform + " jobs scraped successfully: " + count + " results");
    }

    private void logError(String platform, Exception e) {
        System.err.println("❌ Error in " + platform + " scraper: " + e.getMessage());
    }
}
