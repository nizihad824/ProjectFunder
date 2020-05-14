package de.unidue.inf.is.ProjectFunder;

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
import java.util.Objects;

public class NewProjectFund extends HttpServlet {
    final String loggedInUser = "alan@turing.com";

    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {

        String title;
        int id;

        // Check if there was an error with the previous post request
        try {
            id = Integer.parseInt(req.getParameter("id"));
            title = getTitle(id);
            if (title == null) {
                resp.sendError(400,"Id not valid");
                return;
            }
        } catch (NumberFormatException e) {
            resp.sendError(400,"Can not parse id");
            return;
        }


        //Set attributes
        req.setAttribute("projectTitle",title);
        req.setAttribute("loggedInUser",loggedInUser);
        req.setAttribute("id",id);
        req.getRequestDispatcher("ProjectFunder_FTL/new_project_fund.ftl").forward(req,resp);

    }

    protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        Status status;
        try {
            //Read parameters
            boolean anonymous = Objects.equals(req.getParameter("anonymous"),"true"); //Defaults to false if attribute is null
            String user = req.getParameter("user");
            java.math.BigDecimal amount = new java.math.BigDecimal(req.getParameter("amount"));
            int id = Integer.parseInt(req.getParameter("id"));
            if (user == null) {
                resp.sendError(400,"No user specified");
            }

            //Check if input is valid
            status = validInput(user,id,amount);
            if (status == Status.OK) {
                //Valid -- Input into database
                if (inputInDatabase(user,id,amount,anonymous)) {
                    //success -- redirect to detail page
                    resp.sendRedirect("view_project?id=" + id);
                } else {
                    // no success
                    resp.sendError(500,"Databaser Error. No money donated");
                }
            } else {
                switch (status) {
                    case SQL:
                        resp.sendError(500,"Database Error");
                        break;
                    case ZERO:
                        resp.sendError(400,"Amount ist not greater than 0");
                        break;
                    case MONEY:
                        resp.sendError(400,"User does not have enough money");
                        break;
                    case CLOSED:
                        resp.sendError(499,"Project is closed");
                        break;
                    case DONATION:
                        resp.sendError(400,"User has already donated");
                        break;
                }
            }
        } catch (NumberFormatException e) {
            resp.sendError(400,"Can not parse id");
        }
    }

    private boolean inputInDatabase(String user,int id,java.math.BigDecimal amount,boolean anonymous) {
        final String spendenSQL = "INSERT INTO dbp057.spenden(spender, projekt, spendenbetrag, sichtbarkeit) values (?, ?, ?, ?)";
        final String kontoSQL = "UPDATE dbp057.konto SET guthaben = guthaben - ? WHERE inhaber = ?";

        try (Connection con = DBUtil.getExternalConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(spendenSQL);
                 PreparedStatement ps2 = con.prepareStatement(kontoSQL)) {
                ps1.setString(1,user);
                ps1.setInt(2,id);
                ps1.setBigDecimal(3,amount);
                if (anonymous) {
                    ps1.setString(4,"privat");
                } else {
                    ps1.setString(4,"oeffentlich");
                }

                ps2.setBigDecimal(1,amount);
                ps2.setString(2,user);

                ps1.executeUpdate();
                ps2.executeUpdate();

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
            }
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    //Check if the input is valid
    private Status validInput(String user,int id,BigDecimal amount) {
        if (amount.compareTo(new java.math.BigDecimal(0)) < 0) {
            //Amount is NOT greater than 0
            return Status.ZERO;
        }

        final String isopenSQL = "SELECT count(1) FROM dbp057.projekt WHERE kennung = ? AND status = 'offen'";
        final String hasbalanceSQL = "SELECT count(1) FROM dbp057.konto WHERE guthaben >=  ? AND inhaber = ?";
        final String hasdonatedSQL = "SELECT count(1) from dbp057.spenden where spender = ? AND projekt = ?";


        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement open = con.prepareStatement(isopenSQL);
             PreparedStatement balance = con.prepareStatement(hasbalanceSQL);
             PreparedStatement donated = con.prepareStatement(hasdonatedSQL)) {
            open.setInt(1,id);
            balance.setBigDecimal(1,amount);
            balance.setString(2,user);
            donated.setString(1,user);
            donated.setInt(2,id);

            try (ResultSet rOpen = open.executeQuery();
                 ResultSet rBalance = balance.executeQuery();
                 ResultSet rDonated = donated.executeQuery()) {

                //Check if project is open
                if (rOpen.next()) {
                    int rows = rOpen.getInt(1);
                    if (rows != 1) {
                        return Status.CLOSED;
                    }
                } else return Status.SQL;

                //Check if user has enough money
                if (rBalance.next()) {
                    int rows = rBalance.getInt(1);
                    if (rows != 1) {
                        return Status.MONEY;
                    }
                } else return Status.SQL;

                //Check if user has already donated to project
                if (rDonated.next()) {
                    int rows = rDonated.getInt(1);
                    if (rows != 0) {
                        return Status.DONATION;
                    }
                } else return Status.SQL;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Status.SQL;
        }

        //Everything is fine
        return Status.OK;
    }

    //Get title for given id
    //Returns null if not found or database error
    private String getTitle(int id) {
        final String getTitleSQL = "SELECT titel FROM dbp057.projekt WHERE kennung = ?";

        String title;
        try (Connection con = DBUtil.getExternalConnection();
             PreparedStatement ps = con.prepareStatement(getTitleSQL)) {
            ps.setInt(1,id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    title = rs.getString(1);
                } else {
                    title = null;
                }
            }
        } catch (SQLException e) {
            title = null;
            e.printStackTrace();
        }
        return title;
    }

    enum Status {
        OK,CLOSED,ZERO,MONEY,DONATION,SQL
    }
}
