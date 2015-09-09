/*
Copyright 2009-2014 Igor Polevoy

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
package org.sxb.web.exception;

/**
 * @author Igor Polevoy
 */
public class WebException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 537568442067065164L;

	public WebException() {
        super();    
    }

    public WebException(String message) {
        super(message);    
    }

    public WebException(String message, Throwable cause) {
        super(message, cause);    
    }

    public WebException(Throwable cause) {
        super(cause);    
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if(getCause() != null){
            message += "; " + getCause().getMessage(); 
        }
        return message;
    }
}
