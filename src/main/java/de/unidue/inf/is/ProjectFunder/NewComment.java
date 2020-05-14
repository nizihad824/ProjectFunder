package de.unidue.inf.is.ProjectFunder;

import de.unidue.inf.is.utils.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class NewComment extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String loggedInUser = "alan@turing.com";
        String projectTitle = "";
        int projectid;
        try {
            projectid = Integer.parseInt(req.getParameter("id"));
            projectTitle = getTitle(projectid);
            if(projectTitle == null){
                resp.sendError(400, "project does not exist");
                return;
            }
        } catch(NumberFormatException e) {
            resp.sendError(400, "malformed input");
        }
        req.setAttribute("loggedInUser", loggedInUser);
        req.setAttribute("projectTitle", projectTitle);
        req.getRequestDispatcher("ProjectFunder_FTL/new_comment.ftl").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String loggedInUser = req.getParameter("loggedInUser");
        String comment = req.getParameter("comment");
        boolean anonymous = Objects.equals(req.getParameter("anonymous"), "true");
        int projectid;
        try {
            projectid = Integer.parseInt(req.getParameter("id"));
        } catch(NumberFormatException e) {
            resp.sendError(400, "Can not parse id");
            return;
        }
        if(insertComment(comment, anonymous, loggedInUser, projectid)){
            //comment was successfully inserted into database
            resp.sendRedirect("/view_project?id=" + projectid);
        } else {
            //comment was not inserted
            resp.sendError(500, "Database Error");
        }
    }

    private String getTitle(int id)  throws NullPointerException{
        final String getTitleSQL = "SELECT titel FROM dbp057.projekt WHERE kennung = ?";

        String title = "";
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(getTitleSQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    title = rs.getString(1);
                } else {
                    throw new NullPointerException();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return title;
    }

    //Inserts comment into database, returns true iff success
    private boolean insertComment(String comment, boolean anonymous, String user, int projectID)  {
        boolean success = true;
        final String insertCommentSQL = "SELECT id from final table (INSERT INTO dbp057.Kommentar (text, sichtbarkeit) VALUES (?,?))";
        final String insertSchreibtSQL = "INSERT INTO dbp057.schreibt (benutzer, projekt, kommentar) values (?,?,?)";
        short generatedID;

        try (Connection con = DBUtil.getExternalConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(insertCommentSQL);
                 PreparedStatement ps2 = con.prepareStatement(insertSchreibtSQL)) {
                Clob clob = con.createClob();
                clob.setString(1, comment);
                ps1.setClob(1, clob);
                if (anonymous) ps1.setString(2, "privat");
                else ps1.setString(2, "oeffentlich");

                //Get the generated id
                try (ResultSet rs = ps1.executeQuery()) {
                    if(rs.next()) {
                        generatedID = rs.getShort(1);
                    } else {
                        //No id was generated -- ERROR
                        throw new SQLException();
                    }
                }

                ps2.setString(1, user);
                ps2.setInt(2, projectID);
                ps2.setShort(3, generatedID);

                ps2.executeUpdate();

                //Success
                con.commit();
                con.setAutoCommit(true);
            } catch (SQLException e) {
                //rollback all changes
                e.printStackTrace();
                success = false;

                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }
}
