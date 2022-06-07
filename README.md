# dynabeans4drools
Applying dynamic POJO Facts for Drools

Sample Code on how to apply dynamic POJO or Beans in Drools 

```java
  
  Map<String, Class<?>> dynamicPojoClsFields = new HashMap<>();
  dynamicPojoClsFields.put("age", Integer.class);
  dynamicPojoClsFields.put("fullName", String.class);
  dynamicPojoClsFields.put("birthDate", Date.class);
  dynamicPojoClsFields.put("is_non_camel_case", Boolean.class);  
  
  // use my utility to create POJO class
  Class dynamicPojoCls = new DynamicBeanBuilder()
				.withClassFields(dynamicPojoClsFields)
				.withSuperClass(SampleBaseBean.class).build("com.whateverpkg.MyPojo");
  
  Map<String, Object> beanPropValues = new HashMap<>();
  beanPropValues.put("fullName", "Zoe");
  beanPropValues.put("age", 11);
  
  // plain java create instance by reflection
  Object dynaPojoInstance = dynamicPojoCls.getDeclaredConstructor().newInstance();
  
  // use any tools to populate values inside bean 
  BeanUtils.populate(dynaPojoInstance, beanPropValues);
  
  String rule = "( age <= 20 )";
  
  // DRL script
  final StringBuilder drl = new StringBuilder();
  drl.append("package ").append(dynamicPojoCls.getPackageName()).append("; ");
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
  drl.append("when pojo : ").append(dynaCls.getName()).append(" (").append(rule).append(") \n");
  drl.append("then \n");
  drl.append(" System.out.println(\"Dynamic POJO Demo on Drools\"); \n");
  drl.append(" System.out.println(\"Hello there \" + PropertyUtils.getProperty(pojo, \"fullName\")); \n");
  drl.append(" PropertyUtils.setProperty(pojo, \"is_non_camel_case\", true); \n");
  drl.append(" update(pojo); \n");
  drl.append("end \n");  
  
  // use my drools factory utility
  StatelessKieSession sks = DroolsUtil.createStatelessKieSession(drl.toString());
  sks.execute(dynaPojoInstance);
  
  
```
