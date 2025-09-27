package com.zplus.jobportal.controller;

import com.zplus.jobportal.dto.response.JobResponseDTO;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.services.*;
import jakarta.annotation.PreDestroy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final AdzunaScraperService adzunaScraperService;
    private final ReedScraperService reedScraperService;
    private final UpdazzScraperService updazzScraperService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Base URL for deployed Python scraper
    private static final String PYTHON_SCRAPER_BASE_URL = "https://job-scrapper-python.onrender.com";

    private static final String PYTHON_SCRAPER_LINKEDIN = "https://linkedin-scrapper-p7jy.onrender.com";

    public ScrapingController(
            ShineScrapeService shineScraperService,
            TimesJobsScraperService timesJobsScraperService,
            FreshersWorldScraperService freshersworldScraperService,
            SimplyHiredScraperService simplyHiredScraperService,
            AdzunaScraperService adzunaScraperService, ReedScraperService reedScraperService, UpdazzScraperService updazzScraperService
    ) {
        this.shineScraperService = shineScraperService;
        this.timesJobsScraperService = timesJobsScraperService;
        this.freshersworldScraperService = freshersworldScraperService;
        this.simplyHiredScraperService = simplyHiredScraperService;
        this.adzunaScraperService=adzunaScraperService;
        this.reedScraperService = reedScraperService;
        this.updazzScraperService = updazzScraperService;
    }

    // ✅ Python scraper single call
//    @GetMapping("/python-naukri")
//    public ResponseEntity<List<Job>> getPythonNaukriJobs(@RequestParam String jobTitle) {
//        String pythonApiUrl = PYTHON_SCRAPER_BASE_URL + "/scrape/?keyword=" + jobTitle.replace(" ", "%20");
//
//        ResponseEntity<Job[]> response = restTemplate.getForEntity(pythonApiUrl, Job[].class);
//        List<Job> jobs = response.getBody() != null ? List.of(response.getBody()) : new ArrayList<>();
//
//        return ResponseEntity.ok(jobs);
//    }

    // ✅ Shine
    @GetMapping("/shine")
    public List<Job> getShineJobs(@RequestParam String jobTitle) {
        return shineScraperService.scrape(jobTitle);
    }

    // ✅ TimesJobs
    @GetMapping("/timesjobs")
    public List<Job> getTimesJobs(@RequestParam String jobTitle) {
        return timesJobsScraperService.scrapeJobs(jobTitle);
    }

    // ✅ Freshersworld
    @GetMapping("/freshersworld")
    public List<Job> getFreshersworldJobs(@RequestParam String jobTitle) {
        return freshersworldScraperService.scrapeJobs(jobTitle);
    }

    // ✅ SimplyHired
    @GetMapping("/simplyhired")
    public List<Job> getSimplyHiredJobs(@RequestParam String jobTitle, @RequestParam(required = false) String location) {
        return simplyHiredScraperService.scrapeJobs(jobTitle, location);
    }

    @GetMapping("/adzuna")
    public List<JobResponseDTO> getAdzunaJobs(@RequestParam String jobTitle){
        return adzunaScraperService.scrapeJobs(jobTitle);
    }


    @GetMapping("/updazz")
    public List<JobResponseDTO> getUpdazzJobs(@RequestParam String jobTitle) {
        return updazzScraperService.scrapeJobs(jobTitle);
    }


    // ✅ SSE endpoint (stream results)
