package io.mosip.registration.controller.device;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER;
import static io.mosip.registration.constants.LoggerConstants.LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.FXUtils;
import io.mosip.registration.controller.reg.RegistrationController;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.biometric.BiometricExceptionDTO;
import io.mosip.registration.dto.biometric.FaceDetailsDTO;
import io.mosip.registration.dto.biometric.FingerprintDetailsDTO;
import io.mosip.registration.dto.biometric.IrisDetailsDTO;
import io.mosip.registration.dto.mastersync.BiometricAttributeDto;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.bio.impl.BioServiceImpl;
import io.mosip.registration.service.security.AuthenticationService;
import io.mosip.registration.service.sync.MasterSyncService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * {@code GuardianBiometricscontroller} is to capture and display the captured
 * biometrics of Guardian
 * 
 * @author Sravya Surampalli
 * @since 1.0
 */
@Controller
public class GuardianBiometricsController extends BaseController implements Initializable {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(GuardianBiometricsController.class);

	@FXML
	private GridPane biometricBox;

	@FXML
	private GridPane retryBox;

	@FXML
	private ComboBox<BiometricAttributeDto> biometricTypecombo;

	@FXML
	private Label biometricType;

	@FXML
	private ImageView biometricImage;

	@FXML
	private Label qualityScore;

	@FXML
	private Label attemptSlap;

	@FXML
	private Label thresholdScoreLabel;

	@FXML
	private Label thresholdLabel;

	@FXML
	private GridPane biometricPane;

	@FXML
	private Button scanBtn;

	@FXML
	private ProgressBar bioProgress;

	@FXML
	private Label qualityText;

	@FXML
	private ColumnConstraints thresholdPane1;

	@FXML
	private ColumnConstraints thresholdPane2;

	@FXML
	private HBox bioRetryBox;

	@FXML
	private Button continueBtn;

	@FXML
	private Label duplicateCheckLbl;

	@FXML
	private GridPane thresholdBox;

	@FXML
	private Label photoAlert;

	@Autowired
	private Streamer streamer;

	@FXML
	private Label registrationNavlabel;

	/** The scan popup controller. */
	@Autowired
	private ScanPopUpViewController scanPopUpViewController;

	/** The registration controller. */
	@Autowired
	private RegistrationController registrationController;

	/** The face capture controller. */
	@Autowired
	private FaceCaptureController faceCaptureController;

	/** The finger print capture service impl. */
	@Autowired
	private AuthenticationService authenticationService;

	/** The iris facade. */
	@Autowired
	private BioServiceImpl bioService;

	@Autowired
	private Transliteration<String> transliteration;

	@Autowired
	private MasterSyncService masterSync;

	@Autowired
	private WebCameraController webCameraController;

	private String bioValue;

	private FXUtils fxUtils;
	
	private int leftSlapCount = 0;
	private int rightSlapCount = 0;
	private int thumbCount = 0;
	private int rightIrisCount = 0;
	private int leftIrisCount = 0;
	private int retries = 0;


	private List<String> leftSlapexceptionList = new ArrayList<String>();
	private List<String> rightSlapexceptionList = new ArrayList<String>();
	private List<String> thumbsexceptionList = new ArrayList<String>();

	public ImageView getBiometricImage() {
		return biometricImage;
	}

	public Button getScanBtn() {
		return scanBtn;
	}

	public Button getContinueBtn() {
		return continueBtn;
	}

	public Label getPhotoAlert() {
		return photoAlert;
	}

	public GridPane getBiometricPane() {
		return biometricPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 * java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Loading of Guardian Biometric screen started");

		if (getRegistrationDTOFromSession() != null && getRegistrationDTOFromSession().getSelectionListDTO() != null) {
			registrationNavlabel.setText(ApplicationContext.applicationLanguageBundle()
					.getString(RegistrationConstants.UIN_UPDATE_UINUPDATENAVLBL));
		}

		if (getRegistrationDTOFromSession() != null
				&& getRegistrationDTOFromSession().getRegistrationMetaDataDTO().getRegistrationCategory() != null
				&& getRegistrationDTOFromSession().getRegistrationMetaDataDTO().getRegistrationCategory()
						.equals(RegistrationConstants.PACKET_TYPE_LOST)) {
			registrationNavlabel.setText(
					ApplicationContext.applicationLanguageBundle().getString(RegistrationConstants.LOSTUINLBL));
		}
		intializeCaptureCount();
		fxUtils = FXUtils.getInstance();
		fxUtils.setTransliteration(transliteration);
		bioValue = RegistrationUIConstants.SELECT;
		biometricBox.setVisible(false);
		retryBox.setVisible(false);
		continueBtn.setDisable(true);
		populateBiometrics();
		renderBiometrics();

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Loading of Guardian Biometric screen ended");

		biometricTypecombo.valueProperty().addListener(new ChangeListener<BiometricAttributeDto>() {

			@Override
			public void changed(ObservableValue<? extends BiometricAttributeDto> arg0,
					BiometricAttributeDto previousValue, BiometricAttributeDto currentValue) {
				if (null != previousValue && null != currentValue
						&& !previousValue.getName().equalsIgnoreCase(currentValue.getName())) {
					continueBtn.setDisable(true);
				}

			}
		});
	}

