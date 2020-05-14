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

public class EditProject extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {

        final String loggedInUser = "alan@turing.com";

        //Variables the user can edit
        String title = "", beschreibung = "";
        BigDecimal limit = null;
        int vorganger, kategorie;

        int id;

        //Parse id
        try {
            id = Integer.parseInt(req.getParameter("id"));

        } catch (NumberFormatException e) {
            resp.sendError(400,"can not parse id");
            return;
        }

        //Get the infos that the user can edit
        String infoSQL = "SELECT titel, beschreibung, finanzierungslimit, vorgaenger, kategorie FROM dbp057.projekt WHERE kennung = ?";
        try(Connection con = DBUtil.getExternalConnection();
            PreparedStatement ps = con.prepareStatement(infoSQL)) {
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    title = rs.getString("titel");
                    beschreibung = rs.getString("beschreibung");
                    limit = rs.getBigDecimal("finanzierungslimit");
                    vorganger = rs.getInt("vorgaenger");
                    kategorie = rs.getInt("kategorie");
                } else {
                    resp.sendError(400, "No project for that id");
                }
            }
        } catch(SQLException e) {
            resp.sendError(500, "Database Error");
            return;
        }


        //get all projects created by user
        //these are all the projects the user can choose as predecessors
        //except for the project itself
        ArrayList<Project> projects = getProjects(loggedInUser);
        if(projects != null) {
            for (int i = 0; i < projects.size(); i++) {
                if (projects.get(i).getId() == id) projects.remove(i);
            }
        } else {
            resp.sendError(500, "Database Error");
        }

        //Set attributes and forward to user
        req.setAttribute("title", title);
        req.setAttribute("limit", limit);
        req.setAttribute("description", beschreibung);
        req.setAttribute("projectList",projects);
        req.setAttribute("loggedInUser",loggedInUser);
        req.getRequestDispatcher("ProjectFunder_FTL/edit_project.ftl").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        String creator = req.getParameter("creator");
        BigDecimal limit;
        Integer category;
        Integer vorganger;
        String v = req.getParameter("vorganger");
        String title = req.getParameter("title");
        String desciption = req.getParameter("description");
        int id;

        try {
            vorganger = (Objects.equals(v,"None")) ? null : NumberUtils.createInteger(v);
            limit = NumberUtils.createBigDecimal(req.getParameter("limit"));
            category = NumberUtils.createInteger(req.getParameter("category"));
            id = Integer.parseInt(req.getParameter("id"));

        } catch (NumberFormatException e) {
            resp.sendError(500,"Malformed input");
            return;
        }

        Status status = validInput(title,limit,category,vorganger,creator,id);
        if (status == Status.OK) {
            if (updateDatabase(title,desciption,limit,category,vorganger,id)) {
                resp.sendRedirect("/view_project?id=" + id);
            } else {
                //Updating failed
                resp.sendError(500,"Database Error");
            }
        } else {
            //Status is NOT ok
            switch (status) {
                case SQL:
                    resp.sendError(400,"Database Error");
                    break;
                case CLOSED:
                    resp.sendError(500,"Project is closed");
                    break;
                case PREDECESSOR:
                    resp.sendError(500,"vorganger");
                    break;
                case TITLE:
                    resp.sendError(400,"title error");
                    break;
                case CATEGORY:
                    resp.sendError(400,"category");
                    break;
                case LIMIT:
                    resp.sendError(400,"limit error");
                    break;
                case CREATOR:
                    resp.sendError(400,"not allowed");
                    break;

                default:
                    resp.sendError(400,"unknwone");
            }
        }

    }

    private boolean updateDatabase(String title,String description,BigDecimal limit,Integer category,Integer vorganger,int id) {
        final String updateSQL = "UPDATE dbp057.projekt SET titel = ?, beschreibung = ?, finanzierungslimit = ?, kategorie = ?, vorgaenger = ? WHERE kennung = ?";
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(updateSQL)) {
            ps.setString(1,title);
            ps.setString(2,description);
            ps.setBigDecimal(3,limit);
            ps.setInt(4,category);
            if (vorganger == null) {
                ps.setNull(5,Types.INTEGER);
            } else {
                ps.setInt(5,vorganger);
            }
            ps.setInt(6,id);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Status validInput(String title,BigDecimal limit,Integer category,Integer vorganger,String creator,int id) {
        if (creator == null) return Status.CREATOR;
        if (title == null) return Status.TITLE;
        if (category == null) return Status.CATEGORY;
        if (limit == null) return Status.LIMIT;

        if (title.length() == 0 || title.length() > 30) {
            return Status.TITLE;
        }
        if (category < 1 || category > 4) {
            return Status.CATEGORY;
        }

        if (!(vorganger == null)) {
            if (id == vorganger) return Status.PREDECESSOR;
            //Check if chosen predecessor was created by creator
            ArrayList<Project> projects = getProjects(creator);
            if (projects == null) return Status.SQL;
            boolean found = false;
            for (Project p : projects) {
                if (p.getId() == vorganger) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return Status.PREDECESSOR;
            }
        }

        String sql = "SELECT ersteller, finanzierungslimit, status FROM dbp057.projekt WHERE kennung = ?";
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1,id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String c = rs.getString("ersteller");
                    BigDecimal l = rs.getBigDecimal("finanzierungslimit");
                    String status = rs.getString("status");

                    if (!Objects.equals(creator,c)) return Status.CREATOR;
                    if (Objects.equals(status,"geschlossen")) return Status.CLOSED;
                    if (limit.compareTo(l) < 0) return Status.LIMIT;


                } else {
                    //project with that id does not exist
                    return Status.NE;
                }
            }

            return Status.OK;

        } catch (SQLException e) {
            e.printStackTrace();
            return Status.SQL;
        }
    }

    private ArrayList<Project> getProjects(String creator) {

        final String getProjectsSQL = "SELECT titel, kennung from dbp057.projekt WHERE ersteller = ?";
        ArrayList<Project> projects = new ArrayList<>();

        //Create connection and get the project objects
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(getProjectsSQL)) {
            ps.setString(1,creator);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    //Add each result to project list
                    int id = rs.getInt("kennung");
                    String title = rs.getString("titel");

                    Project p = new Project(id,title);
                    projects.add(p);
                }
            }
        } catch (SQLException e) {
            return null;
        }

        return projects;
    }

    enum Status {OK,TITLE,LIMIT,CATEGORY,PREDECESSOR,CREATOR,SQL,NE,CLOSED}
}
