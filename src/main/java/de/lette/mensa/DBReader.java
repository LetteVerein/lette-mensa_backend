package de.lette.mensa;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBReader {


    public String getRecipename(Connection con, int id)
    {
        String name = "";
        try {
            PreparedStatement pStmt  = con.prepareStatement("select NML_BEZEICHNUNG from NMLVERZEICHNIS where NML_ID = ?;");
            pStmt.setInt(1, id);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next())
                name = rs.getString(1);
            pStmt.close();
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return name;
    }

}
