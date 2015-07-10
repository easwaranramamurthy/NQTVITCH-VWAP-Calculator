import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to calculate VWAP scores for each stock at every trade and write it to
 * a file. Please supply input file name as first command line argument. Final
 * Unix shell command used: java CalculateVWAP <input-ITCH50-file> | sort -s
 * -k1,1 > <output-file>
 * 
 * @author easwa_000
 *
 */
public class CalculateVWAP {

  private String filename;

  /**
   * Constructor that initializes in the input file name.
   * 
   * @param filename2
   *          input file name.
   */
  public CalculateVWAP(String filename2) {
    this.filename = filename2;
  }

  /**
   * function to perform the actual calculation and write them out to standard
   * output.
   * 
   * @param maxLines
   * @throws IOException
   */
  private void calculateVWAP(int maxLines) throws IOException {

    FileInputStream input = new FileInputStream(new File(this.filename));

    // writing to System.out
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

    // hashmap to store currently active orders.
    Map<Long, StockAndPrice> activeOrders = new HashMap<Long, StockAndPrice>();

    // hashmap to store current cumulative volume*price for a particular stock
    Map<String, Double> cumulativeVolumePrice = new HashMap<String, Double>();

    // hashmap to store current cumulative volume for a particular stock
    Map<String, Integer> cumulativeVolume = new HashMap<String, Integer>();

    int writtenLines = 0;

    // loop to read the file
    while (input.read() != -1) {

      // number of bytes to read.
      int length = input.read();

      // reading in the desired number of bytes.
      byte[] currLine = new byte[length];
      input.read(currLine);

      // casing on type of message and acting accordingly.
      switch ((char) currLine[0]) {

      // case A runs into case F so no break inserted.
      case 'A':
      case 'F': {
        // add orders to active orders hashmap
        char buyOrSell = (char) currLine[19];
        if (buyOrSell == 'B') {
          long orderRef = Util.getLong(currLine, 11, 19);
          String stockName = Util.getString(currLine, 24, 32);
          double price = Util.getDouble(currLine, 32, 36);
          StockAndPrice currOrder = new StockAndPrice(stockName, price);
          activeOrders.put(orderRef, currOrder);
        }
        break;
      }

      case 'C': {
        // execute an order with price
        long orderRef = Util.getLong(currLine, 11, 19);
        if (activeOrders.containsKey(orderRef)) {
          StockAndPrice sAndP = activeOrders.get(orderRef);

          // price obtained from message, not from active orders hashmap
          double price = Util.getDouble(currLine, 32, 36);

          String stockName = sAndP.getStockName();
          int volume = Util.getInt(currLine, 19, 23);

          // updating current cumulative quantities
          if (!cumulativeVolumePrice.containsKey(stockName)) {
            cumulativeVolumePrice.put(stockName, 0.0);
            cumulativeVolume.put(stockName, 0);
          }
          cumulativeVolumePrice.put(stockName,
              cumulativeVolumePrice.get(stockName) + price * volume);
          cumulativeVolume.put(stockName, cumulativeVolume.get(stockName)
              + volume);

          double cumVolPrice = cumulativeVolumePrice.get(stockName);
          int cumVol = cumulativeVolume.get(stockName);

          // writing the VWAP scores to standard output along with other
          // details.
          out.write(stockName + "\t" + price + "\t" + volume + "\t" + cumVol
              + "\t" + volume * price + "\t" + cumVolPrice + "\t" + cumVolPrice
              / cumVol + "\n");
          writtenLines++;
          activeOrders.remove(orderRef);
        }
        break;
      }
      case 'E': {
        // execute an order at previously noted price
        long orderRef = Util.getLong(currLine, 11, 19);
        if (activeOrders.containsKey(orderRef)) {
          StockAndPrice sAndP = activeOrders.get(orderRef);
          String stockName = sAndP.getStockName();

          // price obtained from active orders hashmap
          double price = sAndP.getPrice();
          int volume = Util.getInt(currLine, 19, 23);

          // updating current cumulative quantities
          if (!cumulativeVolumePrice.containsKey(stockName)) {
            cumulativeVolumePrice.put(stockName, 0.0);
            cumulativeVolume.put(stockName, 0);
          }
          cumulativeVolumePrice.put(stockName,
              cumulativeVolumePrice.get(stockName) + price * volume);
          cumulativeVolume.put(stockName, cumulativeVolume.get(stockName)
              + volume);

          double cumVolPrice = cumulativeVolumePrice.get(stockName);
          int cumVol = cumulativeVolume.get(stockName);

          // writing the VWAP scores to standard output along with other
          // details.
          out.write(stockName + "\t" + price + "\t" + volume + "\t" + cumVol
              + "\t" + volume * price + "\t" + cumVolPrice + "\t" + cumVolPrice
              / cumVol + "\n");
          writtenLines++;
          activeOrders.remove(orderRef);
        }
        break;
      }
      case 'D': {
        // deleting an order from the list of active orders
        long orderRef = Util.getLong(currLine, 11, 19);
        activeOrders.remove(orderRef);
        break;
      }

      case 'U': {
        // replacing an order with a new order.
        long prevOrderRef = Util.getLong(currLine, 11, 19);
        long currOrderRef = Util.getLong(currLine, 19, 27);
        if (activeOrders.containsKey(prevOrderRef)) {
          StockAndPrice sAndP = activeOrders.get(prevOrderRef);
          double price = Util.getDouble(currLine, 31, 35);

          // updating the price, removing the original order and adding the new
          // order.
          sAndP.setPrice(price);
          activeOrders.remove(prevOrderRef);
          activeOrders.put(currOrderRef, sAndP);
        }

        break;
      }

      case 'P': {
        // non displayable trade message
        char buyOrSell = (char) currLine[19];
        // run only when it is a 'Buy' order
        if (buyOrSell == 'B') {
          String stockName = Util.getString(currLine, 24, 32);
          double price = Util.getDouble(currLine, 32, 36);
          int volume = Util.getInt(currLine, 20, 24);

          // do not update if volume is 0
          if (volume == 0) {
            break;
          }

          // updating cumulative quantities
          if (!cumulativeVolumePrice.containsKey(stockName)) {
            cumulativeVolumePrice.put(stockName, 0.0);
            cumulativeVolume.put(stockName, 0);
          }
          cumulativeVolumePrice.put(stockName,
              cumulativeVolumePrice.get(stockName) + price * volume);
          cumulativeVolume.put(stockName, cumulativeVolume.get(stockName)
              + volume);

          double cumVolPrice = cumulativeVolumePrice.get(stockName);
          int cumVol = cumulativeVolume.get(stockName);

          // writing VWAP along with other quantities to standard output.
          out.write(stockName + "\t" + price + "\t" + volume + "\t" + cumVol
              + "\t" + volume * price + "\t" + cumVolPrice + "\t" + cumVolPrice
              / cumVol + "\n");
          writtenLines++;
        }
        break;
      }
      case 'Q': {
        // non-displayable cross trade message
        String stockName = Util.getString(currLine, 19, 27);
        double price = Util.getDouble(currLine, 27, 31);
        int volume = Util.getInt(currLine, 11, 19);

        // do not update if volume is 0
        if (volume == 0) {
          break;
        }

        // updating cumulative quantities
        if (!cumulativeVolumePrice.containsKey(stockName)) {
          cumulativeVolumePrice.put(stockName, 0.0);
          cumulativeVolume.put(stockName, 0);
        }
        cumulativeVolumePrice.put(stockName,
            cumulativeVolumePrice.get(stockName) + price * volume);
        cumulativeVolume.put(stockName, cumulativeVolume.get(stockName)
            + volume);

        double cumVolPrice = cumulativeVolumePrice.get(stockName);
        int cumVol = cumulativeVolume.get(stockName);

        // writing VWAP scores to standard out along with other quantities.
        out.write(stockName + "\t" + price + "\t" + volume + "\t" + cumVol
            + "\t" + volume * price + "\t" + cumVolPrice + "\t" + cumVolPrice
            / cumVol + "\n");
        writtenLines++;
        break;
      }
      }

      // flushing the output at regular intervals so that the Unix sort can run
      // parallely
      if (writtenLines % maxLines == 0) {
        out.flush();
      }

    }

    out.flush();
    out.close();
    input.close();
  }

  public static void main(String[] args) throws IOException {
    // takes input file name as command line argument.
    String filename = args[0];
    CalculateVWAP calculator = new CalculateVWAP(filename);
    // buffer size of 5000 for printing output to system.out (useful for
    // parallel Unix sort)
    calculator.calculateVWAP(5000);
  }
}
