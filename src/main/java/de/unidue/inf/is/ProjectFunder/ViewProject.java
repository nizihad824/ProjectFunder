package de.unidue.inf.is.ProjectFunder;

import de.unidue.inf.is.domain.Comment;
import de.unidue.inf.is.domain.Donation;
import de.unidue.inf.is.utils.DBUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewProject extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {

        final String loggedInUser = "alan@turing.com";

        //SQL Queries
        final String commentSQL = "SELECT name, text from dbp057.comment_section WHERE kennung = ?";
        final String donationSQL = "SELECT name, spendenbetrag from dbp057.donation_section WHERE projekt = ?";
        final String infoSQL = "SELECT * FROM dbp057.info_section WHERE kennung = ?";

        //Info section variables
        java.math.BigDecimal sum, limit;
        String title, description, status, creatorMail, creatorName, icon, vtitle;
        Integer vid;

        //Donation section variables
        ArrayList<Donation> donations = new ArrayList<>();

        //Comment section variables
        ArrayList<Comment> comments = new ArrayList<>();


        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendError(400, "Can not parse id");
            return;
        }


        //Connect to database and get results
        try(Connection con = DBUtil.getExternalConnection();
            PreparedStatement psComment = con.prepareStatement(commentSQL);
            PreparedStatement psDonation = con.prepareStatement(donationSQL);
            PreparedStatement psInfo = con.prepareStatement(infoSQL)) {

            psComment.setInt(1, id);
            psDonation.setInt(1, id);
            psInfo.setInt(1, id);

            try(ResultSet rInfo = psInfo.executeQuery();
                ResultSet rDonation = psDonation.executeQuery();
                ResultSet rComment = psComment.executeQuery()) {

                // Read results for info section
                if (rInfo.next()) {
                    sum = rInfo.getBigDecimal("summe");
                    title = rInfo.getString("titel");
                    description = rInfo.getString("beschreibung");
                    status = rInfo.getString("status");
                    limit = rInfo.getBigDecimal("finanzierungslimit");
                    creatorMail = rInfo.getString("ersteller");
                    creatorName = rInfo.getString("name");
                    icon = rInfo.getString("icon");
                    vid = rInfo.getInt("vid"); //Vorgänger ID
                    if (rInfo.wasNull()) vid = null;
                    vtitle = rInfo.getString("vtitel"); // Vorgänger Titel
                } else {
                    //No project found for that id -- abort
                    resp.sendError(400, "Project not found");
                    return;
                }

                //Read donation result set into ArrayList
                while(rDonation.next()) {
                    String name = rDonation.getString("name");
                    BigDecimal amount = rDonation.getBigDecimal("spendenbetrag");

                    donations.add(new Donation(name, amount));
                }

                //Read comment result set into ArrayList
                while(rComment.next()) {
                    String name = rComment.getString("name");
                    String text = rComment.getString("text");

                    comments.add(new Comment(name, text));
                }
            }

            //Set attributes for info section
            if(sum != null) {
                req.setAttribute("sum", sum);
            } else req.setAttribute("sum", 0);
            req.setAttribute("title", title);
            req.setAttribute("description", description);
            req.setAttribute("status", status);
            req.setAttribute("limit", limit);
            req.setAttribute("creatorMail", creatorMail);
            req.setAttribute("creatorName", creatorName);
            req.setAttribute("icon", icon);
            req.setAttribute("id", id);

            if(vid == null) {
                req.setAttribute("vorganger", false);
            } else {
                req.setAttribute("vorganger", true);
                req.setAttribute("vid", vid);
                req.setAttribute("vtitle", vtitle);
            }

            //Set attribute for donation section
            req.setAttribute("donations", donations);

            //Set attribute for comment section
            req.setAttribute("comments", comments);

            req.setAttribute("loggedInUser", loggedInUser);

            req.getRequestDispatcher("ProjectFunder_FTL/view_project.ftl").forward(req, resp);

        } catch(SQLException e) {
            //Handle Error - Abort
            resp.sendError(500, "Database Error");
        }
    }

    //If user clicks delete button
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {


        int id = Integer.parseInt(req.getParameter("projectid"));
        String loggedInUser = req.getParameter("loggedInUser");

        if(!allowedToDelete(loggedInUser, id)) {
            resp.sendError(400,"User is not allowed to delete this project");
            return;
        }


        // SQL Statements to delete everything comment related
        String getCommentIds = "SELECT kommentar FROM dbp057.schreibt WHERE projekt = ?";
        String deleteComment = "DELETE FROM dbp057.kommentar WHERE id = ?";
        String deleteSchreibt = "DELETE FROM dbp057.schreibt where projekt = ?";

        String getDonations = "SELECT spender, spendenbetrag FROM dbp057.spenden WHERE projekt = ?";
        String updateAccount = "UPDATE dbp057.konto SET guthaben = guthaben + ? WHERE inhaber = ?";
        String deleteDonations = "DELETE FROM dbp057.spenden WHERE projekt = ?";

        String deleteProject = "DELETE from dbp057.projekt where kennung = ?";


        //Array Lists to hold the returned values
        ArrayList<Integer> commentIds = new ArrayList<>();
        ArrayList<Donation> donations = new ArrayList<>();

        try (Connection con = DBUtil.getExternalConnection()) {
            try (PreparedStatement psIds = con.prepareStatement(getCommentIds);
                 PreparedStatement psdeleteComment = con.prepareStatement(deleteComment);
                 PreparedStatement psdeleteSchreibt = con.prepareStatement(deleteSchreibt);
                 PreparedStatement psgetDonations = con.prepareStatement(getDonations);
                 PreparedStatement psupdateAccount = con.prepareStatement(updateAccount);
                 PreparedStatement psdeleteDonations = con.prepareStatement(deleteDonations);
                 PreparedStatement psdeleteProject = con.prepareStatement(deleteProject)) {

                //Prepare the SELECT Queries
                psIds.setInt(1, id);
                psgetDonations.setInt(1, id);

                //Get the results of SELECT Queries
                try(ResultSet rIds = psIds.executeQuery();
                    ResultSet rDonations = psgetDonations.executeQuery()) {

                    //Add all comment Ids to list
                    while(rIds.next()) {
                        commentIds.add(rIds.getInt("kommentar"));
                    }

                    //Add all donations to list
                    while(rDonations.next()) {
                        String name = rDonations.getString("spender");
                        BigDecimal amount = rDonations.getBigDecimal("spendenbetrag");
                        donations.add(new Donation(name, amount));
                    }
                }

                // Delete all the stuff
                con.setAutoCommit(false);

                //delete all comments and schreibt relation
                psdeleteSchreibt.setInt(1, id);
                psdeleteSchreibt.executeUpdate();
                for(Integer cid : commentIds) {
                    psdeleteComment.setInt(1, cid);
                    psdeleteComment.executeUpdate();
                }

                //update accounts
                for(Donation d : donations) {
                    psupdateAccount.setBigDecimal(1, d.getAmount());
                    psupdateAccount.setString(2, d.getName());
                    psupdateAccount.executeUpdate();
                }

                //delete donations
                psdeleteDonations.setInt(1, id);
                psdeleteDonations.executeUpdate();

                //delete project
                psdeleteProject.setInt(1, id);
                psdeleteProject.executeUpdate();


                //Success -- commit changes
                con.commit();
                con.setAutoCommit(true);

            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                resp.sendError(500, "SQL ERROR -- Rolling back");
            }
        } catch(SQLException e) {
            e.printStackTrace();
            resp.sendError(500, "SQL ERROR -- Rolling back");
        }

        resp.sendRedirect("/view_main");

    }

    // returns true iff user = creator
    private boolean allowedToDelete(String user, int id) {
        String getCreatorSQL = "SELECT ersteller FROM dbp057.projekt WHERE kennung = ?";

        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(getCreatorSQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    //Check for equality
                    String creator = rs.getString("ersteller");
                    return Objects.equals(creator, user);
                } else {
                    //ID not found -- nothing to do 
                    return false;
                }
            }
        } catch (SQLException e) {
            //SQL ERROR - return false
            return false;
        }
    }
}

