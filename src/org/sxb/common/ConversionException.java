/*
Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package org.sxb.common;

/**
 * 类型转换用异常抛出类
 * @author Jeffson
 */
public class ConversionException extends RuntimeException{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3684045640530009888L;

	public ConversionException() {
        super();
    }

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
