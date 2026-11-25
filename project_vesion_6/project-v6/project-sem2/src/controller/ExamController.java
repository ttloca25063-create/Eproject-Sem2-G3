package controller;

import model.Exam;
import service.ExamService;

import java.util.List;

public class ExamController {
    private final ExamService service = new ExamService();
    public List<Exam> getAllExams() { return service.getAllExams(); }
    public String saveExam(Exam e) { return service.saveExam(e); }
    public String deleteExam(int examID) { return service.deleteExam(examID); }
}