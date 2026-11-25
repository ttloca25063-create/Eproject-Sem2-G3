package service;

import dao.ExamDAO;
import model.Exam;

import java.util.List;

public class ExamService {
    private final ExamDAO examDAO = new ExamDAO();

    public List<Exam> getAllExams() { return examDAO.findAll(); }

    public String saveExam(Exam e) {
        if (e.getExamCode() == null || e.getExamCode().trim().isEmpty())
            return "Error: Exam code must not be empty.";
        if (e.getExamName() == null || e.getExamName().trim().isEmpty())
            return "Error: Exam name must not be empty.";
        if (e.getExamDate() == null) return "Error: Exam date must not be empty.";
        if (e.getRegistrationDeadline() != null &&
            e.getRegistrationDeadline().isAfter(e.getExamDate()))
            return "Error: Registration deadline must be before exam date.";
        if (e.getExamFee() < 0) return "Error: Exam fee is invalid.";

        if (e.getExamID() == 0) {
            int id = examDAO.insert(e);
            return id > 0 ? "Exam added successfully! ID=" + id : "Error: Could not add exam.";
        }
        return examDAO.update(e) ? "Updated successfully!" : "Error: Could not update.";
    }

    public String deleteExam(int examID) {
        if (examID <= 0) return "Error: Invalid exam ID.";
        return examDAO.delete(examID)
                ? "Exam deleted successfully!"
                : "Error: Could not delete (foreign key constraint).";
    }
}
