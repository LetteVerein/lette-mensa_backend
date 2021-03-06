package de.lette.mensa;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import com.esotericsoftware.minlog.Log;;

/**
 * Accepts JSON Objects and store them into a database
 * @author Leon
 *
 */
@WebServlet("/saveMeal")
public class saveMeal extends HttpServlet {

	private static final long serialVersionUID = 1L;

	String sql_InsertNormalMeal = "INSERT INTO speisen(name,art,beachte,kcal,eiweisse,fette,kohlenhydrate,beschreibung,preis,zusatzstoffe) VALUES (?,?,?,?,?,?,?,?,?,?)";
	String sql_InsertDiaetMeal = "INSERT INTO diaetspeisen(name,art,beachte,kcal,eiweisse,fette,kohlenhydrate,beschreibung,preis,zusatzstoffe) VALUES (?,?,?,?,?,?,?,?,?,?)";
	String sql_getLatestNormalID = "SELECT * from speisen ORDER BY id DESC LIMIT 1";
	String sql_getLatestDiaetID = "SELECT * from diaetspeisen ORDER BY id DESC LIMIT 1";
	String sql_InsertNormalDate = "INSERT INTO termine(datum,speise) VALUES(?, ?)";
	String sql_InsertDiaetDate = "INSERT INTO diaetermine(datum,speise) VALUES(?, ?)";
	String sql_getKey = "SELECT * FROM auth WHERE auth_key=?";
	String sql_findExistingDate = "SELECT * FROM termine WHERE datum=?";
	String sql_deleteExistingNormalDate = "DELETE FROM termine WHERE datum=?";
	String sql_deleteExistingDiaetDate = "DELETE FROM diaetermine WHERE datum=?";

	public saveMeal() {
		super();
	}

