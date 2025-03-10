package io.mosip.idrepository.vid.controller;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.idrepository.core.constant.IdRepoErrorConstants;
import io.mosip.idrepository.core.dto.VidRequestDTO;
import io.mosip.idrepository.core.dto.VidResponseDTO;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoDataValidationException;
import io.mosip.idrepository.core.logger.IdRepoLogger;
import io.mosip.idrepository.core.security.IdRepoSecurityManager;
import io.mosip.idrepository.core.spi.VidService;
import io.mosip.idrepository.core.util.DataValidationUtil;
import io.mosip.idrepository.vid.validator.VidRequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.logger.spi.Logger;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class Vid Controller - controller class for vid service.
 *
 * @author Manoj SP
 * @author Prem Kumar
 */
@RestController
public class VidController {

	private static final String REACTIVATE = "reactivate";

	private static final String DEACTIVATE = "deactivate";

	private static final String UIN = "uin";

	private static final String DEACTIVATE_VID = "deactivateVid";

	/** The Constant VID. */
	private static final String VID = "vid";

	/** The Constant REGENERATE. */
	private static final String REGENERATE = "regenerate";

	/** The Constant REGENERATE_VID. */
	private static final String REGENERATE_VID = "regenerateVid";

	/** The data source. */
	@Autowired
	DataSource dataSource;

	/** The Constant RETRIEVE_UIN_BY_VID. */
	private static final String RETRIEVE_UIN_BY_VID = "retrieveUinByVid";

	/** The Constant UPDATE_VID_STATUS. */
	private static final String UPDATE_VID_STATUS = "updateVidStatus";

	/** The Constant VID_CONTROLLER. */
	private static final String VID_CONTROLLER = "VidController";

	/** The Constant CREATE. */
	private static final String CREATE = "create";

	/** The Constant UPDATE. */
	private static final String UPDATE = "update";

	/** The Vid Service. */
	@Autowired
	private VidService<VidRequestDTO, ResponseWrapper<VidResponseDTO>> vidService;

	/** The Vid Request Validator. */
	@Autowired
	private VidRequestValidator validator;

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(VidController.class);

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	/**
	 * This service will generate a new VID based on VID type provided.
	 *
	 * @param request the request
	 * @param errors  the errors
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@PostMapping(path = "/vid", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> createVid(
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		try {
			validator.validateId(request.getId(), CREATE);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.generateVid(request.getRequest()), HttpStatus.OK);
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, RETRIEVE_UIN_BY_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e, CREATE);
		}
	}

	/**
	 * This method will accepts vid as parameter, if vid is valid it will return
	 * respective uin.
	 *
	 * @param vid the vid
	 * @return uin the uin
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR','ID_AUTHENTICATION')")
	@GetMapping(path = "/vid/{VID}", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> retrieveUinByVid(@PathVariable("VID") String vid)
			throws IdRepoAppException {
		try {
			validator.validateVid(vid);
			return new ResponseEntity<>(vidService.retrieveUinByVid(vid), HttpStatus.OK);
		} catch (InvalidIDException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, RETRIEVE_UIN_BY_VID, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), VID));
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, RETRIEVE_UIN_BY_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		}
	}

	/**
	 * This Method accepts VidRequest body as parameter and vid from url then it
	 * will update the status if it is an valid vid.
	 *
	 * @param vid     the vid
	 * @param request the request
	 * @param errors  the errors
	 * @return VidResponseDTO
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('ID_AUTHENTICATION')")
	@PatchMapping(path = "/vid/{VID}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> updateVidStatus(@PathVariable("VID") String vid,
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		try {
			validator.validateId(request.getId(), UPDATE);
			validator.validateVid(vid);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.updateVid(vid, request.getRequest()), HttpStatus.OK);
		} catch (InvalidIDException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, UPDATE_VID_STATUS, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), VID));
		} catch (IdRepoDataValidationException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, UPDATE_VID_STATUS, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.DATA_VALIDATION_FAILED, e);
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, UPDATE_VID_STATUS, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		}
	}

	/**
	 * This method will accepts an vid, if vid is valid to regenerate then
	 * regenerated vid will be returned as response.
	 *
	 * @param vid the vid
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	// TODO have to add roles when partner service is available
//	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@PostMapping(path = "/vid/{VID}/regenerate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> regenerateVid(@PathVariable("VID") String vid)
			throws IdRepoAppException {
		try {
			validator.validateVid(vid);
			return new ResponseEntity<>(vidService.regenerateVid(vid), HttpStatus.OK);
		} catch (InvalidIDException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, REGENERATE_VID, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), VID));
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, REGENERATE_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e, REGENERATE);
		}
	}

	/**
	 * This method will accept an uin, if uin is valid then it will deactivate all
	 * the respective vid's
	 * 
	 * @param request
	 * @param errors
	 * @return
	 * @throws IdRepoAppException
	 */
	@PostMapping(path = "/vid/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> deactivateVIDsForUIN(
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		try {
			validator.validateId(request.getId(), DEACTIVATE);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.deactivateVIDsForUIN(String.valueOf(request.getRequest().getUin())),
					HttpStatus.OK);
		} catch (InvalidIDException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		}

	}

	/**
	 * This method will accept an uin, if uin is valid then it will reactivate all
	 * the respective vid's
	 * 
	 * @param request
	 * @param errors
	 * @return
	 * @throws IdRepoAppException
	 */
	@PostMapping(path = "/vid/reactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> reactivateVIDsForUIN(
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		try {
			validator.validateId(request.getId(), REACTIVATE);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.reactivateVIDsForUIN(String.valueOf(request.getRequest().getUin())),
					HttpStatus.OK);
		} catch (InvalidIDException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		}

	}
}
