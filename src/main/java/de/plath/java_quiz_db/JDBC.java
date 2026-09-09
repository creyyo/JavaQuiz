package de.plath.java_quiz_db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.postgresql.util.PSQLException;

import de.plath.java_quiz.ChallengePayload;
import de.plath.java_quiz.JavaSyntaxHighlighter;
import de.plath.java_quiz.QuestionParser;

public class JDBC {

	private static String DIRECTORY = "QuestionsTXT";
	// private static String DIRECTORY = "versuchQuestionTXT";
	private static QuestionParser PARSER = new QuestionParser();
	private static Map<Integer, Character> INDEX_TO_ANSWER_LETTER = Map.ofEntries(//
			Map.entry(0, 'A'), Map.entry(1, 'B'), Map.entry(2, 'C'), Map.entry(3, 'D'), Map.entry(4, 'E'),
			Map.entry(5, 'F'), Map.entry(6, 'G'), Map.entry(7, 'H'));

	public static void main(String[] args) {
		recreateTables(true);
		readChallengeFromFiles();
	}

	public static void readChallengeFromFiles() {
		System.out.println("Parsing files");
		List<ChallengePayload> challengeList = PARSER.parseAllFiles(DIRECTORY);
		System.out.println("Parsed successfully");
		challengeList.forEach(challenge -> insertChallenge(challenge));
		System.out.println("Inserted challenges into database");
	}

	public static void recreateTables(Boolean recreateUserAnswers) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = DBConnection.getConnection();
			System.out.println("Opened database successfully");

