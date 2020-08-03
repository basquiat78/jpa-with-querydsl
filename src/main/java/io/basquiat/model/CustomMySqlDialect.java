package io.basquiat.model;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CustomMySqlDialect extends MySQL8Dialect {
	
	public CustomMySqlDialect() {
		
		super();
		registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
	}

}
