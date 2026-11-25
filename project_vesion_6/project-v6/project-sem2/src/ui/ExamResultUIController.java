package ui;

import controller.ExamResultController;
import dao.ExamDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Exam;
import model.ExamResult;
import model.ExamResultDetail;
import model.User;
import util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamResultUIController {

    @FXML private ComboBox<Exam> cbExam;
    @FXML private TableView<ExamResult> tblResults;
    @FXML private TableColumn<ExamResult, Integer> colRegID;
    @FXML private TableColumn<ExamResult, String> colName, colType, colStatus;
    @FXML private TableColumn<ExamResult, Double> colTotal;

    @FXML private Label lblSelectedCandidate, lblMessage;
    @FXML private VBox vboxSkills;
    @FXML private TextArea txtNote;

    private final ExamResultController controller = new ExamResultController();
    private final ExamDAO examDAO = new ExamDAO();
    private final ObservableList<ExamResult> resultList = FXCollections.observableArrayList();

    private ExamResult selectedResult = null;
    private List<ExamResultDetail> currentDetails = null;
    private final Map<Integer, TextField> skillInputMap = new HashMap<>();

    @FXML
    public void initialize() {
        colRegID.setCellValueFactory(new PropertyValueFactory<>("registrationID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("candidateName"));
        colType.setCellValueFactory(new PropertyValueFactory<>("candidateType"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));
        tblResults.setItems(resultList);

        cbExam.setItems(FXCollections.observableArrayList(examDAO.findAll()));
        cbExam.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getExamCode() + " - " + item.getExamName());
            }
        });
        cbExam.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getExamCode() + " - " + item.getExamName());
            }
        });

        cbExam.valueProperty().addListener((obs, o, exam) -> {
            clearRightPanel();
            if (exam != null) loadCandidates(exam.getExamID());
            else resultList.clear();
        });

        tblResults.getSelectionModel().selectedItemProperty().addListener((obs, o, res) -> {
            selectedResult = res;
            if (res != null && cbExam.getValue() != null) {
                loadSkillInputs(cbExam.getValue().getExamID(), res);
            }
        });
    }

    private void loadCandidates(int examID) {
        resultList.setAll(controller.getResultsByExam(examID));
    }

    private void loadSkillInputs(int examID, ExamResult res) {
        lblSelectedCandidate.setText("Candidate: " + res.getCandidateName() + " (Reg ID: " + res.getRegistrationID() + ")");
        txtNote.setText(res.getNote() != null ? res.getNote() : "");
        vboxSkills.getChildren().clear();
        skillInputMap.clear();

        currentDetails = controller.getSkillDetails(examID, res.getResultID());
        for (ExamResultDetail d : currentDetails) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label lbl = new Label(d.getSkillName() + ":");
            lbl.setPrefWidth(140);

            TextField tf = new TextField(res.getResultID() > 0 ? String.valueOf(d.getScore()) : "");
            tf.setPromptText("0 - 100");
            HBox.setHgrow(tf, Priority.ALWAYS);

            skillInputMap.put(d.getSkillID(), tf);
            row.getChildren().addAll(lbl, tf);
            vboxSkills.getChildren().add(row);
        }
    }

    @FXML
    private void handleSaveScore() {
        User current = SessionManager.getCurrentUser();
        if (current != null && current.getRoleID() == 3) {
            showMessage("Error: Only Staff/Admin can modify exam scores.", true);
            return;
        }
        Exam exam = cbExam.getValue();
        if (exam == null || selectedResult == null || currentDetails == null) {
            showMessage("Error: Please select an exam and a candidate from the table.", true);
            return;
        }

        try {
            for (ExamResultDetail d : currentDetails) {
                TextField tf = skillInputMap.get(d.getSkillID());
                double val = Double.parseDouble(tf.getText().trim());
                d.setScore(val);
            }
        } catch (NumberFormatException ex) {
            showMessage("Error: Scores must be valid numbers.", true);
            return;
        }

        String resMsg = controller.saveScore(exam.getExamID(), selectedResult, currentDetails, txtNote.getText().trim());
        boolean err = resMsg.toLowerCase().startsWith("error");
        showMessage(resMsg, err);
        if (!err) loadCandidates(exam.getExamID());
    }

    private void clearRightPanel() {
        selectedResult = null;
        currentDetails = null;
        lblSelectedCandidate.setText("Candidate: (Not selected)");
        vboxSkills.getChildren().clear();
        skillInputMap.clear();
        txtNote.clear();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setStyle(isError ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #27ae60;");
    }
}