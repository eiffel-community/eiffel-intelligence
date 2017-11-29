
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Provides interaction with JMESPath resource
 * 
 */
@RestController
@RequestMapping(value = "/jmespath", produces = "application/string")
public interface RulesController {

	/**
	 * Returns elements by taking in a rule and expression for JMESPath 
	 * 
	 */ 
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<?> runRule(@RequestParam("arg1") String arg1, @RequestBody String arg2);

}