	public void intializeCaptureCount() {
		leftSlapCount = 0;
		rightSlapCount = 0;
	    thumbCount = 0;
		rightIrisCount = 0;
		leftIrisCount = 0;
		retries = 0;
	}
	
	/**
	 * Displays biometrics
	 *
	 * @param event the event for displaying biometrics
	 */
	@FXML
	private void displayBiometric(ActionEvent event) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Displaying biometrics to capture");

		if (null != biometricTypecombo.getValue()) {
			if (biometricTypecombo.getValue().getName().equalsIgnoreCase(RegistrationUIConstants.RIGHT_SLAP)) {
				updateBiometric(biometricTypecombo.getValue().getName(), RegistrationConstants.RIGHTPALM_IMG_PATH,
						RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD,
						RegistrationConstants.FINGERPRINT_RETRIES_COUNT);
			} else if (biometricTypecombo.getValue().getName().equalsIgnoreCase(RegistrationUIConstants.LEFT_SLAP)) {
				updateBiometric(biometricTypecombo.getValue().getName(), RegistrationConstants.LEFTPALM_IMG_PATH,
						RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD,
						RegistrationConstants.FINGERPRINT_RETRIES_COUNT);
			} else if (biometricTypecombo.getValue().getName().equalsIgnoreCase(RegistrationUIConstants.THUMBS)) {
				updateBiometric(biometricTypecombo.getValue().getName(), RegistrationConstants.THUMB_IMG_PATH,
						RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD,
						RegistrationConstants.FINGERPRINT_RETRIES_COUNT);
			} else if (biometricTypecombo.getValue().getName().equalsIgnoreCase(RegistrationUIConstants.RIGHT_IRIS)) {
				updateBiometric(biometricTypecombo.getValue().getName(), RegistrationConstants.RIGHT_IRIS_IMG_PATH,
						RegistrationConstants.IRIS_THRESHOLD, RegistrationConstants.IRIS_RETRY_COUNT);
			} else if (biometricTypecombo.getValue().getName().equalsIgnoreCase(RegistrationUIConstants.LEFT_IRIS)) {
				updateBiometric(biometricTypecombo.getValue().getName(), RegistrationConstants.LEFT_IRIS_IMG_PATH,
						RegistrationConstants.IRIS_THRESHOLD, RegistrationConstants.IRIS_RETRY_COUNT);
			}
		}

		if (!bioValue.equalsIgnoreCase(RegistrationUIConstants.SELECT)) {
			scanBtn.setDisable(false);
			continueBtn.setDisable(true);
			biometricBox.setVisible(true);
			retryBox.setVisible(true);
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Parent/Guardian Biometrics captured");
	}

	/**
	 * Scan the biometrics
	 *
	 * @param event the event for scanning biometrics
	 */
	@FXML
	private void scan(ActionEvent event) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Displaying Scan popup for capturing biometrics");

