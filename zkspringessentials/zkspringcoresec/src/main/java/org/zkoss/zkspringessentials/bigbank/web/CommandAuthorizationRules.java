package org.zkoss.zkspringessentials.bigbank.web;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized command authorization rule table — the ZK equivalent of Spring Security's
 * {@code requestMatchers}. Maps (ViewModel class, command name) to required roles.
 *
 * <p>Used by {@link AuthPhaseListener} to enforce authorization before any {@code @Command}
 * executes. ViewModels not listed here are allowed through without restriction.
 */
@Service
public class CommandAuthorizationRules {

    // Map<ViewModel class, Map<command name, comma-separated required roles>>
    // The special key "*" acts as a wildcard fallback for any unmatched command.
    private final Map<Class<?>, Map<String, String>> rules = new HashMap<>();

    public CommandAuthorizationRules() {
        Map<String, String> vm4Rules = Map.of(
                "adjustBalance", "ROLE_SUPERVISOR,ROLE_TELLER",
                "*", "ROLE_USER");
        rules.put(BigbankViewModel4.class, vm4Rules);
    }

    /**
     * Returns the required roles (comma-separated) for the given ViewModel class and command.
     * Empty optional means no rule is registered → allow through.
     */
    public Optional<String> getRequiredRoles(Class<?> viewModelClass, String commandName) {
        Map<String, String> classRules = rules.get(viewModelClass);
        if (classRules == null) return Optional.empty();
        String specific = classRules.get(commandName);
        if (specific != null) return Optional.of(specific);
        return Optional.ofNullable(classRules.get("*"));
    }
}
