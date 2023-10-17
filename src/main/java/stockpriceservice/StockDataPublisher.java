package stockpriceservice;

import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StockDataPublisher implements Flow.Publisher<StockData> {
    private static final Logger logger = LogManager.getLogger(StockDataPublisher.class);

    private ConcurrentLinkedDeque<StockServicesConsumerData> consumers;
    //private ThreadPoolExecutor executor;
    private ExecutorService executor;

    public StockDataPublisher() {
        consumers = new ConcurrentLinkedDeque<>();

        int numProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("Constructing StockDataPublisher object with " + numProcessors + " processors");
        //executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numProcessors);
        executor = Executors.newFixedThreadPool(numProcessors);
    }

    /**
     * @param subscriber subscriber to subscribe to
     */
    @Override
    public void subscribe(Flow.Subscriber<? super StockData> subscriber) {
        StockSubscription subscription = new StockSubscription();
        Consumer consumer = (Consumer) subscriber;

        StockServicesConsumerData stockServicesConsumerData =
                new StockServicesConsumerData(consumer, subscription);

        subscriber.onSubscribe( subscription);
        logger.info("Added subscriber: " + consumer.getName() );
        consumers.add(stockServicesConsumerData);
    }

    synchronized public void publish(StockData stockData ) {
        consumers.forEach(
                consumerData -> {
                    try {
                        logger.info("Spawning thread for processing " + stockData);
                        executor.execute(new PublisherTask(consumerData, stockData));
                    } catch (Exception e) {
                        consumerData.getSubscriber().onError(e);
                    }
                }
        );
    }

    public void shutdown() {
        consumers.forEach( consumerData -> {
            consumerData.getSubscriber().onComplete();
        });
        executor.shutdown();
        boolean shutdownCompleted = false;

        try {
            shutdownCompleted = executor.awaitTermination( 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Caught InterruptedException during shutdown ", e);
        } finally {
            logger.info("shutdownCompleted: " + shutdownCompleted);
        }
    }
}
