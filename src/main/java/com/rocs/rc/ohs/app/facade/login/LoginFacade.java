package com.rocs.rc.ohs.app.facade.login;


import com.rocs.rc.ohs.app.model.login.Login;

public interface LoginFacade {

    Login login (String username, String password);
}
