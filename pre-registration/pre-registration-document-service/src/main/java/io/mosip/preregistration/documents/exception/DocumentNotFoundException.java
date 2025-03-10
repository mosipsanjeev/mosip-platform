/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.documents.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.documents.errorcodes.ErrorCodes;
import lombok.Getter;

/**
 * This class defines the DocumentNotFoundException that occurs when document is
 * not found
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 * 
 */

@Getter
public class DocumentNotFoundException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7303748392658525834L;

	private MainResponseDTO<?> response;
	/**
	 * Default constructor
	 */
	public DocumentNotFoundException() {
		super();
	}

	/**
	 * @param errorCode
	 *            pass Error code
	 * @param message
	 *            pass Error Message
	 * @param cause
	 *            pass Error cause
	 */
	public DocumentNotFoundException(String errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

	/**
	 * @param errorCode
	 *            pass Error code
	 * @param message
	 *            pass Error Message
	 */
	public DocumentNotFoundException(String errorCode, String message) {
		super(errorCode, message);
	}
	
	/**
	 * 
	 * @param errorCode
	 *    	     pass Error code
	 * @param message
	 *           pass Error Message
	 * @param response
	 * 			 pass response
	 */
	public DocumentNotFoundException(String errorCode, String message,MainResponseDTO<?> response) {
		super(errorCode, message);
		this.response=response;
	}

}
