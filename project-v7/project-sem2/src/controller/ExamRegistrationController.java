package controller;

import model.ExamRegistration;
import service.ExamRegistrationService;

import java.time.LocalDate;
import java.util.List;

public class ExamRegistrationController {
    private final ExamRegistrationService service = new ExamRegistrationService();

    public String handleRegister(int examID, String studentCode, String fullName,
                                 LocalDate dob, String idNumber, String phone, String email) {
        return service.registerExam(examID, studentCode, fullName, dob, idNumber, phone, email);
    }
    public List<ExamRegistration> getRegistrationList(int examID) {
        return service.getRegistrationsByExam(examID);
    }
    public String confirmPayment(int regID) {
        return service.confirmPayment(regID);
    }
}