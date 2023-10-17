package stockpriceservice;

import lombok.Value;

@Value
public class StockData {
    Stock stock;
    double price;
}
