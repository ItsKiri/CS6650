import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Client2 {

    private static AtomicInteger totalRequests = new AtomicInteger(0);
    private static final String CSV_FILE = "ApiStressTest.csv";
    private static BufferedWriter csvWriter;

    public static void main(String[] args) throws InterruptedException, IOException {
        // Extract arguments
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]) * 1000;  // converted to milliseconds
        String IPAddr = args[3];

        // Initialize CSV writer
        csvWriter = new BufferedWriter(new FileWriter(CSV_FILE));

        // Initialization phase
        CountDownLatch initLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    sendRequest("http://" + IPAddr + "/AlbumStore/albums", "POST", false);
                    sendRequest("http://" + IPAddr + "/AlbumStore/albums/fixedAlbumKey123", "GET", false);
                }
                initLatch.countDown();
            });
            thread.start();
        }

        // Wait for initialization threads to finish
        initLatch.await();

        CountDownLatch groupLatch = new CountDownLatch(threadGroupSize * numThreadGroups);
        long startTime = System.currentTimeMillis();

        // Thread groups
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                Thread thread = new Thread(() -> {
                    for (int k = 0; k < 1000; k++) {
                        sendRequest("http://" + IPAddr + "/AlbumStore/albums", "POST", true);
                        sendRequest("http://" + IPAddr + "/AlbumStore/albums/fixedAlbumKey123", "GET", true);
                    }
                    groupLatch.countDown();
                });
                thread.start();
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        groupLatch.await();

        long endTime = System.currentTimeMillis();
        double wallTime = (endTime - startTime) / 1000.0;
        double throughput = totalRequests.get() / wallTime;
        System.out.println("Wall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests/second");

        csvWriter.flush();
        csvWriter.close();



        computeAndPrintStatistics("POST");
        computeAndPrintStatistics("GET");
    }

    private static void sendRequest(String targetURL, String method, Boolean flag) {
        int retries = 5;
        HttpURLConnection httpURLConnection = null;
        if (flag) {
            totalRequests.incrementAndGet();
        }
        while (retries > 0) {
            InputStream inputStream = null;
            try {
                URL url = new URL(targetURL);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                long start = 0;
                if (flag) {
                    start = System.currentTimeMillis();
                }
                httpURLConnection.setRequestMethod(method);

                int responseCode = httpURLConnection.getResponseCode();
                inputStream = httpURLConnection.getInputStream();

                if (flag) {
                    long end = System.currentTimeMillis();
                    long latency = end - start;
                    writeToCSV(start, method, latency, responseCode);
                }

                if (responseCode >= 200 && responseCode < 300) {
                    return;
                } else if (responseCode >= 400 && responseCode < 600) {
                    retries--;
                }

            } catch (Exception e) {
                e.printStackTrace();
                retries--;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }
    }

    private synchronized static void writeToCSV(long start, String method, long latency, int responseCode) throws IOException {
        csvWriter.write(start + "," + method + "," + latency + "," + responseCode);
        csvWriter.newLine();
    }

    private static void computeAndPrintStatistics(String method) throws IOException {
        List<Long> latencies = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(method)) {
                    latencies.add(Long.parseLong(parts[2]));
                }
            }
        }

        Collections.sort(latencies);
        double mean = latencies.stream().mapToLong(val -> val).average().orElse(0.0);
        long median = latencies.get(latencies.size() / 2);
        long p99 = latencies.get((int) (latencies.size() * 0.99));
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);

        System.out.println("Statistics for " + method + ":");
        System.out.println("Mean response time: " + mean + "ms");
        System.out.println("Median response time: " + median + "ms");
        System.out.println("P99 response time: " + p99 + "ms");
        System.out.println("Min response time: " + min + "ms");
        System.out.println("Max response time: " + max + "ms");
    }
}