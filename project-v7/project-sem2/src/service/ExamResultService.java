package service;

import dao.ExamResultDAO;
import model.ExamResult;
import model.ExamResultDetail;

import java.time.LocalDate;
import java.util.List;

public class ExamResultService {
    private final ExamResultDAO resultDAO = new ExamResultDAO();

    public List<ExamResult> getResultsByExam(int examID) {
        return resultDAO.findByExamID(examID);
    }

    public List<ExamResultDetail> getSkillDetails(int examID, int resultID) {
        return resultDAO.getSkillDetails(examID, resultID);
    }

    public String saveScore(int examID, ExamResult result, List<ExamResultDetail> details, String note) {
        if (result == null) return "Error: No candidate selected.";
        if (details == null || details.isEmpty())
            return "Error: Exam has no skill types configured for grading.";

        double sum = 0;
        for (ExamResultDetail d : details) {
            if (d.getScore() < 0 || d.getScore() > 100)
                return "Error: Score for skill " + d.getSkillName() + " must be between 0 and 100.";
            sum += d.getScore();
        }

        // Calculate average score across all skills
        double avgScore = Math.round((sum / details.size()) * 100.0) / 100.0;
        double passScore = resultDAO.getPassScoreByExamID(examID);

        result.setTotalScore(avgScore);
        result.setResultStatus(avgScore >= passScore ? "Pass" : "Fail");
        result.setGradedDate(LocalDate.now());
        result.setNote(note);

        boolean ok = resultDAO.saveResultWithDetails(result, details);
        return ok
                ? String.format("Score saved successfully! Average: %.2f (Pass: %.2f) -> %s",
                                avgScore, passScore, result.getResultStatus())
                : "Error: Could not save result to database.";
    }
}
