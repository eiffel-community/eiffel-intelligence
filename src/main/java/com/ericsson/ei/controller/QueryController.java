
package com.ericsson.ei.controller;

import javax.validation.Valid;
import com.ericsson.ei.controller.model.QueryBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.0.10.11)
 * 
 */
@RestController
@RequestMapping(value = "/query", produces = "application/json")
public interface QueryController {


    /**
     * The REST POST method is used to query Aggregated Objects with the criterias requested, which are present in the request body of the request URL.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> updateQuery(
        @Valid
        @RequestBody
        QueryBody queryBody);

}
