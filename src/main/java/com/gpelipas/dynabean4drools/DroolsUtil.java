/**
 * 
 */
package com.gpelipas.dynabean4drools;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [description]
 *
 * @author gpelipas
 *
 */
public class DroolsUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsUtil.class);

	private DroolsUtil() {
	}

	/**
	 * 
	 * 
	 * @param drlStrs
	 * @return
	 * @throws Exception
	 */
	public static KieContainer createDroolsContainer(String... drlStrs) throws Exception {
		KieServices ks = KieServices.Factory.get();
		return createDroolsContainer(ks, drlStrs);
	}

	/**
	 * Create Drools Container
	 * 
	 * @param ks
	 * @param drlStrs
	 * @return
	 * @throws Exception
	 */
	public static KieContainer createDroolsContainer(KieServices ks, String... drlStrs) throws Exception {
		try {

			if (ks == null) {
				throw new Exception("KieServices must not be empty");
			}

			if (drlStrs == null || drlStrs.length == 0) {
				throw new Exception("DRL scripts must not be empty");
			}

			KieFileSystem kfs = ks.newKieFileSystem();

			String runId = String.valueOf(System.nanoTime());

			int i = 0;
			Resource resource = null;
			for (String drlStr : drlStrs) {
				resource = ks.getResources().newByteArrayResource(drlStr.getBytes()).setResourceType(ResourceType.DRL);
				resource.setSourcePath("com.pelipas.test.drools.run" + runId + "_" + i++ + ".drl");
				kfs.write(resource);
			}

			KieBuilder kb = ks.newKieBuilder(kfs).buildAll();

			Results results = kb.getResults();
			if (results != null && results.hasMessages(Message.Level.ERROR)) {
				throw new Exception("Error occured while preparing Drools Resource - " + results.getMessages());
			}

			return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

		} catch (Exception e) {
			LOGGER.error("Error while creating Drools Container", e);

			throw e;
		}
	}

	/**
	 * Create new Stateless KieSession
	 * 
	 * @param drlStr
	 * @return
	 * @throws Exception
	 */
	public static StatelessKieSession createStatelessKieSession(String drlStr) throws Exception {
		final KieContainer kc = createDroolsContainer(drlStr);
		return kc.newStatelessKieSession();
	}

	/**
	 * Create new Statefull KieSession
	 * 
	 * @param drlStr
	 * @return
	 * @throws Exception
	 */
	public static KieSession createStatefullKieSession(String drlStr) throws Exception {
		final KieContainer kc = createDroolsContainer(drlStr);
		return kc.newKieSession();
	}

	/**
	 * Close KieSession
	 * 
	 * @param ks
	 */
	public static void closeStatefullKieSession(KieSession ks) {
		try {
			if (ks != null) {
				ks.dispose();
			}
		} catch (Exception e) {
			LOGGER.warn("Error while closing KieSession");
		}
	}

	/**
	 * Returns FactType initially loaded in KieContainer
	 * 
	 * @param kc
	 * @param declaredTypePkg
	 * @param declaredType
	 * @return
	 * @throws Exception
	 */
	public static FactType getFactType(KieContainer kc, String declaredTypePkg, String declaredType) throws Exception {
		return kc.getKieBase().getFactType(declaredTypePkg, declaredType);
	}

}
