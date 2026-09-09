package de.plath.java_quiz;

import java.util.List;

public record QuestionPayload(int index, String question, List<String> answers) {

}
