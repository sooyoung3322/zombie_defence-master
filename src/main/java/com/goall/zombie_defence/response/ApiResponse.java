package com.goall.zombie_defence.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiResponse {

	private String result;
	private HttpStatus status;
	private int code;

	private int listSize;

	private String message;

	private Object body;

	private int autoSeq;
	//오토인크리먼트값 가져오기

	public ApiResponse() {

		this(HttpStatus.OK);
	}

	public ApiResponse(HttpStatus status) {

		if (status == null) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		if(status.value() == 200){
			this.result = "true";
		}else{
			this.result = "false";
		}

		this.status = status;
		this.code = status.value();
	}

	public ApiResponse(HttpStatus status, Object body) {

		this(status);
		if(status.value() == 200){
			this.result = "true";
		}else{
			this.result = "false";
		}
		this.body = body;
	}

	public ApiResponse(HttpStatus status, int listSize, Object body) {

		this(status, body);
		this.listSize = listSize;
	}

}
