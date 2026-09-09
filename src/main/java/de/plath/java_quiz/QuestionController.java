package de.plath.java_quiz;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.plath.java_quiz_db.JDBC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class QuestionController {

	@Autowired
	private ChallengeStorage storage;

	@GetMapping("/challenges/{index}")
	public Mono<ChallengePayload> getChallenge(@PathVariable("index") Integer index) {
		ChallengePayload question = storage.getChallenge(index);
		return Mono.just(question);
	}

	@GetMapping("/questions/{index}")
	public Mono<QuestionPayload> getQuestion(@PathVariable("index") Integer index) {
		ChallengePayload challenge = storage.getChallenge(index);
		QuestionPayload question = ChallengeConverter.convertToQuestionPayload(challenge);
		return Mono.just(question);
	}

	@GetMapping({ "/questions/random", "/questions/random/{seed}" })
	public Mono<QuestionPayload> getRandomQuestion(@PathVariable(value = "seed", required = false) Float seed) {
		System.out.println("Endpoint has been called");
		ChallengePayload randomChallenge = storage.getRandomChallenge(seed);
		QuestionPayload randomQuestion = ChallengeConverter.convertToQuestionPayload(randomChallenge);
		return Mono.just(randomQuestion);
	}

//	@GetMapping("/questions/{index}/{answer}")
//	public Mono<SolutionPayload> getSolution(@PathVariable("index") Integer index,
//			@PathVariable("answer") String answer) {
//		ChallengePayload challenge = storage.getChallenge(index);
//		boolean isAnswerCorrect = challenge.correctAnswers().stream().anyMatch(ca -> ca.equalsIgnoreCase(answer));
//		String note = null;
//		if (isAnswerCorrect) {
//			note = challenge.note();
//		}
//		SolutionPayload solution = new SolutionPayload(isAnswerCorrect, note);
//		return Mono.just(solution);
//	}
	
	@GetMapping("/challenges")
	public Flux<ChallengePayload> searchKeyword(@RequestParam String keyword) { 
		List<ChallengePayload> challenges = JDBC.searchKeyword(keyword);
	    return Flux.fromIterable(challenges);
	}
	
	@GetMapping("/questions/{index}/answer")
	public Mono<AnswerPayload> getAnswer(@PathVariable("index") Integer index) {
		ChallengePayload challenge = storage.getChallenge(index);
		return Mono.just(new AnswerPayload(challenge.correctAnswers(), challenge.note()));
	}

	@PostMapping("/questions/{index}/check")
	public Mono<SolutionPayload> checkAnswers(@PathVariable("index") Integer index,
			@RequestBody AnswerSubmission submission) {
		ChallengePayload challenge = storage.getChallenge(index);

		// Correct if the sets match exactly (ignores order and duplicates)
		boolean isAnswerCorrect = new HashSet<>(challenge.correctAnswers())
				.equals(new HashSet<>(submission.getAnswers()));
		
		String note = isAnswerCorrect ? challenge.note() : null;
		
		JDBC.insertUserAnswer("Me", index, submission.getAnswers(), isAnswerCorrect);
		
		return Mono.just(new SolutionPayload(isAnswerCorrect, note));
	}
}

//@GetMapping("/questions")
//public String index() {
//	return "???";
//}