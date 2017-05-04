package de.lette.mensa;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;

/**
 * Looks up names of meals by a given prefix/query
 * 
 * @author Leon, Leo
 *
 */
@WebServlet("/getLatestByQuery")
public class getLatestByQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String sql_getByQueryWithType = "SELECT name FROM `speisen` WHERE name LIKE ? AND ART=? ORDER BY name DESC LIMIT 15";
	ConnectDB connection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() {
		initConnection();
	}

	/**
	 * Invokes a database connection
	 */
	public void initConnection() {
		connection = new ConnectDB();
		ServletContext context = getServletContext();
		String fullPath = context.getRealPath("/WEB-INF/db.cfg");

		connection.init(fullPath);
		try {
			connection.connectDB();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			if (!connection.getDbConnection().isValid(5)) {
				try {
					connection.getDbConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				initConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (connection.getDbConnection() != null) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");

			if (request.getParameter("query") == null || request.getParameter("query") == "") {
				response.getWriter().write("Invalid input");
			} else {

				if (request.getParameter("query").length() > 0) {
					String query = request.getParameter("query");
					String type = request.getParameter("type");
					JSONArray JSONmealNames = getLast(query, type, connection.getDbConnection());
					response.getWriter().write(JSONmealNames.toString());
				} else {
					response.getWriter().write("Invalid input");
					response.sendError(400);
				}

			}
		}
	}

	/**
	 * Checks if the given string is numeric
	 * 
	 * @param string
	 *            String to check
	 * @return True or False
	 */
	public boolean isNumeric(String string) {
		return string.matches("[-+]?\\d*\\.?\\d+");
	}

	/**
	 * @param query
	 *            Query to look for
	 * @param type
	 *            Meal type (main course,dessert, etc.)
	 * @param dbConnection
	 * @return a JSON Object of results
	 */
	private JSONArray getLast(String query, String type, Connection dbConnection) {
		JSONArray foodArray = null;
		try {
			PreparedStatement ps = null;
			ps = dbConnection.prepareStatement(sql_getByQueryWithType);
			ps.setString(1, "%" + query + "%");
			ps.setString(2, type);
			ResultSet rs = ps.executeQuery();

			foodArray = new JSONArray();
			
			int index = 0;
			while (rs.next()) {
				JSONObject foodItem = new JSONObject();
				foodItem.put("meal", rs.getString(1));
				foodItem.put("id", index);
				foodArray.put(foodItem);
				index++;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return foodArray;
	}

}
