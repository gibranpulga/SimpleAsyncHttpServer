package com.gibran.server;

/**
 * 
 * Response data encapsulated so I can return the HTTP response code and the content
 * 
 * @author Gibran
 *
 */
public class ResponseData {

	private final Integer statusCode;
	private final String content;
	
	public ResponseData (Integer statusCode, String content) {
		this.statusCode = statusCode;
		this.content = content;
	}
	
	public Integer getStatusCode() {
		return statusCode;
	}
	
	public String getContent() {
		return content;
	}
	
	
}
