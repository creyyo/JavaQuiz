package de.plath.java_quiz;

import java.util.List;

public class ChallengeConverter {
	
	public static QuestionPayload convertToQuestionPayload(ChallengePayload challenge) {
		int index = challenge.index();
		String question = challenge.question();
		List<String> answers = challenge.answers();
		return new QuestionPayload(index, question, answers);
	}
	
}
