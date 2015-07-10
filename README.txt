Compile the three source files CalculateVWAP.java, Util.java and StockAndPrice.java using javac in UNIX.

Run the java code using the following UNIX command:

java CalculateVWAP <in_file> | sort -s -k1,1 > <output_file>

Output is written in a tab separated text file with the following format:

<Stock Name>\t<Price>\t<Volume>\t<Cumulative Volume>\t<Volume*Price>\t<Cumulative Volume*Price>\t<Running VWAP>

Output is sorted in alphabetical order of Stock Name.