package com.vaadin.starter.bakery.testbench;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.starter.bakery.testbench.elements.ui.DashboardViewElement;
import com.vaadin.starter.bakery.testbench.elements.ui.LoginViewElement;
import com.vaadin.starter.bakery.testbench.elements.ui.StorefrontViewElement;
import com.vaadin.testbench.BrowserTest;

public class LoginIT extends AbstractIT<LoginViewElement> {

	@BrowserTest
	public void loginWorks() {
		LoginViewElement loginView = openLoginView();
		Assertions.assertEquals("Email", loginView.getUsernameLabel());
		loginView.login("barista@vaadin.com", "barista");
	}

	@BrowserTest
	public void logout() {
		LoginViewElement loginView = openLoginView();
		StorefrontViewElement storefront = loginView.login("barista@vaadin.com", "barista");
		storefront.getMenu().logout();
		Assertions.assertTrue(getDriver().getCurrentUrl().endsWith("login"));
	}

	@BrowserTest
	public void loginToNotDefaultUrl() {
		LoginViewElement loginView = openLoginView(getDriver(), APP_URL + "dashboard");
		DashboardViewElement dashboard = loginView.login("admin@vaadin.com", "admin", DashboardViewElement.class);
		Assertions.assertNotNull(dashboard);
	}

	@BrowserTest
	public void openLoginAfterLoggedIn() {
		loginToNotDefaultUrl();
		// Navigating to /login after user is logged in will forward to storefront view
		getDriver().get(APP_URL + "login");
		$(StorefrontViewElement.class).onPage().waitForFirst();
		Assertions.assertTrue($(LoginViewElement.class).all().isEmpty());
	}

	@Override
	protected LoginViewElement openView() {
		return openLoginView();
	}

}