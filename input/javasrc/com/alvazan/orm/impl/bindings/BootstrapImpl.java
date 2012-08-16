package com.alvazan.orm.impl.bindings;

import java.util.Map;

import com.alvazan.orm.api.base.Bootstrap;
import com.alvazan.orm.api.base.DbTypeEnum;
import com.alvazan.orm.api.base.NoSqlEntityManagerFactory;
import com.alvazan.orm.api.spi3.meta.DboDatabaseMeta;
import com.alvazan.orm.api.spi3.meta.NoSqlSessionFactory;
import com.alvazan.orm.api.spi3.meta.conv.Converter;
import com.alvazan.orm.api.spi9.db.NoSqlRawSession;
import com.alvazan.orm.layer0.base.BaseEntityManagerFactoryImpl;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class BootstrapImpl extends Bootstrap {

	@SuppressWarnings("rawtypes")
	@Override
	public NoSqlEntityManagerFactory createInstance(DbTypeEnum type, Map<String, Object> properties, Map<Class, Converter> converters, ClassLoader cl2) {
		Injector injector = Guice.createInjector(new ProductionBindings(type));
		NoSqlEntityManagerFactory factory = injector.getInstance(NoSqlEntityManagerFactory.class);

		Named named = Names.named("logger");
		Key<NoSqlRawSession> key = Key.get(NoSqlRawSession.class, named);
		NoSqlRawSession inst = injector.getInstance(key);
		inst.start(properties);
		
		BaseEntityManagerFactoryImpl impl = (BaseEntityManagerFactoryImpl)factory;
		impl.setInjector(injector);
		
		ClassLoader cl = cl2;
		if(cl == null)
			cl = BootstrapImpl.class.getClassLoader();
		//The expensive scan all entities occurs here...
		impl.setup(properties, converters, cl);
		
		return impl;
	}

	/**
	 * A raw interface for non-ORM situations where flush can still be used to send all addIndex,removeIndex and persists, removes
	 * all at the same time.  This is especially useful as if addIndex fails BEFORE you flush, nothing is sent(no persists, removes)
	 * so there is nothing to really resolve.  We try to fail fast in persist, remove, addIndex, and removeIndex.  Then at flush
	 * time, we actually write out everything (at which point stuff can fail leading to inconsistency that needs to be cleaned up,
	 * but we will log everything that was being written on failure and what was success and what failed)
	 * 
	 * @param type
	 * @param metaDb 
	 * @return
	 */
	public static NoSqlSessionFactory createRawInstance(DbTypeEnum type, DboDatabaseMeta metaDb) {
		Injector injector = Guice.createInjector(new ProductionBindings(type, metaDb));
		return injector.getInstance(NoSqlSessionFactory.class);
	}
}