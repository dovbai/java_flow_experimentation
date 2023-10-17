package stockpriceservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PublisherTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(PublisherTask.class);
    final private StockServicesConsumerData consumerData;
    final private StockData stockData;

    public PublisherTask(StockServicesConsumerData consumerData, StockData stockData) {
        this.consumerData = consumerData;
        this.stockData = stockData;

        logger.info("Constructing PublisherTask for stockDate: " + stockData.toString());
    }

    /**
     *
     */
    @Override
    public void run() {
        StockSubscription subscription = consumerData.getSubscription();

        boolean isUpdate = !subscription.isCancelled() && subscription.getRequested().get() > 0
                && subscription.hasStock(stockData.getStock());

        Consumer consumer = (Consumer) consumerData.getSubscriber();
        logger.info("isUpdate: " + isUpdate + " for stock: " + stockData.getStock() + " consumer: " +
                consumer.getName());
        if( isUpdate ) {
            consumerData.getSubscriber().onNext(stockData);
            subscription.decreaseRequested();
        }
    }
}
