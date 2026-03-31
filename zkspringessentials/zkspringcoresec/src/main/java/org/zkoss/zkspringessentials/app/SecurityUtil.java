package org.zkoss.zkspringessentials.app;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.acls.model.*;
import org.springframework.security.acls.domain.*;
import org.zkoss.spring.SpringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for ZK spring security integration.
 */
public class SecurityUtil {

    /**
     * Returns the current Authentication object from the SecurityContext.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Returns true if the current user has any of the specified roles (comma-separated).
     */
    public static boolean isAnyGranted(String authorities) {
        if (authorities == null || authorities.isEmpty()) return false;
        
        Set<String> requiredRoles = parseAuthorities(authorities);
        Collection<? extends GrantedAuthority> userAuthorities = getPrincipalAuthorities();
        
        return userAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredRoles::contains);
    }

    /**
     * Returns true if the current user has all of the specified roles.
     */
    public static boolean isAllGranted(String authorities) {
        if (authorities == null || authorities.isEmpty()) return false;
        
        Set<String> requiredRoles = parseAuthorities(authorities);
        Set<String> userRoles = getPrincipalAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        return userRoles.containsAll(requiredRoles);
    }

    /**
     * Returns true if the current user has NONE of the specified roles.
     */
    public static boolean isNoneGranted(String authorities) {
        return !isAnyGranted(authorities);
    }

    /**
     * ACL check: Returns true if the current user has the specified permission for a domain object.
     * Permissions can be bit masks (1, 2, 4, 8, 16) or names (READ, WRITE, CREATE, DELETE, ADMINISTRATION).
     */
//    public static boolean isAccessible(String hasPermission, Object domainObject) {
//        if (hasPermission == null || hasPermission.isEmpty() || domainObject == null) {
//            return true;
//        }

//        Authentication auth = getAuthentication();
//        if (auth == null) return false;
//
//        AclService aclService = SpringUtil.getBean(AclService.class);
//        ObjectIdentityRetrievalStrategy oidStrategy = new ObjectIdentityRetrievalStrategyImpl();
//        SidRetrievalStrategy sidStrategy = new SidRetrievalStrategyImpl();
//        PermissionFactory permissionFactory = new DefaultPermissionFactory();
//
//        List<Permission> requiredPermissions = Arrays.stream(hasPermission.split(","))
//                .map(String::trim)
//                .map(p -> {
//                    try {
//                        return permissionFactory.buildFromMask(Integer.parseInt(p));
//                    } catch (NumberFormatException e) {
//                        return permissionFactory.buildFromName(p);
//                    }
//                }).collect(Collectors.toList());
//
//        ObjectIdentity oid = oidStrategy.getObjectIdentity(domainObject);
//        List<Sid> sids = sidStrategy.getSids(auth);
//
//        try {
//            Acl acl = aclService.readAclById(oid, sids);
//            return acl.isGranted(requiredPermissions, sids, false);
//        } catch (NotFoundException nfe) {
//            return false;
//        }
//    }

    private static Collection<? extends GrantedAuthority> getPrincipalAuthorities() {
        Authentication auth = getAuthentication();
        return (auth != null) ? auth.getAuthorities() : Collections.emptyList();
    }

    private static Set<String> parseAuthorities(String authoritiesString) {
        return Arrays.stream(authoritiesString.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}
