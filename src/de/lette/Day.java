package de.lette;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException;

@WebServlet("/MensaServer")
public class Day extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final String sql_terminNormal = "SELECT * FROM termine WHERE datum=?";
	final String sql_speiseNormal = "SELECT * FROM speisen WHERE id=?";
	final String sql_terminDiaet = "SELECT * FROM diaetermine WHERE datum=?";
	final String sql_speiseDiaet = "SELECT * FROM diaetspeisen WHERE id=?";

	@SuppressWarnings("unchecked")
	public JSONObject getWeekfromDate(String date, String termin, String speise, HttpServletResponse response)
			throws SQLException, ClassNotFoundException, IOException, MySQLNonTransientConnectionException {
		JSONObject dateObject = new JSONObject();
		JSONObject typeObject = null;

		ArrayList<Integer> results = new ArrayList<Integer>();
		String[] parts = date.split("-");
		String year = parts[0];
		String month = parts[1];
		int day = Integer.parseInt(parts[2]);
		String formated = "";
		if (day < 10) {
			formated = String.format("%02d", day);
		} else {
			formated = Integer.toString(day);
		}
		ConnectDB connection = new ConnectDB();
		ServletContext context = getServletContext();
		String fullPath = context.getRealPath("/WEB-INF/db.cfg");
		
		connection.init(fullPath);
		connection.connectDB();

		if (connection.getDbConnection() != null) {
			PreparedStatement ps = null;
			ps = connection.getDbConnection().prepareStatement(termin);
			ps.setString(1, year + "-" + month + "-" + formated);
			ResultSet rs = ps.executeQuery();
			results.clear();
			// Füge Speisenummer ins Array
			while (rs.next()) {
				results.add(Integer.parseInt(rs.getString(3)));
			}
			typeObject = new JSONObject();
			// Alle Seisen(-arten) pro Datum
			for (int i1 = 0; i1 < results.size(); i1++) {
				PreparedStatement pss = connection.dbConnection.prepareStatement(speise);
				// Parameter Speise ID für "Where Klausel"
				pss.setInt(1, results.get(i1));
				ResultSet rss = pss.executeQuery();
				// Alle Speiseeigenschaften pro Speise
				while (rss.next()) {
					JSONObject foodObject = new JSONObject();
					if (rss.getString(2) == "") {
						foodObject.put("name", "Kein Name vorhanden");
					} else {
						foodObject.put("name", rss.getString(2));
					}
					if (rss.getString(4) == "") {
						foodObject.put("beachte", "Nichts zu beachten");
					} else {
						foodObject.put("beachte", rss.getString(4));
					}
					foodObject.put("kcal", rss.getString(5));
					foodObject.put("eiweisse", rss.getString(6));
					foodObject.put("fette", rss.getString(7));
					foodObject.put("kolenhydrate", rss.getString(8));
					if (rss.getString(9) == "") {
						foodObject.put("beschreibung", "Keine Beschreibung vorhanden");
					} else {
						foodObject.put("beschreibung", rss.getString(9));
					}
					foodObject.put("preis", rss.getString(10));
					foodObject.put("zusatzstoffe", rss.getString(11));

					String art = rss.getString(3);
					typeObject.put(art, foodObject);
				}
				ps.close();
			}
			dateObject.put(year + "-" + month + "-" + formated, typeObject);
			day++;

			
		}
		return dateObject;

	}

	@SuppressWarnings("unchecked")
	public void getData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ClassNotFoundException, SQLException {
		ConnectDB connection = new ConnectDB();
		ServletContext context = getServletContext();
		String fullPath = context.getRealPath("/WEB-INF/db.cfg");
		
		connection.init(fullPath);
		connection.connectDB();
		
		if (connection.getDbConnection() != null) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.setHeader("Access-Control-Allow-Origin", "*");

			if (request.getParameter("week") == null || request.getParameter("week") == "") {
				response.getWriter().write("Invalid input");
			} else {
				JSONObject masterObj = new JSONObject();
				try {
					// Parameter "week" = StartDatum
					JSONObject usualCanteen = getWeekfromDate(request.getParameter("week"), sql_terminNormal,
							sql_speiseNormal, response);
					JSONObject diatCanteen = getWeekfromDate(request.getParameter("week"), sql_terminDiaet,
							sql_speiseDiaet, response);
					masterObj.put("Mensa0", usualCanteen);
					masterObj.put("Mensa1", diatCanteen);
				} catch (ClassNotFoundException | SQLException e) {
				}
				response.getWriter().write(masterObj.toString());
			}
			// Close DB
			try {
				connection.getDbConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			getData(request, response);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

}