package com.ettrema.secure;

import org.jasypt.digest.config.SimpleDigesterConfig;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;

/**
 *
 */
public class PasswordManager {

    private final ConfigurablePasswordEncryptor passwordEncryptor;

    public PasswordManager() {
        passwordEncryptor = new ConfigurablePasswordEncryptor();
        SimpleDigesterConfig config = new SimpleDigesterConfig();
        config.setIterations(10);
        passwordEncryptor.setConfig(config);
    }

    public PasswordManager(ConfigurablePasswordEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }

    public String digestPassword(String password) {
        return passwordEncryptor.encryptPassword(password);
    }

    public boolean checkPassword(String givenPassword, String passwordHash) {
        return passwordEncryptor.checkPassword(givenPassword, passwordHash);
    }
}
