/**
 * 
 */
package com.gpelipas.dynabean4drools;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

/**
 * <b>NOTE</b> Since Introduction of Java Platform Module System in Java 9 a
 * strange error will be thrown if JVM parameter below is not added:
 * <p>
 * --add-opens java.base/java.lang=ALL-UNNAMED
 * </p>
 * 
 * @author gpelipas
 *
 */
public class TestDynamicPojoOnDrools {
	
	private ObjectMapper mapper = new ObjectMapper();

	private static Class dynamicPojoCls;

	private static Map<String, Class<?>> dynamicPojoClsFields;

	private static final String DRL_CLSPKG = "com.gpelipas.test";

	@BeforeClass
	public static void init() throws Exception {
		setDroolsDateFormat();
		
		dynamicPojoClsFields = new HashMap<String, Class<?>>();
		dynamicPojoClsFields.put("age", Integer.class);
		dynamicPojoClsFields.put("fullName", String.class);
		dynamicPojoClsFields.put("birthDate", Date.class);
		dynamicPojoClsFields.put("non_camelcase_field", String.class);

		final String clsFullName = DRL_CLSPKG.concat(".DynamicPojo");

		dynamicPojoCls = new DynamicBeanBuilder()
				.withClassFields(dynamicPojoClsFields)
				.withSuperClass(SampleBaseBean.class).build(clsFullName);
	}

	private static void setDroolsDateFormat() {
		System.setProperty("drools.dateformat", "yyyy-MM-dd");
	}

	@Test
	public void givenDynamicPojo_whenDynamicValueMatch_thenCorrect() throws Exception {
		Calendar dt = new GregorianCalendar(2011, Calendar.OCTOBER, 24);

		Map<String, Object> beanPropValues = new HashMap<>();
		beanPropValues.put("fullName", "Zoe");
		beanPropValues.put("birthDate", dt.getTime());

		Object dynClsInstance = dynamicPojoCls.getDeclaredConstructor().newInstance();

		BeanUtils.populate(dynClsInstance, beanPropValues);

		Assert.assertTrue(PropertyUtils.getProperty(dynClsInstance, "fullName").equals("Zoe"));
	}

	@Test
	public void givenDynamicPojo_whenRuleProcessSuccessfully_thenCorrect() throws Exception {
		Calendar dt = new GregorianCalendar(2021, Calendar.MARCH, 29);

		Map<String, Object> beanPropValues = new HashMap<>();
		beanPropValues.put("fullName", "Coco");
		beanPropValues.put("birthDate", dt.getTime());

		Object dynClsInstance = dynamicPojoCls.getDeclaredConstructor().newInstance();

		BeanUtils.populate(dynClsInstance, beanPropValues);

		String drl = createDroolScript(dynClsInstance.getClass(), "( birthDate == \"2021-03-29\" )");
		executeDrlStript2(drl, dynClsInstance);

		Assert.assertTrue(PropertyUtils.getProperty(dynClsInstance, "non_camelcase_field").equals("HelloWorld"));
	}
	
	@Test
	public void givenDynamicPojoAndJSONValues_whenRuleProcessSuccessfully_thenCorrect() throws Exception {
		Calendar dt = new GregorianCalendar(2011, Calendar.OCTOBER, 24);

		String jsonValueSource = "{ \"fullName\" : \"Marissa\", \"age\" : 35 }";

		Object dynClsInstance = mapper.readValue(jsonValueSource, dynamicPojoCls);

		String drl = createDroolScript(dynClsInstance.getClass(), "( age >= 30 )");
		executeDrlStript2(drl, dynClsInstance);

		Assert.assertTrue(PropertyUtils.getProperty(dynClsInstance, "non_camelcase_field").equals("HelloWorld"));
	}

	private static String createDroolScript(Class dynaCls, String expression) {
		final StringBuilder drl = new StringBuilder();

		drl.append("\n");
		drl.append("package ").append(DRL_CLSPKG).append("; ");
		drl.append("\n");
		drl.append("import org.apache.commons.beanutils.PropertyUtils; \n");
		drl.append("\n");
		drl.append("dialect \"mvel\" "); // fixes - cannot be resolved to a type
		drl.append("\n\n");
		drl.append("rule ").append("\"myRule").append(drl.hashCode()).append("\" ");
		drl.append("\n");
		drl.append("no-loop true ");
		drl.append("\n");
		drl.append("lock-on-active true");
		drl.append("\n");
		drl.append("when pojo : ").append(dynaCls.getName()).append(" (").append(expression).append(") \n");
		drl.append("then \n");
		drl.append(" System.out.println(\"Dynamic POJO Demo on Drools\"); \n");
		drl.append(" System.out.println(\"Hello there \" + PropertyUtils.getProperty(pojo, \"fullName\")); \n");
		drl.append(" PropertyUtils.setProperty(pojo, \"non_camelcase_field\", \"HelloWorld\"); \n");
		drl.append(" update(pojo); \n");
		drl.append("end \n");

		//System.out.println(drl.toString());

		return drl.toString();
	}

	private static void executeDrlStript2(String script, Object o) throws Exception {
		KieSession ks = null;
		try {
			ks = DroolsUtil.createStatefullKieSession(script);
			ks.insert(o);
			ks.fireAllRules();
		} catch (Exception e) {
			throw e;
		} finally {
			DroolsUtil.closeStatefullKieSession(ks);
		}
	}

	private static void executeDrlStript1(String script, Object o) throws Exception {
		StatelessKieSession sks = DroolsUtil.createStatelessKieSession(script);
		sks.execute(o);
	}

	@Data
	public static class SampleBaseBean implements Serializable {

		private static final long serialVersionUID = 7599382275643039834L;

		private Long id;

	}

}
