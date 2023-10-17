package stockpriceservice;

import lombok.Value;

import java.util.concurrent.Flow;

@Value
public class StockServicesConsumerData {
    Flow.Subscriber<StockData> subscriber;
    StockSubscription subscription;
}
