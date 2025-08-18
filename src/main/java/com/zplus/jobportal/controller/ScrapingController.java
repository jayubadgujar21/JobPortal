package com.zplus.jobportal.controller;



import com.zplus.jobportal.model.Job;

import com.zplus.jobportal.services.SimplyHiredScraperService;
import com.zplus.jobportal.services.FreshersWorldScraperService;
import com.zplus.jobportal.services.ShineScrapeService;
import com.zplus.jobportal.services.TimesJobsScraperService;


import jakarta.annotation.PreDestroy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final ShineScrapeService shineScraperService;//-
    private final TimesJobsScraperService timesJobsScraperService;//-
    private final FreshersWorldScraperService freshersworldScraperService;//-
    private final SimplyHiredScraperService simplyHiredScraperService;//-

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public ScrapingController(
            ShineScrapeService shineScraperService,
            TimesJobsScraperService timesJobsScraperService,
            FreshersWorldScraperService freshersworldScraperService,
            SimplyHiredScraperService simplyHiredScraperService
    )
    {


        this.shineScraperService = shineScraperService;
        this.timesJobsScraperService = timesJobsScraperService;
        this.freshersworldScraperService = freshersworldScraperService;
        this.simplyHiredScraperService = simplyHiredScraperService;

    }


//    @GetMapping("/naukri")
//    public List<Job> getNaukriJobs(@RequestParam String jobTitle) {
//        System.out.println("Starting Naukri scrape for: " + jobTitle);
//        return naukriScraperService.scrape(jobTitle);
//    }

    //    @GetMapping("/upwork")
//    public List<Job> getUpworkJobs(@RequestParam String jobTitle) {
//        System.out.println("Starting Upwork scrape for: " + jobTitle);
//        return upworkScraperService.scrapeJobs(jobTitle);
//    }
    @GetMapping("/simplyhired")
    public List<Job> getSimplyHiredJobs(@RequestParam String jobTitle, @RequestParam(required = false) String location) {
        System.out.println("Starting SimplyHired scrape for: " + jobTitle + ", location: " + location);
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
        System.out.println("Starting Shine scrape for: " + jobTitle);
        return shineScraperService.scrape(jobTitle);
    }
    @GetMapping("/python-naukri")
    public ResponseEntity<String> getPythonNaukriJobs(@RequestParam String jobTitle) {
        String pythonApiUrl = "http://127.0.0.1:10000/scrape/?keyword=" + jobTitle.replace(" ", "%20");
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(pythonApiUrl, String.class);
        return ResponseEntity.ok(response);
    }
    /**
     * Stream job results as they become available from each platform
     */
    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {
        System.out.println("Streaming jobs from all platforms for: " + jobTitle);

        SseEmitter emitter = new SseEmitter(180000L); // 3 minute timeout

        // Run all scrapers in parallel and stream results as they arrive
        CompletableFuture.runAsync(() -> {
            try {
                // Start all scrapers in parallel

                // Python FastAPI scraper
                CompletableFuture<Void> pythonNaukriFuture = CompletableFuture.runAsync(() -> {
                    System.out.println("Starting Python Naukri scrape...");
                    try {
                        String pythonApiUrl = "http://127.0.0.1:8000/scrape/?keyword=" + jobTitle.replace(" ", "%20");
                        RestTemplate restTemplate = new RestTemplate();
                        String response = restTemplate.getForObject(pythonApiUrl, String.class);
                        // System.err.println("->>>>>>>>>>>"+response);
                        // Stream the raw JSON as a separate SSE event
                        emitter.send(SseEmitter.event().name("jobs").data(response));
                        System.out.println("Python Naukri jobs sent");
                    } catch (Exception e) {
                        System.err.println("Error in Python Naukri scraper: " + e.getMessage());
                    }
                }, executorService);




                CompletableFuture<List<Job>> shineFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("Starting Shine scrape...");
                    try {
                        List<Job> jobs = shineScraperService.scrape(jobTitle);
                        System.out.println("Shine jobs found: " + jobs.size());
                        // Send jobs immediately to client
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("jobs")
                                    .data(jobs));
                        } catch (IOException e) {
                            System.err.println("Error sending Shine jobs: " + e.getMessage());
                        }
                        return jobs;
                    } catch (Exception e) {
                        System.err.println("Error in Shine scraper: " + e.getMessage());
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture<List<Job>> timesJobsFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("Starting TimesJobs scrape...");
                    try {
                        List<Job> jobs = timesJobsScraperService.scrapeJobs(jobTitle);
                        System.out.println("TimesJobs jobs found: " + jobs.size());
                        // Send jobs immediately to client
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("jobs")
                                    .data(jobs));
                        } catch (IOException e) {
                            System.err.println("Error sending TimesJobs jobs: " + e.getMessage());
                        }
                        return jobs;
                    } catch (Exception e) {
                        System.err.println("Error in TimesJobs scraper: " + e.getMessage());
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture<List<Job>> freshersworldFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("Starting Freshersworld scrape...");
                    try {
                        List<Job> jobs = freshersworldScraperService.scrapeJobs(jobTitle);
                        System.out.println("Freshersworld jobs found: " + jobs.size());
                        // Send jobs immediately to client
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("jobs")
                                    .data(jobs));
                        } catch (IOException e) {
                            System.err.println("Error sending Freshersworld jobs: " + e.getMessage());
                        }
                        return jobs;
                    } catch (Exception e) {
                        System.err.println("Error in Freshersworld scraper: " + e.getMessage());
                        return new ArrayList<>();
                    }
                }, executorService);

                CompletableFuture<List<Job>> simplyHiredFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("Starting SimplyHired scrape...");
                    try {
                        List<Job> jobs = simplyHiredScraperService.scrapeJobs(jobTitle, null); // or pass location if needed
                        System.out.println("SimplyHired jobs found: " + jobs.size());
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("jobs")
                                    .data(jobs));
                        } catch (IOException e) {
                            System.err.println("Error sending SimplyHired jobs: " + e.getMessage());
                        }
                        return jobs;
                    } catch (Exception e) {
                        System.err.println("Error in SimplyHired scraper: " + e.getMessage());
                        return new ArrayList<>();
                    }
                }, executorService);
                // When all scrapers are done, complete the emitter
                CompletableFuture.allOf(pythonNaukriFuture, shineFuture,timesJobsFuture,freshersworldFuture,simplyHiredFuture)
                        .thenRun(() -> {
                            System.out.println("All scrapers completed");
                            try {
                                // Send a completion event
                                emitter.send(SseEmitter.event()
                                        .name("complete")
                                        .data("All job searches completed"));

                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .exceptionally(e -> {
                            emitter.completeWithError(e);
                            return null;
                        });

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    /**
     * Stream all jobs mixed together as they become available
     */
    // Remove individual CrossOrigin annotation as it's handled globally in WebConfig


    /**
     * Stream all jobs as a single mixed array once all scrapers complete
     */


    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }


}