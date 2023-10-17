package stockpriceservice;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StockSubscription implements Flow.Subscription {

    private static final Logger logger = LogManager.getLogger(StockSubscription.class);

    @Getter
    private boolean  cancelled;
    @Getter private AtomicLong requested = new AtomicLong(0);
    @Setter
    private Set<Stock> stockSet;

    @Override
    public void request(long value) {
        logger.info("Requesting " + value + " items");
        requested.addAndGet(value);
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    public void decreaseRequested() {
        logger.info("Decrementing value");
        requested.decrementAndGet();
    }

    public boolean hasStock( Stock stock ) {
        return stockSet.contains(stock);
    }
}
