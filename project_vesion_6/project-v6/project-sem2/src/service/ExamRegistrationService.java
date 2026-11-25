package service;

import dao.ExamDAO;
import dao.ExamRegistrationDAO;
import dao.StudentDAO;
import model.Exam;
import model.ExamRegistration;

import java.time.LocalDate;
import java.util.List;

public class ExamRegistrationService {
    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public String registerExam(int examID, String studentCode, String fullName,
                               LocalDate dob, String idNumber, String phone, String email) {
        Exam exam = examDAO.getById(examID);
        if (exam == null) return "Error: Exam does not exist.";
        if (exam.getRegistrationDeadline() != null &&
            LocalDate.now().isAfter(exam.getRegistrationDeadline()))
            return "Error: Registration deadline has passed.";
        if (fullName == null || fullName.trim().isEmpty())
            return "Error: Full name must not be empty.";
        if (phone == null || phone.trim().isEmpty())
            return "Error: Phone number must not be empty.";

        ExamRegistration reg = new ExamRegistration();
        reg.setExamID(examID);
        reg.setFullName(fullName.trim());
        reg.setDob(dob);
        reg.setIdNumber(idNumber);
        reg.setPhone(phone);
        reg.setEmail(email);
        reg.setRegistrationDate(LocalDate.now());
        reg.setStatus("Registered");

        if (studentCode != null && !studentCode.trim().isEmpty()) {
            Integer sid = studentDAO.findStudentIDByCode(studentCode.trim());
            if (sid == null) return "Error: Student code not found or inactive.";
            reg.setStudentID(sid);
            reg.setCandidateType("Internal");
            reg.setFeeExempt(true);
            reg.setExamFee(0.0);
            reg.setPaidStatus("Exempted");
        } else {
            if (idNumber == null || idNumber.trim().isEmpty())
                return "Error: External candidates must provide an ID number.";
            reg.setStudentID(null);
            reg.setCandidateType("External");
            reg.setFeeExempt(false);
            reg.setExamFee(exam.getExamFee());
            reg.setPaidStatus("Unpaid");
        }

        if (regDAO.checkDuplicate(examID, reg.getStudentID(), reg.getIdNumber()))
            return "Error: Candidate already registered for this exam.";

        int newID = regDAO.insert(reg);
        if (newID > 0) {
            if ("Internal".equals(reg.getCandidateType()))
                return "Registration successful (Internal - Fee Exempt)! Reg ID: " + newID;
            return "Registration successful! Please pay: " +
                    String.format("%,.0f", reg.getExamFee()) + " USD. Reg ID: " + newID;
        }
        return "Error: Could not save registration.";
    }

    public List<ExamRegistration> getRegistrationsByExam(int examID) {
        return regDAO.findByExamID(examID);
    }

    public String confirmPayment(int regID) {
        ExamRegistration r = regDAO.findById(regID);
        if (r == null) return "Error: Registration not found.";
        if ("Internal".equals(r.getCandidateType())) return "Error: Internal candidates are fee exempt.";
        if ("Paid".equals(r.getPaidStatus())) return "Error: Already paid.";
        return regDAO.updatePaymentStatus(regID, "Paid")
                ? "Payment confirmed successfully!"
                : "Error: Could not update payment status.";
    }
}
