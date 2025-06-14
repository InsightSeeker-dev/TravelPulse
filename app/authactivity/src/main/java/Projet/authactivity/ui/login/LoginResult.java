package Projet.authactivity.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Integer error;
    @Nullable
    private String errorMessage;

    LoginResult(@Nullable Integer error) {
        this.error = error;
        this.errorMessage = null;
    }

    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
        this.error = null;
        this.errorMessage = null;
    }

    LoginResult(@Nullable Integer error, @Nullable String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }

    @Nullable
    LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }

    @Nullable
    String getErrorMessage() {
        return errorMessage;
    }
}