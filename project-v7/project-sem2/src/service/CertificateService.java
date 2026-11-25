package service;

import dao.CertificateDAO;
import model.Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class CertificateService {
    private CertificateDAO certificateDAO;

    public CertificateService(Connection conn) {
        this.certificateDAO = new CertificateDAO(conn);
    }

    public String checkEligibility(int examResultID) throws SQLException {
        try (ResultSet rs = certificateDAO.getEligibilityInfo(examResultID)) {
            if (!rs.next()) {
                return "Exam result record not found!";
            }
            if (rs.getInt("AlreadyIssued") > 0) {
                return "Certificate has already been issued for this exam result!";
            }
            String result = rs.getString("Result");
            if (result == null || !result.equalsIgnoreCase("Pass")) {
                return "Ineligible: Candidate did not pass the exam (Result: " + result + ").";
            }
            boolean isExempt = rs.getBoolean("IsFeeExempt");
            String examPaid = rs.getString("ExamPaid");
            if (!isExempt && !"Paid".equalsIgnoreCase(examPaid)) {
                return "Blocked: Exam fee is unpaid!";
            }
            Integer studentID = (Integer) rs.getObject("StudentID");
            if (studentID != null && certificateDAO.hasUnpaidTuition(studentID)) {
                return "Blocked: Student has unpaid course tuition fees!";
            }
            return "ELIGIBLE";
        }
    }

    public String issueCertificate(int examResultID, int issuedByUserID) throws SQLException {
        String statusCheck = checkEligibility(examResultID);
        if (!"ELIGIBLE".equals(statusCheck)) {
            throw new IllegalStateException(statusCheck);
        }

        try (ResultSet rs = certificateDAO.getEligibilityInfo(examResultID)) {
            if (rs.next()) {
                Integer studentID = (Integer) rs.getObject("StudentID");
                int certTypeID = rs.getInt("CertificateTypeID");
                String certCode = rs.getString("CertCode");

                String certNumber = "CFL-" + LocalDate.now().getYear() + "-" + certCode + "-" +
                                    UUID.randomUUID().toString().substring(0, 5).toUpperCase();

                Certificate newCert = new Certificate(
                    0, certNumber, certTypeID, studentID, examResultID,
                    LocalDate.now(), null, "Valid", issuedByUserID
                );

                if (certificateDAO.insertCertificate(newCert)) {
                    return certNumber;
                }
            }
        }
        return null;
    }
}