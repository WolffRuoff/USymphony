package com.muhlenberg.bot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.muhlenberg.models.Portfolio;
import com.muhlenberg.models.Stock;
import com.symphony.bdk.gen.api.model.V4User;


/**
 *
 * @author sqlitetutorial.net
 */
public class Database {
    /**
     * Connect to a sample database
     */
    private Connection connect() {
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

    public void createNewTable() {
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

    public void addPortfolio(V4User userID, Portfolio port) {
        // SQL statement for adding a portfolio
        String sql = "INSERT INTO portfoliolist (userid, portfolio) VALUES(?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userID.getUserId());
            pstmt.setBytes(2, makeByte(port));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<Portfolio> getPortfolioList(V4User user) {
        String sql = "SELECT portfolio FROM portfoliolist WHERE userid = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public byte[] makeByte(Portfolio port) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(port);
            byte[] portfolioAsBytes = baos.toByteArray();
            //ByteArrayInputStream bais = new ByteArrayInputStream(portfolioAsBytes);
            return portfolioAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Portfolio readBytes(byte[] data) {
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
        Stock s = new Stock("AAPL", "Apple", 125.02, 0.0, true);
        Stock s2 = new Stock("XOM", "Exxon Mobil", 15.21, -.15, true);
        Stock s3 = new Stock("TMUS", "T-Mobile", 156, 1.15, true);
        HashMap<Long, Double> h = new HashMap<Long, Double>();
        V4User user = new V4User();
        Long uId = Long.valueOf(349026222357189L);
        user.setUserId(uId);
        h.put(user.getUserId(), .215);

        HashMap<Stock, Double> h2 = new HashMap<Stock, Double>();
        h2.put(s, 120.00);
        h2.put(s2, 127d);
        h2.put(s3, 17d);

        Portfolio p = new Portfolio("PortTester", 1000, 1.00, h, h2);
        Database db = new Database();
        db.createNewTable();
        db.addPortfolio(user, p);
        ArrayList<Portfolio> portlist = db.getPortfolioList(user);
        System.out.println(portlist.get(0).getName());
    }
}