-- ========================================================
-- Database Setup for Certificate Generation System
-- Language: English
-- Character Set: UTF-8 (utf8mb4)
-- ========================================================

DROP DATABASE IF EXISTS LanguageCenterDB;
CREATE DATABASE LanguageCenterDB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE LanguageCenterDB;

-- 1. Role
CREATE TABLE IF NOT EXISTS `Role` (
    `RoleID` INT AUTO_INCREMENT PRIMARY KEY,
    `RoleName` VARCHAR(100) NOT NULL UNIQUE,
    `Description` TEXT
) ENGINE=InnoDB;

-- 2. Permission
CREATE TABLE IF NOT EXISTS `Permission` (
    `PermissionID` INT AUTO_INCREMENT PRIMARY KEY,
    `PermissionName` VARCHAR(100) NOT NULL UNIQUE,
    `Module` VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- 3. RolePermission
CREATE TABLE IF NOT EXISTS `RolePermission` (
    `RoleID` INT NOT NULL,
    `PermissionID` INT NOT NULL,
    PRIMARY KEY (`RoleID`, `PermissionID`),
    CONSTRAINT `fk_roleperm_role` FOREIGN KEY (`RoleID`) REFERENCES `Role`(`RoleID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_roleperm_perm` FOREIGN KEY (`PermissionID`) REFERENCES `Permission`(`PermissionID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 4. User
CREATE TABLE IF NOT EXISTS `User` (
    `UserID` INT AUTO_INCREMENT PRIMARY KEY,
    `RoleID` INT NULL,
    `Username` VARCHAR(100) NOT NULL UNIQUE,
    `PasswordHash` VARCHAR(255) NOT NULL,
    `FullName` VARCHAR(150) NOT NULL,
    `Email` VARCHAR(150) UNIQUE,
    `Phone` VARCHAR(20),
    `DateOfBirth` DATE,
    `Gender` VARCHAR(20),
    `Address` TEXT,
    `Status` VARCHAR(50) DEFAULT 'Active',
    `CreatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `UpdatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_role` FOREIGN KEY (`RoleID`) REFERENCES `Role`(`RoleID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 5. Report
CREATE TABLE IF NOT EXISTS `Report` (
    `ReportID` INT AUTO_INCREMENT PRIMARY KEY,
    `ReportType` VARCHAR(100) NOT NULL,
    `ReportMonth` VARCHAR(20),
    `CreatedBy` INT NOT NULL,
    `CreatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `FilePath` VARCHAR(255),
    CONSTRAINT `fk_report_user` FOREIGN KEY (`CreatedBy`) REFERENCES `User`(`UserID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 6. Teacher
CREATE TABLE IF NOT EXISTS `Teacher` (
    `TeacherID` INT AUTO_INCREMENT PRIMARY KEY,
    `UserID` INT NOT NULL UNIQUE,
    `TeacherCode` VARCHAR(50) NOT NULL UNIQUE,
    `Degree` VARCHAR(100),
    `Experience` VARCHAR(255),
    `HireDate` DATE,
    `Status` VARCHAR(50) DEFAULT 'Active',
    CONSTRAINT `fk_teacher_user` FOREIGN KEY (`UserID`) REFERENCES `User`(`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 7. Student
CREATE TABLE IF NOT EXISTS `Student` (
    `StudentID` INT AUTO_INCREMENT PRIMARY KEY,
    `UserID` INT UNIQUE,
    `StudentCode` VARCHAR(50) NOT NULL UNIQUE,
    `EnrollmentDate` DATE,
    `Status` VARCHAR(50) DEFAULT 'Active',
    CONSTRAINT `fk_student_user` FOREIGN KEY (`UserID`) REFERENCES `User`(`UserID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 8. SkillType
CREATE TABLE IF NOT EXISTS `SkillType` (
    `SkillTypeID` INT AUTO_INCREMENT PRIMARY KEY,
    `Code` VARCHAR(50) NOT NULL UNIQUE,
    `Name` VARCHAR(100) NOT NULL,
    `MinPassScore` DECIMAL(5, 2) DEFAULT 5.00
) ENGINE=InnoDB;

-- 9. TeacherSkill
CREATE TABLE IF NOT EXISTS `TeacherSkill` (
    `TeacherID` INT NOT NULL,
    `SkillTypeID` INT NOT NULL,
    `ProficiencyLevel` VARCHAR(50),
    PRIMARY KEY (`TeacherID`, `SkillTypeID`),
    CONSTRAINT `fk_teacherskill_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `Teacher`(`TeacherID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_teacherskill_skill` FOREIGN KEY (`SkillTypeID`) REFERENCES `SkillType`(`SkillTypeID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 10. CertificateType
CREATE TABLE IF NOT EXISTS `CertificateType` (
    `CertificateTypeID` INT AUTO_INCREMENT PRIMARY KEY,
    `Code` VARCHAR(50) NOT NULL UNIQUE,
    `Name` VARCHAR(150) NOT NULL,
    `Level` VARCHAR(50),
    `Description` TEXT,
    `PassScore` DECIMAL(5, 2) DEFAULT 5.00
) ENGINE=InnoDB;

-- 11. Course
CREATE TABLE IF NOT EXISTS `Course` (
    `CourseID` INT AUTO_INCREMENT PRIMARY KEY,
    `CourseCode` VARCHAR(50) NOT NULL UNIQUE,
    `CourseName` VARCHAR(150) NOT NULL,
    `CertificateTypeID` INT,
    `Duration` INT COMMENT 'Duration in hours or sessions',
    `TuitionFee` DECIMAL(12, 2) DEFAULT 0.00,
    `StartDate` DATE,
    `EndDate` DATE,
    `Status` VARCHAR(50) DEFAULT 'Active',
    CONSTRAINT `fk_course_certtype` FOREIGN KEY (`CertificateTypeID`) REFERENCES `CertificateType`(`CertificateTypeID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 12. Class
CREATE TABLE IF NOT EXISTS `Class` (
    `ClassID` INT AUTO_INCREMENT PRIMARY KEY,
    `ClassCode` VARCHAR(50) NOT NULL UNIQUE,
    `ClassName` VARCHAR(150) NOT NULL,
    `CourseID` INT NOT NULL,
    `TeacherID` INT COMMENT 'Homeroom teacher',
    `StartDate` DATE,
    `EndDate` DATE,
    `MaxStudents` INT DEFAULT 30,
    `Room` VARCHAR(50),
    `Schedule` VARCHAR(255),
    `Status` VARCHAR(50) DEFAULT 'Open',
    CONSTRAINT `fk_class_course` FOREIGN KEY (`CourseID`) REFERENCES `Course`(`CourseID`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_class_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `Teacher`(`TeacherID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 13. TeachingSchedule
CREATE TABLE IF NOT EXISTS `TeachingSchedule` (
    `ScheduleID` INT AUTO_INCREMENT PRIMARY KEY,
    `ClassID` INT NOT NULL,
    `TeacherID` INT NOT NULL,
    `SkillTypeID` INT,
    `TeachingDate` DATE NOT NULL,
    `StartTime` TIME NOT NULL,
    `EndTime` TIME NOT NULL,
    `Room` VARCHAR(50),
    `Status` VARCHAR(50) DEFAULT 'Scheduled',
    CONSTRAINT `fk_schedule_class` FOREIGN KEY (`ClassID`) REFERENCES `Class`(`ClassID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_schedule_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `Teacher`(`TeacherID`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_schedule_skill` FOREIGN KEY (`SkillTypeID`) REFERENCES `SkillType`(`SkillTypeID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 14. ClassEnrollment
CREATE TABLE IF NOT EXISTS `ClassEnrollment` (
    `EnrollmentID` INT AUTO_INCREMENT PRIMARY KEY,
    `StudentID` INT NOT NULL,
    `ClassID` INT NOT NULL,
    `EnrollmentDate` DATE NOT NULL,
    `TuitionFee` DECIMAL(12, 2) DEFAULT 0.00,
    `Discount` DECIMAL(12, 2) DEFAULT 0.00,
    `PaymentStatus` VARCHAR(50) DEFAULT 'Unpaid',
    `Status` VARCHAR(50) DEFAULT 'Enrolled',
    CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`StudentID`) REFERENCES `Student`(`StudentID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_enrollment_class` FOREIGN KEY (`ClassID`) REFERENCES `Class`(`ClassID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 15. Payment
CREATE TABLE IF NOT EXISTS `Payment` (
    `PaymentID` INT AUTO_INCREMENT PRIMARY KEY,
    `EnrollmentID` INT NOT NULL,
    `Amount` DECIMAL(12, 2) NOT NULL,
    `PaymentDate` DATE NOT NULL,
    `Method` VARCHAR(50),
    `Note` TEXT,
    CONSTRAINT `fk_payment_enrollment` FOREIGN KEY (`EnrollmentID`) REFERENCES `ClassEnrollment`(`EnrollmentID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 16. PeriodicTest
CREATE TABLE IF NOT EXISTS `PeriodicTest` (
    `TestID` INT AUTO_INCREMENT PRIMARY KEY,
    `ClassID` INT NOT NULL,
    `TestName` VARCHAR(150) NOT NULL,
    `TestDate` DATE NOT NULL,
    `SkillTypeID` INT,
    `MaxScore` DECIMAL(5, 2) DEFAULT 10.00,
    CONSTRAINT `fk_periodictest_class` FOREIGN KEY (`ClassID`) REFERENCES `Class`(`ClassID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_periodictest_skill` FOREIGN KEY (`SkillTypeID`) REFERENCES `SkillType`(`SkillTypeID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 17. TestScore
CREATE TABLE IF NOT EXISTS `TestScore` (
    `TestID` INT NOT NULL,
    `StudentID` INT NOT NULL,
    `Score` DECIMAL(5, 2) NOT NULL,
    `Note` TEXT,
    PRIMARY KEY (`TestID`, `StudentID`),
    CONSTRAINT `fk_testscore_test` FOREIGN KEY (`TestID`) REFERENCES `PeriodicTest`(`TestID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_testscore_student` FOREIGN KEY (`StudentID`) REFERENCES `Student`(`StudentID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 18. Exam
CREATE TABLE IF NOT EXISTS `Exam` (
    `ExamID` INT AUTO_INCREMENT PRIMARY KEY,
    `ExamCode` VARCHAR(50) NOT NULL UNIQUE,
    `ExamName` VARCHAR(150) NOT NULL,
    `CertificateTypeID` INT,
    `ExamDate` DATE NOT NULL,
    `RegistrationDeadline` DATE,
    `Location` VARCHAR(255),
    `ExamFee` DECIMAL(12, 2) DEFAULT 0.00,
    `Status` VARCHAR(50) DEFAULT 'Upcoming',
    CONSTRAINT `fk_exam_certtype` FOREIGN KEY (`CertificateTypeID`) REFERENCES `CertificateType`(`CertificateTypeID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 19. ExamRegistration
CREATE TABLE IF NOT EXISTS `ExamRegistration` (
    `RegistrationID` INT AUTO_INCREMENT PRIMARY KEY,
    `ExamID` INT NOT NULL,
    `StudentID` INT NULL COMMENT 'Nullable for external candidates',
    `CandidateType` VARCHAR(50) DEFAULT 'Internal',
    `FullName` VARCHAR(150) NOT NULL,
    `DOB` DATE,
    `IDNumber` VARCHAR(50),
    `Phone` VARCHAR(20),
    `Email` VARCHAR(150),
    `IsFeeExempt` BOOLEAN DEFAULT FALSE,
    `ExamFee` DECIMAL(12, 2) DEFAULT 0.00,
    `PaidStatus` VARCHAR(50) DEFAULT 'Unpaid',
    `RegistrationDate` DATE NOT NULL,
    `Status` VARCHAR(50) DEFAULT 'Registered',
    CONSTRAINT `fk_examreg_exam` FOREIGN KEY (`ExamID`) REFERENCES `Exam`(`ExamID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_examreg_student` FOREIGN KEY (`StudentID`) REFERENCES `Student`(`StudentID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 20. ExamResult
CREATE TABLE IF NOT EXISTS `ExamResult` (
    `ResultID` INT AUTO_INCREMENT PRIMARY KEY,
    `RegistrationID` INT NOT NULL UNIQUE,
    `AverageScore` DECIMAL(5, 2) DEFAULT 0.00,
    `TotalScore` DECIMAL(5, 2) DEFAULT 0.00,
    `Result` VARCHAR(50) DEFAULT 'Pending',
    `ResultStatus` VARCHAR(50) DEFAULT 'Pending',
    `ExamDate` DATE NULL,
    `GradedDate` DATE NULL,
    `GradedBy` INT NULL,
    `Note` TEXT NULL,
    CONSTRAINT `fk_examresult_reg` FOREIGN KEY (`RegistrationID`) REFERENCES `ExamRegistration`(`RegistrationID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_examresult_user` FOREIGN KEY (`GradedBy`) REFERENCES `User`(`UserID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 21. ExamResultDetail
CREATE TABLE IF NOT EXISTS `ExamResultDetail` (
    `DetailID` INT AUTO_INCREMENT PRIMARY KEY,
    `ResultID` INT NOT NULL,
    `SkillTypeID` INT NOT NULL,
    `Score` DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT `fk_examdetail_result` FOREIGN KEY (`ResultID`) REFERENCES `ExamResult`(`ResultID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_examdetail_skill` FOREIGN KEY (`SkillTypeID`) REFERENCES `SkillType`(`SkillTypeID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 22. Certificate
CREATE TABLE IF NOT EXISTS `Certificate` (
    `CertificateID` INT AUTO_INCREMENT PRIMARY KEY,
    `CertificateNumber` VARCHAR(100) NOT NULL UNIQUE,
    `CertificateTypeID` INT NOT NULL,
    `StudentID` INT NULL,
    `ExamResultID` INT UNIQUE,
    `IssueDate` DATE NOT NULL,
    `ExpiryDate` DATE,
    `Status` VARCHAR(50) DEFAULT 'Valid',
    `IssuedBy` INT,
    CONSTRAINT `fk_cert_type` FOREIGN KEY (`CertificateTypeID`) REFERENCES `CertificateType`(`CertificateTypeID`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_cert_student` FOREIGN KEY (`StudentID`) REFERENCES `Student`(`StudentID`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_cert_result` FOREIGN KEY (`ExamResultID`) REFERENCES `ExamResult`(`ResultID`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_cert_user` FOREIGN KEY (`IssuedBy`) REFERENCES `User`(`UserID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ========================================================
-- SEED DATA (All in English)
-- ========================================================

-- Roles
INSERT IGNORE INTO `Role` (`RoleID`, `RoleName`, `Description`) VALUES
(1, 'Admin', 'System Administrator / Director'),
(2, 'Staff', 'Academic & Examination Staff'),
(3, 'Student', 'Course Learner / Exam Candidate');

-- Users (Demo Password for all demo accounts is '123456')
-- SHA-256('123456') = '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'
INSERT IGNORE INTO `User` (`UserID`, `RoleID`, `Username`, `PasswordHash`, `FullName`, `Email`, `Phone`, `DateOfBirth`, `Gender`, `Address`, `Status`) VALUES
(1, 1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'System Administrator', 'admin@cfl.edu', '0901000001', '1988-01-01', 'Male', 'Building A, Central District', 'Active'),
(2, 2, 'staff', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Academic Staff Member', 'staff@cfl.edu', '0901000002', '1992-05-10', 'Female', 'Building B, Central District', 'Active'),
(3, 3, 'student', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Alex Johnson', 'student@cfl.edu', '0901234567', '2003-05-15', 'Male', 'Campus Dormitory Block 5', 'Active'),
(4, 2, 'teacher1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Dr. Robert Miller', 'robert.miller@cfl.edu', '0901000004', '1985-09-20', 'Male', 'Faculty Residence Suite 7', 'Active');

-- Permissions
INSERT IGNORE INTO `Permission` (`PermissionID`, `PermissionName`, `Module`) VALUES
(1, 'MANAGE_USERS', 'Administration'),
(2, 'MANAGE_TEACHERS', 'Academic'),
(3, 'MANAGE_STUDENTS', 'Academic'),
(4, 'MANAGE_SCHEDULES', 'Academic'),
(5, 'MANAGE_COURSES', 'Academic'),
(6, 'MANAGE_CLASSES', 'Academic'),
(7, 'MANAGE_EXAMS', 'Examination'),
(8, 'ISSUE_CERTIFICATES', 'Certification'),
(9, 'VIEW_REPORTS', 'Reporting');

-- Role Permissions
INSERT IGNORE INTO `RolePermission` (`RoleID`, `PermissionID`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
(3, 9);

-- Skills
INSERT IGNORE INTO `SkillType` (`SkillTypeID`, `Code`, `Name`, `MinPassScore`) VALUES
(1, 'LIST', 'Listening', 5.00),
(2, 'SPEAK', 'Speaking', 5.00),
(3, 'READ', 'Reading', 5.00),
(4, 'WRITE', 'Writing', 5.00);

-- Certificate Types / Categories
INSERT IGNORE INTO `CertificateType` (`CertificateTypeID`, `Code`, `Name`, `Level`, `Description`, `PassScore`) VALUES
(1, 'IELTS', 'IELTS Academic', 'B2-C2', 'International English Language Testing System - Academic Module', 6.50),
(2, 'TOEIC', 'TOEIC 4 Skills', 'B1-C1', 'Test of English for International Communication', 600.00),
(3, 'VSTEP', 'VSTEP Level 3-5', 'B1-C1', 'Standardized Test of English Proficiency', 6.00);

-- Teachers
INSERT IGNORE INTO `Teacher` (`TeacherID`, `UserID`, `TeacherCode`, `Degree`, `Experience`, `HireDate`, `Status`) VALUES
(1, 4, 'T001', 'PhD in Applied Linguistics', '8 years in IELTS training', '2020-03-01', 'Active');

-- Teacher Skills
INSERT IGNORE INTO `TeacherSkill` (`TeacherID`, `SkillTypeID`, `ProficiencyLevel`) VALUES
(1, 1, 'IELTS 8.5'),
(1, 2, 'IELTS 8.5'),
(1, 3, 'IELTS 9.0'),
(1, 4, 'IELTS 8.0');

-- Student Record
INSERT IGNORE INTO `Student` (`StudentID`, `UserID`, `StudentCode`, `EnrollmentDate`, `Status`) VALUES
(1, 3, 'SV001', '2026-09-01', 'Active');

-- Course
INSERT IGNORE INTO `Course` (`CourseID`, `CourseCode`, `CourseName`, `CertificateTypeID`, `Duration`, `TuitionFee`, `StartDate`, `EndDate`, `Status`) VALUES
(1, 'C-IELTS-65', 'IELTS Intensive 6.5+', 1, 120, 450.00, '2026-11-01', '2027-02-01', 'Active'),
(2, 'C-TOEIC-600', 'TOEIC Comprehensive 600+', 2, 80, 280.00, '2026-11-15', '2027-01-15', 'Active');

-- Class
INSERT IGNORE INTO `Class` (`ClassID`, `ClassCode`, `ClassName`, `CourseID`, `TeacherID`, `StartDate`, `EndDate`, `MaxStudents`, `Room`, `Schedule`, `Status`) VALUES
(1, 'CLS-IELTS-01', 'IELTS Intensive K01', 1, 1, '2026-11-01', '2027-02-01', 25, 'Room 201', 'Mon-Wed-Fri 18:00-20:00', 'Open'),
(2, 'CLS-TOEIC-01', 'TOEIC Prep K01', 2, 1, '2026-11-15', '2027-01-15', 30, 'Room 102', 'Tue-Thu-Sat 18:00-20:00', 'Open');

-- Class Enrollment
INSERT IGNORE INTO `ClassEnrollment` (`EnrollmentID`, `StudentID`, `ClassID`, `EnrollmentDate`, `TuitionFee`, `Discount`, `PaymentStatus`, `Status`) VALUES
(1, 1, 1, '2026-11-01', 450.00, 50.00, 'Paid', 'Enrolled');

-- Payment
INSERT IGNORE INTO `Payment` (`PaymentID`, `EnrollmentID`, `Amount`, `PaymentDate`, `Method`, `Note`) VALUES
(1, 1, 400.00, '2026-11-01', 'Bank Transfer', 'Full tuition settlement');

-- Periodic Test
INSERT IGNORE INTO `PeriodicTest` (`TestID`, `ClassID`, `TestName`, `TestDate`, `SkillTypeID`, `MaxScore`) VALUES
(1, 1, 'Midterm Reading Test', '2026-11-15', 3, 10.00),
(2, 1, 'Listening Progress Test', '2026-11-20', 1, 10.00);

-- Test Score
INSERT IGNORE INTO `TestScore` (`TestID`, `StudentID`, `Score`, `Note`) VALUES
(1, 1, 8.50, 'Excellent reading comprehension'),
(2, 1, 8.00, 'Good auditory recognition');

-- Teaching Schedule
INSERT IGNORE INTO `TeachingSchedule` (`ScheduleID`, `ClassID`, `TeacherID`, `SkillTypeID`, `TeachingDate`, `StartTime`, `EndTime`, `Room`, `Status`) VALUES
(1, 1, 1, 3, '2026-11-28', '18:00:00', '20:00:00', 'Room 201', 'Scheduled'),
(2, 1, 1, 1, '2026-11-30', '18:00:00', '20:00:00', 'Room 201', 'Scheduled');

-- Exam
INSERT IGNORE INTO `Exam` (`ExamID`, `ExamCode`, `ExamName`, `CertificateTypeID`, `ExamDate`, `RegistrationDeadline`, `Location`, `ExamFee`, `Status`) VALUES
(1, 'EX-IELTS-2612', 'IELTS Official Test Dec 2026', 1, '2026-12-20', '2026-12-10', 'Hall A - Main Building', 220.00, 'Upcoming'),
(2, 'EX-IELTS-2611', 'IELTS Official Test Nov 2026', 1, '2026-11-20', '2026-11-10', 'Hall B - Main Building', 220.00, 'Completed');

-- Exam Registration
INSERT IGNORE INTO `ExamRegistration` (`RegistrationID`, `ExamID`, `StudentID`, `CandidateType`, `FullName`, `DOB`, `IDNumber`, `Phone`, `Email`, `IsFeeExempt`, `ExamFee`, `PaidStatus`, `RegistrationDate`, `Status`) VALUES
(1, 2, 1, 'Internal', 'Alex Johnson', '2003-05-15', '079203001234', '0901234567', 'student@cfl.edu', TRUE, 0.00, 'Exempted', '2026-11-05', 'Registered'),
(2, 1, NULL, 'External', 'Emily Watson', '2002-08-20', '079202005678', '0912345678', 'emily.watson@example.com', FALSE, 220.00, 'Paid', '2026-11-15', 'Registered');

-- Exam Result (ResultID 1 is passed and eligible for certificate)
INSERT IGNORE INTO `ExamResult` (`ResultID`, `RegistrationID`, `AverageScore`, `TotalScore`, `Result`, `ResultStatus`, `ExamDate`, `GradedDate`, `GradedBy`, `Note`) VALUES
(1, 1, 7.50, 7.50, 'Pass', 'Pass', '2026-11-20', '2026-11-22', 1, 'Candidate scored high in Reading and Listening');

-- Exam Result Details
INSERT IGNORE INTO `ExamResultDetail` (`DetailID`, `ResultID`, `SkillTypeID`, `Score`) VALUES
(1, 1, 1, 8.00),
(2, 1, 2, 7.00),
(3, 1, 3, 8.00),
(4, 1, 4, 7.00);

-- Certificate
INSERT IGNORE INTO `Certificate` (`CertificateID`, `CertificateNumber`, `CertificateTypeID`, `StudentID`, `ExamResultID`, `IssueDate`, `ExpiryDate`, `Status`, `IssuedBy`) VALUES
(1, 'CFL-2026-IELTS-A8F21', 1, 1, 1, '2026-11-23', '2028-11-23', 'Valid', 1);
