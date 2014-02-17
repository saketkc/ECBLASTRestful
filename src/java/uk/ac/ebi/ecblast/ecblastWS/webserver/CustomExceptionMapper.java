package uk.ac.ebi.ecblast.ecblastWS.webserver;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ExceptionMapper;
import uk.ac.ebi.ecblast.ecblastWS.utility.APIResponse;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author saket
 */
@Provider
public class CustomExceptionMapper implements ExceptionMapper<ErrorResponse> {

    @Override
    public Response toResponse(ErrorResponse e) {

                 //ErrorInfo a= new ErrorResponse(e.getMessage(), e.getReason(), e.getStatus(), e.getErrorCode());
        APIResponse response = new APIResponse();
        response.setMessage(e.getMessage());
        response.setResponse(e.getReason());
        ResponseBuilder rb = Response.status(e.getStatus()).entity(
                response);

        return rb.build();
    }
}
