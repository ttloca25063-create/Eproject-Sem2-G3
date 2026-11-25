package dao;

import model.ExamResult;
import model.ExamResultDetail;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamResultDAO {

    /**
     * Get list of eligible candidates for an exam (LEFT JOIN with ExamResult to check grading status)
     */
    public List<ExamResult> findByExamID(int examID) {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT reg.RegistrationID, reg.FullName, reg.CandidateType, " +
                     "res.ResultID, res.TotalScore, res.ResultStatus, res.GradedDate, res.Note " +
                     "FROM ExamRegistration reg " +
                     "LEFT JOIN ExamResult res ON reg.RegistrationID = res.RegistrationID " +
                     "WHERE reg.ExamID = ? AND reg.Status != 'Cancelled' " +
                     "AND reg.PaidStatus IN ('Paid', 'Exempted') " +
                     "ORDER BY reg.RegistrationID";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ExamResult r = new ExamResult();
                r.setRegistrationID(rs.getInt("RegistrationID"));
                r.setCandidateName(rs.getString("FullName"));
                r.setCandidateType(rs.getString("CandidateType"));
                r.setResultID(rs.getInt("ResultID")); // 0 if not yet graded
                r.setTotalScore(rs.getDouble("TotalScore"));
                String st = rs.getString("ResultStatus");
                r.setResultStatus(st != null ? st : "Pending");
                Date gd = rs.getDate("GradedDate");
                r.setGradedDate(gd != null ? gd.toLocalDate() : null);
                r.setNote(rs.getString("Note"));
                list.add(r);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    /**
     * Get skill score details by ResultID (or default SkillType list if not yet graded)
     */
    public List<ExamResultDetail> getSkillDetails(int examID, int resultID) {
        List<ExamResultDetail> list = new ArrayList<>();
        String sql = "SELECT st.SkillTypeID AS SkillID, st.Name AS SkillName, d.DetailID, COALESCE(d.Score, 0) AS Score " +
                     "FROM Exam e " +
                     "CROSS JOIN SkillType st " +
                     "LEFT JOIN ExamResultDetail d ON d.SkillTypeID = st.SkillTypeID AND d.ResultID = ? " +
                     "WHERE e.ExamID = ? ORDER BY st.SkillTypeID";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resultID);
            ps.setInt(2, examID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ExamResultDetail d = new ExamResultDetail();
                d.setDetailID(rs.getInt("DetailID"));
                d.setResultID(resultID);
                d.setSkillID(rs.getInt("SkillID"));
                d.setSkillName(rs.getString("SkillName"));
                d.setScore(rs.getDouble("Score"));
                list.add(d);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    /**
     * Get pass score from CertificateType table for a given exam
     */
    public double getPassScoreByExamID(int examID) {
        String sql = "SELECT ct.PassScore FROM Exam e " +
                     "JOIN CertificateType ct ON e.CertificateTypeID = ct.CertificateTypeID " +
                     "WHERE e.ExamID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("PassScore");
        } catch (SQLException ex) { ex.printStackTrace(); }
        return 5.0; // default if not configured
    }

    /**
     * Save or update scores (Transaction: ExamResult + ExamResultDetail)
     */
    public boolean saveResultWithDetails(ExamResult res, List<ExamResultDetail> details) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int resultID = res.getResultID();
            if (resultID == 0) {
                String insertSql = "INSERT INTO ExamResult (RegistrationID, TotalScore, AverageScore, ResultStatus, Result, GradedDate, Note) " +
                                   "VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, res.getRegistrationID());
                    ps.setDouble(2, res.getTotalScore());
                    ps.setDouble(3, res.getTotalScore());
                    ps.setString(4, res.getResultStatus());
                    ps.setString(5, res.getResultStatus());
                    ps.setDate(6, Date.valueOf(res.getGradedDate()));
                    ps.setString(7, res.getNote());
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) resultID = rs.getInt(1);
                }
            } else {
                String updateSql = "UPDATE ExamResult SET TotalScore=?, AverageScore=?, ResultStatus=?, Result=?, GradedDate=?, Note=? " +
                                   "WHERE ResultID=?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, res.getTotalScore());
                    ps.setDouble(2, res.getTotalScore());
                    ps.setString(3, res.getResultStatus());
                    ps.setString(4, res.getResultStatus());
                    ps.setDate(5, Date.valueOf(res.getGradedDate()));
                    ps.setString(6, res.getNote());
                    ps.setInt(7, resultID);
                    ps.executeUpdate();
                }
                // Delete old details before re-inserting
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM ExamResultDetail WHERE ResultID=?")) {
                    del.setInt(1, resultID);
                    del.executeUpdate();
                }
            }

            String detailSql = "INSERT INTO ExamResultDetail (ResultID, SkillTypeID, Score) VALUES (?,?,?)";
            try (PreparedStatement psDet = conn.prepareStatement(detailSql)) {
                for (ExamResultDetail d : details) {
                    psDet.setInt(1, resultID);
                    psDet.setInt(2, d.getSkillID());
                    psDet.setDouble(3, d.getScore());
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            ex.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}
