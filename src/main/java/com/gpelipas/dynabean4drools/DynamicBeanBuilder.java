/**
 * 
 */
package com.gpelipas.dynabean4drools;

import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Dynamic Bean Class Builder
 * 
 * @author gpelipas
 *
 */
public class DynamicBeanBuilder {

	private Class<?> superClass;

	private Map<String, Class<?>> fields;

	public DynamicBeanBuilder withSuperClass(Class<?> superClz) {
		this.superClass = superClz;
		return this;
	}

	public DynamicBeanBuilder withClassFields(Map<String, Class<?>> flds) {
		this.fields = flds;
		return this;
	}

	/**
	 * Returns a new class out from specified build parameters
	 * 
	 * @param className
	 * @return
	 * @throws Exception
	 */
	public Class<?> build(String className) throws Exception {
		return build(className, null);
	}

	/**
	 * Returns a new class out from specified build parameters
	 * 
	 * @param className
	 * @param saveToPath
	 * @return
	 * @throws Exception
	 */
	public Class<?> build(String className, String saveToPath) throws Exception {
		try {
			ClassPool pool = ClassPool.getDefault();
			CtClass cc = pool.makeClass(className);

			if (superClass != null) {
				cc.setSuperclass(resolveCtClass(superClass));
			}

			if (fields != null) {
				for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {
					cc.addField(new CtField(resolveCtClass(entry.getValue()), entry.getKey(), cc));
					cc.addMethod(generateGetter(cc, entry.getKey(), entry.getValue()));
					cc.addMethod(generateSetter(cc, entry.getKey(), entry.getValue()));
				}
			}

			if (saveToPath != null && !saveToPath.isEmpty()) {
				cc.writeFile(saveToPath);
			}

			return cc.toClass();

		} catch (Exception e) {
			throw new Exception("Error occurred while building class - " + className, e);
		}
	}

	/**
	 * Resolve Class
	 * 
	 * @param clazz
	 * @return
	 * @throws NotFoundException
	 */
	private CtClass resolveCtClass(Class<?> clazz) throws NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		return pool.get(clazz.getName());
	}

	private CtMethod generateGetter(CtClass hostClz, String fieldName, Class<?> fieldTypeCls)
			throws CannotCompileException {
		final String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

		StringBuilder sb = new StringBuilder();
		sb.append("public ").append(fieldTypeCls.getName()).append(" ").append(methodName).append("() { ");
		sb.append("return this.").append(fieldName).append(";");
		sb.append(" }");

		return CtMethod.make(sb.toString(), hostClz);
	}

	private CtMethod generateSetter(CtClass hostClz, String fieldName, Class<?> fieldTypeCls)
			throws CannotCompileException {
		final String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

		StringBuilder sb = new StringBuilder();
		sb.append("public void ").append(methodName).append("(").append(fieldTypeCls.getName()).append(" fldName")
				.append(") { ");
		sb.append("this.").append(fieldName).append(" = fldName;");
		sb.append(" }");

		return CtMethod.make(sb.toString(), hostClz);
	}
}