package io.mosip.kernel.masterdata.constant;

/**
 * Constants for RegistrationCenterDevice related errors.
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 *
 */
public enum RegistrationCenterDeviceErrorCode {

	REGISTRATION_CENTER_DEVICE_CREATE_EXCEPTION("KER-MSD-075",
			"Error occurred while inserting a mapping of Device and Center"),
	REGISTRATION_CENTER_DEVICE_DATA_NOT_FOUND("KER-MSD-115", "Mapping for Device and Center not found"),
	REGISTRATION_CENTER_DEVICE_DELETE_EXCEPTION("KER-MSD-105",
			"Error occurred while deleting a mapping of Device and Center"),
	REGISTRATION_CENTER_DEVICE_FETCH_EXCEPTION("KER-MSD-xx","Error occurred while fetching Center Device details");

	private final String errorCode;
	private final String errorMessage;

	private RegistrationCenterDeviceErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
