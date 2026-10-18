package com.goall.commonUtil;

import java.util.*;
import java.io.*;
import java.text.*;
import java.time.LocalDate;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.*;
import javax.crypto.*;
import java.security.*;
import java.security.spec.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommonUtil {
	
	public static String getRemoteIp(HttpServletRequest req) {

		String ip = req.getHeader("HTTP_X_FORWARDED_FOR");

		if (ip == null) ip = req.getRemoteAddr();

		return ip;

	}
}