import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class QuizGameUI extends JFrame {

    private List<String> quizQuestions;
    private List<String> correctAnswers;
    private int currentIndex;

    private JTextArea questionArea;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;

    private JButton prevButton;
    private JButton nextButton;
    private JButton submitButton;
    private JLabel resultLabel;
    private JLabel scoreLabel;

    private int[] userAnswers;
    private int totalQuestions;

    public QuizGameUI() {
        setTitle("Quiz Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        quizQuestions = new ArrayList<>();
        correctAnswers = new ArrayList<>();
        currentIndex = 0;

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Question area
        questionArea = new JTextArea(5, 30);
        questionArea.setEditable(false);
        questionArea.setWrapStyleWord(true);
        questionArea.setLineWrap(true);
        JScrollPane questionScrollPane = new JScrollPane(questionArea);
        mainPanel.add(questionScrollPane, BorderLayout.CENTER);

        // Options area
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
        }

        mainPanel.add(optionsPanel, BorderLayout.WEST);

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        submitButton = new JButton("Submit");
        submitButton.setVisible(false); // Initially not visible

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showQuestion(currentIndex - 1);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedOptionIndex = getSelectedOptionIndex();
                if (selectedOptionIndex != -1) {
                    userAnswers[currentIndex] = selectedOptionIndex; // Save user's answer

                    showQuestion(currentIndex + 1); // Move to the next question

                    // Show submit button when reaching the last question
                    if (currentIndex == totalQuestions - 1) {
                        nextButton.setEnabled(false); // Disable next button at last question
                        submitButton.setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select an option.");
                }
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calculate score and display result
                int score = calculateScore();
                displayScore(score);

                // Show thank-you message
                showThankYouMessage(score);
            }
        });

        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(submitButton);
        mainPanel.add(navPanel, BorderLayout.SOUTH);

        // Result and score display
        JPanel resultPanel = new JPanel(new GridLayout(2, 1));
        resultLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel = new JLabel("", SwingConstants.CENTER);
        resultPanel.add(resultLabel);
        resultPanel.add(scoreLabel);
        mainPanel.add(resultPanel, BorderLayout.NORTH);

        add(mainPanel);

        // Update with absolute file path or let the user provide it
        String filePath = askForFilePath();
        if (filePath == null) {
            JOptionPane.showMessageDialog(this, "No file selected. Exiting.");
            System.exit(1); // Exit the program if no file selected
        }

        if (!loadQuizFromFile(filePath)) {
            JOptionPane.showMessageDialog(this, "Failed to load quiz from file. Exiting.");
            System.exit(1); // Exit the program if file loading fails
        }

        // Initialize userAnswers array with -1 indicating no answer selected
        userAnswers = new int[quizQuestions.size()];
        totalQuestions = quizQuestions.size(); // Set total number of questions

        showQuestion(currentIndex);  // Display the first question initially
    }

    private String askForFilePath() {
        // Using JFileChooser to prompt user for file path
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Quiz File");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return null; // User canceled or closed the dialog
        }
    }

    private boolean loadQuizFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            StringBuilder currentQuestion = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    // Add the current question to quizQuestions
                    quizQuestions.add(currentQuestion.toString());
                    currentQuestion.setLength(0);
                } else if (line.startsWith("Correct Answer: ")) {
                    // Extract and store the correct answer
                    correctAnswers.add(line.substring(15).trim());
                } else {
                    // Accumulate lines to form a question
                    currentQuestion.append(line).append("\n");
                }
            }

            // Add the last question
            if (currentQuestion.length() > 0) {
                quizQuestions.add(currentQuestion.toString());
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showQuestion(int index) {
        if (index >= 0 && index < quizQuestions.size()) {
            currentIndex = index;

            String questionData = quizQuestions.get(currentIndex);
            String[] questionLines = questionData.split("\n");

            String question = questionLines[0];
            questionArea.setText(question);

            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText(questionLines[i + 1]);
                optionButtons[i].setSelected(false);
            }

            // Check if user has already answered this question
            if (userAnswers[currentIndex] != -1) {
                optionButtons[userAnswers[currentIndex]].setSelected(true);
            }

            resultLabel.setText("");
            scoreLabel.setText("Question " + (currentIndex + 1) + " of " + totalQuestions);

            prevButton.setEnabled(currentIndex > 0);
            nextButton.setEnabled(currentIndex < quizQuestions.size() - 1);

            // Hide submit button except at the last question
            submitButton.setVisible(currentIndex == totalQuestions - 1);
        }
    }

    private int getSelectedOptionIndex() {
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                return i;
            }
        }
        return -1; // No option selected
    }

    private int calculateScore() {
        int score = 0;
        for (int i = 0; i < totalQuestions; i++) {
            if (userAnswers[i] != -1) { // Check if user answered the question
                String selectedAnswer = optionButtons[userAnswers[i]].getText();
                String correctAnswer = correctAnswers.get(i);
                if (selectedAnswer.equalsIgnoreCase(correctAnswer)) {
                    score++;
                }
            }
        }
        return score;
    }

    private void displayScore(int score) {
        StringBuilder result = new StringBuilder("<html>");
        for (int i = 0; i < totalQuestions; i++) {
            int selectedAnswerIndex = userAnswers[i];
            if (selectedAnswerIndex != -1) { // Check if user answered the question
                String selectedAnswer = optionButtons[selectedAnswerIndex].getText();
                String correctAnswer = correctAnswers.get(i);
                boolean isCorrect = selectedAnswer.equalsIgnoreCase(correctAnswer);

                result.append("Question ").append(i + 1).append(": ");
                if (isCorrect) {
                    result.append("Correct<br>");
                } else {
                    result.append("Incorrect. Correct answer: ").append(correctAnswer).append("<br>");
                }
            } else {
                result.append("Question ").append(i + 1).append(": ");
                result.append("Not answered<br>");
            }
        }
        result.append("</html>");

        resultLabel.setText(result.toString());
        scoreLabel.setText("Score: " + score + "/" + totalQuestions);
    }

    private void showThankYouMessage(int score) {
        JOptionPane.showMessageDialog(this,
                "Thanks for playing the game!\nYour final score is: " + score + "/" + totalQuestions,
                "Quiz Game",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                QuizGameUI quizUI = new QuizGameUI();
                quizUI.setVisible(true);
            }
        });
    }
}
