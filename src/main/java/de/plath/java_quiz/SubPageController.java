package de.plath.java_quiz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubPageController {

	@GetMapping("/vielzugeheimalsdassdasirgendjemandherausfindenwuerde")
	public String index() {
		return "das crazy";
	}

}