		if (scanBtn.getText().equalsIgnoreCase(RegistrationUIConstants.TAKE_PHOTO)) {
			faceCaptureController.openWebCamWindow(RegistrationConstants.GUARDIAN_IMAGE);
		} else {
			String headerText = "";
			String bioType = "";
			if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.RIGHT_SLAP)) {
				SessionContext.map().put("CAPTURE_EXCEPTION", rightSlapexceptionList);
				headerText = RegistrationUIConstants.FINGERPRINT;
				bioType = RegistrationConstants.FINGERPRINT_SLAB_RIGHT;
			} else if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.LEFT_SLAP)) {
				SessionContext.map().put("CAPTURE_EXCEPTION", leftSlapexceptionList);
				headerText = RegistrationUIConstants.FINGERPRINT;
				bioType = RegistrationConstants.FINGERPRINT_SLAB_LEFT;
			} else if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.THUMBS)) {
				SessionContext.map().put("CAPTURE_EXCEPTION", thumbsexceptionList);
				headerText = RegistrationUIConstants.FINGERPRINT;
				bioType = RegistrationConstants.FINGERPRINT_SLAB_THUMBS;
			} else if (biometricType.getText().contains(RegistrationConstants.IRIS_LOWERCASE)) {
				headerText = RegistrationUIConstants.IRIS_SCAN;
				bioType = "IRIS_DOUBLE";
			}
			scanPopUpViewController.init(this, headerText);
			if (bioService.isMdmEnabled())
				streamer.startStream(bioType, scanPopUpViewController.getScanImage(), biometricImage);
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scan popup closed and captured biometrics");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.controller.BaseController#scan(javafx.stage.Stage)
	 */
	@Override
	public void scan(Stage popupStage) {
		try {

			LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Scan process started for capturing biometrics");

			if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.RIGHT_SLAP)) {
				scanFingers(RegistrationConstants.FINGERPRINT_SLAB_RIGHT,
						RegistrationConstants.RIGHTHAND_SEGMNTD_DUPLICATE_FILE_PATHS, popupStage, Double.parseDouble(
								getValueFromApplicationContext(RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.LEFT_SLAP)) {
				scanFingers(RegistrationConstants.FINGERPRINT_SLAB_LEFT,
						RegistrationConstants.LEFTHAND_SEGMNTD_FILE_PATHS, popupStage, Double.parseDouble(
								getValueFromApplicationContext(RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.THUMBS)) {
				scanFingers(RegistrationConstants.FINGERPRINT_SLAB_THUMBS,
						RegistrationConstants.THUMBS_SEGMNTD_FILE_PATHS, popupStage, Double.parseDouble(
								getValueFromApplicationContext(RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.RIGHT_IRIS)) {
				scanIris(RegistrationConstants.RIGHT.concat(RegistrationConstants.EYE), popupStage,
						Double.parseDouble(getValueFromApplicationContext(RegistrationConstants.IRIS_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase(RegistrationUIConstants.LEFT_IRIS)) {
				scanIris(RegistrationConstants.LEFT.concat(RegistrationConstants.EYE), popupStage,
						Double.parseDouble(getValueFromApplicationContext(RegistrationConstants.IRIS_THRESHOLD)));
			}

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"Exception while getting the scanned biometrics for user registration: %s caused by %s",
							runtimeException.getMessage(),
							runtimeException.getCause() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.BIOMETRIC_SCANNING_ERROR);
		} catch (RegBaseCheckedException regBaseCheckedException) {
			LOGGER.error(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"Exception while getting the scanned biometrics for user registration: %s caused by %s",
					regBaseCheckedException.getMessage(),
					regBaseCheckedException.getCause() + ExceptionUtils.getStackTrace(regBaseCheckedException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.BIOMETRIC_SCANNING_ERROR);
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scan process ended for capturing biometrics");
	}

	/**
	 * Navigating to previous section
	 *
	 * @param event the event for navigating to previous section
	 */
	@FXML
	private void previous(ActionEvent event) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Navigates to previous section");

		webCameraController.closeWebcam();

		if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {
			SessionContext.map().put(RegistrationConstants.UIN_UPDATE_PARENTGUARDIAN_DETAILS, false);

			if ((boolean) SessionContext.getInstance().getUserContext().getUserMap()
					.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION)) {
				SessionContext.map().put(RegistrationConstants.UIN_UPDATE_BIOMETRICEXCEPTION, true);
			} else {
				SessionContext.map().put(RegistrationConstants.UIN_UPDATE_DOCUMENTSCAN, true);
			}
			registrationController.showUINUpdateCurrentPage();

		} else {
			registrationController.showCurrentPage(RegistrationConstants.GUARDIAN_BIOMETRIC,
					getPageDetails(RegistrationConstants.GUARDIAN_BIOMETRIC, RegistrationConstants.PREVIOUS));
		}
	}

	/**
	 * Navigating to next section
	 *
	 * @param event the event for navigating to next section
	 */
	@FXML
	private void next(ActionEvent event) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Navigates to next section");

		webCameraController.closeWebcam();

		if (isChild()) {
			SessionContext.map().put(RegistrationConstants.UIN_UPDATE_PARENTGUARDIAN_DETAILS, false);
			if (!RegistrationConstants.DISABLE
					.equalsIgnoreCase(getValueFromApplicationContext(RegistrationConstants.FACE_DISABLE_FLAG))) {

				SessionContext.getInstance().getMapObject().put(RegistrationConstants.UIN_UPDATE_FACECAPTURE, true);
			} else {
				SessionContext.getInstance().getMapObject().put(RegistrationConstants.UIN_UPDATE_REGISTRATIONPREVIEW,
						true);
				registrationPreviewController.setUpPreviewContent();
			}
			registrationController.showUINUpdateCurrentPage();
		} else {
			registrationController.showCurrentPage(RegistrationConstants.GUARDIAN_BIOMETRIC,
					getPageDetails(RegistrationConstants.GUARDIAN_BIOMETRIC, RegistrationConstants.NEXT));
		}
	}

	/**
	 * Updating biometrics
	 *
	 * @param bioType            biometric type
	 * @param bioImage           biometric image
	 * @param biometricThreshold threshold of biometric
	 * @param retryCount         retry count
	 */
	private void updateBiometric(String bioType, String bioImage, String biometricThreshold, String retryCount) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updating biometrics and clearing previous data");
		clearCaptureData();
		biometricPane.getStyleClass().clear();
		biometricPane.getStyleClass().add(RegistrationConstants.BIOMETRIC_PANES_SELECTED);
		biometricImage.setImage(new Image(this.getClass().getResourceAsStream(bioImage)));
		biometricType.setText(bioType);
		if (!bioType.equalsIgnoreCase(RegistrationUIConstants.PHOTO)) {
			bioValue = bioType;
			thresholdScoreLabel
					.setText(getQualityScore(Double.parseDouble(getValueFromApplicationContext(biometricThreshold))));
			createQualityBox(retryCount, biometricThreshold);
			qualityScore.setText(RegistrationConstants.HYPHEN);
			attemptSlap.setText(RegistrationConstants.HYPHEN);
			duplicateCheckLbl.setText(RegistrationConstants.EMPTY);
		}
		getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO().getFingerprintDetailsDTO()
				.clear();
		getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO().getIrisDetailsDTO().clear();

		getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO().setFace(new FaceDetailsDTO());
		intializeCaptureCount();

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updated biometrics and cleared previous data");
	}

	/**
	 * Scan Iris
	 * 
	 * @param irisType       iris type
	 * @param popupStage     stage
	 * @param thresholdValue threshold value
	 *
	 */
	private void scanIris(String irisType, Stage popupStage, Double thresholdValue) throws RegBaseCheckedException {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, "Scanning Iris");

		IrisDetailsDTO detailsDTO = null;

		List<IrisDetailsDTO> irisDetailsDTOs = getRegistrationDTOFromSession().getBiometricDTO()
				.getIntroducerBiometricDTO().getIrisDetailsDTO();
		try {
			IrisDetailsDTO tempIrisDetail = irisDetailsDTOs.stream()
					.filter((iris) -> iris.getIrisType().contains(irisType)).findFirst().get();
			irisDetailsDTOs.remove(tempIrisDetail);
		} catch (Exception exception) {
		}

		try {
			detailsDTO = new IrisDetailsDTO();
			bioService.getIrisImageAsDTO(detailsDTO, irisType);
		} catch (RegBaseCheckedException | IOException runtimeException) {
			LOGGER.error(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"%s Exception while getting the scanned iris details for user registration: %s caused by %s",
					RegistrationConstants.USER_REG_IRIS_SAVE_EXP, runtimeException.getMessage(),
					ExceptionUtils.getStackTrace(runtimeException)));
			irisDetailsDTOs.remove(detailsDTO);
			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.NO_DEVICE_FOUND);
			return;
		}
		if (detailsDTO.isCaptured()) {
			detailsDTO.getIrises().forEach((iris) -> {
				if(iris.getIrisType().equals(RegistrationConstants.RIGHT.concat(RegistrationConstants.EYE))) {
					leftIrisCount++;
					retries=leftIrisCount;
				}else {
					rightIrisCount++;
					retries=rightIrisCount;
				}
				scanPopUpViewController.getScanImage().setImage(convertBytesToImage(iris.getIris()));
				biometricImage.setImage(convertBytesToImage(iris.getIris()));
				generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.IRIS_SUCCESS_MSG);
				setCapturedValues(iris.getQualityScore(), retries, thresholdValue);
				iris.setNumOfIrisRetry(retries);

				if (validateIrisQulaity(iris, thresholdValue)) {
					if(retries==Integer
								.parseInt(getValueFromApplicationContext(RegistrationConstants.IRIS_RETRY_COUNT)))
						scanBtn.setDisable(true);
					continueBtn.setDisable(false);
				} else {
					scanBtn.setDisable(false);
					continueBtn.setDisable(true);
				}
				irisDetailsDTOs.add(iris);
			});
			popupStage.close();
		} else {
			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.IRIS_SCANNING_ERROR);
		}
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Iris scanning is completed");

	}


	/**
	 * Scan Fingers
	 * 
	 * @param fingerType           finger type
	 * @param segmentedFingersPath segmented finger path
	 * @param popupStage           stage
	 * @param thresholdValue       threshold value
	 */
	private void scanFingers(String fingerType, String[] segmentedFingersPath, Stage popupStage, Double thresholdValue)
			throws RegBaseCheckedException {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scanning Fingerprints started");

		FingerprintDetailsDTO detailsDTO = null;

		List<FingerprintDetailsDTO> fingerprintDetailsDTOs = getRegistrationDTOFromSession().getBiometricDTO()
				.getIntroducerBiometricDTO().getFingerprintDetailsDTO();

		if (fingerprintDetailsDTOs == null || fingerprintDetailsDTOs.isEmpty()) {
			fingerprintDetailsDTOs = new ArrayList<>(1);
			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					.setFingerprintDetailsDTO(fingerprintDetailsDTOs);
		}

		if (fingerprintDetailsDTOs != null) {

			for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
				if (fingerprintDetailsDTO.getFingerType().equals(fingerType)) {
					detailsDTO = fingerprintDetailsDTO;

					for (String segmentedFingerPath : segmentedFingersPath) {
						String[] path = segmentedFingerPath.split("/");
						for (FingerprintDetailsDTO segmentedfpDetailsDTO : fingerprintDetailsDTO
								.getSegmentedFingerprints()) {
							if (segmentedfpDetailsDTO.getFingerType().equals(path[3])) {
								fingerprintDetailsDTO.getSegmentedFingerprints().remove(segmentedfpDetailsDTO);
								break;
							}
						}
					}
					detailsDTO.setNumRetry(fingerprintDetailsDTO.getNumRetry() + 1);
					break;
				}
			}
			if (detailsDTO == null) {
				detailsDTO = new FingerprintDetailsDTO();
				detailsDTO.setNumRetry(detailsDTO.getNumRetry() + 1);
				fingerprintDetailsDTOs.add(detailsDTO);
			}
		}

		try {
			bioService.getFingerPrintImageAsDTO(detailsDTO, fingerType);
			streamer.stop();
			bioService.segmentFingerPrintImage(detailsDTO, segmentedFingersPath, fingerType);
		} catch (Exception exception) {
			streamer.stop();
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format("%s Exception while getting the scanned finger details for user registration: %s ",
							exception.getMessage(), ExceptionUtils.getStackTrace(exception)));
			fingerprintDetailsDTOs.remove(detailsDTO);
			return;
		}

		if (detailsDTO.isCaptured()) {
			if (fingerType.equals(RegistrationConstants.FINGERPRINT_SLAB_RIGHT)) {
				leftSlapCount++;
				retries = leftSlapCount;
			} else if (fingerType.equals(RegistrationConstants.FINGERPRINT_SLAB_LEFT)) {
				rightSlapCount++;
				retries = rightSlapCount;
			} else {
				thumbCount++;
				retries = thumbCount;
			}
			detailsDTO.setNumRetry(retries);
			if (!bioService.isMdmEnabled())
				scanPopUpViewController.getScanImage().setImage(convertBytesToImage(detailsDTO.getFingerPrint()));
			else
				detailsDTO.setFingerPrint(streamer.imageBytes);
			biometricImage.setImage(convertBytesToImage(detailsDTO.getFingerPrint()));
			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.FP_CAPTURE_SUCCESS);

			setCapturedValues(detailsDTO.getQualityScore(), detailsDTO.getNumRetry(), thresholdValue);

			popupStage.close();

			if (validateFingerPrintQulaity(detailsDTO, thresholdValue)
					&& fingerdeduplicationCheck(fingerprintDetailsDTOs)) {
				continueBtn.setDisable(false);
				if (retries == Integer
						.parseInt(getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_RETRIES_COUNT))) {
					scanBtn.setDisable(true);
				}
			}

		} else {
			fingerprintDetailsDTOs.remove(detailsDTO);
			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.FP_DEVICE_ERROR);
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Fingerprints Scanning is completed");

	}

	/**
	 * Updating captured values
	 * 
	 * @param capturedBio    biometric
	 * @param qltyScore      Qulaity score
	 * @param retry          retrycount
	 * @param thresholdValue threshold value
	 */
	private void setCapturedValues(double qltyScore, int retry, double thresholdValue) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updating captured values of biometrics");

		biometricPane.getStyleClass().clear();
		biometricPane.getStyleClass().add(RegistrationConstants.FINGERPRINT_PANES_SELECTED);
		qualityScore.setText(getQualityScore(qltyScore));
		attemptSlap.setText(String.valueOf(retry));

		bioProgress.setProgress(
				Double.valueOf(getQualityScore(qltyScore).split(RegistrationConstants.PERCENTAGE)[0]) / 100);
		qualityText.setText(getQualityScore(qltyScore));
		if (Double.valueOf(getQualityScore(qltyScore).split(RegistrationConstants.PERCENTAGE)[0]) >= thresholdValue) {
			clearAttemptsBox(RegistrationConstants.QUALITY_LABEL_GREEN, retry);
			bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_RED);
			bioProgress.getStyleClass().add(RegistrationConstants.PROGRESS_BAR_GREEN);
			qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_RED);
			qualityText.getStyleClass().add(RegistrationConstants.LABEL_GREEN);
		} else {
			clearAttemptsBox(RegistrationConstants.QUALITY_LABEL_RED, retry);
			bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_GREEN);
			bioProgress.getStyleClass().add(RegistrationConstants.PROGRESS_BAR_RED);
			qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_GREEN);
			qualityText.getStyleClass().add(RegistrationConstants.LABEL_RED);
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updated captured values of biometrics");
	}

	/**
	 * Updating captured values
	 * 
	 * @param retryCount         retry count
	 * @param biometricThreshold threshold value
	 */
	private void createQualityBox(String retryCount, String biometricThreshold) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updating Quality and threshold values of biometrics");

		if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			for (int retry = 0; retry < Integer.parseInt(getValueFromApplicationContext(retryCount)); retry++) {
				Label label = new Label();
				label.getStyleClass().add(RegistrationConstants.QUALITY_LABEL_GREY);
				label.setId(RegistrationConstants.RETRY_ATTEMPT_ID + (retry + 1));
				label.setVisible(true);
				label.setText(String.valueOf(retry + 1));
				label.setAlignment(Pos.CENTER);
				bioRetryBox.getChildren().add(label);
			}

			String threshold = getValueFromApplicationContext(biometricThreshold);

			thresholdLabel.setAlignment(Pos.CENTER);
			thresholdLabel.setText(RegistrationUIConstants.THRESHOLD.concat("  ").concat(threshold)
					.concat(RegistrationConstants.PERCENTAGE));
			thresholdPane1.setPercentWidth(Double.parseDouble(threshold));
			thresholdPane2.setPercentWidth(100.00 - (Double.parseDouble(threshold)));
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updated Quality and threshold values of biometrics");

	}

	/**
	 * Clear attempts box.
	 *
	 * @param styleClass the style class
	 * @param retries    the retries
	 */
	private void clearAttemptsBox(String styleClass, int retries) {
		bioRetryBox.lookup(RegistrationConstants.RETRY_ATTEMPT + retries).getStyleClass().clear();
		bioRetryBox.lookup(RegistrationConstants.RETRY_ATTEMPT + retries).getStyleClass().add(styleClass);
	}

	/**
	 * Clear captured data
	 *
	 */
	private void clearCaptureData() {
		bioProgress.setProgress(0);
		bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_RED);
		bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_GREEN);

		qualityText.setText(RegistrationConstants.EMPTY);
		qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_RED);
		qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_GREEN);

		bioRetryBox.getChildren().clear();
	}

	/**
	 * Validates QualityScore.
	 *
	 * @param fingerprintDetailsDTO the fingerprint details DTO
	 * @param handThreshold         the hand threshold
	 * @return boolean
	 */
	private Boolean validateFingerPrintQulaity(FingerprintDetailsDTO fingerprintDetailsDTO, Double handThreshold) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Validating the quality score of the captured fingers");

		return fingerprintDetailsDTO.getQualityScore() >= handThreshold
				|| (fingerprintDetailsDTO.getQualityScore() < handThreshold)
						&& fingerprintDetailsDTO.getNumRetry() == Integer.parseInt(
								getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_RETRIES_COUNT))
				|| fingerprintDetailsDTO.isForceCaptured();
	}

	/**
	 * Fingerdeduplication check.
	 *
	 * @param fingerprintDetailsDTOs the fingerprint details DT os
	 * @return true, if successful
	 */
	private boolean fingerdeduplicationCheck(List<FingerprintDetailsDTO> fingerprintDetailsDTOs) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Validating the dedupecheck of the captured fingers");

		List<FingerprintDetailsDTO> segmentedFingerprintDetailsDTOs = new ArrayList<>();

		boolean isValid = false;

		for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
			for (FingerprintDetailsDTO segmentedFingerprintDetailsDTO : fingerprintDetailsDTO
					.getSegmentedFingerprints()) {
				segmentedFingerprintDetailsDTOs.add(segmentedFingerprintDetailsDTO);
			}
		}
		if (!validateFingerprint(segmentedFingerprintDetailsDTOs)) {
			isValid = true;
		} else {
			FingerprintDetailsDTO duplicateFinger = (FingerprintDetailsDTO) SessionContext.map()
					.get(RegistrationConstants.DUPLICATE_FINGER);

			Iterator<FingerprintDetailsDTO> iterator = fingerprintDetailsDTOs.iterator();

			while (iterator.hasNext()) {
				FingerprintDetailsDTO value = iterator.next();
				for (FingerprintDetailsDTO duplicate : value.getSegmentedFingerprints()) {
					if (duplicate.getFingerType().equals(duplicateFinger.getFingerType())) {
						iterator.remove();
						break;
					}
				}
			}
			String finger;
			if (duplicateFinger.getFingerType().contains(RegistrationConstants.LEFT.toLowerCase())) {
				finger = duplicateFinger.getFingerType().replace(RegistrationConstants.LEFT.toLowerCase(),
						RegistrationConstants.LEFT_HAND);
			} else {
				finger = duplicateFinger.getFingerType().replace(RegistrationConstants.RIGHT.toLowerCase(),
						RegistrationConstants.RIGHT_HAND);
			}
			duplicateCheckLbl.setText(finger + " " + RegistrationUIConstants.FINGERPRINT_DUPLICATION_ALERT);
		}

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Validated the dedupecheck of the captured fingers");

		return isValid;
	}

	/**
	 * Validates QualityScore.
	 *
	 * @param irisDetailsDTO the iris details DTO
	 * @param irisThreshold  the iris threshold
	 * @return boolean
	 */
	private boolean validateIrisQulaity(IrisDetailsDTO irisDetailsDTO, Double irisThreshold) {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Validating the quality score of the captured iris");

		return irisDetailsDTO.getQualityScore() >= irisThreshold
				|| (Double.compare(irisDetailsDTO.getQualityScore(), irisThreshold) < 0
						&& irisDetailsDTO.getNumOfIrisRetry() == Integer
								.parseInt(getValueFromApplicationContext(RegistrationConstants.IRIS_RETRY_COUNT))
						|| irisDetailsDTO.isForceCaptured());
	}

	/**
	 * Clear Biometric data
	 */
	public void clearCapturedBioData() {

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Clearing the captured biometric data");

		if (getRegistrationDTOFromSession() != null && (getRegistrationDTOFromSession().isUpdateUINChild()
				|| (SessionContext.map().get(RegistrationConstants.IS_Child) != null
						&& (Boolean) SessionContext.map().get(RegistrationConstants.IS_Child)))) {
			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					.setFingerprintDetailsDTO(new ArrayList<>());

			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					.setIrisDetailsDTO(new ArrayList<>());
		}
		duplicateCheckLbl.setText(RegistrationConstants.EMPTY);
		clearCaptureData();
		biometricBox.setVisible(false);
		retryBox.setVisible(false);
		bioValue = RegistrationUIConstants.SELECT;

		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Cleared the captured biometric data");

	}

	/**
	 * Exception fingers count.
	 */
	private Map<String, Integer> exceptionFingersCount(int leftSlapCount, int rightSlapCount, int thumbCount,
			int irisCount) {
		leftSlapexceptionList.clear();
		rightSlapexceptionList.clear();
		thumbsexceptionList.clear();
		Map<String, Integer> exceptionCountMap = new HashMap<>();
		List<BiometricExceptionDTO> biometricExceptionDTOs;
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			biometricExceptionDTOs = getBiometricDTOFromSession().getOperatorBiometricDTO().getBiometricExceptionDTO();
		} else if (getRegistrationDTOFromSession().isUpdateUINNonBiometric()
				|| (boolean) SessionContext.map().get(RegistrationConstants.IS_Child)) {
			biometricExceptionDTOs = getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					.getBiometricExceptionDTO();
		} else {
			biometricExceptionDTOs = getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
					.getBiometricExceptionDTO();
		}
		for (BiometricExceptionDTO biometricExceptionDTO : biometricExceptionDTOs) {

			if ((biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.LEFT.toLowerCase())
					&& biometricExceptionDTO.isMarkedAsException())
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.THUMB)
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.EYE)) {
				getExceptionIdentifier(leftSlapexceptionList, biometricExceptionDTO.getMissingBiometric());
				leftSlapCount++;
			}
			if ((biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.RIGHT.toLowerCase())
					&& biometricExceptionDTO.isMarkedAsException())
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.THUMB)
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.EYE)) {
				getExceptionIdentifier(rightSlapexceptionList, biometricExceptionDTO.getMissingBiometric());
				rightSlapCount++;
			}
			if ((biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.THUMB)
					&& biometricExceptionDTO.isMarkedAsException())) {
				getExceptionIdentifier(thumbsexceptionList, biometricExceptionDTO.getMissingBiometric());
				thumbCount++;
			}
			if ((biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.EYE)
					&& biometricExceptionDTO.isMarkedAsException())) {
				irisCount++;
			}
		}
		exceptionCountMap.put(RegistrationConstants.LEFTSLAPCOUNT, leftSlapCount);
		exceptionCountMap.put(RegistrationConstants.RIGHTSLAPCOUNT, rightSlapCount);
		exceptionCountMap.put(RegistrationConstants.THUMBCOUNT, thumbCount);
		exceptionCountMap.put(RegistrationConstants.EXCEPTIONCOUNT,
				leftSlapCount + rightSlapCount + thumbCount + irisCount);

		return exceptionCountMap;
	}

	/**
	 * Manage biometrics list based on exceptions.
	 */
	public void manageBiometricsListBasedOnExceptions() {

		if (getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
				.getBiometricExceptionDTO() != null
				|| !getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
						.getBiometricExceptionDTO().isEmpty()) {
			int leftSlapCount = 0;
			int rightSlapCount = 0;
			int thumbCount = 0;
			int irisCount = 0;
			biometricTypecombo.getItems().clear();
			populateBiometrics();

			Map<String, Integer> exceptionCount = exceptionFingersCount(leftSlapCount, rightSlapCount, thumbCount,
					irisCount);
			int excepCount = exceptionCount.get(RegistrationConstants.EXCEPTIONCOUNT);

			if ((RegistrationConstants.DISABLE.equalsIgnoreCase(
					getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_DISABLE_FLAG)) && excepCount == 2)
					|| (RegistrationConstants.DISABLE
							.equalsIgnoreCase(getValueFromApplicationContext(RegistrationConstants.IRIS_DISABLE_FLAG))
							&& excepCount == 10)
					|| excepCount == 12) {
				bioValue = RegistrationUIConstants.SELECT;
				biometricBox.setVisible(true);
				bioProgress.setProgress(1);
				biometricTypecombo.setVisible(false);
				thresholdBox.setVisible(false);
				scanBtn.setText(RegistrationUIConstants.TAKE_PHOTO);
				duplicateCheckLbl.setText(RegistrationConstants.EMPTY);
				retryBox.setVisible(false);
				if (getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO().getFace()
						.getFace() == null) {
					scanBtn.setDisable(false);
					continueBtn.setDisable(true);
					updateBiometric(RegistrationUIConstants.PHOTO, RegistrationConstants.IMAGE_PATH, "",
							String.valueOf(RegistrationConstants.PARAM_ZERO));
				} else {
					continueBtn.setDisable(false);
				}
			} else {
				biometricTypecombo.setVisible(true);
				if (bioValue.equalsIgnoreCase(RegistrationUIConstants.SELECT)) {
					biometricBox.setVisible(false);
				} else {
					biometricBox.setVisible(true);
				}
				thresholdBox.setVisible(true);
				scanBtn.setText(RegistrationUIConstants.SCAN);
				duplicateCheckLbl.setText(RegistrationConstants.EMPTY);
				photoAlert.setVisible(false);
				if (exceptionCount.get(RegistrationConstants.LEFTSLAPCOUNT) == 4) {
					modifyBiometricType(RegistrationUIConstants.LEFT_SLAP);
				}
				if (exceptionCount.get(RegistrationConstants.RIGHTSLAPCOUNT) == 4) {
					modifyBiometricType(RegistrationUIConstants.RIGHT_SLAP);
				}
				if (exceptionCount.get(RegistrationConstants.THUMBCOUNT) == 2) {
					modifyBiometricType(RegistrationUIConstants.THUMBS);
				}
				if (anyIrisException(RegistrationConstants.LEFT)) {
					modifyBiometricType(RegistrationUIConstants.LEFT_IRIS);
				}
				if (anyIrisException(RegistrationConstants.RIGHT)) {
					modifyBiometricType(RegistrationUIConstants.RIGHT_IRIS);
				}
				List<String> bioList = new ArrayList<>();
				biometricTypecombo.getItems().forEach(bio -> bioList.add(bio.getName()));
				if (!bioList.contains(bioValue)) {
					bioValue = RegistrationUIConstants.SELECT;
					clearCapturedBioData();
				}
			}

		}
		biometricTypecombo.getSelectionModel().clearSelection();
		biometricTypecombo.setPromptText(bioValue);
		if (bioProgress.getProgress() != 0) {
			if (!scanBtn.getText().equalsIgnoreCase(RegistrationUIConstants.TAKE_PHOTO)) {
				scanBtn.setDisable(true);
			}
			continueBtn.setDisable(false);
		} else {
			continueBtn.setDisable(true);
		}
	}

	/**
	 * Modifying the combobox details
	 */
	private void modifyBiometricType(String biometricType) {
		List<BiometricAttributeDto> biometricAttributeDtos = biometricTypecombo.getItems();
		List<BiometricAttributeDto> bio = new ArrayList<>();
		bio.addAll(biometricAttributeDtos);
		for (BiometricAttributeDto biometricAttributeDto2 : biometricAttributeDtos) {
			if (biometricAttributeDto2.getName().equalsIgnoreCase(biometricType)) {
				bio.remove(biometricAttributeDto2);
				break;
			}

		}
		biometricTypecombo.getItems().clear();
		biometricTypecombo.getItems().addAll(bio);
	}

	/**
	 * Fetching the combobox details
	 */
	@SuppressWarnings("unchecked")
	private <T> void renderBiometrics() {
		LOGGER.info("REGISTRATION - INDIVIDUAL_REGISTRATION - RENDER_COMBOBOXES", RegistrationConstants.APPLICATION_ID,
				RegistrationConstants.APPLICATION_NAME, "Rendering of comboboxes started");

		try {
			StringConverter<T> uiRenderForComboBox = fxUtils.getStringConverterForComboBox();

			biometricTypecombo.setConverter((StringConverter<BiometricAttributeDto>) uiRenderForComboBox);
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.REGISTRATION_CONTROLLER,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException), runtimeException);
		}
		LOGGER.info("REGISTRATION - INDIVIDUAL_REGISTRATION - RENDER_COMBOBOXES", RegistrationConstants.APPLICATION_ID,
				RegistrationConstants.APPLICATION_NAME, "Rendering of comboboxes ended");
	}

	private void populateBiometrics() {
		try {
			biometricTypecombo.getItems().addAll(masterSync.getBiometricType(ApplicationContext.applicationLanguage()));
		} catch (RegBaseCheckedException regBaseCheckedException) {
			LOGGER.error("REGISTRATION - LOADING_BIOMETRICS - RENDER_COMBOBOXES", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					regBaseCheckedException.getMessage() + ExceptionUtils.getStackTrace(regBaseCheckedException));
		}
	}

	private boolean validateFingerprint(List<FingerprintDetailsDTO> fingerprintDetailsDTOs) {
		AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
		authenticationValidatorDTO.setUserId(SessionContext.userContext().getUserId());
		authenticationValidatorDTO.setFingerPrintDetails(fingerprintDetailsDTOs);
		authenticationValidatorDTO.setAuthValidationType("multiple");
		return authenticationService.authValidator("Fingerprint", authenticationValidatorDTO);
	}

}