//    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {
//        // Increase timeout to 5 minutes
//        SseEmitter emitter = new SseEmitter(300000L);
//
//        CompletableFuture.runAsync(() -> {
//            try {
//                // --- Python scraper ---
//                CompletableFuture<Void> pythonNaukriFuture = CompletableFuture.runAsync(() -> {
//                    try {
//                        String pythonApiUrl = PYTHON_SCRAPER_BASE_URL + "/scrape/?keyword=" + jobTitle.replace(" ", "%20");
//                        ResponseEntity<Job[]> response = restTemplate.getForEntity(pythonApiUrl, Job[].class);
//                        if (response.getBody() != null) {
//                            emitter.send(SseEmitter.event().name("jobs").data(List.of(response.getBody())));
//                        }
//                        System.out.println("Naukri jobs scrapped successfully..!! ");
//                    } catch (Exception e) {
//                        System.err.println("Error in Python Naukri scraper: " + e.getMessage());
//                    }
//                }, executorService);
//
//                // --- Shine ---
//                CompletableFuture<List<Job>> shineFuture = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        List<Job> jobs = shineScraperService.scrape(jobTitle);
//                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
//                        System.out.println("Shine jobs scrapped successfully..!! ");
//                        return jobs;
//                    } catch (Exception e) {
//                        return new ArrayList<>();
//                    }
//                }, executorService);
//
//                // --- TimesJobs ---
//                CompletableFuture<List<Job>> timesJobsFuture = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        List<Job> jobs = timesJobsScraperService.scrapeJobs(jobTitle);
//                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
//                        System.out.println("TimesJobs jobs scrapped successfully..!! ");
//                        return jobs;
//                    } catch (Exception e) {
//                        return new ArrayList<>();
//                    }
//                }, executorService);
//
//                // --- Freshersworld ---
//                CompletableFuture<List<Job>> freshersworldFuture = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        List<Job> jobs = freshersworldScraperService.scrapeJobs(jobTitle);
//                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
//                        System.out.println("FresherWorld jobs scrapped successfully..!! ");
//                        return jobs;
//                    } catch (Exception e) {
//                        return new ArrayList<>();
//                    }
//                }, executorService);
//
//                // --- SimplyHired ---
//                CompletableFuture<List<Job>> simplyHiredFuture = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        List<Job> jobs = simplyHiredScraperService.scrapeJobs(jobTitle, null);
//                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
//                        System.out.println("SimplyHired jobs scrapped successfully..!! ");
//                        return jobs;
//                    } catch (Exception e) {
//                        return new ArrayList<>();
//                    }
//                }, executorService);
//
//                // --- ✅ Adzuna ---

    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {
        // Increase timeout to 5 minutes
        SseEmitter emitter = new SseEmitter(300000L);

        ExecutorService streamExecutor = Executors.newFixedThreadPool(10); // increased pool size

        CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                // --- Python Naukri Scraper ---
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        String pythonApiUrl = PYTHON_SCRAPER_BASE_URL + "/scrape/?keyword=" + jobTitle.replace(" ", "%20");
                        ResponseEntity<Job[]> response = restTemplate.getForEntity(pythonApiUrl, Job[].class);
                        if (response.getBody() != null) {
                            List<Job> jobs = new ArrayList<>(Arrays.asList(response.getBody()));
                            sendInChunks(emitter, jobs, "Naukri");
                        }
                        System.out.println("Naukri jobs scrapped successfully..!!");
                    } catch (Exception e) {
                        System.err.println("Error in Python Naukri scraper: " + e.getMessage());
                    }
                }, streamExecutor));

                // --- Shine ---
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Job> jobs = shineScraperService.scrape(jobTitle);
                        sendInChunks(emitter, jobs, "Shine");
                        System.out.println("Shine jobs scrapped successfully..!!");
                    } catch (Exception e) {
                        try {
                            emitter.send(SseEmitter.event().name("error").data("Shine scraper failed: " + e.getMessage()));
                        } catch (IOException ioException) {
                            emitter.completeWithError(ioException);
                        }
                    }

                }, streamExecutor));

                // --- TimesJobs ---
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Job> jobs = timesJobsScraperService.scrapeJobs(jobTitle);
                        sendInChunks(emitter, jobs, "TimesJobs");
                        System.out.println("TimesJobs jobs scrapped successfully..!!");
                    } catch (Exception e) {
                        System.err.println("Error in TimesJobs scraper: " + e.getMessage());
                    }
                }, streamExecutor));

                // --- Freshersworld ---
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Job> jobs = freshersworldScraperService.scrapeJobs(jobTitle);
                        sendInChunks(emitter, jobs, "FreshersWorld");
                        System.out.println("FreshersWorld jobs scrapped successfully..!!");
                    } catch (Exception e) {
                        System.err.println("Error in FreshersWorld scraper: " + e.getMessage());
                    }
                }, streamExecutor));

