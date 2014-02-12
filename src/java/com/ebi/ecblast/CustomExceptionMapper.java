package com.ebi.ecblast;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ExceptionMapper;
import utility.APIResponse;


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
public class CustomExceptionMapper implements ExceptionMapper<ErrorInfo> {

    @Override
    public Response toResponse(ErrorInfo e) {

                 //ErrorInfo a= new ErrorInfo(e.getMessage(), e.getReason(), e.getStatus(), e.getErrorCode());
        APIResponse response = new APIResponse();
        response.setMessage(e.getMessage());
        response.setResponse(e.getReason());
        ResponseBuilder rb = Response.status(e.getStatus()).entity(
                response);

        return rb.build();
    }
}
