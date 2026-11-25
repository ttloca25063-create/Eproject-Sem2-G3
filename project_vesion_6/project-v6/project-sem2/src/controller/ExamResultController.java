package controller;

import model.ExamResult;
import model.ExamResultDetail;
import service.ExamResultService;

import java.util.List;

public class ExamResultController {
    private final ExamResultService service = new ExamResultService();

    public List<ExamResult> getResultsByExam(int examID) {
        return service.getResultsByExam(examID);
    }

    public List<ExamResultDetail> getSkillDetails(int examID, int resultID) {
        return service.getSkillDetails(examID, resultID);
    }

    public String saveScore(int examID, ExamResult result, List<ExamResultDetail> details, String note) {
        return service.saveScore(examID, result, details, note);
    }
}