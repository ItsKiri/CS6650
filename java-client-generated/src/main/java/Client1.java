import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Client1 {

    private static AtomicInteger totalRequests = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        // Extract arguments
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]) * 1000;  // converted to milliseconds
        String IPAddr = args[3];

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
    }

    private static void sendRequest(String targetURL, String method, Boolean flag) {
        int retries = 5;
        HttpURLConnection httpURLConnection = null;

        while (retries > 0) {
            InputStream inputStream = null;
            try {
                URL url = new URL(targetURL);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod(method);

                if (flag) {
                    totalRequests.incrementAndGet();
                }

                int responseCode = httpURLConnection.getResponseCode();
                inputStream = httpURLConnection.getInputStream();


                if (responseCode >= 200 && responseCode < 300) {
                    // success
                    return;
                } else if (responseCode >= 400 && responseCode < 600) {
                    // retry if it's a 4xx or 5xx error
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

}