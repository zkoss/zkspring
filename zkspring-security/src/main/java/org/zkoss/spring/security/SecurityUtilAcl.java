package org.zkoss.spring.security;

import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.UiException;

import java.util.*;

public class SecurityUtilAcl {
	private static ApplicationContext _applicationContext;
	private static AclService _aclService;
	private static ObjectIdentityRetrievalStrategy _objectIdentityRetrievalStrategy;
	private static SidRetrievalStrategy _sidRetrievalStrategy;
	private static PermissionFactory permissionFactory;

	private static List<Permission> parsePermissions(String permissionsString) {
		final Set<Permission> permissions = new HashSet<Permission>();
		final StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(permissionsString, ",", false);

		while (tokenizer.hasMoreTokens()) {
			String permission = tokenizer.nextToken();
			try {
				permissions.add(permissionFactory.buildFromMask(Integer.valueOf(permission)));
			} catch (NumberFormatException nfe) {
				// Not an integer mask. Try using a name
				permissions.add(permissionFactory.buildFromName(permission));
			}
		}
		return new ArrayList<Permission>(permissions);
	}

	private static void initializeIfRequired() {
		if (_applicationContext != null) {
			return;
		}

		_applicationContext = SpringUtil.getApplicationContext();

		Map map = new HashMap();
		ApplicationContext context = _applicationContext;

		while (context != null) {
			map.putAll(context.getBeansOfType(AclService.class));
			context = context.getParent();
		}

		if (map.size() != 1) {
			throw new UiException(
					"Found incorrect number of AclService instances in application context - you must have only have one!");
		}

		_aclService = (AclService) map.values().iterator().next();

		map = _applicationContext.getBeansOfType(SidRetrievalStrategy.class);

		if (map.size() == 0) {
			_sidRetrievalStrategy = new SidRetrievalStrategyImpl();
		} else if (map.size() == 1) {
			_sidRetrievalStrategy = (SidRetrievalStrategy) map.values().iterator().next();
		} else {
			throw new UiException("Found incorrect number of SidRetrievalStrategy instances in application "
					+ "context - you must have only have one!");
		}

		map = _applicationContext.getBeansOfType(ObjectIdentityRetrievalStrategy.class);

		if (map.size() == 0) {
			_objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();
		} else if (map.size() == 1) {
			_objectIdentityRetrievalStrategy = (ObjectIdentityRetrievalStrategy) map.values().iterator().next();
		} else {
			throw new UiException("Found incorrect number of ObjectIdentityRetrievalStrategy instances in "
					+ "application context - you must have only have one!");
		}

		map = _applicationContext.getBeansOfType(PermissionFactory.class);

		if (map.size() == 0) {
			permissionFactory = new DefaultPermissionFactory();
		} else if (map.size() == 1) {
			permissionFactory = (PermissionFactory) map.values().iterator().next();
		} else {
			throw new UiException("Found incorrect number of PermissionFactory instances in "
					+ "application context - you must have only have one!");
		}
	}


	/**
	 * Return true if the current Authentication has one of the specified
	 * permissions to the presented	domain object instance.
	 *
	 * @param hasPermission A comma separated list of integers, each
	 *  representing a required bit mask permission from a subclass of
	 * {@link org.springframework.security.acls.domain.BasePermission}.
	 * @param domainObject The actual domain object instance for which permissions
	 *	are being evaluated.
	 * @return true if current Authentication has one of the specified permission
	 *  to the presented domain object instance.
	 */
	public static boolean isAccessible(String hasPermission, Object domainObject) {

		if (hasPermission == null || "".equals(hasPermission)) {
			return false;
		}
		initializeIfRequired();

		final List<Permission> requiredPermissions = parsePermissions(hasPermission);

		if (domainObject == null) {
			// Of course they have access to a null object!
			return true;
		}

		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			//SecurityContextHolder did not return a non-null Authentication object, so skipping tag body
			return false;
		}

		List<Sid> sids = _sidRetrievalStrategy.getSids(SecurityContextHolder.getContext().getAuthentication());
		ObjectIdentity oid = _objectIdentityRetrievalStrategy.getObjectIdentity(domainObject);

		// Obtain aclEntrys applying to the current Authentication object
		try {
			final Acl acl = _aclService.readAclById(oid, sids);
			return acl.isGranted(requiredPermissions, sids, false);
		} catch (NotFoundException nfe) {
			return false;
		}
	}
}
