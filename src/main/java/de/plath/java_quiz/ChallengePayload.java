package de.plath.java_quiz;

import java.util.List;



public record ChallengePayload(Integer index, String question, List<String> answers, List<String> correctAnswers, String note) {	
}




