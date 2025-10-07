//
//package com.zplus.jobportal.controller;
//
//import com.zplus.jobportal.model.Job;
//import com.zplus.jobportal.services.*;
//import jakarta.annotation.PreDestroy;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.function.Supplier;
//
//@Slf4j
//@RestController
//public class ScrapingController {
//
//    private final ShineScrapeService shineScraperService;
//    private final TimesJobsScraperService timesJobsScraperService;
//    private final FreshersWorldScraperService freshersworldScraperService;
//    private final SimplyHiredScraperService simplyHiredScraperService;
//    private final AdzunaScraperService adzunaScraperService;
//    private final ReedScraperService reedScraperService;
//    private final UpdazzScraperService updazzScraperService;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//    private static final String PYTHON_SCRAPER_BASE_URL = "https://job-scrapper-python.onrender.com";
//    private static final String PYTHON_SCRAPER_LINKEDIN = "https://linkedin-scrapper-p7jy.onrender.com";
//
//    public ScrapingController(
//            ShineScrapeService shineScraperService,
//            TimesJobsScraperService timesJobsScraperService,
//            FreshersWorldScraperService freshersworldScraperService,
//            SimplyHiredScraperService simplyHiredScraperService,
//            AdzunaScraperService adzunaScraperService,
//            ReedScraperService reedScraperService,
//            UpdazzScraperService updazzScraperService
//    ) {
//        this.shineScraperService = shineScraperService;
//        this.timesJobsScraperService = timesJobsScraperService;
//        this.freshersworldScraperService = freshersworldScraperService;
//        this.simplyHiredScraperService = simplyHiredScraperService;
//        this.adzunaScraperService = adzunaScraperService;
//        this.reedScraperService = reedScraperService;
//        this.updazzScraperService = updazzScraperService;
//    }
//
//    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {
//        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes
//
//        emitter.onCompletion(() -> log.info("✅ SSE completed for {}", jobTitle));
//        emitter.onTimeout(() -> {
//            log.warn("⏱️ SSE timeout for {}", jobTitle);
//            safeComplete(emitter);
//        });
//        emitter.onError(e -> {
//            log.error("❌ SSE error for {}: {}", jobTitle, e.getMessage());
//            safeComplete(emitter);
//        });
//
//        // List of scrapers (mix of Python + Java)
//        List<Callable<Void>> scrapers = List.of(
//                () -> runScraper(emitter, "Naukri", () -> fetchPythonNaukri(jobTitle)),
//                () -> runScraper(emitter, "LinkedIn", () -> fetchPythonLinkedIn(jobTitle)),
//                () -> runScraper(emitter, "Shine", () -> shineScraperService.scrape(jobTitle)),
//                () -> runScraper(emitter, "TimesJobs", () -> timesJobsScraperService.scrapeJobs(jobTitle)),
//                () -> runScraper(emitter, "FreshersWorld", () -> freshersworldScraperService.scrapeJobs(jobTitle)),
//                () -> runScraper(emitter, "SimplyHired", () -> simplyHiredScraperService.scrapeJobs(jobTitle, null))
//        );
//
//        CompletableFuture.runAsync(() -> {
//            try {
//                // Run all scrapers concurrently
//                List<Future<Void>> futures = executorService.invokeAll(scrapers);
//
//                for (Future<Void> f : futures) {
//                    try {
//                        f.get(40, TimeUnit.SECONDS); // ⏳ Timeout per scraper
//                    } catch (TimeoutException te) {
//                        log.warn("⚠️ Scraper timed out: {}", te.getMessage());
//                        f.cancel(true);
//                    }
//                }
//
//                safeSend(emitter, SseEmitter.event().name("complete").data("✅ All job searches completed"));
//                safeComplete(emitter);
//
//            } catch (Exception e) {
//                log.error("💥 Error running scrapers", e);
//                safeComplete(emitter);
//            }
//        }, executorService);
//
//        return emitter;
//    }
//
//    // --- Helper Methods ---
//    private Void runScraper(SseEmitter emitter, String platform, Supplier<List<Job>> supplier) {
//        for (int attempt = 1; attempt <= 2; attempt++) {
//            try {
//                List<Job> jobs = supplier.get();
//                if (jobs != null && !jobs.isEmpty()) {
//                    jobs.forEach(j -> j.setPlatform(platform));
//                    sendInChunks(emitter, jobs, platform);
//                    log.info("✅ {} jobs scraped successfully: {} results", platform, jobs.size());
//                    return null;
//                }
//            } catch (Exception e) {
//                log.warn("{} scraper attempt {} failed: {}", platform, attempt, e.getMessage());
//                if (attempt == 2) {
//                    safeSend(emitter, SseEmitter.event().name("error").data(platform + " scraper failed"));
//                }
//            }
//        }
//        return null;
//    }
//
//    private void sendInChunks(SseEmitter emitter, List<Job> jobs, String platform) {
//        int chunkSize = 25;
//        for (int i = 0; i < jobs.size(); i += chunkSize) {
//            List<Job> chunk = jobs.subList(i, Math.min(i + chunkSize, jobs.size()));
//            safeSend(emitter, SseEmitter.event().name("jobs").id(platform + "-" + i).data(chunk));
//        }
//    }
//
//    private List<Job> fetchPythonNaukri(String jobTitle) {
//        String url = PYTHON_SCRAPER_BASE_URL + "/scrape/?keyword=" + jobTitle.replace(" ", "%20");
//        ResponseEntity<Job[]> response = restTemplate.getForEntity(url, Job[].class);
//        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
//    }
//
//    private List<Job> fetchPythonLinkedIn(String jobTitle) {
//        String url = PYTHON_SCRAPER_LINKEDIN + "/scrape?keyword=" + jobTitle.replace(" ", "%20");
//        ResponseEntity<Job[]> response = restTemplate.getForEntity(url, Job[].class);
//        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
//    }
//
//    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
//        try {
//            emitter.send(event);
//        } catch (IOException | IllegalStateException e) {
//            log.debug("⚠️ SSE connection closed. Skipping send. {}", e.getMessage());
//        }
//    }
//
//    private void safeComplete(SseEmitter emitter) {
//        try {
//            emitter.complete();
//        } catch (IllegalStateException ignored) {}
//    }
//
//    @PreDestroy
//    public void shutdownExecutor() {
//        log.info("🛑 Shutting down scraper executor...");
//        executorService.shutdown();
//        try {
//            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
//                executorService.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executorService.shutdownNow();
//        }
//    }
//}
