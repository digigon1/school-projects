package io.swagger.api;

import io.swagger.model.Company;
import io.swagger.model.Contact;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-11-03T17:12:18.187Z")

@Controller
public class CompaniesApiController implements CompaniesApi {

    private static final Logger log = LoggerFactory.getLogger(CompaniesApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public CompaniesApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<List<Company>> companiesGet(@ApiParam(value = "Substring to search for", defaultValue = "") @Valid @RequestParam(value = "search", required = false, defaultValue="") String search) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<Company>>(objectMapper.readValue("[ {  \"name\" : \"name\",  \"id\" : 0,  \"contacts\" : [ {    \"phone\" : \"phone\",    \"name\" : \"name\",    \"id\" : 6,    \"email\" : \"email\"  }, {    \"phone\" : \"phone\",    \"name\" : \"name\",    \"id\" : 6,    \"email\" : \"email\"  } ]}, {  \"name\" : \"name\",  \"id\" : 0,  \"contacts\" : [ {    \"phone\" : \"phone\",    \"name\" : \"name\",    \"id\" : 6,    \"email\" : \"email\"  }, {    \"phone\" : \"phone\",    \"name\" : \"name\",    \"id\" : 6,    \"email\" : \"email\"  } ]} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<Company>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<Company>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<Contact>> companiesIdContactsGet(@ApiParam(value = "Company id to list contacts",required=true) @PathVariable("id") Long id,@ApiParam(value = "Substring to search for", defaultValue = "") @Valid @RequestParam(value = "search", required = false, defaultValue="") String search) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<Contact>>(objectMapper.readValue("[ {  \"phone\" : \"phone\",  \"name\" : \"name\",  \"id\" : 6,  \"email\" : \"email\"}, {  \"phone\" : \"phone\",  \"name\" : \"name\",  \"id\" : 6,  \"email\" : \"email\"} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<Contact>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<Contact>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> companiesIdContactsPost(@ApiParam(value = "Company id to create contact",required=true) @PathVariable("id") Long id,@ApiParam(value = "Contact object that needs to be added" ,required=true )  @Valid @RequestBody Contact body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> companiesIdDelete(@ApiParam(value = "Company id to delete",required=true) @PathVariable("id") Long id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Company> companiesIdGet(@ApiParam(value = "ID of company to return",required=true) @PathVariable("id") Long id) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Company>(objectMapper.readValue("{  \"name\" : \"name\",  \"id\" : 0,  \"contacts\" : [ {    \"phone\" : \"phone\",    \"name\" : \"name\",    \"id\" : 6,    \"email\" : \"email\"  }, {    \"phone\" : \"phone\",    \"name\" : \"name\",    \"id\" : 6,    \"email\" : \"email\"  } ]}", Company.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<Company>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Company>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> companiesIdPut(@ApiParam(value = "ID of company that needs to be updated",required=true) @PathVariable("id") Long id,@ApiParam(value = "Updated name of the company") @RequestParam(value="name", required=false)  String name) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> companiesPost(@ApiParam(value = "Company object that needs to be added" ,required=true )  @Valid @RequestBody Company body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

}
