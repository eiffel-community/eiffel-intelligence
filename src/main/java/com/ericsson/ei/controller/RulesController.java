
package com.ericsson.ei.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Provides interaction with resource
 * (Generated with springmvc-raml-parser v.0.8.6)
 * 
 */
@RestController
@RequestMapping(value = "/jmespath", produces = "application/string")
public interface RulesController {


    /**
     * Test rules
     */
	
//   @RequestMapping(value = "", method = RequestMethod.GET)
//	public String runRule(@RequestParam("arg1") String arg1);  
   
    
//   @RequestMapping(value = "", method = RequestMethod.GET)
//   public String runRule(@RequestParam("arg1") String arg1);
//    
   @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> runRule(@RequestParam("arg1") String arg1, @RequestBody String arg2);
   
//   @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
//   public String runRule(@RequestParam("arg1") String arg1, @RequestBody String arg2);

}


