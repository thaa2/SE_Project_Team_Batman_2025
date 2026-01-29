# GUI Dashboards - Test Management System

## Overview
The Test Management System now features professional, user-friendly Swing-based GUI dashboards for both Students and Educators. The dashboards have replaced the console-based menu system with an intuitive graphical interface.

## Architecture

### Components

#### 1. StudentDashboard (Swing GUI)
**Location:** `src/student/StudentDashboard.java`

A comprehensive dashboard for students with the following features:

**Panels/Sections:**
- **Dashboard Tab**: Welcome screen with statistics and quick action cards
  - Quizzes Attempted counter
  - Average Score display
  - Courses Enrolled counter
  - Quick Action buttons: Attempt Quiz, View Results, Browse Courses

- **My Results Tab**: View quiz attempt history
  - Table showing all quiz scores
  - Displays: Quiz Name, Score, Total Questions, Percentage, Date
  - Sortable and scrollable results

- **Attempt Quiz Tab**: Select and start new quizzes
  - General Quiz option
  - Course-specific quiz option
  - Easy navigation to quiz modules

- **Courses Tab**: Browse and enroll in courses
  - Display of all available courses
  - Course description and enrollment button
  - Real-time course list from database

- **Profile Tab**: View and manage account information
  - Name, Email, Age, Gender
  - Birth Date and User ID
  - All information retrieved from database

**Key Features:**
- Modern card-based UI design
- Gradient header with personalized welcome message
- Responsive sidebar navigation
- Color-coded statistics
- Database integration for real-time data
- Smooth panel transitions

**Design Elements:**
- Primary Color: #4361EE (Blue)
- Secondary Color: #673BB7 (Purple)
- Accent Color: #4CAF50 (Green)
- Font: Segoe UI
- Rounded corners on all cards

---

#### 2. EducatorDashboard (Swing GUI)
**Location:** `src/educator/EducatorDashboard.java`

A feature-rich dashboard for educators with comprehensive course and question management.

**Panels/Sections:**
- **Dashboard Tab**: Overview and quick statistics
  - Total Questions created
  - My Courses count
  - Students Taught count
  - Quizzes Created count
  - Quick action cards for common tasks

- **Add Questions Tab**: Create quiz questions
  - Three question type options:
    1. **MCQ (Multiple Choice Questions)**
       - Support for 3-5 options
       - Visual dialog for question entry
       - Correct answer selection
    
    2. **True/False Questions**
       - Simple dialog interface
       - Binary answer selection
    
    3. **Short Answer Questions**
       - Text-based correct answer input
       - Flexible answer matching

  - Dialog windows for seamless question creation
  - Option to associate questions with specific courses
  - Success confirmation messages

- **Manage Courses Tab**: Create and manage courses
  - List of all educator's courses in table format
  - "Create New Course" button
  - Course details: ID, Name, Lesson Content
  - Edit action buttons for each course
  - Database-backed course management

- **Student Results Tab**: View performance analytics
  - Comprehensive results table
  - Shows: Student Name, Score, Total Questions, Percentage, Attempt Date
  - Real-time data from QuizScores database
  - Sortable columns

- **Analytics Tab**: View statistics and insights
  - Average Student Score
  - Total Quiz Attempts
  - Highest Score achieved
  - Lowest Score in class
  - Visual statistics cards

- **Profile Tab**: View educator account details
  - Name, Email, Age, Gender
  - Birth Date and User ID
  - Professional profile display

**Key Features:**
- Advanced course management
- Multiple question type support
- Real-time student analytics
- Dialog-based question creation
- Database integration for persistence
- Comprehensive statistics dashboard
- Course-question association

**Design Elements:**
- Modern professional look
- Gradient navigation
- Card-based layouts
- Dialog windows for complex operations
- Tables for data display
- Color-coded statistics

---

## QuizController Integration

**File:** `src/quiz/QuizController.java`

The QuizController has been updated to launch the appropriate GUI dashboard based on user role:

```java
public static void runQuizModule(Scanner sc, User user) {
    if (user.getRole() == Role.STUDENT) {
        Student student = (Student) user;
        new StudentDashboard(student);
    } else if (user.getRole() == Role.EDUCATOR) {
        Educator educator = (Educator) user;
        new EducatorDashboard(educator);
    }
}
```

This replaces the previous console-based menu system with the new GUI dashboards.

---

## Database Integration

