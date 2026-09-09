package de.plath.java_quiz;

import java.util.List;
import org.junit.jupiter.api.Test;

class QuestionParserTest {

	private QuestionParser parser = new QuestionParser();

	@Test
	public void parseAllFiles() {
		String dir = "QuestionsTXT";
		List<ChallengePayload> allFiles = parser.parseAllFiles(dir);
		System.out.println(allFiles);
	}

}
