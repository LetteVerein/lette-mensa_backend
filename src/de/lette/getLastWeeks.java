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

import org.json.JSONArray;
import org.json.simple.JSONObject;

@WebServlet("/getLastWeeks")
public class getLastWeeks extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String sql_getLastWeeks = "SELECT name FROM `speisen` WHERE name !=\"\" ORDER BY name DESC LIMIT ?";

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {// Connect DB
			ConnectDB connection = new ConnectDB();
			ServletContext context = getServletContext();
			String fullPath = context.getRealPath("/WEB-INF/db.cfg");
			
			connection.init(fullPath);
			connection.connectDB();

			if (connection.getDbConnection() != null) {
				response.setCharacterEncoding("UTF-8");
				response.setContentType("application/json");
				response.setHeader("Access-Control-Allow-Origin", "*");

				if (request.getParameter("weeks") == null || request.getParameter("weeks") == "") {
					response.getWriter().write("Invalid input");
				} else {
					int weeks = Integer.parseInt(request.getParameter("weeks"));
					JSONArray JSONmealNames = getLast(weeks, connection.getDbConnection());
					response.getWriter().write(JSONmealNames.toString());
				}
				// Close DB
				try {
					connection.getDbConnection().close();
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

	@SuppressWarnings({ "unchecked", "null" })
	private JSONArray getLast(int amountOfWeeks, Connection dbConnection) {
		JSONArray foodObject = null;
		try {
			PreparedStatement ps = null;
			ps = dbConnection.prepareStatement(sql_getLastWeeks);
			int weeks = amountOfWeeks * 5;
			ps.setInt(1, weeks);
			ResultSet rs = ps.executeQuery();

			foodObject = new JSONArray();
			while (rs.next()) {
				foodObject.put(rs.getString(1));
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return foodObject;
	}

}
