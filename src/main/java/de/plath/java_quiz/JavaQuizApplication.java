package de.plath.java_quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*
 * QuestionParser as Util
 * all Questions become Challenges 
 * clear "\r" from Challenges
 * clear "." from Answer
 * clear  "Java OCA OCP Practice Question 2" from documents 
 * inspect documents that dont match the pattern 
 * 
 */
@SpringBootApplication
public class JavaQuizApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaQuizApplication.class, args);
	}
}