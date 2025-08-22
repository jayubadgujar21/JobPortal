package com.zplus.jobportal.controller;

import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.services.SimplyHiredScraperService;
import com.zplus.jobportal.services.FreshersWorldScraperService;
import com.zplus.jobportal.services.ShineScrapeService;
import com.zplus.jobportal.services.TimesJobsScraperService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ScrapingController {

    private final ShineScrapeService shineScraperService;
    private final TimesJobsScraperService timesJobsScraperService;
    private final FreshersWorldScraperService freshersworldScraperService;
    private final SimplyHiredScraperService simplyHiredScraperService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Inject Python scraper base URL from application.properties
    @Value("${python.scraper.url}")
    private String pythonScraperBaseUrl;

    public ScrapingController(
            ShineScrapeService shineScraperService,
            TimesJobsScraperService timesJobsScraperService,
            FreshersWorldScraperService freshersworldScraperService,
            SimplyHiredScraperService simplyHiredScraperService
    ) {
        this.shineScraperService = shineScraperService;
        this.timesJobsScraperService = timesJobsScraperService;
        this.freshersworldScraperService = freshersworldScraperService;
        this.simplyHiredScraperService = simplyHiredScraperService;
    }

    @GetMapping("/simplyhired")
    public List<Job> getSimplyHiredJobs(@RequestParam String jobTitle, @RequestParam(required = false) String location) {
        return simplyHiredScraperService.scrapeJobs(jobTitle, location);
    }

    @GetMapping("/freshersworld")
    public List<Job> getFreshersworldJobs(@RequestParam String jobTitle) {
        return freshersworldScraperService.scrapeJobs(jobTitle);
    }

    @GetMapping("/timesjobs")
    public List<Job> getTimesJobs(@RequestParam String jobTitle) {
        return timesJobsScraperService.scrapeJobs(jobTitle);
    }

    @GetMapping("/shine")
    public List<Job> getShineJobs(@RequestParam String jobTitle) {
        return shineScraperService.scrape(jobTitle);
    }

    // ✅ Python scraper endpoint
    @GetMapping("/python-naukri")
    public ResponseEntity<String> getPythonNaukriJobs(@RequestParam String jobTitle) {
        String pythonApiUrl = pythonScraperBaseUrl + "/scrape/?keyword=" + jobTitle.replace(" ", "%20");
        String response = restTemplate.getForObject(pythonApiUrl, String.class);
        return ResponseEntity.ok(response);
    }

    // ✅ SSE endpoint
    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {
        SseEmitter emitter = new SseEmitter(180000L);

        CompletableFuture.runAsync(() -> {
            try {
                // Python scraper async
                CompletableFuture<Void> pythonNaukriFuture = CompletableFuture.runAsync(() -> {
                    try {
                        String pythonApiUrl = pythonScraperBaseUrl + "/scrape/?keyword=" + jobTitle.replace(" ", "%20");
                        String response = restTemplate.getForObject(pythonApiUrl, String.class);
                        emitter.send(SseEmitter.event().name("jobs").data(response));
                    } catch (Exception e) {
                        System.err.println("Error in Python Naukri scraper: " + e.getMessage());
                    }
                }, executorService);

                // Other scrapers (same as your code) ...
                CompletableFuture<List<Job>> shineFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Job> jobs = shineScraperService.scrape(jobTitle);
                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
                        return jobs;
                    } catch (Exception e) {
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture<List<Job>> timesJobsFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Job> jobs = timesJobsScraperService.scrapeJobs(jobTitle);
                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
                        return jobs;
                    } catch (Exception e) {
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture<List<Job>> freshersworldFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Job> jobs = freshersworldScraperService.scrapeJobs(jobTitle);
                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
                        return jobs;
                    } catch (Exception e) {
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture<List<Job>> simplyHiredFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Job> jobs = simplyHiredScraperService.scrapeJobs(jobTitle, null);
                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
                        return jobs;
                    } catch (Exception e) {
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture.allOf(pythonNaukriFuture, shineFuture, timesJobsFuture, freshersworldFuture, simplyHiredFuture)
                        .thenRun(() -> {
                            try {
                                emitter.send(SseEmitter.event().name("complete").data("All job searches completed"));
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        });

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }
}
