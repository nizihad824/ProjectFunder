package de.unidue.inf.is.ProjectFunder;

import de.unidue.inf.is.domain.Project;
import de.unidue.inf.is.domain.SupportedProjects;
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

public class ViewProfile extends HttpServlet {

    protected void doGet(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {
        String loggedInUser = "alan@turing.com";
        //SQL Queries
        final String supportedSQL = "SELECT * FROM dbp057.supported WHERE spender = ?";
        final String createdSQL = "SELECT kennung, titel, status, icon, summe FROM dbp057.mainpage WHERE email = ?";
        final String infoSQL = "SELECT * FROM dbp057.profileinfo WHERE email = ?";

        String userEmail = request.getParameter("u");
        if (userEmail == null) {
            response.sendError(400,"user is null");
            return;
        }

        //Info section variables
        String name;
        int funding, created;

        //Created and supported projects
        ArrayList<Project> userCreatedProjects = new ArrayList<>();
        ArrayList<SupportedProjects> supportedProjects = new ArrayList<>();

        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement psInfo = con.prepareStatement(infoSQL);
             PreparedStatement psCreated = con.prepareStatement(createdSQL);
             PreparedStatement psSupported = con.prepareStatement(supportedSQL)
        ) {
            psInfo.setString(1,userEmail);
            psCreated.setString(1,userEmail);
            psSupported.setString(1,userEmail);
            try (ResultSet rsInfo = psInfo.executeQuery();
                 ResultSet rsCreated = psCreated.executeQuery();
                 ResultSet rsSupported = psSupported.executeQuery()
            ) {
                //Load info section of profile page
                if (rsInfo.next()) {
                    name = rsInfo.getString("name");
                    funding = rsInfo.getInt("funding");
                    created = rsInfo.getInt("created");
                } else {
                    response.sendError(400,"Unknown user");
                    return;
                }
                //Load projected which have been created by that user
                while (rsCreated.next()) {
                    int id = rsCreated.getInt("kennung");
                    String titel = rsCreated.getString("titel");
                    String status = rsCreated.getString("status");
                    String icon = rsCreated.getString("icon");
                    java.math.BigDecimal sum = rsCreated.getBigDecimal("summe");
                    sum = (sum == null) ? BigDecimal.valueOf(0) : sum;
                    Project p = new Project(id,titel,"",status,icon,sum,"");
                    userCreatedProjects.add(p);
                }
                //Load projects that the user has donated to
                while (rsSupported.next()) {
                    int id = rsSupported.getInt("kennung");
                    String titel = rsSupported.getString("titel");
                    String status = rsSupported.getString("status");
                    String icon = rsSupported.getString("icon");
                    java.math.BigDecimal betrag = rsSupported.getBigDecimal("spendenbetrag");
                    java.math.BigDecimal limit = rsSupported.getBigDecimal("limit");

                    SupportedProjects sp = new SupportedProjects(id,titel,status,icon,limit,betrag);
                    supportedProjects.add(sp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(500,"Database Error");
            return;
        }

        request.setAttribute("userEmail",userEmail);
        request.setAttribute("loggedInUser", loggedInUser);
        request.setAttribute("name",name);
        request.setAttribute("created",created);
        request.setAttribute("funding",funding);
        request.setAttribute("userCreatedProjects",userCreatedProjects);
        request.setAttribute("supportedProjects",supportedProjects);
        request.getRequestDispatcher("ProjectFunder_FTL/view_profile.ftl").forward(request,response);
    }
}
