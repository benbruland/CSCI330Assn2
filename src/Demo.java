import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Collections;

class Demo {

	static Connection conn = null;

	public static void main(String[] args) throws Exception {
		// Get connection properties
		String paramsFile = "ConnectionParameters.txt";
		if (args.length >= 1) {
			paramsFile = args[0];
		}
		Properties connectprops = new Properties();
		connectprops.load(new FileInputStream(paramsFile));

		try {
			// Get connection
			Class.forName("com.mysql.jdbc.Driver");
			String dburl = connectprops.getProperty("dburl");
			String username = connectprops.getProperty("user");
			conn = DriverManager.getConnection(dburl, connectprops);
			System.out.printf("Database connection %s %s established.%n", dburl, username);

			showCompanies();

			// Enter Ticker and TransDate, Fetch data for that ticker and date
			Scanner in = new Scanner(System.in);
			while (true) {
				HashMap<String, Date> currentDataSet = new HashMap<>();
				LinkedList<String> dateIds = new LinkedList<>();
				System.out.print("Enter ticker and date (YYYY.MM.DD): ");
				String line = in.nextLine();
				int lineLength = line.trim().length();
				String[] data = line.trim().split("\\s+");
				String ticker;
				if (lineLength == 0) { // You can't use data.length for this.
					break;
				} else {
					ticker = data[0];
				}
				if (data.length == 2) {
					showTickerDay(data[0], data[1]);
				} else if (data.length == 3) {
					ticker = data[0];
					String initialDay = data[1];
					String endDay = data[2];
					PreparedStatement dateRangeRequest = conn.prepareStatement(
							"select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice, Volume, AdjustedClose"
									+ "  from PriceVolume " + "  where Ticker = ? " + " and TransDate between ? and ? "
									+ "order by TransDate DESC");
					dateRangeRequest.setString(1, ticker);
					dateRangeRequest.setString(2, initialDay);
					dateRangeRequest.setString(3, endDay);
					ResultSet dateRange = dateRangeRequest.executeQuery();
					while (dateRange.next()) {
						double opening = Double.parseDouble(dateRange.getString(2));
						double high = Double.parseDouble(dateRange.getString(3));
						double low = Double.parseDouble(dateRange.getString(4));
						double closing = Double.parseDouble(dateRange.getString(5));
						int volume = Integer.parseInt(dateRange.getString(6).trim());
						double adjClose = Double.parseDouble(dateRange.getString(7));
						String dateString = dateRange.getString(1);
						if (!currentDataSet.containsKey(dateString)) {
							currentDataSet.put(dateString,
									new Date(high, low, opening, closing, adjClose, volume, dateString));
							dateIds.add(dateString);
						}
					}
					dateRange.close();
				} else if (data.length == 1) {
					showCompanyName(data[0]);
					ticker = data[0];
					PreparedStatement dataRequest = conn.prepareStatement(
							"select *" + "  from PriceVolume " + "  where Ticker = ?" + "order by TransDate DESC");
					dataRequest.setString(1, ticker);
					ResultSet dateRange = dataRequest.executeQuery();
					while (dateRange.next()) {
						double opening = Double.parseDouble(dateRange.getString(3));
						double high = Double.parseDouble(dateRange.getString(4));
						double low = Double.parseDouble(dateRange.getString(5));
						double closing = Double.parseDouble(dateRange.getString(6));
						int volume = Integer.parseInt(dateRange.getString(7).trim());
						double adjClose = Double.parseDouble(dateRange.getString(8));
						String dateString = dateRange.getString(2);
						// System.out.printf("%s\n", dateRange.getString(2));
						currentDataSet.put(dateString,
								new Date(high, low, opening, closing, adjClose, volume, dateString));
						dateIds.add(dateString);
					}
					dataRequest.close();
				}
				String currentDate = dateIds.pollFirst(); // Take the first one off
				int totalSplits = 0;
				for (String previousDate : dateIds) {
					Date currentDateData = currentDataSet.get(currentDate);
					Date previousDateData = currentDataSet.get(previousDate);
					double prevClose = previousDateData.getClosingPrice();
					double currentOpen = currentDateData.getOpeningPrice();
					double closeToOpen = prevClose / currentOpen;
					if (Math.abs(closeToOpen - 3.0) < 0.3) {
						if (currentDateData.getClosingPrice() > prevClose) {
							System.out.printf("Stock Split on: %s 3:1\n", currentDateData.getDateString());
						} else {
							System.out.printf("Stock Split on: %s 3:1\n", previousDateData.getDateString());
						}
						totalSplits++;
					}
					if (Math.abs(closeToOpen - 2.0) < 0.2) {
						if (currentDateData.getClosingPrice() > prevClose) {
							System.out.printf("Stock Split on: %s 2:1\n", currentDateData.getDateString());
						} else {
							System.out.printf("Stock Split on: %s 2:1\n", previousDateData.getDateString());
						}
						totalSplits++;
					}
					if (Math.abs(closeToOpen - 1.5) < 0.15) {
						if (currentDateData.getClosingPrice() > prevClose) {
							System.out.printf("Stock Split on: %s 3:2\n", currentDateData.getDateString());
						} else {
							System.out.printf("Stock Split on: %s 3:2\n", previousDateData.getDateString());
						}
						totalSplits++;
					}
					currentDate = previousDate;
				}
				System.out.printf("Total Splits in %d trading days: %d\n", currentDataSet.size(), totalSplits);

			}
			conn.close();
		} catch (SQLException ex) {
			System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n", ex.getMessage(), ex.getSQLState(),
					ex.getErrorCode());
		}
	}

	static void showCompanies() throws SQLException {
		// Create and execute a query
		Statement stmt = conn.createStatement();
		ResultSet results = stmt.executeQuery("select Ticker, Name from Company");

		// Show results
		while (results.next()) {
			System.out.printf("%5s %s%n", results.getString("Ticker"), results.getString("Name"));
		}
		stmt.close();
	}

	static void showCompanyName(String ticker) throws SQLException {
		PreparedStatement nameStmt = conn.prepareStatement("select name from company where ticker = ?");
		nameStmt.setString(1, ticker);
		ResultSet name = nameStmt.executeQuery();
		if (name.next()) {
			System.out.printf("%s\n", name.getString(1));
		} else {
			System.out.printf("Stock is not in database.\n");
		}
		nameStmt.close();
	}

	static void showTickerDay(String ticker, String date) throws SQLException {
		// Prepare query
		PreparedStatement pstmt = conn.prepareStatement("select OpenPrice, HighPrice, LowPrice, ClosePrice "
				+ "  from PriceVolume " + "  where Ticker = ? and TransDate = ?");

		// Fill in the blanks
		pstmt.setString(1, ticker);
		pstmt.setString(2, date);
		ResultSet rs = pstmt.executeQuery();

		// Did we get anything? If so, output data.
		if (rs.next()) {
			System.out.printf("Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f%n", Double.parseDouble(rs.getString(1)),
					Double.parseDouble(rs.getString(2)), Double.parseDouble(rs.getString(3)),
					Double.parseDouble(rs.getString(4)));
		} else {
			System.out.printf("Ticker %s, Date %s not found.%n", ticker, date);
		}
		pstmt.close();
	}

	static void printTickerDateRange(String ticker, String initialDay, String endDay) throws SQLException {
		PreparedStatement dateRangeRequest = conn.prepareStatement("select OpenPrice, HighPrice, LowPrice, ClosePrice "
				+ "  from PriceVolume " + "  where Ticker = ? and TransDate between ? and ?");
		dateRangeRequest.setString(1, ticker);
		dateRangeRequest.setString(2, initialDay);
		dateRangeRequest.setString(3, endDay);
		ResultSet dateRange = dateRangeRequest.executeQuery();
		while (dateRange.next()) {
			System.out.printf("Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f%n",
					Double.parseDouble(dateRange.getString(1)), Double.parseDouble(dateRange.getString(2)),
					Double.parseDouble(dateRange.getString(3)), Double.parseDouble(dateRange.getString(4)));
		}
		dateRangeRequest.close();
	}

	static void printTickerData(String ticker) throws SQLException {
		PreparedStatement tickerDataReq = conn.prepareStatement("select *" + " from PriceVolume " + "where Ticker = ?");
		tickerDataReq.setString(1, ticker);
		ResultSet tickerData = tickerDataReq.executeQuery();
		while (tickerData.next()) {
			System.out.printf(
					"%s, Date: %s Open: %.2f, High: %.2f Low: %.2f Close: %.2f Share Volume: %d AjdClose: %.2f", ticker,
					tickerData.getString(2), Double.parseDouble(tickerData.getString(3)),
					Double.parseDouble(tickerData.getString(4)), Double.parseDouble(tickerData.getString(5)),
					Double.parseDouble(tickerData.getString(6)),
					Integer.parseInt(tickerData.getString(7).replaceAll("\\s+", "")),
					Double.parseDouble(tickerData.getString(8)));
			System.out.println();
		}
		tickerDataReq.close();
	}

	static void getStockDataInRange(String ticker, String start, String end) throws SQLException {
		PreparedStatement dateRangeRequest = conn.prepareStatement("select OpenPrice, HighPrice, LowPrice, ClosePrice "
				+ "  from PriceVolume " + "  where Ticker = ? and TransDate between ? and ?");
		dateRangeRequest.setString(1, ticker);
		dateRangeRequest.setString(2, start);
		dateRangeRequest.setString(3, end);
		ResultSet dateRange = dateRangeRequest.executeQuery();
		dateRange.close();
	}

}
