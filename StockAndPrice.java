
public class StockAndPrice {

  private String stockName;
  private double price;
    
  public StockAndPrice(String stockName2, double price2) {
    this.stockName = stockName2;
    this.price = price2;
  }
  
  
  @Override
  public String toString() {
    return this.stockName+","+this.price+",";
  }
  
  public double getPrice() {
    return price;
  }
  
  public String getStockName() {
    return stockName;
  }

  public void setPrice(double price) {
    this.price = price;
  }
  
  
}
