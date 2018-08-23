package de.lette.mensa;

import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Searches the database by a given day and prints the results as a JSON Object
 */
@WebServlet("/api")
public class MensaServer extends HttpServlet {

    private ConnectDB connection;

    private final String sql_terminNormal = "SELECT * FROM termine WHERE datum=?";
    private final String sql_speiseNormal = "SELECT * FROM speisen WHERE id=?";
    private final String sql_terminDiaet = "SELECT * FROM diaetermine WHERE datum=?";
    private final String sql_speiseDiaet = "SELECT * FROM diaetspeisen WHERE id=?";

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() {
        initConnection();
    }

    @Override
    public void destroy() {
        connection.closeDBConnection();
    }

    /**
     * Invokes a database connection
     */
    private void initConnection() {
        connection = new ConnectDB();
        ServletContext context = getServletContext();
        String fullPath = context.getRealPath("/WEB-INF/db.cfg");

        connection.init(fullPath);
        try {
            connection.connectDB();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests data from the database given by the date and returns a JSONObject
     * @param date to search for
     * @param sql_date statement to get mealIDs with the given date
     * @param sql_meal statement to get a meal from the mealID
     * @return JSONObject with the menu data in it
     * @throws SQLException
     */
    private JSONObject getDataByDate(String date, String sql_date, String sql_meal) throws SQLException {
        ArrayList<Integer> mealIDs = new ArrayList<>();

        PreparedStatement getMealByDate = connection.getDbConnection().prepareStatement(sql_date);
        getMealByDate.setString(1, date);

        ResultSet mealIdSet = getMealByDate.executeQuery();
        mealIDs.clear();
        while (mealIdSet.next())
            mealIDs.add(Integer.parseInt(mealIdSet.getString(3)));
        getMealByDate.close();

        JSONObject mealOfType = new JSONObject();
        for (Integer id : mealIDs) {
            PreparedStatement findMealById = connection.getDbConnection().prepareStatement(sql_meal);
            findMealById.setInt(1, id);
            ResultSet mealSet = findMealById.executeQuery();
            while (mealSet.next()) {
                JSONObject mealObject = new JSONObject();
                mealObject.put("name", mealSet.getString(2).isEmpty() ? "Kein Name vorhanden" : mealSet.getString(2));
                mealObject.put("beachte", mealSet.getString(4).isEmpty() ? "Nichts zu beachten" : mealSet.getString(4));
                mealObject.put("kcal", mealSet.getString(5));
                mealObject.put("eiweisse", mealSet.getString(6));
                mealObject.put("fette", mealSet.getString(7));
                mealObject.put("kolenhydrate", mealSet.getString(8));
                mealObject.put("beschreibung", mealSet.getString(9).isEmpty() ? "Keine Beschreibung vorhanden" : mealSet.getString(9));
                mealObject.put("preis", mealSet.getString(10));
                mealObject.put("zusatzstoffe", mealSet.getString(11));

                String mealType = mealSet.getString(3);
                mealOfType.put(mealType, mealObject);
            }
            findMealById.close();
        }

        return new JSONObject().put(date, mealOfType);
    }

    /**
     * validates the given input and print response
     * @param request
     * @param response
     * @throws IOException
     */
    private void validateAndPrintData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (connection.getDbConnection() != null) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            if (request.getParameter("day") == null || request.getParameter("day").isEmpty()) {
                response.getWriter().write("Invalid input");
            } else {
                JSONObject masterObj = new JSONObject();
                try {
                    if (checkValidDate(request.getParameter("day"))) {
                        JSONObject usualCanteen = getDataByDate(request.getParameter("day"), sql_terminNormal,
                                sql_speiseNormal);
                        JSONObject diatCanteen = getDataByDate(request.getParameter("day"), sql_terminDiaet,
                                sql_speiseDiaet);
                        masterObj.put("Mensa0", usualCanteen);
                        masterObj.put("Mensa1", diatCanteen);
                    } else {
                        response.sendError(400);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                response.getWriter().write(masterObj.toString());
                Log.info("- " + request.getRemoteAddr() + " requested " + request.getParameter("day"));
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
                Log.info("Connecting to database");
            if (connection.getDbConnection().isValid(5)) {
            } else {
                connection.getDbConnection().close();
                initConnection();
                System.out.println("Working");
            }
            /*if (connection.getDbConnection().isValid(5)) {
                validateAndPrintData(request, response);
            } else {
                connection.getDbConnection().close();
                initConnection();
                validateAndPrintData(request, response);
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * checks if the given date is in the correct format (YYYY-MM-DD)
     *
     * @param date Date to check
     * @return true if its valid
     */
    private boolean checkValidDate(String date) {
        return Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}", date);
    }

}