Both dashboards are fully integrated with the SQLite database:

### Tables Used:
- **Courses**: For course management and storage
- **Questions**: For quiz question storage
- **QuizScores**: For student results and analytics
- **user**: For user profile information

### Key Database Operations:
1. **StudentDashboard**:
   - Retrieve enrolled courses
   - Fetch student quiz results
   - Display course content

2. **EducatorDashboard**:
   - Save new questions to database
   - Create and manage courses
   - Load student results for analytics
   - Retrieve educator statistics

---

## User Classes Enhancements

**File:** `src/auth/User.java`

Added getter methods for protected fields:
- `getEmail()`
- `getAge()`
- `getGender()`
- `getBirthDate()`

This allows dashboard components to access user information securely.

---

## Features

### Student Dashboard Features
✅ Dashboard with statistics and quick actions
✅ Quiz attempt history with detailed results
✅ Browse and enroll in courses
✅ View enrolled course content
✅ Comprehensive profile view
✅ Real-time data from database
✅ User-friendly navigation

### Educator Dashboard Features
✅ Dashboard with teaching statistics
✅ Create MCQ, True/False, and Short Answer questions
✅ Manage courses (create, edit, view)
✅ View all student results and performance
✅ Real-time analytics dashboard
✅ Question type selection with dialogs
✅ Course-question association
✅ Professional statistics display

---

## Design Patterns Used

1. **CardLayout**: For panel switching between different sections
2. **GridLayout**: For arranging statistics and action cards
3. **BoxLayout**: For vertical and horizontal arrangement
4. **MVC**: Model (Database) - View (GUI) - Controller (Listeners)
5. **Dialog Windows**: For complex operations (question creation, course creation)

---

## Color Scheme

| Element | Color | Hex Code |
|---------|-------|----------|
| Primary Header | Blue to Purple Gradient | #4361EE - #673BB7 |
| Text | Dark Gray | #3C3C3C |
| Cards | White | #FFFFFF |
| Background | Light Gray | #F0F2F5 |
| Success | Green | #4CAF50 |
| Warning | Orange | #FF9800 |
| Error | Red | #DC3545 |

---

## Running the Application

```bash
# Compile
javac -cp "lib/*" -d bin src/**/*.java

# Run
java -cp "lib/*;bin" main.Main
```

Login with a student or educator account to access the respective dashboards.

---

## Future Enhancements

Potential improvements for the dashboards:

1. **StudentDashboard**:
   - Quiz timer during attempts
   - Performance analytics charts
   - Course progress tracking
   - Discussion forum integration
   - Notification system

2. **EducatorDashboard**:
   - Question bank/library
   - Bulk question import
   - Advanced analytics with graphs
   - Quiz scheduling
   - Student messaging system
   - Question difficulty levels
   - Quiz template creation

---

## Troubleshooting

### Dashboard not displaying?
- Ensure Java version supports Swing (Java 8+)
- Check database connection in DataStore.connect()
- Verify all compiled classes are in bin/ folder

### Data not loading?
- Check database file (School.db) exists
- Verify educator_id or student_id is correct
- Check browser console for SQL errors

### Buttons not responding?
- Ensure ActionListeners are properly connected
- Check for null references in panel creation
- Verify cardLayout.show() calls are correct

---

## Class Diagram

```
StudentDashboard extends JFrame
├── StudentDashboard(Student)
├── initializeUI()
├── createHeaderPanel()
├── createSidebarPanel()
├── createDashboardPanel()
├── createResultsPanel()
├── createAttemptPanel()
├── createCoursesPanel()
├── createProfilePanel()
└── Helper methods...

EducatorDashboard extends JFrame
├── EducatorDashboard(Educator)
├── initializeUI()
├── createHeaderPanel()
├── createSidebarPanel()
├── createDashboardPanel()
├── createAddQuestionsPanel()
├── createCoursesPanel()
├── createStudentResultsPanel()
├── createAnalyticsPanel()
├── createProfilePanel()
├── Dialog methods (MCQ, TrueFalse, ShortAnswer)
└── Database helper methods...
```

---

## Version History

### v1.0 (Current)
- Initial GUI dashboard implementation
- Student and Educator dashboards
- Database integration
- Question creation for educators
- Course management
- Result viewing and analytics

---

## Contact & Support

For issues or questions about the GUI dashboards, refer to the main documentation or contact the development team.
