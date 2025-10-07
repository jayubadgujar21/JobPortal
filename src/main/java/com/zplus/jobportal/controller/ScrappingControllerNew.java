//package com.zplus.jobportal.controller;
//
//import com.zplus.jobportal.dto.response.JobResponseDTO;
//import com.zplus.jobportal.model.Job;
//import com.zplus.jobportal.services.*;
//import jakarta.annotation.PreDestroy;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@RestController
//class ScrapingControllerNew {
//
//    private final ShineScrapeService shineScraperService;
//    private final TimesJobsScraperService timesJobsScraperService;
//    private final FreshersWorldScraperService freshersworldScraperService;
//    private final SimplyHiredScraperService simplyHiredScraperService;
//    private final AdzunaScraperService adzunaScraperService;
//    private final ReedScraperService reedScraperService;
//    private final UpdazzScraperService updazzScraperService;
//
//    // Increased thread pool for better parallelism
//    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
//
//    // Separate executor for streaming
//    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // ✅ Base URL for deployed Python scraper
//    private static final String PYTHON_SCRAPER_BASE_URL = "https://job-scrapper-python.onrender.com";
//    private static final String PYTHON_SCRAPER_LINKEDIN = "https://linkedin-scrapper-p7jy.onrender.com";
//
//    // Timeout constants
//    private static final long SCRAPER_TIMEOUT_MS = 45000; // 45 seconds per scraper
//    private static final long SSE_TIMEOUT_MS = 300000; // 5 minutes total
//
//    // Track sent jobs to avoid duplicates
//    private final Set<String> sentJobIds = ConcurrentHashMap.newKeySet();
//    private final AtomicInteger totalSentJobs = new AtomicInteger(0);
//
//    public ScrapingControllerNew(
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
//    // ✅ Enhanced SSE endpoint with guaranteed delivery
//    @GetMapping(value = "/stream-all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamAllJobs(@RequestParam String jobTitle) {
//        // Reset tracking for new request
//        sentJobIds.clear();
//        totalSentJobs.set(0);
//
//        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
//
//        // Configure emitter for better performance
//        emitter.onCompletion(() -> {
//            System.out.println("🎯 SSE Completed. Total jobs sent: " + totalSentJobs.get());
//            sentJobIds.clear();
//        });
//
//        emitter.onTimeout(() -> {
//            System.out.println("⏰ SSE Timeout. Total jobs sent: " + totalSentJobs.get());
//            sentJobIds.clear();
//        });
//
//        emitter.onError((ex) -> {
//            System.err.println("❌ SSE Error: " + ex.getMessage());
//            sentJobIds.clear();
//        });
//
//        streamExecutor.execute(() -> {
//            try {
//                List<CompletableFuture<Void>> allFutures = new ArrayList<>();
//                List<String> successfulScrapers = Collections.synchronizedList(new ArrayList<>());
//                List<String> failedScrapers = Collections.synchronizedList(new ArrayList<>());
//
//                System.out.println("🚀 Starting scrapers for: " + jobTitle);
//
//                // 1. Python Naukri Scraper - Fast and reliable
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "Naukri";
//                    try {
//                        String pythonApiUrl = PYTHON_SCRAPER_BASE_URL + "/scrape/?keyword=" +
//                                jobTitle.replace(" ", "%20");
//                        System.out.println("🔗 Calling: " + pythonApiUrl);
//
//                        ResponseEntity<Job[]> response = restTemplate.getForEntity(pythonApiUrl, Job[].class);
//
//                        if (response.getBody() != null && response.getBody().length > 0) {
//                            List<Job> jobs = new ArrayList<>(Arrays.asList(response.getBody()));
//                            int sentCount = sendJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 2. LinkedIn Scraper - Fast and reliable
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "LinkedIn";
//                    try {
//                        String linkedinApiUrl = PYTHON_SCRAPER_LINKEDIN + "/scrape?keyword=" +
//                                jobTitle.replace(" ", "%20");
//                        System.out.println("🔗 Calling: " + linkedinApiUrl);
//
//                        ResponseEntity<Job[]> response = restTemplate.getForEntity(linkedinApiUrl, Job[].class);
//
//                        if (response.getBody() != null && response.getBody().length > 0) {
//                            List<Job> jobs = new ArrayList<>(Arrays.asList(response.getBody()));
//                            jobs.forEach(job -> job.setPlatform("LinkedIn"));
//                            int sentCount = sendJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 3. Adzuna Scraper - Fast API
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "Adzuna";
//                    try {
//                        List<JobResponseDTO> jobs = adzunaScraperService.scrapeJobs(jobTitle);
//                        if (jobs != null && !jobs.isEmpty()) {
//                            // Convert to Job objects or send as DTO
//                            int sentCount = sendAdzunaJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 4. FreshersWorld Scraper
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "FreshersWorld";
//                    try {
//                        List<Job> jobs = freshersworldScraperService.scrapeJobs(jobTitle);
//                        if (jobs != null && !jobs.isEmpty()) {
//                            int sentCount = sendJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 5. Shine Scraper
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "Shine";
//                    try {
//                        List<Job> jobs = shineScraperService.scrape(jobTitle);
//                        if (jobs != null && !jobs.isEmpty()) {
//                            int sentCount = sendJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 6. TimesJobs Scraper
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "TimesJobs";
//                    try {
//                        List<Job> jobs = timesJobsScraperService.scrapeJobs(jobTitle);
//                        if (jobs != null && !jobs.isEmpty()) {
//                            int sentCount = sendJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 7. SimplyHired Scraper
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "SimplyHired";
//                    try {
//                        List<Job> jobs = simplyHiredScraperService.scrapeJobs(jobTitle, null);
//                        if (jobs != null && !jobs.isEmpty()) {
//                            int sentCount = sendJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // 8. Updazz Scraper
//                allFutures.add(CompletableFuture.runAsync(() -> {
//                    String scraperName = "Updazz";
//                    try {
//                        List<JobResponseDTO> jobs = updazzScraperService.scrapeJobs(jobTitle);
//                        if (jobs != null && !jobs.isEmpty()) {
//                            int sentCount = sendUpdazzJobsSafely(emitter, jobs, scraperName);
//                            successfulScrapers.add(scraperName + "(" + sentCount + " jobs)");
//                            System.out.println("✅ " + scraperName + " completed: " + sentCount + "/" + jobs.size() + " jobs sent");
//                        } else {
//                            System.out.println("⚠️ " + scraperName + " returned no jobs");
//                        }
//                    } catch (Exception e) {
//                        failedScrapers.add(scraperName);
//                        System.err.println("❌ Error in " + scraperName + " scraper: " + e.getMessage());
//                        sendErrorSafely(emitter, scraperName + " failed: " + e.getMessage());
//                    }
//                }, executorService).orTimeout(SCRAPER_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//
//                // Wait for ALL futures to complete (success or failure)
//                CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]))
//                        .exceptionally(ex -> null) // Ignore exceptions at this level
//                        .thenAccept(v -> {
//                            try {
//                                // Final summary
//                                String summary = String.format(
//                                        "🎯 Scraping Complete! Successful: %s | Failed: %s | Total Jobs Sent: %d",
//                                        successfulScrapers, failedScrapers, totalSentJobs.get()
//                                );
//
//                                emitter.send(SseEmitter.event()
//                                        .name("summary")
//                                        .data(summary));
//
//                                emitter.send(SseEmitter.event()
//                                        .name("complete")
//                                        .data(totalSentJobs.get() + " jobs collected"));
//
//                                System.out.println(summary);
//                                emitter.complete();
//
//                            } catch (IOException e) {
//                                System.err.println("Error sending completion: " + e.getMessage());
//                                emitter.completeWithError(e);
//                            } finally {
//                                sentJobIds.clear();
//                            }
//                        });
//
//            } catch (Exception e) {
//                try {
//                    emitter.send(SseEmitter.event()
//                            .name("error")
//                            .data("Initialization error: " + e.getMessage()));
//                    emitter.complete();
//                } catch (IOException ex) {
//                    emitter.completeWithError(ex);
//                } finally {
//                    sentJobIds.clear();
//                }
//            }
//        });
//
//        return emitter;
//    }
//
//    // ✅ Safe job sending with duplicate prevention
//    private int sendJobsSafely(SseEmitter emitter, List<Job> jobs, String platform) {
//        if (jobs == null || jobs.isEmpty()) {
//            return 0;
//        }
//
//        int sentCount = 0;
//        List<Job> uniqueJobs = new ArrayList<>();
//
//        // Filter duplicates and set platform
//        for (Job job : jobs) {
//            if (job == null) continue;
//
//            // Create unique ID based on title + company + location
//            String jobId = generateJobId(job);
//
//            if (!sentJobIds.contains(jobId)) {
//                sentJobIds.add(jobId);
//                if (job.getPlatform() == null) {
//                    job.setPlatform(platform);
//                }
//                uniqueJobs.add(job);
//            }
//        }
//
//        // Send in small chunks with error handling
//        int chunkSize = 5; // Smaller chunks for reliability
//        for (int i = 0; i < uniqueJobs.size(); i += chunkSize) {
//            List<Job> chunk = uniqueJobs.subList(i, Math.min(i + chunkSize, uniqueJobs.size()));
//
//            try {
//                emitter.send(SseEmitter.event()
//                        .name("jobs")
//                        .data(chunk));
//
//                sentCount += chunk.size();
//                totalSentJobs.addAndGet(chunk.size());
//
//                // Small delay to prevent overwhelming
//                Thread.sleep(30);
//
//            } catch (IOException e) {
//                System.err.println("❌ Client disconnected while sending " + platform + " jobs");
//                break;
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//
//        return sentCount;
//    }
//
//    // ✅ Safe sending for Adzuna jobs
//    private int sendAdzunaJobsSafely(SseEmitter emitter, List<JobResponseDTO> jobs, String platform) {
//        if (jobs == null || jobs.isEmpty()) {
//            return 0;
//        }
//
//        int sentCount = 0;
//        List<JobResponseDTO> uniqueJobs = new ArrayList<>();
//
//        // Filter duplicates
//        for (JobResponseDTO job : jobs) {
//            if (job == null) continue;
//
//            String jobId = generateJobId(job);
//            if (!sentJobIds.contains(jobId)) {
//                sentJobIds.add(jobId);
//                uniqueJobs.add(job);
//            }
//        }
//
//        // Send in chunks
//        int chunkSize = 5;
//        for (int i = 0; i < uniqueJobs.size(); i += chunkSize) {
//            List<JobResponseDTO> chunk = uniqueJobs.subList(i, Math.min(i + chunkSize, uniqueJobs.size()));
//
//            try {
//                emitter.send(SseEmitter.event()
//                        .name("adzuna-jobs")
//                        .data(chunk));
//
//                sentCount += chunk.size();
//                totalSentJobs.addAndGet(chunk.size());
//
//                Thread.sleep(30);
//
//            } catch (IOException e) {
//                System.err.println("❌ Client disconnected while sending " + platform + " jobs");
//                break;
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//
//        return sentCount;
//    }
//
//    // ✅ Safe sending for Updazz jobs
//    private int sendUpdazzJobsSafely(SseEmitter emitter, List<JobResponseDTO> jobs, String platform) {
//        // Similar implementation to Adzuna
//        return sendAdzunaJobsSafely(emitter, jobs, platform);
//    }
//
//    // ✅ Generate unique job ID to prevent duplicates
//    private String generateJobId(Job job) {
//        return (job.getJobTitle() + "_" + job.getCompany() + "_" + job.getLocation()).toLowerCase().hashCode() + "";
//    }
//
//    private String generateJobId(JobResponseDTO job) {
//        return (job.getJobTitle() + "_" + job.getCompany() + "_" + job.getLocation()).toLowerCase().hashCode() + "";
//    }
//
//    // ✅ Safe error sending
//    private void sendErrorSafely(SseEmitter emitter, String error) {
//        try {
//            emitter.send(SseEmitter.event()
//                    .name("scraper-error")
//                    .data(error));
//        } catch (IOException e) {
//            System.err.println("Failed to send error: " + e.getMessage());
//        }
//    }
//
//    @PreDestroy
//    public void shutdownExecutors() {
//        System.out.println("Shutting down executors...");
//        executorService.shutdown();
//        streamExecutor.shutdown();
//        try {
//            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
//                executorService.shutdownNow();
//            }
//            if (!streamExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
//                streamExecutor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executorService.shutdownNow();
//            streamExecutor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//}