			statement = connection.createStatement();
			String userAnswersStatementPrefix = "DROP TABLE IF EXISTS user_answers;\r\n";
			String questionAndAnswerStatement = "DROP TABLE IF EXISTS answers;\r\n"
					+ "DROP TABLE IF EXISTS questions;\r\n" + "\r\n" + "CREATE TABLE questions(\r\n"
					+ "   id INT GENERATED ALWAYS AS IDENTITY NOT NULL,\r\n" + "   question_text TEXT NOT NULL,\r\n"
					+ "   note TEXT NOT NULL,\r\n" + "   PRIMARY KEY(id)\r\n" + ");\r\n" + "\r\n"
					+ "CREATE TABLE answers(\r\n" + "   id INT GENERATED ALWAYS AS IDENTITY NOT NULL,\r\n"
					+ "   question_id INT NOT NULL,\r\n" + "   answer_letter CHAR NOT NULL,\r\n"
					+ "   answer_text TEXT NOT NULL,\r\n" + "   is_correct BOOLEAN NOT NULL,\r\n"
					+ "   PRIMARY KEY(id),\r\n" + "   CONSTRAINT fk_questions\r\n"
					+ "      FOREIGN KEY(question_id)\r\n" + "        REFERENCES questions(id)\r\n" + ");";
			String userAnswersStatementSuffix = "\r\n" + "CREATE TABLE user_answers(\r\n"
					+ "   id INT GENERATED ALWAYS AS IDENTITY NOT NULL,\r\n" + "   user_id VARCHAR(50) NOT NULL,\r\n"
					+ "   question_id INT NOT NULL,\r\n" + "   given_answers TEXT[] NOT NULL,\r\n"
					+ "   is_correct BOOLEAN NOT NULL,\r\n" + "   answered_at TIMESTAMP NOT NULL DEFAULT NOW(),\r\n"
					+ "   PRIMARY KEY(id),\r\n" + "   CONSTRAINT fk_questions\r\n"
					+ "      FOREIGN KEY(question_id)\r\n" + "        REFERENCES questions(id)\r\n" + ");";
			String sqlStatement = questionAndAnswerStatement;
			if (recreateUserAnswers) {
				sqlStatement = userAnswersStatementPrefix + questionAndAnswerStatement + userAnswersStatementSuffix;
			}
			statement.executeUpdate(sqlStatement);
			statement.close();
			connection.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Tables recreated successfully");
	}

	public static ChallengePayload getChallenge(int index) {
		String selectQuestion = "SELECT * FROM questions WHERE id = ?";
		String selectAnswers = "SELECT * FROM answers WHERE question_id = ?";

		try (Connection c = DBConnection.getConnection();
				PreparedStatement questionsStmt = c.prepareStatement(selectQuestion);
				PreparedStatement answersStmt = c.prepareStatement(selectAnswers);) {

			questionsStmt.setLong(1, index);
			ResultSet rsQuestions = questionsStmt.executeQuery();

			if (!rsQuestions.next()) {
				return null;
			}

			int id = rsQuestions.getInt("id");
			String questionText = rsQuestions.getString("question_text");
			questionText = JavaSyntaxHighlighter.highlight(questionText);
			String note = rsQuestions.getString("note");

			answersStmt.setLong(1, id);
			ResultSet rsAnswers = answersStmt.executeQuery();

			List<String> answers = new ArrayList<>();
			List<String> correctAnswers = new ArrayList<>();
			while (rsAnswers.next()) {
				String answerText = rsAnswers.getString("answer_text");
				answers.add(answerText);

				Boolean isCorrect = rsAnswers.getBoolean("is_correct");
				if (isCorrect) {
					String answerLetter = rsAnswers.getString("answer_letter");
					correctAnswers.add(answerLetter);
				}
			}
			ChallengePayload challenge = new ChallengePayload(id, questionText, answers, correctAnswers, note);
			return challenge;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ChallengePayload getRandomChallenge(Float seed) {
		String selectSql = "SELECT setseed(?)";
		String selectQuestion = "SELECT * FROM questions ORDER BY random() LIMIT 1";
		String selectAnswers = "SELECT * FROM answers WHERE question_id = ?";

		try (Connection c = DBConnection.getConnection();
				PreparedStatement pstmt = c.prepareStatement(selectSql);
				PreparedStatement questionsStmt = c.prepareStatement(selectQuestion);
				PreparedStatement answersStmt = c.prepareStatement(selectAnswers);) {

			if (seed != null) {
				pstmt.setFloat(1, seed);
				pstmt.executeQuery();
			}

			ResultSet rsQuestions = questionsStmt.executeQuery();

			if (!rsQuestions.next()) {
				return null;
			}

			int id = rsQuestions.getInt("id");
			String questionText = rsQuestions.getString("question_text");
			questionText = JavaSyntaxHighlighter.highlight(questionText);
			String note = rsQuestions.getString("note");

			answersStmt.setLong(1, id);
			ResultSet rsAnswers = answersStmt.executeQuery();

			List<String> answers = new ArrayList<>();
			List<String> correctAnswers = new ArrayList<>();
			while (rsAnswers.next()) {
				String answerText = rsAnswers.getString("answer_text");
				answers.add(answerText);

				Boolean isCorrect = rsAnswers.getBoolean("is_correct");
				if (isCorrect) {
					String answerLetter = rsAnswers.getString("answer_letter");
					correctAnswers.add(answerLetter);
				}
			}
			ChallengePayload challenge = new ChallengePayload(id, questionText, answers, correctAnswers, note);
			return challenge;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<ChallengePayload> searchKeyword(String keyword) {
		String selectQuestion = "SELECT * FROM questions WHERE question_text LIKE ? OR note LIKE ?";
		String selectAnswers = "SELECT * FROM answers WHERE question_id = ?";

		try (Connection c = DBConnection.getConnection();
				PreparedStatement questionsStmt = c.prepareStatement(selectQuestion);
				PreparedStatement answersStmt = c.prepareStatement(selectAnswers);) {

			String quotedKeyword = "%" + keyword + "%";
			questionsStmt.setString(1, quotedKeyword);
			questionsStmt.setString(2, quotedKeyword);
			ResultSet rsQuestions = questionsStmt.executeQuery();
			
			List<ChallengePayload> challenges = new ArrayList<>();
			while(rsQuestions.next()) {
				int id = rsQuestions.getInt("id");
				String questionText = rsQuestions.getString("question_text");
				questionText = JavaSyntaxHighlighter.highlight(questionText);
				String note = rsQuestions.getString("note");
				
				answersStmt.setLong(1, id);
				ResultSet rsAnswers = answersStmt.executeQuery();
				
				List<String> answers = new ArrayList<>();
				List<String> correctAnswers = new ArrayList<>();
				while (rsAnswers.next()) {
					String answerText = rsAnswers.getString("answer_text");
					answers.add(answerText);
					
					Boolean isCorrect = rsAnswers.getBoolean("is_correct");
					if (isCorrect) {
						String answerLetter = rsAnswers.getString("answer_letter");
						correctAnswers.add(answerLetter);
					}
				}
				ChallengePayload challenge = new ChallengePayload(id, questionText, answers, correctAnswers, note);
				challenges.add(challenge);
			}

			return challenges;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;	}

	public static void insertUserAnswer(String userId, int questionId, List<String> givenAnswers, boolean isCorrect) {
		String sql = "INSERT INTO USER_ANSWERS (USER_ID, QUESTION_ID, GIVEN_ANSWERS, IS_CORRECT) VALUES (?,?,?,?)";

		try (Connection c = DBConnection.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {

			pstmt.setString(1, userId);
			pstmt.setInt(2, questionId);

			Array sqlArray = c.createArrayOf("text", givenAnswers.toArray());
			pstmt.setArray(3, sqlArray);

			pstmt.setBoolean(4, isCorrect);

			pstmt.executeUpdate();
			System.out.println("Answer inserted for user " + userId);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertChallenge(ChallengePayload challenge) {
		String questionText = challenge.question();
		List<String> answers = challenge.answers();
		List<String> correctAnswers = challenge.correctAnswers();
		String note = challenge.note();

		String selectSql = "SELECT id FROM questions WHERE question_text = ? AND note = ?";
		String insertQuestionSql = "INSERT INTO questions(question_text, note) VALUES (?,?)";
		String insertAnswerSql = "INSERT INTO answers (question_id, answer_letter, answer_text, is_correct) VALUES (?,?,?,?)";

		try (Connection c = DBConnection.getConnection();
				PreparedStatement selectStmt = c.prepareStatement(selectSql);
				PreparedStatement qStmt = c.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement aStmt = c.prepareStatement(insertAnswerSql)) {

			selectStmt.setString(1, questionText);
			selectStmt.setString(2, note);
			ResultSet rs = selectStmt.executeQuery();
			if (rs.next()) {
				// TODO: "Question already exists" is too unspecific and skips questions (about
				// 30)
				System.out.println("Question already exists, skipping: " + questionText + "; " + note);
				return;
			}

			qStmt.setString(1, questionText);
			qStmt.setString(2, note);
			qStmt.executeUpdate();

			long questionId;
			try (ResultSet keys = qStmt.getGeneratedKeys()) {
				keys.next();
				questionId = keys.getLong(1);
			}

			IntStream.range(0, answers.size()).forEach(i -> {
				String answerText = answers.get(i);
				try {
					Character letter = INDEX_TO_ANSWER_LETTER.get(i);
					aStmt.setLong(1, questionId);
					aStmt.setString(2, String.valueOf(letter));
					aStmt.setString(3, answerText);
					aStmt.setBoolean(4, correctAnswers.contains(String.valueOf(letter)));
					aStmt.executeUpdate();
				} catch (PSQLException pe) {
//					pe.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
