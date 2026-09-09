package de.plath.java_quiz;

import java.util.Map;

import org.springframework.stereotype.Component;

import de.plath.java_quiz_db.JDBC;

@Component
public class ChallengeStorage {

	private Map<Integer, ChallengePayload> challenges;

	public ChallengePayload getChallenge(int index) {
		return JDBC.getChallenge(index);
	}

	public int getChallengeCount() {
		return challenges.size();
	}

	public ChallengePayload getRandomChallenge(Float seed) {
		return JDBC.getRandomChallenge(seed);
	}
}
