import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stockpriceservice.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

public class TestStockService {
    private static final Logger logger = LogManager.getLogger(TestStockService.class);

    private StockSubscription getStockSubscription() {
        StockSubscription stockSubscription = new StockSubscription();

        Set<Stock> stockSet = new HashSet<>();
        stockSet.add(Stock.AAPL);
        stockSet.add(Stock.GOOGL);

        stockSubscription.setStockSet(stockSet);
        return stockSubscription;
    }

    @Test
    public void testStockData () {
        Stock stock = Stock.AAPL;
        double price = 10;

        StockData stockData = new StockData(stock, price);

        Assertions.assertEquals(stock, stockData.getStock());
        Assertions.assertEquals(price, stockData.getPrice());
    }

    @Test
    public void testStockSubscriptionSetup() {

        StockSubscription stockSubscription = getStockSubscription();
        Assertions.assertTrue(stockSubscription.hasStock(Stock.AAPL));
        Assertions.assertFalse(stockSubscription.hasStock(Stock.KO));
    }

    @Test
    public void testStockSubscriptionCancel() {
        StockSubscription stockSubscription = getStockSubscription();

        Assertions.assertFalse(stockSubscription.isCancelled());

        stockSubscription.cancel();

        Assertions.assertTrue(stockSubscription.isCancelled());
    }

    @Test
    public void testStockSubscriptionRequest() {
        StockSubscription stockSubscription = getStockSubscription();
        Assertions.assertEquals(0, stockSubscription.getRequested().get());

        stockSubscription.request(1);
        Assertions.assertEquals(1, stockSubscription.getRequested().get());

        stockSubscription.decreaseRequested();
        Assertions.assertEquals(0, stockSubscription.getRequested().get());
    }

    @Test
    public void testConsumer() {
        Set<Stock> stockSet = new HashSet<>();
        stockSet.add(Stock.AAPL);
        stockSet.add(Stock.GOOGL);

        Consumer testConsumer = new Consumer("testConsumer", stockSet);

        Assertions.assertEquals("testConsumer", testConsumer.getName());

        Flow.Subscription subscription = new StockSubscription();

        testConsumer.onSubscribe(subscription);

        StockSubscription stockSubscription = (StockSubscription) subscription;

        Assertions.assertTrue(stockSubscription.hasStock(Stock.AAPL));
        Assertions.assertEquals(1, stockSubscription.getRequested().get());

        StockData stockData = new StockData(Stock.AAPL, 10);
        testConsumer.onNext(stockData);

        Assertions.assertEquals(10, testConsumer.getMaxValue(Stock.AAPL));
    }

    @Test
    public void testStockServicesConsumerData (){
        Set<Stock> stockSet = new HashSet<>();
        stockSet.add(Stock.AAPL);
        stockSet.add(Stock.GOOGL);

        Consumer testConsumer = new Consumer("testConsumer", stockSet);

        Assertions.assertEquals("testConsumer", testConsumer.getName());

        Flow.Subscription subscription = new StockSubscription();

        testConsumer.onSubscribe(subscription);

        StockServicesConsumerData data = new StockServicesConsumerData( testConsumer,
                (StockSubscription)subscription);

        Assertions.assertEquals( testConsumer, data.getSubscriber());
        Assertions.assertEquals( subscription, data.getSubscription());
    }

    @Test
    public void testPublisherTask() {
        // Prepare consumer data
        Set<Stock> stockSet = new HashSet<>();
        stockSet.add(Stock.AAPL);
        stockSet.add(Stock.GOOGL);

        Consumer testConsumer = new Consumer("testConsumer", stockSet);

        Assertions.assertEquals("testConsumer", testConsumer.getName());

        Flow.Subscription subscription = new StockSubscription();

        testConsumer.onSubscribe(subscription);

        Assertions.assertEquals( testConsumer.getMaxValue(Stock.AAPL), 0);

        StockServicesConsumerData consumerData = new StockServicesConsumerData( testConsumer,
                (StockSubscription)subscription);

        // set a stock price
        StockData stockData = new StockData(Stock.AAPL, 10);

        PublisherTask publisherTask = new PublisherTask(consumerData, stockData);

        publisherTask.run();
        Assertions.assertEquals( testConsumer.getMaxValue(Stock.AAPL), 10);

    }

    @Test
    public void testStockDataPublisher() {
        StockDataPublisher stockDataPublisher =
                new StockDataPublisher();

        // Prepare subscriber (consumer)
        Set<Stock> stockSet = new HashSet<>();
        stockSet.add(Stock.AAPL);
        stockSet.add(Stock.GOOGL);

        Consumer testConsumer = new Consumer("testConsumer", stockSet);

        stockDataPublisher.subscribe( testConsumer);

        // Make new data and publish
        StockData stockData = new StockData(Stock.AAPL, 10);
        stockDataPublisher.publish(stockData);

//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            logger.error("Caught InterruptedException", e);
//        }

        // Check new stock price
        double newValue = testConsumer.getMaxValue(Stock.AAPL);
        Assertions.assertEquals(10, testConsumer.getMaxValue(Stock.AAPL));
    }

    @Test
    public void testAllService() {
        StockDataPublisher stockDataPublisher =
                new StockDataPublisher();

        // Prepare 3 consumers and subscribe through publisher
        Set<Stock> stockSet1 = new HashSet<>();
        stockSet1.add(Stock.AAPL);
        stockSet1.add(Stock.MSFT);
        stockSet1.add(Stock.GOOGL);
        Consumer technologyConsumer = new Consumer("technology consumer", stockSet1);
        stockDataPublisher.subscribe(technologyConsumer);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Set<Stock> stockSet2 = new HashSet<>();
        stockSet2.add(Stock.KO);
        stockSet2.add(Stock.PG);
        Consumer staplesConsumer = new Consumer("staples consumer", stockSet2);
        stockDataPublisher.subscribe(staplesConsumer);

//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        Set<Stock> stockSet3 = new HashSet<>();
        stockSet3.add(Stock.AAPL);
        stockSet3.add(Stock.MSFT);
        stockSet3.add(Stock.GOOGL);
        stockSet3.add(Stock.KO);
        stockSet3.add(Stock.PG);

        Consumer everythingConsumer = new Consumer("everything consumer", stockSet3);
        stockDataPublisher.subscribe(everythingConsumer);
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        // Data for updates
        Stock[] stocks = {Stock.AAPL, Stock.KO};
        double[] prices = {1, 2};

        for (int i = 0; i < stocks.length; ++i) {
            StockData stockData = new StockData(stocks[i], prices[i]);

            stockDataPublisher.publish(stockData);

//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }

        // Shutdown service
        logger.info("Shutting down service");
        stockDataPublisher.shutdown();

//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        // Check for updates
        Assertions.assertEquals(1, technologyConsumer.getMaxValue(Stock.AAPL));
        Assertions.assertEquals(1, everythingConsumer.getMaxValue(Stock.AAPL));
        Assertions.assertEquals(2, staplesConsumer.getMaxValue(Stock.KO));
        return;
    }
}
