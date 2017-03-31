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


@WebServlet("/getLastWeeks")
public class getLastWeeks extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String sql_getLastWeeks = "SELECT name FROM `speisen` WHERE name !=\"\" ORDER BY name DESC LIMIT ?";
	ConnectDB connection;
	
	@Override	
	public void init()
	{
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

		if (connection.getDbConnection() != null) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");

			if (request.getParameter("weeks") == null || request.getParameter("weeks") == "") {
				response.getWriter().write("Invalid input");
			} else {
				
				if(isNumeric(request.getParameter("weeks")) && request.getParameter("weeks").length() < 5)
				{
					int weeks = Integer.parseInt(request.getParameter("weeks"));
					JSONObject JSONmealNames = getLast(weeks, connection.getDbConnection());
					response.getWriter().write(JSONmealNames.toString());
				}
				else 
				{
					response.getWriter().write("Invalid input");
					response.sendError(400);
				}
				
			}
			// Close DB
			try {
				connection.getDbConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  

	private JSONObject getLast(int amountOfWeeks, Connection dbConnection) {
		JSONObject foodObject = null;
		try {
			PreparedStatement ps = null;
			ps = dbConnection.prepareStatement(sql_getLastWeeks);
			int weeks = amountOfWeeks * 5;
			ps.setInt(1, weeks);
			ResultSet rs = ps.executeQuery();

			foodObject = new JSONObject();
			int index = 0;
			while (rs.next()) {
				foodObject.put("item"+index,rs.getString(1));
				index++;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return foodObject;
	}

}
