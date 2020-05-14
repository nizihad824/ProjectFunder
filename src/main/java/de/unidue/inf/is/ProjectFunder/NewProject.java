package de.unidue.inf.is.ProjectFunder;

import de.unidue.inf.is.domain.Project;
import de.unidue.inf.is.utils.DBUtil;
import org.apache.commons.lang.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class NewProject extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final String loggedInUser = "alan@turing.com";

        //Check if a project was created
        boolean success = false;
        String succ = request.getParameter("e");
        if (Objects.equals(succ, "0")) {
            success = true;
        }


        //get all projects created by user
        //these are all the projects the user can choose as predecessors
        ArrayList<Project> projects = getProjects(loggedInUser);

        //Set attributes and forward to user
        request.setAttribute("projectList", projects);
        request.setAttribute("succ", success);
        request.setAttribute("loggedInUser", loggedInUser);
        request.getRequestDispatcher("ProjectFunder_FTL/new_project.ftl").forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //Parse all input parameters
        String creator = req.getParameter("creator");
        BigDecimal limit;
        Integer category;
        Integer vorganger;
        String v = req.getParameter("vorganger");
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        try{
            vorganger = (Objects.equals(v, "None"))? null : NumberUtils.createInteger(v);
            limit = NumberUtils.createBigDecimal(req.getParameter("limit"));
            category = NumberUtils.createInteger(req.getParameter("category"));
        } catch(NumberFormatException e) {
            resp.sendError(500, "Malformed input");
            return;
        }

        //Check if input is valid and store status code
        Status status = validInput(title, limit, category, vorganger, creator);
        if (status == Status.OK) {
            //Input is valid
            if (createProject(title,description,limit,creator,vorganger,category)) {
                //Created project ..
                resp.sendRedirect("/new_project?e=0");
            } else {
                //Project was not created -- DB ERROR
                resp.sendError(500, "Database Error while trying to create project");
            }
        } else {
            // Input is not valid
            String error_message;
            int error_code = 400;
            switch(status) {
                case SQL:
                    error_code = 500;
                    error_message = "Database Error";
                    break;
                case LIMIT:
                    error_message = "Limit is smaller than 100";
                    break;
                case TITLE:
                    error_message = "Title is null or title lenght is zero or greater than 30";
                    break;
                case CREATOR:
                    error_message = "Creator is null";
                    break;
                case CATEGORY:
                    error_message = "category is not between 1 and 4";
                    break;
                case PREDECESSOR:
                    error_message = "predecessor was not created by user";
                    break;
                default:
                    error_message = "Unknown error";
            }
            resp.sendError(error_code, error_message);
        }
    }
    //validates user input, returns status code indicating success or failure
    private Status validInput(String title, BigDecimal limit, Integer category, Integer pid, String creator) {
        if (creator == null) return Status.CREATOR;
        if (title == null) return Status.TITLE;
        if (category == null) return Status.CATEGORY;
        if (limit == null) return Status.LIMIT;

        if (title.length() == 0 || title.length() > 30) {
            return Status.TITLE;
        }
        if (limit.compareTo(new java.math.BigDecimal(100)) < 0) {
            //limit is smaller than 100
            return Status.LIMIT;
        }

        if (category < 1 || category > 4) {
            //Unknown category
            return Status.CATEGORY;
        }

        if(!(pid == null)) {
            //Check if chosen predecessor was created by creator
            ArrayList<Project> projects = getProjects(creator);
            if(projects == null) return Status.SQL;
            boolean found = false;
            for(Project p : projects) {
                if(p.getId() == pid) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                return Status.PREDECESSOR;
            }


        }

        return Status.OK;
    }

    //Returns all projects created by parameter creator
    //Returns null on database error
    private ArrayList<Project> getProjects(String creator) {
        final String getProjectsSQL = "SELECT titel, kennung from dbp057.projekt WHERE ersteller = ?";
        ArrayList<Project> projects = new ArrayList<>();

        //Create connection and get the project objects
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(getProjectsSQL)) {
            ps.setString(1, creator);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    //Add each result to project list
                    int id = rs.getInt("kennung");
                    String title = rs.getString("titel");

                    Project p = new Project(id, title);
                    projects.add(p);
                }
            }
        } catch(SQLException e) {
            return null;
        }

        return projects;
    }

    //Creates project with given parameters
    private boolean createProject(String titel, String beschreibung, BigDecimal limit, String ersteller, Integer vorganger, Integer kategorie) {
        final String newSQL = "INSERT INTO dbp057.projekt (titel, beschreibung, finanzierungslimit, ersteller, vorgaenger, kategorie) VALUES (?, ?, ?, ?, ?, ?)";
        try(Connection con = DBUtil.getExternalConnection();
            PreparedStatement ps = con.prepareStatement(newSQL)) {

            ps.setString(1, titel);
            ps.setString(2, beschreibung);
            ps.setBigDecimal(3, limit);
            ps.setString(4, ersteller);
            if(vorganger == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, vorganger);
            }
            ps.setInt(6, kategorie);
            ps.executeUpdate();

            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Status codes
    enum Status {
        OK, TITLE, LIMIT, CATEGORY, PREDECESSOR, SQL,CREATOR
    }
}
