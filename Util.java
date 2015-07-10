import java.nio.ByteBuffer;

public class Util {

  public static long getLong(byte[] data, int beg, int end) {
    long value = 0;
    for (int i = beg; i < end; i++) {
      value += ((long) data[i] & 0xffL) << (8 * i);
    }
    return value;
  }

  public static int getInt(byte[] currLine, int beg, int end) {
    return ByteBuffer.wrap(currLine, beg, end-beg).getInt();
  }

  public static String getString(byte[] currLine, int beg, int end) {
    StringBuilder builder = new StringBuilder();
    for (int i = beg; i < end; i++) {
      char currChar = (char) currLine[i];
      if (currChar != ' ') {
        builder.append(currChar);
      }
    }
    return builder.toString();
  }

  public static double getDouble(byte[] currLine, int beg, int end) {
    return Util.getInt(currLine, beg, end) / 10000.0;
  }

}
