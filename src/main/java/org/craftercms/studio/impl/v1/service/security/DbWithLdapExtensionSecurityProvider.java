/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.studio.api.v1.dal.User;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class DbWithLdapExtensionSecurityProvider extends DbSecurityProvider {

    private final static Logger logger = LoggerFactory.getLogger(DbWithLdapExtensionSecurityProvider.class);

    @Override
    public String authenticate(String username, String password) {

        // Mapper for user data if user is successfully authenticated
        AuthenticatedLdapEntryContextMapper<User> mapper = new AuthenticatedLdapEntryContextMapper<User>() {
            @Override
            public User mapWithContext(DirContext dirContext, LdapEntryIdentification ldapEntryIdentification) {
                try {
                    // User entry - extract attributes
                    DirContextOperations dirContextOperations = (DirContextOperations)dirContext.lookup(ldapEntryIdentification.getRelativeName());
                    Attributes attributes = dirContextOperations.getAttributes();
                    String emailAttribName = studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_EMAIL);
                    String firstNameAttribName = studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_FIRST_NAME);
                    String lastNameAttribName = studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_LAST_NAME);
                    Attribute emailAttrib = attributes.get(emailAttribName);
                    Attribute firstNameAttrib = attributes.get(firstNameAttribName);
                    Attribute lastNameAttrib = attributes.get(lastNameAttribName);

                    User user = new User();
                    user.setActive(1);
                    user.setUsername(username);

                    if (emailAttrib != null && emailAttrib.get() != null) {
                        user.setEmail(emailAttrib.get().toString());
                    } else {
                        logger.error("No LDAP attribute " + emailAttribName + " found for username " + username + ". User will " +
                                     "not be imported into DB.");
                        return null;
                    }
                    if (firstNameAttrib != null && firstNameAttrib.get() != null) {
                        user.setFirstname(firstNameAttrib.get().toString());
                    } else {
                        logger.warn("No LDAP attribute " + firstNameAttribName + " found for username " + username);
                    }
                    if (lastNameAttrib != null && lastNameAttrib.get() != null) {
                        user.setLastname(lastNameAttrib.get().toString());
                    } else {
                        logger.warn("No LDAP attribute " + lastNameAttribName + " found for username " + username);
                    }

                    return user;
                } catch (NamingException e) {
                    logger.error("Error getting details from LDAP for username " + username, e);

                    return null;
                }
            }
        };

        // Create ldap query to authenticate user
        LdapQuery ldapQuery = query().where(studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_USERNAME)).is(username);
        User user = null;
        try {
            user = ldapTemplate.authenticate(ldapQuery, password, mapper);
        } catch (EmptyResultDataAccessException e) {
            logger.error("User " + username + " not found with external security provider. Trying to authenticate against studio database");
            // When user not found try to authenticate against studio database
            return super.authenticate(username, password);
        } catch (Exception e) {
            logger.error("LDAP authentication failed: ", e);
        }
        if (user != null) {
            // When user authenticated against LDAP, upsert user data into studio database
            boolean toRet = true;
            if (super.userExists(username)) {
                try {
                    updateUserInternal(user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail());
                } catch (UserNotFoundException e) {
                    logger.error("Error updating user " + username + " with data from external authentication provider", e);
                }
            } else {
                try {
                    createUser(user.getUsername(), password, user.getFirstname(), user.getLastname(), user.getEmail(), true);
                } catch (UserAlreadyExistsException e) {
                    logger.error("Error adding user " + username + " from external authentication provider", e);
                }
            }

            String token = createToken(user);
            storeSessionTicket(token);
            storeSessionUsername(username);
            return token;
        }

        return null;
    }

    private boolean updateUserInternal(String username, String firstName, String lastName, String email) throws UserNotFoundException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", username);
            params.put("firstname", firstName);
            params.put("lastname", lastName);
            params.put("email", email);
            params.put("externallyManaged", 1);
            securityMapper.updateUser(params);
            return true;
        }
    }

    public LdapTemplate getLdapTemplate() { return ldapTemplate; }
    public void setLdapTemplate(LdapTemplate ldapTemplate) { this.ldapTemplate = ldapTemplate; }

    protected LdapTemplate ldapTemplate;
}
