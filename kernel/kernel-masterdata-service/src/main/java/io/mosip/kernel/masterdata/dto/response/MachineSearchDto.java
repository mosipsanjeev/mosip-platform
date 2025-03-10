package io.mosip.kernel.masterdata.dto.response;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.dto.getresponse.extn.BaseDto;
import io.mosip.kernel.masterdata.validator.FilterType;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Machine", description = "Machine Detail resource")
public class MachineSearchDto extends BaseDto {
	/**
	 * Field for machine id
	 */
	@FilterType(types = { FilterTypeEnum.EQUALS })
	@NotBlank
	@Size(min = 1, max = 10)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;
	/**
	 * Field for machine name
	 */
	@FilterType(types = { FilterTypeEnum.EQUALS, FilterTypeEnum.STARTSWITH, FilterTypeEnum.CONTAINS })
	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;
	/**
	 * Field for machine serial number
	 */
	@FilterType(types = { FilterTypeEnum.EQUALS, FilterTypeEnum.STARTSWITH, FilterTypeEnum.CONTAINS })
	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "serialNum", required = true, dataType = "java.lang.String")
	private String serialNum;
	/**
	 * Field for machine mac address
	 */
	@FilterType(types = { FilterTypeEnum.EQUALS, FilterTypeEnum.STARTSWITH, FilterTypeEnum.CONTAINS })
	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "macAddress", required = true, dataType = "java.lang.String")
	private String macAddress;
	/**
	 * Field for machine IP address
	 */

	@Size(min = 1, max = 17)
	@ApiModelProperty(value = "ipAddress", required = true, dataType = "java.lang.String")
	private String ipAddress;
	/**
	 * Field for machine specification Id
	 */
	@FilterType(types = { FilterTypeEnum.EQUALS, FilterTypeEnum.STARTSWITH, FilterTypeEnum.CONTAINS })
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "machineSpecId", required = true, dataType = "java.lang.String")
	private String machineSpecId;
	/**
	 * Field for language code
	 */
	@FilterType(types = { FilterTypeEnum.EQUALS })
	@ValidLangCode
	@NotBlank
	@Size(min = 1, max = 3)
	@ApiModelProperty(value = "langCode", required = true, dataType = "java.lang.String")
	private String langCode;

	/**
	 * Field for is validity of the Device
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime validityDateTime;

	/**
	 * Zone Code for Machine.
	 */
	private String zoneCode;

	/**
	 * Zone name for Machine.
	 */
	private String zone;

	/**
	 * Machine Type name of Machine.
	 */
	private String machineTypeName;

	/**
	 * Machine center mapping status.
	 */
	private String mapStatus;

}
