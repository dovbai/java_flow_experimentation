package stockpriceservice;

import java.util.Set;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import lombok.Setter;
import newsservice.Category;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

public class Consumer implements Flow.Subscriber<StockData> {
    private static final Logger logger = LogManager.getLogger(Consumer.class);

    private Map<Stock, Double> stockMaxValues;

    private StockSubscription stockSubscription;
    private  String name;
    private Set<Stock> stockSet;

    private void initMaxValues() {
        stockMaxValues = new HashMap<>();

        for( Stock stock : stockSet ) {
            stockMaxValues.put( stock, 0.0);
        }
    }

    public Consumer( String name, Set<Stock> stockSet) {
        this.name = name;
        this.stockSet = stockSet;

        // Initialize max stock values for set
        initMaxValues();

        logger.info("Initialized consumer " + name + " with " + stockSet.size() + " stocks");
    }


    /**
     * @param subscription subscription for this consumer
     */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if( subscription instanceof StockSubscription) {
            stockSubscription = (StockSubscription) subscription;
            this.stockSubscription = stockSubscription;
            this.stockSubscription.setStockSet(stockSet);
            this.stockSubscription.request(1);
        }
    }

    /**
     * @param stockData - data about specific stock
     */
    @Override
    public void onNext(StockData stockData) {
        Stock stock = stockData.getStock();
        double price = stockData.getPrice();

        if( price > stockMaxValues.get(stock)) {
            logger.info(name + ": Updating max prices for " + stock + " to " + price );
            stockMaxValues.put(stock, price);
            logger.info(name + ": Updated max prices for " + stock + " is " + stockMaxValues.get(stock));
        }
    }

    /**
     * @param throwable
     */
    @Override
    public void onError(Throwable throwable) {
        logger.error("Error occurred ", throwable);
    }

    /**
     *
     */
    @Override
    public void onComplete() {
        logger.info("Consumer " + name + " completed");
    }

    public String getName() {
        return name;
    }

    public double getMaxValue( Stock stock ) {
        if( !stockMaxValues.containsKey(stock ) ){
            logger.error("Stock " + stock + " not monitored by " + name);
            throw new IllegalArgumentException("Stock " + stock + " not monitored by " + name);
        }

        return stockMaxValues.get(stock);
    }
}
