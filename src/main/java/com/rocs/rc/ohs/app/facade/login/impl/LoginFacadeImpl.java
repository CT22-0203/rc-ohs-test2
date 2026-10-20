package com.rocs.rc.ohs.app.facade.login.impl;


import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.dao.login.LoginDao;
import com.rocs.rc.ohs.data.dao.login.impl.LoginDaoImpl;

public class LoginFacadeImpl implements LoginFacade {

    private final LoginDao loginDao = new LoginDaoImpl();

    @Override
    public Login login(String username, String password) {
        return this.loginDao.login(username);
    }
}
