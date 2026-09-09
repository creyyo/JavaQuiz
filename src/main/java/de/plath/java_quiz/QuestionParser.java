package de.plath.java_quiz;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestionParser {

	private static boolean DEBUG = false;

	public List<ChallengePayload> parseAllFiles(String dir) {
		ArrayList<ChallengePayload> challengeList = new ArrayList<>();
		Set<String> listOfFiles = listFilesInDirectory(dir);
		int totalFileCount = listOfFiles.size();
		int currentIndex = 0;
		for (String fileName : listOfFiles) {
			// System.out.println("parsing file: " + fileName);
			String fileURI = dir + "/" + fileName;
			String fileContent = readFileContent(fileURI);
			Optional<ChallengePayload> challengeOptional = parseContent(currentIndex, fileName, fileContent);

			if (challengeOptional.isPresent()) {
				challengeList.add(challengeOptional.get());
				currentIndex += 1;
			}
		}
		int parsedChallengesCount = challengeList.size();
		System.out.println("Successfully parsed " + parsedChallengesCount + " Challenges of " + totalFileCount);

		return challengeList;
	}
	
	

	private String extractQuestion(String fileContent) {
		final Pattern pattern = Pattern.compile("Question(.+?)QuestionEnd", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			String question = matcher.group(1);
			return question;
		} else {
			return null;
		}
	}
	
	private List<String> extractAnswers(String fileContent) {
		String regex = "^[A-Ha-h]\\.\\s.*";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(fileContent);
		List<String> result = new ArrayList<>();
		while (matcher.find()) {
			String answer = matcher.group();
			result.add(answer);
		}
		return result;
	}

	private String extractCorrectAnswer1(String fileContent) {
		final Pattern pattern = Pattern.compile("Answer: (.+?)\n", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			String correctAnswer = matcher.group(1);
			return correctAnswer;
		} else {
			return null;
		}
	}

	private String extractCorrectAnswer2(String fileContent) {
		final Pattern pattern = Pattern.compile("Click to view the answer(.+?)", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			String correctAnswer = matcher.group(1);
			return correctAnswer;
		} else {
			return null;
		}
	}

	private String extractNote(String fileContent) {
		// System.out.println(fileContent);
		final Pattern pattern = Pattern.compile("Note(.+?)NoteEnd", Pattern.DOTALL); // |Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			String note = matcher.group(1);
			return note;
		} else {
			return null;
		}
	}

	private Optional<ChallengePayload> parseContent(int index, String fileName, String fileContent) {
		String question = extractQuestion(fileContent);
		if (question == null) {
			if (DEBUG)
				System.out.println(fileName + ": could not parse question");
			return Optional.empty();
		}
		List<String> answers = extractAnswers(fileContent);
		if (answers.isEmpty()) {
			if (DEBUG)
				System.out.println(fileName + ": could not parse answers");
			return Optional.empty();
		}
		
		String correctAnswer = extractCorrectAnswer1(fileContent);
		if (correctAnswer == null) {
			correctAnswer = extractCorrectAnswer2(fileContent);
		}
		if (correctAnswer == null) {
			if (DEBUG)
				System.out.println(fileName + ": could not parse correct answer");
			return Optional.empty();
		}
		
	    List<String> correctAnswers = Arrays.stream(correctAnswer.split(","))
	            .map(s -> s.replaceAll("[^A-H]", "").trim()) // keep only A–D letters
	            .filter(s -> !s.isBlank())
	            .collect(Collectors.toList());
	    
		String note = extractNote(fileContent);
		if (note == null) {
			if (DEBUG)
				System.out.println(fileName + ": could not parse note");
			return Optional.empty();
		}
		return Optional.of(new ChallengePayload(index, question, answers, correctAnswers, note));
	}

	private Set<String> listFilesInDirectory(String dir) {
		return Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory()).map(File::getName)
				.collect(Collectors.toSet());
	}

	private String readFileContent(String fileURI) {
		try {
			String text;
			text = new String(Files.readAllBytes(Paths.get(fileURI)), StandardCharsets.UTF_8);
			return text;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
