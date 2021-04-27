package com.muhlenberg.bot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import com.muhlenberg.models.Portfolio;
import com.muhlenberg.models.Stock;
import com.symphony.bdk.gen.api.model.V4User;

import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

/**
 *
 * @author sqlitetutorial.net
 */
public class Database {
    /**
     * Connect to a sample database
     */
    private static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:src/main/resources/db/bergbot.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewPortTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS portfoliolist (\n" + "	userid integer NOT NULL,\n"
                + "	portfolio blob NOT NULL);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewClientPortTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS clientportfoliolist (\n" + " userid integer NOT NULL,\n"
                + "	portid integer NOT NULL);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addPortfolio(V4User userID, Portfolio port) {
        // Create table (won't do anything if it already exists)
        createNewPortTable();
        createNewClientPortTable();

        // Make sure portfolio doesn't have existing name
        Portfolio portfol = getPortfolio(userID, port.getName(), false);
        int i = 0;

        // If portfolio of same name exists, append i to the name of the new one
        while (portfol != null) {
            i++;
            portfol = getPortfolio(userID, port.getName() + i, false);
        }
        if (i > 0) {
            port.setName(port.getName() + i);
        }

        // SQL statement for adding a portfolio
        String sql = "INSERT INTO portfoliolist (userid, portfolio) VALUES(?,?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userID.getUserId());
            pstmt.setBytes(2, makeByte(port));
            pstmt.executeUpdate();

            sql = "INSERT INTO clientportfoliolist (userid, portid) VALUES(?,?)";
            PreparedStatement pstmt2 = conn.prepareStatement(sql);
            Integer rowid = getPortfolioID(userID, port.getName());
            // Add clients to clientportfoliolist
            for (Map.Entry<Long, Double> entry : port.getClientBreakdown().entrySet()) {
                pstmt2.setLong(1, entry.getKey());
                pstmt2.setInt(2, rowid);
                pstmt2.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void placeOrder(V4User userID, Portfolio newPort, String ticker, Double shares, Double price,
            Double orderAmount) {

        // Make sure portfolio doesn't have existing name
        Portfolio port = getPortfolio(userID, newPort.getName(), false);

        yahoofinance.Stock stock;
        try {
            stock = YahooFinance.get(ticker);
            StockQuote quote = stock.getQuote();

            // Figure out if stock is largecap
            BigDecimal marketCap = stock.getStats().getMarketCap();
            BigDecimal threshold = new BigDecimal(10000000000l);
            boolean isLargeCap = false;
            if (marketCap.compareTo(threshold) >= 0) {
                isLargeCap = true;
            }

            Stock newStock = new Stock(ticker, stock.getName(), price, price, quote.getChangeInPercent().doubleValue(),
                    isLargeCap);
            newPort.addAsset(newStock, shares);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // SQL statement for adding a portfolio
        String sql = "UPDATE portfoliolist SET portfolio = ? WHERE userid = ? AND portfolio = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, makeByte(newPort));
            pstmt.setLong(2, userID.getUserId());
            pstmt.setBytes(3, makeByte(port));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<Portfolio> getPortfolioList(V4User user) {
        String sql = "SELECT portfolio FROM portfoliolist WHERE userid = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, user.getUserId());
            ResultSet rs = pstmt.executeQuery();

            // Convert ResultSet to an ArrayList
            ArrayList<Portfolio> portList = new ArrayList<Portfolio>();
            while (rs.next()) {
                // System.out.println(readBytes(rs.getBytes("portfolio")).getName());
                portList.add(readBytes(rs.getBytes("portfolio")));
            }
            return portList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static ArrayList<Portfolio> getClientPortfolioList(V4User user) {
        String sql = "SELECT portid FROM clientportfoliolist WHERE userid = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, user.getUserId());
            ResultSet rs = pstmt.executeQuery();

            // Convert ResultSet to an ArrayList
            ArrayList<Integer> portIdList = new ArrayList<Integer>();
            while (rs.next()) {
                // System.out.println(readBytes(rs.getBytes("portfolio")).getName());
                portIdList.add(rs.getInt("portid"));
            }
            // Make sure list isn't empty
            if (portIdList.size() == 0) {
                return null;
            }

            // Retrieve portfolios with rowids
            ArrayList<Portfolio> portList = new ArrayList<Portfolio>();
            for (int rowids : portIdList) {
                sql = "SELECT portfolio FROM portfoliolist WHERE rowid = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(sql);
                pstmt2.setInt(1, rowids);
                rs = pstmt2.executeQuery();
                while (rs.next()) {
                    portList.add(readBytes(rs.getBytes("portfolio")));
                }
            }
            return portList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static Portfolio getPortfolio(V4User user, String name, boolean isClient) {
        ArrayList<Portfolio> ports;
        if(isClient){
            ports = getClientPortfolioList(user);
        }
        else{
            ports = getPortfolioList(user);
        }
        int size = ports.size();

        for (int i = 0; i < size; i++) {
            if (ports.get(i).getName().equals(name)) {
                return ports.get(i);
            }
        }
        return null;
    }

    // Retrieves the rowid of a portfolio
    public static int getPortfolioID(V4User user, String name) {
        String sql = "SELECT rowid, portfolio FROM portfoliolist WHERE userid = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, user.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // System.out.println(readBytes(rs.getBytes("portfolio")).getName());
                if (readBytes(rs.getBytes("portfolio")).getName().equals(name)) {
                    return rs.getInt("rowid");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    // Serializes portfolio to bytes
    public static byte[] makeByte(Portfolio port) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(port);
            byte[] portfolioAsBytes = baos.toByteArray();
            // ByteArrayInputStream bais = new ByteArrayInputStream(portfolioAsBytes);
            return portfolioAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Converts bytes to a portfolio object
    public static Portfolio readBytes(byte[] data) {
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            Portfolio port = (Portfolio) ois.readObject();
            return port;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         * Stock s = new Stock("AAPL", "Apple", 125.02, 125.02, 0.0, true); Stock s2 =
         * new Stock("XOM", "Exxon Mobil", 15.21, 15.21, -.15, true); Stock s3 = new
         * Stock("TMUS", "T-Mobile", 156, 156, 1.15, true); HashMap<Long, Double> h =
         * new HashMap<Long, Double>(); V4User user = new V4User(); Long uId =
         * Long.valueOf(349026222357189L); user.setUserId(uId); h.put(user.getUserId(),
         * .215);
         * 
         * HashMap<Stock, Double> h2 = new HashMap<Stock, Double>(); h2.put(s, 120.00);
         * h2.put(s2, 127d); h2.put(s3, 17d);
         * 
         * Portfolio p = new Portfolio("PortTester", 1000, 1.00, h, h2, "^GSPC");
         * Database.createNewPortTable(); Database.addPortfolio(user, p);
         * ArrayList<Portfolio> portlist = Database.getPortfolioList(user);
         * System.out.println(portlist.get(0).getName());
         */
        V4User user = new V4User();
        Long uId = Long.valueOf(349026222357189L);
        user.setUserId(uId);
        System.out.println(Database.getClientPortfolioList(user));
    }
}