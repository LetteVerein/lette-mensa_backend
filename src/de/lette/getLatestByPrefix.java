package de.lette;

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
 * Looks up names of meals by a given prefix
 * 
 * @author Leon
 *
 */
@WebServlet("/getLatestByPrefix")
public class getLatestByPrefix extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String sql_getByPrefixWithType = "SELECT name FROM `speisen` WHERE name LIKE ? AND ART=? ORDER BY name DESC LIMIT 15";
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

			if (request.getParameter("prefix") == null || request.getParameter("prefix") == "") {
				response.getWriter().write("Invalid input");
			} else {

				if (request.getParameter("prefix").length() > 0) {
					String prefix = request.getParameter("prefix");
					String type = request.getParameter("type");
					JSONObject JSONmealNames = getLast(prefix, type, connection.getDbConnection());
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
	 * @param prefix
	 *            Prefix to look for
	 * @param type
	 *            Meal type (main course,dessert, etc.)
	 * @param dbConnection
	 * @return a JSON Object of results
	 */
	private JSONObject getLast(String prefix, String type, Connection dbConnection) {
		JSONObject foodObject = null;
		try {
			PreparedStatement ps = null;
			ps = dbConnection.prepareStatement(sql_getByPrefixWithType);
			ps.setString(1, "%" + prefix + "%");
			ps.setString(2, type);
			ResultSet rs = ps.executeQuery();

			foodObject = new JSONObject();
			int index = 0;
			while (rs.next()) {
				foodObject.put("item" + index, rs.getString(1));
				index++;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return foodObject;
	}

}
