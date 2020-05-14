package de.unidue.inf.is.ProjectFunder;

import de.unidue.inf.is.domain.Project;
import de.unidue.inf.is.utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Search extends HttpServlet {

    protected void doGet(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {
        String loggedInUser = "alan@turing.com";
        final String searchSQL = "SELECT * FROM dbp057.mainpage WHERE titel like ? ESCAPE '!'";
        //Error message to show when there is a database error
        boolean error_detected = false;
        //List to hold results
        ArrayList<Project> resultList = new ArrayList<>();

        //Escape characters in search query
        String input;
        input = request.getParameter("title");
        if (input != null && !input.equals("")) {
            input = input
                    .replace("!","!!")
                    .replace("%","!%")
                    .replace("_","!_")
                    .replace("[","![");

            //Connect to the database
            try (Connection con = DBUtil.getExternalConnection();
                 PreparedStatement ps = con.prepareStatement(searchSQL)) {
                ps.setString(1,input + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        //Create object for each result and add it to resultList
                        int id = rs.getInt("kennung");
                        String title = rs.getString("titel");
                        String name = rs.getString("name");
                        String status = rs.getString("status");
                        String icon = rs.getString("icon");
                        java.math.BigDecimal sum = rs.getBigDecimal("summe");
                        sum = (sum == null) ? BigDecimal.valueOf(0) : sum;
                        String email = rs.getString("email");

                        Project p = new Project(id,title,name,status,icon,sum,email);
                        resultList.add(p);
                    }
                }

            } catch (SQLException e) {
                //SQL ERROR
                error_detected = true;
            }
        }

        //Set attributes and forward results to user
        request.setAttribute("error_detected",error_detected);
        request.setAttribute("loggedInUser", loggedInUser);
        request.setAttribute("resultList",resultList);
        request.getRequestDispatcher("ProjectFunder_FTL/search.ftl").forward(request,response);
    }
}


