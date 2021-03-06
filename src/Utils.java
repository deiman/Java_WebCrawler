import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class Utils {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	private static final String DB_URL = "jdbc:mysql://localhost/crawledInfo?useSSL=false";
	private static final String USER = "agodinez";
	private static final String PASS = "SecretPassword";
	static int numConnections = 0;
	@SuppressWarnings("unused")
	private static Document doc;

	// check if connection to root url is good
	public static boolean connectToUrl ( String url ) {
		boolean success;
		if ( url != null ) {
			try {
				doc = Jsoup.connect(url).get();
				success = true;
			} catch ( Exception e ) {
				success = false;
			}
		 } else {
			success = false;
		}
		return success;
	}
	
	// format string url
	public static String trim(String s, int width) {
		if (s.length() > width)
			return s.substring(0, width-1) + ".";
		else
			return s;
	}
	
	// custom print method
	public static void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}
	
	// connect to mysql
	public static Connection connectDatabase () {
		Connection conn = null;
	    try {
	    	Class.forName( JDBC_DRIVER );
	        try {
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    return conn;
	}
	
	// insert into database
	public static boolean writeToDatabase ( Connection conn, String url, String elem, String layer, String tag ) {
	    boolean success = false;
		try {
			Statement stmt = conn.createStatement();
			switch(tag){
				case "media":
					stmt.executeUpdate("INSERT INTO scraped (url, media, layer) " 
				    + "VALUES ('"+ url +"','"+ elem +"','"+ layer +"')");
					break;
				case "imports":
					stmt.executeUpdate("INSERT INTO scraped (url, imports, layer) " 
				    + "VALUES ('"+ url +"','"+ elem +"','"+ layer +"')");					
					break;
				case "links":
					stmt.executeUpdate("INSERT INTO scraped (url, links, layer) " 
				    + "VALUES ('"+ url +"','"+ elem +"','"+ layer +"')");	
					break;
			}		
		    success = true;
		    
		} catch ( MysqlDataTruncation e) {
			System.out.println("skipping a super long url");

		} catch ( MySQLSyntaxErrorException e) {
			System.out.println("skipping malformed link");
		}catch (SQLException e) {
			e.printStackTrace();	
		} 
		return success;
	}	
	
	// close database connection
	public static boolean closeDatabaseConnection ( Connection conn) {
	    boolean success = false;
		try {
			conn.close();
		    success = true;
		} catch (SQLException e) {
			e.printStackTrace();	
		}
		return success;
	}
	
	// search database returns ResultSet
	public static ResultSet search ( Connection conn, String url, String tag) {
		ResultSet rs = null;
		try {
			String linksSQL = "SELECT links FROM scraped WHERE links IS NOT NULL AND links <> '' AND url = '"+ url +"'";
			String mediaSQL = "SELECT media FROM scraped WHERE media IS NOT NULL AND media <> '' AND url = '"+ url +"'";
			String importsSQL = "SELECT imports FROM scraped WHERE imports IS NOT NULL AND imports <> '' AND url = '"+ url +"'";
			Statement stmt = conn.createStatement();
			switch(tag){
			case "media":
			    rs = stmt.executeQuery(mediaSQL);
				break;
			case "imports":
			    rs = stmt.executeQuery(importsSQL);
				break;
			case "links":
			    rs = stmt.executeQuery(linksSQL);
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();	
		} 
		return rs;
	}
	
	// get scraped urls as a set to avoid duplicate handling
	public static Set < String > getScrapedUrls ( Connection conn ) {
		Set < String > scrapedURLS = new HashSet <String>  ();
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT url FROM scraped";
		    ResultSet rs = stmt.executeQuery( sql );
		    while( rs.next() ) {
		        String result = rs.getString(1);
		        if (result != null) {
		            result = result.trim();
		        }
		        scrapedURLS.add(result);
		    }

		} catch ( SQLException e ) {
			e.printStackTrace();	
		}
		return scrapedURLS;
	}
}