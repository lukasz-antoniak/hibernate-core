/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.service.jta.platform.internal;

import java.util.Arrays;
import java.util.Collection;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * {@link org.hibernate.service.jta.platform.spi.JtaPlatform} implementation for Weblogic
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class WeblogicJtaPlatform extends AbstractJtaPlatform {
	public static final String TM_NAME = "javax.transaction.TransactionManager";
	public static final String UT_NAME = "javax.transaction.UserTransaction";

	@Override
	protected TransactionManager locateTransactionManager() {
		return (TransactionManager) jndiService().locate( TM_NAME );
	}

	@Override
	protected UserTransaction locateUserTransaction() {
		return (UserTransaction) jndiService().locate( UT_NAME );
	}

	@Override
	public Collection<String> getCharacteristicClassNames() {
		// Tested on WebLogic 11g (10.3.4.0) and 12c (12.1.1.0).
		return Arrays.asList( "weblogic.Server", "com.oracle.classloader.launch.Launcher" );
	}
}
