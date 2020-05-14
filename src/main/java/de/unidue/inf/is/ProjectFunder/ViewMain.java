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

public final class ViewMain extends HttpServlet {

    // Returns the mainpage results
    // returns null iff database error
    private ArrayList<Project> getResults() {
        String sql = "SELECT * FROM dbp057.mainpage";

        // List to hold the results
        ArrayList<Project> projectList = new ArrayList<>();

        //connect to database and get results
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                //Read and parse the result set into java objects
                int id = rs.getInt("kennung");
                String title = rs.getString("titel");
                String name = rs.getString("name");
                String status = rs.getString("status");
                String icon = rs.getString("icon");
                java.math.BigDecimal sum = rs.getBigDecimal("summe");
                sum = (sum == null) ? BigDecimal.valueOf(0) : sum;
                String email = rs.getString("email");

                Project p = new Project(id,title,name,status,icon,sum,email);
                projectList.add(p);
            }
        } catch (SQLException e) {
            //On error return null
            return null;
        }

        return projectList;
    }

    protected void doGet(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {
        String loggedInUser = "alan@turing.com";

        //Get results and split them in open and closed projects
        ArrayList<Project> projects = getResults();
        ArrayList<Project> projects_open = new ArrayList<>();
        ArrayList<Project> projects_closed = new ArrayList<>();
        if (projects != null) {
            //NO database error
            for (Project p : projects) {
                if (p.getStatus().equals("offen")) {
                    projects_open.add(p);
                } else {
                    projects_closed.add(p);
                }
            }
        } else {
            //Database Error
            response.sendError(500,"Database Error");
        }

        // set attributes and forward to user
        request.setAttribute("projects_open",projects_open);
        request.setAttribute("projects_closed",projects_closed);
        request.setAttribute("loggedInUser",loggedInUser);
        request.getRequestDispatcher("ProjectFunder_FTL/view_main.ftl").forward(request,response);
    }
}