	/**
	 * Connects to the database and parses the JSON Object to call to save it
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void getData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			ConnectDB connection = new ConnectDB(); // Creates a instance of a
													// connection
			response.setHeader("Access-Control-Allow-Origin", "*");
			ServletContext context = getServletContext();
			String fullPath = context.getRealPath("/WEB-INF/db.cfg");
			connection.init(fullPath);
			connection.connectDB();

			String POSTData = ""; // Will save the JSON data from the POST
			StringBuffer tempReader = new StringBuffer();
			String line = null;

			// Reads the JSON Data from the request
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					tempReader.append(line);
			} catch (Exception e) {
			}
			try {
				POSTData = tempReader.toString();
			} catch (Exception e) {
				throw new IOException("Error parsing JSON request string");
			}

			if (connection.getDbConnection() != null) {
				JSONObject wholeJSON = new JSONObject(POSTData.trim());
				JSONObject normalCanteen = (JSONObject) wholeJSON.get("Mensa0");
				JSONObject diaetCanteen = (JSONObject) wholeJSON.get("Mensa1");

				PreparedStatement ps;
				ps = connection.getDbConnection().prepareStatement(sql_getKey);
				String authKey = wholeJSON.getString("auth_pw");
				Log.info("- " + request.getRemoteAddr() + " sent request with the password " + authKey);

				ps.setString(1, authKey);
				ResultSet rs = ps.executeQuery();

				int rsCount = 0;
				while (rs.next()) {
					rsCount++;
				}
				if (rsCount < 1) {
					response.sendError(401);
					Log.error("User rejected");
					return;
				}

				ps.close();
				ps = null;

				Iterator<?> keys = normalCanteen.keys();
				String jsonDate = (String) keys.next();
				String[] types = { "Vorspeise", "Vollkost", "Vegetarisch", "Beilagen", "Dessert" };
				JSONObject datesJSON = null;
				int count = 0;

				for (String index : types) {

					if (normalCanteen.get(jsonDate) instanceof JSONObject) {
						datesJSON = (JSONObject) normalCanteen.get(jsonDate);
						if (count == 0) {
							delteExistingDate(jsonDate, false, connection);
						}
						count++;
						try {
							JSONObject appetizerJSON = (JSONObject) datesJSON.get(index);
							saveMealToDB((String) appetizerJSON.get("name"), index,
									(String) appetizerJSON.get("beachte"), (String) appetizerJSON.get("kcal"),
									(String) appetizerJSON.get("eiweisse"), (String) appetizerJSON.get("fette"),
									(String) appetizerJSON.get("kolenhydrate"),
									(String) appetizerJSON.get("beschreibung"), (String) appetizerJSON.get("preis"),
									(String) appetizerJSON.get("zusatzstoffe"), false, jsonDate, connection);
						} catch (JSONException e) {
						}
					}

				}

				keys = null;
				count = 0;
				keys = diaetCanteen.keys();
				jsonDate = (String) keys.next();
				String[] diaetTypes = { "Vorspeise", "Leichte-Vollkost", "Dessert", "Gem�seteller", "Dessert" };

				for (String index : diaetTypes) {

					if (diaetCanteen.get(jsonDate) instanceof JSONObject) {
						datesJSON = (JSONObject) diaetCanteen.get(jsonDate);
						if (count == 0) {
							delteExistingDate(jsonDate, true, connection);
						}
						count++;

						JSONObject mealJSON = (JSONObject) datesJSON.get(index);
						saveMealToDB((String) mealJSON.get("name"), index, (String) mealJSON.get("beachte"),
								(String) mealJSON.get("kcal"), (String) mealJSON.get("eiweisse"),
								(String) mealJSON.get("fette"), (String) mealJSON.get("kolenhydrate"),
								(String) mealJSON.get("beschreibung"), (String) mealJSON.get("preis"),
								(String) mealJSON.get("zusatzstoffe"), true, jsonDate, connection);
					}

				}

				try {
					connection.dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes all data of a day
	 * @param date Date to delete
	 * @param diaet Decide between diaet and the "normal" canteen
	 * @param connection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void delteExistingDate(String date, Boolean diaet, ConnectDB connection)
			throws ClassNotFoundException, SQLException {
		if (connection.getDbConnection() != null) {
			PreparedStatement ps;
			ps = connection.getDbConnection().prepareStatement(sql_findExistingDate);
			ps.setString(1, date);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (diaet == false) {
					ps = connection.getDbConnection().prepareStatement(sql_deleteExistingNormalDate);

				} else {
					ps = connection.getDbConnection().prepareStatement(sql_deleteExistingDiaetDate);
				}
				ps.setString(1, date);
				ps.executeUpdate();
				break;
			}
			ps.close();
			rs.close();
			rs = null;
			ps = null;
		}

	}

	/*
	 * Saves a meal into the meal table in the database
	 * @param name
	 * @param type
	 * @param notice
	 * @param kcal
	 * @param protein
	 * @param fat
	 * @param carbohydrates
	 * @param description
	 * @param price
	 * @param additives
	 * @param diaet
	 * @param date
	 * @param connection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void saveMealToDB(String name, String type, String notice, String kcal, String protein, String fat,
			String carbohydrates, String description, String price, String additives, Boolean diaet, String date,
			ConnectDB connection) throws ClassNotFoundException, SQLException {

		if (connection.getDbConnection() != null) {
			PreparedStatement ps;
			if (diaet == false) {
				ps = connection.getDbConnection().prepareStatement(sql_InsertNormalMeal);
			} else {
				ps = connection.getDbConnection().prepareStatement(sql_InsertDiaetMeal);
			}
			ps.setString(1, name);
			ps.setString(2, type);
			ps.setString(3, notice);
			ps.setString(4, kcal);
			ps.setString(5, protein);
			ps.setString(6, fat);
			ps.setString(7, carbohydrates);
			ps.setString(8, description);
			ps.setString(9, price);
			ps.setString(10, additives);
			ps.executeUpdate();
			ps.close();
			ps = null;

			if (diaet == false) {
				ps = connection.getDbConnection().prepareStatement(sql_getLatestNormalID);
			} else {
				ps = connection.getDbConnection().prepareStatement(sql_getLatestDiaetID);
			}

			ResultSet rs = ps.executeQuery();
			int foodId = 0;
			while (rs.next()) {
				foodId = rs.getInt(1);
			}
			ps.close();
			ps = null;
			rs = null;
			if (diaet == false) {
				ps = connection.getDbConnection().prepareStatement(sql_InsertNormalDate);
			} else {
				ps = connection.getDbConnection().prepareStatement(sql_InsertDiaetDate);
			}

			ps.setString(1, date);
			ps.setInt(2, foodId);
			ps.executeUpdate();
			ps.close();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		getData(request, response);

	}
}
