package io.mosip.idrepository.saltgenerator.step.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant;
import io.mosip.authentication.saltgenerator.entity.SaltEntity;
import io.mosip.authentication.saltgenerator.step.SaltReader;

public class SaltReaderTest {

	SaltReader saltReader = new SaltReader();

	MockEnvironment mockEnv = new MockEnvironment();

	@Test
	public void testRead() {
		mockEnv.setProperty(IdAuthenticationSaltGeneratorConstant.START_SEQ.getValue(), String.valueOf(0));
		mockEnv.setProperty(IdAuthenticationSaltGeneratorConstant.END_SEQ.getValue(), String.valueOf(1));
		ReflectionTestUtils.setField(saltReader, "env", mockEnv);
		saltReader.initialize();
		SaltEntity saltEntity = saltReader.read();
		assertTrue(saltEntity.getId().equals(0l));
	}
	
	@Test
	public void testReadNull() {
		mockEnv.setProperty(IdAuthenticationSaltGeneratorConstant.START_SEQ.getValue(), String.valueOf(1));
		mockEnv.setProperty(IdAuthenticationSaltGeneratorConstant.END_SEQ.getValue(), String.valueOf(0));
		ReflectionTestUtils.setField(saltReader, "env", mockEnv);
		saltReader.initialize();
		assertNull(saltReader.read());
	}
}