//                CompletableFuture<List<JobResponseDTO>> adzunaFuture = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        List<JobResponseDTO> jobs = adzunaScraperService.scrapeJobs(jobTitle);
//                        emitter.send(SseEmitter.event().name("jobs").data(jobs));
//                        System.out.println("Adzuna jobs scrapped successfully..!! ");
//                        return jobs;
//                    } catch (Exception e) {
//                        System.err.println("Error in Adzuna scraper: " + e.getMessage());
//                        return new ArrayList<>();
//                    }
//                }, executorService);

                // --- SimplyHired ---
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Job> jobs = simplyHiredScraperService.scrapeJobs(jobTitle, null);
                        sendInChunks(emitter, jobs, "SimplyHired");
                        System.out.println("SimplyHired jobs scrapped successfully..!!");
                    } catch (Exception e) {
                        System.err.println("Error in SimplyHired scraper: " + e.getMessage());
                    }
                }, streamExecutor));

                // --- Python LinkedIn Scraper ---
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        String linkedinApiUrl = PYTHON_SCRAPER_LINKEDIN + "/scrape?keyword=" + jobTitle.replace(" ", "%20");
                        ResponseEntity<Job[]> response = restTemplate.getForEntity(linkedinApiUrl, Job[].class);
                        if (response.getBody() != null) {
                            List<Job> jobs = new ArrayList<>(Arrays.asList(response.getBody()));
                            jobs.forEach(job -> job.setPlatform("LinkedIn"));
                            sendInChunks(emitter, jobs, "LinkedIn");
                        }
                        System.out.println("LinkedIn jobs scrapped successfully..!!");
                    } catch (Exception e) {
                        System.err.println("Error in Python LinkedIn scraper: " + e.getMessage());
                    }
                }, streamExecutor));

                // Wait for all scrapers to finish
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((res, ex) -> {
                    try {
                        if (ex != null) {
                            emitter.send(SseEmitter.event().name("error").data("Some scrapers failed"));
                        }
                        emitter.send(SseEmitter.event().name("complete").data("All job searches completed"));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, streamExecutor);

        return emitter;
    }

    // --- Helper method: send jobs in chunks ---
    private void sendInChunks(SseEmitter emitter, List<Job> jobs, String platform) throws IOException {
        int chunkSize = 20; // send 20 jobs at a time
        for (int i = 0; i < jobs.size(); i += chunkSize) {
            List<Job> chunk = jobs.subList(i, Math.min(i + chunkSize, jobs.size()));
            chunk.forEach(job -> job.setPlatform(platform));
            emitter.send(SseEmitter.event().name("jobs").data(chunk));
        }
    }


//
//                CompletableFuture<Void> pythonLinkedinFuture = CompletableFuture.runAsync(() -> {
//                    try {
//                        String linkedinApiUrl = PYTHON_SCRAPER_LINKEDIN + "/scrape?keyword=" + jobTitle.replace(" ", "%20");
//                        ResponseEntity<Job[]> response = restTemplate.getForEntity(linkedinApiUrl, Job[].class);
//                        if (response.getBody() != null) {
//                                List<Job> jobs = List.of(response.getBody());
//
//                                // ✅ Add platform field
//                                jobs.forEach(job -> job.setPlatform("LinkedIn"));
//
//                            // Send to SSE emitter
//                            emitter.send(SseEmitter.event().name("jobs").data(jobs));
//                        }
//                        System.out.println("Linkedin python jobs scrapped successfully..!! ");
//                    } catch (Exception e) {
//                        System.err.println("Error in Python linkedin scraper: " + e.getMessage());
//                    }
//                }, executorService);
//
//
//                // ✅ Complete after all finish
//                CompletableFuture.allOf(
//                         shineFuture, timesJobsFuture, freshersworldFuture, simplyHiredFuture, pythonLinkedinFuture
//                ).whenComplete((res, ex) -> {
//                    try {
//                        if (ex != null) {
//                            emitter.send(SseEmitter.event().name("error").data("Some scrapers failed"));
//                        }
//                        emitter.send(SseEmitter.event().name("complete").data("All job searches completed"));
//                        emitter.complete();
//                    } catch (IOException e) {
//                        emitter.completeWithError(e);
//                    }
//                });
//
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        }, executorService);
//
//        return emitter;
//    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }
}
